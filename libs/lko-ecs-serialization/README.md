# lko-ecs-serialization

基于Kotlin Serialization的lko-ecs高性能序列化框架，支持实体、组件和系统数据的完整序列化与反序列化操作。

## 特性

- ✅ **多格式支持**: JSON、CBOR等多种序列化格式
- ✅ **类型安全**: 编译时类型检查，避免运行时错误
- ✅ **高性能**: Archetype-aware序列化、零拷贝、增量序列化、对象池
- ✅ **版本管理**: 支持数据格式版本迁移和兼容性处理
- ✅ **数据验证**: 完整的数据验证机制，确保数据完整性
- ✅ **错误处理**: 完善的错误处理流程，包括异常捕获和友好提示
- ✅ **易用性**: 流畅的DSL API，简化配置和使用

## 快速开始

### 1. 添加依赖

在`build.gradle.kts`中添加依赖：

```kotlin
dependencies {
    implementation(projects.libs.lkoEcsSerialization)
}
```

### 2. 配置序列化

```kotlin
val world = world {
    serialization {
        components {
            component(Position.serializer())
            component(Velocity.serializer())
            component(PlayerInfo.serializer())
        }
        format("json", JsonFormat())
        format("cbor", CborFormat())
        config = SerializationConfig(
            enableValidation = true,
            enableVersioning = true,
            onMissingSerializer = OnMissingStrategy.WARN
        )
    }
}
```

### 3. 序列化实体

```kotlin
val entity = world.entity {
    setPersisting(context.serialization, Position(0f, 0f))
    setPersisting(context.serialization, Velocity(1f, 1f))
    setPersisting(context.serialization, PlayerInfo("Player1", 100))
}

val jsonData = context.serialization.serialize(entity, JsonFormat())
val cborData = context.serialization.serialize(entity, CborFormat())
```

### 4. 反序列化实体

```kotlin
val restoredEntity = context.serialization.deserialize<Entity>(
    EntitySerializer(context),
    jsonData,
    JsonFormat()
)
```

## 核心API

### SerializationConfig

序列化配置类，控制序列化行为。

```kotlin
data class SerializationConfig(
    val enableValidation: Boolean = true,           // 启用数据验证
    val enableVersioning: Boolean = true,          // 启用版本管理
    val enableCompression: Boolean = false,          // 启用压缩
    val onMissingSerializer: OnMissingStrategy,       // 缺失序列化器策略
    val onValidationError: OnValidationError,          // 验证错误策略
    val skipMalformedComponents: Boolean = true,      // 跳过格式错误的组件
    val namespaces: List<String>,                   // 命名空间列表
    val prefix: String                              // 序列化名称前缀
)
```

### ComponentSerializers

组件序列化器接口，管理组件的序列化和反序列化。

```kotlin
interface ComponentSerializers {
    val module: SerializersModule
    fun getClassFor(serialName: String): KClass<out Component>
    fun <T : Component> getSerializerFor(key: String): DeserializationStrategy<T>?
    fun getSerialNameFor(kClass: KClass<out Component>): String?
}
```

### Format

序列化格式接口，支持多种序列化格式。

```kotlin
interface Format {
    val ext: String              // 文件扩展名
    val mimeType: String          // MIME类型
    fun <T> encode(serializer: SerializationStrategy<T>, value: T): ByteArray
    fun <T> decode(deserializer: DeserializationStrategy<T>, data: ByteArray): T
}
```

### EntitySerializer

实体序列化器，负责实体的序列化和反序列化。

```kotlin
class EntitySerializer(
    private val context: SerializationContext
) : KSerializer<Entity> {
    override fun serialize(encoder: Encoder, value: Entity)
    override fun deserialize(decoder: Decoder): Entity
}
```

## 使用场景

### 1. 网络传输

```kotlin
class NetworkService(
    private val context: SerializationContext
) {
    fun sendEntity(entity: Entity) {
        val data = context.serialization.serialize(entity, CborFormat())
        networkSocket.send(data)
    }

    fun receiveEntity(data: ByteArray): Entity {
        return context.serialization.deserialize<Entity>(
            EntitySerializer(context),
            data,
            CborFormat()
        )
    }
}
```

### 2. 本地存储

