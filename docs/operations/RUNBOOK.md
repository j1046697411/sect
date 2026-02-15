# 运维手册

本文档提供部署、监控和问题排查的指导。

## 部署流程

### 桌面应用部署

#### macOS

```bash
# 1. 构建 Release 版本
./gradlew :client:release

# 2. 打包 DMG
./gradlew :client:packageDmg

# 3. 签名（需要开发者证书）
./gradlew :client:notarizeDmg
```

#### Windows

```bash
# 1. 构建 Release 版本
./gradlew :client:release

# 2. 打包 MSI
./gradlew :client:packageMsi

# 或打包可执行 JAR
./gradlew :client:packageUberJarForCurrentOS
```

#### Linux

```bash
# 1. 构建 Release 版本
./gradlew :client:release

# 2. 打包 DEB
./gradlew :client:packageDeb

# 或打包 RPM
./gradlew :client:packageRpm
```

### Android 应用部署

#### 构建 APK

```bash
# Debug 构建
./gradlew :client:assembleDebug
# 输出: client/build/outputs/apk/debug/

# Release 构建（需要签名配置）
./gradlew :client:assembleRelease
```

#### 发布到 Play Store

```bash
# 1. 配置签名
# 在 gradle.properties 中添加：
# android.signing.keystore.path
# android.storePassword
# android.keyAlias
# android.keyPassword

# 2. 生成 App Bundle
./gradlew :client:bundleRelease

# 3. 上传到 Play Store
# 使用 Google Play Console 或 fastlane
```

## 持续集成

### GitHub Actions 示例

```yaml
name: Build and Test

on:
  push:
    branches: [main]
  pull_request:
    branches: [main]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      
      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
          
      - name: Cache Gradle
        uses: actions/cache@v3
        with:
          path: |
            ~/.gradle/caches
            ~/.gradle/wrapper
          key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle*') }}
          restore-keys: |
            ${{ runner.os }}-gradle-
            
      - name: Build
        run: ./gradlew build
        
      - name: Test
        run: ./gradlew test
        
      - name: Lint
        run: ./gradlew :client:lint

  desktop:
    needs: build
    runs-on: macos-latest
    steps:
      - uses: actions/checkout@v4
      
      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          
      - name: Build DMG
        run: ./gradlew :client:packageDmg
        
      - name: Upload Artifact
        uses: actions/upload-artifact@v3
        with:
          name: desktop-app
          path: client/build/outputs/**/*.dmg
```

## 监控和日志

### 日志配置

项目使用 Kotlin 标准日志库。运行时日志位置：

- **桌面应用**: `~/.sect/logs/`
- **Android**: Logcat

### 性能监控

ECS 框架提供基准测试：

```bash
# 运行基准测试
./gradlew benchmark

# 运行特定基准测试
./gradlew :benchmarks:lko-ecs-benchmarks:mainBenchmark
```

基准测试结果示例：

```
Benchmark                      Mode  Cnt    Score    Error   Units
EntityCreateBenchmark.create  avgt   10    0.123 ±  0.015  us/op
QueryBenchmark.query          avgt   10    0.456 ±  0.032  us/op
```

### 代码覆盖率监控

```bash
# 生成所有模块覆盖率报告
./gradlew allCoverage

# 查看 HTML 报告
open libs/lko-ecs/build/reports/kover/htmlJvm/index.html

# 生成 XML 报告（CI 集成）
./gradlew :libs:lko-ecs:koverXmlReportJvm
```

## 常见问题及修复

### 问题 1: Gradle 构建超时

**症状**: 构建过程中下载依赖超时

**解决方案**:
```bash
# 1. 清理 Gradle 缓存
./gradlew clean

# 2. 删除本地缓存
rm -rf ~/.gradle/caches/modules-2/files-2.1/

# 3. 使用国内镜像
# 项目已配置阿里云镜像：maven.aliyun.com
```

### 问题 2: 热重载不工作

**症状**: 代码修改后界面未更新

**解决方案**:
```bash
# 1. 确保使用热重载任务
./gradlew :client:hotReloadJvmDev

# 2. 检查是否有编译错误
./gradlew :client:compileJvmDev --info

# 3. 重启热重载
# 停止当前进程，重新运行
```

### 问题 3: Android 构建失败

**症状**: `sdk.dir` 未找到或 Android 资源错误

**解决方案**:
```bash
# 1. 设置 ANDROID_HOME
export ANDROID_HOME=/path/to/android/sdk

# 2. 接受许可协议
yes | $ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager --licenses

# 3. 安装所需组件（当前项目使用 compileSdk 36）
$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager "platforms;android-36" "build-tools;36.0.0"
```

