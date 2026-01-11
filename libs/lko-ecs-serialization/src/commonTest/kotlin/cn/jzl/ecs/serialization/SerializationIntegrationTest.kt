package cn.jzl.ecs.serialization

import cn.jzl.ecs.World
import cn.jzl.ecs.entity
import cn.jzl.ecs.serialization.addon.SerializationAddon
import cn.jzl.ecs.serialization.addon.serialization
import cn.jzl.ecs.serialization.core.SerializationConfig
import cn.jzl.ecs.serialization.core.Version
import cn.jzl.ecs.serialization.core.VersionManager
import cn.jzl.ecs.serialization.entity.EntitySerializer
import cn.jzl.ecs.serialization.entity.Persistable
import cn.jzl.ecs.serialization.format.CborFormat
import cn.jzl.ecs.serialization.format.JsonFormat
import cn.jzl.ecs.serialization.format.Format
import cn.jzl.ecs.world
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.encodeToString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@Serializable
data class TestPosition(val x: Float, val y: Float)

@Serializable
data class TestVelocity(val dx: Float, val dy: Float)

@Serializable
data class TestPlayerInfo(val name: String, val health: Int)

class SerializationIntegrationTest {

    private lateinit var world: World
    private lateinit var jsonFormat: JsonFormat
    private lateinit var cborFormat: CborFormat

    @Test
    fun `test serialization addon installation`() {
        world = world {
            serialization {
                components {
                    component(TestPosition.serializer())
                    component(TestVelocity.serializer())
                    component(TestPlayerInfo.serializer())
                }
                format("json", JsonFormat())
                format("cbor", CborFormat())
                config = SerializationConfig(
                    enableValidation = true,
                    onMissingSerializer = SerializationConfig.OnMissingStrategy.WARN
                )
            }
        }

        val serializationModule = SerializationAddon.onInstall(world, Unit)
        assertNotNull(serializationModule)
        assertNotNull(serializationModule.serializers)
        assertNotNull(serializationModule.formats)
    }

    @Test
    fun `test entity serialization with json`() {
        setupWorld()

        val entity = world.entity {
            setPersisting(world.serialization.context, TestPosition(10f, 20f))
            setPersisting(world.serialization.context, TestVelocity(1f, 1f))
            setPersisting(world.serialization.context, TestPlayerInfo("TestPlayer", 100))
        }

        val jsonData = world.serialization.serialize(
            EntitySerializer(world.serialization.context),
            entity,
            jsonFormat
        )

        assertNotNull(jsonData)
        assertTrue(jsonData.isNotEmpty())

        val jsonString = jsonData.decodeToString()
        assertTrue(jsonString.contains("TestPosition"))
        assertTrue(jsonString.contains("TestVelocity"))
        assertTrue(jsonString.contains("TestPlayerInfo"))
    }

    @Test
    fun `test entity serialization with cbor`() {
        setupWorld()

        val entity = world.entity {
            setPersisting(world.serialization.context, TestPosition(0f, 0f))
            setPersisting(world.serialization.context, TestVelocity(0f, 0f))
        }

        val cborData = world.serialization.serialize(
            EntitySerializer(world.serialization.context),
            entity,
            cborFormat
        )

        assertNotNull(cborData)
        assertTrue(cborData.isNotEmpty())
    }

    @Test
    fun `test entity deserialization with json`() {
        setupWorld()

        val originalEntity = world.entity {
            setPersisting(world.serialization.context, TestPosition(5f, 10f))
            setPersisting(world.serialization.context, TestVelocity(0.5f, 0.5f))
            setPersisting(world.serialization.context, TestPlayerInfo("Player1", 50))
        }

        val jsonData = world.serialization.serialize(
            EntitySerializer(world.serialization.context),
            originalEntity,
            jsonFormat
        )

        val restoredEntity = world.serialization.deserialize(
            EntitySerializer(world.serialization.context),
            jsonData,
            jsonFormat
        )

        val position = restoredEntity.get<TestPosition>()
        val velocity = restoredEntity.get<TestVelocity>()
        val playerInfo = restoredEntity.get<TestPlayerInfo>()

        assertNotNull(position)
        assertNotNull(velocity)
        assertNotNull(playerInfo)

        assertEquals(5f, position?.x)
        assertEquals(10f, position?.y)
        assertEquals(0.5f, velocity?.dx)
        assertEquals(0.5f, velocity?.dy)
        assertEquals("Player1", playerInfo?.name)
        assertEquals(50, playerInfo?.health)
    }

