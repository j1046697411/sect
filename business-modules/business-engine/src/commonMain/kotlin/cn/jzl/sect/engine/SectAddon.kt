package cn.jzl.sect.engine

import cn.jzl.ecs.addon.createAddon
import cn.jzl.ecs.addon.components
import cn.jzl.ecs.component.componentId
import cn.jzl.ecs.component.tag
import cn.jzl.sect.core.sect.SectComponent
import cn.jzl.sect.core.sect.SectResourceComponent
import cn.jzl.sect.core.cultivation.CultivationComponent
import cn.jzl.sect.core.cultivation.Realm
import cn.jzl.sect.core.disciple.AttributeComponent
import cn.jzl.sect.core.sect.PositionComponent
import cn.jzl.sect.core.facility.FacilityComponent
import cn.jzl.sect.core.facility.FacilityType
import cn.jzl.sect.core.ai.BehaviorStateComponent

// 集中注册第一阶段所需的组件类型
object SectAddon {
    val addon = createAddon<Unit>("sect") {
        components {
            world.componentId<SectComponent>()
            world.componentId<SectResourceComponent>()
            world.componentId<CultivationComponent>()
            // 允许通过 Realm 作为标签来区分境界
            world.componentId<Realm> { it.tag() }
            world.componentId<AttributeComponent>()
            world.componentId<PositionComponent>()
            world.componentId<FacilityComponent>()
            world.componentId<FacilityType> { it.tag() }
            world.componentId<BehaviorStateComponent>()
        }
    }
}
