---
name: kotlin-ecs
description: 用于Sect项目编写Kotlin ECS（实体组件系统）代码的指南。该技能提供了关于项目ECS架构、代码风格和最佳实践的专业知识。当用户请求为Sect项目编写或修改Kotlin ECS代码时使用，包括组件、系统、实体和查询。
license: 完整条款见LICENSE.txt
---

# Kotlin ECS 技能

本技能为Sect项目编写Kotlin ECS（实体组件系统）代码提供指导。

## 关于项目

Sect项目是一个使用ECS（实体组件系统）架构进行游戏开发的Kotlin Multiplatform项目。核心ECS实现位于`lko-ecs`模块中。

## 项目结构

### 核心ECS模块
```
libs/lko-ecs/
├── src/
│   ├── commonMain/kotlin/cn/jzl/ecs/
│   │   ├── addon/          # ECS扩展功能
│   │   ├── archetype/      # 原型系统
│   │   ├── component/      # 组件定义
│   │   │   └── game/       # 游戏特定组件
│   │   ├── entity/         # 实体管理
│   │   ├── family/         # 实体家族
│   │   ├── observer/       # 观察者模式
│   │   ├── query/          # 查询系统
│   │   ├── relation/       # 实体关系
│   │   └── system/         # 系统定义
│   │       └── game/       # 游戏特定系统
│   └── commonTest/         # 测试代码
```

### 核心概念

1. **Entity（实体）**: 唯一标识符，代表游戏中的一个对象
2. **Component（组件）**: 数据容器，存储实体的属性
3. **System（系统）**: 处理逻辑，操作具有特定组件的实体
4. **Archetype（原型）**: 相同组件组合的实体集合
5. **Query（查询）**: 查询符合特定条件的实体
6. **Family（家族）**: 管理具有相同组件的实体

## 代码风格指南

### 命名约定

- **类名**: PascalCase（例如：`TimeSystem`、`GameTime`）
- **函数名**: camelCase（例如：`update`、`getCurrentSeason`）
- **属性名**: camelCase（例如：`gameTime`、`gameSpeed`）
- **常量**: UPPER_SNAKE_CASE（例如：`TICKS_PER_SECOND`、`SECONDS_PER_MINUTE`）
- **包名**: 小写加圆点（例如：`cn.jzl.ecs.system.game`）

### 代码格式化

- 使用4个空格进行缩进
- 每行限制120个字符
- 公共API使用显式类型声明
- 类型明显时，局部变量使用隐式类型声明
- 所有公共类、函数和属性使用KDoc文档

### 组件设计

组件是ECS架构中的核心数据容器，定义了实体的属性和状态。组件设计应遵循"仅包含数据、可序列化、小巧专注"等原则。

**详细组件定义指南**: 请参考 [component-definition.md](references/component-definition.md) 获取完整的组件设计原则、类型、规范、最佳实践和示例。

组件定义指南包含以下内容：
- 组件设计原则和最佳实践
- 5种组件类型（数据组件、标签组件、状态组件、关系组件、配置组件）
- 组件定义规范和命名约定
- 丰富的组件示例（基础属性、事件、资源、技能属性等）
- 组件生命周期管理
- 组件序列化和测试指南

### 组件使用示例

```kotlin
// 创建实体并添加组件
val entity = world.createEntity()

// 添加多属性组件（使用data class）
entity.setComponent(Position(0.0f, 0.0f, 0.0f))
entity.setComponent(BaseAttributes())

// 添加单属性组件（使用value class）
entity.setComponent(Speed(5.0f))
entity.setComponent(Damage(10))

// 添加标签组件（使用sealed interface）
entity.setComponent(PlayerTag)

// 定义查询上下文
class MovementQueryContext(world: World) : EntityQueryContext(world) {
    val position by component<Position>()
    val velocity by component<Velocity>()
}

// 查询具有特定组件的实体
val query = world.query { MovementQueryContext(world) }

// 遍历查询结果
query.forEach { context ->
    // 使用组件访问器获取组件
    val position = context.position
    val velocity = context.velocity
    
    // 更新位置
    position.x += velocity.dx * deltaTime
    position.y += velocity.dy * deltaTime
}
```

### 系统设计

1. **单一职责**: 每个系统应处理一个特定方面
2. **WorldOwner**: 系统应实现`WorldOwner`接口
3. **Update方法**: 提供带有deltaTime参数的`update`方法
4. **依赖关系**: 系统应独立或具有清晰的依赖关系

