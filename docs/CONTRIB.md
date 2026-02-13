# 贡献指南

感谢您对宗门修真录项目的关注！本指南将帮助您了解项目的开发工作流程。

## 项目概述

宗门修真录是一款基于 Kotlin/Gradle 的修真题材游戏，采用 Compose Multiplatform + ECS 架构开发。

## 项目结构

| 模块 | 路径 | 说明 |
|------|------|------|
| composeApp | `composeApp/` | 应用主模块（桌面/Android/Web/WASM） |
| androidApp | `androidApp/` | Android 原生应用 |
| libs/lko-ecs | `libs/lko-ecs/` | ECS 框架核心 |
| libs/lko-ecs-serialization | `libs/lko-ecs-serialization/` | ECS 序列化支持 |
| libs/lko-di | `libs/lko-di/` | 依赖注入 |
| libs/lko-core | `libs/lko-core/` | 核心工具库 |
| business-modules/* | `business-modules/` | 游戏业务模块 |
| benchmarks/lko-ecs-benchmarks | `benchmarks/lko-ecs-benchmarks/` | 性能基准测试 |

## 开发环境要求

- **JDK**: 17+
- **Gradle**: 8.4+ (使用 Gradle Wrapper)
- **Kotlin**: 2.3.0
- **Android SDK** (可选): 用于 Android 构建 (compileSdk 36)

## 环境设置

### 1. 克隆项目

```bash
git clone https://github.com/your-repo/sect.git
cd sect
```

### 2. 启动开发环境

```bash
# 首次构建（下载依赖）
./gradlew build

# 运行桌面版 Demo
./gradlew :composeApp:run

# 开启热重载开发
./gradlew :composeApp:hotReloadJvmDev
```

### 3. Android 开发

```bash
# 设置 ANDROID_HOME 环境变量
export ANDROID_HOME=/path/to/android/sdk

# 构建 Debug APK
./gradlew :composeApp:assembleDebug
```

## 可用脚本

以下是项目中常用的 Gradle 任务（单一事实来源：AGENTS.md + build.gradle.kts）：

### 构建任务

| 命令 | 说明 |
|------|------|
| `./gradlew build` | 构建整个项目 |
| `./gradlew assemble` | 组装所有输出 |
| `./gradlew clean` | 清理构建目录 |

### 运行任务

| 命令 | 说明 |
|------|------|
| `./gradlew :composeApp:run` | 运行桌面版 Demo (JVM) |
| `./gradlew :composeApp:hotReloadJvmDev` | 热重载开发模式 |
| `./gradlew :androidApp:installDebug` | 安装 Debug APK 到设备 |

### 测试任务

| 命令 | 说明 |
|------|------|
| `./gradlew test` | 运行所有测试 |
| `./gradlew :libs:lko-ecs:test` | 运行 ECS 模块测试 |
| `./gradlew :libs:lko-ecs:test --tests "cn.jzl.ecs.WorldTest"` | 运行特定测试类 |
| `./gradlew :libs:lko-ecs:test --tests "cn.jzl.ecs.WorldTest.testName"` | 运行特定测试方法 |
| `./gradlew :libs:lko-ecs:test --continuous` | 持续测试模式 |
| `./gradlew cleanAllTests` | 清理所有测试结果 |

### 代码覆盖率任务

| 命令 | 说明 |
|------|------|
| `./gradlew allCoverage` | 所有库模块测试并生成覆盖率报告 |
| `./gradlew ecsCoverage` | ECS 模块测试并生成覆盖率报告 |
| `./gradlew :libs:lko-ecs:koverHtmlReportJvm` | 生成 ECS HTML 覆盖率报告 |
| `open libs/lko-ecs/build/reports/kover/htmlJvm/index.html` | 查看覆盖率报告 |

### 代码检查任务

| 命令 | 说明 |
|------|------|
| `./gradlew :composeApp:lint` | 运行 lint 检查 |
| `./gradlew :composeApp:lintFix` | 运行 lint 并自动修复 |
| `./gradlew check` | 运行所有检查 |

### 基准测试任务

| 命令 | 说明 |
|------|------|
| `./gradlew benchmark` | 运行所有基准测试 |
| `./gradlew :benchmarks:lko-ecs-benchmarks:mainBenchmark` | 运行 ECS 基准测试 |

### 发布任务

| 命令 | 说明 |
|------|------|
| `./gradlew :composeApp:packageDmg` | 打包 macOS 应用 |
| `./gradlew :composeApp:packageMsi` | 打包 Windows MSI |
| `./gradlew :composeApp:packageDeb` | 打包 Linux DEB |

## 环境配置

项目使用 `gradle.properties` 进行配置：

| 属性 | 说明 | 默认值 |
|------|------|--------|
| `kotlin.code.style` | Kotlin 代码风格 | official |
| `kotlin.daemon.jvmargs` | Kotlin Daemon JVM 参数 | -Xmx3072M |
| `org.gradle.jvmargs` | Gradle JVM 参数 | -Xmx3072M -Dfile.encoding=UTF-8 |
| `org.gradle.configuration-cache` | 启用配置缓存 | true |
| `org.gradle.caching` | 启用构建缓存 | true |
| `android.nonTransitiveRClass` | R 类非传递 | true |
| `android.useAndroidX` | 使用 AndroidX | true |

## 测试流程

### 运行测试

```bash
# 运行所有模块的测试
./gradlew test

# 运行特定模块测试
./gradlew :libs:lko-ecs:test

# 运行测试并查看详细输出
./gradlew :libs:lko-ecs:test --info

# 运行测试并实时输出
./gradlew :libs:lko-ecs:test --console=plain
```

### 测试结构

```
src/
├── commonTest/       # 共享测试
├── jvmTest/          # JVM 测试
├── androidTest/      # Android 测试
└── wasmJsTest/       # WASM/JS 测试
```

### 编写测试

```kotlin
// 示例：ECS 框架测试
class WorldTest {
    @Test
    fun testEntityCreation() = runTest {
        val world = World()
        world.componentId<Health>() { it.component() }
        world.componentId<Position>() { it.component() }
        
        val entity = world.entity {
            it.addComponent(Health(100, 100))
            it.addComponent(Position(0, 0))
        }
        
        assert(entity.has<Health>())
        assert(entity.get<Health>().current == 100)
    }
}
```

## 代码规范

- **命名**: 类 PascalCase，变量/函数 camelCase
- **组件**: 名词（Health、Position）
- **标签**: 形容词+Tag（ActiveTag）
- **服务**: 功能+Service（HealthService）
- **导入**: 不用通配符，按标准库/第三方/项目分组
- **注释**: 重要类/函数用 KDoc
- **格式化**: 缩进 4 空格，行宽 120 字符

详见 [AGENTS.md](AGENTS.md) 中的 ECS 框架使用注意事项。

## 提交规范

### 提交信息格式

```
<类型>: <简短描述>

<详细描述>
```

### 类型

- `feat`: 新功能
- `fix`: 错误修复
- `refactor`: 重构
- `docs`: 文档更新
- `test`: 测试相关
- `chore`: 构建/工具链变更

### 示例

```
feat: 添加弟子修炼系统

- 新增 Disciple 类和修炼逻辑
- 实现经验值和等级系统
- 添加修炼事件回调
```

## 常见问题

### Q: Gradle 构建失败，提示找不到依赖

A: 确保网络畅通，Gradle 会自动从配置的仓库下载依赖。如遇访问问题，检查 `settings.gradle.kts` 中的仓库配置。国内镜像：maven.aliyun.com

### Q: 热重载不生效

A: 尝试重新运行 `./gradlew :composeApp:hotReloadJvmDev`，确保没有编译错误。

### Q: 测试失败，提示缺少组件

A: 在测试中确保先注册组件 ID：
```kotlin
world.componentId<YourComponent>() { it.component() }
```

### Q: Android 构建失败

A: 确保设置 ANDROID_HOME 并安装所需组件：
```bash
export ANDROID_HOME=/path/to/android/sdk
yes | $ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager --licenses
$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager "platforms;android-36" "build-tools;36.0.0"
```

## 相关文档

- [ECS 架构文档](docs/ecs-architecture.md)
- [游戏核心玩法](docs/sect_cultivation_core_gameplay.md)
- [项目规范](AGENTS.md)
