# ECS 常见模式指南

> 真实项目总结的六大设计模式，包含场景描述、代码示例和最佳实践。

---

## 1. 组件设计模式

### 1.1 原子化原则

组件应该保持原子性，即每个组件只包含一个概念的数据。

```kotlin
// ✅ 正确：分离的关注点
data class Health(val current: Int, val max: Int)
data class Position(val x: Int, val y: Int)

// ❌ 错误：混合多个概念
data class EntityData(val health: Int, val x: Int, val y: Int)
```

### 1.2 value class 使用

单属性组件使用 `@JvmInline value class` 避免装箱开销。

```kotlin
// ✅ 正确：单属性使用 value class
@JvmInline value class Level(val value: Int)
@JvmInline value class Name(val value: String)

world.componentId<Level>()
entity.addComponent(Level(10))
```

### 1.3 Tag 设计

Tag 使用 `sealed class` 定义，不包含数据。

```kotlin
// ✅ 正确
sealed class ActiveTag
sealed class DeadTag

world.componentId<ActiveTag> { it.tag() }
entity.addTag<ActiveTag>()

// ❌ 错误
data class ActiveTag(val timestamp: Long)
```

---

## 2. Factory 模式

### 场景描述

封装复杂实体创建逻辑。

### 实现方式

继承 `EntityRelationContext`，提供 `createXxx()` 方法。

```kotlin
class PlayerFactory : EntityRelationContext {
    override lateinit var world: World
    
    fun createPlayer(name: String, level: Int): Entity {
        return world.entity {
            it.addComponent(PlayerName(name))
            it.addComponent(PlayerLevel(level))
            it.addComponent(Health(100, 100))
            it.addTag<ActiveTag>()
            it.addTag<PlayerTag>()
        }
    }
}

// 使用
val factory = PlayerFactory()
factory.world = world
val player = factory.createPlayer("掌门", 1)
```

---

## 3. Service 模式

### 场景描述

封装业务逻辑，查询和修改组件。

### 实现方式

继承 `EntityRelationContext`，内部使用 `world.query()`。

```kotlin
class HealthService : EntityRelationContext {
    override lateinit var world: World
    
    private class HealthContext(world: World) : EntityQueryContext(world) {
        val health: Health by component<Health>()
    }
    
    fun healAll(amount: Int) {
        world.query { HealthContext(this) }
            .filter { it.health.current < it.health.max }
            .forEach { ctx ->
                val health = ctx.health
                ctx.entity.editor {
                    it.addComponent(health.copy(
                        current = minOf(health.current + amount, health.max)
                    ))
                }
            }
    }
}
```

---

## 4. Query 模式

### 场景描述

通过定义 Context 类筛选符合条件的实体。

### 实现方式

继承 `EntityQueryContext`，使用 `by component<T>()` 委托。

```kotlin
class PositionContext(world: World) : EntityQueryContext(world) {
    val position: Position by component<Position>()
}

class ActivePlayerContext(world: World) : EntityQueryContext(world) {
    val name: PlayerName by component<PlayerName>()
    val level: PlayerLevel by component<PlayerLevel>()
    
    override fun FamilyBuilder.configure() {
        component<ActiveTag>()
        component<PlayerTag>()
    }
}

// 使用
world.query { PositionContext(this) }
    .filter { it.position.y > 0 }
    .forEach { ctx -> println(ctx.position) }

world.query { ActivePlayerContext(this) }
    .forEach { ctx -> println(ctx.name.value) }
```

### 缓存查询

```kotlin
class GameService : EntityRelationContext {
    override lateinit var world: World
    
    // ✅ 正确：缓存上下文
    private val positionContext by lazy { PositionContext(world) }
    
    fun process() {
        world.query { positionContext }.forEach { }
    }
    
    // ❌ 错误：每次创建新实例
    fun bad() {
        world.query { PositionContext(world) }
    }
}
```

---

## 5. Batch 模式

### 场景描述

批量操作多个实体，避免在迭代中修改导致异常。

### 实现方式

直接在遍历中处理，不需要收集结果。

```kotlin
// ✅ 正确：直接遍历处理
fun healAllTeam() {
    world.query { HealthContext(this) }
        .filter { it.health.current < it.health.max }
        .forEach { ctx ->
            val health = ctx.health
            ctx.entity.editor {
                it.addComponent(health.copy(current = health.current + 50))
            }
        }
}

// ❌ 错误：迭代中直接修改（会锁定实体结构）
fun bad() {
    world.query { HealthContext(this) }.forEach { ctx ->
        ctx.entity.editor { }  // 可能异常
    }
}
```

### 使用对象池

```kotlin
class BatchService : EntityRelationContext {
    override lateinit var world: World
    
    private val editorPool by lazy { BatchEntityEditorPool(world) }
    
    fun batchAdd(entities: List<Entity>) {
        val editor = editorPool.obtain()
        try {
            entities.forEach { entity ->
                editor.entity = entity
                editor.addComponent(Buff("power"))
            }
            editor.apply()
        } finally {
            editorPool.release(editor)
        }
    }
}
```

---

## 6. Observer 模式

