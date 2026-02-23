# lko-core - 基础工具库

## 模块定位
高性能 Kotlin 多平台核心工具库，提供专门优化的集合实现和位操作工具。

**层级**: 基础设施层（零依赖）

## 核心职责
- 提供高性能原始类型列表（FastList 系列）
- 位操作工具（Bits、BitSet）
- 基础排序与集合优化

## 目录结构
```
lko-core/src/commonMain/kotlin/cn/jzl/core/
├── Core.kt                # 核心定义
├── bits/                  # 位操作工具
│   ├── Bits.kt            # 位操作扩展函数
│   └── BitSet.kt          # 位集合实现
└── list/                  # 高性能列表
    ├── IntFastList.kt     # Int 专用快速列表
    ├── IntMutableFastList.kt
    ├── LongFastList.kt
    ├── FloatFastList.kt
    ├── DoubleFastList.kt
    ├── ObjectFastList.kt  # 对象快速列表
    └── ...                # 其他原始类型列表
```

## 关键 API

### 高性能列表
| 类型 | 说明 | 使用场景 |
|------|------|----------|
| `IntFastList` | Int 专用列表 | ECS 组件 ID 存储 |
| `LongFastList` | Long 专用列表 | 实体 ID 存储 |
| `ObjectFastList<T>` | 对象列表 | 通用对象存储 |
| `*MutableFastList` | 可变版本 | 需要修改的场景 |

### 位操作工具
```kotlin
// 位提取
value.extract08(offset)      // 提取 8 位
value.extract(offset, count) // 提取指定位数

// 位插入
value.insert08(data, offset) // 插入 8 位
value.insert(data, offset, count)

// 位检查
value.hasBits(mask)          // 检查位是否设置
value.hasBitSet(index)       // 检查单个位

// 位遍历
value.fastForEachOneBits { index -> }
```

## 使用方式

```kotlin
// 使用快速列表
val list = IntMutableFastList()
list.add(1)
list.add(2)
list.insertLastAll(intArrayOf(3, 4, 5))

// 位操作
var flags = 0
flags = flags.setBits(0b111)      // 设置位
flags = flags.unsetBits(0b010)    // 清除位
val hasFlag = flags.hasBits(0b001) // 检查位
```

## 依赖关系

```kotlin
// build.gradle.kts
dependencies {
    // 无任何外部依赖！
}
```

## AI 开发指引

### 开发原则
- **极致性能**: 严禁引入任何外部依赖
- **边界检查**: 集合操作必须包含详尽的边界检查测试
- **无装箱**: 优先使用原始类型特化集合（如 `IntFastList`）
- **零分配**: 核心循环中避免对象分配

### 添加新功能检查清单
- [ ] 是否避免引入外部依赖？
- [ ] 是否有边界检查？
- [ ] 是否有性能测试？
- [ ] 测试覆盖率是否 > 80%？

### 禁止事项
- ❌ 禁止引入任何外部库依赖
- ❌ 禁止在核心循环中分配对象
- ❌ 禁止跳过边界检查

## 测试要求
- 边界条件测试（空列表、越界访问）
- 性能测试（与 stdlib 集合对比）
- 原始类型特化测试
- 位操作正确性测试
