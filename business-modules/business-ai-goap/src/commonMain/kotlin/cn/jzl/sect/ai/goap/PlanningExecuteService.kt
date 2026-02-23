/**
 * GOAP 计划执行服务
 *
 * 负责 GOAP 计划的实际执行，支持：
 * - 任务调度和排队
 * - 协程支持异步任务
 * - 延迟执行
 * - 计划完成事件通知
 *
 * 使用示例：
 * ```kotlin
 * val executeService by world.di.instance<PlanningExecuteService>()
 *
 * // 执行计划（通过 PlanningService.execPlan 调用）
 * executeService.executePlan(agent, plan)
 *
 * // 监听计划完成事件
 * agent.observe<OnPlanExecutionCompleted>().exec {
 *     println("计划执行完成！")
 * }
 * ```
 */
package cn.jzl.sect.ai.goap

import cn.jzl.di.instance
import cn.jzl.ecs.Updatable
import cn.jzl.ecs.World
import cn.jzl.ecs.entity.Entity
import cn.jzl.ecs.entity.EntityRelationContext
import cn.jzl.ecs.observer.Observer
import cn.jzl.ecs.observer.emit
import cn.jzl.ecs.observer.observe
import cn.jzl.log.Logger
import cn.jzl.sect.common.countdown.CountdownService
import cn.jzl.sect.common.countdown.OnCountdownComplete
import cn.jzl.sect.common.time.TimeService
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.isActive
import kotlin.coroutines.Continuation
import kotlin.coroutines.ContinuationInterceptor
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.RestrictsSuspension
import kotlin.coroutines.createCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.time.Duration

/**
 * 计划执行完成事件标签
 *
 * 当计划执行完成时触发此事件
 */
sealed class OnPlanExecutionCompleted

/**
 * 任务调度器接口
 *
 * 用于将任务调度到主线程执行
 */
interface TaskDispatcher {
    /**
     * 调度任务执行
     *
     * @param task 要执行的任务
     */
    fun dispatch(task: Runnable)
}

/**
 * 计划执行服务
 *
 * 实现任务调度和计划执行的核心服务
 *
 * @property world ECS 世界实例
 */
class PlanningExecuteService(override val world: World) : EntityRelationContext, Updatable, TaskDispatcher {

    private val log: Logger by world.di.instance(argProvider = { "PlanningExecuteService" })

    private val waitingTasks = mutableListOf<Runnable>()
    private val activeTasks = mutableListOf<Runnable>()

    override fun dispatch(task: Runnable) {
        waitingTasks.add(task)
    }

    private fun <R> executeTask(agent: Entity, task: suspend EntityTaskContext.() -> R): Entity {
        return world.childOf(agent) {
            val entityPlanContext = EntityPlanContext<R>(world, this@PlanningExecuteService, agent, it)
            val duration = task.createCoroutine(entityPlanContext, entityPlanContext)
            dispatch { duration.resume(Unit) }
        }
    }

    /**
     * 执行计划
     *
     * 为智能体创建执行上下文并依次执行计划中的动作
     *
     * @param agent 智能体实体
     * @param plan 要执行的计划
     * @return 执行任务的调度实体
     */
    fun executePlan(agent: Entity, plan: Plan): Entity = executeTask(agent) {
        log.debug { "agent $agent, start exec ${plan.goal.name}" }
        plan.actions.forEach { it.task.run { exec() } }
        log.debug { "agent $agent, end exec ${plan.goal.name}" }
    }

    override fun update(delta: Duration) {
        activeTasks.clear()
        activeTasks.addAll(waitingTasks)
        waitingTasks.clear()
        activeTasks.forEach { it.run() }
        activeTasks.clear()
    }

    private class EntityPlanContext<R>(
        world: World,
        private val taskDispatcher: TaskDispatcher,
        agent: Entity,
        dispatcher: Entity
    ) : EntityTaskContext(world, agent, dispatcher), Continuation<R> {

        override val context: CoroutineContext get() = EmptyCoroutineContext

        override suspend fun <R> suspendTask(block: (Continuation<R>) -> Unit): R = suspendCoroutine {
            taskDispatcher.dispatch { block(it) }
        }

        override fun resumeWith(result: Result<R>) {
            world.destroy(dispatcher)
            world.emit<OnPlanExecutionCompleted>(agent)
        }
    }
}

/**
 * 实体任务上下文
 *
 * 提供任务执行的上下文环境，支持挂起函数
 *
 * @property world ECS 世界实例
 * @property agent 执行任务的智能体
 * @property dispatcher 任务调度实体
 */
@RestrictsSuspension
abstract class EntityTaskContext(
    override val world: World,
    val agent: Entity,
    val dispatcher: Entity
) : EntityRelationContext {
    internal val log: Logger by world.di.instance(argProvider = { "EntityTaskContext" })
    internal val timeService by world.di.instance<TimeService>()

    internal val countdownService by world.di.instance<CountdownService>()

    /**
     * 挂起任务执行
     *
     * @param R 返回值类型
     * @param block 挂起块，接收 Continuation 用于恢复执行
     * @return 挂起结果
     */
    abstract suspend fun <R> suspendTask(block: (Continuation<R>) -> Unit): R
}

/**
 * 延迟执行
 *
 * 在任务上下文中挂起指定时长
 *
 * @param delay 延迟时长
 */
suspend fun EntityTaskContext.delay(delay: Duration): Unit = suspendTask { continuation ->
    val currentTime = timeService.getCurrentGameTime()
    log.debug { "start delay $agent startTime = $currentTime, delay = $delay" }
    var observer: Observer? = null
    observer = world.observe<OnCountdownComplete>(dispatcher).exec {
        if (!continuation.context.isActive) return@exec
        continuation.resume(Unit)
        log.debug { "end delay $agent completeTime = ${timeService.getCurrentGameTime()}" }
        observer?.close()
    }
    countdownService.countdown(dispatcher, delay)
}

/**
 * 动作任务接口
 *
 * 定义动作执行的具体逻辑，支持挂起函数
 */
fun interface ActionTask {
    /**
     * 执行动作
     *
     * 在实体任务上下文中执行
     */
    suspend fun EntityTaskContext.exec()
}
