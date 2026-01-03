package cn.jzl.ecs.component

import cn.jzl.ecs.relation.Relation

fun interface ComponentStoreFactory<C> {
    fun create(relation: Relation): ComponentStore<C>

    companion object : ComponentStoreFactory<Any> {
        override fun create(relation: Relation): ComponentStore<Any> = GeneralComponentStore()
    }
}