package cn.jzl.ecs.observer

import cn.jzl.ecs.World
import cn.jzl.ecs.WorldOwner
import cn.jzl.ecs.entity.Entity
import cn.jzl.ecs.relation.Relation

interface ObserverContext : WorldOwner {
    override val world: World
    val entity: Entity
    val involvedRelation: Relation
}