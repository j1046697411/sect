# lko-di - 依赖注入框架

## 模块定位
基于 Kodein 的轻量级依赖注入框架，提供上下文感知的依赖管理和作用域控制。

**层级**: 服务层（依赖 lko-core）

## 核心职责
- 依赖注入容器管理
- 上下文感知的依赖解析
- 作用域和生命周期管理
- 依赖循环检测

## 目录结构
```
lko-di/src/commonMain/kotlin/cn/jzl/di/
├── DI.kt                  # 容器主接口
├── DIAware.kt             # 感知接口
├── DIImpl.kt              # DI 实现
├── DIContainer.kt         # 容器实现
├── DIContainerImpl.kt
├── DIBuilder.kt           # 构建器
├── DIMainBuilder.kt
├── Binding.kt             # 绑定定义
├── Bindings.kt            # 绑定类型
├── BindBuilder.kt         # 绑定构建器
├── Scope.kt               # 作用域定义
├── Scopes.kt
├── DIModule.kt            # 模块化配置
├── DIContext.kt           # 上下文
├── ContextTranslator.kt   # 上下文转换
├── DIException.kt         # 异常定义
├── DependencyLoopException.kt  # 循环依赖异常
└── ...
```

## 关键 API

### 容器创建
```kotlin
// 创建 DI 容器
val di = DI(name = "MyDI") {
    bind<SomeService>() with singleton { SomeServiceImpl() }
    bind<AnotherService>() with factory { arg: String -> AnotherServiceImpl(arg) }
}

// 模块化
val myModule = DIModule("myModule") {
    bind<Service>() with singleton { ServiceImpl() }
}
```

### 依赖注入
```kotlin
// 通过 DIAware 接口
class MyClass : DIAware {
    override val di: DI = myDI
    val service: SomeService by instance()
}

// 直接获取
val service: SomeService by di.instance()
```

### 作用域
```kotlin
// 单例
bind<Service>() with singleton { ServiceImpl() }

// 工厂（每次创建新实例）
bind<Service>() with factory { ServiceImpl() }

// 带参数
bind<Service>() with factory { arg: Config -> ServiceImpl(arg) }

// 原型
bind<Service>() with prototype { ServiceImpl() }
```

## 使用方式

```kotlin
// 1. 定义服务
interface Logger {
    fun log(message: String)
}

class ConsoleLogger : Logger {
    override fun log(message: String) = println(message)
}

// 2. 创建 DI 容器
val appDI = DI {
    bind<Logger>() with singleton { ConsoleLogger() }
}

// 3. 使用依赖
class UserService(override val di: DI) : DIAware {
    private val logger: Logger by instance()
    
    fun doSomething() {
        logger.log("Doing something")
    }
}

// 4. 带参数的工厂
val di = DI {
    bind<Database>() with factory { config: DbConfig -> 
        Database.connect(config) 
    }
}

val db: Database by di.instance(arg = DbConfig("localhost"))
```

## 依赖关系

```kotlin
// build.gradle.kts
dependencies {
    implementation(projects.libs.lkoCore)
    implementation(libs.kodein.kaverit)  // 类型 token 支持
}
```

## AI 开发指引

### 开发原则
- **轻量化**: 保持 DI 逻辑简洁，避免过度封装
- **作用域管理**: 严格控制单例与工厂模式的使用场景
- **错误处理**: 依赖循环必须能被检测并清晰报错

### 绑定类型选择
| 场景 | 绑定类型 | 说明 |
|------|----------|------|
| 无状态服务 | `singleton` | 全局唯一实例 |
| 有状态服务 | `factory` | 每次创建新实例 |
| 需要配置 | `factory` + 参数 | 通过参数传递配置 |
| 原型模式 | `prototype` | 每次创建新实例 |

### 禁止事项
- ❌ 禁止循环依赖
- ❌ 禁止在 singleton 中持有可变状态
- ❌ 禁止过度使用 DI（简单场景直接创建）

## 测试要求
- 依赖解析测试
- 循环依赖检测测试
- 作用域生命周期测试
- 多上下文测试
