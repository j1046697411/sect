# ECS Relation 系统详解

> 深入理解实体间关系机制：普通 Relation、Single Relation、Shared Component 以及内置的父子/实例系统。

---

## 1. 基础概念

### 1.1 Relation 是什么

Relation（关系）是 ECS 框架中用于表达实体之间关联的特殊机制。它将两个实体通过一种「关系类型」连接起来，就像现实世界中人与人之间的「朋友关系」或「上下级关系」一样。

在代码层面，`Relation` 是一个包含 `kind`（关系类型）和 `target`（目标实体）的数据结构。它既可以表示纯粹的引用关系，也可以携带数据。

简单来说，Relation = 关系类型 + 目标实体 [+ 关系数据]。


### 1.2 与 Component/Tag 的本质区别

| 特性 | Component | Tag | Relation |
|------|-----------|-----|----------|
| 存储位置 | 实体自身 | 位图标记 | 实体自身 |
| 数据内容 | 任意数据 | 无数据 | 目标实体 ID / 任意数据 |
| 数量限制 | 同一类型仅一个 | 无限制 | 同一类型可有多个目标（除非是 Single-Target Constraint） |
| 查询能力 | 通过 Family | 通过 Family | 通过 Relation 查询 |

### 1.3 核心术语

- **Relation Kind (关系类型)**：类似于组件类型，定义关系的含义（如 `OwnedBy`）。
- **Relation Target (关系目标)**：关系指向的实体。
- **Relation Data (关系数据)**：附加在关系上的额外数据。
- **Relation (完整关系)**：关系类型 + 关系目标的组合。

---

## 2. 核心类型

### 2.1 普通 Relation

普通 Relation 是最基本的关系类型，将一个实体指向另一个目标实体。它用于表达「A 拥有 B」或「A 引用 B」这样的关联。

```kotlin
sealed class OwnerBy
sealed class EquippedBy

// 添加普通 Relation
world.editor(sword) {
    addRelation<OwnerBy>(player)
    addRelation<EquippedBy>(player)
}

// 获取 Relation 目标的数据
val targetData = sword.getRelation<OwnerBy>(player)
```

带数据的 Relation 允许在关系中附加额外信息：

```kotlin
// 方式一：泛型指定数据类型
world.editor(entity) {
    addRelation<OwnerBy, Name>("倚天剑")
}

// 方式二：参数传递数据
world.editor(entity) {
    addRelation<OwnerBy>(player, "倚天剑")
}

// 获取数据
val name: String = entity.getRelation<Name, OwnerBy>()
```

### 2.2 单目标约束关系 (Single-Target Constraint Relation)

单目标约束关系是一种特殊的 Relation，通过在注册时声明，确保该类型的关系在同一实体上**只能有一个目标**。

- **行为**：当添加该类型的新目标时，系统会自动移除旧的目标。
- **用途**：表达排他性关系，如「主武器」、「当前选中的目标」等。

```kotlin
sealed class MainWeapon

// 第一次设置
world.editor(player) {
    addRelation<MainWeapon>(sword)
}

// 第二次设置：会自动移除与 sword 的 MainWeapon 关系
world.editor(player) {
    addRelation<MainWeapon>(shield)
}

// 获取单目标关系的数据（通常用于 Single Relation 模式）
val data = player.getRelation<WeaponData, MainWeapon>()
```

### 2.3 Shared Component


Shared Component（共享组件）是一种特殊的 Relation，它不存储在实体自身，而是存储在全局的 Component 表中。所有引用同一 Shared Component 的实体共享同一份数据，实现类似「全局配置」的效果。

```kotlin
data class GameConfig(
    val maxHealth: Int = 100,
    val gravity: Float = 9.8f,
    val enableCheats: Boolean = false
)

data class GlobalConfig(
    val serverUrl: String,
    val debugMode: Boolean
)

// 添加 Shared Component（使用已有实例）
val config = GlobalConfig("https://api.game.com", true)
world.editor(entity) {
    addSharedComponent(config)
}

// 添加 Shared Component（自动创建默认实例）
world.editor(entity) {
    addSharedComponent<GlobalConfig>()
}

// 获取 Shared Component
val retrievedConfig = entity.getSharedComponent<GlobalConfig>()
```

