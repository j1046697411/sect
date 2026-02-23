package cn.jzl.sect.facility.services

import cn.jzl.ecs.World
import cn.jzl.ecs.entity.EntityRelationContext
import cn.jzl.sect.core.facility.FacilityType
import cn.jzl.sect.engine.SectWorld
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * 设施使用服务测试类
 */
class FacilityUsageServiceTest : EntityRelationContext {
    override lateinit var world: World

    @BeforeTest
    fun setup() {
        world = SectWorld.create("测试宗门")
    }

    @Test
    fun `使用修炼室应增加修炼效率`() {
        // Given
        val service = FacilityUsageService(world)

        // When
        val effect = service.get