# 事件定义指南

事件系统是Sect项目中用于处理实体组件变化的机制。通过事件系统，你可以观察实体组件的变化并执行相应的逻辑。

## 核心概念

### Observer

Observer是一个用于监听事件的对象，它可以观察实体组件的变化并执行回调函数。

### ObserverContext

ObserverContext是Observer的上下文，包含了事件相关的信息，如世界、实体和涉及的关系。

### ObserverContextWithData

ObserverContextWithData是带数据的Observer上下文，除了包含ObserverContext的信息外，还包含了事件数据。

### ObserveService

ObserveService是事件分发服务，负责将事件分发给对应的Observer。

## 定义事件

事件可以是任何类型，通常使用数据类来定义事件，以便携带相关数据。

### 1. 简单事件

简单事件是指不携带任何数据的事件，通常用于通知某些状态变化。

```kotlin
// 定义简单事件（空类）
class EntityCreatedEvent
class EntityDestroyedEvent
class ComponentAddedEvent
class ComponentRemovedEvent
```

### 2. 带数据的事件

带数据的事件用于携带相关数据，以便观察者能够获取事件的详细信息。

```kotlin
// 定义带数据的事件
data class PositionChangedEvent(val oldX: Float, val oldY: Float, val newX: Float, val newY: Float)
data class HealthChangedEvent(val oldHealth: Int, val newHealth: Int)
data class EntityMovedEvent(val fromPosition: Position, val toPosition: Position)

// 更复杂的事件数据类
data class CombatEvent(
    val attacker: Entity,
    val defender: Entity,
    val damage: Int,
    val critical: Boolean = false
)
```

### 3. 泛型事件

泛型事件可以用于处理多种类型的实体或组件。

```kotlin
// 定义泛型事件
data class ComponentChangedEvent<T>(val component: T, val oldValue: T? = null)

data class EntityEvent<T>(val entity: Entity, val eventType: String, val data: T? = null)
```

## 观察事件

观察事件是指创建Observer来监听特定类型的事件。

### 1. 观察简单事件

```kotlin
// 观察简单事件
world.observe<EntityCreatedEvent> {}.exec {
    // 处理实体创建事件
    println("Entity created: ${entity.id}")
}
```

### 2. 观察带数据的事件

```kotlin
// 观察带数据的事件
world.observeWithData<PositionChangedEvent> {}.exec {
    // 处理位置变化事件
    println("Entity ${entity.id} moved from (${event.oldX}, ${event.oldY}) to (${event.newX}, ${event.newY})")
}
```

### 3. 观察特定实体的事件

```kotlin
// 观察特定实体的事件
val playerEntity = world.createEntity()
playerEntity.observeWithData<HealthChangedEvent> {}.exec {
    // 处理玩家健康值变化事件
    println("Player health changed from ${event.oldHealth} to ${event.newHealth}")
}
```

### 4. 观察事件并使用查询

```kotlin
// 创建查询
val positionQuery = world.createQuery<PositionQueryContext> {
    all(Position::class)
}

// 观察事件并使用查询
world.observeWithData<PositionChangedEvent> {}.exec(positionQuery) { queryContext ->
    // 处理位置变化事件，并使用查询上下文
    val position = queryContext.getComponent(Position::class)
    println("Entity ${entity.id} new position: (${position.x}, ${position.y}, ${position.z})")
}
```

## 触发事件

触发事件是指向观察者发送事件通知。

### 1. 触发简单事件

```kotlin
// 触发简单事件
world.emit<EntityCreatedEvent>(entity)

// 或者使用实体的扩展函数
entity.emit<EntityCreatedEvent>()
```

### 2. 触发带数据的事件

```kotlin
// 触发带数据的事件
val oldPosition = Position(0.0f, 0.0f, 0.0f)
val newPosition = Position(1.0f, 1.0f, 0.0f)
val event = PositionChangedEvent(oldPosition.x, oldPosition.y, newPosition.x, newPosition.y)

world.emit(entity, event)

// 或者使用实体的扩展函数
entity.emit(event)
```

## 事件处理的最佳实践

1. **事件设计原则**：
   - 事件应该尽可能小，只包含必要的数据
   - 事件名称应该清晰地反映事件的含义
   - 事件数据类应该是不可变的（使用val属性）

2. **观察者设计原则**：
   - 观察者应该尽可能简单，只处理必要的逻辑
   - 避免在观察者中执行耗时操作，以免影响性能
   - 及时关闭不再使用的观察者，避免内存泄漏

