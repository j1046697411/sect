# 更新 AGENTS.md 文件的 Skill 实现计划

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 创建一个 skill 用于自动更新项目下所有 AGENTS.md 文件

**Architecture:** 基于模板驱动 + 自动扫描 + AI 智能填充的混合方案，支持自然语言触发和子任务调用

**Tech Stack:** Kotlin, OpenCode Skill System, Markdown 模板

---

## Task 1: 创建 Skill 目录结构

**Files:**
- Create: `.opencode/skills/updating-agents-md/SKILL.md`
- Create: `.opencode/skills/updating-agents-md/templates/module.md`
- Create: `.opencode/skills/updating-agents-md/templates/lib.md`
- Create: `.opencode/skills/updating-agents-md/templates/docs.md`

**Step 1: 创建目录**

Run: `mkdir -p .opencode/skills/updating-agents-md/templates`
Expected: 目录创建成功

**Step 2: 创建 SKILL.md 主文档**

```markdown
---
name: updating-agents-md
description: Use when updating AGENTS.md files across the project, when project structure changes, or when synchronizing documentation with code
---

# 更新 AGENTS.md 文件

## 概述
自动更新项目下所有 AGENTS.md 文件，确保文档与代码同步。

## 触发条件
- 用户说 "更新 AGENTS.md" 或类似指令
- 项目结构发生变化
- 需要同步文档与代码

## 执行流程

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│  扫描阶段    │ ──▶ │  模板匹配    │ ──▶ │  内容填充    │
└─────────────┘     └─────────────┘     └─────────────┘
                                              │
                                              ▼
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│  完成报告    │ ◀── │  Git 提交    │ ◀── │  验证阶段    │
└─────────────┘     └─────────────┘     └─────────────┘
```

## 目录类型识别

| 目录模式 | 模板 | 说明 |
|----------|------|------|
| `business-modules/*` | module.md | 业务模块 |
| `libs/*` | lib.md | 基础库 |
| `docs/*` | docs.md | 文档目录 |
| 根目录 | 内置 | 项目概览 |

## 使用方式

```bash
# 全量更新
"更新所有 AGENTS.md 文件"

# 指定目录更新
"更新 business-modules 的 AGENTS.md"

# 指定模块更新
"更新 business-cultivation 的 AGENTS.md"
```

## 扫描逻辑

1. 使用 glob 查找 `src/commonMain/**/*.kt` 文件
2. 解析文件提取类/接口/组件列表
3. 读取 `build.gradle.kts` 提取依赖
4. 读取现有 AGENTS.md 保留重要内容

## 验证逻辑

1. 检查子模块索引链接是否存在
2. 验证链接路径正确性
3. 确保格式一致性

## Git 提交

- 按目录分组提交
- 格式: `docs: 更新 {目录} 的 AGENTS.md`
```

**Step 3: 创建 module.md 模板**

```markdown
# {{MODULE_NAME}} - 模块描述

## 模块定位
{{MODULE_DESCRIPTION}}

**层级**: {{LAYER}}

## 核心职责
{{CORE_RESPONSIBILITIES}}

## 目录结构
```
{{DIRECTORY_STRUCTURE}}
```

## 关键 API

### 组件
| 组件 | 用途 | 说明 |
|------|------|------|
{{COMPONENTS_TABLE}}

### 服务
| 服务 | 用途 | 核心方法 |
|------|------|----------|
{{SERVICES_TABLE}}

## 使用方式

```kotlin
{{USAGE_EXAMPLE}}
```

## 依赖关系

```kotlin
// build.gradle.kts
dependencies {
{{DEPENDENCIES}}
}
```

## AI 开发指引

### 开发原则
{{DEVELOPMENT_PRINCIPLES}}

### 添加新功能检查清单
- [ ] 是否遵循模块职责？
- [ ] 测试覆盖率是否达标？
- [ ] 是否遵循 TDD 开发模式？

## 禁止事项
{{FORBIDDEN_PATTERNS}}

## 测试要求
{{TEST_REQUIREMENTS}}
```

**Step 4: 创建 lib.md 模板**

```markdown
# {{MODULE_NAME}} - 基础库描述

## 模块定位
{{MODULE_DESCRIPTION}}

**层级**: {{LAYER}}

## 核心职责
{{CORE_RESPONSIBILITIES}}

## 目录结构
```
{{DIRECTORY_STRUCTURE}}
```

## 关键 API

### 核心接口
| 接口/类 | 用途 | 说明 |
|---------|------|------|
{{KEY_APIS_TABLE}}

## 使用方式

```kotlin
{{USAGE_EXAMPLE}}
```

## 依赖关系

```kotlin
// build.gradle.kts
dependencies {
{{DEPENDENCIES}}
}
```

## AI 开发指引

### 开发原则
{{DEVELOPMENT_PRINCIPLES}}

### 性能要求
{{PERFORMANCE_REQUIREMENTS}}

## 测试要求
{{TEST_REQUIREMENTS}}
```

**Step 5: 创建 docs.md 模板**

```markdown
# {{DIR_NAME}} - 文档目录

## 目录定位
{{DIRECTORY_DESCRIPTION}}

## 文档索引

| 文档 | 说明 | 用途 |
|------|------|------|
{{DOCS_TABLE}}

## 子目录索引
{{SUBDIRS_INDEX}}
```

**Step 6: 提交**

```bash
git add .opencode/skills/updating-agents-md/
git commit -m "feat: 创建 updating-agents-md skill 的目录结构和模板"
```

---

## Task 2: 完善根目录 AGENTS.md 模板

**Files:**
- Create: `.opencode/skills/updating-agents-md/templates/root.md`

**Step 1: 创建 root.md 模板**

```markdown
# {{PROJECT_NAME}} - Agent 项目规范

