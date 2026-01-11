# Query定义指南

Query系统是Sect项目中用于查询ECS世界中实体的机制。通过Query，你可以根据组件组合筛选实体，并对匹配的实体执行操作。

## 核心概念

### Query

Query是一个用于查询实体的对象，它可以根据组件组合筛选实体，并提供遍历匹配实体的方法。

### EntityQueryContext

EntityQueryContext是Query的上下文，包含了查询相关的信息，如世界、实体和组件访问器。

### Accessor

Accessor是用于访问实体组件的对象，分为只读访问器和读写访问器。

### Family

Family是一组具有相同组件组合的实体，Query使用Family来组织和访问实体。

## 定义查询

查询通过`World.query()`方法创建，你可以在lambda表达式中定义查询条件和组件访问器。

### 1. 简单查询

简单查询是指查询具有特定组件组合的实体。

```kotlin
// 定义查询上下文
class PositionQueryContext(world: World) : EntityQueryContext(world) {
    val position by component<Position>()
}

// 查询具有Position组件的实体
val positionQuery = world.query { PositionQueryContext(world) }

// 定义查询上下文
class MovementQueryContext(world: World) : EntityQueryContext(world) {
    val position by component<Position>()
    val velocity by component<Velocity>()
}

// 查询具有Position和Velocity组件的实体
val movementQuery = world.query { MovementQueryContext(world) }
```

### 2. 使用EntityQueryContext

EntityQueryContext提供了更强大的查询能力，你可以在查询中定义组件访问器。

```kotlin
// 定义查询上下文
class PositionQueryContext(world: World) : EntityQueryContext(world) {
    // 定义组件访问器
    val position by component<Position>()
}

// 创建查询
val positionQuery = world.query { PositionQueryContext(world) }
```

### 3. 带有多个组件访问器的查询

```kotlin
// 定义查询上下文
class MovementQueryContext(world: World) : EntityQueryContext(world) {
    // 定义多个组件访问器
    val position by component<Position>()
    val velocity by component<Velocity>()
}

// 创建查询
val movementQuery = world.query { MovementQueryContext(world) }
```

### 4. 使用FamilyBuilder配置查询

你可以通过重写`configure()`方法来自定义查询条件。

```kotlin
// 定义带自定义配置的查询上下文
class CustomQueryContext(world: World) : EntityQueryContext(world) {
    val position by component<Position>()
    val health by component<Health>()
    
    // 重写configure方法，自定义查询条件
    override fun FamilyBuilder.configure() {
        all(Position::class, Health::class)
        none(Dead::class)
    }
}

// 创建查询
val customQuery = world.query { CustomQueryContext(world) }
```

## 使用查询

### 1. 遍历查询结果

```kotlin
// 定义查询上下文
class MovementQueryContext(world: World) : EntityQueryContext(world) {
    val position by component<Position>()
    val velocity by component<Velocity>()
}

// 创建查询
val movementQuery = world.query { MovementQueryContext(world) }

// 遍历查询结果
movementQuery.forEach { context ->
    // 使用组件访问器获取组件
    val position = context.position
    val velocity = context.velocity
    
    // 更新位置
    position.x += velocity.dx * deltaTime
    position.y += velocity.dy * deltaTime
}
```

### 2. 使用collect方法

```kotlin
// 定义查询上下文
class PositionQueryContext(world: World) : EntityQueryContext(world) {
    val position by component<Position>()
}

// 创建查询
val positionQuery = world.query { PositionQueryContext(world) }

// 使用collect方法遍历查询结果
positionQuery.collect {
    // 处理查询结果
    println("Entity ${it.entity.id} at position (${it.position.x}, ${it.position.y})")
}
```

### 3. 检查查询是否包含原型

```kotlin
// 定义查询上下文
class MovementQueryContext(world: World) : EntityQueryContext(world) {
    val position by component<Position>()
    val velocity by component<Velocity>()
}

// 创建查询
val movementQuery = world.query { MovementQueryContext(world) }

// 检查查询是否包含特定原型
val archetype = world.archetypeService.getArchetype(Position::class, Velocity::class)
if (archetype in movementQuery.family) {
    println("Query contains the archetype")
}
```

## 查询条件

查询条件通过在`EntityQueryContext`中重写`configure()`方法来定义。

### 1. all条件

`all`条件要求实体具有所有指定的组件。

```kotlin
// 定义查询上下文，使用all条件
class MovementQueryContext(world: World) : EntityQueryContext(world) {
    val position by component<Position>()
    val velocity by component<Velocity>()
    
    // 重写configure方法，使用all条件
    override fun FamilyBuilder.configure() {
        all(Position::class, Velocity::class)
    }
}

// 创建查询
val movementQuery = world.query { MovementQueryContext(world) }
```

### 2. any条件

`any`条件要求实体具有至少一个指定的组件。

```kotlin
// 定义查询上下文，使用any条件
class OptionalQueryContext(world: World) : EntityQueryContext(world) {
    val position by component<Position>(optional = true)
    val velocity by component<Velocity>(optional = true)
    
    // 重写configure方法，使用any条件
    override fun FamilyBuilder.configure() {
        any(Position::class, Velocity::class)
    }
}

// 创建查询
val optionalQuery = world.query { OptionalQueryContext(world) }
```

### 3. none条件

`none`条件要求实体不具有任何指定的组件。

```kotlin
// 定义查询上下文，使用none条件
class NoDeadQueryContext(world: World) : EntityQueryContext(world) {
    val health by component<Health>()
    
    // 重写configure方法，使用none条件
    override fun FamilyBuilder.configure() {
        all(Health::class)
        none(Dead::class)
    }
}

// 创建查询
val noDeadQuery = world.query { NoDeadQueryContext(world) }
```