### 实体级别

```kotlin
val entity = world.entity { }

entity.observe<OnInserted>().exec {
    println("组件添加: ${this.entity.id}")
}

entity.editor {
    it.addComponent(Health(100, 100))
}
```

### 世界级别

```kotlin
world.observe<OnInserted>().exec {
    println("实体 ${this.entity.id} 添加组件")
}
```

### 带数据

```kotlin
var data: String? = null

entity.observeWithData<String>().exec {
    data = this.event
}

world.emit(entity, "事件数据")
```

### 与 Query 结合

```kotlin
val entity = world.entity { it.addComponent(Position(10, 20)) }

entity.observe<OnInserted>().exec(world.query { PositionContext(this) }) {
    if (this.position.y > 0) {
        println("在地面上方")
    }
}
```

---

## 7. 模式组合示例

```kotlin
class DiscipleService : EntityRelationContext {
    override lateinit var world: World
    
    // Factory: 创建弟子
    fun createDisciple(name: String, level: Int): Entity {
        return world.entity {
            it.addComponent(DiscipleName(name))
            it.addComponent(DiscipleLevel(level))
            it.addComponent(Cultivation(0, 100))
            it.addTag<ActiveTag>()
        }
    }
    
    // Service: 修炼
    fun cultivate(disciple: Entity, amount: Int) {
        val cultivation = disciple.getComponent<Cultivation>() ?: return
        val newCultivation = cultivation.copy(current = cultivation.current + amount)
        
        if (newCultivation.current >= newCultivation.max) {
            disciple.editor {
                it.addComponent(Cultivation(0, newCultivation.max * 2))
            }
        } else {
            disciple.editor { it.addComponent(newCultivation) }
        }
    }
    
    // Query + Batch: 批量处理
    private class DiscipleContext(world: World) : EntityQueryContext(world) {
        val cultivation: Cultivation by component()
        
        override fun FamilyBuilder.configure() {
            component<DiscipleTag>()
            component<ActiveTag>()
        }
    }
    
    fun promoteReady() {
        world.query { DiscipleContext(this) }
            .filter { it.cultivation.current >= it.cultivation.max }
            .forEach { ctx ->
                ctx.entity.editor { it.addComponent(Cultivation(0, 200)) }
            }
    }
}
```

---

## 8. Relation 模式

Relation（关系）是 ECS 框架中表达实体间关联的机制，常用于 Ownership、Hierarchy、Prefab 等场景。

### 8.1 Ownership 模式（OwnerBy）

用于表示实体拥有关系（如玩家拥有武器、NPC 拥有道具）。

```kotlin
package cn.jzl.ecs

// 定义关系类型
sealed class OwnerBy
sealed class ContainedBy

// 添加拥有关系
val player = world.entity {
    it.addComponent(PlayerName("掌门"))
}

val sword = world.entity {
    it.addComponent(WeaponData(50, 100))
    it.addRelation<OwnerBy>(player)  // 剑被玩家拥有
}

val shield = world.entity {
    it.addComponent(DefenseData(30))
    it.addRelation<OwnerBy>(player)
}

// 查询玩家拥有的所有实体
class OwnerByContext(world: World, val player: Entity) : EntityQueryContext(world) {
    val ownerBy: Entity by relation(player)
    
    override fun FamilyBuilder.configure() {
        relation(relations.kind<OwnerBy>())
        relation(relations.target(player))
    }
}
val ownedByPlayer = world.query { OwnerByContext(this, player) }

// 反向查询：查询被某玩家拥有的所有实体
class ReverseOwnerByContext(world: World, val target: Entity) : EntityQueryContext(world) {
    val owner: Entity by relationUp<OwnerBy>()
    
    override fun FamilyBuilder.configure() {
        relation(relations.kind<OwnerBy>())
        relation(relations.target(target))
    }
}
val ownedByPlayer2 = world.query { ReverseOwnerByContext(this, player) }

// 玩家丢弃武器时删除关系
fun dropItem(item: Entity, owner: Entity) {
    world.editor(item) {
        // 删除拥有关系
    }
}
```

### 8.2 Hierarchy 模式（Parent/Child）

用于表示实体层级关系（如物品栏、场景图）。

```kotlin
package cn.jzl.ecs

// 方式一：使用 parent() 方法快捷创建子实体
val player = world.entity {
    it.addComponent(Transform())
}

val weapon = player.childOf {
    it.addComponent(Transform())
    it.addComponent(WeaponData(100, 50))
}

// 方式二：使用 addRelation 添加层级关系
val armor = world.entity {
    it.addComponent(DefenseData(50))
    it.addRelation(components.childOf, player)
}

// 遍历所有子实体
class ChildrenContext(world: World, val parent: Entity) : EntityQueryContext(world) {
    val childOf: Entity by relation(parent)
    
    override fun FamilyBuilder.configure() {
        relation(relations.kind<ChildOf>())
        relation(relations.target(parent))
    }
}
fun getChildren(parent: Entity, action: (Entity) -> Unit) {
    world.query { ChildrenContext(this, parent) }
        .forEach { ctx -> action(ctx.second) }
}

// 层级变换传播系统
class TransformChildContext(world: World) : EntityQueryContext(world) {
    var transform: Transform by component<Transform>()
    val childOf: Entity by relation<ChildOf>()
    
    override fun FamilyBuilder.configure() {
        component<Transform>()
        relation(relations.kind<ChildOf>())
    }
}

class TransformSystem : EntityRelationContext {
    override lateinit var world: World
    
    fun update(dt: Float) {
        world.query { TransformChildContext(this) }.forEach { ctx, entity ->
            val parentEntity = ctx.childOf
            val parentTransform = parentEntity.getComponent<Transform>()
            // 计算世界变换矩阵
        }
    }
}
```

