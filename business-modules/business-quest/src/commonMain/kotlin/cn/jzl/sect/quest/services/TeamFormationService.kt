/**
 * 团队组建服务
 *
 * 提供团队组建管理功能：
 * - 查找可用的长老
 * - 查找可用的内门弟子
 * - 查找可用的外门弟子
 * - 组建完整团队
 */
package cn.jzl.sect.quest.services

import cn.jzl.ecs.World
import cn.jzl.ecs.editor
import cn.jzl.ecs.entity.Entity
import cn.jzl.ecs.entity.EntityRelationContext
import cn.jzl.ecs.entity.addComponent
import cn.jzl.ecs.entity.id
import cn.jzl.ecs.family.FamilyBuilder
import cn.jzl.ecs.query
import cn.jzl.ecs.query.EntityQueryContext
import cn.jzl.ecs.query.forEach
import cn.jzl.sect.core.cultivation.CultivationProgress
import cn.jzl.sect.core.cultivation.Realm
import cn.jzl.sect.core.sect.SectPositionInfo
import cn.jzl.sect.core.sect.SectPositionType
import cn.jzl.sect.quest.components.QuestComponent
import cn.jzl.sect.quest.components.QuestExecutionComponent

/**
 * 团队组建服务
 *
 * 提供团队组建管理功能的核心服务：
 * - 查找可用的长老
 * - 查找可用的内门弟子
 * - 查找可用的外门弟子
 * - 组建完整团队
 *
 * 使用方式：
 * ```kotlin
 * val teamFormationService by world.di.instance<TeamFormationService>()
 * val team = teamFormationService.formTeam(questId)
 * ```
 *
 * @property world ECS 世界实例
 */
class TeamFormationService(override val world: World) : EntityRelationContext {

    /**
     * 查找可用的长老
     *
     * @return 找到的长老实体的ID，如果没有则返回null
     */
    fun findAvailableElder(): Entity? {
        val query = world.query { ElderQueryContext(world) }
        var selectedElder: Entity? = null
        var highestRealmValue = -1

        query.forEach { ctx ->
            // 只考虑职位为长老的实体
            if (ctx.position.position == SectPositionType.ELDER) {
                val realmValue = getRealmValue(ctx.cultivation.realm)
                if (realmValue > highestRealmValue) {
                    highestRealmValue = realmValue
                    selectedElder = ctx.entity
                }
            }
        }

        return selectedElder
    }

    /**
     * 查找可用的内门弟子
     *
     * @param min 最少人数（默认3）
     * @param max 最多人数（默认5）
     * @return 内门弟子实体列表，如果人数不足则返回空列表
     */
    fun findAvailableInnerDisciples(min: Int = 3, max: Int = 5): List<Entity> {
        val query = world.query { InnerDiscipleQueryContext(world) }
        val disciples = mutableListOf<Entity>()

        query.forEach { ctx ->
            // 只考虑职位为内门弟子的实体
            if (ctx.position.position == SectPositionType.DISCIPLE_INNER) {
                disciples.add(ctx.entity)
            }
        }

        // 如果人数不足，返回空列表
        if (disciples.size < min) {
            return emptyList()
        }

        // 随机选择min到max之间的人数
        val targetCount = (min..max.coerceAtMost(disciples.size)).random()
        return disciples.shuffled().take(targetCount)
    }

    /**
     * 查找可用的外门弟子
     *
     * @param min 最少人数（默认10）
     * @param max 最多人数（默认20）
     * @return 外门弟子实体列表，如果人数不足则返回空列表
     */
    fun findAvailableOuterDisciples(min: Int = 10, max: Int = 20): List<Entity> {
        val query = world.query { OuterDiscipleQueryContext(world) }
        val disciples = mutableListOf<Entity>()

        query.forEach { ctx ->
            // 只考虑职位为外门弟子的实体
            if (ctx.position.position == SectPositionType.DISCIPLE_OUTER) {
                disciples.add(ctx.entity)
            }
        }

        // 如果人数不足，返回空列表
        if (disciples.size < min) {
            return emptyList()
        }

        // 随机选择min到max之间的人数
        val targetCount = (min..max.coerceAtMost(disciples.size)).random()
        return disciples.shuffled().take(targetCount)
    }

    /**
     * 组建团队
     *
     * @param questId 任务ID
     * @return 团队组建结果
     */
    fun formTeam(questId: Long): TeamFormationResult {
        // 查找各类人员
        val elder = findAvailableElder()
        val innerDisciples = findAvailableInnerDisciples()
        val outerDisciples = findAvailableOuterDisciples()

        // 检查是否满足最低要求
        if (elder == null || innerDisciples.isEmpty() || outerDisciples.isEmpty()) {
            return TeamFormationResult(
                success = false,
                elder = null,
                innerDisciples = emptyList(),
                outerDisciples = emptyList()
            )
        }

        // 更新任务执行组件
        val query = world.query { QuestExecutionQueryContext(world) }
        query.forEach { ctx ->
            if (ctx.quest.questId == questId) {
                world.editor(ctx.entity) {
                    it.addComponent(
                        QuestExecutionComponent(
                            questId = questId,
                            elderId = elder.id.toLong(),
                            innerDiscipleIds = innerDisciples.map { d -> d.id.toLong() },
                            outerDiscipleIds = outerDisciples.map { d -> d.id.toLong() },
                            progress = 0.0f,
                            startTime = System.currentTimeMillis(),
                            estimatedEndTime = System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000 // 预计7天后完成
                        )
                    )
                }
            }
        }

        return TeamFormationResult(
            success = true,
            elder = elder,
            innerDisciples = innerDisciples,
            outerDisciples = outerDisciples
        )
    }

    /**
     * 获取境界的数值权重（用于比较境界高低）
     */
    private fun getRealmValue(realm: Realm): Int {
        return when (realm) {
            Realm.MORTAL -> 0
            Realm.QI_REFINING -> 1
            Realm.FOUNDATION -> 2
            Realm.GOLDEN_CORE -> 3
            Realm.NASCENT_SOUL -> 4
            Realm.SOUL_TRANSFORMATION -> 5
            Realm.TRIBULATION -> 6
            Realm.IMMORTAL -> 7
        }
    }

    /**
     * 查询上下文 - 长老
     */
    class ElderQueryContext(world: World) : EntityQueryContext(world) {
        val position: SectPositionInfo by component()
        val cultivation: CultivationProgress by component()

        override fun FamilyBuilder.configure() {
            component<SectPositionInfo>()
            component<CultivationProgress>()
        }
    }

    /**
     * 查询上下文 - 内门弟子
     */
    class InnerDiscipleQueryContext(world: World) : EntityQueryContext(world) {
        val position: SectPositionInfo by component()

        override fun FamilyBuilder.configure() {
            component<SectPositionInfo>()
        }
    }

    /**
     * 查询上下文 - 外门弟子
     */
    class OuterDiscipleQueryContext(world: World) : EntityQueryContext(world) {
        val position: SectPositionInfo by component()

        override fun FamilyBuilder.configure() {
            component<SectPositionInfo>()
        }
    }

    /**
     * 查询上下文 - 任务执行
     */
    class QuestExecutionQueryContext(world: World) : EntityQueryContext(world) {
        val quest: QuestComponent by component()
        val execution: QuestExecutionComponent by component()
    }
}