### 4. or条件

`or`条件用于组合多个条件，实体只需满足其中一个条件即可。

```kotlin
// 定义查询上下文，使用or条件
class ComplexQueryContext(world: World) : EntityQueryContext(world) {
    val position by component<Position>()
    val velocity by component<Velocity>()
    val health by component<Health>()
    val damage by component<Damage>()
    
    // 重写configure方法，使用or条件
    override fun FamilyBuilder.configure() {
        or {
            all(Position::class, Velocity::class)
            all(Health::class, Damage::class)
        }
    }
}

// 创建查询
val complexQuery = world.query { ComplexQueryContext(world) }
```

## 组件访问器

组件访问器允许你在查询上下文中访问实体的组件。

### 1. 基本组件访问器

```kotlin
// 定义组件访问器
class PositionQueryContext(world: World) : EntityQueryContext(world) {
    val position by component<Position>()
}
```

### 2. 可选组件访问器

```kotlin
// 定义可选组件访问器
class OptionalQueryContext(world: World) : EntityQueryContext(world) {
    val position by component<Position>(optional = true)
}
```

### 3. 关系访问器

```kotlin
// 定义关系访问器
class RelationQueryContext(world: World) : EntityQueryContext(world) {
    val parent by relation<Parent>()
    val children by relation<Children>()
}
```

### 4. 关系向上访问器

```kotlin
// 定义关系向上访问器
class RelationUpQueryContext(world: World) : EntityQueryContext(world) {
    val parentEntity by relationUp<Parent>()
}
```

## 完整示例

### 1. 定义查询

```kotlin
// 定义查询上下文
class CombatQueryContext(world: World) : EntityQueryContext(world) {
    // 定义组件访问器
    val health by component<Health>()
    val damage by component<Damage>()
    val position by component<Position>()
    
    // 重写configure方法，自定义查询条件
    override fun FamilyBuilder.configure() {
        all(Health::class, Damage::class, Position::class)
        none(Dead::class)
    }
}

// 创建查询
val combatQuery = world.query { CombatQueryContext(world) }
```

### 2. 使用查询

```kotlin
// 在系统中使用查询
class CombatSystem(override val world: World) : WorldOwner {
    private val combatQuery = world.query { CombatQueryContext(world) }
    
    fun update(deltaTime: Float) {
        // 遍历查询结果
        combatQuery.forEach {context ->
            // 处理战斗逻辑
            println("Entity ${context.entity.id} has health: ${context.health.current}")
            println("Entity ${context.entity.id} has damage: ${context.damage.value}")
            
            // 更新健康值（示例）
            context.health.current -= context.damage.value
            
            // 如果健康值为0，添加Dead组件
            if (context.health.current <= 0) {
                context.entity.setComponent(Dead())
            }
        }
    }
}
```

## 查询的生命周期

### 1. 创建查询

查询通过`world.query()`方法创建，创建后可以多次使用。

### 2. 缓存查询

查询会自动缓存结果，当实体或组件发生变化时，查询会自动更新缓存。

### 3. 关闭查询

查询实现了`AutoCloseable`接口，可以通过`close()`方法关闭查询，释放资源。

```kotlin
// 关闭查询
query.close()
```

## 性能考虑

1. **缓存查询**：查询结果会自动缓存，避免重复计算
2. **批量处理**：查询会批量处理同一原型的实体，提高性能
3. **避免在查询中创建对象**：尽量避免在查询的lambda表达式中创建新对象
4. **使用适当的查询条件**：根据需要选择合适的查询条件，避免过度查询
5. **及时关闭查询**：不再使用的查询应该及时关闭，释放资源

## 查询与观察者的结合使用

查询可以与观察者结合使用，用于观察具有特定组件组合的实体。

```kotlin
// 观察具有Position和Velocity组件的实体
world.observeWithData<PositionChangedEvent> {}.exec(movementQuery) { context ->
    // 处理位置变化事件
    println("Entity ${entity.id} moved from (${event.oldX}, ${event.oldY}) to (${event.newX}, ${event.newY})")
    
    // 使用查询上下文
    println("Current position from query: (${context.position.x}, ${context.position.y})")
}
```

## 查询的使用场景

1. **系统更新**：在系统中使用查询来更新具有特定组件的实体
2. **碰撞检测**：查询具有碰撞组件的实体，进行碰撞检测
3. **AI行为**：查询具有AI组件的实体，更新AI行为
4. **渲染**：查询具有渲染组件的实体，进行渲染
5. **事件处理**：与观察者结合，处理具有特定组件的实体的事件

## 最佳实践

1. **复用查询**：尽量复用查询对象，避免频繁创建新查询
2. **合理设计查询条件**：根据实际需要设计查询条件，避免过度查询
3. **使用组件访问器**：使用组件访问器来访问组件，提高代码可读性和性能
4. **及时关闭查询**：不再使用的查询应该及时关闭，释放资源
5. **避免在查询中执行耗时操作**：查询的lambda表达式应该尽量简单，避免执行耗时操作

## 总结

查询系统是ECS架构中的核心组件之一，它提供了强大的实体查询能力。通过合理使用查询系统，你可以高效地处理具有特定组件组合的实体，提高系统的性能和可读性。

在使用查询系统时，应该遵循以下原则：

- 合理设计查询条件
- 复用查询对象
- 使用组件访问器
- 及时关闭查询
- 避免在查询中执行耗时操作

通过遵循这些原则，你可以充分发挥查询系统的优势，构建高效、可维护的ECS系统。