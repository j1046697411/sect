package cn.jzl.ecs.query

import cn.jzl.ecs.relation.Relation

data class RelationWithData<T>(val relation: Relation, val data: T)