# ECS 反模式指南

> 常见错误与正确做法，帮助开发者避免 ECS 开发中的陷阱。

---

## 1. 在 Query 迭代中直接修改实体

### ❌ 错误示例
```kotlin
fun healAll() {
    world.query { HealthContext(this) }.forEach { ctx ->
        val health = ctx.health
        ctx.entity.editor {
            it.addComponent(health.copy(current = health.current + 50))
        }
    }
}
```
Query 迭代时锁定实体结构,直接修改会触发异常。

### ✅ 正确做法
```kotlin
fun healAll() {
    val entities = world.query { HealthContext(this) }.map { it.entity }.toList()
    entities.forEach { entity ->
        val health = entity.getComponent<Health>() ?: return@forEach
        entity.editor { it.addComponent(health.copy(current = health.current + 50)) }
    }
}
```

---

## 2. 忘记注册 ComponentId

### ❌ 错误示例
```kotlin
data class Experience(val value: Long)
val entity = world.entity { it.addComponent(Experience(1000)) }  // 运行时异常
```
未注册 ComponentId,系统无法识别该组件。

### ✅ 正确做法
```kotlin
private val gameAddon = createAddon<Unit>("game") {
    components { world.componentId<Experience>() }
}
```

---

## 3. 混合职责的大组件

### ❌ 错误示例
```kotlin
data class PlayerStats(val name: String, val level: Int, val health: Int, val mana: Int, val positionX: Int, val positionY: Int, val experience: Long)
```
违反原子化原则,频繁创建大对象,GC 压力大。

### ✅ 正确做法
```kotlin
data class PlayerName(val value: String)
data class PlayerLevel(val value: Int)
data class Health(val current: Int, val max: Int)
data class Position(val x: Int, val y: Int)
```

---

## 4. 单属性使用 data class

### ❌ 错误示例
```kotlin
data class Level(val value: Int)
```
产生装箱开销,高频迭代下性能损耗明显。

### ✅ 正确做法
```kotlin
@JvmInline value class Level(val value: Int)
world.componentId<Level>()
entity.addComponent(Level(10))
```

---

## 5. 直接修改组件属性

### ❌ 错误示例
```kotlin
val health = entity.getComponent<Health>()!!
health.current = 150  // 违反不可变原则
```
组件设计为不可变数据结构,直接修改会破坏数据一致性。

### ✅ 正确做法
```kotlin
val health = entity.getComponent<Health>()!!
entity.editor { it.addComponent(health.copy(current = minOf(health.current + 50, health.max))) }
```

---

## 6. Service 中保存状态

### ❌ 错误示例
```kotlin
class PlayerService : EntityRelationContext {
    override lateinit var world: World
    private var lastPlayerId: Int = 0
    private val playerCache = mutableMapOf<Int, PlayerData>()
}
```
ECS 原则是数据驱动,Service 应为无状态工具类。

### ✅ 正确做法
```kotlin
class PlayerService : EntityRelationContext {
    override lateinit var world: World
    fun getPlayerById(id: Int): Entity? = 
        world.query { PlayerIdContext(this) }.filter { it.playerId.value == id }.firstOrNull()?.entity
}
```

---

## 7. 混淆组件与标签

### ❌ 错误示例
```kotlin
data class EnemyTag(val enemyType: Int)  // 标签含数据
```
标签用于标记实体类型或状态,应为无数据的标记。

### ✅ 正确做法
```kotlin
sealed class EnemyTag  // 标签
data class EnemyType(val type: Int)  // 数据放组件
```

---

## 快速参考

| 反模式 | 错误后果 | 修复方案 |
|--------|----------|----------|
| Query 中修改 | 异常/数据错乱 | 先收集到列表 |
| 未注册 ComponentId | 运行时崩溃 | createAddon 中注册 |
| 大组件 | GC 压力 | 拆分为原子组件 |
| 单属性 data class | 装箱开销 | @JvmInline value class |
| 直接修改属性 | 数据不一致 | 使用 copy() |
| Service 保存状态 | 状态不同步 | 状态存入组件 |
| 标签含数据 | 架构混乱 | sealed class |

---

## 下一步

- 正确模式: [02-patterns.md](02-patterns.md)
- 核心概念: [01-core-concepts.md](01-core-concepts.md)
- 测试指南: [08-testing.md](08-testing.md)
