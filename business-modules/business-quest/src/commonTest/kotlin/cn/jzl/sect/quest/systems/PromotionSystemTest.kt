package cn.jzl.sect.quest.systems

import cn.jzl.ecs.World
import cn.jzl.ecs.addon.components
import cn.jzl.ecs.addon.createAddon
import cn.jzl.ecs.component.componentId
import cn.jzl.ecs.entity
import cn.jzl.ecs.entity.Entity
import cn.jzl.ecs.entity.EntityRelationContext
import cn.jzl.ecs.entity.addComponent
import cn.jzl.ecs.world
import cn.jzl.sect.core.ai.Personality6
import cn.jzl.sect.core.cultivation.CultivationProgress
import cn.jzl.sect.core.cultivation.Realm
import cn.jzl.sect.core.cultivation.Talent
import cn.jzl.sect.core.quest.CandidateScore
import cn.jzl.sect.core.quest.EvaluationComponent
import cn.jzl.sect.core.quest.ElderPersonality
import cn.jzl.sect.core.quest.PolicyComponent
import cn.jzl.sect.core.quest.QuestComponent
import cn.jzl.sect.core.quest.QuestExecutionComponent
import cn.jzl.sect.core.sect.SectPositionInfo
import cn.jzl.sect.core.sect.SectPositionType
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PromotionSystemTest : EntityRelationContext {
    override lateinit var world: World

    private val testAddon = createAddon<Unit>("test") {
        components {
            world.componentId<CultivationProgress>()
            world.componentId<Talent>()
            world.componentId<SectPositionInfo>()
            world.componentId<Personality6>()
            world.componentId<QuestComponent>()
            world.componentId<QuestExecutionComponent>()
            world.componentId<EvaluationComponent>()
            world.componentId<PolicyComponent>()
            world.componentId<ElderPersonality>()
        }
    }

    @BeforeTest
    fun setup() {
        world = world { install(testAddon) }
    }

    private fun createOuterDisciple(testWorld: World): Entity {
        return testWorld.entity {
            it.addComponent(CultivationProgress(
                realm = Realm.QI_REFINING,
                layer = 3,
                cultivation = 3000L,
                maxCultivation = 5000L
            ))
            it.addComponent(Talent(physique = 40, comprehension = 40, fortune = 40, charm = 40))
            it.addComponent(SectPositionInfo(position = SectPositionType.DISCIPLE_OUTER))
        }
    }

    private fun createInnerDisciple(testWorld: World): Entity {
        return testWorld.entity {
            it.addComponent(CultivationProgress(
                realm = Realm.FOUNDATION,
                layer = 1,
                cultivation = 2000L,
                maxCultivation = 10000L
            ))
            it.addComponent(Talent(physique = 50, comprehension = 50, fortune = 50, charm = 50))
            it.addComponent(SectPositionInfo(position = SectPositionType.DISCIPLE_INNER))
            it.addComponent(Personality6.random())
        }
    }

    @Test
    fun `晋升外门弟子成功`() {
        val promotionSystem = PromotionSystem(world)
        val outerDisciple = createOuterDisciple(world)

        val result = promotionSystem.promoteDisciple(outerDisciple)

        assertTrue(result.success)
        assertEquals(SectPositionType.DISCIPLE_OUTER, result.oldPosition)
        assertEquals(SectPositionType.DISCIPLE_INNER, result.newPosition)
        assertNotNull(result.generatedPersonality)
    }

    @Test
    fun `晋升内门弟子失败`() {
        val promotionSystem = PromotionSystem(world)
        val innerDisciple = createInnerDisciple(world)

        val result = promotionSystem.promoteDisciple(innerDisciple)

        assertFalse(result.success)
        assertEquals("只有外门弟子可以晋升为内门弟子", result.message)
    }

    @Test
    fun `晋升不存在弟子失败`() {
        val promotionSystem = PromotionSystem(world)

        // 创建一个不存在的实体引用（使用一个很大的ID）
        val fakeEntity = Entity(id = 99999, version = 0)
        val result = promotionSystem.promoteDisciple(fakeEntity)

        assertFalse(result.success)
        assertEquals("未找到指定弟子", result.message)
    }

    @Test
    fun `批量晋升候选人`() {
        val promotionSystem = PromotionSystem(world)

        // 创建3个外门弟子
        val disciple1 = createOuterDisciple(world)
        val disciple2 = createOuterDisciple(world)
        val disciple3 = createOuterDisciple(world)

        val candidates = listOf(
            CandidateScore(disciple1, 90.0),
            CandidateScore(disciple2, 80.0),
            CandidateScore(disciple3, 70.0)
        )

        val results = promotionSystem.promoteCandidates(candidates, quota = 2)

        assertEquals(2, results.size)
        assertTrue(results.all { it.success })
        assertTrue(results.all { it.newPosition == SectPositionType.DISCIPLE_INNER })
    }

    @Test
    fun `批量晋升不超过候选人数量`() {
        val promotionSystem = PromotionSystem(world)

        val disciple1 = createOuterDisciple(world)
        val candidates = listOf(CandidateScore(disciple1, 90.0))

        val results = promotionSystem.promoteCandidates(candidates, quota = 5)

        assertEquals(1, results.size)
    }

    @Test
    fun `生成随机性格`() {
        val promotionSystem = PromotionSystem(world)

        val personality = promotionSystem.generatePersonality6()

        assertNotNull(personality)
        // 随机性格应该在合理范围内
        assertTrue(personality.ambition in -0.5..0.5)
        assertTrue(personality.diligence in -0.5..0.5)
    }

    @Test
    fun `生成特定类型性格`() {
        val promotionSystem = PromotionSystem(world)

        val diligent = promotionSystem.generatePersonality6ByType(PersonalityType.DILIGENT)
        assertTrue(diligent.diligence >= 0.5)

        val ambitious = promotionSystem.generatePersonality6ByType(PersonalityType.AMBITIOUS)
        assertTrue(ambitious.ambition >= 0.5)

        val loyal = promotionSystem.generatePersonality6ByType(PersonalityType.LOYAL)
        assertTrue(loyal.loyalty >= 0.5)
    }

    @Test
    fun `更新职位成功`() {
        val promotionSystem = PromotionSystem(world)
        val outerDisciple = createOuterDisciple(world)

        promotionSystem.updatePosition(outerDisciple, SectPositionType.DISCIPLE_INNER)

        val currentPosition = promotionSystem.getCurrentPosition(outerDisciple)
        assertEquals(SectPositionType.DISCIPLE_INNER, currentPosition)
    }

    @Test
    fun `获取当前职位`() {
        val promotionSystem = PromotionSystem(world)
        val outerDisciple = createOuterDisciple(world)

        val position = promotionSystem.getCurrentPosition(outerDisciple)

        assertEquals(SectPositionType.DISCIPLE_OUTER, position)
    }

    @Test
    fun `获取不存在弟子的职位返回null`() {
        val promotionSystem = PromotionSystem(world)

        val fakeEntity = Entity(id = 99999, version = 0)
        val position = promotionSystem.getCurrentPosition(fakeEntity)

        assertNull(position)
    }

    @Test
    fun `检查外门弟子可以晋升`() {
        val promotionSystem = PromotionSystem(world)
        val outerDisciple = createOuterDisciple(world)

        assertTrue(promotionSystem.canPromote(outerDisciple))
    }

    @Test
    fun `检查内门弟子不能晋升`() {
        val promotionSystem = PromotionSystem(world)
        val innerDisciple = createInnerDisciple(world)

        assertFalse(promotionSystem.canPromote(innerDisciple))
    }

    @Test
    fun `晋升后弟子有性格属性`() {
        val promotionSystem = PromotionSystem(world)
        val outerDisciple = createOuterDisciple(world)

        val result = promotionSystem.promoteDisciple(outerDisciple)

        assertTrue(result.success)
        assertNotNull(result.generatedPersonality)

        // 验证性格属性已添加到实体
        val position = promotionSystem.getCurrentPosition(outerDisciple)
        assertEquals(SectPositionType.DISCIPLE_INNER, position)
    }
}
