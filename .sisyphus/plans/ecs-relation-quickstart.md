# ECS 关系系统快速开始指南

## 核心概念

在 `lko-ecs` 框架中，**一切都是关系（Relation）**。

### 关系的本质

```kotlin
// 关系 = (类型, 目标实体)
Relation(kind: ComponentId, target: Entity)
```

### 组件和标签是特殊的关系

| 概念 | 在关系中的表示 | 说明 |
|------|---------------|------|
| **普通组件** | `Relation(kind, components.componentOf)` | 组件数据绑定到实体本身 |
| **共享组件** | `Relation(kind, components.sharedOf)` | 多个实体共享同一个组件实例 |
| **标签** | `Relation(kind, components.componentOf)` | 无数据的关系（tag） |
| **实体间关系** | `Relation(kind, otherEntity)` | 两个实体之间的关联 |

---

## 快速开始

### 1. 定义关系类型

```kotlin
// ✅ 优先：单属性使用 value class（性能更好）
@JvmInline
value class Likes(val level: Int)

@JvmInline
value class BelongsTo(val since: Long)

// ✅ 多属性使用 data class
data class Position(val x: Float, val y: Float)
data class CombatStats(val hp: Int, val mp: Int, val attack: Int)

// ✅ 定义无数据的关系（标签）
sealed class ParentOf
sealed class FriendsWith
```

### 2. 注册关系到 World

```kotlin
val gameAddon = createAddon<Unit>("game") {
    components {
        // value class 关系
        world.componentId<Likes>()
        world.componentId<BelongsTo>()
        // data class 关系
        world.componentId<Position>()
        world.componentId<Health>()
        // 标签
        world.componentId<ParentOf> { it.tag() }
        world.componentId<FriendsWith> { it.tag() }
    }
}

val world = world {
    install(gameAddon)
}
```

### 3. 创建实体并添加关系

```kotlin
val player = world.entity { }
val sword = world.entity {
    it.addComponent(ItemName("龙鳞剑"))
    it.addComponent(ItemDamage(100))
}

// 添加关系
player.editor {
    it.addRelation<Likes>(sword, Likes(level = 10))
}

sword.editor {
    it.parent(player)
}
```

### 4. 查询（使用 query 方法）

```kotlin
import cn.jzl.ecs.query.EntityQueryContext
import cn.jzl.ecs.family.FamilyBuilder
import cn.jzl.ecs.family.component
import cn.jzl.ecs.family.relation

class PlayerItemsContext(world: World) : EntityQueryContext(world) {
    val itemName: ItemName by component()
    val likes: Likes by relation(player)
    
    override fun FamilyBuilder.configure() {
        relation<BelongsTo>(player)
    }
}

world.query { PlayerItemsContext(this) }.forEach { ctx, entity ->
    println("物品: ${ctx.itemName.name}, 喜欢等级: ${ctx.likes.level}")
}
```

---

## 查询上下文详解

### 基础语法

```kotlin
class MyQueryContext(world: World) : EntityQueryContext(world) {
    
    // 1. 基础组件 - 必须存在
    val position: Position by component()
    val health: Health by component()
    
    // 2. 可选组件 - 可以不存在（可空类型）
    val nickname: Nickname? by component()
    
    // 3. 可选组 - 同组内至少满足一个
    val weapon1: Weapon? by component(OptionalGroup.One)
    val weapon2: Armor? by component(OptionalGroup.One)
    
    // 4. 可写组件 - 遍历过程中可修改
    var velocity: Velocity by component()
    
}
```

### 完整示例

```kotlin
class GameQueryContext(world: World) : EntityQueryContext(world) {
    
    // ========== 基础条件（必须存在）==========
    val position: Position by component()
    val health: Health by component()
    
    // ========== 可选组件（可以不存在）==========
    val nickname: Nickname? by component()
    val buff: Buff? by component()
    
    // ========== 可选组（至少满足一个）==========
    val weapon: Weapon? by component(OptionalGroup.One)
    val armor: Armor? by component(OptionalGroup.One)
    
    // ========== 可写组件（遍历中可修改）==========
    var velocity: Velocity by component()
    var mana: Mana by component()
    
}
```

### OptionalGroup 详解

| 类型 | 语法 | 行为 |
|------|------|------|
| **必须存在** | `val x: T by component<T>()` | 实体必须有 T 组件 |
| **可选（可空）** | `val x: T? by component<T>()` | T 组件可以不存在，返回 null |
| **可选组** | `val x: T? by component<T>(OptionalGroup.One)` | 同组内至少满足一个 |
| **可写** | `var x: T by component<T>()` | 必须存在，且可修改 |

