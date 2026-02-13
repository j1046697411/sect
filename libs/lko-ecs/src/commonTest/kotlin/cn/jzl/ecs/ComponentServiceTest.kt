package cn.jzl.ecs

import cn.jzl.ecs.addon.components
import cn.jzl.ecs.addon.createAddon
import cn.jzl.ecs.component.componentId
import cn.jzl.ecs.component.tag
import cn.jzl.ecs.component.singleRelation
import cn.jzl.ecs.entity.Entity
import cn.jzl.ecs.entity.EntityRelationContext
import cn.jzl.ecs.entity.id
import cn.jzl.ecs.relation.Relation
import cn.jzl.ecs.relation.kind
import cn.jzl.ecs.relation.relation
import cn.jzl.ecs.relation.target
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * ComponentService的单元测试
 * 
 * 测试ComponentService的以下功能：
 * - holdsData: 检查关系是否持有数据
 * - isSingleRelation: 检查是否为单一关系
 * - isShadedComponent: 检查是否为共享组件
 * - getOrRegisterEntityForClass: 注册组件类
 */
class ComponentServiceTest : EntityRelationContext {
    
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
            world.componentId<TestTag> { it.tag() }
            world.componentId<SingleRelationTag> { 
                it.tag()
                it.singleRelation()
            }
        }
    }

    @Test
    fun testHoldsData_returnsTrueForNormalComponent() {
        // Given: 正常组件的关系 - 使用componentOf作为target
        val positionId = world.components.id<Position>()
        val positionRelation = Relation(positionId, world.components.componentOf)
        
        // When: 检查是否持有数据
        val holdsData = world.componentService.holdsData(positionRelation)
        
        // Then: 正常组件应该持有数据
        assertTrue(holdsData)
    }

    @Test
    fun testHoldsData_returnsFalseForTag() {
        // Given: 标签组件的关系
        val tagId = world.components.id<TestTag>()
        val tagRelation = Relation(tagId, world.components.componentOf)
        
        // When: 检查是否持有数据
        val holdsData = world.componentService.holdsData(tagRelation)
        
        // Then: 标签不持有数据
        assertFalse(holdsData)
    }

    @Test
    fun testIsSingleRelation_returnsFalseForNormalComponent() {
        // Given: 正常组件的关系
        val positionId = world.components.id<Position>()
        val positionRelation = Relation(positionId, world.components.componentOf)
        
        // When: 检查是否为单一关系
        val isSingle = world.componentService.isSingleRelation(positionRelation)
        
        // Then: 正常组件不是单一关系
        assertFalse(isSingle)
    }

    @Test
    fun testIsSingleRelation_returnsTrueForSingleRelationTag() {
        // Given: 单一关系标签的关系
        val singleRelationId = world.components.id<SingleRelationTag>()
        val singleRelationRelation = Relation(singleRelationId, world.components.componentOf)
        
        // When: 检查是否为单一关系
        val isSingle = world.componentService.isSingleRelation(singleRelationRelation)
        
        // Then: 单一关系标签是单一关系
        assertTrue(isSingle)
    }

    @Test
    fun testIsShadedComponent_returnsTrueForSharedComponent() {
        // Given: 共享组件的关系 (sharedOf作为target)
        val positionId = world.components.id<Position>()
        val sharedRelation = Relation(positionId, world.components.sharedOf)
        
        // When: 检查是否为共享组件
        val isShaded = world.componentService.isShadedComponent(sharedRelation)
        
        // Then: 使用sharedOf作为target的是共享组件
        assertTrue(isShaded)
    }

    @Test
    fun testIsShadedComponent_returnsFalseForNormalComponent() {
        // Given: 正常组件的关系
        val positionId = world.components.id<Position>()
        val positionRelation = Relation(positionId, world.components.componentOf)
        
        // When: 检查是否为共享组件
        val isShaded = world.componentService.isShadedComponent(positionRelation)
        
        // Then: 正常组件不是共享组件
        assertFalse(isShaded)
    }

    @Test
    fun testGetOrRegisterEntityForClass_returnsSameIdForSameClass() {
        // When: 获取同一个类的组件ID两次
        val id1 = world.componentService.getOrRegisterEntityForClass(Position::class)
        val id2 = world.componentService.getOrRegisterEntityForClass(Position::class)
        
        // Then: 应该返回相同的ID
        assertEquals(id1, id2)
    }

    @Test
    fun testGetOrRegisterEntityForClass_returnsDifferentIdForDifferentClass() {
        // When: 获取不同类的组件ID
        val positionId = world.componentService.getOrRegisterEntityForClass(Position::class)
        val velocityId = world.componentService.getOrRegisterEntityForClass(Velocity::class)
        
        // Then: 应该返回不同的ID
        assertTrue(positionId != velocityId)
    }

    @Test
    fun testEntityTags_containsTagComponent() {
        // Given: 已经注册了标签组件 TestTag
        
        // When: 检查entityTags中是否包含TestTag
        val testTagId = world.components.id<TestTag>()
        
        // Then: entityTags应该包含TestTag
        assertTrue(testTagId.data in world.componentService.entityTags)
    }

    @Test
    fun testEntityTags_doesNotContainNormalComponent() {
        // Given: 已经注册了正常组件 Position
        
        // When: 检查entityTags中是否包含Position
        val positionId = world.components.id<Position>()
        
        // Then: entityTags不应该包含Position
        assertFalse(positionId.data in world.componentService.entityTags)
    }

    @Test
    fun testSingleRelationBits_containsSingleRelationTag() {
        // Given: 已经注册了单一关系标签 SingleRelationTag
        
        // When: 检查singleRelationBits中是否包含SingleRelationTag
        val singleRelationId = world.components.id<SingleRelationTag>()
        
        // Then: singleRelationBits应该包含SingleRelationTag
        assertTrue(singleRelationId.data in world.componentService.singleRelationBits)
    }

    @Test
    fun testSingleRelationBits_doesNotContainNormalComponent() {
        // Given: 已经注册了正常组件 Position
        
        // When: 检查singleRelationBits中是否包含Position
        val positionId = world.components.id<Position>()
        
        // Then: singleRelationBits不应该包含Position
        assertFalse(positionId.data in world.componentService.singleRelationBits)
    }

    // 测试数据类
    private data class Position(val x: Int, val y: Int)
    private data class Velocity(val dx: Int, val dy: Int)
    private sealed class TestTag
    private sealed class SingleRelationTag
}