Shared Component 的特点：

- 数据存储在全局表中，不随实体迁移 Archetype
- 多个实体共享同一份数据，修改会影响所有引用者
- 适合存储全局配置、缓存数据等

### 2.4 内置类型

ECS 框架内置了几种常用的 Relation 类型，它们在 `Components` 对象中定义：

| 类型 | ComponentId | 用途 |
|------|-------------|------|
| `ComponentOf` | `components.componentOf` | 普通组件标记 |
| `SharedOf` | `components.sharedOf` | 共享组件标记 |
| `ChildOf` | `components.childOf` | 父子层级关系 |
| `InstanceOf` | `components.instanceOf` | 预制体实例化 |

这些内置类型通过 sealed class 定义：

```kotlin
sealed class SharedOf    // 标记 Shared Component
sealed class ComponentOf // 标记普通 Component
sealed class ChildOf     // 标记父子关系
sealed class InstanceOf  // 标记预制体实例
sealed class Prefab      // 标记预制体
sealed class NoInherit  // 标记不继承关系
```

---

## 3. 完整 API

### 3.1 EntityCreateContext（添加 Relation）

`EntityCreateContext` 提供了在创建实体时添加 Relation 的能力。

```kotlin
// 创建实体并添加 Relation
val sword = world.entity {
    // 普通 Relation（指向目标实体）
    addRelation<OwnerBy>(player)
    
    // 带数据的 Relation
    addRelation<OwnerBy>(player, "倚天剑")
    addRelation<OwnerBy, Name>("屠龙刀")
    
    // Single-Target Constraint Relation (单目标约束)
    addRelation<MainWeapon>(sword)
    
    // Shared Component
    addSharedComponent<GlobalConfig>(config)
    addSharedComponent<GameConfig>()  // 使用默认实例
    
    // 组件和 Tag
    addComponent(WeaponData(50, 100))
    addTag<ActiveTag>()
    
    // 父子关系快捷方式
    parent(parentEntity)
}
```

完整的添加 API 列表：

```kotlin
// 在 world.entity { ... } 或 world.editor(entity) { ... } 中使用
addRelation<K>(target)                              // 无数据
addRelation<K>(target, data)                        // 带数据
addRelation<K, T>(data)                             // 带数据类型声明

// Shared Component
addSharedComponent<C>(component)                    // 使用实例
addSharedComponent<C>()                             // 使用默认实例

// 组件和 Tag
addComponent<C>(component)
addTag<C>()

// 父子关系
parent(parent)
```

### 3.2 EntityUpdateContext（删除 Relation）

删除 Relation 需要使用 `EntityUpdateContext`，它通过 `editor` 方法进入：

```kotlin
// 进入编辑上下文
world.editor(entity) {
    // 删除特定的 Relation
    // 注意：需要使用 Relation 对象
    removeRelation(relations.relation<OwnerBy>(player))
    
    // 删除所有某种类型的关系
    removeRelation(relations.kind<OwnerBy>())
    
    // 删除组件
    removeComponent(components.id<Health>())
    
    // 删除 Tag
    removeTag<ActiveTag>()
}
```

### 3.3 EntityRelationContext（查询 Relation）

`EntityRelationContext` 是所有实体操作的基础接口，提供了丰富的查询方法：

