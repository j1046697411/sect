package cn.jzl.sect.engine

import cn.jzl.ecs.addon.createAddon
import cn.jzl.ecs.addon.components
import cn.jzl.ecs.component.componentId
import cn.jzl.ecs.component.tag
import cn.jzl.sect.core.sect.Sect
import cn.jzl.sect.core.sect.SectResource
import cn.jzl.sect.core.cultivation.Cultivation
import cn.jzl.sect.core.disciple.Attribute
import cn.jzl.sect.core.sect.Position
import cn.jzl.sect.core.facility.Facility
import cn.jzl.sect.core.ai.BehaviorState
import cn.jzl.sect.core.disciple.Loyalty
import cn.jzl.sect.core.time.GameTime
import cn.jzl.sect.core.resource.ResourceProduction

// 集中注册第一阶段所需的组件类型
object SectAddon {
    val addon = createAddon<Unit>("sect") {
        components {
            world.componentId<Sect>()
            world.componentId<SectResource>()
            world.componentId<Cultivation>()
            world.componentId<Attribute>()
            world.componentId<Position>()
            world.componentId<Facility>()
            world.componentId<BehaviorState>()
            world.componentId<Loyalty>()
            world.componentId<GameTime>()
            world.componentId<ResourceProduction>()
        }
    }
}