**系统示例**:
```kotlin
class MovementSystem(override val world: World) : WorldOwner {
    fun update(deltaTime: Float) {
        // 系统逻辑在这里
    }
}
```

### Addon系统

Addon系统是Sect项目中用于扩展ECS功能的模块化机制。通过Addon，你可以封装组件、系统和实体的初始化逻辑，实现功能的模块化和复用。

**详细Addon定义指南**: 请参考 [addon-definition.md](references/addon-definition.md) 获取完整的Addon创建、配置和使用指南。

Addon系统的主要特点：
- 支持配置化的Addon创建
- 提供生命周期回调
- 支持依赖注入
- 模块化设计，便于扩展和复用

### 事件系统

事件系统是Sect项目中用于处理实体组件变化的机制。通过事件系统，你可以观察实体组件的变化并执行相应的逻辑。

**详细事件定义指南**: 请参考 [event-definition.md](references/event-definition.md) 获取完整的事件定义、观察和触发指南。

事件系统的主要特点：
- 支持观察实体组件的变化
- 支持带数据和不带数据的事件
- 支持观察特定实体的事件
- 支持在系统和Addon中集成使用

### 查询系统

查询系统是Sect项目中用于查询ECS世界中实体的机制。通过查询系统，你可以根据组件组合筛选实体，并对匹配的实体执行操作。

**详细查询定义指南**: 请参考 [query-definition.md](references/query-definition.md) 获取完整的查询定义和使用指南。

查询系统的主要特点：
- 支持根据组件组合筛选实体
- 支持定义组件访问器，方便访问实体组件
- 支持自定义查询条件
- 自动缓存查询结果，提高性能

### 测试

1. **单元测试**: 隔离测试单个组件和系统
2. **集成测试**: 测试系统之间的交互
3. **通用测试源集**: 将测试放在`commonTest`中以支持多平台
4. **测试命名**: 使用`should_behavior_when_condition`格式

## 常见模式

### 时间管理

项目使用`TimeSystem`管理游戏时间，具有以下功能：
- 可配置的游戏速度
- 季节变化
- 角色成长周期
- 暂停功能

### 组件组织

- 核心组件直接放在`component/`目录下
- 游戏特定组件放在`component/game/`目录下
- 相关组件分组在一起

### 系统组织

- 核心系统直接放在`system/`目录下
- 游戏特定系统放在`system/game/`目录下
- 系统应以其主要职责命名

## 最佳实践

1. **组合优于继承**: 使用组件组合实体行为
2. **数据局部性**: 将相关组件分组以提高性能
3. **避免全局状态**: 使用ECS世界进行状态管理
4. **高效使用查询**: 尽可能缓存查询
5. **批量操作**: 批量处理实体以提高性能
6. **保持系统简单**: 将复杂系统拆分为更小、更专注的系统
7. **文档化公共API**: 使用KDoc文档化所有公共类和函数
8. **使用Kotlin特性**: 利用Kotlin的数据类、密封类和扩展函数等特性

## 工具和库

- **Kotlin Multiplatform**: 用于跨平台支持
- **kotlinx.serialization**: 用于组件序列化
- **kotlinx.coroutines**: 用于异步操作（如有需要）

## 示例工作流

1. **识别需求**: 确定需要实现的功能
2. **设计组件**: 定义所需的数据组件
3. **实现组件**: 创建可序列化的数据类
4. **设计系统**: 定义系统逻辑
5. **实现系统**: 编写带有update方法的系统类
6. **测试**: 编写单元测试和集成测试
7. **集成**: 将系统添加到ECS世界

## 参考资料

- **ECS架构**: 详见references/ecs-architecture.md获取详细的ECS概念
- **Kotlin最佳实践**: 详见references/kotlin-best-practices.md获取Kotlin编码指南
- **项目特定要求**: 详见references/project-specifics.md获取项目特定要求
- **组件定义**: 详见references/component-definition.md获取详细的组件定义指南
- **Addon定义**: 详见references/addon-definition.md获取详细的Addon创建和使用指南
- **事件定义**: 详见references/event-definition.md获取详细的事件定义、观察和触发指南
- **查询定义**: 详见references/query-definition.md获取完整的查询定义和使用指南
