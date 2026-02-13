package cn.jzl.ecs

import cn.jzl.ecs.addon.components
import cn.jzl.ecs.addon.createAddon
import cn.jzl.ecs.component.componentId
import cn.jzl.ecs.entity.EntityRelationContext
import cn.jzl.ecs.entity.addComponent
import cn.jzl.ecs.entity.addTag
import cn.jzl.ecs.family.component
import cn.jzl.ecs.relation.Relation
import cn.jzl.ecs.relation.component
import cn.jzl.ecs.relation.kind
import cn.jzl.ecs.relation.relation
import cn.jzl.ecs.relation.sharedComponent
import cn.jzl.ecs.relation.target
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * FamilyService的单元测试
 * 
 * 测试FamilyService的以下功能：
 * - family: 创建家族查询
 * - buildArchetype: 构建原型
 * - registerArchetype: 注册原型
 * - getArchetypeBits: 获取原型位图
 */
class FamilyServiceTest : EntityRelationContext {
    
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
            world.componentId<PlayerTag>()
            world.componentId<EnemyTag>()
        }
    }

    @Test
    fun testFamily_withComponent() {
        // Given: 创建一些实体
        world.entity { it.addComponent(Position(1, 1)) }
        world.entity { it.addComponent(Position(2, 2)) }
        world.entity { it.addComponent(Health(100)) }
        
        // When: 查询有Position组件的家族
        val family = world.familyService.family { component<Position>() }
        
        // Then: 应该返回2个实体
        assertEquals(2, family.size)
    }

    @Test
    fun testFamily_withMultipleComponents() {
        // Given: 创建具有多个组件的实体
        world.entity {
            it.addComponent(Position(1, 1))
            it.addComponent(Health(100))
        }
        world.entity { it.addComponent(Position(2, 2)) }
        world.entity { it.addComponent(Health(50)) }
        
        // When: 查询同时有Position和Health的家族
        val family = world.familyService.family { 
            component<Position>()
            component<Health>()
        }
        
        // Then: 应该返回1个实体
        assertEquals(1, family.size)
    }

    @Test
    fun testBuildArchetype() {
        // Given: 创建一些实体
        world.entity { it.addComponent(Position(1, 1)) }
        world.entity { 
            it.addComponent(Position(2, 2))
            it.addComponent(Health(100))
        }
        
        // When: 构建原型
        val archetypes = mutableListOf<cn.jzl.ecs.archetype.Archetype>()
        world.familyService.buildArchetype(archetypes::add) {
            component<Position>()
        }
        
        // Then: 应该返回至少1个原型
        assertTrue(archetypes.isNotEmpty())
    }

    @Test
    fun testFamily_caching() {
        // Given: 创建实体
        world.entity { it.addComponent(Position(1, 1)) }
        
        // When: 多次获取同一个家族
        val family1 = world.familyService.family { component<Position>() }
        val family2 = world.familyService.family { component<Position>() }
        
        // Then: 应该返回相同的家族实例
        assertEquals(family1, family2)
    }

    @Test
    fun testGetArchetypeBits() {
        // Given: 创建实体
        world.entity { it.addComponent(Position(1, 1)) }
        
        // When: 获取Position组件的原型位图
        val positionId = world.components.id<Position>()
        val bits = world.familyService.getArchetypeBits(
            Relation(positionId, world.components.componentOf)
        )
        
        // Then: 位图应该不为空
        assertTrue(bits.isNotEmpty())
    }

    // 测试数据类
    private data class Position(val x: Int, val y: Int)
    private data class Velocity(val dx: Int, val dy: Int)
    private data class Health(val value: Int)
    private sealed class PlayerTag
    private sealed class EnemyTag
}
