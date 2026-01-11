# Kotlin ECS 任务示例

## 任务：创建一个移动系统

### 任务描述
在Sect项目中创建一个移动系统，该系统负责根据实体的速度组件更新其位置组件。

### 技术要求
- 使用Kotlin语言
- 遵循项目的ECS架构
- 符合项目的代码风格指南
- 支持游戏速度调节

### 实现步骤

1. **定义组件**
   - 确保`Position`和`Velocity`组件已经存在
   - 如果不存在，创建这两个组件

2. **创建移动系统**
   - 创建`MovementSystem`类，实现`WorldOwner`接口
   - 添加`update`方法，接收`deltaTime`参数
   - 在`update`方法中实现移动逻辑

3. **实现移动逻辑**
   - 获取所有具有`Position`和`Velocity`组件的实体
   - 遍历这些实体，根据速度更新位置
   - 考虑游戏速度因素

4. **测试系统**
   - 编写单元测试验证移动系统的正确性
   - 确保系统在不同游戏速度下都能正常工作

### 代码示例

**组件定义**：
```kotlin
// 如果Position和Velocity组件不存在，添加到component目录
@Serializable
data class Position(
    var x: Float = 0.0f,
    var y: Float = 0.0f
)

@Serializable
data class Velocity(
    var dx: Float = 0.0f,
    var dy: Float = 0.0f
)
```

**移动系统实现**：
```kotlin
package cn.jzl.ecs.system.game

import cn.jzl.ecs.World
import cn.jzl.ecs.WorldOwner
import cn.jzl.ecs.component.Position
import cn.jzl.ecs.component.Velocity

/**
 * 移动系统
 * 负责根据速度组件更新实体的位置
 */
class MovementSystem(override val world: World) : WorldOwner {
    /**
     * 更新实体位置
     * @param deltaTime 帧间隔时间（秒）
     */
    fun update(deltaTime: Float) {
        // 获取所有具有Position和Velocity组件的实体
        val entities = world.getFamily(Position::class, Velocity::class)
        
        // 遍历实体并更新位置
        for (entity in entities) {
            val position = entity.getComponent(Position::class)
            val velocity = entity.getComponent(Velocity::class)
            
            // 根据速度和时间更新位置
            position.x += velocity.dx * deltaTime
            position.y += velocity.dy * deltaTime
        }
    }
}
```

**测试代码**：
```kotlin
package cn.jzl.ecs.system.game

import cn.jzl.ecs.World
import cn.jzl.ecs.component.Position
import cn.jzl.ecs.component.Velocity
import kotlin.test.Test
import kotlin.test.assertEquals

class MovementSystemTest {
    @Test
    fun `should update position based on velocity`() {
        // 创建世界和系统
        val world = World()
        val movementSystem = MovementSystem(world)
        
        // 创建实体并添加组件
        val entity = world.createEntity()
        entity.setComponent(Position(0.0f, 0.0f))
        entity.setComponent(Velocity(1.0f, 2.0f))
        
        // 调用更新方法
        val deltaTime = 1.0f
        movementSystem.update(deltaTime)
        
        // 验证位置更新是否正确
        val position = entity.getComponent(Position::class)
        assertEquals(1.0f, position.x)
        assertEquals(2.0f, position.y)
    }
}
```

### 验收标准

1. 移动系统能够正确根据速度更新实体位置
2. 系统能够处理多个实体
3. 系统符合项目的代码风格
4. 单元测试通过
5. 系统能够与其他系统正确集成

### 集成说明

将移动系统添加到游戏主循环中，与其他系统一起更新：

```kotlin
class GameEngine {
    private val world = World()
    private val timeSystem = TimeSystem(world)
    private val movementSystem = MovementSystem(world)
    
    fun update(deltaTime: Float) {
        // 更新时间系统
        val gameDeltaTime = timeSystem.update(deltaTime)
        
        // 更新移动系统
        movementSystem.update(gameDeltaTime)
        
        // 更新其他系统
        // ...
    }
}
```

## 扩展任务

1. 添加碰撞检测功能
2. 实现平滑移动效果
3. 添加移动速度限制
4. 支持不同类型的移动（如重力、加速度）