package cn.jzl.sect.engine

import cn.jzl.ecs.ECSDsl
import cn.jzl.ecs.World
import cn.jzl.ecs.addon.Addon
import cn.jzl.ecs.addon.WorldSetup
import cn.jzl.ecs.entity.Entity
import cn.jzl.ecs.entity.addComponent
import cn.jzl.ecs.world
import cn.jzl.sect.core.ai.CurrentBehavior
import cn.jzl.sect.core.ai.BehaviorType
import cn.jzl.sect.core.ai.Personality6
import cn.jzl.sect.core.ai.Personality8
import cn.jzl.sect.core.cultivation.CultivationProgress
import cn.jzl.sect.core.cultivation.Realm
import cn.jzl.sect.core.cultivation.Talent
import cn.jzl.sect.core.demo.Name
import cn.jzl.sect.core.vitality.Vitality
import cn.jzl.sect.core.vitality.Spirit
import cn.jzl.sect.core.disciple.Age
import cn.jzl.sect.core.disciple.SectLoyalty
import cn.jzl.sect.core.facility.Facility
import cn.jzl.sect.core.facility.FacilityType
import cn.jzl.sect.core.sect.SectPositionType
import cn.jzl.sect.core.sect.SectPositionInfo
import cn.jzl.sect.core.sect.Sect
import cn.jzl.sect.core.sect.SectTreasury
import cn.jzl.sect.core.time.GameTime
import cn.jzl.sect.core.resource.ResourceProduction
import cn.jzl.sect.core.resource.ResourceType
import cn.jzl.sect.cultivation.systems.CultivationSystem
import cn.jzl.sect.cultivation.systems.SimpleBehaviorSystem
import cn.jzl.sect.disciples.systems.DiscipleInfoSystem
import cn.jzl.sect.resource.systems.ResourceProductionSystem
import cn.jzl.sect.resource.systems.ResourceConsumptionSystem
import cn.jzl.sect.facility.systems.SectStatusSystem
import cn.jzl.sect.quest.systems.SelectionTaskSystem
import cn.jzl.sect.quest.systems.TeamFormationSystem
import cn.jzl.sect.quest.systems.QuestExecutionSystem
import cn.jzl.sect.quest.systems.ElderEvaluationSystem
import cn.jzl.sect.quest.systems.PromotionSystem
import cn.jzl.sect.quest.systems.PolicySystem
import cn.jzl.sect.engine.systems.TimeSystem

import cn.jzl.ecs.entity

import kotlin.jvm.JvmInline
import kotlin.math.max

/**
 * 宗门世界 - 游戏主世界管理
 */
@Suppress("UNUSED_PARAMETER")
object SectWorld {

    /**
     * 创建宗门世界
     * @param sectName 宗门名称
     * @return 配置好的 ECS 世界
     *
     * 注意：生产代码应使用 WorldProvider.initialize()，
     * 此方法主要用于测试和WorldProvider内部调用
     */
    @OptIn(ECSDsl::class)
    fun create(sectName: String): World {
        val world = world {
            WorldSetupInstallHelper.install(this, SectAddon.addon)
        }

        // 创建初始实体
        createInitialEntities(world, sectName)

        return world
    }

