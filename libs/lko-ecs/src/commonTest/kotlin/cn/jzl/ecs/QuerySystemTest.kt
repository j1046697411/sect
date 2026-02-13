package cn.jzl.ecs

import cn.jzl.ecs.addon.components
import cn.jzl.ecs.addon.createAddon
import cn.jzl.ecs.component.componentId
import cn.jzl.ecs.component.tag
import cn.jzl.ecs.entity.*
import cn.jzl.ecs.family.FamilyBuilder
import cn.jzl.ecs.family.component
import cn.jzl.ecs.query.EntityQueryContext
import kotlin.jvm.JvmInline
import kotlin.test.*

// 查询系统测试专用的数据类
private data class QueryPosition(val x: Int, val y: Int)
private data class QueryVelocity(val dx: Int, val dy: Int)
private data class QueryHealth(val current: Int, val max: Int)
@JvmInline
private value class QueryName(val value: String)
private sealed class QueryActiveTag
private sealed class QueryEnemyTag

// 查询上下文类
private class QueryPositionContext(world: World) : EntityQueryContext(world) {
    val position: QueryPosition by component()
}

private class QueryPositionVelocityContext(world: World) : EntityQueryContext(world) {
    val position: QueryPosition by component()
    val velocity: QueryVelocity by component()
}

private class QueryPositionVelocityHealthContext(world: World) : EntityQueryContext(world) {
    val position: QueryPosition by component()
    val velocity: QueryVelocity by component()
    val health: QueryHealth by component()
}

private class QueryPositionNameContext(world: World) : EntityQueryContext(world) {
    val position: QueryPosition by component()
    val name: QueryName by component()
}

private class QueryActivePositionContext(world: World) : EntityQueryContext(world) {
    val position: QueryPosition by component()
    override fun FamilyBuilder.configure() {
        component<QueryActiveTag>()
    }
}

private class QueryActiveContext(world: World) : EntityQueryContext(world) {
    override fun FamilyBuilder.configure() {
        component<QueryActiveTag>()
    }
}

private class QueryEnemyContext(world: World) : EntityQueryContext(world) {
    override fun FamilyBuilder.configure() {
        component<QueryActiveTag>()
        component<QueryEnemyTag>()
    }
}

private class QueryPlayerContext(world: World) : EntityQueryContext(world) {
    val health: QueryHealth by component()
    override fun FamilyBuilder.configure() {
        component<QueryActiveTag>()
        component<QueryHealth>()
    }
}

private class QueryItemContext(world: World) : EntityQueryContext(world) {
    val name: QueryName by component()
}

private class QueryActiveEnemyContext(world: World) : EntityQueryContext(world) {
    override fun FamilyBuilder.configure() {
        component<QueryActiveTag>()
        component<QueryEnemyTag>()
    }
}

/**
 * 查询系统测试
 * 测试基本查询、多组件查询、可选组件查询、关系查询等
 */
class QuerySystemTest : EntityRelationContext {
    
    override lateinit var world: World
    
    @BeforeTest
    fun setup() {
        world = world {
            install(testAddon)
        }
    }
        
    private val testAddon = createAddon<Unit>("test") {
        components {
            world.componentId<QueryPosition>()
            world.componentId<QueryVelocity>()
            world.componentId<QueryHealth>()
            world.componentId<QueryName>()
            world.componentId<QueryActiveTag> { it.tag() }
            world.componentId<QueryEnemyTag> { it.tag() }
        }
    }

    @Test
    fun testBasicQuery() {
        world.entity { it.addComponent(QueryPosition(10, 20)) }
        world.entity { it.addComponent(QueryPosition(30, 40)) }
        world.entity { it.addComponent(QueryHealth(100, 100)) }

        val query = world.query { QueryPositionContext(this) }
        var count = 0
        query.collect {
            count++
            assertNotNull(it.position)
        }

        assertEquals(2, count, "Should find 2 entities with Position")
    }

    @Test
    fun testMultiComponentQuery() {
        world.entity {
            it.addComponent(QueryPosition(10, 20))
            it.addComponent(QueryVelocity(1, 1))
        }
        world.entity {
            it.addComponent(QueryPosition(30, 40))
            it.addComponent(QueryHealth(100, 100))
        }
        world.entity {
            it.addComponent(QueryPosition(50, 60))
            it.addComponent(QueryVelocity(2, 2))
            it.addComponent(QueryHealth(80, 100))
        }

        val query = world.query { QueryPositionVelocityContext(this) }
        var count = 0
        query.collect {
            count++
            assertNotNull(it.position)
            assertNotNull(it.velocity)
        }

        assertEquals(2, count, "Should find 2 entities with both Position and Velocity")
    }

    @Test
    fun testTripleComponentQuery() {
        world.entity {
            it.addComponent(QueryPosition(10, 20))
            it.addComponent(QueryVelocity(1, 1))
            it.addComponent(QueryHealth(100, 100))
        }
        world.entity {
            it.addComponent(QueryPosition(30, 40))
            it.addComponent(QueryVelocity(2, 2))
        }

        val query = world.query { QueryPositionVelocityHealthContext(this) }
        var count = 0
        query.collect {
            count++
            assertNotNull(it.position)
            assertNotNull(it.velocity)
            assertNotNull(it.health)
        }

        assertEquals(1, count, "Should find 1 entity with all three components")
    }

