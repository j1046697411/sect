package cn.jzl.sect.disciples.systems

import cn.jzl.sect.core.relation.RelationshipType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

/**
 * 师徒系统测试类
 */
class MasterApprenticeSystemTest {

    @Test
    fun `拜师应建立师徒关系`() {
        // Given
        val relationshipSystem = RelationshipSystem()
        val masterApprenticeSystem = MasterApprenticeSystem(relationshipSystem)
        val masterId = 1L
        val apprenticeId = 2L

        // When
        val result = masterApprenticeSystem.apprenticeToMaster(apprenticeId, masterId)

        // Then
        assertTrue(result)
        assertTrue(relationshipSystem.hasRelationship(apprenticeId, masterId))
        val relationship = relationshipSystem.getRelationship(apprenticeId, masterId)
        assertEquals(RelationshipType.MASTER_APPRENTICE, relationship?.type)
    }

    @Test
    fun `获取师父应返回正确的师父ID`() {
        // Given
        val relationshipSystem = RelationshipSystem()
        val masterApprenticeSystem = MasterApprenticeSystem(relationshipSystem)
        val masterId = 1L
        val apprenticeId = 2L
        masterApprenticeSystem.apprenticeToMaster(apprenticeId, masterId)

        // When
        val foundMasterId = masterApprenticeSystem.getMaster(apprenticeId)

        // Then
        assertEquals(masterId, foundMasterId)
    }

    @Test
    fun `获取徒弟列表应返回所有徒弟`() {
        // Given
        val relationshipSystem = RelationshipSystem()
        val masterApprenticeSystem = MasterApprenticeSystem(relationshipSystem)
        val masterId = 1L
        val apprenticeId1 = 2L
        val apprenticeId2 = 3L
        masterApprenticeSystem.apprenticeToMaster(apprenticeId1, masterId)
        masterApprenticeSystem.apprenticeToMaster(apprenticeId2, masterId)

        // When
        val apprentices = masterApprenticeSystem.getApprentices(masterId)

        // Then
        assertEquals(2, apprentices.size)
        assertTrue(apprentices.contains(apprenticeId1))
        assertTrue(apprentices.contains(apprenticeId2))
    }

    @Test
    fun `解除师徒关系应移除关系`() {
        // Given
        val relationshipSystem = RelationshipSystem()
        val masterApprenticeSystem = MasterApprenticeSystem(relationshipSystem)
        val masterId = 1L
        val apprenticeId = 2L
        masterApprenticeSystem.apprenticeToMaster(apprenticeId, masterId)

        // When
        masterApprenticeSystem.dissolveMasterApprenticeRelationship(apprenticeId, masterId)

        // Then
        assertFalse(relationshipSystem.hasRelationship(apprenticeId, masterId))
        assertEquals(null, masterApprenticeSystem.getMaster(apprenticeId))
    }

    @Test
    fun `获取修炼效率加成应返回20_对于师徒关系`() {
        // Given
        val relationshipSystem = RelationshipSystem()
        val masterApprenticeSystem = MasterApprenticeSystem(relationshipSystem)
        val masterId = 1L
        val apprenticeId = 2L
        masterApprenticeSystem.apprenticeToMaster(apprenticeId, masterId)

        // When
        val bonus = masterApprenticeSystem.getCultivationEfficiencyBonus(apprenticeId)

        // Then - 师徒关系提供20%修炼效率加成
        assertEquals(20, bonus)
    }

    @Test
    fun `获取功法学习加成应返回30_对于师徒关系`() {
        // Given
        val relationshipSystem = RelationshipSystem()
        val masterApprenticeSystem = MasterApprenticeSystem(relationshipSystem)
        val masterId = 1L
        val apprenticeId = 2L
        masterApprenticeSystem.apprenticeToMaster(apprenticeId, masterId)

        // When
        val bonus = masterApprenticeSystem.getSkillLearningBonus(apprenticeId)

        // Then - 师徒关系提供30%功法学习加成
        assertEquals(30, bonus)
    }

    @Test
    fun `没有师父时获取修炼效率加成应返回0`() {
        // Given
        val relationshipSystem = RelationshipSystem()
        val masterApprenticeSystem = MasterApprenticeSystem(relationshipSystem)
        val apprenticeId = 2L

        // When
        val bonus = masterApprenticeSystem.getCultivationEfficiencyBonus(apprenticeId)

        // Then
        assertEquals(0, bonus)
    }

    @Test
    fun `没有师父时获取功法学习加成应返回0`() {
        // Given
        val relationshipSystem = RelationshipSystem()
        val masterApprenticeSystem = MasterApprenticeSystem(relationshipSystem)
        val apprenticeId = 2L

        // When
        val bonus = masterApprenticeSystem.getSkillLearningBonus(apprenticeId)

        // Then
        assertEquals(0, bonus)
    }

    @Test
    fun `重复拜师应返回false`() {
        // Given
        val relationshipSystem = RelationshipSystem()
        val masterApprenticeSystem = MasterApprenticeSystem(relationshipSystem)
        val masterId1 = 1L
        val masterId2 = 3L
        val apprenticeId = 2L
        masterApprenticeSystem.apprenticeToMaster(apprenticeId, masterId1)

        // When - 尝试拜另一个师父
        val result = masterApprenticeSystem.apprenticeToMaster(apprenticeId, masterId2)

        // Then - 应该失败，因为已经有师父了
        assertFalse(result)
        assertEquals(masterId1, masterApprenticeSystem.getMaster(apprenticeId))
    }
}