### 8.3 Prefab 模式（InstanceOf）

用于预制体实例化（如怪物模板生成实例）。

```kotlin
package cn.jzl.ecs

// 定义怪物预制体
val goblinPrefab = world.entity {
    it.addComponent(Health(50, 50))
    it.addComponent(AttackData(10))
    it.addComponent(MoveSpeed(5f))
    it.addTag<EnemyTag>()
}
// 标记为预制体
world.entity { it.addTag<Prefab>() }

// 实例化预制体
val goblin1 = goblinPrefab.instanceOf {
    it.addComponent(Position(10, 20))
    it.addComponent(Level(1))
}

val goblin2 = goblinPrefab.instanceOf {
    it.addComponent(Position(30, 40))
    it.addComponent(Level(2))
}

// 查询所有实例
class InstanceOfContext(world: World, val prefab: Entity) : EntityQueryContext(world) {
    val instanceOf: Entity by relation(prefab)
    
    override fun FamilyBuilder.configure() {
        relation(relations.kind<InstanceOf>())
        relation(relations.target(prefab))
    }
}
val allGoblins = world.query { InstanceOfContext(this, goblinPrefab) }

// 预制体工厂
class MonsterFactory : EntityRelationContext {
    override lateinit var world: World
    
    fun spawnMonster(prefab: Entity, position: Position): Entity {
        return prefab.instanceOf {
            it.addComponent(position)
            it.addComponent(MonsterState(AIState.IDLE))
        }
    }
}
```

### 8.4 Shared Component 模式（全局配置）

用于全局共享配置（如游戏设置、环境参数）。

```kotlin
package cn.jzl.ecs

// 游戏配置数据
data class GameConfig(
    val maxPlayers: Int = 4,
    val roundTime: Int = 60,
    val enablePvP: Boolean = true
)

// 场景配置数据
data class SceneConfig(
    val sceneId: Int,
    val backgroundMusic: String,
    val weather: String
)

// 添加 Shared Component（首次添加需要实例）
val config = GameConfig(maxPlayers = 8, enablePvP = false)
val sceneConfig = SceneConfig(1, "battle.mp3", "rain")

val gameWorld = world.entity {
    it.addSharedComponent(config)
    it.addSharedComponent(sceneConfig)
}

// 后续添加不需要实例（使用已存在的共享组件）
val gameWorld2 = world.entity {
    it.addSharedComponent<GameConfig>()
}

// 更新共享组件（所有引用该组件的实体都会被更新）
val gameWorld3 = world.entity {
    it.addSharedComponent(GameConfig(maxPlayers = 16))
}

// 获取配置
val retrievedConfig = gameWorld.getSharedComponent<GameConfig>()
println("最大玩家数: ${retrievedConfig.maxPlayers}")

// Shared Component 适合存储全局共享数据
// 多个实体引用同一份数据，修改会影响所有引用者
class GameConfigService : EntityRelationContext {
    override lateinit var world: World
    
    private var config: GameConfig? = null
    
    fun initConfig() {
        val entity = world.entity {
            it.addSharedComponent(GameConfig())
        }
        config = entity.getSharedComponent<GameConfig>()
    }
    
    fun updateConfig(maxPlayers: Int) {
        val current = config ?: return
        // 创建新实例（Shared Component 不可变）
        val newConfig = current.copy(maxPlayers = maxPlayers)
        
        class SharedOfContext(world: World) : EntityQueryContext(world) {
            val sharedConfig: GameConfig by component<GameConfig>()
            
            override fun FamilyBuilder.configure() {
                relation(relations.sharedComponent<GameConfig>())
            }
        }
        
        world.query { SharedOfContext(this) }.forEach { ctx, entity ->
            entity.addSharedComponent(newConfig)
        }
    }
}
```

---

## 快速参考

| 模式 | 继承类 | 关键方法 |
|------|--------|----------|
| Factory | EntityRelationContext | `createXxx()` |
| Service | EntityRelationContext | 业务方法 + `world.query` |
| Query | EntityQueryContext | `by component()` |
| Observer | 无 | `.observe<T>().exec { }` |
| Batch | 无 | `toList()` + 循环 |

---

## 下一步

- 核心概念: [01-core-concepts.md](01-core-concepts.md)
- 快速开始: [00-quick-start.md](00-quick-start.md)
- 性能优化: [07-performance.md](07-performance.md)
- 测试指南: [08-testing.md](08-testing.md)