---

## 高级用法

### 在 System 中使用查询

```kotlin
class InventorySystem(override val world: World) : EntityRelationContext {
    
    class PlayerInventoryQuery(world: World, val player: Entity) : EntityQueryContext(world) {
        val itemName: ItemName by component()
        val belongsTo: BelongsTo by relation(player)
        
        override fun FamilyBuilder.configure() {
            relation<BelongsTo>(player)
        }
    }
    
    fun getPlayerItems(player: Entity): List<Entity> {
        return world.query { PlayerInventoryQuery(this, player) }
            .map { it.second }
            .toList()
    }
}
```

### 监听关系变化

```kotlin
world.observer<BelongsTo>(components.onInserted) { entity, relation ->
    val owner = relation.target
    println("实体 $entity 现在属于 $owner")
}
```

---

## 最佳实践

### 1. 单属性优先使用 value class

```kotlin
// ✅ 正确：单属性使用 value class（避免装箱，性能更好）
@JvmInline
value class Level(val value: Int)

@JvmInline
value class Age(val years: Int)

// ✅ 正确：多属性使用 data class
data class Position(val x: Float, val y: Float)
data class Health(val current: Int, val max: Int)

// ❌ 错误：单属性使用 data class
data class Level(val value: Int)
```

### 2. relation 必须指定 target

```kotlin
// ❌ 错误
val likes: Likes by relation<Likes>()

// ✅ 正确
val likes: Likes by relation(targetEntity)
```

### 3. 可选组的使用场景

```kotlin
// 玩家可以选择装备武器或防具，但至少要有一个
class PlayerEquipmentContext(world: World) : EntityQueryContext(world) {
    val weapon: Weapon? by component(OptionalGroup.One)
    val armor: Armor? by component(OptionalGroup.One)
    
    override fun FamilyBuilder.configure() {
        component<Player>()
    }
}
```

### 4. 可写组件用于批量修改

```kotlin
class MovementSystem(override val world: World) : EntityRelationContext {
    
    class MovableContext(world: World) : EntityQueryContext(world) {
        var position: Position by component()
        var velocity: Velocity by component()
        
        override fun FamilyBuilder.configure() {
            component<Position>()
            component<Velocity>()
        }
    }
    
    fun updateMovement(dt: Float) {
        world.query { MovableContext(this) }.forEach { ctx, entity ->
            ctx.position = Position(
                ctx.position.x + ctx.velocity.dx * dt,
                ctx.position.y + ctx.velocity.dy * dt
            )
        }
    }
}
```

---

## 数据定义决策树

```
定义数据时：
│
├── 只有 1 个属性？
│   └── ✅ 使用 value class（性能更好）
│       @JvmInline value class Level(val value: Int)
│
└── 有 2+ 个属性？
    └── ✅ 使用 data class
        data class Position(val x: Float, val y: Float)
        data class Health(val current: Int, val max: Int)
```

### 具体示例

| 场景 | 推荐类型 | 代码示例 |
|------|---------|----------|
| 等级 | value class | `@JvmInline value class Level(val value: Int)` |
| 经验 | value class | `@JvmInline value class Experience(val xp: Long)` |
| 坐标 | data class | `data class Position(val x: Float, val y: Float)` |
| 生命值 | data class | `data class Health(val current: Int, val max: Int)` |
| 战斗力 | value class | `@JvmInline value class CombatPower(val power: Int)` |
| 装备属性 | data class | `data class Equipment(val attack: Int, val defense: Int)` |

---

## 关系 vs 组件 vs 标签

```
需要存储数据？
├── 是 → 几个属性？
│   ├── 1 个 → value class
│   │       ├── 关联特定实体？ → 关系
│   │       └── 属于实体？ → 组件
│   │
│   └── 2+ 个 → data class
│           ├── 关联特定实体？ → 关系
│           └── 属于实体？ → 组件
│
└── 否 → sealed class
        ├── 关联特定实体？ → 关系标签
        └── 标记特性？ → 标签
```

---

## 注意事项

1. **单属性优先 value class**：性能更好，避免装箱
2. **必须使用 query 方法**：`world.query { }`
3. **relation 需要 target**：`relation<K>(target)`
4. **可选组件使用可空类型**
5. **可选组使用 OptionalGroup.One**
6. **可写组件使用 var**
7. **Addon 中注册**：类型必须注册

---

## 下一步

1. 阅读 `QuerySystemTest.kt` 学习完整示例
2. 查看 `ComponentTest.kt` 了解组件使用
3. 运行 `./gradlew :libs:lko-ecs:test`
