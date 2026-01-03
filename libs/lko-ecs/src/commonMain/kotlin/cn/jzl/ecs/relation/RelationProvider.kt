package cn.jzl.ecs.relation

import cn.jzl.ecs.component.Components
import kotlin.jvm.JvmInline

@JvmInline
value class RelationProvider(@PublishedApi internal val comps: Components)