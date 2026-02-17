# 组件存储特化优化 - 设计文档

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:writing-plans to create implementation plan.

**目标:** 为数值类型组件提供特化存储，消除装箱开销，提升大量实体数值更新场景的性能。

**架构:** 在 `createAddon` 时，通过 DSL 显式配置组件的存储类型。系统为数值类型组件选择对应的特化存储（如 IntFastList），避免基本类型装箱为对象。

**技术栈:** Kotlin Multiplatform, ECS (lko-ecs), FastList (lko-core)

---

## 背景

### 当前实现

所有组件都使用 `GeneralComponentStore`，底层为 `ObjectFastList<Any>`：

```
组件 → GeneralComponentStore → ObjectFastList<Any> → Array<Any?>
                                                    ↑ 装箱开销
```

### 问题

- **数值组件**（Level, Health, Experience 等）存储时发生装箱
- 每帧更新数万个实体时，CPU 开销显著
- 内存占用较高

### 已有资源

lko-core 已提供特化集合：
- `IntFastList` → `IntArray`
- `FloatFastList` → `FloatArray`
- `LongFastList` → `LongArray`
- `DoubleFastList` → `DoubleArray`

---

## 方案设计

### API 设计

参考 Tag 的 DSL 模式：

```kotlin
// 单个组件配置
world.componentId<Level> { 
    it.store { intStore() }
}

world.componentId<Health> { 
    it.store { floatStore() }
}

world.componentId<Experience> { 
    it.store { longStore() }
}

// 默认行为：未配置的组件仍使用 GeneralComponentStore
```

### DSL 工厂函数

```kotlin
// ComponentStores.kt 新增
fun intStore(): ComponentStore<Any> = IntComponentStore()
fun floatStore(): ComponentStore<Any> = FloatComponentStore()
fun longStore(): ComponentStore<Any> = LongComponentStore()
fun doubleStore(): ComponentStore<Any> = DoubleComponentStore()
fun objectStore(): ComponentStore<Any> = GeneralComponentStore()  // 默认
```

### 架构变更

```
Before:
Relation → ComponentStoreFactory → GeneralComponentStore → ObjectFastList

After:
Relation → ComponentStoreFactory → [根据配置选择]
                                   ├── IntComponentStore → IntFastList
                                   ├── FloatComponentStore → FloatFastList
                                   ├── LongComponentStore → LongFastList
                                   ├── DoubleComponentStore → DoubleFastList
                                   └── GeneralComponentStore (默认)
```

---

## 关键文件

### 新增文件

| 文件 | 说明 |
|------|------|
| `libs/lko-ecs/src/commonMain/kotlin/cn/jzl/ecs/component/IntComponentStore.kt` | Int 类型特化存储 |
| `libs/lko-ecs/src/commonMain/kotlin/cn/jzl/ecs/component/FloatComponentStore.kt` | Float 类型特化存储 |
| `libs/lko-ecs/src/commonMain/kotlin/cn/jzl/ecs/component/LongComponentStore.kt` | Long 类型特化存储 |
| `libs/lko-ecs/src/commonMain/kotlin/cn/jzl/ecs/component/DoubleComponentStore.kt` | Double 类型特化存储 |
| `libs/lko-ecs/src/commonMain/kotlin/cn/jzl/ecs/component/ComponentStores.kt` | 工厂函数 |

### 修改文件

| 文件 | 变更 |
|------|------|
| `libs/lko-ecs/.../ComponentConfigureContext.kt` | 添加 `store()` DSL |
| `libs/lko-ecs/.../ComponentService.kt` | 支持存储类型配置 |

---

## 实现步骤

详见 `docs/plans/2026-02-16-component-store-optimization.md`

---

## 预期收益

- **CPU 性能**: 数值更新场景降低 30-50%（无装箱）
- **内存占用**: 数值组件数组节省 50-70%

---

## 测试计划

1. 单元测试：各特化 Store 的基本操作
2. 集成测试：配置存储类型后查询正常工作
3. 性能测试：对比优化前后的数值更新性能
