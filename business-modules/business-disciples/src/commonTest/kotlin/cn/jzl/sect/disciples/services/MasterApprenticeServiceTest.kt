package cn.jzl.sect.disciples.services

import cn.jzl.sect.disciples.components.RelationshipType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

/**
 * 师徒服务测试类
 */
class MasterApprenticeServiceTest {

    @Test
    fun `拜师应建立师徒关系`() {
        // Given
        val relationshipService = RelationshipService()
        val masterApprenticeService = MasterApprenticeService()
        masterApprenticeService.apply {
            // 通过反射设置私有字段
            javaClass.getDeclaredField("relationshipService").apply {
                isAccessible = true
                set(masterApprenticeService, relationshipService)
            }
        }
        val masterId = 1L
        val apprenticeId = 2L

        // When
        val result = masterApprenticeService.apprenticeToMaster(apprenticeId, masterId)

        // Then
        assertTrue(result)
        assertTrue(relationshipService.hasRelationship(apprenticeId, masterId))
        val relationship = relationshipService.getRelationship(apprenticeId, masterId)
        assertEquals(RelationshipType.MASTER_APPRENTICE, relationship?.type)
    }

    @Test
    fun `获取师父应返回正确的师父ID`() {
        // Given
        val relationshipService = RelationshipService()
        val masterApprenticeService = MasterApprenticeService()
        masterApprenticeService.apply {
            javaClass.getDeclaredField("relationshipService").apply {
                isAccessible = true
                set(masterApprenticeService, relationshipService)
            }
        }
        val masterId = 1L
        val apprenticeId = 2L
        masterApprenticeService.apprenticeToMaster(apprenticeId, masterId)

        // When
        val foundMasterId = masterApprenticeService.getMaster(apprenticeId)

        // Then
        assertEquals(masterId, foundMasterId)
    }

    @Test
    fun `获取徒弟列表应返回所有徒弟`() {
        // Given
        val relationshipService = RelationshipService()
        val masterApprenticeService = MasterApprenticeService()
        masterApprenticeService.apply {
            javaClass.getDeclaredField("relationshipService").apply {
                isAccessible = true
                set(masterApprenticeService, relationshipService)
            }
        }
        val masterId = 1L
        val apprenticeId1 = 2L
        val apprenticeId2 = 3L
        masterApprenticeService.apprenticeToMaster(apprenticeId1, masterId)
        masterApprenticeService.apprenticeToMaster(apprenticeId2, masterId)

        // When
        val apprentices = masterApprenticeService.getApprentices(masterId)

        // Then
        assertEquals(2, apprentices.size)
        assertTrue(apprentices.contains(apprenticeId1))
        assertTrue(apprentices.contains(apprenticeId2))
    }

    @Test
    fun `解除师徒关系应移除关系`() {
        // Given
        val relationshipService = RelationshipService()
        val masterApprenticeService = MasterApprenticeService()
        masterApprenticeService.apply {
            javaClass.getDeclaredField("relationshipService").apply {
                isAccessible = true
                set(masterApprenticeService, relationshipService)
            }
        }
        val masterId = 1L
        val apprenticeId = 2L
        masterApprenticeService.apprenticeToMaster(apprenticeId, masterId)

        // When
        masterApprenticeService.dissolveMasterApprenticeRelationship(apprenticeId, masterId)

        // Then
        assertFalse(relationshipService.hasRelationship(apprenticeId, masterId))
        assertEquals(null, masterApprenticeService.getMaster(apprenticeId))
    }

    @Test
    fun `获取修炼效率加成应返回20_对于师徒关系`() {
        // Given
        val relationshipService = RelationshipService()
        val masterApprenticeService = MasterApprenticeService()
        masterApprenticeService.apply {
            javaClass.getDeclaredField("relationshipService").apply {
                isAccessible = true
                set(masterApprenticeService, relationshipService)
            }
        }
        val masterId = 1L
        val apprenticeId = 2L
        masterApprenticeService.apprenticeToMaster(apprenticeId, masterId)

        // When
        val bonus = masterApprenticeService.getCultivationEfficiencyBonus(apprenticeId)

        // Then - 师徒关系提供20%修炼效率加成
        assertEquals(20, bonus)
    }

    @Test
    fun `获取功法学习加成应返回30_对于师徒关系`() {
        // Given
        val relationshipService = RelationshipService()
        val masterApprenticeService = MasterApprenticeService()
        masterApprenticeService.apply {
            javaClass.getDeclaredField("relationshipService").apply {
                isAccessible = true
                set(masterApprenticeService, relationshipService)
            }
        }
        val masterId = 1L
        val apprenticeId = 2L
        masterApprenticeService.apprenticeToMaster(apprenticeId, masterId)

        // When
        val bonus = masterApprenticeService.getSkillLearningBonus(apprenticeId)

        // Then - 师徒关系提供30%功法学习加成
        assertEquals(30, bonus)
    }

    @Test
    fun `没有师父时获取修炼效率加成应返回0`() {
        // Given
        val relationshipService = RelationshipService()
        val masterApprenticeService = MasterApprenticeService()
        masterApprenticeService.apply {
            javaClass.getDeclaredField("relationshipService").apply {
                isAccessible = true
                set(masterApprenticeService, relationshipService)
            }
        }
        val apprenticeId = 2L

        // When
        val bonus = masterApprenticeService.getCultivationEfficiencyBonus(apprenticeId)

        // Then
        assertEquals(0, bonus)
    }

    @Test
    fun `没有师父时获取功法学习加成应返回0`() {
        // Given
        val relationshipService = RelationshipService()
        val masterApprenticeService = MasterApprenticeService()
        masterApprenticeService.apply {
            javaClass.getDeclaredField("relationshipService").apply {
                isAccessible = true
                set(masterApprenticeService, relationshipService)
            }
        }
        val apprenticeId = 2L

        // When
        val bonus = masterApprenticeService.getSkillLearningBonus(apprenticeId)

        // Then
        assertEquals(0, bonus)
    }

    @Test
    fun `重复拜师应返回false`() {
        // Given
        val relationshipService = RelationshipService()
        val masterApprenticeService = MasterApprenticeService()
        masterApprenticeService.apply {
            javaClass.getDeclaredField("relationshipService").apply {
                isAccessible = true
                set(masterApprenticeService, relationshipService)
            }
        }
        val masterId1 = 1L
        val masterId2 = 3L
        val apprenticeId = 2L
        masterApprenticeService.apprenticeToMaster(apprenticeId, masterId1)

        // When - 尝试拜另一个师父
        val result = masterApprenticeService.apprenticeToMaster(apprenticeId, masterId2)

        // Then - 应该失败，因为已经有师父了
        assertFalse(result)
        assertEquals(masterId1, masterApprenticeService.getMaster(apprenticeId))
    }
}