```kotlin
class MySystem : EntityRelationContext {
    override lateinit var world: World
    
    fun process(entity: Entity) {
        // 获取 Relation 数据
        val ownerData: OwnerBy = entity.getRelation<OwnerBy>(player)
        val name: String = entity.getRelation<Name, OwnerBy>()
        
        // 获取组件
        val health: Health = entity.getComponent<Health>()
        
        // 获取 Shared Component
        val config: GlobalConfig = entity.getSharedComponent<GlobalConfig>()
        
        // 检查存在性
        val hasOwner: Boolean = entity.hasRelation(relations.relation<OwnerBy>(player))
        val hasHealth: Boolean = entity.hasComponent<Health>()
        val hasTag: Boolean = entity.hasTag<ActiveTag>()
        val hasShared: Boolean = entity.hasSharedComponent<GlobalConfig>()
    }
}
```

查询 API 速查：

```kotlin
// 获取数据
entity.getRelation<K>(target)           // 获取指定目标的 Relation
entity.getRelation<K, T>()             // 获取 Single Relation 的数据
entity.getComponent<C>()               // 获取组件
entity.getSharedComponent<C>()        // 获取 Shared Component

// 检查存在
entity.hasRelation(relation)           // 检查 Relation 是否存在
entity.hasComponent<C>()              // 检查组件是否存在
entity.hasTag<C>()                    // 检查 Tag 是否存在
entity.hasSharedComponent<C>()        // 检查 Shared Component 是否存在
```

---

## 4. 使用场景

### 4.1 Ownership（拥有关系）

Ownership 用于表达实体之间的拥有关系，例如玩家拥有武器、商人拥有商品、宗门拥有弟子等。

```kotlin
sealed class OwnerBy
sealed class ContainedBy

// 玩家创建武器
val player = world.entity { }
val sword = world.entity {
    addComponent(WeaponData(50, 100))
    addRelation<OwnerBy>(player)
}
val shield = world.entity {
    addComponent(DefenseData(30))
    addRelation<OwnerBy>(player)
}

// 查询玩家拥有的所有武器
class OwnerByQueryContext(world: World, val player: Entity) : EntityQueryContext(world) {
    val ownerBy: OwnerBy by relation(player)
    
    override fun FamilyBuilder.configure() {
        relation<OwnerBy>(player)
    }
}
world.query { OwnerByQueryContext(this, player) }.forEach { ctx, entity ->
    println("拥有武器: $entity")
}

// 物品栏系统
val inventory = world.entity {
    addComponent(InventoryData(10))
}
val potion = world.entity {
    addComponent(ItemData("生命药水"))
    addRelation<ContainedBy>(inventory)
}
```

### 4.2 Hierarchy（父子层级）

父子层级用于表达实体之间的层级关系，例如武器挂在角色身上、UI 组件的嵌套、场景中的物体层级等。

```kotlin
// 方式一：使用 parent() 方法
val player = world.entity {
    addComponent(Transform())
}
val weapon = player.childOf {
    addComponent(Transform())
    addComponent(WeaponData(100, 50))
}

// 方式二：使用 addRelation
val armor = world.entity {
    addComponent(DefenseData(50))
    addRelation(components.childOf, player)
}

// 遍历子实体
class ChildrenQueryContext(world: World, val parent: Entity) : EntityQueryContext(world) {
    val childOf: ChildOf by relation<ChildOf, Player>()
    
    override fun FamilyBuilder.configure() {
        relation<ChildOf>(parent)
    }
}

fun getChildren(parent: Entity): List<Entity> {
    return world.query { ChildrenQueryContext(this, parent) }
        .map { it.second }
        .toList()
}

// 层级变换传播
class TransformChildQuery(world: World) : EntityQueryContext(world) {
    var transform: Transform by component()
    val childOf: ChildOf by relation()
    
    override fun FamilyBuilder.configure() {
        component<Transform>()
        component<ChildOf>()
    }
}

class TransformSystem : EntityRelationContext {
    override lateinit var world: World
    
    fun update(dt: Float) {
        world.query { TransformChildQuery(this) }.forEach { ctx, entity ->
            val parentTransform = ctx.childOf.getComponent<Transform>()
            // 计算世界变换矩阵
        }
    }
}
```

### 4.3 Prefab（预制体实例化）

Prefab 用于创建可复用的实体模板，然后通过实例化创建具体的游戏对象。这在游戏开发中非常常见，例如创建怪物模板、道具模板等。

