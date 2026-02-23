# lko-log - 日志框架

## 模块定位
轻量级日志框架，提供多级别日志输出和与 ECS 的集成。

**层级**: 服务层（依赖 lko-di, lko-ecs）

## 核心职责
- 多级别日志输出（VERBOSE, DEBUG, INFO, WARN, ERROR）
- 与 ECS World 集成
- 支持异常堆栈输出
- 可扩展的日志实现

## 目录结构
```
lko-log/src/commonMain/kotlin/cn/jzl/log/
├── Logger.kt              # 日志接口
├── ConsoleLogger.kt       # 控制台实现
└── LogLevel.kt            # 日志级别定义
```

## 关键 API

### 日志级别
```kotlin
enum class LogLevel {
    VERBOSE,  // 详细日志
    DEBUG,    // 调试日志
    INFO,     // 信息日志
    WARN,     // 警告日志
    ERROR     // 错误日志
}
```

### Logger 接口
```kotlin
interface Logger {
    fun verbose(error: Throwable? = null, block: () -> Any)
    fun debug(error: Throwable? = null, block: () -> Any)
    fun info(error: Throwable? = null, block: () -> Any)
    fun warn(error: Throwable? = null, block: () -> Any)
    fun error(error: Throwable? = null, block: () -> Any)
}
```

## 使用方式

```kotlin
// 1. 安装日志插件
world.install(logAddon)

// 2. 获取 Logger
val logger: Logger by world.di.instance()

// 3. 使用日志
logger.debug { "调试信息: ${entity.id}" }
logger.info { "实体创建完成" }
logger.warn { "资源不足" }

// 4. 带异常的日志
try {
    // ...
} catch (e: Exception) {
    logger.error(e) { "发生错误" }
}

// 5. 带名称的 Logger
val namedLogger: Logger by world.di.instance(arg = "MyComponent")
namedLogger.info { "来自 MyComponent 的日志" }
```

## 依赖关系

```kotlin
// build.gradle.kts
dependencies {
    implementation(projects.libs.lkoDi)
    implementation(projects.libs.lkoEcs)
}
```

## AI 开发指引

### 开发原则
- **轻量级**: 保持日志逻辑简洁
- **懒加载**: 使用 lambda 延迟计算日志内容
- **可配置**: 支持运行时调整日志级别

### 日志级别选择
| 级别 | 场景 | 说明 |
|------|------|------|
| VERBOSE | 详细跟踪 | 仅调试时使用 |
| DEBUG | 调试信息 | 开发阶段使用 |
| INFO | 重要信息 | 正常运行信息 |
| WARN | 警告 | 潜在问题 |
| ERROR | 错误 | 需要处理的错误 |

### 禁止事项
- ❌ 禁止在日志 lambda 中执行耗时操作
- ❌ 禁止使用 `println` 替代日志
- ❌ 禁止在核心循环中频繁打印日志

## 测试要求
- 日志级别测试
- 异常堆栈测试
- 性能测试
