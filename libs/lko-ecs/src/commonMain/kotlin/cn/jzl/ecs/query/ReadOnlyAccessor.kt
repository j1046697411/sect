package cn.jzl.ecs.query

import kotlin.properties.ReadOnlyProperty

interface ReadOnlyAccessor<V> : Accessor, ReadOnlyProperty<EntityQueryContext, V>