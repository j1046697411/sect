/**
 * 任务执行服务
 *
 * 提供任务执行管理功能：
 * - 计算任务成功率
 * - 计算伤亡人数
 * - 执行任务并返回结果
 */
package cn.jzl.sect.quest.services

import cn.jzl.di.instance
import cn.jzl.ecs.World
import cn.jzl.ecs.editor
import cn.jzl.ecs.entity.Entity
import cn.jzl.ecs.entity.EntityRelationContext
import cn.jzl.ecs.entity.addComponent
import cn.jzl.ecs.entity.id
import cn.jzl.ecs.query
import cn.jzl.ecs.query.EntityQueryContext
import cn.jzl.ecs.query.forEach
import cn.jzl.log.Logger
import cn.jzl.sect.core.cultivation.Realm
import cn.jzl.sect.cultivation.components.CultivationProgress
import cn.jzl.sect.quest.components.ExecutionResult
import cn.jzl.sect.quest.components.QuestComponent
import cn.jzl.sect.quest.components.QuestDifficulty
import cn.jzl.sect.quest.components.QuestExecutionComponent
import cn.jzl.sect.quest.components.QuestStatus
import kotlin.random.Random
import kotlin.time.Clock

/**
 * 任务执行服务
 *
 * 提供任务执行管理功能的核心服务：
 * - 计算任务成功率
 * - 计算伤亡人数
 * - 执行任务并返回结果
 *
 * 使用方式：
 * ```kotlin
 * val questExecutionService by world.di.instance<QuestExecutionService>()
 * val result = questExecutionService.executeQuest(questId)
 * ```
 *
 * @property world ECS 世界实例
 */
class QuestExecutionService(override val world: World) : EntityRelationContext {

    private val log: Logger by world.di.instance(argProvider = { "QuestExecutionService" })
    private val random = Random.Default

    /**
     * 计算任务成功率
     *
     * @param difficulty 任务难度
     * @param team 团队组成
     * @return 成功率（0.0 - 1.0）
     */
    fun calculateSuccessRate(difficulty: QuestDifficulty, team: TeamFormationResult): Double {
        log.debug { "开始计算任务成功率" }
        if (!team.success || team.elder == null) {
            log.debug { "计算任务成功率完成: 团队组建失败，成功率 0.0" }
            return 0.0
        }

        // 基础成功率
        val baseRate = when (difficulty) {
            QuestDifficulty.EASY -> 0.8
            QuestDifficulty.NORMAL -> 0.6
            QuestDifficulty.HARD -> 0.4
        }

        // 计算团队实力加成
        val teamStrength = calculateTeamStrength(team)
        val strengthBonus = teamStrength * 0.3

        // 计算人数加成
        val memberCount = team.totalCount
        val countBonus = (memberCount / 25.0) * 0.1

        // 最终成功率
        val finalRate = baseRate + strengthBonus + countBonus
        val result = finalRate.coerceIn(0.1, 0.95)
        log.debug { "计算任务成功率完成: 成功率 $result" }
        return result
    }

    /**
     * 计算伤亡人数
     *
     * @param outerDisciples 外门弟子列表
     * @param difficulty 任务难度
     * @return 伤亡人数
     */
    fun calculateCasualties(outerDisciples: List<Entity>, difficulty: QuestDifficulty): Int {
        log.debug { "开始计算伤亡人数" }
        if (outerDisciples.isEmpty()) {
            log.debug { "计算伤亡人数完成: 无外门弟子，伤亡 0" }
            return 0
        }

        // 基础伤亡率
        val baseCasualtyRate = when (difficulty) {
            QuestDifficulty.EASY -> 0.05
            QuestDifficulty.NORMAL -> 0.15
            QuestDifficulty.HARD -> 0.3
        }

        // 随机波动
        val randomFactor = random.nextDouble(0.5, 1.5)
        val actualRate = baseCasualtyRate * randomFactor

        // 计算伤亡人数
        val casualties = (outerDisciples.size * actualRate).toInt()
        val result = casualties.coerceIn(0, outerDisciples.size)
        log.debug { "计算伤亡人数完成: 伤亡 $result" }
        return result
    }

