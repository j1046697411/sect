# lko-ecs-serialization

**Module:** lko-ecs-serialization

## Description

ECS 实体序列化框架，支持多格式（JSON、CBOR）持久化和网络传输。

## Responsibilities

- 实体序列化与反序列化
- 多格式支持（JSON、CBOR）
- 版本管理（版本迁移）
- 组件序列化器
- 数据验证

## Public API surface

### Core
- `SerializationConfig` - 序列化配置
- `SerializationContext` - 序列化上下文
- `VersionManager` - 版本管理器
- `ComponentSerializers` - 组件序列化器集合

### Format
- `Format` - 格式接口
- `JsonFormat` - JSON 格式
- `CborFormat` - CBOR 格式
- `Formats` - 格式工具

### Validation
- `SchemaValidator` - schema 验证器
- `DataValidator` - 数据验证器

### Error
- `ErrorHandler` - 错误处理器
- `SerializationException` - 序列化异常
- `OnValidationError` - 验证错误策略
- `OnMissingStrategy` - 缺失策略

### Entity
- `EntitySerializer` - 实体序列化器
- `PolymorphicComponentSerializer` - 多态组件序列化器
- `Persistable` - 可持久化标记

### Addon
- `SerializationAddon` - 序列化插件
- `SerializationBuilder` - 序列化构建器
- `SerializationModule` - 序列化模块

### Performance
- `IncrementalSerializer` - 增量序列化器
- `ArchetypeAwareSerializer` - 原型感知序列化器
- `SerializationObjectPool` - 对象池

## Dependencies

- `lko-ecs` - ECS 框架核心
- Kotlinx Serialization Core
- Kotlinx Serialization JSON
- Kotlinx Serialization CBOR

## Testing approach

- 单元测试覆盖核心功能
- 集成测试验证序列化/反序列化
- 目标覆盖率 80% 以上

## Code style guidelines

- 遵循项目通用 Kotlin 编码规范
- 包名：`cn.jzl.ecs.serialization`
- 类/接口：PascalCase
- 函数/属性：camelCase

## Migration/Compatibility

- 当前版本为初始版本，无迁移需求

## Contributing notes

- 新增序列化格式需实现 Format 接口
- 复杂组件需提供自定义序列化器
- 版本迁移需在 VersionManager 中声明
