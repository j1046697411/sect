## ECS反模式

> 创建时间: 2026-02-23
> 分类: lessons
> 状态: active
> 标签: #ecs #陷阱

**问题/背景**:
ECS 开发中常见错误，需要避免。

**解决方案/内容**:

| 反模式 | 后果 | 正确做法 |
|--------|------|----------|
| Query 中修改实体 | 异常/数据错乱 | 先收集到列表再处理 |
| 未注册 ComponentId | 运行时崩溃 | createAddon 中注册 |
| 大组件（混合职责） | GC 压力 | 拆分为原子组件 |
| 单属性用 data class | 装箱开销 | @JvmInline value class |
| 直接修改组件属性 | 数据不一致 | 使用 copy() |
| Service 保存状态 | 状态不同步 | 状态存入组件 |
| 标签含数据 | 架构混乱 | sealed class 无数据 |

**示例**:
```kotlin
// ❌ 错误：大组件
data class PlayerStats(val name: String, val level: Int, val health: Int)

// ✅ 正确：原子组件
data class PlayerName(val value: String)
@JvmInline value class Level(val value: Int)
data class Health(val current: Int, val max: Int)
```

**关联记忆**: #conventions:ECS架构
