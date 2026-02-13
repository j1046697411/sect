# Kover 代码覆盖率配置

项目已配置 [Kover](https://github.com/Kotlin/kotlinx-kover) 用于代码覆盖率分析。

## 快速开始

### 生成所有可用模块的覆盖率报告

```bash
# 一键生成所有模块报告（推荐）
./gradlew allCoverage

# 或使用脚本
./scripts/kover-report-all.sh
```

### 单个模块报告

```bash
# ECS 模块
./gradlew :libs:lko-ecs:koverHtmlReportJvm

# Core 模块
./gradlew :libs:lko-core:koverHtmlReportJvm

# DI 模块
./gradlew :libs:lko-di:koverHtmlReportJvm

# 所有模块（脚本方式）
./scripts/kover-report-all.sh libs:lko-ecs
```

### 查看报告

```bash
# macOS
open libs/lko-ecs/build/reports/kover/htmlJvm/index.html
open libs/lko-core/build/reports/kover/htmlJvm/index.html
open libs/lko-di/build/reports/kover/htmlJvm/index.html

# Linux
xdg-open libs/lko-ecs/build/reports/kover/htmlJvm/index.html
```

## 模块覆盖情况

| 模块 | 测试状态 | 覆盖率报告 | 备注 |
|------|----------|------------|------|
| **lko-ecs** | ✅ 有测试 | ✅ 可用 | ECS 框架核心，147个测试 |
| **lko-core** | ⚠️ 无测试 | ⚠️ 无数据 | 需添加测试 |
| **lko-di** | ⚠️ 无测试 | ⚠️ 无数据 | 需添加测试 |
| **lko-ecs-serialization** | ❌ 编译错误 | ❌ 不可用 | 需修复编译错误 |

## 配置说明

### 根项目配置 (`build.gradle.kts`)

Kover 已配置为自动应用到所有 Kotlin 子项目：

```kotlin
subprojects {
    plugins.withId("org.jetbrains.kotlin.multiplatform") {
        apply(plugin = "org.jetbrains.kotlinx.kover")
        
        extensions.configure<kotlinx.kover.gradle.plugin.dsl.KoverProjectExtension> {
            reports {
                filters {
                    excludes {
                        classes("*Test*")        // 排除测试类
                        classes("*\$*")          // 排除内部类
                    }
                }
            }
        }
    }
}
```

### 可用任务

#### 报告任务
- `koverHtmlReportJvm` - HTML 格式报告（JVM 平台）
- `koverXmlReportJvm` - XML 格式报告（JVM 平台，CI 用）
- `koverBinaryReportJvm` - 二进制格式报告

#### 便捷任务
- `./gradlew allCoverage` - 所有模块的测试和报告
- `./gradlew ecsCoverage` - 仅 ECS 模块

## 报告位置

### lko-ecs 模块
- **HTML**: `libs/lko-ecs/build/reports/kover/htmlJvm/index.html`
- **XML**: `libs/lko-ecs/build/reports/kover/reportJvm.xml`

### lko-core 模块
- **HTML**: `libs/lko-core/build/reports/kover/htmlJvm/index.html`
- **XML**: `libs/lko-core/build/reports/kover/reportJvm.xml`

### lko-di 模块
- **HTML**: `libs/lko-di/build/reports/kover/htmlJvm/index.html`
- **XML**: `libs/lko-di/build/reports/kover/reportJvm.xml`

## CI/CD 集成

### GitHub Actions 示例

```yaml
name: Coverage

on: [push, pull_request]

jobs:
  coverage:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Set up JDK
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
          
      - name: Run Tests with Coverage
        run: ./gradlew allCoverage
        
      - name: Upload ECS Coverage
        uses: codecov/codecov-action@v3
        with:
          files: libs/lko-ecs/build/reports/kover/reportJvm.xml
          flags: ecs
          
      - name: Upload Core Coverage
        uses: codecov/codecov-action@v3
        with:
          files: libs/lko-core/build/reports/kover/reportJvm.xml
          flags: core
```

## 待办事项

### 1. 添加缺失的测试

**lko-core 模块**
- 核心工具类测试
- 集合类测试
- 位操作测试

**lko-di 模块**
- 依赖注入测试
- 生命周期管理测试
- 作用域测试

### 2. 修复编译错误

**lko-ecs-serialization 模块**
- 修复 `EntitySerializationExtensions.kt` 中的 API 兼容性问题
- 更新对 ECS 内部 API 的引用
- 重新启用该模块的覆盖率报告

## 故障排除

### 报告为空或覆盖率 0%

模块可能没有测试代码。检查：
```bash
find libs/MODULE_NAME/src -name "*Test.kt"
```

### lko-ecs-serialization 编译错误

该模块暂时无法生成报告。需要修复：
- `EntitySerializationExtensions.kt` 中的 unresolved reference
- `World.components` 等 internal API 访问问题

### 多平台报告

目前主要关注 JVM 平台报告（`kover*Jvm`）。Android 和 JS 平台报告可用但可能不完整。

## 参考文档

- [Kover GitHub](https://github.com/Kotlin/kotlinx-kover)
- [Kover Gradle Plugin DSL](https://kotlin.github.io/kotlinx-kover/gradle-plugin-dsl/0.9.0/)
- [Kover 官方文档](https://kotlin.github.io/kotlinx-kover/)
