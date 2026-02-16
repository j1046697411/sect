# ECS 核心概念详解

> 深入理解 Entity、Component、Tag、Relation、World 及两大 Context 的区别。

---

## 1. Entity（实体）

Entity 是游戏对象的唯一标识，本质是整数 ID，不存储任何数据。

```kotlin
// ✅ 创建
val player = world.entity { }

// ✅ 带组件
val enemy = world.entity {
    it.addComponent(Health(50, 100))
    it.addComponent(Position(10, 20))
}

// ✅ 子实体
val weapon = player.childOf { it.addComponent(Name("Sword")) }

// ✅ 实例化
val goblin = prefab.instanceOf { it.addComponent(Level(3)) }

// ✅ 销毁
player.destroy()

// ❌ 销毁后使用（危险）
player.destroy()
player.getComponent<Health>()  // null 或异常
```

---

## 2. Component（组件）

### 三种类型

| 类型 | 适用场景 | 示例 |
|------|----------|------|
| `data class` | 多属性 | `Health(current, max)` |
| `@JvmInline value class` | 单属性高性能 | `Level(val value: Int)` |
| `sealed class` | Tag | `ActiveTag` |

### 使用示例

```kotlin
// data class
data class Health(val current: Int, val max: Int)

// value class
@JvmInline value class Level(val value: Int)

// sealed class (Tag)
sealed class ActiveTag

// 注册
world.componentId<Health>()
world.componentId<Level>()
world.componentId<ActiveTag> { it.tag() }

// ❌ 忘记注册 → 运行时崩溃
// ❌ 直接修改 → 用 copy()
entity.editor { it.addComponent(health.copy(current = 50)) }
```

---

## 3. Tag（标记系统）

Tag 用于标记实体状态，不包含数据，存储在位图中性能极高。

```kotlin
sealed class ActiveTag
sealed class DeadTag

// 操作
entity.addTag<ActiveTag>()
entity.hasTag<ActiveTag>()
entity.editor { it.removeTag<ActiveTag>() }

// ❌ 用 hasComponent 检查 Tag（语义错误）
```

---

## 4. Relation（关系系统）

表示实体间的关系，内置 OwnerBy、Parent/Child、InstanceOf。

```kotlin
sealed class OwnerBy

// 添加关系
val sword = world.entity {
    it.addRelation<OwnerBy>(player)
}

// 获取关系
val owner = entity.getRelation<OwnerBy, Name>()

// 预制体实例化
val goblin1 = goblinPrefab.instanceOf { it.addComponent(Name("G1")) }
```

### 4.1 Relation 与 Component/Tag 对比

Relation（关系）用于表示实体之间的关联，是 ECS 架构中连接实体的桥梁。与 Component（组件）和 Tag（标签）相比，Relation 有其独特的适用场景：

| 特性 | Component（组件） | Tag（标签） | Relation（关系） |
|------|------------------|-------------|------------------|
| **数据存储** | 存放具体数据（data class） | 无数据，仅标记 | 关联目标实体 |
| **数量限制** | 同类型仅一个 | 同类型仅一个 | 同类型可多个 |
| **查询方式** | `query<Component>()` | `query<Tag>()` | `query<Relation>()` |
| **典型用途** | 属性、数值、状态 | 类型标记、状态标记 | 拥有者、父子、实例 |
| **跨实体** | 否（绑定单个实体） | 否（绑定单个实体） | 是（连接两个实体） |
| **删除影响** | 仅影响自身 | 仅影响自身 | 影响关系两端 |

### 4.2 四种 Relation 类型

ECS 框架提供四种关系类型，适用于不同场景：

**1. 普通 Relation（多对一）**

使用 `addRelation<K>(target)` 添加，适用于一个实体被多个其他实体关联的场景：

```kotlin
sealed class OwnerBy

val sword = world.entity {
    it.addRelation<OwnerBy>(player)  // 剑归属于玩家
}
val shield = world.entity {
    it.addRelation<OwnerBy>(player)  // 盾也归属于玩家
}
```

**2. Single Relation（一对一）**

使用 `addRelation<K>()` 添加，适用于一对一关系，无目标实体时为 `null`：

```kotlin
sealed class HeldWeapon

val player = world.entity {
    it.addRelation<HeldWeapon>(sword)  // 玩家手持剑
}
val owner = player.getRelation<HeldWeapon, Entity>()  // 获取目标实体
```

**3. Shared Component（共享组件）**

使用 `addSharedComponent<C>()` 添加，组件在关系双方共享，修改一方会影响另一方：

```kotlin
data class TeamId(val value: Int)

val team = world.entity {
    it.addSharedComponent(TeamId(1))
}

val member1 = world.entity {
    it.addRelation<componentOf>(team)
}
val member2 = world.entity {
    it.addRelation<componentOf>(team)
}
```

**4. 内置 Relation 类型**

框架提供四种内置 Relation，简化常见关系模式：

