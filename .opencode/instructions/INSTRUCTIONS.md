# Everything Claude Code - OpenCode 指令

本文档整合了 Claude Code 配置中的核心规则和指南，供 OpenCode 使用。

## 安全指南（关键）

### 强制安全检查

在每次提交前：
- [ ] 无硬编码密钥（API 密钥、密码、令牌）
- [ ] 所有用户输入已验证
- [ ] SQL 注入防护（参数化查询）
- [ ] XSS 防护（HTML 消毒）
- [ ] CSRF 保护已启用
- [ ] 认证/授权已验证
- [ ] 所有端点已启用速率限制
- [ ] 错误消息不泄露敏感数据

### 密钥管理

```typescript
// 永远不要：硬编码密钥
const apiKey = "sk-proj-xxxxx"

// 始终：使用环境变量
const apiKey = process.env.OPENAI_API_KEY

if (!apiKey) {
  throw new Error('OPENAI_API_KEY not configured')
}
```

### 安全响应协议

如果发现安全问题：
1. 立即停止
2. 使用 **security-reviewer** 代理
3. 在继续之前修复关键问题
4. 轮换所有暴露的密钥
5. 审查整个代码库以查找类似问题

---

## 编码风格

### 不可变性（关键）

始终创建新对象，永远不要修改：

```javascript
// 错误：修改
function updateUser(user, name) {
  user.name = name  // 修改！
  return user
}

// 正确：不可变
function updateUser(user, name) {
  return {
    ...user,
    name
  }
}
```

### 文件组织

多个小文件 > 少量大文件：
- 高内聚，低耦合
- 通常 200-400 行，最多 800 行
- 从大型组件中提取工具函数
- 按功能/领域组织，而非按类型

### 错误处理

始终全面处理错误：

```typescript
try {
  const result = await riskyOperation()
  return result
} catch (error) {
  console.error('Operation failed:', error)
  throw new Error('Detailed user-friendly message')
}
```

### 输入验证

始终验证用户输入：

```typescript
import { z } from 'zod'

const schema = z.object({
  email: z.string().email(),
  age: z.number().int().min(0).max(150)
})

const validated = schema.parse(input)
```

### 代码质量检查清单

在标记工作完成前：
- [ ] 代码可读且命名良好
- [ ] 函数小巧（<50 行）
- [ ] 文件专注（<800 行）
- [ ] 无深层嵌套（>4 层）
- [ ] 正确的错误处理
- [ ] 无 console.log 语句
- [ ] 无硬编码值
- [ ] 无修改（使用不可变模式）

---

## 测试要求

### 最低测试覆盖率：80%

测试类型（全部必需）：
1. **单元测试** - 单个函数、工具、组件
2. **集成测试** - API 端点、数据库操作
3. **端到端测试** - 关键用户流程（Playwright）

### 测试驱动开发

强制工作流程：
1. 先写测试（红灯）
2. 运行测试 - 应该失败
3. 编写最小实现（绿灯）
4. 运行测试 - 应该通过
5. 重构（改进）
6. 验证覆盖率（80%+）

### 测试失败排查

1. 使用 **tdd-guide** 代理
2. 检查测试隔离
3. 验证模拟是否正确
4. 修复实现，而非测试（除非测试错误）

---

## Git 工作流程

### 提交消息格式

```
<type>: <description>

<optional body>
```

类型：feat, fix, refactor, docs, test, chore, perf, ci

### 拉取请求工作流程

创建 PR 时：
1. 分析完整提交历史（不仅是最新提交）
2. 使用 `git diff [base-branch]...HEAD` 查看所有更改
3. 起草全面的 PR 摘要
4. 包含带 TODO 的测试计划
5. 如果是新分支，使用 `-u` 标志推送

### 功能实现工作流程

1. **先规划**
   - 使用 **planner** 代理创建实现计划
   - 识别依赖和风险
   - 分解为阶段

2. **TDD 方法**
   - 使用 **tdd-guide** 代理
   - 先写测试（红灯）
   - 实现以通过测试（绿灯）
   - 重构（改进）
   - 验证 80%+ 覆盖率

