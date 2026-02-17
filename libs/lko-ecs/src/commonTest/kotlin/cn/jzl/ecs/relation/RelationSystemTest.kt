package cn.jzl.ecs.relation

import cn.jzl.ecs.World
import cn.jzl.ecs.addon.components
import cn.jzl.ecs.addon.createAddon
import cn.jzl.ecs.component.componentId
import cn.jzl.ecs.component.singleRelation
import cn.jzl.ecs.component.tag
import cn.jzl.ecs.editor
import cn.jzl.ecs.entity
import cn.jzl.ecs.entity.*
import cn.jzl.ecs.world
import kotlin.test.Test

private sealed class OwnedBy

class RelationSystemTest : EntityRelationContext {
    override val world: World = world {
        install(relationAddon)
    }

    private val relationAddon = createAddon("relationAddon") {
        components {

            // 添加拥有者关系，并且拥有者关系是单关系，就是说拥有者同时只能有一个。
            world.componentId<OwnedBy> {
                it.tag()
                it.singleRelation()
            }
        }
    }

    @Test
    fun addOwnedByRelation() {
        val entity1 = world.entity { }
        val entity2 = world.entity { }
        val entity3 = world.entity { }

        // 添加拥有关系，entity2 拥有 entity1
        entity1.editor { it.addRelation<OwnedBy>(entity2) }

        //添加拥有者关系，entity3 拥有 entity1，因为OwnedBy关系类似是singleRelation，所以entity2 拥有 entity1这个关系会被自动移除
        entity1.editor { it.addRelation<OwnedBy>(entity3)}

        // 移除拥有者关系，
        entity1.editor { it.removeRelation<OwnedBy>(entity3)}

    }

}
