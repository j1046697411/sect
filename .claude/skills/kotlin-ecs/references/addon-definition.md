# Addon 定义指南

Addon系统是Sect项目中用于扩展ECS功能的模块化机制。通过Addon，你可以封装组件、系统和实体的初始化逻辑，实现功能的模块化和复用。

## 核心概念

### Addon

Addon是一个泛型数据类，用于定义模块化的ECS扩展。它包含三个主要属性：

- `name`: Addon的名称
- `configurationFactory`: 用于创建Addon配置的工厂函数
- `onInstall`: 安装Addon时执行的回调函数

### AddonSetup

AddonSetup用于设置Addon的生命周期回调，提供了一系列便捷方法来配置不同阶段的行为。

### Phase

Phase定义了Addon的生命周期阶段，包括：

1. `ADDONS_CONFIGURED`: 配置阶段
2. `INIT_COMPONENTS`: 初始化组件
3. `INIT_SYSTEMS`: 初始化系统
4. `INIT_ENTITIES`: 初始化实体
5. `ENABLE`: 启用阶段

## 创建Addon

### 1. 创建不带配置的Addon

```kotlin
import cn.jzl.ecs.addon.*

val myAddon = createAddon("my-addon") {
    // Addon初始化逻辑
    
    // 配置回调
    configure {
        // 配置阶段逻辑
    }
    
    // 组件初始化回调
    components {
        // 组件初始化逻辑
    }
    
    // 系统初始化回调
    systems {
        // 系统初始化逻辑
    }
    
    // 实体初始化回调
    entities {
        // 实体初始化逻辑
    }
    
    // 启动回调
    onStart {
        // 启动阶段逻辑
    }
    
    // 返回Addon实例（可选）
    MyAddonInstance()
}

// Addon实例类（可选）
data class MyAddonInstance(val name: String = "my-addon")
```

### 2. 创建带配置的Addon

```kotlin
import cn.jzl.ecs.addon.*

// 配置类
data class MyAddonConfig(val enableFeature: Boolean = true, val maxEntities: Int = 100)

val myConfigurableAddon = createAddon("my-configurable-addon", {
    // 配置工厂函数
    MyAddonConfig(maxEntities = 200)
}) {
    // Addon初始化逻辑，可访问配置
    val config = this.configuration
    
    if (config.enableFeature) {
        // 启用特性
    }
    
    // 配置回调
    configure {
        // 配置阶段逻辑
    }
    
    // 返回Addon实例
    MyAddonInstance()
}
```

## Addon生命周期回调

Addon提供了以下生命周期回调方法：

### 1. 配置阶段

```kotlin
configure {
    // 配置阶段逻辑
    // 用于设置全局配置
}
```

### 2. 组件初始化

```kotlin
components {
    // 组件初始化逻辑
    // 用于注册自定义组件
}
```

### 3. 系统初始化

```kotlin
systems {
    // 系统初始化逻辑
    // 用于创建和注册系统
}
```

### 4. 实体初始化

```kotlin
entities {
    // 实体初始化逻辑
    // 用于创建初始实体
}
```

### 5. 启动阶段

```kotlin
onStart {
    // 启动阶段逻辑
    // 用于启用功能
}
```

## 安装Addon

Addon通过WorldSetup进行安装：

```kotlin
import cn.jzl.ecs.addon.*

// 创建WorldSetup实例（通常由ECS框架提供）
val worldSetup = WorldSetup(injector, phaseTaskRegistry)

// 安装Addon
worldSetup.install(myAddon)

// 安装带配置的Addon，并覆盖默认配置
worldSetup.install(myConfigurableAddon) {
    enableFeature = false
    maxEntities = 150
}
```

## 依赖注入

Addon支持依赖注入，你可以在AddonSetup中使用inject方法来配置依赖：

```kotlin
val myAddonWithInjection = createAddon("my-addon-with-injection") {
    // 配置依赖注入
    injects {
        // 绑定依赖
        this bind singleton { MyService() }
    }
    
    // 其他初始化逻辑
}
```

## 完整示例

### 1. 定义Addon

```kotlin
import cn.jzl.ecs.addon.*
import cn.jzl.ecs.component.game.*
import cn.jzl.ecs.system.game.*

// 配置类
data class MovementAddonConfig(val enableGravity: Boolean = true)

// Addon实例类
data class MovementAddonInstance(val config: MovementAddonConfig)

// 创建Addon
val movementAddon = createAddon("movement-addon", {
    MovementAddonConfig(enableGravity = true)
}) {
    val config = this.configuration
    
    // 注册组件
    components {
        // 组件注册逻辑（如果需要）
    }
    
    // 创建系统
    systems {
        // 创建移动系统
        val movementSystem = MovementSystem(this)
        
        // 如果启用重力，创建重力系统
        if (config.enableGravity) {
            val gravitySystem = GravitySystem(this)
        }
    }
    
    // 创建初始实体
    entities {
        // 创建一个带有位置和速度组件的实体
        val entity = createEntity()
        entity.setComponent(Position(0.0f, 0.0f, 0.0f))
        entity.setComponent(Velocity(1.0f, 0.0f, 0.0f))
    }
    
    // 启动逻辑
    onStart {
        // 启动阶段逻辑
    }
    
    // 返回Addon实例
    MovementAddonInstance(config)
}
```

### 2. 安装Addon

```kotlin
import cn.jzl.ecs.addon.*

// 安装默认配置的Addon
worldSetup.install(movementAddon)

// 安装自定义配置的Addon
worldSetup.install(movementAddon) {
    enableGravity = false
}
```

## Addon最佳实践

1. **保持Addon小巧专注**: 每个Addon应专注于一个特定功能或功能集合
2. **提供合理的默认配置**: 使Addon易于使用，同时允许自定义
3. **使用清晰的命名**: Addon名称应明确反映其功能
4. **文档化Addon**: 为Addon提供清晰的文档，说明其功能和配置选项
5. **避免依赖循环**: 确保Addon之间的依赖关系清晰，避免循环依赖
6. **使用依赖注入**: 利用依赖注入简化Addon之间的协作
7. **支持禁用特性**: 允许通过配置禁用Addon的某些特性

## Addon与ECS的关系

Addon系统是ECS架构的扩展，它提供了一种模块化的方式来组织和管理ECS功能。通过Addon，你可以：

1. 封装组件、系统和实体的初始化逻辑
2. 实现功能的模块化和复用
3. 支持功能的动态启用和禁用
4. 简化大型项目的组织和管理
5. 促进团队协作和代码复用

## 常见使用场景

1. **功能模块**: 将特定功能（如移动、战斗、AI）封装为Addon
2. **插件系统**: 允许第三方开发者扩展游戏功能
3. **游戏模式**: 不同的游戏模式使用不同的Addon组合
4. **调试工具**: 创建用于调试和测试的Addon
5. **性能优化**: 根据需要启用或禁用性能密集型功能
