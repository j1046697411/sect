package cn.jzl.sect.disciples.services

import cn.jzl.di.instance
import cn.jzl.ecs.world
import cn.jzl.sect.disciples.disciplesAddon
import cn.jzl.sect.disciples.components.RelationshipType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.BeforeTest

/**
 * 关系服务测试类
 */
class RelationshipServiceTest {

    private val world = world {
        install(disciplesAddon)
    }
    private val service: RelationshipService by world.di.instance()

    @Test
    fun `建立关系应创建双向关系`() {
        // Given
        val sourceId = 1L
        val targetId = 2L

        // When
        val relationship = service.establishRelationship(
            sourceId = sourceId,
            targetId = targetId,
            type = RelationshipType.FRIENDLY,
            level = 60
        )

        // Then
        assertEquals(sourceId, relationship.sourceId)
        assertEquals(targetId, relationship.targetId)
        assertEquals(RelationshipType.FRIENDLY, relationship.type)
        assertEquals(60, relationship.level)
    }

    @Test
    fun `查询关系应返回正确的关系列表`() {
        // Given
        service.establishRelationship(1L, 2L, RelationshipType.FRIENDLY, 60)
        service.establishRelationship(1L, 3L, RelationshipType.PEER, 70)
        service.establishRelationship(2L, 3L, RelationshipType.COOPERATOR, 80)

        // When
        val relationships = service.getRelationships(1L)

        // Then
        assertEquals(2, relationships.size)
        assertTrue(relationships.any { it.targetId == 2L })
        assertTrue(relationships.any { it.targetId == 3L })
    }

    @Test
    fun `查询特定类型关系应返回过滤后的列表`() {
        // Given
        service.establishRelationship(1L, 2L, RelationshipType.FRIENDLY, 60)
        service.establishRelationship(1L, 3L, RelationshipType.PEER, 70)
        service.establishRelationship(1L, 4L, RelationshipType.FRIENDLY, 80)

        // When
        val friendlyRelations = service.getRelationshipsByType(1L, RelationshipType.FRIENDLY)

        // Then
        assertEquals(2, friendlyRelations.size)
        assertTrue(friendlyRelations.all { it.type == RelationshipType.FRIENDLY })
    }

    @Test
    fun `解除关系应移除关系`() {
        // Given
        service.establishRelationship(1L, 2L, RelationshipType.FRIENDLY, 60)

        // When
        service.dissolveRelationship(1L, 2L)

        // Then
        val relationships = service.getRelationships(1L)
        assertEquals(0, relationships.size)
    }

    @Test
    fun `获取关系等级应返回正确值`() {
        // Given
        service.establishRelationship(1L, 2L, RelationshipType.FRIENDLY, 75)

        // When
        val level = service.getRelationshipLevel(1L, 2L)

        // Then
        assertEquals(75, level)
    }

    @Test
    fun `获取不存在的关系等级应返回0`() {
        // Given
        // 无需准备数据

        // When
        val level = service.getRelationshipLevel(1L, 2L)

        // Then
        assertEquals(0, level)
    }

    @Test
    fun `改善关系应增加关系等级`() {
        // Given
        service.establishRelationship(1L, 2L, RelationshipType.FRIENDLY, 50)

        // When
        service.improveRelationship(1L, 2L, 20)

        // Then
        val level = service.getRelationshipLevel(1L, 2L)
        assertEquals(70, level)
    }

    @Test
    fun `恶化关系应降低关系等级`() {
        // Given
        service.establishRelationship(1L, 2L, RelationshipType.FRIENDLY, 50)

        // When
        service.worsenRelationship(1L, 2L, 15)

        // Then
        val level = service.getRelationshipLevel(1L, 2L)
        assertEquals(35, level)
    }

    @Test
    fun `获取关系效果加成应返回正确值`() {
        // Given
        service.establishRelationship(1L, 2L, RelationshipType.FRIENDLY, 80)

        // When
        val bonus = service.getRelationshipEffectBonus(1L, 2L)

        // Then - 80 / 5 = 16
        assertEquals(16, bonus)
    }
}