## 铁律（切勿删除）

1. **所有测试用例发现的问题，都必须修复，不能跳过或者忽略**
2. **修改任何代码文档后，都必须同步提交 git 到本地，每次提交只提交自己修改的部分**
3. **在实现业务框架的时候发现核心框架的问题，必须立即修复，不能延迟到后续版本**
4. **发现任何文档或者代码中的问题，必须立即修复，不能延迟到后续版本**
5. **实现业务逻辑的时候，必须使用 TDD 模式，先编写测试用例，再实现功能**
6. **组件必须做到单一职责**
7. **当一个类或者文件行数超过 500 行时，必须重构拆分，做到高内聚，低耦合**

---

## 构建与测试命令

```bash
{{BUILD_COMMANDS}}
```

---

## 代码风格规范

### 语言与编码
- **语言**: Kotlin (100%)，所有注释和文档使用**中文**
- **编码**: UTF-8，换行符使用 LF (Unix 风格)

### 包名命名
{{PACKAGE_NAMING}}

### 类命名
{{CLASS_NAMING}}

### 格式化
- **缩进**: 4 空格（禁止 Tab）
- **行宽**: 120 字符
- **括号**: K&R 风格（左括号不换行）
- **空行**: 方法之间 1 个空行，类之间 2 个空行

### 导入规范
{{IMPORT_RULES}}

### 类型规范
{{TYPE_RULES}}

### 错误处理
{{ERROR_HANDLING}}

---

## 模块依赖规范

### 依赖引入方式
{{DEPENDENCY_IMPORT}}

### 依赖层级
```
{{DEPENDENCY_LAYERS}}
```

---

## 测试规范

### 测试风格 (BDD)
{{TEST_STYLE}}

### 测试命名
{{TEST_NAMING}}

### TDD 流程
1. **🔴 红**: 编写失败的测试 → `./gradlew test` → 确认失败
2. **🟢 绿**: 编写最小实现 → 确认通过
3. **🔵 重构**: 优化代码结构 → 保持通过

---

## 反模式 (Anti-Patterns)

| 类别 | 禁止行为 | 替代方案 |
|------|----------|----------|
{{ANTI_PATTERNS}}

---

## 关键文件位置

| 任务 | 位置 |
|------|------|
{{KEY_FILES}}

---

## 文档参考

{{DOC_REFERENCES}}
```

**Step 2: 提交**

```bash
git add .opencode/skills/updating-agents-md/templates/root.md
git commit -m "feat: 添加根目录 AGENTS.md 模板"
```

---

## Task 3: 更新 libs/AGENTS.md 子模块索引

**Files:**
- Modify: `libs/AGENTS.md`

**Step 1: 确认当前内容**

Run: `cat libs/AGENTS.md | head -70`
Expected: 查看当前 AGENTS.md 内容

**Step 2: 验证子模块索引完整性**

检查是否包含:
- lko-core
- lko-di
- lko-ecs
- lko-ecs-serialization
- lko-log

**Step 3: 如果需要更新，按模板更新**

确保格式与模板一致。

**Step 4: 提交（如有变更）**

```bash
git add libs/AGENTS.md
git commit -m "docs: 更新 libs/AGENTS.md 子模块索引"
```

---

## Task 4: 更新 business-modules/AGENTS.md 子模块索引

**Files:**
- Modify: `business-modules/AGENTS.md`

**Step 1: 确认当前内容**

Run: `cat business-modules/AGENTS.md | head -100`
Expected: 查看当前 AGENTS.md 内容

**Step 2: 验证子模块索引完整性**

检查是否包含所有 13 个子模块。

**Step 3: 如果需要更新，按模板更新**

确保格式与模板一致。

**Step 4: 提交（如有变更）**

```bash
git add business-modules/AGENTS.md
git commit -m "docs: 更新 business-modules/AGENTS.md 子模块索引"
```

---

## Task 5: 创建 skill 使用示例文档

**Files:**
- Create: `.opencode/skills/updating-agents-md/README.md`

**Step 1: 创建使用示例文档**

```markdown
# updating-agents-md Skill 使用指南

## 快速开始

### 全量更新
```
用户: 更新所有 AGENTS.md 文件
```

### 指定目录更新
```
用户: 更新 libs 目录的 AGENTS.md
```

### 指定模块更新
```
用户: 更新 business-cultivation 的 AGENTS.md
```

## 执行流程

1. **扫描阶段**: 遍历目录，提取代码结构
2. **模板匹配**: 根据目录类型选择模板
3. **内容填充**: AI 根据扫描结果填充内容
4. **验证阶段**: 检查索引链接、格式一致性
5. **Git 提交**: 按目录分组提交

## 模板变量

| 变量 | 说明 |
|------|------|
| `{{MODULE_NAME}}` | 模块名称 |
| `{{MODULE_DESCRIPTION}}` | 模块描述 |
| `{{DIRECTORY_STRUCTURE}}` | 目录树 |
| `{{KEY_APIS_TABLE}}` | API 列表 |
| `{{DEPENDENCIES}}` | 依赖列表 |
| `{{USAGE_EXAMPLE}}` | 使用示例 |

## 注意事项

- 保持现有重要内容不被覆盖
- 确保子模块索引与实际文件一致
- 按目录分组提交，便于回滚
```

**Step 2: 提交**

```bash
git add .opencode/skills/updating-agents-md/README.md
git commit -m "docs: 添加 updating-agents-md skill 使用指南"
```

---

## 完成检查

- [ ] Skill 目录结构创建完成
- [ ] 四个模板文件创建完成
- [ ] libs/AGENTS.md 验证完成
- [ ] business-modules/AGENTS.md 验证完成
- [ ] 使用示例文档创建完成
- [ ] 所有变更已提交到 git