```kotlin
// 定义怪物预制体
val goblinPrefab = world.entity {
    addComponent(Health(50, 50))
    addComponent(AttackData(10))
    addComponent(MoveSpeed(5f))
    addTag<EnemyTag>()
}
world.entity { addTag<Prefab>() }  // 标记为预制体

// 实例化预制体
val goblin1 = goblinPrefab.instanceOf {
    addComponent(Position(10, 20))
    addComponent(Level(1))
}

val goblin2 = goblinPrefab.instanceOf {
    addComponent(Position(30, 40))
    addComponent(Level(2))
}

// 查询所有实例
class InstanceOfQueryContext(world: World, val prefab: Entity) : EntityQueryContext(world) {
    val instanceOf: InstanceOf by relation(prefab)
    
    override fun FamilyBuilder.configure() {
        relation<InstanceOf>(prefab)
    }
}
val allGoblins = world.query { InstanceOfQueryContext(this, goblinPrefab) }
```

预制体的内部实现：

```kotlin
// instanceOf 内部实现
fun World.instanceOf(prefab: Entity, configuration: EntityCreateContext.(Entity) -> Unit): Entity = entity {
    configuration(it)
    addRelation(components.instanceOf, prefab)  // 添加 InstanceOf Relation
}
```

### 4.4 Shared Component（全局配置）

Shared Component 适合存储需要在多个实体间共享的数据，例如游戏配置、缓存数据、全局状态等。

```kotlin
// 游戏配置
data class GameConfig(
    val maxPlayers: Int = 4,
    val roundTime: Int = 60,
    val enablePvP: Boolean = true
)

// 场景配置
data class SceneConfig(
    val sceneId: Int,
    val backgroundMusic: String,
    val weather: String
)

// 使用方式
val gameConfig = GameConfig(maxPlayers = 8, enablePvP = false)
val sceneConfig = SceneConfig(1, "battle.mp3", "rain")

// 在实体上添加 Shared Component
val gameWorld = world.entity {
    addSharedComponent(gameConfig)
    addSharedComponent(sceneConfig)
}

// 获取配置
val config = gameWorld.getSharedComponent<GameConfig>()
println("最大玩家数: ${config.maxPlayers}")
```

---

## 5. 进阶话题

### 5.1 Family 查询 + Relation

Family 查询系统可以与 Relation 深度集成，实现复杂的关系查询：

```kotlin
// 查询某个玩家拥有的所有实体
class PlayerOwnedQuery(world: World, val player: Entity) : EntityQueryContext(world) {
    val ownerBy: OwnerBy by relation(player)
    
    override fun FamilyBuilder.configure() {
        relation<OwnerBy>(player)
    }
}
val playerItems = world.query { PlayerOwnedQuery(this, player) }

// 查询所有装备了武器的角色
class EquippedWeaponQuery(world: World) : EntityQueryContext(world) {
    val ownerBy: OwnerBy by relation()
    var weaponData: WeaponData by component()
    
    override fun FamilyBuilder.configure() {
        relation<OwnerBy>()
        component<WeaponData>()
    }
}
val equippedCharacters = world.query { EquippedWeaponQuery(this) }
    .map { ctx ->
        ctx.ownerBy to ctx.weaponData
    }

// 查询父子层级中的所有子实体
class ChildrenQuery2(world: World, val parent: Entity) : EntityQueryContext(world) {
    val childOf: ChildOf by relation(parent)
    
    override fun FamilyBuilder.configure() {
        relation<ChildOf>(parent)
    }
}
val allChildren = world.query { ChildrenQuery2(this, parentEntity) }

// 复合查询：玩家的武器且已装备
class PlayerEquippedWeaponQuery(world: World, val player: Entity) : EntityQueryContext(world) {
    val ownerBy: OwnerBy by relation(player)
    var weaponData: WeaponData by component()
    
    override fun FamilyBuilder.configure() {
        relation<OwnerBy>(player)
        component<IsEquipped>()
        component<WeaponData>()
    }
}
val equippedWeapons = world.query { PlayerEquippedWeaponQuery(this, player) }
```

