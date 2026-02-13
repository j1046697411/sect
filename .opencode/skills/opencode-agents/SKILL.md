---
name: opencode-agents
description: OpenCode 代理系统使用指南。了解如何配置和使用主代理、子代理，以及如何为项目创建自定义代理。
---

# OpenCode 代理系统

OpenCode 代理是专门的人工智能助手，可以针对特定任务和工作流程进行配置。

## 代理类型

### 主代理 (Primary)

主代理是与您直接交互的主要助手。使用 Tab 键或 `switch_agent` 键绑定循环浏览它们。

| 代理 | 说明 | 工具权限 |
|------|------|----------|
| Build | 默认代理，完全访问 | 全部启用 |
| Plan | 规划代理，受限访问 | 默认全部 ask |

### 子代理 (Subagent)

子代理是主代理可以调用的专业助手。可通过 @提及 手动调用。

| 代理 | 说明 | 工具权限 |
|------|------|----------|
| General | 通用代理，多步骤任务 | 完整访问 |
| Explore | 只读代理，探索代码库 | 只读 |

## 代理配置

### JSON 配置

在 `opencode.json` 中配置：

```json
{
  "agent": {
    "build": {
      "mode": "primary",
      "model": "anthropic/claude-sonnet-4-5",
      "prompt": "{file:./prompts/build.txt}",
      "tools": {
        "write": true,
        "edit": true,
        "bash": true
      }
    },
    "code-reviewer": {
      "description": "代码审查专家",
      "mode": "subagent",
      "model": "anthropic/claude-opus-4-5",
      "prompt": "{file:./prompts/agents/code-reviewer.md}",
      "tools": {
        "write": false,
        "edit": false
      }
    }
  }
}
```

### Markdown 配置

在 `.opencode/prompts/agents/` 目录下创建 Markdown 文件：

```markdown
---
name: agent-name
description: 代理描述
mode: subagent
model: anthropic/claude-opus-4-5
tools:
  write: false
  edit: false
  bash: true
---

代理提示词内容...
```

## 配置选项

| 选项 | 说明 |
|------|------|
| description | 代理用途描述 |
| mode | primary 或 subagent |
| model | 使用的模型 |
| prompt | 提示词文件路径或内联内容 |
| tools | 工具权限配置 |
| temperature | 模型温度（默认 0.7） |

## 工具权限

```json
{
  "tools": {
    "write": true,    // 创建新文件
    "edit": true,     // 编辑现有文件
    "bash": true,     // 执行 shell 命令
    "read": true      // 读取文件
  }
}
```

## 项目代理

当前项目已配置的代理：

| 代理 | 模式 | 用途 |
|------|------|------|
| build | primary | 主要编码代理 |
| planner | subagent | 实现规划 |
| architect | subagent | 系统设计 |
| tdd-guide | subagent | 测试驱动开发 |
| code-reviewer | subagent | 代码审查 |
| security-reviewer | subagent | 安全分析 |

## 使用方式

### 主代理切换

- 使用 Tab 键循环浏览主代理
- 使用配置的 `switch_agent` 键绑定

### 子代理调用

```
@tdd-guide 帮我实现一个健康服务
@code-reviewer 审查这段代码
@security-reviewer 检查安全问题
```

### 会话导航

- `<Leader>+Right` 向前循环父级 → 子级
- `<Leader>+Left` 向后循环

## 创建自定义代理

### 1. 创建提示词文件

在 `.opencode/prompts/agents/` 创建 `<name>.md`：

```markdown
---
name: ecs-expert
description: ECS 框架专家，处理 ECS 相关问题
mode: subagent
model: anthropic/claude-opus-4-5
tools:
  write: false
  edit: false
  bash: true
---

你是 ECS 框架专家，专注于：
- 组件设计
- 查询优化
- 实体生命周期管理
...
```

### 2. 注册到配置文件

在 `opencode.json` 中添加：

```json
{
  "agent": {
    "ecs-expert": {
      "description": "ECS 框架专家",
      "mode": "subagent",
      "prompt": "{file:./prompts/agents/ecs-expert.md}",
      "tools": {
        "read": true,
        "bash": true
      }
    }
  }
}
```

## 最佳实践

1. **明确职责** - 每个代理应有明确的单一职责
2. **限制权限** - 只给予必要的工具权限
3. **详细描述** - 提供清晰的使用场景描述
4. **模型选择** - 根据任务复杂度选择合适模型
5. **提示词优化** - 提供具体、可操作的指导

## 参考链接

- [OpenCode 代理文档](https://opencode.ai/docs/agents)
