# 更新 AGENTS.md 文件的 Skill 设计

## 概述

创建一个 skill 用于自动更新项目下所有 AGENTS.md 文件，支持：
- 自动扫描提取模块信息
- 格式规范化
- 内容同步更新
- 索引链接验证

## Skill 基本信息

```yaml
---
name: updating-agents-md
description: Use when updating AGENTS.md files across the project, when project structure changes, or when synchronizing documentation with code
---
```

## 目录结构

```
.opencode/skills/updating-agents-md/
├── SKILL.md              # 主文档（约 200 行）
└── templates/
    ├── module.md         # 业务模块模板
    ├── lib.md            # 基础库模板  
    └── docs.md           # 文档目录模板
```

## 核心流程

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

## 模板设计

每个 AGENTS.md 应包含以下标准章节：

| 章节 | 内容 | 来源 |
|------|------|------|
| 模块定位 | 一句话描述 | AI 分析 |
| 目录结构 | 文件树 | 自动扫描 |
| 关键 API | 组件/服务列表 | 自动扫描 |
| 使用方式 | 代码示例 | AI 生成 |
| 依赖关系 | build.gradle.kts | 自动提取 |
| 开发指引 | 注意事项 | AI 分析 |

## 目录类型识别

| 目录模式 | 模板 | 特殊处理 |
|----------|------|----------|
| `business-modules/*` | module.md | 提取 Addon、Service、Component |
| `libs/*` | lib.md | 提取依赖关系、性能要求 |
| `docs/*` | docs.md | 文档索引 |
| 根目录 | root.md | 项目概览、构建命令 |

## 实现要点

### 1. 扫描逻辑
- 使用 glob 查找所有 `src/commonMain/**/*.kt` 文件
- 解析文件名提取类/接口列表
- 读取 build.gradle.kts 提取依赖

### 2. 模板变量
- `{{MODULE_NAME}}` - 模块名称
- `{{DIRECTORY_STRUCTURE}}` - 目录树
- `{{KEY_APIS}}` - API 列表
- `{{DEPENDENCIES}}` - 依赖列表
- `{{USAGE_EXAMPLE}}` - 使用示例

### 3. 索引验证
- 检查子模块索引链接是否存在
- 验证链接路径正确性
- 自动修复缺失或错误的链接

### 4. Git 提交策略
- 按目录分组提交
- 提交信息格式：`docs: 更新 {目录} 的 AGENTS.md`

## 测试计划

1. 单模块更新测试
2. 全量更新测试
3. 索引验证测试
4. 格式一致性测试
