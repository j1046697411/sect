---
name: memory-bank
description: Use when need to store, retrieve, or update project knowledge. Automatically trigger at: (1) conversation start - search relevant memories, (2) problem solved - auto record, (3) user preference detected - auto save, (4) existing solution changed - auto update.
---

# 记忆库技能（全自动版）

记忆库是项目的自我进化知识库，**全自动**存储、检索和维护知识，无需用户确认。

## 存储位置

```
docs/memory-bank/
├── conventions/    # 技术规范与约定
├── solutions/      # 问题解决方案
├── preferences/    # 用户偏好
└── lessons/        # 错误教训
```

---

## 全自动触发场景

### 1. 对话开始 → 自动搜索
```
触发: 用户提出新问题
动作: 
  1. 分析问题特征，选择优先分类
  2. 多维度搜索相关记忆
  3. 自动加载相关记忆到上下文
输出: 静默执行，不输出搜索过程
```

### 2. 问题解决 → 自动记录
```
触发: 成功解决一个技术问题/Bug/设计决策
动作:
  1. 自动分析问题类型
  2. 自动生成标题、标签、分类
  3. 自动查找相关记忆建立关联
  4. 直接写入文件，更新 _stats.json
输出: 仅在完成后简短提示 "已记录: [标题]"
```

### 3. 偏好发现 → 自动保存
```
触发: 用户表达偏好（"我喜欢" / "不要" / "偏好"）
动作:
  1. 提取偏好内容
  2. 自动写入 preferences/
输出: 静默执行
```

### 4. 记忆过时 → 自动更新
```
触发: 发现已有记忆与新情况矛盾
动作:
  1. 标记旧记忆为 deprecated
  2. 创建新版本记忆
  3. 建立替代关系
输出: 仅提示 "已更新: [标题]"
```

---

## 自动分类算法

根据内容关键词自动判断分类：

| 关键词特征 | 分类 | 示例 |
|-----------|------|------|
| "应该" / "必须" / "规范" / "约定" / "标准" | conventions | 组件应该使用 value class |
| "解决" / "实现" / "方案" / "优化" | solutions | ECS Query 性能优化方案 |
| "喜欢" / "偏好" / "风格" / "不要" | preferences | 我喜欢简洁回复 |
| "错误" / "bug" / "陷阱" / "避免" / "教训" | lessons | Query 中修改实体的陷阱 |
| "设计" / "机制" / "系统" | conventions → solutions | 战斗系统设计 |

---

## 自动标签生成

根据内容自动提取标签：

### 技术标签
- 内容含 ECS/Entity/Component/Query → #ecs
- 内容含 Kotlin/协程/多平台 → #kotlin
- 内容含 Compose/UI → #compose
- 内容含 测试/TDD/BDD → #测试

### 系统标签
- 内容含 角色/弟子/掌门 → #角色系统
- 内容含 经济/灵石/贡献点 → #经济系统
- 内容含 战斗/伤害 → #战斗系统
- 内容含 功法/技能 → #功法系统
- 内容含 设施/建筑 → #设施系统

### 类型标签
- conventions → #规范
- solutions → #方案
- preferences → #偏好
- lessons → #陷阱

---

## 自动关联算法

新记忆创建时，自动搜索并关联：

```
1. 同分类相似标题 → 关联度 +2
2. 标签重叠 ≥ 2个 → 关联度 +1
3. 内容关键词重叠 ≥ 3个 → 关联度 +1
4. 关联度 ≥ 2 → 建立双向关联
```

### 关联类型
- **补充关系**: conventions ↔ solutions（规范与实现）
- **因果关系**: lessons → solutions（教训与修复）
- **扩展关系**: 同系统的多个设计文档

---

## 智能检索系统

### 分类优先级
| 问题特征 | 优先分类 |
|---------|---------|
| "怎么解决" / "bug" / "修复" | lessons → solutions |
| "规范" / "应该" | conventions |
| "我喜欢" / "偏好" | preferences |
| "设计" / "机制" | conventions → solutions |

### 多维度搜索
```
1. 分类筛选 → 根据问题特征选择分类
2. 标签匹配 → grep "#标签"
3. 标题匹配 → grep "^## .*关键词"
4. 内容匹配 → grep "关键词"
5. 关联扩展 → 从匹配记忆的"关联记忆"查找
```

### 同义词映射
| 用户输入 | 映射 |
|---------|------|
| bug/问题/错误 | lessons, #陷阱 |
| 优化/性能 | solutions, #性能 |
| 设计/机制 | conventions, #系统 |
| 角色/弟子 | #角色系统 |
| 经济/灵石 | #经济系统 |

---

## 记忆格式模板

```markdown
## [简洁标题]

> 创建时间: YYYY-MM-DD
> 分类: [conventions|solutions|preferences|lessons]
> 状态: [active|deprecated]
> 标签: #标签1 #标签2

**问题/背景**:
[描述]

**解决方案/内容**:
[内容]

**示例**（可选）:
[代码]

**关联记忆**: #分类:相关记忆标题
```

---

## 全自动工作流

### 创建记忆（全自动）
```
1. 分析内容 → 确定分类
2. 提取关键词 → 生成标签
3. 搜索相似记忆 → 建立关联
4. 生成文件名 → 写入文件
5. 更新 _stats.json → 添加关联关系
6. git commit → 提交变更
```

### 搜索记忆（全自动）
```
1. 分析问题 → 确定优先分类
2. 多维度搜索 → 收集候选
3. 按相关度排序 → 取 Top 5
4. 静默加载 → 到上下文
```

### 更新记忆（全自动）
```
1. 标记旧记忆 → deprecated + 废弃原因
2. 创建新记忆 → 关联旧记忆
3. 更新 _stats.json → 维护关联
4. git commit → 提交变更
```

---

## 定期维护

### 自动清理（每周）
- 检测 deprecated 超过 30 天的记忆
- 检测使用次数为 0 超过 60 天的记忆
- 生成清理建议报告

### 关联优化（每周）
- 检测孤立记忆（无关联）
- 分析内容相似性，建议新关联
- 更新 _stats.json

---

## 文件更新规范

每次记忆变更时，同步更新：

### _stats.json
```json
{
  "usage": { "分类:文件名.md": 使用次数 },
  "relations": { "分类:文件名.md": ["关联记忆列表"] },
  "updated": "时间戳"
}
```

### Git 提交
```bash
git add docs/memory-bank/
git commit -m "memory: [操作] [标题]"
```

---

## 示例：全自动记录流程

**场景**: 修复了一个 Compose 初始化时序 bug

```
1. 检测到问题解决
2. 分析内容：
   - 关键词: Compose, 初始化, LaunchedEffect, remember
   - 分类: lessons（是错误教训）
   - 标签: #compose #初始化 #时序 #陷阱
3. 搜索相似记忆：
   - 找到 "compose初始化时序问题" → 已存在，跳过
4. 若不存在则创建：
   - 文件: lessons/compose初始化时序问题.md
   - 关联: #conventions:kotlin多平台开发规范
5. 更新 _stats.json
6. git commit
7. 输出: "已记录: compose初始化时序问题"
```

---

## 注意事项

- 所有操作全自动，无需用户确认
- 记录后仅简短提示，不打断工作流
- 定期汇总变更报告给用户
- 避免重复记录相同内容