    /**
     * 创建初始实体
     */
    private fun createInitialEntities(world: World, sectName: String) {
        // 宗门基础信息
        world.entity {
            it.addComponent(Sect(
                name = sectName,
                leaderId = 0,
                foundedYear = 1
            ))
            it.addComponent(SectTreasury(
                spiritStones = 1000L,
                contributionPoints = 0L
            ))
            it.addComponent(GameTime(
                year = 1,
                month = 1,
                day = 1,
                hour = 0
            ))
        }

        // 掌门
        world.entity {
            it.addComponent(Name(name = "青云子"))
            it.addComponent(CultivationProgress(
                realm = Realm.FOUNDATION,
                layer = 5,
                cultivation = 5000L,
                maxCultivation = 10000L
            ))
            it.addComponent(Talent(
                physique = 70,
                comprehension = 65,
                fortune = 50,
                charm = 60
            ))
            it.addComponent(Vitality(currentHealth = 100, maxHealth = 100))
            it.addComponent(Spirit(currentSpirit = 50, maxSpirit = 50))
            it.addComponent(Age(age = 80))
            it.addComponent(SectPositionInfo(position = SectPositionType.LEADER))
            it.addComponent(CurrentBehavior(type = BehaviorType.CULTIVATE))
            it.addComponent(SectLoyalty(value = 100))
        }

        // 长老名字列表
        val elderNames = listOf("玄明", "玉清")

        // 长老（2 名）
        repeat(2) { i ->
            world.entity {
                it.addComponent(Name(name = elderNames[i]))
                it.addComponent(CultivationProgress(
                    realm = Realm.FOUNDATION,
                    layer = 3,
                    cultivation = 3000L,
                    maxCultivation = 10000L
                ))
                it.addComponent(Talent(
                    physique = 55 + i * 5,
                    comprehension = 50 + i * 5,
                    fortune = 45 + i * 5,
                    charm = 50
                ))
                it.addComponent(Vitality(currentHealth = 100, maxHealth = 100))
                it.addComponent(Spirit(currentSpirit = 50, maxSpirit = 50))
                it.addComponent(Age(age = 60 + i * 5))
                it.addComponent(SectPositionInfo(position = SectPositionType.ELDER))
                it.addComponent(CurrentBehavior(type = BehaviorType.CULTIVATE))
                it.addComponent(SectLoyalty(value = 90))
            }
        }

        // 内门弟子名字列表
        val innerNames = listOf("李明", "王强", "张伟", "刘洋", "陈杰", "杨帆", "赵鹏", "黄磊")

        // 内门弟子（8 名）- 筑基期1-3层，有完整AI性格属性
        repeat(8) { i ->
            val layer = 1 + (i % 3) // 1,2,3,1,2,3,1,2
            world.entity {
                it.addComponent(Name(name = innerNames[i]))
                it.addComponent(CultivationProgress(
                    realm = Realm.FOUNDATION,
                    layer = layer,
                    cultivation = layer * 2000L,
                    maxCultivation = 10000L
                ))
                it.addComponent(Talent(
                    physique = 45 + i * 3,
                    comprehension = 45 + i * 3,
                    fortune = 50 + i * 2,
                    charm = 50 + i * 2
                ))
                it.addComponent(Vitality(currentHealth = 100, maxHealth = 100))
                it.addComponent(Spirit(currentSpirit = 60, maxSpirit = 60))
                it.addComponent(Age(age = 25 + i * 2))
                it.addComponent(SectPositionInfo(position = SectPositionType.DISCIPLE_INNER))
                it.addComponent(CurrentBehavior(type = BehaviorType.CULTIVATE))
                it.addComponent(SectLoyalty(value = 85))
                // 内门弟子有完整的AI性格属性（使用Personality6）
                it.addComponent(Personality6.random())
            }
        }

        // 外门弟子名字列表
        val outerNames = listOf("周文", "吴武", "郑飞", "孙翔", "钱进")

        // 外门弟子（5 名）
        repeat(5) { i ->
            val layer = 3 + (i % 3) // 3,4,5,3,4
            world.entity {
                it.addComponent(Name(name = outerNames[i]))
                it.addComponent(CultivationProgress(
                    realm = Realm.QI_REFINING,
                    layer = layer,
                    cultivation = layer * 1000L,
                    maxCultivation = 5000L
                ))
                it.addComponent(Talent(
                    physique = 30 + i * 5,
                    comprehension = 30 + i * 5,
                    fortune = 40,
                    charm = 45
                ))
                it.addComponent(Vitality(currentHealth = 100, maxHealth = 100))
                it.addComponent(Spirit(currentSpirit = 50, maxSpirit = 50))
                it.addComponent(Age(age = 18 + i))
                it.addComponent(SectPositionInfo(position = SectPositionType.DISCIPLE_OUTER))
                it.addComponent(CurrentBehavior(type = BehaviorType.CULTIVATE))
                it.addComponent(SectLoyalty(value = 80))
            }
        }

        // 设施
        world.entity {
            it.addComponent(Facility(
                type = FacilityType.CULTIVATION_ROOM,
                level = 1,
                capacity = 5,
                efficiency = 1.1f
            ))
        }
        world.entity {
            it.addComponent(Facility(
                type = FacilityType.DORMITORY,
                level = 1,
                capacity = 20,
                efficiency = 1.0f
            ))
        }

        // 灵石矿脉（资源生产设施）
        world.entity {
            it.addComponent(ResourceProduction(
                type = ResourceType.SPIRIT_STONE,
                baseOutput = 50L,        // 每天产出50灵石
                efficiency = 1.0f,
                isActive = true
            ))
        }
    }

    /**
     * 获取所有系统实例
     * @param world ECS 世界
     * @return 系统容器
     */
    fun getSystems(world: World): SectSystems {
        return SectSystems(
            timeSystem = TimeSystem(world),
            cultivationSystem = CultivationSystem(world),
            behaviorSystem = SimpleBehaviorSystem(world),
            discipleInfoSystem = DiscipleInfoSystem(world),
            resourceProductionSystem = ResourceProductionSystem(world),
            resourceConsumptionSystem = ResourceConsumptionSystem(world),
            sectStatusSystem = SectStatusSystem(world),
            selectionTaskSystem = SelectionTaskSystem(world),
            teamFormationSystem = TeamFormationSystem(world),
            questExecutionSystem = QuestExecutionSystem(world),
            elderEvaluationSystem = ElderEvaluationSystem(world),
            promotionSystem = PromotionSystem(world),
            policySystem = PolicySystem(world)
        )
    }

    private object WorldSetupInstallHelper {
        @Suppress("UNCHECKED_CAST")
        fun install(ws: cn.jzl.ecs.addon.WorldSetup, addon: cn.jzl.ecs.addon.Addon<*, *>) {
            ws.install(addon as cn.jzl.ecs.addon.Addon<Any, Any>) {}
        }
    }
}

/**
 * 系统容器 - 统一管理所有业务系统
 */
data class SectSystems(
    val timeSystem: TimeSystem,
    val cultivationSystem: CultivationSystem,
    val behaviorSystem: SimpleBehaviorSystem,
    val discipleInfoSystem: DiscipleInfoSystem,
    val resourceProductionSystem: ResourceProductionSystem,
    val resourceConsumptionSystem: ResourceConsumptionSystem,
    val sectStatusSystem: SectStatusSystem,
    val selectionTaskSystem: SelectionTaskSystem,
    val teamFormationSystem: TeamFormationSystem,
    val questExecutionSystem: QuestExecutionSystem,
    val elderEvaluationSystem: ElderEvaluationSystem,
    val promotionSystem: PromotionSystem,
    val policySystem: PolicySystem
)
