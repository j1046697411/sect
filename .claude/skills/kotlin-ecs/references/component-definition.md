# 组件定义指南

组件是ECS架构中的核心数据容器，定义了实体的属性和状态。本指南提供了在Sect项目中定义和使用组件的详细说明。

## 组件设计原则

在Sect项目中，组件设计应遵循以下原则：

1. **仅包含数据**: 组件应仅包含数据，不包含任何业务逻辑
2. **可序列化**: 使用`@Serializable`标记所有组件以支持游戏状态的保存和加载
3. **默认值**: 为所有属性提供合理的默认值，便于组件初始化
4. **小巧专注**: 每个组件应代表一个单一关注点，避免大型复合组件
5. **不可变性考虑**: 对于只读属性，考虑使用val而不是var
6. **清晰命名**: 组件名称应明确反映其包含的数据或功能
7. **类型选择**: 根据属性数量选择合适的组件类型：
   - 多属性：使用`data class`
   - 单属性：使用`value class`（推荐）
   - 无属性标签：使用`sealed interface`或`sealed class`

## 组件定义规则

### 1. 多属性组件 - 使用 data class

当组件需要存储多个相关属性时，使用`data class`定义：

```kotlin
@Serializable
data class Position(
    var x: Float = 0.0f,
    var y: Float = 0.0f,
    var z: Float = 0.0f
)

@Serializable
data class Health(
    var current: Int = 100,
    var max: Int = 100
)
```

### 2. 单属性组件 - 使用 value class

当组件只需要存储一个属性时，推荐使用`value class`定义，它提供了更好的性能和类型安全性：

```kotlin
@JvmInline
@Serializable
value class Speed(val value: Float = 0.0f)

@JvmInline
@Serializable
value class Damage(val value: Int = 0)

@JvmInline
@Serializable
value class PlayerId(val value: Long)
```

### 3. 标签组件 - 使用 sealed interface 或 sealed class

当组件作为标签使用（无属性）时，推荐使用`sealed interface`或`sealed class`定义，它们提供了更好的类型安全性和扩展性：

```kotlin
// 使用 sealed interface 定义标签（推荐，更轻量）
@Serializable
sealed interface EntityTag

@Serializable
object PlayerTag : EntityTag

@Serializable
object EnemyTag : EntityTag

@Serializable
object ProjectileTag : EntityTag

// 使用 sealed class 定义标签（适合需要额外功能的情况）
@Serializable
sealed class CharacterType

@Serializable
object Human : CharacterType()

@Serializable
object Monster : CharacterType()

@Serializable
object Spirit : CharacterType()
```

### 4. 状态组件 - 使用 sealed class

使用`sealed class`定义实体的不同状态，提供类型安全的状态管理：

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

### 5. 关系组件 - 使用 data class

定义实体之间的关系时，使用`data class`：

```kotlin
@Serializable
data class Parent(val entityId: Long)

@Serializable
@JvmInline
value class Child(val entityId: Long)

@Serializable
data class Children(val values: MutableList<Child> = mutableListOf())

@Serializable
@JvmInline
value class Target(val entityId: Long? = null)
```

### 6. 配置组件 - 使用 data class

用于存储系统或实体的配置数据：

```kotlin
@Serializable
data class MovementConfig(
    val maxSpeed: Float = 5.0f,
    val acceleration: Float = 2.0f,
    val deceleration: Float = 1.5f
)

@Serializable
data class CombatConfig(
    val criticalChance: Float = 0.1f,
    val criticalMultiplier: Float = 2.0f
)
```

## 组件定义规范

1. **文件结构**: 每个组件应放在单独的文件中，文件名与组件名一致
2. **包命名**: 核心组件放在`cn.jzl.ecs.component`包下，游戏特定组件放在`cn.jzl.ecs.component.game`包下
3. **继承**: 组件不应继承自其他组件，而应通过组合使用
4. **类型安全**: 优先使用Kotlin的类型系统，如枚举类、密封类和数据类
5. **注释**: 为复杂组件和属性添加KDoc注释

## 组件使用最佳实践

