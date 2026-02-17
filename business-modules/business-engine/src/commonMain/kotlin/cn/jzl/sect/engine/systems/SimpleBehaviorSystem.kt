package cn.jzl.sect.engine.systems

import kotlin.math.min
import cn.jzl.ecs.World
import cn.jzl.ecs.query
import cn.jzl.ecs.editor
import cn.jzl.ecs.entity.getComponent
import cn.jzl.ecs.entity.addComponent
import cn.jzl.ecs.query.EntityQueryContext
import cn.jzl.ecs.family.component
import cn.jzl.ecs.query.forEach
import cn.jzl.sect.core.ai.BehaviorStateComponent
import cn.jzl.sect.core.ai.BehaviorType
import cn.jzl.sect.core.cultivation.CultivationComponent
import cn.jzl.sect.core.disciple.AttributeComponent
import cn.jzl.ecs.entity.EntityRelationContext

class SimpleBehaviorSystem(private val world: World) {
    
    fun update(dt: Float) {
        val agentsQuery = world.query { AgentQueryContext(this) }
        
        // 收集需要变更的实体和数据，避免在迭代中直接编辑（虽然理论上支持，但当前似乎有边界问题）
        val updates = mutableListOf<UpdateData>()
        
        agentsQuery.forEach { ctx ->
            val bs = ctx.behavior
            val attr = ctx.attribute
            val cult = ctx.cultivation

            val spiritRatio = if (attr.maxSpirit > 0) attr.spirit.toFloat() / attr.maxSpirit else 0f
            val nextBehavior = when {
                spiritRatio >= 0.3f -> BehaviorType.CULTIVATE
                attr.health < (attr.maxHealth * 0.3) -> BehaviorType.REST
                else -> BehaviorType.WORK
            }

            updates.add(UpdateData(ctx.entity, nextBehavior, bs, attr, cult))
        }
        
        updates.forEach { data ->
            val entity = data.entity
            val nextBehavior = data.nextBehavior
            val bs = data.bs
            val attr = data.attr
            val cult = data.cult
            
            world.editor(entity) {
                // 更新行为状态
                if (bs.currentBehavior != nextBehavior) {
                    it.addComponent(BehaviorStateComponent(currentBehavior = nextBehavior))
                }

                // 根据新行为执行简化逻辑
                when (nextBehavior) {
                    BehaviorType.CULTIVATE -> {
                        val delta = attr.strength.toLong()
                        val newCultivation = cult.cultivation + delta
                        it.addComponent(CultivationComponent(
                            realm = cult.realm,
                            layer = cult.layer,
                            cultivation = newCultivation,
                            maxCultivation = cult.maxCultivation
                        ))
                    }
                    BehaviorType.REST -> {
                        val newSpirit = min(attr.spirit + 5, attr.maxSpirit)
                        it.addComponent(AttributeComponent(
                            physique = attr.physique,
                            comprehension = attr.comprehension,
                            fortune = attr.fortune,
                            charm = attr.charm,
                            strength = attr.strength,
                            agility = attr.agility,
                            intelligence = attr.intelligence,
                            endurance = attr.endurance,
                            health = attr.health,
                            maxHealth = attr.maxHealth,
                            spirit = newSpirit,
                            maxSpirit = attr.maxSpirit,
                            age = attr.age
                        ))
                    }
                    BehaviorType.WORK -> {
                        val newHealth = min(attr.health + 1, attr.maxHealth)
                        it.addComponent(AttributeComponent(
                            physique = attr.physique,
                            comprehension = attr.comprehension,
                            fortune = attr.fortune,
                            charm = attr.charm,
                            strength = attr.strength,
                            agility = attr.agility,
                            intelligence = attr.intelligence,
                            endurance = attr.endurance,
                            health = newHealth,
                            maxHealth = attr.maxHealth,
                            spirit = attr.spirit,
                            maxSpirit = attr.maxSpirit,
                            age = attr.age
                        ))
                    }
                    else -> {}
                }
            }
        }
    }
    
    private data class UpdateData(
        val entity: cn.jzl.ecs.entity.Entity,
        val nextBehavior: BehaviorType,
        val bs: BehaviorStateComponent,
        val attr: AttributeComponent,
        val cult: CultivationComponent
    )
    
    class AgentQueryContext(world: World) : EntityQueryContext(world), EntityRelationContext {
        val behavior: BehaviorStateComponent by component()
        val cultivation: CultivationComponent by component()
        val attribute: AttributeComponent by component()
    }
}
