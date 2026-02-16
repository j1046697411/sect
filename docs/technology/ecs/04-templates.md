# ECS 代码模板

> 可直接复制使用的十大代码模板。

---

## T-001: 创建实体

```kotlin
// 文件: src/factories/player/PlayerFactory.kt
class PlayerFactory : EntityRelationContext {
    override lateinit var world: World

    fun createPlayer(name: String, level: Int): Entity {
        return world.entity {
            it.addComponent(PlayerName(name))
            it.addComponent(PlayerLevel(level))
            it.addComponent(Health(100, 100))
            it.addTag<ActiveTag>()
        }
    }
}
```

---

## T-002: 定义 Component

```kotlin
// 文件: src/components/PlayerComponent.kt
// 多属性组件
data class Health(val current: Int, val max: Int)
data class Position(val x: Int, val y: Int)

// 单属性高性能组件
@JvmInline value class Level(val value: Int)

// 注册
world.componentId<Health>()
world.componentId<Position>()
world.componentId<Level>()
```

---

## T-003: 定义 Tag

```kotlin
// 文件: src/tags/GameTags.kt
sealed class ActiveTag
sealed class DeadTag
sealed class PlayerTag

// 注册 - 使用 tag()
world.componentId<ActiveTag> { it.tag() }
world.componentId<DeadTag> { it.tag() }

// 使用
entity.addTag<ActiveTag>()
entity.hasTag<ActiveTag>()
entity.editor { it.removeTag<ActiveTag>() }
```

---

## T-004: 定义 Relation

```kotlin
// 文件: src/relations/GameRelations.kt
sealed class OwnerBy
sealed class Mentorship

// 注册
world.componentId<OwnerBy>()
world.componentId<Mentorship>()

// 使用
val sword = world.entity {
    it.addRelation<OwnerBy>(player)
}
val owner = sword.getRelation<OwnerBy, Name>()
```

---

## T-004a: 带数据的 Relation

```kotlin
// 文件: src/relations/GameRelations.kt

// 定义 Relation
sealed class EquippedBy  // 装备关系
sealed class ItemSlot    // 槽位关系
sealed class ParentChild // 父子关系

// 注册
world.componentId<EquippedBy>()
world.componentId<ItemSlot>()
world.componentId<ParentChild>()

// 使用 - 带数据的 Relation
val sword = world.entity {
    it.addRelation<EquippedBy, Position>(Position(0, 0))  // 装备在主手位置
    it.addRelation<ItemSlot>(player, "main_hand")          // 槽位名称
}

// 使用 - 目标 + 数据
val parent = world.entity { }
val child = world.entity {
    it.addRelation<ParentChild>(parent, 0)  // 序号 0
}

// 查询带数据的 Relation
val slot = sword.getRelation<ItemSlot, String>()        // 获取槽位名称
val pos = sword.getRelation<EquippedBy, Position>()     // 获取位置
val index = child.getRelation<ParentChild, Int>()       // 获取序号

// 可选：删除/更新 Relation
sword.editor {
    it.removeRelation<EquippedBy, Position>()
    it.addRelation<EquippedBy, Position>(Position(1, 1)) // 重新设置
}
```

---

## T-005: 创建 Service

```kotlin
// 文件: src/services/HealthService.kt
class HealthService : EntityRelationContext {
    override lateinit var world: World

    private class HealthContext(world: World) : EntityQueryContext(world) {
        val health: Health by component()
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

    fun damage(entity: Entity, amount: Int) {
        val health = entity.getComponent<Health>() ?: return
        entity.editor { it.addComponent(health.copy(current = maxOf(0, health.current - amount))) }
    }
}
```

---

## T-006: 创建 Factory

```kotlin
// 文件: src/factories/enemy/EnemyFactory.kt
class EnemyFactory : EntityRelationContext {
    override lateinit var world: World

    fun createEnemy(type: String, level: Int): Entity {
        return world.entity {
            it.addComponent(EnemyType(type))
            it.addComponent(Level(level))
            it.addComponent(Health(50 * level, 50 * level))
            it.addComponent(Position(0, 0))
            it.addTag<EnemyTag>()
            it.addTag<ActiveTag>()
        }
    }

    fun createBoss(name: String, level: Int): Entity {
        return world.entity {
            it.addComponent(Name(name))
            it.addComponent(Level(level))
            it.addComponent(Health(1000 * level, 1000 * level))
            it.addTag<EnemyTag>()
            it.addTag<BossTag>()
        }
    }
}
```

---

## T-007: 基本查询

