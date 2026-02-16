# ECS 速查表

> 一页纸可打印版本，包含最常用的操作代码。

---

## 1. 类型选择决策树

```
需要定义什么？
  │
  ├─ 多个属性 → data class Component
  │            └── data class Health(val current: Int, val max: Int)
  │
  ├─ 单个属性 → @JvmInline value class
  │            └── @JvmInline value class Level(val value: Int)
  │
  ├─ 状态标记 → sealed class Tag
  │            └── sealed class Alive
  │
  └─ 实体关联 → sealed class Relation
               └── sealed class OwnerBy
```

---

## 2. 操作对照表

### 定义 Component / Tag

| 操作 | 代码 |
|------|------|
| 多属性组件 | `data class Health(val current: Int, val max: Int)` |
| 单属性组件 | `@JvmInline value class Level(val value: Int)` |
| 状态标记 | `sealed class Alive` |
| 关系类型 | `sealed class OwnerBy` |

### 注册 Component / Tag

| 操作 | 代码 |
|------|------|
| 普通组件 | `world.componentId<Health>()` |
| Tag | `world.componentId<Alive> { it.tag() }` |

### 实体操作

| 操作 | 代码 |
|------|------|
| 创建空实体 | `val e = world.entity { }` |
| 创建并添加组件 | `world.entity { it.addComponent(Health(100, 100)) }` |
| 添加 Tag | `entity.addTag<Alive>()` |
| 获取组件 | `entity.getComponent<Health>()` |
| 检查 Tag | `entity.hasTag<Alive>()` |
| 检查组件 | `entity.hasComponent<Health>()` |
| 更新组件 | `entity.editor { it.addComponent(health.copy(current = 50)) }` |
| 移除组件 | `entity.editor { it.removeComponent<Health>() }` |
| 移除 Tag | `entity.editor { it.removeTag<Alive>() }` |
| 销毁实体 | `entity.destroy()` |

### 查询操作

| 操作 | 代码 |
|------|------|
| 基础查询 | ```kotlin
class HealthContext(world: World) : EntityQueryContext(world) {
    val health: Health by component()
}
world.query { HealthContext(this) }
``` |
| 多组件查询 | ```kotlin
class PositionHealthContext(world: World) : EntityQueryContext(world) {
    val position: Position by component()
    val health: Health by component()
}
world.query { PositionHealthContext(this) }
``` |
| 过滤 | `.filter { it.current > 50 }` |
| 映射 | `.map { it.current }` |
| 链式操作 | ```kotlin
world.query { HealthContext(this) }
    .filter { it.health.current > 0 }
    .toList()
``` |
| 遍历 | `.forEach { entity -> ... }` |

### 关系操作

| 操作 | 代码 |
|------|------|
| 添加关系 | `child.addRelation<OwnerBy>(parent)` |
| 获取关系 | `entity.getRelation<OwnerBy, ParentType>()` |
| 子实体 | `val child = parent.childOf { it.addComponent(...) }` |
| 实例化 | `val copy = prefab.instanceOf { it.addComponent(...) }` |

### 高级关系操作

| 操作 | 代码 |
|------|------|
| 带数据 Relation | `entity.addRelation<OwnerBy>(target, "武器名")` |
| 获取数据 | `val name = entity.getRelation<OwnerBy, String>()` |
| Single Relation | `entity.addRelation<IsEquipped>()` |
| Shared Component | `entity.addSharedComponent<Config>(config)` |
| 获取 Shared | `entity.getSharedComponent<Config>()` |
| 父子快捷 | `entity.parent(parent)` |

---

## 3. System 实现

```kotlin
// 查询型 System
class HealthSystem : EntityRelationContext {
    override lateinit var world: World

    fun update() {
        class HealthContext(world: World) : EntityQueryContext(world) {
            val health: Health by component()
        }
        world.query { HealthContext(this) }
            .filter { it.health.current > 0 }
            .forEach { ctx ->
                // 处理逻辑
            }
    }
}

// 修改型 System（使用 EntityQueryContext）
class DamageSystem : EntityRelationContext {
    fun applyDamage(target: Entity, damage: Int) {
        val health = target.getComponent<Health>() ?: return
        target.editor {
            it.addComponent(health.copy(current = maxOf(0, health.current - damage)))
        }
    }
}
```

---

## 4. 常见 Import

```kotlin
// ECS 核心
import cn.jzl.ecs.World
import cn.jzl.ecs.Entity
import cn.jzl.ecs.world.world

// 组件服务
import cn.jzl.ecs.component.componentId

// 查询服务
import cn.jzl.ecs.family.FamilyBuilder
import cn.jzl.ecs.query.component

// 关系
import cn.jzl.ecs.relation.childOf
import cn.jzl.ecs.relation.instanceOf
import cn.jzl.ecs.relation.addRelation
import cn.jzl.ecs.relation.getRelation
```

---

## 5. 完整示例

```kotlin
// 1. 定义
data class Health(val current: Int, val max: Int)
@JvmInline value class Level(val value: Int)
sealed class Alive

// 2. 注册
val addon = createAddon<Unit>("game") {
    components {
        world.componentId<Health>()
        world.componentId<Level>()
        world.componentId<Alive> { it.tag() }
    }
}

// 3. 创建 World
val world = world { install(addon) }

// 4. 创建实体
val player = world.entity {
    it.addComponent(Health(100, 100))
    it.addComponent(Level(1))
    it.addTag<Alive>()
}

// 5. 查询更新
class HealthContext(world: World) : EntityQueryContext(world) {
    val health: Health by component()
}
world.query { HealthContext(this) }
    .filter { it.health.current > 0 }
    .toList()
    .forEach { ctx ->
        // 处理逻辑
    }
```

---

## 6. 常见错误

| 错误 | 原因 | 解决 |
|------|------|------|
| 运行时异常 | 组件未注册 | 在 addAddon 中调用 componentId |
| 编译错误 | 直接修改组件 | 使用 copy() |
| 查询返回空 | 未调用 toList() | 消费 QueryStream |
| Tag 检查失败 | 用 hasComponent | 改用 hasTag |

---

> 详细文档: [00-quick-start.md](00-quick-start.md) | [01-core-concepts.md](01-core-concepts.md)