    /**
     * 执行任务
     *
     * @param questId 任务ID
     * @return 执行结果
     */
    fun executeQuest(questId: Long): ExecutionResult {
        log.debug { "开始执行任务: questId=$questId" }
        // 查找任务
        val questQuery = world.query { QuestQueryContext(world) }
        var targetQuest: QuestComponent? = null
        var targetExecution: QuestExecutionComponent? = null
        var targetEntity: Entity? = null

        questQuery.forEach { ctx ->
            if (ctx.quest.questId == questId) {
                targetQuest = ctx.quest
                targetExecution = ctx.execution
                targetEntity = ctx.entity
            }
        }

        if (targetQuest == null || targetExecution == null || targetEntity == null) {
            return ExecutionResult(
                completionRate = 0.0f,
                efficiency = 0.0f,
                quality = 0.0f,
                survivalRate = 0.0f,
                casualties = 0
            )
        }

        // 构建团队
        val team = buildTeamFromExecution(targetExecution!!)

        // 计算成功率
        val successRate = calculateSuccessRate(targetQuest!!.difficulty, team)

        // 判断是否成功
        val isSuccess = random.nextDouble() < successRate

        // 计算伤亡
        val casualties = if (isSuccess) {
            calculateCasualties(team.outerDisciples, targetQuest!!.difficulty)
        } else {
            // 失败时伤亡更多
            (calculateCasualties(team.outerDisciples, targetQuest!!.difficulty) * 1.5).toInt()
                .coerceAtMost(team.outerDisciples.size)
        }

        // 计算各项指标
        val completionRate = if (isSuccess) {
            random.nextDouble(0.7, 1.0).toFloat()
        } else {
            random.nextDouble(0.2, 0.6).toFloat()
        }

        val efficiency = calculateEfficiency(team)
        val quality = if (isSuccess) {
            random.nextDouble(0.6, 1.0).toFloat()
        } else {
            random.nextDouble(0.1, 0.5).toFloat()
        }

        val survivalRate = if (team.outerDisciples.isEmpty()) {
            1.0f
        } else {
            (team.outerDisciples.size - casualties).toFloat() / team.outerDisciples.size
        }

        // 更新任务状态
        val newStatus = if (isSuccess) QuestStatus.COMPLETED else QuestStatus.CANCELLED
        world.editor(targetEntity!!) {
            it.addComponent(
                QuestComponent(
                    questId = targetQuest!!.questId,
                    type = targetQuest!!.type,
                    difficulty = targetQuest!!.difficulty,
                    status = newStatus,
                    createdAt = targetQuest!!.createdAt,
                    maxParticipants = targetQuest!!.maxParticipants,
                    description = targetQuest!!.description
                )
            )
            it.addComponent(
                QuestExecutionComponent(
                    questId = targetExecution!!.questId,
                    elderId = targetExecution!!.elderId,
                    innerDiscipleIds = targetExecution!!.innerDiscipleIds,
                    outerDiscipleIds = targetExecution!!.outerDiscipleIds,
                    progress = 100.0f,
                    startTime = targetExecution!!.startTime,
                    estimatedEndTime = Clock.System.now().toEpochMilliseconds()
                )
            )
        }

        val result = ExecutionResult(
            completionRate = completionRate,
            efficiency = efficiency,
            quality = quality,
            survivalRate = survivalRate,
            casualties = casualties
        )
        log.debug { "执行任务完成: questId=$questId, success=$isSuccess, casualties=$casualties" }
        return result
    }

