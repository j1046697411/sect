# 设计文档：cultivation 模块突破事件化重构

## 问题背景

cultivation 模块存在隐式耦合问题：
- `CultivationService` 通过 `update()` 返回事件列表
- 调用方需要主动处理返回的事件
- 不符合 ECS 事件驱动的设计理念

## 解决方案

使用 ECS 框架的观察者系统，将突破事件改为发布/订阅模式。

## 设计详情

### 1. 事件定义

**位置**: `business-cultivation/src/commonMain/kotlin/cn/jzl/sect/cultivation/events/`

```kotlin
// BreakthroughSuccessEvent.kt
data class BreakthroughSuccessEvent(
    val oldRealm: Realm,
    val oldLayer: Int,
    val newRealm: Realm,
    val newLayer: Int
)

// BreakthroughFailedEvent.kt
data class BreakthroughFailedEvent(
    val currentRealm: Realm,
    val currentLayer: Int,
    val attemptedRealm: Realm,
    val attemptedLayer: Int
)
```

**设计要点**：
- 两个独立事件类，类型安全
- 纯数据类，无业务逻辑
- 不包含实体引用（通过观察者上下文获取）

### 2. Service 修改

**修改前**:
```kotlin
fun update(hours: Int): List<BreakthroughEvent> {
    // ... 返回突破事件列表
}
```

**修改后**:
```kotlin
fun update(hours: Int) {
    // 突破成功时
    world.emit(entity, BreakthroughSuccessEvent(oldRealm, oldLayer, newRealm, newLayer))
    
    // 突破失败时
    world.emit(entity, BreakthroughFailedEvent(currentRealm, currentLayer, attemptedRealm, attemptedLayer))
}
```

**修改要点**：
- 移除返回值
- 使用 `world.emit()` 发送事件
- 保持内部日志功能

### 3. 订阅示例

```kotlin
// 其他模块订阅示例
world.observeWithData<BreakthroughSuccessEvent>().exec {
    // this.entity - 突破的弟子实体
    // this.event - BreakthroughSuccessEvent 数据
    updateDiscipleAttribute(this.entity, this.event.newRealm)
}
```

**订阅要点**：
- 各模块在各自的 Addon 中注册订阅
- 通过 `world.observeWithData<Event>()` 订阅
- 观察者上下文包含实体引用

## 文件变更

| 文件 | 变更类型 | 说明 |
|------|----------|------|
| `events/BreakthroughSuccessEvent.kt` | 新增 | 突破成功事件 |
| `events/BreakthroughFailedEvent.kt` | 新增 | 突破失败事件 |
| `services/CultivationService.kt` | 修改 | 改用 emit 发送事件 |
| `CultivationAddon.kt` | 修改 | 注册事件类型 |
| `AGENTS.md` | 更新 | 更新文档 |

## 测试要点

- [ ] 突破成功时发送 `BreakthroughSuccessEvent`
- [ ] 突破失败时发送 `BreakthroughFailedEvent`
- [ ] 事件数据正确
- [ ] 订阅者能正确接收事件

## 风险评估

| 风险 | 影响 | 缓解措施 |
|------|------|----------|
| 无调用方依赖返回值 | 低 | 已确认无依赖 |
| 事件类型未注册 | 中 | 在 Addon 中确保注册 |

## 后续步骤

1. 实现事件类
2. 修改 CultivationService
3. 更新测试用例
4. 更新文档