```kotlin
class StorageService(
    private val context: SerializationContext
) {
    fun saveEntity(entity: Entity, filename: String) {
        val data = context.serialization.serialize(entity, JsonFormat(prettyPrint = true))
        File(filename).writeBytes(data)
    }

    fun loadEntity(filename: String): Entity {
        val data = File(filename).readBytes()
        return context.serialization.deserialize<Entity>(
            EntitySerializer(context),
            data,
            JsonFormat()
        )
    }
}
```

### 3. 批量序列化

```kotlin
class BatchSerializationService(
    private val context: SerializationContext
) {
    fun serializeEntities(entities: List<Entity>): List<ByteArray> {
        val archetypeSerializer = ArchetypeAwareSerializer(context)
        return archetypeSerializer.serializeEntities(entities)
    }

    fun deserializeEntities(dataList: List<ByteArray>): List<Entity> {
        val archetypeSerializer = ArchetypeAwareSerializer(context)
        return archetypeSerializer.deserializeEntities(dataList)
    }
}
```

### 4. 增量序列化

```kotlin
class IncrementalService(
    private val context: SerializationContext
) {
    private val incrementalSerializer = IncrementalSerializer(context)

    fun serializeEntity(entity: Entity): ByteArray? {
        return incrementalSerializer.serializeIncremental(entity)
    }

    fun deserializeEntity(entityId: String, data: ByteArray): Entity {
        return incrementalSerializer.deserializeIncremental(entityId, data)
    }

    fun serializeBatch(entities: List<Entity>): Map<Entity, ByteArray> {
        return incrementalSerializer.serializeBatch(entities)
    }
}
```

## 性能优化

### Archetype-aware序列化

利用lko-ecs的Archetype机制，批量序列化相同组件组合的实体，提高缓存命中率。

```kotlin
val archetypeSerializer = ArchetypeAwareSerializer(context)
val dataList = archetypeSerializer.serializeEntities(entities)
```

### 增量序列化

只序列化变化的组件，减少数据传输量。

```kotlin
val incrementalSerializer = IncrementalSerializer(context)
val data = incrementalSerializer.serializeIncremental(entity)
if (data != null) {
    // 只有数据变化时才序列化
    sendToServer(data)
}
```

### 对象池

复用序列化过程中的临时对象，减少GC压力。

```kotlin
val objectPool = SerializationObjectPool(context)
val json = objectPool.acquireJson(prettyPrint = true)
try {
    val data = json.encodeToString(serializer, value)
} finally {
    objectPool.releaseJson(json)
}
```

## 数据验证

### Schema验证

定义组件的模式，验证数据完整性。

```kotlin
val schemaValidator = SchemaValidator(
    schemas = mapOf(
        Position::class to Schema(
            kClass = Position::class,
            requiredFields = listOf("x", "y"),
            validators = mapOf(
                "x" to RangeValidator(0f, 1000f),
                "y" to RangeValidator(0f, 1000f)
            )
        )
    ),
    context = context
)

val result = schemaValidator.validate(position)
if (!result.isValid) {
    println("Validation errors: ${result.errors}")
}
```

### 自定义验证器

实现自定义验证逻辑。

```kotlin
class CustomValidator : DataValidator<MyComponent> {
    override fun validate(data: MyComponent): ValidationResult {
        return if (data.value > 0 && data.value < 100) {
            ValidationResult.success()
        } else {
            ValidationResult.error("Value must be between 0 and 100")
        }
    }
}
```

## 错误处理

### 错误处理器

配置错误处理策略。

```kotlin
val errorHandler = DefaultErrorHandler(
    logErrors = true,
    throwOnCriticalErrors = true
)

val result = errorHandler.handle(exception, context)
if (result.shouldContinue) {
    // 使用fallback值继续处理
    val fallbackValue = result.fallbackValue
}
```

### 重试机制

自动重试失败的序列化操作。

```kotlin
val retryHandler = RetryErrorHandler(
    maxRetries = 3,
    delegate = DefaultErrorHandler()
)

val result = retryHandler.handle(exception, context)
```

## 版本管理

### 版本迁移

注册版本迁移处理器。

