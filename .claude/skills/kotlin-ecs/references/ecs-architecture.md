# ECS 架构

## 什么是 ECS？

ECS（实体组件系统）是一种主要用于游戏开发的架构模式，将代码组织为三个主要部分：

1. **实体（Entity）**：唯一标识符，代表游戏世界中的一个对象
2. **组件（Component）**：数据容器，存储实体的属性
3. **系统（System）**：处理逻辑，操作具有特定组件的实体

## 核心原则

### 组合优于继承

ECS 使用组件组合代替传统的类继承。实体由多个组件组成，这些组件定义了实体的行为和属性，而不是创建深层的继承层次结构。

**传统继承**：
```kotlin
class GameObject {
    var position: Position
    var velocity: Velocity
}

class Player : GameObject {
    var health: Int
    var mana: Int
}

class Enemy : GameObject {
    var damage: Int
    var aggroRange: Float
}
```

**ECS 组合**：
```kotlin
// 组件
@Serializable data class Position(var x: Float, var y: Float)
@Serializable data class Velocity(var dx: Float, var dy: Float)
@Serializable data class Health(var value: Int)
@Serializable data class Mana(var value: Int)
@Serializable data class Damage(var value: Int)
@Serializable data class AggroRange(var value: Float)

// 实体由组件组合而成
// 玩家实体：Position + Velocity + Health + Mana
// 敌人实体：Position + Velocity + Damage + AggroRange
```

### 数据与逻辑分离

ECS 严格分离数据（组件）和逻辑（系统）。这种分离提供了几个好处：

- **更好的性能**：数据存储在连续内存中，提高了缓存局部性
- **更容易并行化**：系统可以并行运行，因为它们只访问特定组件
- **更大的灵活性**：实体可以在运行时动态组合和修改

### 系统操作组件家族

系统操作具有特定组件集合（"家族"）的实体。例如，`MovementSystem` 会操作同时具有 `Position` 和 `Velocity` 组件的实体。

## Sect 项目中的 ECS

### 实体创建

使用 `World` 类创建实体：

```kotlin
val world = World()
val entity = world.createEntity()
```

### 添加组件

使用 `setComponent` 方法向实体添加组件：

```kotlin
entity.setComponent(Position(0.0f, 0.0f))
entity.setComponent(Velocity(1.0f, 0.0f))
entity.setComponent(Health(100))
```

### 系统实现

系统实现 `WorldOwner` 接口并提供 `update` 方法：

```kotlin
class MovementSystem(override val world: World) : WorldOwner {
    fun update(deltaTime: Float) {
        // 获取所有具有 Position 和 Velocity 组件的实体
        val entities = world.getFamily(Position::class, Velocity::class)
        
        // 根据速度更新位置
        for (entity in entities) {
            val position = entity.getComponent(Position::class)
            val velocity = entity.getComponent(Velocity::class)
            
            position.x += velocity.dx * deltaTime
            position.y += velocity.dy * deltaTime
        }
    }
}
```

### 查询系统

项目提供了强大的查询系统用于过滤实体：

```kotlin
// 查询具有 Position 和 Velocity 组件的实体
val query = world.createQuery {
    all(Position::class, Velocity::class)
    any(Health::class, Shield::class)
    none(Dead::class)
}

// 获取匹配的实体
val entities = query.entities
```

## 性能考虑

1. **缓存局部性**：同一类型的组件存储在连续内存中
2. **批量处理**：批量处理实体以最小化缓存未命中
3. **避免循环中的组件查找**：遍历实体时缓存组件引用
4. **使用原型**：原型将具有相同组件组合的实体分组，以便更快访问
5. **限制系统依赖**：系统应尽可能独立，以便更好地并行化

## 最佳实践

1. **保持组件小巧**：每个组件应代表一个单一关注点
2. **组件中无逻辑**：组件应仅包含数据
3. **系统应专注**：每个系统应处理一个特定方面
4. **使用正确的抽象**：根据需要在家族和查询之间选择
5. **分析性能**：使用分析工具识别瓶颈
6. **记录组件用法**：清楚记录哪些系统使用哪些组件
7. **使用序列化**：使用 `@Serializable` 标记组件以支持保存/加载功能

## 常见模式

### 标签组件

标签组件是用于标记实体具有特定属性的空组件：

```kotlin
@Serializable
object PlayerTag // 玩家实体标记

@Serializable
object EnemyTag // 敌人实体标记
```

### 状态组件

状态组件表示实体的当前状态：

```kotlin
@Serializable
sealed class CharacterState {
    @Serializable object Idle : CharacterState()
    @Serializable object Moving : CharacterState()
    @Serializable object Attacking : CharacterState()
    @Serializable object Defending : CharacterState()
    @Serializable object Dead : CharacterState()
}

@Serializable
data class State(var value: CharacterState = CharacterState.Idle)
```

### 关系组件

关系组件定义实体之间的关系：

```kotlin
@Serializable
data class Parent(var entityId: Long)

@Serializable
data class Children(var entityIds: MutableList<Long> = mutableListOf())
```

## 结论

ECS 为游戏开发提供了灵活、高性能的架构。通过分离数据和逻辑，并使用组合代替继承，ECS 允许在复杂游戏系统中实现更大的灵活性和可扩展性。Sect 项目的 ECS 实现为使用 Kotlin Multiplatform 构建游戏系统提供了坚实的基础。