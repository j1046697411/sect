# Kotlin 最佳实践

## 编码风格

### 命名规范

- **类和接口**：使用 PascalCase
  ```kotlin
  class TimeSystem {
      // ...
  }
  
  interface WorldOwner {
      // ...
  }
  ```

- **函数和属性**：使用 camelCase
  ```kotlin
  fun update(deltaTime: Float) {
      // ...
  }
  
  var gameSpeed = 1.0f
  ```

- **常量**：使用 UPPER_SNAKE_CASE
  ```kotlin
  const val TICKS_PER_SECOND = 20
  const val SECONDS_PER_MINUTE = 60
  ```

- **包名**：使用小写字母，避免使用下划线
  ```kotlin
  package cn.jzl.ecs.system.game
  ```

### 代码格式化

- 使用 4 个空格进行缩进，不要使用制表符
- 每行代码不超过 120 个字符
- 花括号使用 K&R 风格（左花括号在同一行）
  ```kotlin
  fun update(deltaTime: Float) {
      // 代码块
  }
  ```

- 逗号后添加空格
  ```kotlin
  data class Position(
      var x: Float = 0.0f,
      var y: Float = 0.0f,
      var z: Float = 0.0f
  )
  ```

## 语言特性使用

### 数据类

使用数据类来表示不可变数据或简单的数据容器

```kotlin
@Serializable
data class GameTime(
    var year: Int = 1,
    var month: Int = 1,
    var day: Int = 1,
    var hour: Int = 0,
    var minute: Int = 0,
    var second: Int = 0
)
```

### 密封类

使用密封类来表示有限的状态集合

```kotlin
sealed class GameState {
    object Idle : GameState()
    object Playing : GameState()
    object Paused : GameState()
    object GameOver : GameState()
}
```

### 扩展函数

使用扩展函数来增强现有类型的功能

```kotlin
fun GameTime.format(): String {
    return "${year}年${month}月${day}日 ${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}:${second.toString().padStart(2, '0')}"
}
```

### 空安全

- 避免使用 `!!` 运算符，除非你确定值不为 null
- 使用 `?.` 安全调用运算符
- 使用 `?:` Elvis 运算符提供默认值

```kotlin
// 推荐
val name = entity?.name ?: "Unknown"

// 不推荐
val name = entity!!.name
```

### 类型推断

- 局部变量使用类型推断
- 公共 API 使用显式类型声明

```kotlin
// 局部变量 - 类型推断
val deltaTime = 1.0f / TICKS_PER_SECOND

// 公共 API - 显式类型
fun update(deltaTime: Float): Float {
    // ...
}
```

## 性能优化

### 避免不必要的对象创建

- 重用对象而不是创建新对象
- 使用对象池管理频繁创建和销毁的对象
- 避免在循环中创建对象

### 内联函数

对于小型、频繁调用的函数使用 `inline` 修饰符

```kotlin
inline fun <reified T> Entity.getComponent(): T? {
    // 实现
}
```

### 使用 `const` 代替 `val` 用于编译时常量

```kotlin
// 推荐
const val MAX_HEALTH = 100

// 不推荐（除非值在运行时计算）
val MAX_HEALTH = 100
```

### 延迟初始化

对于资源密集型对象使用 `lateinit` 或 `lazy` 初始化

```kotlin
// 延迟初始化
lateinit var world: World

// 懒加载
val gameEngine by lazy {
    GameEngine()
}
```

## 测试最佳实践

### 测试命名

使用 `should_behavior_when_condition` 格式命名测试方法

```kotlin
@Test
fun `should_update_position_based_on_velocity`() {
    // 测试实现
}

@Test
fun `should_not_update_when_paused`() {
    // 测试实现
}
```

### 测试隔离

- 每个测试方法应该测试一个特定的行为
- 测试之间不应该有依赖关系
- 使用 `@BeforeEach` 和 `@AfterEach` 来设置和清理测试环境

### 断言

使用清晰的断言消息

```kotlin
// 推荐
assertEquals(expectedPosition, actualPosition, "Position should be updated correctly")

// 不推荐
assertEquals(expectedPosition, actualPosition)
```

## 文档

### KDoc

为所有公共类、函数和属性编写 KDoc 文档

```kotlin
/**
 * 更新游戏时间
 * @param deltaTime 帧间隔时间（秒）
 * @return 更新的游戏时间增量（秒）
 */
fun update(deltaTime: Float = 1.0f / TICKS_PER_SECOND): Float {
    // 实现
}
```

### 注释

- 只在代码不清晰时添加注释
- 注释应该解释为什么，而不是是什么
- 避免过时的注释

```kotlin
// 推荐 - 解释为什么使用这个值
const val TICKS_PER_SECOND = 20 // 与大多数游戏引擎保持一致

// 不推荐 - 只是重复了代码的作用
val gameTime = GameTime() // 创建游戏时间对象
```

## 架构最佳实践

### 单一职责原则

每个类或函数应该只有一个职责

```kotlin
// 好的设计 - 每个系统负责一个特定功能
class TimeSystem(override val world: World) : WorldOwner {
    // 只负责时间管理
}

class MovementSystem(override val world: World) : WorldOwner {
    // 只负责移动
}
```

### 依赖注入

使用依赖注入来管理对象之间的依赖关系

```kotlin
class GameEngine(private val world: World) {
    private val timeSystem = TimeSystem(world)
    private val movementSystem = MovementSystem(world)
    
    // ...
}
```

### 不可变性优先

尽可能使用不可变数据，只在必要时使用可变数据

```kotlin
// 不可变数据类
@Serializable
data class Position(val x: Float, val y: Float, val z: Float)

// 可变数据类（仅在必要时使用）
@Serializable
data class MutablePosition(var x: Float, var y: Float, var z: Float)
```

## 多平台开发

### 公共代码

将共享代码放在 `commonMain` 目录下

```
src/
├── commonMain/    # 共享代码
├── jvmMain/       # JVM 特定代码
└── jsMain/        # JavaScript 特定代码
```

### 平台特定代码

使用 `expect` 和 `actual` 关键字处理平台特定实现

```kotlin
// commonMain
 expect fun platformName(): String

// jvmMain
 actual fun platformName(): String = "JVM"

// jsMain
 actual fun platformName(): String = "JavaScript"
```

### 序列化

使用 `kotlinx.serialization` 处理跨平台序列化

```kotlin
@Serializable
data class GameState(
    val time: GameTime,
    val entities: List<Entity>
)
```

## 代码审查要点

1. **可读性**：代码是否容易理解？
2. **性能**：是否有性能瓶颈？
3. **正确性**：代码是否按预期工作？
4. **安全性**：是否有潜在的安全问题？
5. **可维护性**：代码是否容易维护和扩展？
6. **一致性**：是否符合项目的编码风格？
7. **测试覆盖**：是否有足够的测试覆盖？

遵循这些最佳实践将有助于编写高质量、可维护的 Kotlin 代码，提高项目的整体质量和开发效率。