### 5.2 Observer 事件

Relation 的添加、删除、修改都会触发 Observer 事件，这使得我们可以监听实体关系的变化：

```kotlin
// 监听组件变化（包括 Relation）
world.observe(entity, OnInserted) { _, _, relation ->
    println("添加了 Relation: $relation")
}

world.observe(entity, OnRemoved) { _, _, relation ->
    println("删除了 Relation: $relation")
}

world.observe(entity, OnUpdated) { _, _, relation ->
    println("更新了 Relation: $relation")
}

// 监听实体创建和销毁
world.observe(entity, OnEntityCreated) { entity ->
    println("实体创建: $entity")
}

world.observe(entity, OnEntityDestroyed) { entity ->
    println("实体销毁: $entity")
}
```

### 5.3 性能注意事项

Relation 系统在设计时考虑了性能，但仍有一些需要注意的点：

1. **Relation 查询有开销**：每次 `getRelation` 都需要在 Archetype 中查找组件索引，避免在热路径中频繁调用。

2. **缓存查询结果**：如果需要多次查询同一 Relation，考虑将结果缓存到局部变量或组件中。

3. **批量操作优化**：使用 `BatchEntityEditor` 进行批量 Relation 操作，减少 Archetype 切换次数。

```kotlin
// 性能不佳：频繁查询
class OwnerByQuery2(world: World) : EntityQueryContext(world) {
    val owner: Entity by relation()
    val weapon: WeaponData by component()
    
    override fun FamilyBuilder.configure() {
        relation<OwnerBy>()
        component<WeaponData>()
    }
}

world.query { OwnerByQuery2(this) }.forEach { ctx, entity ->
    val owner = ctx.owner  // 使用缓存的查询结果
    val weapon = ctx.weapon
    // 处理...
}

// 优化：使用 Query Context 缓存
class OwnerContext(world: World) : EntityQueryContext(world) {
    val owner: Entity by relation()
    val weapon: WeaponData by component()
}

world.query { OwnerContext(this) }.forEach { ctx ->
    // owner 和 weapon 已被缓存
    process(ctx.owner, ctx.weapon)
}
```

4. **Shared Component 变更影响所有引用者**：修改 Shared Component 会影响所有引用该组件的实体，确保这是预期行为。

5. **避免循环引用**：实体间的循环引用（如 A 持有 B，B 持有 A）可能导致查询死循环，设计时需注意。

---

## 6. 最佳实践

### 6.1 命名规范

Relation 类型的命名应遵循以下规范：

1. **使用 sealed class**：Relation 类型应使用 sealed class 定义，确保类型安全。

2. **命名模式**：
   - 描述所有关系：`OwnedBy`、`ContainedBy`、`MountedOn`
   - 描述排他性关系：`MainWeapon`、`TargetOf`
   - 描述实例：`InstanceOf`、`ChildOf`

```kotlin
// 好的命名
sealed class OwnerBy           // 被谁拥有
sealed class ContainedBy       // 被谁包含
sealed class EquippedBy        // 被谁装备
sealed class MainWeapon        // 主武器（单目标约束）
sealed class InstanceOf        // 实例化自

// 不好的命名
sealed class Has               // 过于模糊
sealed class Rel               // 缩写不清晰
sealed class A                 // 无意义名称
```

### 6.2 生命周期管理

Relation 的生命周期需要谨慎管理：

1. **实体销毁时自动清理**：当实体被销毁时，其所有 Relation 会被自动清理。

2. **手动清理不再使用的关系**：如果两个实体解绑，应及时删除它们之间的关系。

