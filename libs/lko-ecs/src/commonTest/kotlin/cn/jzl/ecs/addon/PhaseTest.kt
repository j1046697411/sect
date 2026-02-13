package cn.jzl.ecs.addon

import kotlin.test.Test
import kotlin.test.assertEquals

class PhaseTest {

    @Test
    fun testPhaseEntries() {
        assertEquals(5, Phase.entries.size)
    }

    @Test
    fun testPhaseOrder() {
        val phases = Phase.entries.toList()
        
        assertEquals(Phase.ADDONS_CONFIGURED, phases[0])
        assertEquals(Phase.INIT_COMPONENTS, phases[1])
        assertEquals(Phase.INIT_SYSTEMS, phases[2])
        assertEquals(Phase.INIT_ENTITIES, phases[3])
        assertEquals(Phase.ENABLE, phases[4])
    }

    @Test
    fun testPhaseOrdinal() {
        assertEquals(0, Phase.ADDONS_CONFIGURED.ordinal)
        assertEquals(1, Phase.INIT_COMPONENTS.ordinal)
        assertEquals(2, Phase.INIT_SYSTEMS.ordinal)
        assertEquals(3, Phase.INIT_ENTITIES.ordinal)
        assertEquals(4, Phase.ENABLE.ordinal)
    }

    @Test
    fun testPhaseName() {
        assertEquals("ADDONS_CONFIGURED", Phase.ADDONS_CONFIGURED.name)
        assertEquals("INIT_COMPONENTS", Phase.INIT_COMPONENTS.name)
        assertEquals("INIT_SYSTEMS", Phase.INIT_SYSTEMS.name)
        assertEquals("INIT_ENTITIES", Phase.INIT_ENTITIES.name)
        assertEquals("ENABLE", Phase.ENABLE.name)
    }

    @Test
    fun testPhaseValueOf() {
        assertEquals(Phase.ADDONS_CONFIGURED, Phase.valueOf("ADDONS_CONFIGURED"))
        assertEquals(Phase.INIT_COMPONENTS, Phase.valueOf("INIT_COMPONENTS"))
        assertEquals(Phase.INIT_SYSTEMS, Phase.valueOf("INIT_SYSTEMS"))
        assertEquals(Phase.INIT_ENTITIES, Phase.valueOf("INIT_ENTITIES"))
        assertEquals(Phase.ENABLE, Phase.valueOf("ENABLE"))
    }
}
