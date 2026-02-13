# Kover 快速参考

## 一键命令

```bash
# 运行所有模块的测试 + 生成覆盖率报告（推荐）
./gradlew allCoverage

# 或使用脚本
./scripts/kover-report-all.sh
```

## 模块状态

| 模块 | 命令 | 状态 |
|------|------|------|
| **lko-ecs** | `./gradlew ecsCoverage` | ✅ 147个测试 |
| **lko-core** | `./gradlew :libs:lko-core:koverHtmlReportJvm` | ⚠️ 无测试 |
| **lko-di** | `./gradlew :libs:lko-di:koverHtmlReportJvm` | ⚠️ 无测试 |
| **lko-ecs-serialization** | - | ❌ 编译错误 |

## 常用任务

| 任务 | 说明 |
|------|------|
| `./gradlew allCoverage` | 所有可用模块的测试和报告 |
| `./gradlew ecsCoverage` | ECS 模块测试和报告 |
| `./gradlew :libs:MODULE:koverHtmlReportJvm` | 指定模块 HTML 报告 |
| `./gradlew :libs:MODULE:koverXmlReportJvm` | 指定模块 XML 报告 |
| `./gradlew :libs:MODULE:test` | 仅运行测试 |

## 报告位置

### lko-ecs
- **HTML**: `libs/lko-ecs/build/reports/kover/htmlJvm/index.html`
- **XML**: `libs/lko-ecs/build/reports/kover/reportJvm.xml`

### lko-core
- **HTML**: `libs/lko-core/build/reports/kover/htmlJvm/index.html`
- **XML**: `libs/lko-core/build/reports/kover/reportJvm.xml`

### lko-di
- **HTML**: `libs/lko-di/build/reports/kover/htmlJvm/index.html`
- **XML**: `libs/lko-di/build/reports/kover/reportJvm.xml`

## 查看报告

```bash
# macOS - 所有模块
open libs/lko-ecs/build/reports/kover/htmlJvm/index.html
open libs/lko-core/build/reports/kover/htmlJvm/index.html
open libs/lko-di/build/reports/kover/htmlJvm/index.html
```

## 配置位置

- 版本目录: `gradle/libs.versions.toml`
- 根项目配置: `build.gradle.kts`
- 详细文档: `docs/kover-coverage.md`

## CI/CD 集成

XML 报告路径：
```
libs/lko-ecs/build/reports/kover/reportJvm.xml
libs/lko-core/build/reports/kover/reportJvm.xml
libs/lko-di/build/reports/kover/reportJvm.xml
```

## 待办事项

1. **lko-core**: 添加单元测试
2. **lko-di**: 添加单元测试  
3. **lko-ecs-serialization**: 修复编译错误后启用
