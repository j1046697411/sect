# 基础核心组件 (Core Components)

本模块包含游戏中最基础的、跨领域通用的组件定义。

## 设计原则

1. **原子化设计**：每个组件只包含一个概念的数据
2. **不可变性**：使用 `val` 声明所有字段，通过 `copy()` 更新
3. **类型安全**：单属性使用 `@JvmInline value class`，多属性使用 `data class`

## 组件列表

### 身份标识类

| 组件 | 类型 | 说明 |
|------|------|------|
| `EntityName` | `@JvmInline value class` | 实体名称，用于显示 |
| `EntityId` | `@JvmInline value class` | 唯一标识符，系统自动生成 |
| `Identity` | `data class` | 身份信息组合（姓名+性别+出生日期） |

### 时间相关类

| 组件 | 类型 | 说明 |
|------|------|------|
| `Age` | `@JvmInline value class` | 年龄，精确到年 |
| `BirthDate` | `data class` | 出生日期（年/月/日） |
| `Lifespan` | `@JvmInline value class` | 寿命上限（年） |

### 资源持有类

| 组件 | 类型 | 说明 |
|------|------|------|
| `SpiritStone` | `@JvmInline value class` | 灵石数量，主要货币 |
| `ContributionPoints` | `@JvmInline value class` | 贡献点，门派内部货币 |
| `Reputation` | `@JvmInline value class` | 声望值，影响社交和事件 |

### 位置信息类

| 组件 | 类型 | 说明 |
|------|------|------|
| `Location` | `data class` | 位置坐标（区域ID/设施ID/x/y） |
| `CurrentRegion` | `@JvmInline value class` | 当前所在区域ID |

## 使用示例

```kotlin
// 创建弟子时添加基础组件
world.entity {
    it.addComponent(EntityName("张三"))
    it.addComponent(Age(18))
    it.addComponent(SpiritStone(100))
    it.addComponent(Location(regionId = 1, facilityId = null, x = 0f, y = 0f))
}
```

## 依赖关系

- **无外部依赖**：本模块是底层基础模块
- **被依赖方**：所有其他组件模块都可能使用这些基础组件

## 性能考虑

- `value class` 在运行时被内联，无装箱开销
- 所有组件都是不可变的，支持并发安全
- 单属性组件占用的内存极小（与原始类型相同）