| 类型 | 说明 | 典型用法 |
|------|------|----------|
| `componentOf` | 组件共享关系 | 多个实体共享同一组件数据 |
| `sharedOf` | 资源共享关系 | 实体间共享资源（如装备池） |
| `childOf` | 父子层级关系 | 实体树结构（装备栏位、任务链） |
| `instanceOf` | 实例化关系 | 预制体实例、怪物生成 |

```kotlin
// 预制体实例化（继承关系）
val goblinPrefab: Entity
val goblin1 = goblinPrefab.instanceOf { entity ->
    entity.addComponent(Name("哥布林A"))
    entity.addComponent(Hp(100))
}

// 层级关系示例
val inventory = world.entity { it.addComponent(Inventory()) }
val slot1 = world.entity { it.addRelation<childOf>(inventory) }
val item = world.entity { it.addRelation<childOf>(slot1) }
```

### 4.3 Relation 查询与遍历

Relation 的查询支持多种遍历方式，适用于不同业务需求：

```kotlin
// 查询拥有特定关系的所有实体
world.query<OwnerBy>().forEach { entity ->
    val owner = entity.getRelation<OwnerBy, Name>()
}

// 反向查询：查找关联到当前实体的所有实体
world.query<OwnerBy>().filter { it.getRelation<OwnerBy, Entity>() == player }

// 条件查询：查找特定目标的关系
world.query<OwnerBy>().filter { relation ->
    relation.getRelation<OwnerBy, Entity>() == player
}
```

> 📚 完整的关系系统 API 与高级用法，请参阅 [Relation 系统详解](05-relation-system.md)。

---

## 5. World（世界容器）

ECS 核心容器，管理实体、组件和系统。

```kotlin
// 创建
val world = world { install(gameAddon) }

// 核心 API
world.entity { }                                    // 创建实体
world.query { HealthContext(this) }                // Query DSL
```

| 服务 | 职责 |
|------|------|
| EntityService | 实体生命周期 |
| ComponentService | 组件注册存储 |
| RelationService | 关系管理 |
| FamilyService | 实体过滤 |

---

## 6. QueryContext vs EntityRelationContext

### EntityRelationContext

基础接口，提供实体操作能力。

```kotlin
class MySystem : EntityRelationContext {
    override lateinit var world: World
    
    fun process() {
        world.entity { it.addComponent(Health(100, 100)) }
        entity.hasComponent<Health>()
    }
}
```

### EntityQueryContext

继承自 `EntityRelationContext`，提供组件属性委托，用于查询。

```kotlin
class HealthContext(world: World) : EntityQueryContext(world) {
    val health: Health by component()
}

// 使用
world.query { HealthContext(this) }
    .filter { it.health.current > 0 }
    .forEach { ctx -> println(ctx.health.current) }
```

### 查询上下文四种声明方式

```kotlin
// 1. 基础组件 - 必须存在
class PositionContext(world: World) : EntityQueryContext(world) {
    val position: Position by component()  // 必须有 Position
}

// 2. 可选组件 - 可以不存在
class OptionalContext(world: World) : EntityQueryContext(world) {
    val nickname: Nickname? by component()  // 可以不存在
}

// 3. 可选组 - 同组至少满足一个
class OptionalGroupContext(world: World) : EntityQueryContext(world) {
    val weapon: Weapon? by component(OptionalGroup.One)
    val armor: Armor? by component(OptionalGroup.One)
    // weapon 或 armor 至少有一个
}

// 4. 可写组件 - 遍历中可修改
class WritableContext(world: World) : EntityQueryContext(world) {
    var velocity: Velocity by component()
    // 遍历中可修改 ctx.velocity = ...
}
```

### 区别

| 特性 | EntityRelationContext | EntityQueryContext |
|------|----------------------|--------------------|
| 用途 | 修改操作 | 查询过滤 |
| 访问 | `getComponent<T>()` | `val x: T by component()` |

### 选择

```kotlin
// 修改 → EntityRelationContext
class SpawnSystem : EntityRelationContext {
    fun spawn() { world.entity { } }
}

// 查询 → EntityQueryContext
class DamageSystem : EntityRelationContext {
    fun applyDamage() {
        world.query { HealthContext(this) }
            .filter { it.health.current > 0 }
            .forEach { ctx -> /* 处理 */ }
    }
}
```

---

## 最佳实践

1. Component 选型：多属性 `data class`，单属性 `value class`，标记 `sealed class`
2. 注册不可忘：所有 Component/Tag 必须在 `createAddon` 中注册
3. 修改用 copy：Component 不可变，更新必须 `copy()`
4. Tag 语义：状态用 `hasTag()`，数据用 `hasComponent()`
5. Context 选择：修改用 `EntityRelationContext`，查询用 `EntityQueryContext`

---

## 下一步

- 查询系统: [02-patterns.md](02-patterns.md)
- 快速开始: [00-quick-start.md](00-quick-start.md)
- 性能优化: [07-performance.md](07-performance.md)
