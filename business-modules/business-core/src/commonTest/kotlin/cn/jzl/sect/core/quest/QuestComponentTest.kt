package cn.jzl.sect.core.quest

import cn.jzl.ecs.*
import cn.jzl.ecs.addon.components
import cn.jzl.ecs.addon.createAddon
import cn.jzl.ecs.component.componentId
import cn.jzl.ecs.entity.*
import kotlin.test.*

class QuestComponentTest : EntityRelationContext {
    override lateinit var world: World

    private val testAddon = createAddon<Unit>("test") {
        components {
            world.componentId<QuestComponent>()
        }
    }

    @BeforeTest
    fun setup() {
        world = world { install(testAddon) }
    }

    @Test
    fun testQuestTypeEnumValues() {
        assertEquals(QuestType.RESOURCE_COLLECTION, QuestType.valueOf("RESOURCE_COLLECTION"))
        assertEquals(QuestType.FACILITY_CONSTRUCTION, QuestType.valueOf("FACILITY_CONSTRUCTION"))
        assertEquals(QuestType.RUIN_EXPLORATION, QuestType.valueOf("RUIN_EXPLORATION"))
        assertEquals(QuestType.BEAST_HUNT, QuestType.valueOf("BEAST_HUNT"))
    }

    @Test
    fun testQuestDifficultyEnumValues() {
        assertEquals(QuestDifficulty.EASY, QuestDifficulty.valueOf("EASY"))
        assertEquals(QuestDifficulty.NORMAL, QuestDifficulty.valueOf("NORMAL"))
        assertEquals(QuestDifficulty.HARD, QuestDifficulty.valueOf("HARD"))
    }

    @Test
    fun testQuestStatusEnumValues() {
        assertEquals(QuestStatus.PENDING_APPROVAL, QuestStatus.valueOf("PENDING_APPROVAL"))
        assertEquals(QuestStatus.IN_PROGRESS, QuestStatus.valueOf("IN_PROGRESS"))
        assertEquals(QuestStatus.COMPLETED, QuestStatus.valueOf("COMPLETED"))
        assertEquals(QuestStatus.CANCELLED, QuestStatus.valueOf("CANCELLED"))
    }

    @Test
    fun testQuestComponentCreation() {
        val entity = world.entity {
            it.addComponent(QuestComponent(
                questId = 1L,
                type = QuestType.BEAST_HUNT,
                difficulty = QuestDifficulty.HARD,
                status = QuestStatus.IN_PROGRESS,
                createdAt = 1000L,
                maxParticipants = 5,
                description = "猎杀妖兽任务"
            ))
        }

        val quest = entity.getComponent<QuestComponent>()
        assertEquals(1L, quest.questId)
        assertEquals(QuestType.BEAST_HUNT, quest.type)
        assertEquals(QuestDifficulty.HARD, quest.difficulty)
        assertEquals(QuestStatus.IN_PROGRESS, quest.status)
        assertEquals(1000L, quest.createdAt)
        assertEquals(5, quest.maxParticipants)
        assertEquals("猎杀妖兽任务", quest.description)
    }

    @Test
    fun testQuestTypeDisplayName() {
        assertEquals("资源采集", QuestType.RESOURCE_COLLECTION.displayName)
        assertEquals("设施建设", QuestType.FACILITY_CONSTRUCTION.displayName)
        assertEquals("遗迹探索", QuestType.RUIN_EXPLORATION.displayName)
        assertEquals("妖兽猎杀", QuestType.BEAST_HUNT.displayName)
    }

    @Test
    fun testQuestDifficultyDisplayName() {
        assertEquals("简单", QuestDifficulty.EASY.displayName)
        assertEquals("普通", QuestDifficulty.NORMAL.displayName)
        assertEquals("困难", QuestDifficulty.HARD.displayName)
    }

    @Test
    fun testQuestStatusDisplayName() {
        assertEquals("待审批", QuestStatus.PENDING_APPROVAL.displayName)
        assertEquals("进行中", QuestStatus.IN_PROGRESS.displayName)
        assertEquals("已完成", QuestStatus.COMPLETED.displayName)
        assertEquals("已取消", QuestStatus.CANCELLED.displayName)
    }
}
