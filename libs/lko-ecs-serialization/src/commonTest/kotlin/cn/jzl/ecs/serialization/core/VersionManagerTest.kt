package cn.jzl.ecs.serialization.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class VersionManagerTest {

    @Test
    fun `test version parsing`() {
        val version = Version.parse("1.2.3")
        assertEquals(1, version.major)
        assertEquals(2, version.minor)
        assertEquals(3, version.patch)
        assertEquals("1.2.3", version.toString())
    }

    @Test
    fun `test version compatibility`() {
        val v100 = Version(1, 0, 0)
        val v110 = Version(1, 1, 0)
        val v120 = Version(1, 2, 0)
        val v200 = Version(2, 0, 0)

        assertTrue(v100.isCompatible(v100))
        assertTrue(v100.isCompatible(v110))
        assertTrue(v110.isCompatible(v120))
        assertTrue(v100.isCompatible(v120))

        assertFalse(v100.isCompatible(v200))
        assertFalse(v110.isCompatible(v200))
    }

    @Test
    fun `test version comparison`() {
        val v100 = Version(1, 0, 0)
        val v110 = Version(1, 1, 0)
        val v101 = Version(1, 0, 1)
        val v200 = Version(2, 0, 0)

        assertTrue(v110.isGreaterThan(v100))
        assertTrue(v101.isGreaterThan(v100))
        assertTrue(v200.isGreaterThan(v110))
        assertFalse(v100.isGreaterThan(v110))
        assertFalse(v100.isGreaterThan(v101))
    }

    @Test
    fun `test version migration`() {
        val versionManager = VersionManager()

        versionManager.registerMigration(
            from = Version(1, 0, 0),
            to = Version(2, 0, 0)
        ) { oldData ->
            "migrated_$oldData"
        }

        val result = versionManager.migrate("test_data", "1.0.0", "2.0.0")
        assertEquals("migrated_test_data", result)
    }

    @Test
    fun `test version migration chain`() {
        val versionManager = VersionManager()

        versionManager.registerMigration(
            from = Version(1, 0, 0),
            to = Version(1, 1, 0)
        ) { oldData -> "v1.1_$oldData" }

        versionManager.registerMigration(
            from = Version(1, 1, 0),
            to = Version(2, 0, 0)
        ) { oldData -> "v2.0_$oldData" }

        val result = versionManager.migrate("test_data", "1.0.0", "2.0.0")
        assertEquals("v2.0_v1.1_test_data", result)
    }

    @Test
    fun `test version manager current version`() {
        val versionManager = VersionManager()
        assertEquals("1.0.0", versionManager.getCurrentVersion())
    }

    @Test
    fun `test version manager custom version`() {
        val customVersion = Version(2, 5, 10)
        val versionManager = VersionManager(customVersion)
        assertEquals("2.5.10", versionManager.getCurrentVersion())
    }
}