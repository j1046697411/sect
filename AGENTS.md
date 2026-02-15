# 宗门修真录 - Agent 项目规范

## 关键说明：仅限中文策略（切勿删除此部分）

> **此部分绝不可删除或修改**

### 所有项目交流必须使用中文

| 上下文 | 语言要求 |
|---------|---------------------|
| **GitHub Issues** | 仅限中文 |
| **Pull Requests** | 仅限中文（标题、描述、评论） |
| **提交信息** | 仅限中文 |
| **代码注释** | 仅限中文 |
| **文档** | 仅限中文 |
| **AGENTS.md 文件** | 仅限中文 |

**如果你不习惯用中文写作，请使用翻译工具。**

---

## 开发流程：测试驱动开发 (TDD) - 强制

本项目强制使用 TDD（测试驱动开发）模式。所有新功能开发必须遵循 **红-绿-重构** 循环：

1. **🔴 红 (Red)**: 
   - 编写失败的测试用例。
   - 运行测试 (`./gradlew test` 或 `./gradlew :module:test`) 确认失败。
2. **🟢 绿 (Green)**: 
   - 编写最小实现代码，只为通过测试。
   - 运行测试，确认通过。
3. **🔵 重构 (Refactor)**: 
   - 优化代码结构，保持测试通过。
   - 确保代码符合项目规范。

**禁止**: 在没有测试的情况下编写业务代码。
**禁止**: 一次性编写大量代码后再补测试。

---

## 代码验收标准 - 强制 (Definition of Done)

所有代码提交（Commit/PR）前必须满足以下标准：

1. **测试通过**: 
   - 运行 `./gradlew test` 确保所有测试用例通过。
   - 禁止提交失败的测试。

2. **代码覆盖率**:
   - 运行 `./gradlew allCoverage` 生成报告。
   - 核心逻辑（ECS系统、业务算法）覆盖率必须达标（建议 > 80%）。
   - 必须检查生成的 HTML 报告，确认未覆盖的分支逻辑。

3. **静态检查 (Lint)**:
   - 代码无明显 Lint 警告。
   - 运行 `./gradlew lint` (如适用) 或 IDE 检查确保无语法/风格错误。

4. **无脏代码**:
   - 移除所有临时的 `println`, `TODO` (除非是长期规划), `Unused import`。

---

## 项目概述

Kotlin/Gradle 多模块 ECS 游戏项目。核心架构基于 Entity-Component-System (ECS) 模式。

## 模块结构

| 模块 | 说明 |
|------|------|
| `composeApp` | 应用主模块 (桌面/Android/Web/WASM) |
| `libs/lko-ecs` | ECS 核心框架 |
| `libs/lko-core` | 基础工具库 |
| `libs/lko-di` | 依赖注入框架 |
| `business-modules/*` | 业务逻辑模块 |

## 常用命令

### 构建与检查
- **全量构建**: `./gradlew build`
- **清理**: `./gradlew clean`
- **Android Lint**: `./gradlew :composeApp:lint`
- **运行 Demo**: `./gradlew :composeApp:run`

### 测试 (关键)
- **运行所有测试**: `./gradlew test`
- **运行特定模块测试**: `./gradlew :libs:lko-ecs:test`
- **运行单个测试类**: `./gradlew :libs:lko-ecs:test --tests "cn.jzl.ecs.WorldTest"`
- **运行单个测试方法**: `./gradlew :libs:lko-ecs:test --tests "cn.jzl.ecs.WorldTest.testName"`
- **持续测试模式**: `./gradlew :libs:lko-ecs:test --continuous`

### 代码覆盖率
- **生成报告**: `./gradlew allCoverage`
- **查看报告**: `open libs/lko-ecs/build/reports/kover/htmlJvm/index.html`

## 代码风格规范

- **命名约定**: 
  - 类/接口: PascalCase (`World`)
  - 函数/属性: camelCase (`getComponent`)
  - 常量: UPPER_SNAKE_CASE (`MAX_ENTITIES`)
  - 组件: 名词 (`Health`)
  - 标签: 形容词+Tag (`ActiveTag`)
  - 系统: 功能+System (`MovementSystem`)
- **格式化**: 4 个空格缩进，120 字符行宽，UTF-8 编码。
- **导入顺序**: Kotlin/Java 标准库 -> 第三方库 -> 项目内部模块。
- **错误处理**: 优先使用标准异常，严禁吞掉异常，ECS 系统中防止未捕获异常。

## ECS 核心规范 (强制)

1.  **组件 vs 标签**:
    - **组件**: 必须包含数据 (`data class`/`value class`)。
    - **标签**: 必须不含数据 (`sealed class`/`object`)。
    - **严禁混用**: 禁止用 `addComponent` 加标签，禁止用 `addTag` 加组件。

2.  **实体操作**:
    - **创建**: 使用 `world.entity { ... }`。
    - **修改**: 必须使用 `entity.editor { ... }`。组件是不可变的，使用 `copy()` 修改。

3.  **查询**:
    - 使用 `Context` 定义查询。
    - **严禁**: 在遍历 Query 结果时直接修改实体结构（可能导致并发修改异常）。

## 测试规范

### TDD 核心规则 (强制)
- **绝不在测试前编写实现**: 必须先有失败的测试。
- **绝不删除失败的测试**: 必须修复代码使测试通过。
- **测试文件位置**: 
  - 单元测试: `src/commonTest/kotlin/...` 或 `src/test/kotlin/...`
  - 与被测代码包结构保持一致。
- **BDD 注释**: 使用 `// Given`, `// When`, `// Then` 清晰标注测试步骤。

### 测试类命名
- **测试数据类**: 使用模块前缀避免冲突 (如 `CompPosition`, `QueryPosition`)。

## 常见陷阱与强制禁止 (Critical Anti-Patterns)

以下模式**严格禁止**，代码审查时一旦发现必须拒绝：

1.  **组件/标签混用**:
    *   ❌ 禁止: `addComponent(ActiveTag)` (标签当组件用)
    *   ❌ 禁止: `addTag<Health>()` (组件当标签用)
2.  **查询中修改实体**:
    *   ❌ 禁止: 在 `world.query { ... }.forEach { ... }` 循环内部直接调用 `entity.editor {}` 修改实体结构（增删组件）。
    *   ✅ 正确: 先收集需要修改的实体，循环结束后统一处理。
3.  **Lambda 参数遮蔽**:
    *   ❌ 禁止: 嵌套 Lambda 中隐式使用 `it` 导致指代不明。
    *   ✅ 正确: 显式命名参数，如 `repeat(10) { index -> ... }`。
4.  **导入混淆**:
    *   注意区分 `cn.jzl.ecs.family.component` (用于 Context 查询) 和 `cn.jzl.ecs.relation.component` (用于 Relation 定义)。

## 参考文档
- ECS 详细文档: `docs/ecs-architecture.md`
