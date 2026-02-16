package cn.jzl.ecs

import cn.jzl.ecs.addon.components
import cn.jzl.ecs.addon.createAddon
import cn.jzl.ecs.component.ComponentStore
import cn.jzl.ecs.component.componentId
import cn.jzl.ecs.component.doubleStore
import cn.jzl.ecs.component.floatStore
import cn.jzl.ecs.component.intStore
import cn.jzl.ecs.component.longStore
import cn.jzl.ecs.component.store
import cn.jzl.ecs.entity.EntityRelationContext
import cn.jzl.ecs.entity.addComponent
import cn.jzl.ecs.family.component
import cn.jzl.ecs.query.EntityQueryContext
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

typealias TestLevel = Int
typealias TestExperience = Long
typealias TestDamage = Double
typealias TestHealth = Float

class TestLevelContext(world: World) : EntityQueryContext(world) {
    val level: TestLevel by component()
}

class TestExperienceContext(world: World) : EntityQueryContext(world) {
    val experience: TestExperience by component()
}

class TestDamageContext(world: World) : EntityQueryContext(world) {
    val damage: TestDamage by component()
}

class TestHealthContext(world: World) : EntityQueryContext(world) {
    val health: TestHealth by component()
}

class ComponentStoreIntegrationTest : EntityRelationContext {

    override lateinit var world: World

    @BeforeTest
    fun setup() {
        world = world {
            install(testAddon)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private val testAddon = createAddon<Unit>("test") {
        components {
            world.componentId<TestLevel> { it.store { intStore() as ComponentStore<Any> } }
            world.componentId<TestExperience> { it.store { longStore() as ComponentStore<Any> } }
            world.componentId<TestDamage> { it.store { doubleStore() as ComponentStore<Any> } }
            world.componentId<TestHealth> { it.store { floatStore() as ComponentStore<Any> } }
        }
    }

    @Test
    fun testIntStoreEntities() {
        repeat(100) { index ->
            world.entity { entity ->
                entity.addComponent(index)
            }
        }

        val query = world.query { TestLevelContext(this) }
        var count = 0
        query.collect {
            count++
        }

        assertEquals(100, count)
    }

    @Test
    fun testIntStoreQueryWithCondition() {
        repeat(100) { index ->
            world.entity { entity ->
                entity.addComponent(index)
            }
        }

        val query = world.query { TestLevelContext(this) }
        var count = 0
        query.collect { ctx ->
            if (ctx.level > 50) count++
        }

        assertEquals(49, count)
    }

    @Test
    fun testIntStoreValues() {
        repeat(10) { index ->
            world.entity { entity ->
                entity.addComponent(index)
            }
        }

        val query = world.query { TestLevelContext(this) }
        var sum = 0
        query.collect { ctx ->
            sum += ctx.level
        }

        assertEquals(45, sum)
    }

    @Test
    fun testLongStoreEntities() {
        repeat(50) { index ->
            world.entity { entity ->
                entity.addComponent(index.toLong())
            }
        }

        val query = world.query { TestExperienceContext(this) }
        var count = 0
        query.collect {
            count++
        }

        assertEquals(50, count)
    }

    @Test
    fun testDoubleStoreEntities() {
        repeat(50) { index ->
            world.entity { entity ->
                entity.addComponent(index.toDouble())
            }
        }

        val query = world.query { TestDamageContext(this) }
        var count = 0
        query.collect {
            count++
        }

        assertEquals(50, count)
    }

    @Test
    fun testFloatStoreEntities() {
        repeat(50) { index ->
            world.entity { entity ->
                entity.addComponent(index.toFloat())
            }
        }

        val query = world.query { TestHealthContext(this) }
        var count = 0
        query.collect {
            count++
        }

        assertEquals(50, count)
    }
}
