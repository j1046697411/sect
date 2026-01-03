package cn.jzl.ecs.query

import kotlin.properties.ReadWriteProperty

interface ReadWriteAccessor<V> : ReadOnlyAccessor<V>, ReadWriteProperty<EntityQueryContext, V>