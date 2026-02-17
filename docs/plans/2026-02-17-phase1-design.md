# 第一阶段：角色与宗门基础架构设计

**版本**: v1.0  
**日期**: 2026-02-17

## 概述

第一阶段实现宗门修真录的基础架构，包括：
- ECS 组件定义（角色、宗门、设施）
- 简化版境界体系
- 基础AI行为系统
- 初始数据初始化

---

## 1. 角色系统

### 1.1 境界体系（简化版）

| 境界 | 层数 | 寿命加成 | 修炼速度 |
|------|------|----------|----------|
| 炼气期 | 1-9 | +0年 | 1.0x |
| 筑基期 | 1-9 | +50年 | 1.2x |

**突破成功率**: 炼气→筑基 50%

### 1.2 角色组件

```kotlin
data class CultivationComponent(
    val realm: Realm,           // 境界枚举
    val layer: Int,             // 1-9
    val cultivationBase: Long, // 修为基数
    val maxCultivation: Long,   // 当前境界最大修为
)

enum class Realm {
    MORTAL,     // 凡人而未入门
    QI_REFINING, // 炼气期
    FOUNDATION,  // 筑基期
}
```

### 1.3 角色属性组件

```kotlin
data class AttributeComponent(
    // 基础资质
    val physique: Int,    // 根骨 1-100
    val comprehension: Int, // 悟性 1-100
    val fortune: Int,     // 福缘 1-100
    val charm: Int,       // 魅力 1-100
    
    // 四维属性
    val strength: Int,    // 力量
    val agility: Int,     // 敏捷
    val intelligence: Int, // 智力
    val endurance: Int,   // 耐力
    
    // 状态
    val health: Int,      // 生命值
    val maxHealth: Int,   // 最大生命值
    val spirit: Int,      // 灵气值
    val maxSpirit: Int,   // 最大灵气值
    val age: Int,         // 年龄
)
```

### 1.4 性格组件

```kotlin
data class PersonalityComponent(
    val ambition: Int,     // 野心 1-100
    val diligence: Int,   // 勤勉 1-100
    val loyalty: Int,     // 忠诚 1-100
    val greed: Int,       // 贪婪 1-100
    val kindness: Int,    // 和善 1-100
    val coldness: Int,    // 冷漠 1-100
)
```

### 1.5 职务组件

```kotlin
enum class Position {
    DISCIPLE_OUTER,    // 外门弟子
    DISCIPLE_INNER,    // 内门弟子
    DISCIPLE_CORE,     // 亲传弟子
    ELDER,             // 长老
    LEADER,            // 掌门
}

data class PositionComponent(
    val position: Position,
    val department: Department? = null, // 所属部门
)
```

---

## 2. 宗门系统

### 2.1 宗门组件

```kotlin
data class SectComponent(
    val name: String,
    val leaderId: EntityId,    // 掌门ID
    val foundedYear: Int,     // 创立年份
)
```

### 2.2 宗门资源组件

```kotlin
data class SectResourceComponent(
    val spiritStones: Long = 1000,     // 灵石
    val contributionPoints: Long = 0, // 贡献点
)
```

---

## 3. 设施系统

### 3.1 设施定义

```kotlin
enum class FacilityType {
    CULTIVATION_ROOM,  // 修炼室
    ALCHEMY_ROOM,      // 炼丹房
    FORGE_ROOM,        // 炼器坊
    LIBRARY,           // 藏经阁
    WAREHOUSE,         // 仓库
    DORMITORY,         // 弟子房
}

data class FacilityComponent(
    val type: FacilityType,
    val level: Int = 1,          // 1-5级
    val capacity: Int = 0,      // 可容纳人数
    val efficiency: Float = 1f, // 效率加成
)
```

**设施效果（简化定义）**：

| 设施 | 等级1 | 等级2 | 等级3 | 等级4 | 等级5 |
|------|-------|-------|-------|-------|-------|
| 修炼室 | +10% | +20% | +35% | +50% | +70% |
| 炼丹房 | +0% | +20% | +50% | +100% | +200% |

---

## 4. AI行为系统

### 4.1 行为类型

```kotlin
enum class BehaviorType {
    CULTIVATE,   // 修炼
    WORK,        // 工作
    REST,        // 休息
    SOCIAL,      // 社交
}
```

### 4.2 行为状态组件

```kotlin
data class BehaviorStateComponent(
    val currentBehavior: BehaviorType = BehaviorType.CULTIVATE,
    val behaviorStartTime: Long = 0,
    val lastBehaviorTime: Long = 0,
)
```

### 4.3 简单AI决策逻辑

```
优先级：修炼 > 工作 > 休息

决策规则：
1. 灵气值 >= 30% → 修炼
2. 否则 → 工作
3. 健康值 < 30% → 休息
```

---

## 5. 初始数据

### 5.1 初始弟子配置

| 职务 | 数量 | 境界 | 年龄 |
|------|------|------|------|
| 掌门 | 1 | 筑基期5层 | 80 |
| 长老 | 3 | 筑基期3层 | 60-70 |
| 外门弟子 | 5 | 炼气期3-5层 | 18-25 |

### 5.2 初始资源

- 灵石：1000
- 贡献点：0

---

## 6. 实现范围

### 6.1 第一阶段交付物

1. **组件定义** (`business-core`)
   - CultivationComponent 境界组件
   - AttributeComponent 属性组件
   - PersonalityComponent 性格组件
   - PositionComponent 职务组件
   - SectComponent 宗门组件
   - FacilityComponent 设施组件
   - BehaviorStateComponent 行为状态组件

2. **初始化系统** (`business-engine`)
   - SectWorld 初始化逻辑
   - 创建初始弟子
   - 创建初始设施

3. **AI行为系统** (`business-ai`)
   - SimpleBehaviorSystem 简单行为系统
   - 基于状态的决策逻辑

4. **测试**
   - 组件单元测试
   - 初始化集成测试
   - AI行为测试

### 6.2 后续扩展

- 完整境界体系（金丹、元婴、化神、大乘）
- 完整资源系统（粮食、木材、矿石、布料）
- 设施建造和维护逻辑
- 复杂AI决策（基于性格）
- 战斗系统
- 任务系统
