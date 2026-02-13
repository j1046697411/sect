# 前端结构 - 应用层

**最后更新**: 2026-02-14
**入口点**: composeApp/src/commonMain/kotlin/cn/jzl/sect/App.kt

## 应用架构

### 多平台支持

```
composeApp/
├── src/
│   ├── jvmMain/kotlin/      # JVM 桌面应用
│   ├── androidMain/kotlin/  # Android 应用
│   ├── jsMain/kotlin/       # JavaScript (Web)
│   ├── wasmJsMain/kotlin/   # WebAssembly
│   └── commonMain/kotlin/   # 共享代码
```

### 平台入口

| 平台 | 入口文件 | 说明 |
|------|----------|------|
| JVM | main.kt | Kotlin/Swing 或 JavaFX |
| Android | MainActivity.kt | Android Activity |
| JS | Platform.js.kt | Kotlin/JS 浏览器 |
| WasmJS | Platform.wasmJs.kt | WebAssembly |

## 核心组件

### 应用主文件

| 文件 | 用途 |
|------|------|
| App.kt | 共享应用逻辑 |
| Platform.kt | 平台接口定义 |
| Greeting.kt | 问候组件 |

### 平台特定实现

| 平台 | 实现 |
|------|------|
| JVM | Platform.jvm.kt |
| Android | Platform.android.kt |
| JS | Platform.js.kt |
| WasmJS | Platform.wasmJs.kt |

## Android 应用

```
androidApp/
├── src/main/
│   ├── kotlin/
│   │   └── cn/jzl/sect/
│   │       ├── MainActivity.kt
│   │       └── Platform.android.kt
│   └── java/
│       └── cn/jzl/sect/
│           └── MainActivity.kt
```

### MainActivity.kt

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SectGameApp()
        }
    }
}
```

## 共享代码结构

### composeApp/src/commonMain/kotlin/cn/jzl/sect/

```
sect/
├── App.kt           # 应用入口
├── Platform.kt      # 平台抽象
└── Greeting.kt     # UI 组件
```

### Platform.kt 抽象接口

```kotlin
expect class Platform() {
    val name: String
}

fun greet(): String = "Hello, ${Platform().name}"
```

### 平台实现示例

```kotlin
// JVM
actual class Platform actual constructor() {
    actual val name = "JVM"
}

// Android
actual class Platform actual constructor() {
    actual val name = "Android"
}
```

## 数据流

```
用户交互
    ↓
Compose UI
    ↓
游戏状态 (ECS World)
    ↓
渲染 (Compose/Canvas)
```

## UI 框架

| 平台 | 框架 |
|------|------|
| Android | Jetpack Compose |
| JVM | Compose Desktop |
| Web | Compose HTML |

## 相关文档

- [整体架构](architecture.md)
- [页面布局设计](../docs/pages/)
- [游戏需求文档](../docs/文字游戏需求文档_GRD.md)
