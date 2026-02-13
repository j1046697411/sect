# 设施组件 (Facility Components)

本模块定义宗门设施相关的所有组件。

## 设计原则

1. **功能分离**：生产、存储、修炼等功能独立
2. **等级系统**：设施可升级，等级影响效果
3. **状态管理**：建设、运行、维护、空闲等状态

## 组件列表

### 1. 设施基础

```kotlin
// 设施类型
enum class FacilityType {
    CULTIVATION_ROOM,    // 修炼室
    ALCHEMY_LAB,         // 炼丹房
    FORGE,               // 炼器坊
    TALISMAN_TOWER,      // 符箓阁
    STORAGE,             // 仓库
    LIBRARY,             // 藏经阁
    HERB_GARDEN,         // 灵药园
    TRAINING_GROUND,     // 演武场
    MISSION_HALL,        // 任务大厅
    MARKET,              // 交易市场
    RESIDENCE            // 居住区
}

// 设施基础信息
data class FacilityInfo(
    val name: String,
    val type: FacilityType,
    val level: Int,              // 等级 1-5
    val maxLevel: Int
)

// 设施状态（使用 Tag）
sealed class ConstructingTag   // 建设中
sealed class OperatingTag      // 运行中
sealed class MaintainingTag    // 维护中
sealed class IdleTag           // 空闲中
sealed class DamagedTag        // 损坏中
```

### 2. 设施效果

```kotlin
// 修炼设施效果
data class CultivationFacilityEffect(
    val speedBonus: Float,       // 修炼速度加成
    val breakthroughBonus: Float, // 突破成功率加成
    val capacity: Int            // 容纳人数
)

// 生产设施效果
data class ProductionFacilityEffect(
    val outputRate: Float,       // 产出速率（每小时）
    val qualityBonus: Float,     // 品质加成
    val workerCapacity: Int      // 工人容量
)

// 存储设施效果
data class StorageFacilityEffect(
    val capacity: Map<ResourceType, Int>,  // 各类资源容量
    val preservationRate: Float   // 保存率（防止损耗）
)
```

### 3. 建设信息

```kotlin
data class ConstructionInfo(
    val progress: Float,         // 建设进度 0-100
    val requiredResources: Map<ResourceType, Int>,
    val buildTime: Float,        // 总建设时间（小时）
    val workers: List<Entity>    // 参与建设的工人
)
```

### 4. 维护消耗

```kotlin
data class MaintenanceCost(
    val spiritStonePerDay: Int,  // 每日灵石消耗
    val materialsPerDay: Map<ResourceType, Int>,
    val lastMaintenance: Long    // 上次维护时间
)
```

## 使用场景

### 创建修炼室

```kotlin
world.entity {
    it.addComponent(FacilityInfo("初级修炼室", FacilityType.CULTIVATION_ROOM, 1, 5))
    it.addComponent(CultivationFacilityEffect(speedBonus = 0.1f, breakthroughBonus = 0f, capacity = 5))
    it.addComponent(MaintenanceCost(spiritStonePerDay = 5))
    it.addTag<IdleTag>()
}
```

## 依赖关系

- **依赖**：`core` 模块
- **依赖**：`resources` 模块（使用 ResourceType）
