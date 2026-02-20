package cn.jzl.sect.engine

import cn.jzl.ecs.ECSDsl
import cn.jzl.ecs.World
import cn.jzl.ecs.addon.Addon
import cn.jzl.ecs.addon.WorldSetup
import cn.jzl.ecs.entity.Entity
import cn.jzl.ecs.entity.addComponent
import cn.jzl.ecs.world
import cn.jzl.sect.core.ai.BehaviorState
import cn.jzl.sect.core.ai.BehaviorType
import cn.jzl.sect.core.cultivation.Cultivation
import cn.jzl.sect.core.cultivation.Realm
import cn.jzl.sect.core.disciple.Attribute
import cn.jzl.sect.core.facility.Facility
import cn.jzl.sect.core.facility.FacilityType
import cn.jzl.sect.core.sect.SectPosition
import cn.jzl.sect.core.sect.Position
import cn.jzl.sect.core.sect.Sect
import cn.jzl.sect.core.sect.SectResource
import cn.jzl.sect.core.time.GameTime
import cn.jzl.sect.core.resource.ResourceProduction
import cn.jzl.sect.core.resource.ResourceType
import cn.jzl.sect.core.disciple.Loyalty
import cn.jzl.sect.cultivation.systems.CultivationSystem
import cn.jzl.sect.cultivation.systems.SimpleBehaviorSystem
import cn.jzl.sect.disciples.systems.DiscipleInfoSystem
import cn.jzl.sect.resource.systems.ResourceProductionSystem
import cn.jzl.sect.resource.systems.ResourceConsumptionSystem
import cn.jzl.sect.facility.systems.SectStatusSystem

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
            it.addComponent(SectResource(
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
            it.addComponent(Cultivation(
                realm = Realm.FOUNDATION,
                layer = 5,
                cultivation = 5000L,
                maxCultivation = 10000L
            ))
            it.addComponent(Attribute(
                physique = 70,
                comprehension = 65,
                fortune = 50,
                charm = 60,
                age = 80
            ))
            it.addComponent(Position(position = SectPosition.LEADER))
            it.addComponent(BehaviorState(currentBehavior = BehaviorType.CULTIVATE))
            it.addComponent(Loyalty(value = 100))
        }

        // 长老（2 名）
        repeat(2) { i ->
            world.entity {
                it.addComponent(Cultivation(
                    realm = Realm.FOUNDATION,
                    layer = 3,
                    cultivation = 3000L,
                    maxCultivation = 10000L
                ))
                it.addComponent(Attribute(
                    physique = 55 + i * 5,
                    comprehension = 50 + i * 5,
                    age = 60 + i * 5
                ))
                it.addComponent(Position(position = SectPosition.ELDER))
                it.addComponent(BehaviorState(currentBehavior = BehaviorType.CULTIVATE))
                it.addComponent(Loyalty(value = 90))
            }
        }

        // 外门弟子（5 名）
        repeat(5) { i ->
            val layer = 3 + (i % 3) // 3,4,5,3,4
            world.entity {
                it.addComponent(Cultivation(
                    realm = Realm.QI_REFINING,
                    layer = layer,
                    cultivation = layer * 1000L,
                    maxCultivation = 5000L
                ))
                it.addComponent(Attribute(
                    physique = 30 + i * 5,
                    comprehension = 30 + i * 5,
                    age = 18 + i
                ))
                it.addComponent(Position(position = SectPosition.DISCIPLE_OUTER))
                it.addComponent(BehaviorState(currentBehavior = BehaviorType.CULTIVATE))
                it.addComponent(Loyalty(value = 80))
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
            cultivationSystem = CultivationSystem(world),
            behaviorSystem = SimpleBehaviorSystem(world),
            discipleInfoSystem = DiscipleInfoSystem(world),
            resourceProductionSystem = ResourceProductionSystem(world),
            resourceConsumptionSystem = ResourceConsumptionSystem(world),
            sectStatusSystem = SectStatusSystem(world)
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
    val cultivationSystem: CultivationSystem,
    val behaviorSystem: SimpleBehaviorSystem,
    val discipleInfoSystem: DiscipleInfoSystem,
    val resourceProductionSystem: ResourceProductionSystem,
    val resourceConsumptionSystem: ResourceConsumptionSystem,
    val sectStatusSystem: SectStatusSystem
)