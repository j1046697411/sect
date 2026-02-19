package cn.jzl.sect.engine

import cn.jzl.ecs.ECSDsl
import cn.jzl.ecs.World
import cn.jzl.ecs.addon.Addon
import cn.jzl.ecs.addon.WorldSetup
import cn.jzl.ecs.entity.Entity
import cn.jzl.ecs.entity.addComponent
import cn.jzl.ecs.world
import cn.jzl.sect.core.ai.BehaviorStateComponent
import cn.jzl.sect.core.ai.BehaviorType
import cn.jzl.sect.core.cultivation.CultivationComponent
import cn.jzl.sect.core.cultivation.Realm
import cn.jzl.sect.core.disciple.AttributeComponent
import cn.jzl.sect.core.facility.FacilityComponent
import cn.jzl.sect.core.facility.FacilityType
import cn.jzl.sect.core.sect.Position
import cn.jzl.sect.core.sect.PositionComponent
import cn.jzl.sect.core.sect.SectComponent
import cn.jzl.sect.core.sect.SectResourceComponent
import cn.jzl.sect.core.time.TimeComponent
import cn.jzl.sect.core.resource.ResourceProductionComponent
import cn.jzl.sect.core.resource.ResourceType
import cn.jzl.sect.core.disciple.LoyaltyComponent

import cn.jzl.ecs.entity

import kotlin.jvm.JvmInline
import kotlin.math.max

@Suppress("UNUSED_PARAMETER")
object SectWorld {
    @OptIn(ECSDsl::class)
    fun create(sectName: String): World {
        val world = world {
            WorldSetupInstallHelper.install(this, SectAddon.addon)
        }

        // 宗门基础信息
        world.entity {
            it.addComponent(SectComponent(
                name = sectName,
                leaderId = 0,
                foundedYear = 1
            ))
            it.addComponent(SectResourceComponent(
                spiritStones = 1000L,
                contributionPoints = 0L
            ))
            it.addComponent(TimeComponent(
                year = 1,
                month = 1,
                day = 1,
                hour = 0
            ))
        }

        // 掌门
        world.entity {
            it.addComponent(CultivationComponent(
                realm = Realm.FOUNDATION,
                layer = 5,
                cultivation = 5000L,
                maxCultivation = 10000L
            ))
            it.addComponent(AttributeComponent(
                physique = 70,
                comprehension = 65,
                fortune = 50,
                charm = 60,
                age = 80
            ))
            it.addComponent(PositionComponent(position = Position.LEADER))
            it.addComponent(BehaviorStateComponent(currentBehavior = BehaviorType.CULTIVATE))
            it.addComponent(LoyaltyComponent(value = 100))
        }

        // 长老（2 名）
        repeat(2) { i ->
            world.entity {
                it.addComponent(CultivationComponent(
                    realm = Realm.FOUNDATION,
                    layer = 3,
                    cultivation = 3000L,
                    maxCultivation = 10000L
                ))
                it.addComponent(AttributeComponent(
                    physique = 55 + i * 5,
                    comprehension = 50 + i * 5,
                    age = 60 + i * 5
                ))
                it.addComponent(PositionComponent(position = Position.ELDER))
                it.addComponent(BehaviorStateComponent(currentBehavior = BehaviorType.CULTIVATE))
                it.addComponent(LoyaltyComponent(value = 90))
            }
        }

        // 外门弟子（5 名）
        repeat(5) { i ->
            val layer = 3 + (i % 3) // 3,4,5,3,4
            world.entity {
                it.addComponent(CultivationComponent(
                    realm = Realm.QI_REFINING,
                    layer = layer,
                    cultivation = layer * 1000L,
                    maxCultivation = 5000L
                ))
                it.addComponent(AttributeComponent(
                    physique = 30 + i * 5,
                    comprehension = 30 + i * 5,
                    age = 18 + i
                ))
                it.addComponent(PositionComponent(position = Position.DISCIPLE_OUTER))
                it.addComponent(BehaviorStateComponent(currentBehavior = BehaviorType.CULTIVATE))
                it.addComponent(LoyaltyComponent(value = 80))
            }
        }

        // 设施
        world.entity {
            it.addComponent(FacilityComponent(
                type = FacilityType.CULTIVATION_ROOM,
                level = 1,
                capacity = 5,
                efficiency = 1.1f
            ))
        }
        world.entity {
            it.addComponent(FacilityComponent(
                type = FacilityType.DORMITORY,
                level = 1,
                capacity = 20,
                efficiency = 1.0f
            ))
        }

        // 灵石矿脉（资源生产设施）
        world.entity {
            it.addComponent(ResourceProductionComponent(
                type = ResourceType.SPIRIT_STONE,
                baseOutput = 50L,        // 每天产出50灵石
                efficiency = 1.0f,
                isActive = true
            ))
        }

        return world
    }
    
    private object WorldSetupInstallHelper {
        @Suppress("UNCHECKED_CAST")
        fun install(ws: cn.jzl.ecs.addon.WorldSetup, addon: cn.jzl.ecs.addon.Addon<*, *>) {
            ws.install(addon as cn.jzl.ecs.addon.Addon<Any, Any>) {}
        }
    }
}
