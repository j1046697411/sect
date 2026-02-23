---
name: memory-bank
description: Use when need to store, retrieve, or update project knowledge. Automatically trigger at: (1) conversation start - search relevant memories, (2) problem solved - auto record, (3) user preference detected - auto save, (4) existing solution changed - auto update.
---

# 记忆库技能

记忆库是项目的自我进化知识库，**全自动**存储、检索和维护知识。

## 存储位置

```
docs/memory-bank/
├── index.md          ← 唯一索引（自动生成）
├── conventions/      # 技术规范与约定
├── solutions/        # 问题解决方案
├── lessons/          # 错误教训
└── preferences/      # 用户偏好
```

---

## 索引维护

**索引文件**：`docs/memory-bank/index.md`

**自动生成**：
```bash
python3 scripts/generate-memory-index.py
```

**触发时机**：
- 新增记忆后
- 修改记忆标签后
- 删除记忆后

---

## 全自动触发场景

### 1. 对话开始 → 自动搜索
```
触发: 用户提出新问题
动作: 
  1. 读取 index.md 获取系统域索引
  2. 根据关键词定位相关分类
  3. 加载相关记忆到上下文
输出: 静默执行
```

### 2. 问题解决 → 自动记录
```
触发: 成功解决一个技术问题/Bug/设计决策
动作:
  1. 分析问题类型，确定分类
  2. 生成简洁标题（5 字以内）
  3. 提取系统域标签 + 类型标签
  4. 写入文件
  5. 运行脚本更新索引
输出: "已记录: [标题]"
```

### 3. 偏好发现 → 自动保存
```
触发: 用户表达偏好（"我喜欢" / "不要" / "偏好"）
动作:
  1. 提取偏好内容
  2. 写入 preferences/
  3. 运行脚本更新索引
输出: 静默执行
```

### 4. 记忆过时 → 自动更新
```
触发: 发现已有记忆与新情况矛盾
动作:
  1. 标记旧记忆状态为 deprecated
  2. 创建新版本记忆
  3. 建立关联关系
  4. 运行脚本更新索引
输出: "已更新: [标题]"
```

---

## 自动分类算法

根据内容关键词自动判断分类：

| 关键词特征 | 分类 | 示例 |
|-----------|------|------|
| "应该" / "必须" / "规范" / "约定" | conventions | 组件应该使用 value class |
| "解决" / "实现" / "方案" / "优化" | solutions | ECS Query 性能优化 |
| "喜欢" / "偏好" / "风格" / "不要" | preferences | 我喜欢简洁回复 |
| "错误" / "bug" / "陷阱" / "避免" / "教训" | lessons | Query 中修改实体陷阱 |

---

## 自动标签生成

### 系统域标签（必选一个）
| 内容关键词 | 标签 |
|-----------|------|
| 角色/弟子/掌门/长老 | #角色系统 |
| 经济/灵石/贡献点 | #经济系统 |
| 战斗/伤害 | #战斗系统 |
| 功法/技能 | #功法系统 |
| 设施/建筑 | #设施系统 |
| 可玩性/政策/玩家 | #可玩性 |
| ECS/Entity/Component/Query | #ecs |
| Kotlin/协程/多平台 | #kotlin |
| Compose/UI | #compose |
| 测试/TDD/BDD | #测试 |

### 类型标签（必选一个）
| 分类 | 标签 |
|------|------|
| conventions | #规范 |
| solutions | #方案 |
| preferences | #偏好 |
| lessons | #陷阱 |

---

## 记忆格式模板

```markdown
## {简洁标题}

> 创建时间: YYYY-MM-DD
> 分类: [conventions|solutions|preferences|lessons]
> 状态: [active|deprecated]
> 标签: #系统域 #类型

**问题/背景**:
[一句话描述]

**解决方案/内容**:
[核心内容]

**关联记忆**: #分类:标题
```

---

## 命名规范

### 标题命名
- **简洁**：控制在 5 字以内
- **可读**：纯中文，避免中英混杂
- **可搜索**：关键词前置

### 示例
| 原标题 | 新标题 |
|--------|--------|
| 角色系统设计角色层级系统 | 角色层级 |
| 游戏可玩性设计ai行为信息分级 | 信息分级 |
| ecs-query-性能优化 | Query性能优化 |

---

## 工作流

### 创建记忆
```
1. 分析内容 → 确定分类
2. 提取关键词 → 生成标签
3. 生成文件名 → 写入文件
4. 运行脚本 → 更新索引
5. git commit → 提交变更
```

### 搜索记忆
```
1. 读取 index.md
2. 按系统域或分类定位
3. 加载相关记忆
```

### 更新记忆
```
1. 修改文件内容
2. 标记状态为 deprecated（如需要）
3. 运行脚本 → 更新索引
4. git commit
```

---

## 注意事项

- 所有操作后必须运行 `python3 scripts/generate-memory-index.py` 更新索引
- 标题控制在 5 字以内
- 标签格式：#系统域 #类型（两个标签）
- 每次变更必须 git commit