```kotlin
val versionManager = VersionManager()

versionManager.registerMigration(
    from = Version(1, 0, 0),
    to = Version(2, 0, 0)
) { oldData ->
    // 迁移逻辑
    migrateToV2(oldData)
}

val migratedData = versionManager.migrate(data, "1.0.0", "2.0.0")
```

### 版本兼容性检查

检查数据版本兼容性。

```kotlin
if (versionManager.isCompatible(dataVersion)) {
    // 版本兼容，可以反序列化
    val entity = deserialize(data)
} else {
    // 版本不兼容，需要迁移
    val migratedData = versionManager.migrate(data, dataVersion)
    val entity = deserialize(migratedData)
}
```

## 最佳实践

### 1. 组件设计

- 使用`@Serializable`注解标记可序列化组件
- 优先使用不可变数据结构（data class、value class）
- 避免循环引用
- 为复杂类型提供自定义序列化器

```kotlin
@Serializable
data class Position(
    val x: Float,
    val y: Float
)

@Serializable
data class PlayerInfo(
    val name: String,
    val health: Int
)
```

### 2. 性能优化

- 使用Archetype-aware序列化批量处理实体
- 启用增量序列化减少数据传输
- 使用对象池复用临时对象
- 选择合适的序列化格式（CBOR用于性能，JSON用于可读性）

```kotlin
val archetypeSerializer = ArchetypeAwareSerializer(context)
val dataList = archetypeSerializer.serializeEntities(entities)

val incrementalSerializer = IncrementalSerializer(context)
val data = incrementalSerializer.serializeIncremental(entity)
```

### 3. 错误处理

- 配置适当的错误处理策略
- 记录序列化错误便于调试
- 提供合理的fallback值
- 避免因单个错误导致整个序列化失败

```kotlin
val config = SerializationConfig(
    onMissingSerializer = OnMissingStrategy.WARN,
    skipMalformedComponents = true
)

val errorHandler = DefaultErrorHandler(
    logErrors = true,
    throwOnCriticalErrors = false
)
```

### 4. 版本管理

- 始终包含版本信息
- 提供版本迁移路径
- 保持向后兼容性
- 文档化版本变更

```kotlin
@Serializable
data class ComponentV1(
    val version: String = "1.0",
    val data: String
)

@Serializable
data class ComponentV2(
    val version: String = "2.0",
    val data: String,
    val metadata: Map<String, String>
)
```

## 与geary-serialization兼容性

本框架与geary-serialization保持核心功能逻辑的兼容性：

- ✅ 组件序列化机制
- ✅ 多态序列化支持
- ✅ 实体序列化扩展
- ✅ 格式抽象接口
- ✅ 配置和验证机制

针对lko-ecs的深度适配：

- ✅ Archetype-aware序列化优化
- ✅ 零拷贝查询集成
- ✅ 增量序列化支持
- ✅ 对象池复用机制
- ✅ lko-ecs DSL集成

## 性能基准

| 操作类型 | 基准要求 | 测试方法 |
|---------|----------|----------|
| 实体序列化 | ≥50,000实体/秒 | 批量序列化1000个实体 |
| 组件序列化 | ≥100,000组件/秒 | 序列化10000个组件 |
| 反序列化 | ≥80,000实体/秒 | 反序列化1000个实体 |
| 内存占用 | ≤64字节/实体 | 测量序列化后数据大小 |

## 故障排除

### 常见问题

1. **序列化失败**
   - 检查组件是否标记为`@Serializable`
   - 验证序列化器是否已注册
   - 检查数据是否包含循环引用

2. **反序列化失败**
   - 验证数据格式是否正确
   - 检查版本兼容性
   - 确认所有必需的序列化器已注册

3. **性能问题**
   - 使用Archetype-aware序列化
   - 启用增量序列化
   - 使用对象池减少GC压力
   - 选择合适的序列化格式

### 调试技巧

```kotlin
// 启用详细日志
val config = SerializationConfig(
    enableValidation = true,
    onMissingSerializer = OnMissingStrategy.ERROR
)

// 使用性能监控
val stats = archetypeSerializer.getCacheStats()
println("Archetype cache size: ${stats.archetypeCacheSize}")
println("Entity cache size: ${stats.entityCacheSize}")
```

## 贡献

欢迎贡献代码、报告问题或提出改进建议。

## 许可证

与lko-ecs保持相同的许可证。