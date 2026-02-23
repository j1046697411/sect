---
name: memory-bank
description: Use when need to store, retrieve, or update project knowledge. Automatically trigger at: (1) conversation start - search relevant memories, (2) problem solved - record solution, (3) user preference detected - save preference, (4) existing solution changed - update memory.
---

# 记忆库技能

记忆库是项目的自我进化知识库，存储和检索有价值的信息，帮助 AI 在后续对话中保持上下文连贯性。

## 存储位置

```
docs/memory-bank/
├── conventions/    # 技术规范与约定
├── solutions/      # 问题解决方案
├── preferences/    # 用户偏好
└── lessons/        # 错误教训
```

## 自动触发场景（强制）

### 1. 对话开始时 → 搜索记忆
```
触发: 用户提出新问题
动作: 搜索 docs/memory-bank/ 下相关记忆
```

### 2. 问题解决后 → 添加记忆
```
触发: 成功解决一个有价值的问题
动作: 询问用户是否记录，添加到 solutions/
```

### 3. 发现偏好时 → 保存偏好
```
触发: 用户明确表达偏好
动作: 添加到 preferences/
示例: "我喜欢简洁回复" / "不要解释基础概念"
```

### 4. 方案变化时 → 更新记忆
```
触发: 发现已有记忆过时或有误
动作: 标记旧记忆为 deprecated，添加更新版本
```

## 智能检索系统

### 分类优先级映射
根据问题类型，优先搜索对应分类：

| 问题特征 | 优先分类 | 示例 |
|---------|---------|------|
| "怎么解决" / "如何实现" / "bug" / "修复" | lessons → solutions | 时间不流逝怎么修 |
| "规范" / "约定" / "标准" / "应该" | conventions | 命名应该用什么 |
| "我喜欢" / "偏好" / "风格" | preferences | 我喜欢简洁回复 |
| "设计" / "机制" / "系统" | conventions → solutions | 战斗系统怎么设计的 |

### 多维度搜索策略
```
1. 分类筛选 → 根据问题特征选择 1-2 个分类
2. 标签匹配 → 搜索 **标签** 字段中的关键词
3. 标题匹配 → 搜索 ## 标题 中的关键词
4. 内容匹配 → 搜索正文中的关键词
5. 关联查找 → 从匹配记忆的"关联记忆"扩展
```

### 搜索命令示例
```bash
# 按分类+关键词搜索
grep -r "关键词" docs/memory-bank/lessons/ docs/memory-bank/solutions/

# 按标签搜索
grep -r "#角色系统" docs/memory-bank/

# 按标题搜索
grep -r "^## .*关键词" docs/memory-bank/

# 组合搜索（分类优先）
grep -r "#ecs" docs/memory-bank/solutions/ docs/memory-bank/lessons/
```

### 同义词映射
| 用户输入 | 映射搜索词 |
|---------|-----------|
| bug / 问题 / 错误 | lessons, #bug, #错误 |
| 优化 / 性能 | solutions, #性能, #优化 |
| 设计 / 机制 | conventions, solutions, #系统, #机制 |
| 角色 / 弟子 | #角色系统, #弟子 |
| 经济 / 灵石 | #经济系统, #灵石 |

## 记忆分类

| 分类 | 用途 | 示例 |
|------|------|------|
| `conventions` | 技术规范与约定 | 项目强制使用中文 |
| `solutions` | 问题解决方案 | ECS Query 性能优化 |
| `preferences` | 用户偏好 | 偏好简洁回复 |
| `lessons` | 错误教训 | 避免在 Query 中修改实体 |

## 核心操作

### 搜索记忆

```bash
# 搜索关键词
grep -r "关键词" docs/memory-bank/

# 搜索特定分类
grep -r "关键词" docs/memory-bank/solutions/

# 列出所有记忆
find docs/memory-bank -name "*.md" -type f
```

### 添加记忆

**步骤：**
1. 确定分类（conventions/solutions/preferences/lessons）
2. 生成文件名：`{简洁标题}.md`（小写、连字符）
3. 使用模板创建文件
4. 询问用户确认后写入

**文件命名规范：**
```
solutions/ecs-query-性能优化.md
conventions/宗门修真录术语规范.md
preferences/用户偏好简洁回复.md
```

