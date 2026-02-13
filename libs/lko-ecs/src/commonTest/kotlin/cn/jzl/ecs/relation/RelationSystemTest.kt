package cn.jzl.ecs.relation

import cn.jzl.ecs.World
import cn.jzl.ecs.entity.*
import cn.jzl.ecs.world


class RelationSystemTest : EntityRelationContext {
    override val world: World by lazy {
        world {  }
    }
}