3. **代码审查**
   - 编写代码后立即使用 **code-reviewer** 代理
   - 处理关键和高优先级问题
   - 尽可能修复中优先级问题

4. **提交和推送**
   - 详细的提交消息
   - 遵循约定式提交格式

---

## 代理编排

### 可用代理

| 代理 | 用途 | 使用场景 |
|-------|---------|-------------|
| planner | 实现规划 | 复杂功能、重构 |
| architect | 系统设计 | 架构决策 |
| tdd-guide | 测试驱动开发 | 新功能、错误修复 |
| code-reviewer | 代码审查 | 编写代码后 |
| security-reviewer | 安全分析 | 提交前 |
| build-error-resolver | 修复构建错误 | 构建失败时 |
| e2e-runner | 端到端测试 | 关键用户流程 |
| refactor-cleaner | 死代码清理 | 代码维护 |
| doc-updater | 文档更新 | 更新文档 |
| go-reviewer | Go 代码审查 | Go 项目 |
| go-build-resolver | Go 构建错误 | Go 构建失败 |
| database-reviewer | 数据库优化 | SQL、模式设计 |

### 即时代理使用

无需用户提示：
1. 复杂功能请求 - 使用 **planner** 代理
2. 刚编写/修改的代码 - 使用 **code-reviewer** 代理
3. 错误修复或新功能 - 使用 **tdd-guide** 代理
4. 架构决策 - 使用 **architect** 代理

---

## 性能优化

### 模型选择策略

**Haiku**（Sonnet 90% 的能力，3 倍成本节省）：
- 频繁调用的轻量级代理
- 结对编程和代码生成
- 多代理系统中的工作代理

**Sonnet**（最佳编码模型）：
- 主要开发工作
- 编排多代理工作流程
- 复杂编码任务

**Opus**（最深推理）：
- 复杂架构决策
- 最大推理需求
- 研究和分析任务

### 上下文窗口管理

避免在上下文窗口最后 20% 进行：
- 大规模重构
- 跨多个文件的功能实现
- 调试复杂交互

### 构建故障排查

如果构建失败：
1. 使用 **build-error-resolver** 代理
2. 分析错误消息
3. 逐步修复
4. 每次修复后验证

---

## 常见模式

### API 响应格式

```typescript
interface ApiResponse<T> {
  success: boolean
  data?: T
  error?: string
  meta?: {
    total: number
    page: number
    limit: number
  }
}
```

### 自定义 Hook 模式

```typescript
export function useDebounce<T>(value: T, delay: number): T {
  const [debouncedValue, setDebouncedValue] = useState<T>(value)

  useEffect(() => {
    const handler = setTimeout(() => setDebouncedValue(value), delay)
    return () => clearTimeout(handler)
  }, [value, delay])

  return debouncedValue
}
```

### 仓库模式

```typescript
interface Repository<T> {
  findAll(filters?: Filters): Promise<T[]>
  findById(id: string): Promise<T | null>
  create(data: CreateDto): Promise<T>
  update(id: string, data: UpdateDto): Promise<T>
  delete(id: string): Promise<void>
}
```

---

## OpenCode 特定说明

由于 OpenCode 不支持钩子，以下在 Claude Code 中自动化的操作必须手动执行：

### 编写/编辑代码后
- 运行 `prettier --write <file>` 格式化 JS/TS 文件
- 运行 `npx tsc --noEmit` 检查 TypeScript 错误
- 检查并移除 console.log 语句

### 提交前
- 手动运行安全检查
- 验证代码中无密钥
- 运行完整测试套件

### 可用命令

在 OpenCode 中使用这些命令：
- `/plan` - 创建实现计划
- `/tdd` - 强制 TDD 工作流程
- `/code-review` - 审查代码更改
- `/security` - 运行安全审查
- `/build-fix` - 修复构建错误
- `/e2e` - 生成端到端测试
- `/refactor-clean` - 移除死代码
- `/orchestrate` - 多代理工作流程

---

## 成功指标

成功标准：
- 所有测试通过（80%+ 覆盖率）
- 无安全漏洞
- 代码可读且可维护
- 性能可接受
- 满足用户需求