### 问题 4: 测试失败 - ConcurrentModificationException

**症状**: 在遍历查询结果时修改实体抛出异常

**错误代码**:
```kotlin
// 错误
world.query { DiscipleContext(this) }.forEach { ctx ->
    ctx.entity.editor { it.addComponent(newHealth) }
}
```

**解决方案**:
```kotlin
// 正确：先收集实体，再修改
val entities = world.query { DiscipleContext(this) }
    .map { it.entity }
    .toList()
entities.forEach { entity ->
    entity.editor { it.addComponent(newHealth) }
}
```

### 问题 5: 组件查询返回空结果

**症状**: 查询不到预期的实体

**可能原因**:
1. 组件未注册
2. Archetype 不匹配

**解决方案**:
```kotlin
// 确保在启动时注册所有组件
class GameWorld {
    fun init() {
        world.componentId<Health>() { it.component() }
        world.componentId<Position>() { it.component() }
    }
}

// 使用正确的查询方式
class DiscipleContext(world: World) : EntityQueryContext(world) {
    override fun FamilyBuilder.configure() {
        component<Health>()  // 使用 component，不是 kind 或 tag
    }
}
```

### 问题 6: Lint 检查失败

**症状**: `lint` 任务报告错误

**解决方案**:
```bash
# 查看详细错误
./gradlew :client:lint --info

# 自动修复
./gradlew :client:lintFix

# 如需忽略特定错误，在 build.gradle.kts 中配置
android {
    lint {
        warning 'MissingTranslation', 'HardcodedText'
        error 'NewApi'
        abortOnError false
    }
}
```

### 问题 7: Kover 覆盖率生成失败

**症状**: 覆盖率报告任务失败

**解决方案**:
```bash
# 确保 Kover 插件正确应用
./gradlew :libs:lko-ecs:koverHtmlReportJvm --info

# 检查测试是否运行
./gradlew :libs:lko-ecs:test
./gradlew :libs:lko-ecs:koverHtmlReportJvm
```

### 问题 8: Kotlin Multiplatform 插件警告

**症状**: AGP 兼容性警告

**警告信息**:
```
The 'org.jetbrains.kotlin.multiplatform' plugin deprecated compatibility 
with Android Gradle plugin: 'com.android.library'
```

**解决方案**: 计划迁移到 `com.android.kotlin.multiplatform.library` 插件（AGP 9.0+）

## 回滚流程

### 版本回滚

```bash
# 1. 查看版本历史
git log --oneline -20

# 2. 回滚到特定版本
git checkout <commit-hash>

# 3. 重新构建
./gradlew clean build
```

### 依赖回滚

如需回滚某个依赖版本，在 `gradle/libs.versions.toml` 中锁定版本：

```toml
[versions]
# 锁定特定版本
kotlin = "2.3.0"

[libraries]
# 或在依赖中指定版本
kotlin-test = { module = "org.jetbrains.kotlin:kotlin-test", version = "2.3.0" }
```

### 部署回滚

如需回滚已发布的应用：

1. **桌面应用**: 重新打包上一个稳定版本
2. **Android**: 在 Play Console 中使用「紧急回滚」功能

## 健康检查

### 构建健康检查

```bash
# 1. 运行完整构建
./gradlew build

# 2. 运行所有测试
./gradlew test

# 3. 运行代码检查
./gradlew :client:lint

# 4. 运行基准测试（验证性能未退化）
./gradlew benchmark

# 5. 生成覆盖率报告
./gradlew allCoverage
```

### 发布前检查清单

- [ ] 所有测试通过
- [ ] Lint 检查无错误
- [ ] 基准测试结果在预期范围内
- [ ] 覆盖率报告已生成
- [ ] CHANGELOG 已更新
- [ ] 版本号已更新
- [ ] 构建产物已验证

## 依赖版本管理

项目使用 Gradle 版本目录 (`gradle/libs.versions.toml`) 管理依赖：

| 依赖 | 版本 |
|------|------|
| Kotlin | 2.3.0 |
| Compose Multiplatform | 1.9.3 |
| Android Gradle Plugin | 8.11.2 |
| Android compileSdk | 36 |
| Android minSdk | 24 |
| Kover | 0.9.1 |

### Gradle 任务（单一事实来源）

项目在 `build.gradle.kts` 中定义了以下自定义任务：

| 任务 | 说明 |
|------|------|
| `allCoverage` | 运行所有库模块测试并生成覆盖率报告 |
| `ecsCoverage` | 运行 ECS 模块测试并生成覆盖率报告 |

## 相关文档

- [贡献指南](./CONTRIB.md)
- [ECS 架构文档](../technology/ecs-architecture.md)
- [项目规范](../AGENTS.md)
