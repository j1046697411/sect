package cn.jzl.ecs.entity

import cn.jzl.ecs.WorldOwner
import cn.jzl.ecs.relation.Relation

interface EntityEditor : WorldOwner {

    fun addRelation(entity: Entity, relation: Relation, data: Any)

    fun addRelation(entity: Entity, relation: Relation)

    fun removeRelation(entity: Entity, relation: Relation)
}

