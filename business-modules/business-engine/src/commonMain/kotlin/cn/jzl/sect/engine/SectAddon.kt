package cn.jzl.sect.engine

import cn.jzl.ecs.addon.createAddon
import cn.jzl.ecs.addon.components
import cn.jzl.ecs.component.componentId
import cn.jzl.ecs.component.tag
import cn.jzl.sect.core.sect.Sect
import cn.jzl.sect.core.sect.SectTreasury
import cn.jzl.sect.core.cultivation.Realm
import cn.jzl.sect.core.combat.CombatAttribute
import cn.jzl.sect.core.vitality.Vitality
import cn.jzl.sect.core.vitality.Spirit
import cn.jzl.sect.core.common.Name
import cn.jzl.sect.core.common.Description
import cn.jzl.sect.core.common.Level
import cn.jzl.sect.core.sect.SectPositionInfo
import cn.jzl.sect.core.ai.CurrentBehavior
import cn.jzl.sect.core.disciple.SectLoyalty
import cn.jzl.sect.core.time.GameTime
import cn.jzl.sect.core.ai.Personality6
import cn.jzl.sect.cultivation.components.CultivationProgress
import cn.jzl.sect.cultivation.components.Talent
import cn.jzl.sect.disciples.components.Relationship
import cn.jzl.sect.resource.components.ResourceProduction
import cn.jzl.sect.quest.components.QuestComponent
import cn.jzl.sect.quest.components.QuestExecutionComponent
import cn.jzl.sect.quest.components.EvaluationComponent
import cn.jzl.sect.quest.components.PolicyComponent
import cn.jzl.sect.quest.components.ElderPersonality
import cn.jzl.sect.facility.components.Facility
import cn.jzl.sect.facility.components.FacilityStatus

// 集中注册第一阶段所需的组件类型
object SectAddon {
    val addon = createAddon<Unit>("sect") {
        components {
            // 宗门相关
            world.componentId<Sect>()
            world.componentId<SectTreasury>()
            world.componentId<SectPositionInfo>()
            world.componentId<SectLoyalty>()

            // 修炼相关
            world.componentId<CultivationProgress>()
            world.componentId<Talent>()
            world.componentId<Realm>()

            // 战斗相关
            world.componentId<CombatAttribute>()

            // 状态相关
            world.componentId<Vitality>()
            world.componentId<Spirit>()

            // 通用可复用
            world.componentId<Name>()
            world.componentId<Description>()
            world.componentId<Level>()

            // 设施相关
            world.componentId<Facility>()
            world.componentId<FacilityStatus>()

            // AI 相关
            world.componentId<CurrentBehavior>()

            // 时间相关
            world.componentId<GameTime>()

            // 资源相关
            world.componentId<ResourceProduction>()

            // 任务相关
            world.componentId<QuestComponent>()
            world.componentId<QuestExecutionComponent>()
            world.componentId<EvaluationComponent>()
            world.componentId<PolicyComponent>()
            world.componentId<ElderPersonality>()

            // AI性格相关
            world.componentId<Personality6>()

            // 关系相关
            world.componentId<Relationship>()
        }
    }
}