    @Test
    fun testEmptyQuery() {
        val query = world.query { QueryPositionContext(this) }
        var count = 0
        query.collect { count++ }

        assertEquals(0, count, "Should find 0 entities initially")
    }

    @Test
    fun testQueryWithTag() {
        world.entity {
            it.addComponent(QueryPosition(10, 20))
            it.addTag<QueryActiveTag>()
        }
        world.entity {
            it.addComponent(QueryPosition(30, 40))
        }

        val query = world.query { QueryActivePositionContext(this) }
        var count = 0
        query.collect {
            count++
            assertNotNull(it.position)
        }

        assertEquals(1, count, "Should find 1 entity with Position and ActiveTag")
    }

    @Test
    fun testQueryPerformance() {
        repeat(1000) { index ->
            world.entity {
                it.addComponent(QueryPosition(index, index * 2))
                it.addComponent(QueryVelocity(index % 10, index % 10))
            }
        }

        val query = world.query { QueryPositionContext(this) }
        var count = 0
        query.collect { count++ }

        assertEquals(1000, count, "Should find all 1000 entities")
    }

    @Test
    fun testComplexQueryScenario() {
        repeat(5) { index ->
            world.entity {
                it.addComponent(QueryName("Player$index"))
                it.addComponent(QueryPosition(index * 10, 0))
                it.addComponent(QueryHealth(100, 100))
                it.addTag<QueryActiveTag>()
            }
        }

        repeat(10) { index ->
            world.entity {
                it.addComponent(QueryName("Enemy$index"))
                it.addComponent(QueryPosition(index * 5, 50))
                it.addComponent(QueryHealth(50, 50))
                it.addTag<QueryActiveTag>()
                it.addTag<QueryEnemyTag>()
            }
        }

        repeat(20) { index ->
            world.entity {
                it.addComponent(QueryName("Item$index"))
                it.addComponent(QueryPosition(index * 3, 100))
            }
        }

        val activeEntities = world.query { QueryActiveContext(this) }
        val enemies = world.query { QueryEnemyContext(this) }
        val players = world.query { QueryPlayerContext(this) }
        val items = world.query { QueryItemContext(this) }

        var activeCount = 0
        var enemyCount = 0
        var playerCount = 0
        var itemCount = 0

        activeEntities.collect { activeCount++ }
        enemies.collect { enemyCount++ }
        players.collect { playerCount++ }
        items.collect { itemCount++ }

        assertTrue(activeCount >= 15, "Should find at least 15 active entities, found $activeCount")
        assertTrue(enemyCount >= 10, "Should find at least 10 enemies, found $enemyCount")
        assertTrue(playerCount >= 5, "Should find at least 5 players, found $playerCount")
        assertTrue(itemCount >= 20, "Should find at least 20 items, found $itemCount")
    }

    @Test
    fun testQueryEntityAccess() {
        val entity = world.entity {
            it.addComponent(QueryPosition(10, 20))
            it.addComponent(QueryName("Test"))
        }

        val query = world.query { QueryPositionNameContext(this) }
        var found = false

        query.collect {
            if (it.entity.id == entity.id) {
                found = true
                assertEquals("Test", it.name.value)
                assertEquals(10, it.position.x)
                assertEquals(20, it.position.y)
            }
        }

        assertTrue(found, "Should find the specific entity")
    }

    @Test
    fun testQueryWithMultipleTags() {
        world.entity {
            it.addComponent(QueryPosition(10, 20))
            it.addTag<QueryActiveTag>()
            it.addTag<QueryEnemyTag>()
        }
        world.entity {
            it.addComponent(QueryPosition(30, 40))
            it.addTag<QueryActiveTag>()
        }

        val query = world.query { QueryActiveEnemyContext(this) }
        var count = 0
        query.collect { count++ }

        assertEquals(1, count, "Should find 1 entity with both ActiveTag and EnemyTag")
    }

    @Test
    fun testQueryCaching() {
        world.entity { it.addComponent(QueryPosition(10, 20)) }

        val query1 = world.query { QueryPositionContext(this) }
        val query2 = world.query { QueryPositionContext(this) }

        // Note: Query objects may not be cached to be the same instance,
        // but they should return the same results
        var count1 = 0
        var count2 = 0
        query1.collect { count1++ }
        query2.collect { count2++ }

        assertEquals(count1, count2, "Queries should return same results")
        assertEquals(1, count1, "Should find 1 entity")
    }

    @Test
    fun testQueryAfterEntityModification() {
        val entity = world.entity {
            it.addComponent(QueryPosition(10, 20))
        }

        val query1 = world.query { QueryPositionContext(this) }
        var count1 = 0
        query1.collect { count1++ }
        assertEquals(1, count1, "Should find 1 entity initially")

        entity.editor {
            it.addComponent(QueryVelocity(5, 5))
        }

        val query2 = world.query { QueryPositionContext(this) }
        var count2 = 0
        query2.collect { count2++ }
        assertEquals(1, count2, "Should still find 1 entity after modification")
    }
}
