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
import cn.jzl.ecs.family.FamilyBuilder
import cn.jzl.ecs.family.relation
import cn.jzl.ecs.query
import cn.jzl.ecs.query.EntityQueryContext
import cn.jzl.ecs.query.OptionalGroup
import cn.jzl.ecs.query.map
import cn.jzl.ecs.query.toList
import cn.jzl.ecs.world
import kotlin.jvm.JvmInline
import kotlin.test.Test

private sealed class OwnedBy

sealed class TestTag

@JvmInline
value class Test1Data(val data: Int)

@JvmInline
data class Test2Data(val data1: Int, val data2: Long)

data class GameConfig(val x: Int, val data2: Int)

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

            world.componentId<Test1Data>()
            world.componentId<Test2Data>()
            world.componentId<TestTag> { it.tag() }

            world.componentId<GameConfig>()
        }
    }

    @Test
    fun addOwnedByRelation() {
        val entity1 = world.entity {
            // 添加组件
            it.addComponent(Test1Data(1))
            //添加组件
            it.addComponent(Test2Data(1, 4))
            // 添加tag
            it.addTag<TestTag>()

            // 添加共享组件, 首次添加的时候必须放入GameConfig对象
            it.addSharedComponent<GameConfig>(GameConfig(1,2))
        }

        val entity2 = world.entity {
            // 添加共享组件，后面添加的时候就不需要再次添加GameConfig对象
            it.addSharedComponent<GameConfig>()
        }
        val entity3 = world.entity {
            // 添加共享组件，并且更新共享组件。所有关联的共享组件都会被更新
            it.addSharedComponent<GameConfig>(GameConfig(2, 3))
        }

        //更新/删除，组件、标记。方法一
        entity1.editor {
            // 更新组件
            it.addComponent(Test1Data(2))

            // 移除组件
            it.removeComponent<Test2Data>()

            // 移除tag
            it.removeTag<TestTag>()
        }

        //更新/删除，组件、标记。方法一
        world.editor(entity1) {
            // 更新组件
            it.addComponent(Test1Data(2))

            // 移除组件
            it.removeComponent<Test2Data>()

            // 移除tag
            it.removeTag<TestTag>()
        }



        // 添加拥有关系，entity2 拥有 entity1
        entity1.editor { it.addRelation<OwnedBy>(entity2) }

        //添加拥有者关系，entity3 拥有 entity1，因为OwnedBy关系类似是singleRelation，所以entity2 拥有 entity1这个关系会被自动移除
        entity1.editor { it.addRelation<OwnedBy>(entity3)}

        // 移除拥有者关系，
        entity1.editor { it.removeRelation<OwnedBy>(entity3)}
    }

    class TestQueryContext(world: World) : EntityQueryContext(world) {

        // 查询 所有者关系（必定存在）
        val owner: Entity by relationUp<OwnedBy>()

        // 必定存在组件Test1Data
        val component3 by component<Test1Data>()

        // 必须存在组件Test2Data，并且可以修改和赋值
        var test2 by component<Test2Data>()

        // 可能存在存在组件Test2Data，并且可以修改和赋值
        var test3 by component<Test2Data?>()

        // 可能存在组件Test1Data
        val component by component<Test1Data?>()

        // Test2Data 或者Test1Data 组件中至少存在一个以上
        val component2 by component<Test2Data?>(optionalGroup = OptionalGroup.One)
        val component1 by component<Test1Data?>(optionalGroup = OptionalGroup.One)

        override fun FamilyBuilder.configure() {

            // 存在共享组件GameConfig
            relation(relations.sharedComponent<GameConfig>())

            //存在组件Test1Data
            relation(relations.component<Test1Data>())

            // 存在目标是target的关系，kind没有限制
            relation(relations.target(target = target))

            // 存在类型是kind的关系，target没有限制
            relation(relations.kind(kind = kind))
        }
    }
}
