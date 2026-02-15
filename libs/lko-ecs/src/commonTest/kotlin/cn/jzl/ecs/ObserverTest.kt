package cn.jzl.ecs

import cn.jzl.ecs.addon.components
import cn.jzl.ecs.addon.createAddon
import cn.jzl.ecs.component.OnInserted
import cn.jzl.ecs.component.componentId
import cn.jzl.ecs.component.tag
import cn.jzl.ecs.entity.*
import cn.jzl.ecs.observer.*
import cn.jzl.ecs.query.EntityQueryContext
import kotlin.test.*


// 观察者测试专用的数据类
private data class ObserverPosition(val x: Int, val y: Int)
private data class ObserverHealth(val current: Int, val max: Int)
private sealed class ObserverActiveTag

// 查询上下文类
private class ObserverPositionContext(world: World) : EntityQueryContext(world) {
    val position: ObserverPosition by component()
}

private class ObserverHealthContext(world: World) : EntityQueryContext(world) {
    val health: ObserverHealth by component()
}

/**
 * 观察者系统测试
 * 测试组件添加观察者、组件移除观察者、实体创建/销毁观察者等
 */
class ObserverTest : EntityRelationContext {
    
    override lateinit var world: World
    
    @BeforeTest
    fun setup() {
        world = world {
            install(testAddon)
        }
    }
            
    private val testAddon = createAddon<Unit>("test") {
        components {
            world.componentId<ObserverPosition>()
            world.componentId<ObserverHealth>()
            world.componentId<ObserverActiveTag> { it.tag() }
            world.componentId<OnInserted>()
        }
    }

    @Test
    fun testObserverOnComponentInsert() {
        var observerCalled = false

        val entity = world.entity {}
        
        entity.observe<OnInserted>().exec {
            observerCalled = true
        }

        entity.editor {
            it.addComponent(ObserverPosition(10, 20))
        }

        assertTrue(observerCalled, "Observer should be called when component is inserted")
    }

    @Test
    fun testObserverOnWorldLevel() {
        var observerCalled = false

        world.observe<OnInserted>().exec {
            observerCalled = true
        }

        val entity = world.entity {}
        entity.editor {
            it.addComponent(ObserverPosition(10, 20))
        }

        assertTrue(observerCalled, "World-level observer should be called")
    }

    @Test
    fun testMultipleObservers() {
        var observer1Called = false
        var observer2Called = false

        val entity = world.entity {}

        entity.observe<OnInserted>().exec {
            observer1Called = true
        }

        entity.observe<OnInserted>().exec {
            observer2Called = true
        }

        entity.editor {
            it.addComponent(ObserverPosition(10, 20))
        }

        assertTrue(observer1Called, "First observer should be called")
        assertTrue(observer2Called, "Second observer should be called")
    }

    @Test
    fun testObserverWithData() {
        var receivedData: String? = null

        val entity = world.entity {}

        entity.observeWithData<String>().exec {
            receivedData = this.event
        }

        world.emit(entity, "test data")

        assertEquals("test data", receivedData, "Observer should receive correct data")
    }

    @Test
    fun testObserverWithQuery() {
        val entity = world.entity {
            it.addComponent(ObserverPosition(10, 20))
        }

        val query = world.query { ObserverPositionContext(this) }

        var observerCalled = false
        var hasPosition = false

        entity.observe<OnInserted>().exec(query) {
            observerCalled = true
            hasPosition = true
        }

        entity.editor {
            it.addComponent(ObserverHealth(100, 100))
        }

        assertTrue(observerCalled, "Observer with query should be called")
        assertTrue(hasPosition, "Query should confirm entity has Position")
    }

    @Test
    fun testObserverContextAccess() {
        var entityAccessed = false

        val entity = world.entity {}

        entity.observe<OnInserted>().exec {
            entityAccessed = true
            assertNotNull(this.entity, "Should be able to access entity from context")
            assertEquals(entity.id, this.entity.id, "Context should have correct entity")
        }

        entity.editor {
            it.addComponent(ObserverPosition(10, 20))
        }

        assertTrue(entityAccessed, "Should access entity from observer context")
    }

    @Test
    fun testMultipleEventObservers() {
        var insertedCalled = false
        var customEventCalled = false

        val entity = world.entity {}

        entity.observe<OnInserted>().exec {
            insertedCalled = true
        }

        entity.observe<String>().exec {
            customEventCalled = true
        }

        entity.editor {
            it.addComponent(ObserverPosition(10, 20))
        }

        world.emit(entity, "custom event")

        assertTrue(insertedCalled, "OnInserted observer should be called")
        assertTrue(customEventCalled, "Custom event observer should be called")
    }



    @Test
    fun testObserverWithTagFilter() {
        var activeTagObserverCalled = false

        val entity = world.entity {
            it.addTag<ObserverActiveTag>()
        }

        entity.observe<OnInserted>().exec {
            if (this.entity.hasTag<ObserverActiveTag>()) {
                activeTagObserverCalled = true
            }
        }

        entity.editor {
            it.addComponent(ObserverPosition(10, 20))
        }

        assertTrue(activeTagObserverCalled, "Observer should detect ActiveTag")
    }

    @Test
    fun testWorldObserveWithEntity() {
        var observerCalled = false
        val entity = world.entity {}

        world.observe<OnInserted>(entity).exec {
            observerCalled = true
        }

        entity.editor {
            it.addComponent(ObserverPosition(10, 20))
        }

        assertTrue(observerCalled, "World observer with entity should be called")
    }

    @Test
    fun testWorldObserveWithCustomConfigure() {
        var observerCalled = false

        val entity = world.entity {}

        world.observe(entity) {
            yield(world.components.id<OnInserted>())
        }.exec {
            observerCalled = true
        }

        entity.editor {
            it.addComponent(ObserverPosition(10, 20))
        }

        assertTrue(observerCalled, "World observer with custom configure should be called")
    }

    @Test
    fun testWorldEmitWithInvolvedRelation() {
        var receivedData: String? = null
        val entity = world.entity {}

        world.observeWithData<String>().exec {
            receivedData = this.event
        }

        world.emit(entity, "test with relation")

        assertEquals("test with relation", receivedData)
    }

    @Test
    fun testWorldEmitWithoutData() {
        var observerCalled = false
        val entity = world.entity {}

        world.observe<OnInserted>().exec {
            observerCalled = true
        }

        world.emit<OnInserted>(entity)

        assertTrue(observerCalled)
    }

    @Test
    fun testEntityEmitWithWorldOwner() {
        var observerCalled = false
        val entity = world.entity {}

        world.observe<OnInserted>().exec {
            observerCalled = true
        }

        with(entity) {
            emit<OnInserted>()
        }

        assertTrue(observerCalled)
    }

    @Test
    fun testEntityEmitWithDataAndWorldOwner() {
        var receivedData: String? = null
        val entity = world.entity {}

        world.observeWithData<String>().exec {
            receivedData = this.event
        }

        with(entity) {
            emit("test data")
        }

        assertEquals("test data", receivedData)
    }
}