3. **事件触发原则**：
   - 只在必要时触发事件
   - 避免频繁触发事件，以免影响性能
   - 确保事件数据的一致性

## 完整示例

### 1. 定义事件

```kotlin
// 定义事件数据类
data class HealthChangedEvent(val oldHealth: Int, val newHealth: Int)
data class PositionChangedEvent(val oldX: Float, val oldY: Float, val newX: Float, val newY: Float)
```

### 2. 创建观察者

```kotlin
// 创建健康值变化观察者
val healthObserver = world.observeWithData<HealthChangedEvent> {}.exec {
    println("Entity ${entity.id} health changed from ${event.oldHealth} to ${event.newHealth}")
    
    // 如果健康值为0，发送死亡事件
    if (event.newHealth <= 0) {
        entity.emit<EntityDiedEvent>()
    }
}

// 创建位置变化观察者
val positionObserver = world.observeWithData<PositionChangedEvent> {}.exec {
    println("Entity ${entity.id} moved from (${event.oldX}, ${event.oldY}) to (${event.newX}, ${event.newY})")
}
```

### 3. 触发事件

```kotlin
// 创建实体
val entity = world.createEntity()
entity.setComponent(Health(100, 100))
entity.setComponent(Position(0.0f, 0.0f, 0.0f))

// 触发位置变化事件
val oldPosition = Position(0.0f, 0.0f, 0.0f)
val newPosition = Position(1.0f, 1.0f, 0.0f)
entity.setComponent(newPosition)
entity.emit(PositionChangedEvent(oldPosition.x, oldPosition.y, newPosition.x, newPosition.y))

// 触发健康值变化事件
val oldHealth = Health(100, 100)
val newHealth = Health(80, 100)
entity.setComponent(newHealth)
entity.emit(HealthChangedEvent(oldHealth.current, newHealth.current))
```

### 4. 关闭观察者

```kotlin
// 关闭观察者
healthObserver.close()
positionObserver.close()
```

## 事件与系统的集成

事件系统可以与ECS系统很好地集成，用于处理实体组件的变化。

### 在系统中观察事件

```kotlin
class MovementSystem(override val world: World) : WorldOwner {
    private lateinit var positionObserver: Observer
    
    override fun initialize() {
        // 在系统初始化时创建观察者
        positionObserver = world.observeWithData<PositionChangedEvent> {}.exec {
            // 处理位置变化事件
            println("Entity ${entity.id} moved")
        }
    }
    
    override fun update(deltaTime: Float) {
        // 系统更新逻辑
    }
    
    override fun dispose() {
        // 在系统销毁时关闭观察者
        positionObserver.close()
    }
}
```

### 在系统中触发事件

```kotlin
class CombatSystem(override val world: World) : WorldOwner {
    fun attack(attacker: Entity, defender: Entity, damage: Int) {
        // 获取防御者的健康值组件
        val health = defender.getComponent(Health::class)
        if (health != null) {
            val oldHealth = health.current
            val newHealth = max(0, oldHealth - damage)
            health.current = newHealth
            
            // 触发健康值变化事件
            defender.emit(HealthChangedEvent(oldHealth, newHealth))
        }
    }
}
```

## 性能考虑

1. **避免频繁触发事件**：频繁触发事件会影响性能，应该只在必要时触发事件。
2. **简化观察者逻辑**：观察者逻辑应该尽可能简单，避免执行耗时操作。
3. **及时关闭观察者**：不再使用的观察者应该及时关闭，避免内存泄漏和不必要的事件处理。
4. **使用适当的事件粒度**：事件粒度应该适中，既不能太细（导致频繁触发），也不能太粗（导致信息不足）。

## 事件系统的使用场景

1. **实体生命周期管理**：监听实体的创建、销毁和组件变化
2. **游戏状态管理**：处理游戏状态的变化，如战斗开始、结束等
3. **UI更新**：当游戏状态变化时更新UI
4. **成就系统**：监听玩家行为，触发成就解锁
5. **音效和视觉效果**：当特定事件发生时播放音效或显示视觉效果
6. **AI行为**：基于事件触发AI行为

## 事件与Addon的集成

事件系统可以与Addon系统集成，用于处理Addon的生命周期事件。

```kotlin
val myAddon = createAddon("myAddon") {
    // 在Addon初始化时创建观察者
    val observer = world.observeWithData<AddonInitializedEvent> {}.exec {
        // 处理Addon初始化事件
        println("Addon ${event.addonName} initialized")
    }
    
    // 在Addon销毁时关闭观察者
    onStart {
        // 启动逻辑
    }
}
```