```kotlin
// 玩家丢弃武器时
world.editor(weapon) {
    // 删除拥有关系
    // 注意：需要使用实际的 Relation 对象
}

// 更好的方式：使用命令模式收集变更
val relationRemovals = mutableListOf<Pair<Entity, Relation>>()

// 在系统中收集需要移除的关系
class OwnerByQuery3(world: World) : EntityQueryContext(world) {
    val owner: Entity by relation()
    
    override fun FamilyBuilder.configure() {
        relation<OwnerBy>()
    }
}

world.query { OwnerByQuery3(this) }.forEach { ctx, entity ->
    val owner = ctx.owner
    if (!owner.isActive()) {
        relationRemovals.add(entity to relations.relation<OwnerBy>(owner))
    }
}

// 批量处理
relationRemovals.forEach { (entity, relation) ->
    world.editor(entity) {
        // 删除关系
    }
}
```

### 6.3 常见陷阱

1. **混淆 Relation 和 Component**：
   ```kotlin
   // 错误：使用 addComponent 添加关系
   entity.addComponent(OwnerBy(player))
   
// 正确：使用 addRelation
world.editor(entity) {
    addRelation<OwnerBy>(player)
}

   ```

2. **忘记 Relation 的目标可能无效**：
   ```kotlin
   // 检查目标实体是否有效
   val target = entity.getRelation<OwnerBy>()
   if (target != Entity.ENTITY_INVALID && target.isActive()) {
       // 处理...
   }
   ```

3. **在循环中创建 Relation 对象**：
   ```kotlin
   // 性能问题：每次迭代都创建新 Relation
   entities.forEach { entity ->
       val relation = relations.relation<OwnerBy>(player)  // 重复创建
       // 处理...
   }
   
   // 优化：预先创建 Relation 对象
   val ownerRelation = relations.relation<OwnerBy>(player)
   entities.forEach { entity ->
       // 使用预创建的 relation
   }
   ```

4. **Shared Component 修改影响范围不明**：
   ```kotlin
   // 危险：修改 Shared Component 影响所有引用者
   val config = entity.getSharedComponent<GameConfig>()
   config.maxPlayers = 100  // 错误：组件是不可变的
   
// 正确：创建新的实例
val newConfig = config.copy(maxPlayers = 100)
world.editor(entity) {
    addSharedComponent(newConfig)
}

   ```

---

## 7. 快速参考

### 速查表

#### 添加 Relation

| 场景 | 代码 |
|------|------|
| 普通 Relation | `addRelation<OwnerBy>(player)` |
| 带数据 | `addRelation<OwnerBy>(player, "名称")` |
| 单目标约束 | `addRelation<MainWeapon>(sword)` |
| Shared Component | `addSharedComponent<Config>(config)` |
| 父子关系 | `parent(parent)` |
| 实例化 | `prefab.instanceOf { }` |

#### 查询 Relation

| 场景 | 代码 |
|------|------|
| 获取指定目标的数据 | `entity.getRelation<OwnerBy>(player)` |
| 获取单目标数据 | `entity.getRelation<T, K>()` |
| 获取组件 | `entity.getComponent<Health>()` |
| 获取 Shared | `entity.getSharedComponent<Config>()` |
| 检查存在 | `entity.hasRelation(relation)` |

#### 检查状态

| 场景 | 代码 |
|------|------|
| 检查 Relation | `entity.hasRelation(relations.relation<OwnerBy>(player))` |
| 检查组件 | `entity.hasComponent<Health>()` |
| 检查 Tag | `entity.hasTag<ActiveTag>()` |
| 检查 Shared | `entity.hasSharedComponent<Config>()` |

### 内置 Relation 类型

| 类型 | 用处 |
|------|------|
| `components.childOf` | 父子层级 |
| `components.instanceOf` | 预制体实例化 |
| `components.componentOf` | 组件标记 |
| `components.sharedOf` | 共享组件标记 |

---

## 下一步

- 模板系统：[04-templates.md](04-templates.md)
- 查询 DSL：[02-patterns.md](02-patterns.md)
- 快速开始：[00-quick-start.md](00-quick-start.md)
- 性能优化：[07-performance.md](07-performance.md)