### 更新记忆

**步骤：**
1. 找到目标记忆文件
2. 在文件头部添加 `> 状态: deprecated` 和 `> 废弃原因:`
3. 创建新的记忆文件
4. 在新文件中添加 `> 替代: 旧记忆ID`

### 浏览记忆

```bash
# 列出所有记忆
find docs/memory-bank -name "*.md" -type f | sort

# 查看特定分类
ls -la docs/memory-bank/solutions/

# 读取记忆内容
cat docs/memory-bank/solutions/ecs-query-性能优化.md
```

## 记忆格式模板

```markdown
## [简洁标题]

> 创建时间: YYYY-MM-DD
> 分类: [conventions|solutions|preferences|lessons]
> 状态: [active|deprecated|archived]
> 标签: #标签1 #标签2

**问题/背景**:
[清晰描述问题或背景]

**解决方案/内容**:
[具体解决方案或详细内容]

**示例**（可选）:
[代码示例或使用示例]

**关联记忆**: #相关记忆ID
```

## 完整示例

### 添加解决方案

```markdown
## ECS Query 性能优化

> 创建时间: 2024-01-15
> 分类: solutions
> 状态: active
> 标签: #ecs #性能 #优化

**问题/背景**:
在遍历大量实体时，query().forEach 循环中出现频繁的对象分配，导致 GC 压力过大。

**解决方案/内容**:
1. 使用 FastList 替代标准 List
2. 在循环外预分配临时对象
3. 避免在循环中创建 lambda

**示例**:
```kotlin
// ❌ 错误：循环中创建对象
query.forEach { ctx ->
    val temp = SomeObject()  // 每次创建
}

// ✅ 正确：循环外预分配
val temp = SomeObject()
query.forEach { ctx ->
    temp.reset()
}
```

**关联记忆**: #solutions:ecs-query-基础用法
```

### 添加用户偏好

```markdown
## 用户偏好简洁回复

> 创建时间: 2024-01-15
> 分类: preferences
> 状态: active
> 标签: #偏好 #简洁

**问题/背景**:
用户明确表示喜欢简洁直接的回复风格。

**解决方案/内容**:
- 优先给出解决方案，避免冗长解释
- 使用代码示例代替文字描述
- 不解释基础概念，除非用户询问
- 回复控制在 4 行以内（除代码外）

**示例触发语句**:
- "我喜欢简洁的回复"
- "直接给我代码"
- "不要解释那么多"
```

### 标记过时记忆

```markdown
## 旧方案标题

> 创建时间: 2024-01-01
> 分类: solutions
> 状态: deprecated
> 废弃原因: 项目升级后 API 变化，此方案不再适用
> 替代方案: solutions:新方案标题
> 标签: #旧标签

**问题/背景**:
[保留原内容]

**解决方案/内容**:
[保留原内容，但标记为过时]

---
⚠️ **此记忆已过时，请使用新方案**
```

## 使用检查清单

### 添加记忆前
- [ ] 确定合适的分类
- [ ] 检查是否已存在类似记忆
- [ ] 准备好简洁的标题
- [ ] 准备好相关标签

### 添加记忆时
- [ ] 使用标准模板格式
- [ ] 填写所有必要字段
- [ ] 添加相关标签提高可搜索性
- [ ] 关联相关记忆（如有）

### 更新记忆时
- [ ] 先标记旧记忆为 deprecated
- [ ] 在旧记忆中说明废弃原因
- [ ] 创建新的记忆文件
- [ ] 在新记忆中引用旧记忆

## 最佳实践

1. **及时记录** - 问题解决后立即记录，避免遗忘
2. **分类准确** - 选择正确的分类便于检索
3. **标题简洁** - 使用简短明确的标题
4. **标签完整** - 添加足够的标签提高搜索命中率
5. **建立关联** - 将相关记忆关联起来形成知识网络
6. **定期清理** - 标记过时记忆保持记忆库整洁

## 注意事项

- 记忆文件使用 UTF-8 编码
- 文件名使用小写字母和连字符
- 添加记忆前先征求用户同意
- 不要覆盖已有记忆，而是创建新版本
- deprecated 状态的记忆保留但不再推荐使用
