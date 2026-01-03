package cn.jzl.ecs.component

import cn.jzl.ecs.World
import cn.jzl.ecs.WorldOwner
import cn.jzl.ecs.entity.Entity
import cn.jzl.ecs.relation.Relation
import cn.jzl.ecs.relation.kind

class ShadedComponentService(override val world: World) : WorldOwner {
    private val components = mutableMapOf<Entity, Any>()

    operator fun get(relation: Relation): Any? = components[relation.kind]

    operator fun set(relation: Relation, component: Any) {
        components[relation.kind] = component
    }
}