```kotlin
// 文件: src/systems/MovementSystem.kt
class MovementSystem : EntityRelationContext {
    private class PositionContext(world: World) : EntityQueryContext(world) {
        val position: Position by component()
        val velocity: Velocity by component()
    }

    fun processMovement() {
        world.query { PositionContext(this) }
            .forEach { ctx ->
                val pos = ctx.position
                val vel = ctx.velocity
                ctx.entity.editor {
                    it.addComponent(Position(pos.x + vel.dx, pos.y + vel.dy))
                }
            }
    }

    // 收集结果
    fun findAll(): List<Entity> = world.query { PositionContext(this) }.map { it.entity }.toList()
    fun count(): Int = world.query { PositionContext(this) }.count()
}
```

---

## T-008: 条件查询

```kotlin
// 文件: src/systems/CombatSystem.kt
class CombatSystem : EntityRelationContext {
    private class ActivePlayerContext(world: World) : EntityQueryContext(world) {
        val health: Health by component()
        val level: Level by component()

        override fun FamilyBuilder.configure() {
            component<PlayerTag>()
            component<ActiveTag>()
        }
    }

    fun findNeedHeal(): List<Entity> = world.query { ActivePlayerContext(this) }
        .filter { it.health.current < it.health.max }
        .map { it.entity }.toList()

    fun findFullHealth(): List<Entity> = world.query { ActivePlayerContext(this) }
        .filter { it.health.current >= it.health.max }
        .map { it.entity }.toList()

    fun findHighLevel(threshold: Int): List<Entity> = world.query { ActivePlayerContext(this) }
        .filter { it.level.value >= threshold }
        .map { it.entity }.toList()
}
```

---

## T-009: 批量更新

```kotlin
// 文件: src/systems/BuffSystem.kt
class BuffSystem : EntityRelationContext {
    private class HealthContext(world: World) : EntityQueryContext(world) {
        val health: Health by component()
    }

    fun batchHeal(amount: Int) {
        val entities = world.query { HealthContext(this) }
            .filter { it.health.current < it.health.max }
            .map { it.entity }
            .toList()

        entities.forEach { entity ->
            val health = entity.getComponent<Health>() ?: return@forEach
            entity.editor {
                it.addComponent(health.copy(current = minOf(health.current + amount, health.max)))
            }
        }
    }

    // 使用对象池
    private val editorPool by lazy { BatchEntityEditorPool(world) }

    fun batchAddComponent(entities: List<Entity>, component: Component) {
        val editor = editorPool.obtain()
        try {
            entities.forEach { entity ->
                editor.entity = entity
                editor.addComponent(component)
            }
            editor.apply()
        } finally {
            editorPool.release(editor)
        }
    }
}
```

---

## T-010: 设置 Observer

```kotlin
// 文件: src/systems/ObserverSystem.kt
class ObserverSystem : EntityRelationContext {
    // 实体级别观察者
    fun setupEntityObserver() {
        val entity = world.entity { }
        entity.observe<OnInserted>().exec {
            println("实体 ${this.entity.id} 添加组件")
        }
        entity.editor { it.addComponent(Health(100, 100)) }
    }

    // 世界级别观察者
    fun setupWorldObserver() {
        world.observe<OnInserted>().exec {
            println("实体 ${this.entity.id} 添加组件")
        }
    }

    // 带数据的观察者
    fun setupDataObserver() {
        var lastEvent: String? = null
        val entity = world.entity { }
        entity.observeWithData<String>().exec { lastEvent = this.event }
        world.emit(entity, "玩家升级")
        println("收到: $lastEvent")
    }

    // 与 Query 结合
    fun setupFilteredObserver() {
        val entity = world.entity { it.addComponent(Position(10, 20)) }
        val query = world.query { PositionContext(this) }
        entity.observe<OnInserted>().exec(query) {
            if (this.position.y > 10) println("在地面上方")
        }
    }
}
```

---

## 快速参考

| 模板 | 用途 | 关键 API |
|------|------|----------|
| T-001 | 创建实体 | `world.entity { }` |
| T-002 | 组件 | `data class` / `value class` |
| T-003 | 标签 | `sealed class` + `tag()` |
| T-004 | 关系 | `addRelation<T>(entity)` |
| T-004a | 带数据的关系 | `addRelation<K, T>(data)` |
| T-005 | 服务 | `EntityRelationContext` + `query` |
| T-006 | 工厂 | `createXxx()` |
| T-007 | 查询 | `world.query { Context(this) }` |
| T-008 | 过滤 | `.filter { }` |
| T-009 | 批量 | `.toList()` + 循环 |
| T-010 | 事件 | `.observe<T>().exec { }` |

---

## 下一步

- 核心概念: [01-core-concepts.md](01-core-concepts.md)
- 常见模式: [02-patterns.md](02-patterns.md)
- 组件存储特化: [06-component-store.md](06-component-store.md)