    /**
     * 计算团队实力（0.0 - 1.0）
     */
    private fun calculateTeamStrength(team: TeamFormationResult): Double {
        var totalStrength = 0.0
        var memberCount = 0

        // 长老实力
        team.elder?.let { elder ->
            val cultivation = getEntityCultivation(elder)
            totalStrength += cultivation * 0.4
            memberCount++
        }

        // 内门弟子实力
        team.innerDisciples.forEach { disciple ->
            val cultivation = getEntityCultivation(disciple)
            totalStrength += cultivation * 0.35
            memberCount++
        }

        // 外门弟子实力
        team.outerDisciples.forEach { disciple ->
            val cultivation = getEntityCultivation(disciple)
            totalStrength += cultivation * 0.25
            memberCount++
        }

        return if (memberCount > 0) (totalStrength / memberCount).coerceIn(0.0, 1.0) else 0.0
    }

    /**
     * 获取实体的修炼实力（0.0 - 1.0）
     */
    private fun getEntityCultivation(entity: Entity): Double {
        val query = world.query { CultivationQueryContext(world) }
        var cultivationValue = 0.0

        query.forEach { ctx ->
            if (ctx.entity == entity) {
                cultivationValue = when (ctx.cultivation.realm) {
                    Realm.MORTAL -> 0.1 + (ctx.cultivation.layer / 10.0) * 0.2
                    Realm.QI_REFINING -> 0.3 + (ctx.cultivation.layer / 10.0) * 0.3
                    Realm.FOUNDATION -> 0.6 + (ctx.cultivation.layer / 10.0) * 0.4
                    Realm.GOLDEN_CORE -> 1.0 + (ctx.cultivation.layer / 10.0) * 0.5
                    Realm.NASCENT_SOUL -> 1.5 + (ctx.cultivation.layer / 10.0) * 0.6
                    Realm.SOUL_TRANSFORMATION -> 2.1 + (ctx.cultivation.layer / 10.0) * 0.7
                    Realm.TRIBULATION -> 2.8 + (ctx.cultivation.layer / 10.0) * 0.8
                    Realm.IMMORTAL -> 3.6
                }
            }
        }

        return cultivationValue.coerceIn(0.0, 1.0)
    }

    /**
     * 计算效率
     */
    private fun calculateEfficiency(team: TeamFormationResult): Float {
        // 基于团队人数和构成计算效率
        val baseEfficiency = 0.5f
        val elderBonus = if (team.elder != null) 0.15f else 0.0f
        val innerBonus = team.innerDisciples.size * 0.05f
        val outerBonus = team.outerDisciples.size * 0.01f

        return (baseEfficiency + elderBonus + innerBonus + outerBonus).coerceIn(0.0f, 1.0f)
    }

    /**
     * 从执行组件构建团队
     */
    private fun buildTeamFromExecution(execution: QuestExecutionComponent): TeamFormationResult {
        val elder = if (execution.elderId > 0) {
            findEntityById(execution.elderId.toInt())
        } else null

        val innerDisciples = execution.innerDiscipleIds.mapNotNull { findEntityById(it.toInt()) }
        val outerDisciples = execution.outerDiscipleIds.mapNotNull { findEntityById(it.toInt()) }

        return TeamFormationResult(
            success = elder != null && innerDisciples.isNotEmpty() && outerDisciples.isNotEmpty(),
            elder = elder,
            innerDisciples = innerDisciples,
            outerDisciples = outerDisciples
        )
    }

    /**
     * 根据ID查找实体
     */
    private fun findEntityById(id: Int): Entity? {
        val query = world.query { EntityIdQueryContext(world) }
        var foundEntity: Entity? = null

        query.forEach { ctx ->
            if (ctx.entity.id == id) {
                foundEntity = ctx.entity
            }
        }

        return foundEntity
    }

    /**
     * 查询上下文 - 任务
     */
    class QuestQueryContext(world: World) : EntityQueryContext(world) {
        val quest: QuestComponent by component()
        val execution: QuestExecutionComponent by component()
    }

    /**
     * 查询上下文 - 修炼
     */
    class CultivationQueryContext(world: World) : EntityQueryContext(world) {
        val cultivation: CultivationProgress by component()
    }

    /**
     * 查询上下文 - 实体ID
     */
    class EntityIdQueryContext(world: World) : EntityQueryContext(world)
}