1. **组件组合**: 通过组合多个小型组件来定义实体行为，而非创建大型单一组件
2. **组件复用**: 设计可复用的组件，避免为不同实体创建相似的组件
3. **组件命名一致性**: 保持组件名称和属性名称的一致性，便于理解和维护
4. **组件生命周期**: 考虑组件的生命周期，及时添加和移除组件
5. **避免组件膨胀**: 定期审查组件，移除不再使用的属性或组件
6. **使用组件查询**: 利用ECS查询系统高效地获取具有特定组件组合的实体

## 组件示例

### 1. 多属性组件示例 - 使用 data class

```kotlin
package cn.jzl.ecs.component.game

import kotlinx.serialization.Serializable

/**
 * 角色基础属性组件
 * @property realm 修为境界（炼气、筑基、金丹、元婴、化神、渡劫、成仙）
 * @property realmStage 境界层次（初期、中期、后期、巅峰）
 * @property age 年龄
 * @property gender 性别（男/女）
 * @property qiBlood 气血值
 * @property spiritPower 灵力值
 * @property cultivationProgress 当前境界的修炼进度（0-100）
 */
@Serializable
data class BaseAttributes(
    var realm: String = "炼气",
    var realmStage: String = "初期",
    var age: Int = 18,
    var gender: String = "男",
    var qiBlood: Int = 100,
    var spiritPower: Int = 100,
    var cultivationProgress: Float = 0f
)

/**
 * 位置组件
 * @property x X坐标
 * @property y Y坐标
 * @property z Z坐标
 */
@Serializable
data class Position(
    var x: Float = 0.0f,
    var y: Float = 0.0f,
    var z: Float = 0.0f
)

/**
 * 资源组件
 * @property type 资源类型（灵石、药材、矿石、灵草等）
 * @property amount 当前数量
 * @property maxCapacity 最大容量
 * @property growthRate 资源增长率
 */
@Serializable
data class ResourceComponent(
    var type: String,
    var amount: Int = 0,
    var maxCapacity: Int = 1000,
    var growthRate: Float = 0.0f
)
```

### 2. 单属性组件示例 - 使用 value class

```kotlin
package cn.jzl.ecs.component.game

import kotlinx.serialization.Serializable

/**
 * 速度组件
 * @property value 速度值
 */
@JvmInline
@Serializable
value class Speed(val value: Float = 0.0f)

/**
 * 伤害组件
 * @property value 伤害值
 */
@JvmInline
@Serializable
value class Damage(val value: Int = 0)

/**
 * 攻击力组件
 * @property value 攻击力值
 */
@JvmInline
@Serializable
value class AttackPower(val value: Int = 10)

/**
 * 防御力组件
 * @property value 防御力值
 */
@JvmInline
@Serializable
value class Defense(val value: Int = 10)

/**
 * 灵力值组件
 * @property value 灵力值
 */
@JvmInline
@Serializable
value class SpiritPower(val value: Int = 100)
```

### 3. 标签组件示例 - 使用 sealed interface

```kotlin
package cn.jzl.ecs.component.game

import kotlinx.serialization.Serializable

/**
 * 实体标签接口
 */
@Serializable
sealed interface EntityTag

/**
 * 玩家标签
 */
@Serializable
object PlayerTag : EntityTag

/**
 * 敌人标签
 */
@Serializable
object EnemyTag : EntityTag

/**
 * 投射物标签
 */
@Serializable
object ProjectileTag : EntityTag

/**
 * 场景对象标签
 */
@Serializable
object SceneObjectTag : EntityTag

/**
 * 角色类型标签接口
 */
@Serializable
sealed interface CharacterType

/**
 * 人类角色
 */
@Serializable
object HumanType : CharacterType

/**
 * 妖兽角色
 */
@Serializable
object MonsterType : CharacterType

/**
 * 灵物角色
 */
@Serializable
object SpiritType : CharacterType
```

### 4. 状态组件示例 - 使用 sealed class

