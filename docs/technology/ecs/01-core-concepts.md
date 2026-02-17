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
| `sealed class` | Tag 或 Relation Kind | `ActiveTag`, `OwnedBy` |

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

ECS 框架提供多种关系类型，适用于不同场景：

**1. 普通 Relation (Many-to-One)**

使用 `addRelation<K>(target)` 添加。表示多个源实体可以指向同一个目标实体。这是最常用的关系类型。

```kotlin
sealed class OwnerBy

val sword = world.entity {
    it.addRelation<OwnerBy>(player)  // 剑归属于玩家
}
val shield = world.entity {
    it.addRelation<OwnerBy>(player)  // 盾也归属于玩家
}
```

**2. Single Relation (Single-Target Constraint)**

使用 `addRelation<K, T>()` 或 `addRelation<K>(target)` 添加（取决于是否标记为 Single）。这类关系约束一个实体对于该类型只能拥有**一个**目标。再次添加会替换原有的关系。

```kotlin
sealed class HeldWeapon

val player = world.entity {
    it.addRelation<HeldWeapon>(sword)  // 玩家手持剑
}
// 如果之后执行 it.addRelation<HeldWeapon>(axe)，则会自动替换之前的 sword
```

**3. Shared Component（共享组件）**

使用 `addSharedComponent<C>()` 添加，组件数据在多个实体间共享。

```kotlin
data class TeamId(val value: Int)

val teamMember = world.entity {
    it.addSharedComponent(TeamId(1))
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

// 反向查询：查找关联到当前实体的所有实体（使用 relationUp）
class ReverseQueryContext(world: World, val target: Entity) : EntityQueryContext(world) {
    val owner: Entity by relationUp<OwnerBy>()
    
    override fun FamilyBuilder.configure() {
        relation(relations.kind<OwnerBy>())
        relation(relations.target(target))
    }
}

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

> ⚠️ **重要限制**：任何结构性修改（添加/删除组件、标签、关系）必须在 `editor` 作用域或 `world.entity` 创建作用域内进行。

```kotlin
class MySystem : EntityRelationContext {
    override lateinit var world: World
    
    fun process(entity: Entity) {
        // ✅ 结构修改必须在 editor 作用域内
        world.editor(entity) {
            it.addComponent(Health(100, 100))
        }
        
        // ❌ 禁止在非 editor 作用域直接修改结构
        // entity.addComponent(...) // 编译或运行时报错
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
    .forEach { ctx -> 
        // 这里的 ctx 是 HealthContext，可以访问 health
        println(ctx.health.current) 
    }
```

### 查询上下文四种声明方式

```kotlin
// 1. 基础组件 - 必须存在（使用泛型指定类型）
class PositionContext(world: World) : EntityQueryContext(world) {
    val position: Position by component<Position>()  // 必须有 Position
}

// 2. 可选组件 - 可以不存在（可空类型）
class OptionalContext(world: World) : EntityQueryContext(world) {
    val nickname: Nickname? by component<Nickname?>()  // 可以不存在
}

// 3. 可选组 - 同组至少满足一个
class OptionalGroupContext(world: World) : EntityQueryContext(world) {
    val weapon: Weapon? by component<Weapon?>(optionalGroup = OptionalGroup.One)
    val armor: Armor? by component<Armor?>(optionalGroup = OptionalGroup.One)
    // weapon 或 armor 至少有一个
}

// 4. 可写组件 - 遍历中可修改数据（非结构）
class WritableContext(world: World) : EntityQueryContext(world) {
    var velocity: Velocity by component<Velocity>()
    // 允许修改数据: ctx.velocity = Velocity(1, 1)
    // 但禁止在此修改结构: ctx.entity.addComponent(...)
}
```

### 区别

| 特性 | EntityRelationContext | EntityQueryContext |
|------|----------------------|--------------------|
| 用途 | 修改操作（需配合 editor） | 查询过滤 |
| 访问 | `getComponent<T>()` | `val x: T by component()` |

### 选择

```kotlin
// 修改结构 → 使用 editor
class SpawnSystem : EntityRelationContext {
    fun spawn() { 
        world.entity { it.addComponent(Name("New Entity")) } 
    }
    
    fun update(entity: Entity) {
        world.editor(entity) {
            it.addTag<ActiveTag>()
        }
    }
}

// 查询 → EntityQueryContext
class DamageSystem : EntityRelationContext {
    fun applyDamage() {
        world.query { HealthContext(this) }
            .filter { it.health.current > 0 }
            .forEach { ctx ->
                // 修改组件数据可以使用 WritableContext 或 editor
                world.editor(ctx.entity) {
                    // ... 执行修改
                }
            }
    }
}
```

---

## 最佳实践

1. Component 选型：多属性 `data class`，单属性 `value class`，标记 `sealed class`
2. 注册不可忘：所有 Component/Tag 必须在 `createAddon` 中注册
3. 修改用 copy：Component 不可变，更新必须 `copy()`
4. Tag 语义：状态用 `hasTag()`，数据用 `hasComponent()`
5. Context 选择：修改结构用 `editor`，修改数据用 `WritableContext` 或 `editor`，查询用 `EntityQueryContext`

---

## 下一步

- 查询系统: [02-patterns.md](02-patterns.md)
- 快速开始: [00-quick-start.md](00-quick-start.md)
- 性能优化: [07-performance.md](07-performance.md)
