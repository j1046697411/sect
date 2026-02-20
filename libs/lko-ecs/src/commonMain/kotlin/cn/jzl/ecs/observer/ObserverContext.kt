package cn.jzl.ecs.observer

import cn.jzl.ecs.World
import cn.jzl.ecs.WorldOwner
import cn.jzl.ecs.entity.Entity
import cn.jzl.ecs.relation.Relation

/**
 * 观察者上下文接口
 *
 * ObserverContext 提供观察者在事件处理时访问相关信息的能力，
 * 包括触发事件的实体、涉及的关系以及世界实例。
 *
 * ## 使用场景
 * - 在观察者处理函数中访问触发事件的实体
 * - 获取事件涉及的关系信息
 * - 访问世界服务执行进一步操作
 *
 * ## 使用示例
 * ```kotlin
 * world.observe<OnUpdated>(Health::class).exec {
 *     println("Entity ${entity.id} health updated")
 *     println("Involved relation: $involvedRelation")
 * }
 * ```
 *
 * @see ObserverContextWithData 带事件数据的上下文
 */
interface ObserverContext : WorldOwner {
    /**
     * 关联的 ECS 世界
     */
    override val world: World

    /**
     * 触发事件的实体
     */
    val entity: Entity

    /**
     * 事件涉及的关系
     *
     * 如果事件不涉及特定关系，则为 [ObserveService.notInvolvedRelation]
     */
    val involvedRelation: Relation
}
