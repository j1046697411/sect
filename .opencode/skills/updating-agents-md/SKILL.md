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
│  扫描目录    │     │  选择模板    │     │  AI 填充    │
└─────────────┘     └─────────────┘     └─────────────┘
                                              │
                                              ▼
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│  完成报告    │ ◀── │  Git 提交    │ ◀── │  验证阶段    │
│  变更摘要    │     │  分步提交    │     │  检查索引    │
└─────────────┘     └─────────────┘     └─────────────┘
```

## 目录类型识别

| 目录模式 | 模板 | 说明 |
|----------|------|------|
| `business-modules/*` | module.md | 业务模块 |
| `libs/*` | lib.md | 基础库 |
| `docs/*` | docs.md | 文档目录 |
| 根目录 | root.md | 项目概览 |

## 使用方式

```bash
# 全量更新
"更新所有 AGENTS.md 文件"

# 指定目录更新
"更新 business-modules 的 AGENTS.md"
"更新 libs 的 AGENTS.md"

# 指定模块更新
"更新 business-cultivation 的 AGENTS.md"
```

## 扫描逻辑

1. **查找 AGENTS.md 文件**
   ```bash
   find . -name "AGENTS.md" -type f
   ```

2. **扫描源代码文件**
   ```bash
   # 扫描 Kotlin 源文件
   glob "{模块}/src/commonMain/**/*.kt"
   
   # 提取类/接口列表
   grep "^class\|^interface\|^object\|^data class\|^sealed class\|^enum class" *.kt
   ```

3. **提取依赖信息**
   - 读取 `build.gradle.kts` 中的 `dependencies` 块
   - 提取 `implementation(projects.*)` 依赖

4. **读取现有内容**
   - 保留重要的自定义内容
   - 提取模块描述和开发指引

## 模板变量

| 变量 | 说明 | 来源 |
|------|------|------|
| `{{MODULE_NAME}}` | 模块名称 | 目录名 |
| `{{MODULE_DESCRIPTION}}` | 模块描述 | AI 分析 |
| `{{LAYER}}` | 层级 | 目录类型 |
| `{{DIRECTORY_STRUCTURE}}` | 目录树 | 自动扫描 |
| `{{COMPONENTS_TABLE}}` | 组件表格 | 扫描 components/ |
| `{{SERVICES_TABLE}}` | 服务表格 | 扫描 services/ |
| `{{KEY_APIS_TABLE}}` | API 表格 | 扫描所有类 |
| `{{DEPENDENCIES}}` | 依赖列表 | build.gradle.kts |
| `{{USAGE_EXAMPLE}}` | 使用示例 | AI 生成 |
| `{{DEVELOPMENT_PRINCIPLES}}` | 开发原则 | AI 分析 |

## 验证逻辑

### 1. 子模块索引验证
```kotlin
// 检查索引中的链接是否存在
索引: [business-core](./business-core/AGENTS.md)
验证: business-core/AGENTS.md 文件存在？
```

### 2. 格式一致性验证
- 检查章节顺序是否符合模板
- 检查表格格式是否正确
- 检查代码块是否有语法高亮标记

### 3. 内容完整性验证
- 必要章节是否存在
- 关键 API 是否有说明
- 使用示例是否有效

## Git 提交策略

### 按目录分组提交
```bash
# libs 目录
git add libs/*/AGENTS.md libs/AGENTS.md
git commit -m "docs: 更新 libs 目录的 AGENTS.md 文件"

# business-modules 目录
git add business-modules/*/AGENTS.md business-modules/AGENTS.md
git commit -m "docs: 更新 business-modules 目录的 AGENTS.md 文件"

# docs 目录
git add docs/*/AGENTS.md docs/AGENTS.md
git commit -m "docs: 更新 docs 目录的 AGENTS.md 文件"

# 根目录
git add AGENTS.md
git commit -m "docs: 更新根目录 AGENTS.md"
```

### 提交信息格式
- 新增: `docs: 创建 {模块} 的 AGENTS.md`
- 更新: `docs: 更新 {模块} 的 AGENTS.md`
- 批量: `docs: 批量更新 AGENTS.md 文件`

## 执行检查清单

### 扫描阶段
- [ ] 查找所有 AGENTS.md 文件
- [ ] 扫描各模块源代码结构
- [ ] 提取依赖关系
- [ ] 读取现有内容

### 模板匹配阶段
- [ ] 识别目录类型
- [ ] 加载对应模板
- [ ] 准备模板变量

### 内容填充阶段
- [ ] 生成目录结构
- [ ] 提取关键 API
- [ ] 生成使用示例
- [ ] 填充开发指引

### 验证阶段
- [ ] 检查子模块索引
- [ ] 验证链接有效性
- [ ] 检查格式一致性

### 提交阶段
- [ ] 按目录分组暂存
- [ ] 生成提交信息
- [ ] 执行提交

## 注意事项

1. **保留现有重要内容** - 不要覆盖用户自定义的重要信息
2. **增量更新** - 只更新变化的部分，减少 diff 噪音
3. **分步提交** - 便于 review 和回滚
4. **验证后再提交** - 确保链接有效、格式正确