```kotlin
package cn.jzl.ecs.component.game

import kotlinx.serialization.Serializable

/**
 * 角色状态密封类
 */
@Serializable
sealed class CharacterState {
    /** 空闲状态 */
    @Serializable object Idle : CharacterState()
    /** 移动状态 */
    @Serializable object Moving : CharacterState()
    /** 攻击状态 */
    @Serializable object Attacking : CharacterState()
    /** 防御状态 */
    @Serializable object Defending : CharacterState()
    /** 死亡状态 */
    @Serializable object Dead : CharacterState()
    /** 修炼状态 */
    @Serializable object Cultivating : CharacterState()
}

/**
 * 角色状态组件
 * @property value 当前角色状态
 */
@Serializable
data class State(var value: CharacterState = CharacterState.Idle)

/**
 * 事件状态密封类
 */
@Serializable
sealed class EventStatus {
    /** 未处理状态 */
    @Serializable object Pending : EventStatus()
    /** 处理中状态 */
    @Serializable object Processing : EventStatus()
    /** 已处理状态 */
    @Serializable object Completed : EventStatus()
    /** 已取消状态 */
    @Serializable object Cancelled : EventStatus()
}
```

### 5. 关系组件示例 - 混合使用 data class 和 value class

```kotlin
package cn.jzl.ecs.component.game

import kotlinx.serialization.Serializable

/**
 * 父实体组件
 * @property value 父实体ID
 */
@JvmInline
@Serializable
value class Parent(val value: Long)

/**
 * 子实体组件
 * @property value 子实体ID
 */
@JvmInline
@Serializable
value class Child(val value: Long)

/**
 * 子实体列表组件
 * @property values 子实体ID列表
 */
@Serializable
data class Children(val values: MutableList<Child> = mutableListOf())

/**
 * 目标实体组件
 * @property value 目标实体ID
 */
@JvmInline
@Serializable
value class Target(val value: Long? = null)

/**
 * 所有者组件
 * @property value 所有者实体ID
 */
@JvmInline
@Serializable
value class Owner(val value: Long)
```

### 6. 配置组件示例 - 使用 data class

```kotlin
package cn.jzl.ecs.component.game

import kotlinx.serialization.Serializable

/**
 * 移动配置组件
 * @property maxSpeed 最大速度
 * @property acceleration 加速度
 * @property deceleration 减速度
 */
@Serializable
data class MovementConfig(
    val maxSpeed: Float = 5.0f,
    val acceleration: Float = 2.0f,
    val deceleration: Float = 1.5f
)

/**
 * 战斗配置组件
 * @property criticalChance 暴击率
 * @property criticalMultiplier 暴击倍率
 */
@Serializable
data class CombatConfig(
    val criticalChance: Float = 0.1f,
    val criticalMultiplier: Float = 2.0f
)
```

## 组件与系统的交互

组件通过系统进行操作和更新。系统使用ECS查询来获取具有特定组件组合的实体，然后对这些实体的组件进行操作。

### 组件查询示例

#### 1. 查询多属性组件

```kotlin
// 查询具有 Position 和 BaseAttributes 组件的实体
val query = world.createQuery {
    all(Position::class, BaseAttributes::class)
}

// 获取匹配的实体
val entities = query.entities
```

#### 2. 查询包含 value class 组件的实体

```kotlin
// 查询具有 Position 和 Speed 组件的实体
val query = world.createQuery {
    all(Position::class, Speed::class)
}

// 获取匹配的实体
val entities = query.entities
```

#### 3. 查询包含 sealed interface 标签的实体

```kotlin
// 查询所有带有 PlayerTag 的实体
val query = world.createQuery {
    all(PlayerTag::class)
}

// 获取匹配的实体
val entities = query.entities
```

#### 4. 复杂查询示例

```kotlin
// 查询具有 Position 和 Speed 组件，且是玩家或敌人的实体
val query = world.createQuery {
    all(Position::class, Speed::class)
    any(PlayerTag::class, EnemyTag::class)
    none(Dead::class)
}

// 获取匹配的实体
val entities = query.entities
```

### 组件更新示例

#### 1. 更新多属性组件