    @Test
    fun `test entity deserialization with cbor`() {
        setupWorld()

        val originalEntity = world.entity {
            setPersisting(world.serialization.context, TestPosition(1f, 2f))
            setPersisting(world.serialization.context, TestVelocity(0.1f, 0.2f))
        }

        val cborData = world.serialization.serialize(
            EntitySerializer(world.serialization.context),
            originalEntity,
            cborFormat
        )

        val restoredEntity = world.serialization.deserialize(
            EntitySerializer(world.serialization.context),
            cborData,
            cborFormat
        )

        val position = restoredEntity.get<TestPosition>()
        val velocity = restoredEntity.get<TestVelocity>()

        assertNotNull(position)
        assertNotNull(velocity)

        assertEquals(1f, position?.x)
        assertEquals(2f, position?.y)
        assertEquals(0.1f, velocity?.dx)
        assertEquals(0.2f, velocity?.dy)
    }

    @Test
    fun `test persistable component`() {
        setupWorld()

        val entity = world.entity {
            setPersisting(world.serialization.context, TestPosition(0f, 0f))
        }

        val persistingComponents = entity.getAllPersisting(world.serialization.context)

        assertTrue(persistingComponents.isNotEmpty())
        assertTrue(persistingComponents.any { it is TestPosition })
    }

    @Test
    fun `test version manager`() {
        val versionManager = VersionManager()

        assertEquals("1.0.0", versionManager.getCurrentVersion())
        assertTrue(versionManager.isCompatible("1.0.0"))
        assertTrue(versionManager.isCompatible("0.9.0"))
        assertTrue(versionManager.isCompatible("1.0.1"))
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
    fun `test format registration`() {
        setupWorld()

        val jsonFormat = world.serialization.formats.get("json")
        val cborFormat = world.serialization.formats.get("cbor")

        assertNotNull(jsonFormat)
        assertNotNull(cborFormat)
        assertEquals("json", jsonFormat?.ext)
        assertEquals("cbor", cborFormat?.ext)
    }

    @Test
    fun `test batch serialization`() {
        setupWorld()

        val entities = (1..100).map { i ->
            world.entity {
                setPersisting(world.serialization.context, TestPosition(i.toFloat(), i.toFloat()))
                setPersisting(world.serialization.context, TestVelocity(0.1f, 0.1f))
            }
        }

        val dataList = entities.map { entity ->
            world.serialization.serialize(
                EntitySerializer(world.serialization.context),
                entity,
                jsonFormat
            )
        }

        assertEquals(100, dataList.size)
        dataList.forEach { data ->
            assertNotNull(data)
            assertTrue(data.isNotEmpty())
        }
    }

    @Test
    fun `test serialization config`() {
        setupWorld()

        val config = world.serialization.config

        assertTrue(config.enableValidation)
        assertTrue(config.enableVersioning)
        assertEquals(SerializationConfig.OnMissingStrategy.WARN, config.onMissingSerializer)
    }

    private fun setupWorld() {
        world = world {
            serialization {
                components {
                    component(TestPosition.serializer())
                    component(TestVelocity.serializer())
                    component(TestPlayerInfo.serializer())
                }
                format("json", JsonFormat())
                format("cbor", CborFormat())
                config = SerializationConfig(
                    enableValidation = true,
                    onMissingSerializer = SerializationConfig.OnMissingStrategy.WARN
                )
            }
        }

        jsonFormat = JsonFormat()
        cborFormat = CborFormat()
    }
}