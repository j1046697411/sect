# 状态标签 (Status Tags)

本模块定义弟子和设施的行为状态标签。

## 设计原则

1. **互斥性**：同一时间通常只有一个行为状态
2. **轻量级**：标签无数据，仅用于标记
3. **可查询性**：便于系统筛选特定状态的实体

## 标签列表

### 1. 行为状态标签

| 标签 | 说明 | 适用对象 |
|------|------|----------|
| `IdleTag` | 空闲状态，未进行任何活动 | 弟子 |
| `CultivatingTag` | 修炼中，提升修为 | 弟子 |
| `WorkingTag` | 工作中，执行任务 | 弟子 |
| `RestingTag` | 休息中，恢复状态 | 弟子 |
| `TravelingTag` | 移动中，前往某地 | 弟子 |
| `FightingTag` | 战斗中 | 弟子 |
| `MeditatingTag` | 冥想/闭关 | 弟子 |
| `SocializingTag` | 社交中 | 弟子 |

### 2. 健康状态标签

| 标签 | 说明 | 适用对象 |
|------|------|----------|
| `InjuredTag` | 受伤中，需要治疗 | 弟子 |
| `PoisonedTag` | 中毒 | 弟子 |
| `SickTag` | 生病 | 弟子 |
| `DisabledTag` | 残疾/重伤 | 弟子 |
| `PregnantTag` | 怀孕（如果有后代系统） | 弟子 |

### 3. 特殊状态标签

| 标签 | 说明 | 适用对象 |
|------|------|----------|
| `InBreakthroughTag` | 正在突破境界 | 弟子 |
| `InSeclusionTag` | 闭关中（不接收外部信息） | 弟子 |
| `UnderObservationTag` | 被观察/监视 | 弟子 |
| `ProtectedTag` | 受保护状态 | 弟子 |
| `BanishedTag` | 被逐出宗门 | 弟子 |
| `OnMissionTag` | 外出执行任务 | 弟子 |

### 4. 设施状态标签

| 标签 | 说明 | 适用对象 |
|------|------|----------|
| `ConstructingTag` | 建设中 | 设施 |
| `OperatingTag` | 运行中 | 设施 |
| `MaintainingTag` | 维护中 | 设施 |
| `FacilityIdleTag` | 空闲中 | 设施 |
| `DamagedTag` | 损坏中 | 设施 |
| `UpgradingTag` | 升级中 | 设施 |

## 标签切换规则

```kotlin
// 状态切换示例
fun changeBehaviorState(entity: Entity, newState: KClass<*>) {
    // 先移除旧的行为标签
    val behaviorTags = listOf(
        IdleTag::class,
        CultivatingTag::class,
        WorkingTag::class,
        RestingTag::class,
        // ... 其他行为标签
    )
    
    entity.editor {
        behaviorTags.forEach { tag ->
            if (entity.hasTag(tag)) it.removeTag(tag)
        }
        it.addTag(newState)
    }
}

// 使用
changeBehaviorState(disciple, CultivatingTag::class)
```

## 查询示例

```kotlin
// 查询所有正在修炼的弟子
val cultivatingDisciples = world.query {
    entityFilter {
        hasTag<CultivatingTag>()
        hasComponent<CultivationRealm>()
    }
}

// 查询所有空闲且未受伤的弟子
val availableDisciples = world.query {
    entityFilter {
        hasTag<IdleTag>()
        !hasTag<InjuredTag>()
        hasComponent<AliveTag>()
    }
}

// 查询所有运行中的设施
val operatingFacilities = world.query {
    entityFilter {
        hasTag<OperatingTag>()
        hasComponent<FacilityInfo>()
    }
}
```

## 与其他模块的关系

- **被使用**：所有 Service 模块都会检查这些标签
- **与组件配合**：标签表示状态，组件存储状态数据
  - 如：`InjuredTag` + `Injury` 组件配合使用

## 注意事项

1. **标签切换性能**：频繁切换标签会改变 Archetype，有一定开销
2. **批量更新**：考虑批量处理而非逐个实体更新
3. **状态一致性**：确保标签与组件数据一致