```kotlin
// 在系统中更新位置组件
fun update(deltaTime: Float) {
    val entities = world.getFamily(Position::class, Speed::class)
    
    for (entity in entities) {
        val position = entity.getComponent(Position::class)
        val speed = entity.getComponent(Speed::class)
        
        if (position != null && speed != null) {
            position.x += speed.value * deltaTime
            position.y += speed.value * deltaTime
        }
    }
}
```

#### 2. 更新 value class 组件

```kotlin
// 在系统中更新速度组件
fun updateSpeed(entity: Entity, newSpeed: Float) {
    // 更新速度组件
    entity.setComponent(Speed(newSpeed))
    
    // 或者获取现有组件并更新
    val currentSpeed = entity.getComponent(Speed::class)
    if (currentSpeed != null) {
        // 注意：value class 是不可变的，需要重新创建实例
        val updatedSpeed = Speed(currentSpeed.value * 1.5f) // 增加50%速度
        entity.setComponent(updatedSpeed)
    }
}
```

#### 3. 处理 sealed class 状态组件

```kotlin
// 在系统中处理角色状态
fun updateCharacterState(entity: Entity, newState: CharacterState) {
    val stateComponent = entity.getComponent(State::class)
    if (stateComponent != null) {
        stateComponent.value = newState
        
        // 根据状态执行不同的逻辑
        when (newState) {
            is CharacterState.Cultivating -> {
                // 处理修炼逻辑
                val attributes = entity.getComponent(BaseAttributes::class)
                if (attributes != null) {
                    attributes.cultivationProgress += 0.1f
                }
            }
            is CharacterState.Attacking -> {
                // 处理攻击逻辑
                val damage = entity.getComponent(Damage::class)
                // 应用伤害...
            }
            // 处理其他状态...
        }
    }
}
```

#### 4. 使用标签组件进行逻辑分支

```kotlin
// 根据标签执行不同的逻辑
fun processEntity(entity: Entity) {
    if (entity.hasComponent(PlayerTag::class)) {
        // 处理玩家实体
        processPlayer(entity)
    } else if (entity.hasComponent(EnemyTag::class)) {
        // 处理敌人实体
        processEnemy(entity)
    } else if (entity.hasComponent(ProjectileTag::class)) {
        // 处理投射物实体
        processProjectile(entity)
    }
}
```

## 组件生命周期

### 添加组件

```kotlin
val entity = world.createEntity()
entity.setComponent(Position(0.0f, 0.0f, 0.0f))
entity.setComponent(Velocity(1.0f, 0.0f, 0.0f))
```

### 获取组件

```kotlin
val position = entity.getComponent(Position::class)
if (position != null) {
    // 使用位置组件
}
```

### 更新组件

```kotlin
val position = entity.getComponent(Position::class)
if (position != null) {
    position.x += 1.0f
    position.y += 1.0f
}
```

### 移除组件

```kotlin
entity.removeComponent(Position::class)
```

## 组件序列化

所有组件必须使用`@Serializable`注解标记，以支持游戏状态的保存和加载。

```kotlin
@file:kotlinx.serialization.ExperimentalSerializationApi

package cn.jzl.ecs.component.game

import kotlinx.serialization.Serializable

@Serializable
data class Position(
    var x: Float = 0.0f,
    var y: Float = 0.0f,
    var z: Float = 0.0f
)
```

## 组件测试

组件测试应重点关注：

1. **序列化/反序列化**: 确保组件可以正确地序列化和反序列化
2. **默认值**: 确保组件的默认值设置正确
3. **数据一致性**: 确保组件的数据在各种操作下保持一致

### 组件测试示例

```kotlin
@Test
fun `should_serialize_and_deserialize_position_component`() {
    // 创建组件实例
    val original = Position(1.0f, 2.0f, 3.0f)
    
    // 序列化
    val json = Json.encodeToString(original)
    
    // 反序列化
    val deserialized = Json.decodeFromString<Position>(json)
    
    // 验证结果
    assertEquals(original, deserialized)
}

@Test
fun `should_use_default_values_when_not_specified`() {
    val position = Position()
    
    assertEquals(0.0f, position.x)
    assertEquals(0.0f, position.y)
    assertEquals(0.0f, position.z)
}
```
