# ECS 常见陷阱与解决方案

**提取时间：** 2026-02-14
**适用场景：** 使用 ECS 框架开发时避免常见错误

## 陷阱1：组件不可变性误解

### 问题
```kotlin
// ❌ 错误：直接修改组件属性
data class Health(val current: Int, val max: Int)

entity.getComponent<Health>()!!.current = 50  // 编译错误！
```

### 解决方案
```kotlin
// ✅ 正确：使用 copy() 创建新实例
val health = entity.getComponent<Health>()!!
entity.editor {
    it.addComponent(health.copy(current = 50))
}
```

### 原理
ECS 组件是**不可变数据类**（immutable），必须遵循：
1. 组件定义时使用 `val` 而非 `var`
2. 更新时使用 `copy()` 创建新实例
3. 通过 `editor { }` 块提交更改

---

## 陷阱2：组件 vs Tag 混淆

### 问题
```kotlin
sealed class ActiveTag  // 标签，无数据

data class Health(val current: Int, val max: Int)  // 组件，有数据

// ❌ 错误：混用 API
world.entity {
    it.addComponent(ActiveTag)      // 错误：Tag 不是 Component
    it.addTag(Health(100, 100))     // 错误：Component 不是 Tag
}
```

### 解决方案

| 类型 | 定义 | 添加 | 检查 | 移除 |
|------|------|------|------|------|
| **Component** | `data class Health(...)` | `addComponent(Health(...))` | `hasComponent<Health>()` | `removeComponent<Health>()` |
| **Tag** | `sealed class ActiveTag` | `addTag<ActiveTag>()` | `hasTag<ActiveTag>()` | `removeTag<ActiveTag>()` |

```kotlin
// ✅ 正确用法
world.entity {
    it.addComponent(Health(100, 100))   // Component
    it.addTag<ActiveTag>()               // Tag
}
```

---

## 陷阱3：查询时修改实体

### 问题
```kotlin
// ❌ 错误：遍历时修改导致 ConcurrentModificationException
world.query { PositionContext(this) }.forEach { ctx ->
    ctx.entity.editor { 
        it.addComponent(NewComponent())  // 危险！
    }
}
```

### 解决方案
```kotlin
// ✅ 正确：先收集再修改
val entities = world.query { PositionContext(this) }
    .map { it.entity }
    .toList()

entities.forEach { entity ->
    entity.editor { it.addComponent(NewComponent()) }
}

// ✅ 或者：使用 toList() 强制收集
world.query { PositionContext(this) }
    .toList()  // 立即收集到List
    .forEach { ctx ->
        ctx.entity.editor { ... }
    }
```

---

## 陷阱4：忘记注册组件ID

### 问题
```kotlin
// ❌ 错误
val world = world {}

world.entity {
    it.addComponent(Health(100, 100))  // 运行时错误！
}
```

### 错误信息
```
IllegalStateException: Component Health is not registered
```

### 解决方案
```kotlin
// ✅ 正确：在 world 创建时注册
val world = world {
    components {
        world.componentId<Health>()
        world.componentId<Position>()
        world.componentId<ActiveTag> { it.tag() }  // Tag 需要标记
    }
}
```

---

## 陷阱5：EntityCreateContext lambda 参数名冲突

### 问题
```kotlin
// ❌ 错误：it 指代不明确
repeat(10) {
    world.entity {
        it.addComponent(Position(it, it * 2))  // it 冲突！
    }
}
```

### 错误分析
- 外层 `it` 是 `Int`（repeat 的索引）
- 内层 `it` 是 `EntityCreateContext`
- 混淆导致编译错误或逻辑错误

### 解决方案
```kotlin
// ✅ 正确：明确命名参数
repeat(10) { index ->
    world.entity { ctx ->
        ctx.addComponent(Position(index, index * 2))
    }
}
```

---

## 陷阱6：单属性组件使用 data class

### 问题
```kotlin
// ❌ 不推荐：单属性使用 data class
data class Level(val value: Int)
data class Exp(val value: Long)
```

### 影响
- 性能开销（装箱/拆箱）
- 内存占用更大

### 解决方案
```kotlin
// ✅ 正确：单属性使用 @JvmInline value class
@JvmInline value class Level(val value: Int)
@JvmInline value class Exp(val value: Long)
```

### 性能对比
| 类型 | 内存占用 | 性能 |
|------|---------|------|
| `data class` | 对象引用 + 字段 | 需要堆分配 |
| `@JvmInline value class` | 仅字段值 | 内联，无额外开销 |

---

## 陷阱7：Service 中保存状态

### 问题
```kotlin
// ❌ 错误：Service 保存可变状态
class BadService(override val world: World) : EntityRelationContext {
    private var counter = 0  // 不要这样做！
    
    fun increment() {
        counter++  // 违反 ECS 原则
    }
}
```

### 原因
Service 应该是**无状态**的，状态应该存储在 Component 中

### 解决方案
```kotlin
// ✅ 正确：状态存储在 Component 中
data class Counter(val value: Int)

class CounterService(override val world: World) : EntityRelationContext {
    fun increment(entity: Entity) {
        val counter = entity.getComponent<Counter>()!!
        entity.editor {
            it.addComponent(counter.copy(value = counter.value + 1))
        }
    }
}
```

---

## 陷阱8：关系类型与数据类型混淆

### 问题
```kotlin
sealed class Mentorship  // 关系类型

// ❌ 错误：混用类型
entity.editor {
    it.addRelation<Mentorship>(mentor)                    // 无数据关系
    it.addRelation<MentorshipData>(mentor, MentorshipData(...))  // 有数据关系
}
```

### 正确用法
```kotlin
// 无数据关系
entity.editor {
    it.addRelation<Mentorship>(target = mentor)
}

// 有数据关系（泛型和data类型一致）
entity.editor {
    it.addRelation<MentorshipData>(target = mentor, data = MentorshipData(2024))
}
```

---

## 陷阱9：QueryStream 操作误解

### 问题
```kotlin
// ❌ 错误：QueryStream 不是 Collection，不能直接使用 size
val query = world.query { PositionContext(this) }
println(query.size)  // 错误！
```

### 解决方案
```kotlin
// ✅ 正确：先转换为 List
val results = world.query { PositionContext(this) }.toList()
println(results.size)

// ✅ 或者：使用 count()
val count = world.query { PositionContext(this) }.count()
```

---

## 陷阱10：Family 与 Archetype 混淆

### 问题
```kotlin
// ❌ 错误：直接访问 archetypes 属性
val family = world.familyService.family { component<Position>() }
println(family.archetypes.size())  // 错误！
```

### 解决方案
```kotlin
// ✅ 正确：使用 archetypes.size 属性
val family = world.familyService.family { component<Position>() }
println(family.archetypes.size)  // ✅ 属性访问

// ✅ 获取实体总数
println(family.size)  // 所有 archetype 的 size 总和
```

---

## 快速检查清单

编写 ECS 代码时，检查以下要点：

- [ ] 组件定义为 `data class` 或 `@JvmInline value class`
- [ ] 标签定义为 `sealed class`
- [ ] 所有组件已在 world 创建时注册 `componentId<>`
- [ ] 使用 `addComponent()` 添加组件，`addTag<>()` 添加标签
- [ ] 更新组件时使用 `copy()` + `editor { }`
- [ ] 遍历查询时不直接修改实体（先 `toList()`）
- [ ] Service 不保存可变状态
- [ ] lambda 参数名明确，避免 `it` 冲突

## 使用时机

- 编写新功能前复习此清单
- 调试奇怪行为时检查是否踩坑
- 代码审查时作为检查项
- 新成员入职学习材料
