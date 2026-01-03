package cn.jzl.ecs

import cn.jzl.ecs.addon.components
import cn.jzl.ecs.addon.createAddon
import cn.jzl.ecs.component.OnInserted
import cn.jzl.ecs.component.componentId
import cn.jzl.ecs.component.tag
import cn.jzl.ecs.entity.*
import cn.jzl.ecs.family.component
import cn.jzl.ecs.observer.emit
import cn.jzl.ecs.observer.observe
import cn.jzl.ecs.query.EntityQueryContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class WorldTest : EntityRelationContext {
    override val world: World by lazy {
        world {
            install(testAddon)
        }
    }
    private val testAddon = createAddon<Unit>("testAddon") {
        components {
            world.componentId<Pos>()
            world.componentId<OwnerBy> {
                it.tag()
            }
            world.componentId<Health>()
            world.componentId<Name>()
        }
    }

    @Test
    fun testObserver() {
        val entity = world.entity {}
        entity.observe<OnInserted>().exec {
            println("Entity inserted: ${this.entity}, involved: ${this.involvedRelation}")
        }
        world.observe<OnInserted>().exec {
            println("World entity inserted: ${this.entity}, involved: ${this.involvedRelation}")
        }

        entity.editor {
            it.addComponent(Pos(10, 20))
        }
    }

    @Test
    fun testBasicEntityCreation() {
        // 测试基本实体创建
        val entity = world.entity {}
        assertNotNull(entity)
        assertTrue(entity.id > 0)
        println("Basic entity created: $entity")
    }

    @Test
    fun testComponentAdditionAndRetrieval() {
        // 测试组件添加和获取
        val entity = world.entity {
            it.addComponent(Pos(10, 20))
            it.addComponent(Health(100))
        }
        
        val pos = entity.getComponent<Pos>()
        val health = entity.getComponent<Health>()
        
        assertEquals(Pos(10, 20), pos)
        assertEquals(Health(100), health)
        println("Components retrieved: pos=$pos, health=$health")
    }

    @Test
    fun testRelationManagement() {
        // 测试关系管理
        val owner = world.entity {
            it.addComponent(Name("Owner"))
        }
        val entity = world.entity {
            it.addComponent(Pos(5, 5))
            it.addRelation<OwnerBy>(owner)
        }
        
        val pos = entity.getComponent<Pos>()
        assertEquals(Pos(5, 5), pos)
        println("Entity with relation created: $entity, owner: $owner")
    }

    @Test
    fun testFamilySystem() {
        // 测试家族系统
        val entity1 = world.entity {
            it.addComponent(Pos(1, 1))
            it.addComponent(Health(50))
        }
        val entity2 = world.entity {
            it.addComponent(Pos(2, 2))
        }
        val entity3 = world.entity {
            it.addComponent(Health(75))
        }
        
        val posFamily = world.familyService.family { component<Pos>() }
        val healthFamily = world.familyService.family { component<Health>() }
        val bothFamily = world.familyService.family { 
            component<Pos>() 
            component<Health>() 
        }
        
        assertEquals(2, posFamily.size)
        assertEquals(2, healthFamily.size)
        assertEquals(1, bothFamily.size)
        
        println("Pos family: ${posFamily.archetypes.joinToString() { it.id.toString() }}")
        println("Health family: ${healthFamily.archetypes.joinToString() { it.id.toString() }}")
        println("Both family: ${bothFamily.archetypes.joinToString() { it.id.toString() }}")
    }

    @Test
    fun testEntityHierarchy() {
        // 测试实体层次结构
        val parent = world.entity {
            it.addComponent(Name("Parent"))
        }
        val child = parent.childOf {
            it.addComponent(Pos(0, 0))
        }
        
        assertNotNull(child)
        println("Parent: $parent, Child: $child")
    }

    @Test
    fun testQuerySystem() {
        // 测试查询系统
        val entity1 = world.entity {
            it.addComponent(Pos(10, 20))
        }
        val entity2 = world.entity {
            it.addComponent(Pos(30, 40))
        }
        val entity3 = world.entity {
            it.addComponent(Health(100))
        }
        
        val query = world.query { PosContext(this) }
        var count = 0
        query.collect {
            count++
            println("Query result: entity=${it.entity} => pos=${it.pos}")
        }
        
        assertEquals(2, count)
    }

    @Test
    fun testComponentUpdate() {
        // 测试组件更新
        val entity = world.entity {
            it.addComponent(Pos(1, 1))
            it.addComponent(Health(50))
        }
        
        // 更新组件
        entity.editor { 
            it.addComponent(Pos(2, 2))
            it.addComponent(Health(75))
        }
        
        val updatedPos = entity.getComponent<Pos>()
        val updatedHealth = entity.getComponent<Health>()
        
        assertEquals(Pos(2, 2), updatedPos)
        assertEquals(Health(75), updatedHealth)
        println("Components updated: pos=$updatedPos, health=$updatedHealth")
    }

    @Test
    fun testMultipleRelations() {
        // 测试多重关系
        val owner1 = world.entity { it.addComponent(Name("Owner1")) }
        val owner2 = world.entity { it.addComponent(Name("Owner2")) }
        
        val entity = world.entity {
            it.addComponent(Pos(0, 0))
            it.addRelation<OwnerBy>(owner1)
            it.addRelation<OwnerBy>(owner2)
        }
        
        println("Entity with multiple owners: $entity")
        println("Owner1: $owner1, Owner2: $owner2")
    }

    @Test
    fun testInstanceOfPattern() {
        // 测试实例化模式
        val prefab = world.entity {
            it.addComponent(Pos(100, 100))
            it.addComponent(Health(200))
        }
        
        val instance = prefab.instanceOf {
            it.addComponent(Name("Instance"))
        }
        
        assertNotNull(instance)
        val pos = instance.getComponent<Pos>()
        val health = instance.getComponent<Health>()
        val name = instance.getComponent<Name>()
        
        assertEquals(Pos(100, 100), pos)
        assertEquals(Health(200), health)
        assertEquals(Name("Instance"), name)
        println("Instance created from prefab: $instance")
    }

    @Test
    fun testComplexScenario() {
        // 测试复杂场景
        val player = world.entity {
            it.addComponent(Name("Player"))
            it.addComponent(Pos(0, 0))
            it.addComponent(Health(100))
        }
        
        val weapon1 = player.childOf {
            it.addComponent(Name("Sword"))
            it.addComponent(Pos(1, 0))
        }
        
        val weapon2 = player.childOf {
            it.addComponent(Name("Bow"))
            it.addComponent(Pos(-1, 0))
        }
        
        val enemy = world.entity {
            it.addComponent(Name("Enemy"))
            it.addComponent(Pos(10, 10))
            it.addComponent(Health(50))
            it.addRelation<OwnerBy>(player)
        }
        
        // 查询所有有位置的实体
        val posEntities = world.familyService.family { component<Pos>() }
        val namedEntities = world.familyService.family { component<Name>() }
        
        println("Complex scenario:")
        println("Player: $player")
        println("Weapons: $weapon1, $weapon2")
        println("Enemy: $enemy")
        println("Entities with position: ${posEntities.size}")
        println("Named entities: ${namedEntities.size}")
    }

    data class Pos(val x: Int, val y: Int)
    data class Health(val value: Int)
    data class Name(val value: String)
    sealed class OwnerBy

    class PosContext(world: World) : EntityQueryContext(world) {
        val pos: Pos by component()
    }
}