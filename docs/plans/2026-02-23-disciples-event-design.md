# 设计文档：disciples 模块事件化重构

## 问题背景

disciples 模块存在隐式耦合问题：
- `DiscipleInfoService` 直接查询 cultivation 模块的组件
- 突破事件发生时没有通知 disciples 模块

## 解决方案

采用"查询 + 事件更新结合"方案：
1. `getAllDisciples()` 保持查询方式（需要实时数据）
2. 订阅 cultivation 模块的突破事件，执行相关业务逻辑

## 设计详情

### 1. 订阅突破事件

在 `DisciplesAddon` 或初始化时订阅事件：

```kotlin
// 订阅突破成功事件
world.observeWithData<BreakthroughSuccessEvent>().exec {
    handleBreakthrough(this.entity, this.event)
}

// 订阅突破失败事件（可选）
world.observeWithData<BreakthroughFailedEvent>().exec {
    handleBreakthroughFailure(this.entity, this.event)
}
```

### 2. 事件处理逻辑

```kotlin
private fun handleBreakthrough(entity: Entity, event: BreakthroughSuccessEvent) {
    // 根据新境界判断是否需要晋升职位
    val newPosition = calculateNewPosition(event.newRealm)
    if (newPosition != currentPosition) {
        // 触发晋升逻辑
        promoteDisciple(entity, newPosition)
    }
    
    // 如果有师父，通知师父
    notifyMaster(entity, event)
}
```

### 3. 定义 disciples 模块事件（可选）

如果需要对外发送事件：

```kotlin
// DisciplePromotedEvent.kt
data class DisciplePromotedEvent(
    val entity: Entity,
    val oldPosition: SectPositionType,
    val newPosition: SectPositionType,
    val reason: PromotionReason
)
```

## 文件变更

| 文件 | 变更类型 | 说明 |
|------|----------|------|
| `events/DisciplePromotedEvent.kt` | 新增 | 弟子晋升事件 |
| `DisciplesAddon.kt` | 修改 | 添加事件订阅 |
| `DiscipleInfoService.kt` | 修改 | 添加事件处理方法 |

## 验收标准

- [ ] 订阅 BreakthroughSuccessEvent
- [ ] 收到突破事件后执行晋升判断
- [ ] 现有测试通过
- [ ] 文档更新
