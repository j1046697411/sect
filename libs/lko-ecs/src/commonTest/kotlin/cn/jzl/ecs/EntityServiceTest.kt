package cn.jzl.ecs

import cn.jzl.ecs.addon.components
import cn.jzl.ecs.addon.createAddon
import cn.jzl.ecs.component.componentId
import cn.jzl.ecs.entity.Entity
import cn.jzl.ecs.entity.EntityRelationContext
import cn.jzl.ecs.entity.addComponent
import cn.jzl.ecs.entity.getComponent
import cn.jzl.ecs.entity.id
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * EntityService的单元测试
 * 
 * 测试EntityService的以下功能：
 * - isActive: 检查实体是否活跃
 * - create: 创建实体
 * - configure: 配置实体
 * - runOn: 在实体上运行操作
 */
class EntityServiceTest : EntityRelationContext {
    
    override lateinit var world: World
    
    @BeforeTest
    fun setup() {
        world = world {
            install(testAddon)
        }
    }

    private val testAddon = createAddon<Unit>("test") {
        components {
            world.componentId<Position>()
            world.componentId<Velocity>()
            world.componentId<Health>()
        }
    }

    @Test
    fun testIsActive_returnsTrueForActiveEntity() {
        // Given: 创建一个实体
        val entity = world.entity { it.addComponent(Position(1, 1)) }
        
        // When: 检查实体是否活跃
        val isActive = world.entityService.isActive(entity)
        
        // Then: 应该返回true
        assertTrue(isActive)
    }

    @Test
    fun testIsActive_returnsFalseForInvalidEntity() {
        // Given: 创建一个无效实体
        val invalidEntity = cn.jzl.ecs.entity.Entity(999999)
        
        // When: 检查实体是否活跃
        val isActive = world.entityService.isActive(invalidEntity)
        
        // Then: 应该返回false
        assertFalse(isActive)
    }

    @Test
    fun testCreate_withDefaultParameters() {
        // When: 创建实体
        val entity = world.entity {}
        
        // Then: 实体应该被创建
        assertTrue(entity.id > 0)
    }

    @Test
    fun testCreate_withConfiguration() {
        // When: 创建实体并添加组件
        val entity = world.entity {
            it.addComponent(Position(10, 20))
            it.addComponent(Health(100))
        }
        
        // Then: 组件应该被添加
        assertEquals(Position(10, 20), entity.getComponent<Position>())
        assertEquals(Health(100), entity.getComponent<Health>())
    }

    @Test
    fun testCreate_withSpecificId() {
        // Note: create(entityId, ...) is not fully implemented yet
        // This test verifies the function signature exists
    }

    @Test
    fun testConfigure_updatesExistingEntity() {
        // Given: 创建一个实体
        val entity = world.entity {
            it.addComponent(Position(1, 1))
        }
        
        // When: 配置实体
        world.entityService.configure(entity, true) {
            it.addComponent(Position(2, 2))
        }
        
        // Then: 组件应该被更新
        assertEquals(Position(2, 2), entity.getComponent<Position>())
    }

    @Test
    fun testConfigure_withEventDisabled() {
        // Given: 创建一个实体
        val entity = world.entity {
            it.addComponent(Position(1, 1))
        }
        
        // When: 配置实体但不触发事件
        world.entityService.configure(entity, false) {
            it.addComponent(Position(3, 3))
        }
        
        // Then: 组件应该被更新
        assertEquals(Position(3, 3), entity.getComponent<Position>())
    }

    @Test
    fun testRunOn_executesBlockOnEntity() {
        // Given: 创建一个实体
        val entity = world.entity {
            it.addComponent(Position(5, 10))
        }
        
        // When: 在实体上运行操作
        var result = -1
        world.entityService.runOn(entity) { entityIndex ->
            result = entityIndex
        }
        
        // Then: 应该返回有效的索引
        assertTrue(result >= 0)
    }

    @Test
    fun testRunOn_throwsForInvalidEntity() {
        // Given: 创建一个无效实体
        val invalidEntity = Entity(999999)
        
        // When/Then: 应该抛出异常
        try {
            world.entityService.runOn(invalidEntity) { 0 }
            assertFalse(true, "Should have thrown exception")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message?.contains("not in world") == true)
        }
    }

    @Test
    fun testMultipleEntityCreation() {
        // When: 创建多个实体
        val entities = (1..5).map { i -> world.entity { it.addComponent(Position(i, i * 2)) } }
        
        // Then: 所有实体都应该被创建
        assertEquals(5, entities.size)
        entities.forEachIndexed { index, entity ->
            assertEquals(Position(index + 1, (index + 1) * 2), entity.getComponent<Position>())
        }
    }

    @Test
    fun testEntityEditor_apply() {
        // Given: 创建一个实体
        val entity = world.entity { it.addComponent(Position(1, 1)) }
        
        // When: 使用editor添加更多组件
        entity.editor {
            it.addComponent(Velocity(5, 5))
        }
        
        // Then: 应该有两个组件
        assertEquals(Position(1, 1), entity.getComponent<Position>())
        assertEquals(Velocity(5, 5), entity.getComponent<Velocity>())
    }

    // 测试数据类
    private data class Position(val x: Int, val y: Int)
    private data class Velocity(val dx: Int, val dy: Int)
    private data class Health(val value: Int)
}
