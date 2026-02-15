# lko-ecs-serialization - AI 指引

## 模块定位
ECS 实体序列化框架，支持多格式（JSON、CBOR）持久化和网络传输。

## 核心职责
- 实体序列化与反序列化
- 多格式支持（JSON、CBOR）
- 版本管理（版本迁移）
- 组件序列化器注册
- 数据验证机制

## AI 开发指引
- **多平台适配**: 确保序列化逻辑在所有 KMP 平台一致。
- **版本兼容**: 修改组件结构时必须考虑向后兼容性。
- **性能优化**: 使用 `ArchetypeAwareSerializer` 进行批量处理。

## 关键 API
- `SerializationContext`: 序列化上下文
- `EntitySerializer`: 实体序列化核心
- `VersionManager`: 版本迁移管理
- `Format`: 序列化格式抽象
