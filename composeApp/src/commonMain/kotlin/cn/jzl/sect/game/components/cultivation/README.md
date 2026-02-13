# 修炼组件 (Cultivation Components)

本模块定义修炼系统相关的所有组件，包括境界、进度、功法等。

## 设计原则

1. **境界体系完整**：覆盖从炼气到飞升的完整修炼体系
2. **进度独立追踪**：修炼进度与境界分离，便于计算和展示
3. **功法灵活配置**：支持多种功法类型和效果

## 组件列表

### 1. 境界系统 (Cultivation Realm)

使用密封类表示层次化的境界体系，从炼气期到飞升。

**主要境界**：
- `QiRefining` (炼气期 1-9层) - 基础突破率 100%
- `Foundation` (筑基期) - 基础突破率 50%，寿命+50年
- `GoldenCore` (金丹期) - 基础突破率 30%，寿命+150年
- `NascentSoul` (元婴期) - 基础突破率 20%，寿命+400年
- `DeityTransformation` (化神期) - 基础突破率 10%，寿命+800年
- `Mahayana` (大乘期) - 基础突破率 5%，寿命+1500年
- `Ascension` (飞升) - 基础突破率 1%，游戏通关

### 2. 修炼进度

```kotlin
@JvmInline value class CultivationProgress(val percentage: Float)
```

当前境界的修炼进度（0.0 - 100.0），满100%时尝试突破。

### 3. 功法系统

- `CultivationTechnique` - 功法定义（类型、等级、效果）
- `TechniqueProgress` - 功法修炼进度
- `MainTechnique` - 标记当前主修功法

### 4. 突破相关

- `BreakthroughAttempt` - 正在进行的突破尝试
- `BreakthroughRecord` - 突破历史记录

## 依赖关系

- **依赖**：`core` 模块（基础类型）
- **依赖**：`disciple` 模块（使用 `InnateTalent` 计算修炼速度）
