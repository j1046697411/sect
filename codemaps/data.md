# 数据模型 - ECS 序列化与存储

**最后更新**: 2026-02-14
**入口点**: libs/lko-ecs-serialization/src/commonMain/kotlin/

## 序列化模块结构

### 核心组件

| 文件 | 用途 | 导出 |
|------|------|------|
| SerializationConfig.kt | 序列化配置 | 配置选项 |
| SerializationContext.kt | 序列化上下文 | 运行时上下文 |
| ComponentSerializers.kt | 组件序列化器 | 序列化/反序列化 |

### 格式支持

| 文件 | 格式 |
|------|------|
| JsonFormat.kt | JSON |
| CborFormat.kt | CBOR |
| Format.kt | 格式接口 |

### 验证

| 文件 | 用途 |
|------|------|
| SchemaValidator.kt | Schema 验证 |
| DataValidator.kt | 数据验证 |

### 性能优化

| 文件 | 用途 |
|------|------|
| IncrementalSerializer.kt | 增量序列化 |
| ArchetypeAwareSerializer.kt | 原型感知序列化 |
| SerializationObjectPool.kt | 对象池 |

## 组件序列化

### 基础组件

```kotlin
@Component
data class Position(val x: Float, val y: Float)

// 自动生成序列化器
@Serializable
data class Health(val current: Int, val max: Int)
```

### 多态组件

```kotlin
@Serializable
sealed class Item {
    @Serializable
    data class Weapon(val damage: Int) : Item()
    
    @Serializable
    data class Potion(val healing: Int) : Item()
}
```

## 序列化配置

```kotlin
val config = SerializationConfig {
    // 缺失组件策略
    onMissing = OnMissingStrategy.ERROR
    
    // 验证策略
    onValidationError = OnValidationError.WARN
    
    // 版本管理
    versionManager = VersionManager()
}
```

## 持久化

### 实体序列化

```kotlin
// 序列化实体
val entity = world.entity { ... }
val bytes = EntitySerializer.serialize(entity)

// 反序列化
val restored = EntitySerializer.deserialize(bytes)
```

### 世界保存/加载

```kotlin
// 保存世界
val saveData = WorldProviderSerializer.serialize(world)

// 加载世界
val world = WorldProviderSerializer.deserialize(saveData)
```

## 数据验证

### Schema 验证

```kotlin
val validator = SchemaValidator(schema)
val result = validator.validate(entityData)

if (!result.isValid) {
    println("Validation errors: ${result.errors}")
}
```

### 自定义验证器

```kotlin
data class HealthValidator : DataValidator<Health> {
    override fun validate(data: Health): ValidationResult {
        return when {
            data.current < 0 -> ValidationResult.Error("Current health cannot be negative")
            data.max <= 0 -> ValidationResult.Error("Max health must be positive")
            data.current > data.max -> ValidationResult.Error("Current exceeds max")
            else -> ValidationResult.Ok
        }
    }
}
```

## 外部依赖

| 依赖 | 用途 |
|------|------|
| kotlinx.serialization | 序列化框架 |
| kotlinx.cbor | CBOR 格式 |
| lko-ecs | ECS 核心 |
| lko-di | 依赖注入 |

## 相关代码地图

- [ECS 后端](backend.md)
- [整体架构](architecture.md)
- [ECS 架构详解](../docs/ecs-architecture.md)
