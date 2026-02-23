# lko-ecs-serialization - ECS 序列化支持

## 模块定位
ECS 实体序列化框架，支持多格式（JSON、CBOR）持久化和网络传输。

**层级**: 可选扩展层（依赖 lko-ecs）

## 核心职责
- 实体序列化与反序列化
- 多格式支持（JSON、CBOR）
- 版本管理（版本迁移）
- 组件序列化器注册
- 数据验证机制

## 目录结构
```
lko-ecs-serialization/src/commonMain/kotlin/cn/jzl/ecs/serialization/
├── core/                  # 核心接口
│   ├── SerializationContext.kt
│   ├── SerializationConfig.kt
│   ├── ComponentSerializers.kt
│   ├── VersionManager.kt
│   ├── OnMissingStrategy.kt
│   └── OnValidationError.kt
│
├── format/                # 格式支持
│   ├── Format.kt
│   ├── JsonFormat.kt
│   ├── CborFormat.kt
│   └── Formats.kt
│
├── entity/                # 实体序列化
│   ├── EntitySerializer.kt
│   ├── EntitySerializationExtensions.kt
│   ├── PolymorphicComponentSerializer.kt
│   └── Persistable.kt
│
├── performance/           # 性能优化
│   ├── ArchetypeAwareSerializer.kt
│   ├── IncrementalSerializer.kt
│   └── SerializationObjectPool.kt
│
├── validation/            # 数据验证
│   ├── SchemaValidator.kt
│   └── DataValidator.kt
│
├── error/                 # 错误处理
│   ├── ErrorHandler.kt
│   └── SerializationException.kt
│
└── addon/                 # 插件集成
    ├── SerializationAddon.kt
    ├── SerializationModule.kt
    ├── SerializationBuilder.kt
    ├── ComponentSerializersBuilder.kt
    ├── ComponentSerializersImpl.kt
    └── WorldSerializationExt.kt
```

## 关键 API

### 序列化配置
```kotlin
// 创建序列化插件
val serializationAddon = createAddon("serialization") {
    configure {
        // 配置格式
        format = JsonFormat()
        
        // 配置版本
        version = 1
        
        // 配置缺失策略
        onMissing = OnMissingStrategy.SKIP
        
        // 配置验证策略
        onValidationError = OnValidationError.THROW
    }
    
    // 注册组件序列化器
    serializers {
        register<Position>(PositionSerializer)
        register<Health>(HealthSerializer)
    }
}
```

### 实体序列化
```kotlin
// 序列化单个实体
val data = entity.serialize()

// 序列化整个世界
val worldData = world.serialize()

// 反序列化
val entity = world.deserializeEntity(data)
world.deserializeWorld(worldData)
```

### 格式选择
```kotlin
// JSON 格式（可读，体积大）
val jsonFormat = JsonFormat()

// CBOR 格式（二进制，体积小）
val cborFormat = CborFormat()

// 配置格式
serializationAddon.configure {
    format = jsonFormat
}
```

### 版本迁移
```kotlin
// 注册版本迁移器
versionManager.registerMigration(1, 2) { data ->
    // 迁移逻辑
    data.copy(newField = data.oldField * 2)
}
```

## 使用方式

```kotlin
// 1. 安装序列化插件
world.install(serializationAddon)

// 2. 注册组件序列化器
world.serializers {
    register<Position>(PositionSerializer)
    register<Velocity>(VelocitySerializer)
}

// 3. 序列化实体
val savedData = entity.serialize()

// 4. 持久化
file.writeText(savedData.toString())

// 5. 加载并反序列化
val loadedData = file.readText()
val entity = world.deserializeEntity(loadedData)
```

## 依赖关系

```kotlin
// build.gradle.kts
dependencies {
    implementation(projects.libs.lkoEcs)
    implementation(libs.kotlinx.serialization.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.serialization.cbor)
}
```

## AI 开发指引

### 开发原则
- **多平台适配**: 确保序列化逻辑在所有 KMP 平台一致
- **版本兼容**: 修改组件结构时必须考虑向后兼容性
- **性能优化**: 使用 `ArchetypeAwareSerializer` 进行批量处理

### 序列化器实现
```kotlin
// 自定义序列化器
object PositionSerializer : ComponentSerializer<Position> {
    override fun serialize(component: Position): SerializedData {
        return mapOf("x" to component.x, "y" to component.y)
    }
    
    override fun deserialize(data: SerializedData): Position {
        return Position(data["x"] as Int, data["y"] as Int)
    }
}
```

### 版本兼容策略
| 场景 | 策略 | 说明 |
|------|------|------|
| 新增字段 | 提供默认值 | `data.getOrDefault("newField", 0)` |
| 删除字段 | 跳过 | `OnMissingStrategy.SKIP` |
| 重命名字段 | 迁移器 | 注册版本迁移 |

### 禁止事项
- ❌ 禁止修改已发布版本的序列化格式
- ❌ 禁止跳过版本迁移
- ❌ 禁止在序列化器中持有状态

## 测试要求
- 序列化/反序列化测试
- 版本迁移测试
- 多格式兼容测试
- 性能测试
- 边界条件测试
