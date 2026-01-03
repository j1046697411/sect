package cn.jzl.ecs.observer

import cn.jzl.ecs.entity.Entity
import cn.jzl.ecs.relation.Relation

fun interface ObserverHandle {
    fun handle(entity: Entity, event: Any?, involved: Relation)
}