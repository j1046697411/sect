# 设计文档：修复 ECS 关系系统相关文档错误

## 1. 背景
在对 `lko-ecs` 框架的文档进行审计时，发现 `Single Relation` (单目标关系) 的定义存在根本性错误，且大量 API 示例缺失必要的 `editor` 上下文保护，这将严重误导开发者。

## 2. 发现的错误清单

### 2.1 Single Relation 定义错误
- **错误描述**：文档将 `Single Relation` 描述为“无目标的 Tag-like 关系”。
- **实际逻辑**：`Single Relation` 是**单目标约束关系**。通过在注册时调用 `singleRelation()` 声明，确保该类型的关系在同一实体上只能有一个目标。当添加新目标时，旧目标会自动移除。

### 2.2 API 签名与用法错误
- **错误描述**：示例中混淆了 `getRelation<K, T>()` 和 `addRelation<K>(target, data)` 的泛型与参数。
- **实际逻辑**：
    - `addRelation<K>(target)`: 添加无数据关系。
    - `addRelation<K>(target, data)`: 添加带数据关系。
    - `getRelation<K>(target)`: 获取指定目标的关系数据。
    - `getRelation<K, T>()`: 仅适用于 Single Relation 或确定只有一个关系的场景获取数据。

### 2.3 缺失 editor 上下文
- **错误描述**：大量示例直接在 `entity` 上调用 `addRelation` 或 `addTag`。
- **实际逻辑**：根据框架要求，所有修改实体结构的操作必须在 `entity.editor { ... }` 块内执行。

## 3. 修复方案

### 3.1 更新核心文档 (05-relation-system.md)
- 重写 `Single Relation` 章节，解释“单目标约束”和“自动覆盖”机制。
- 更新所有代码块，确保使用 `editor { ... }`。
- 修正带数据关系的 API 说明。

### 3.2 更新基础文档 (01-core-concepts.md)
- 修正关系分类（普通多对一 vs 单目标一对一）。
- 强化 `editor` 上下文的概念说明。

### 3.3 更新速查表 (CHEATSHEET.md)
- 更正关系操作表格中的所有示例代码。

### 3.4 同步更新 AGENT.md 与 00-quick-start.md
- 确保 AI 助手引导和快速入门指南中的代码片段 100% 准确。

## 4. 验收标准
- [ ] 所有提及 `Single Relation` 的地方均定义为“单目标约束关系”。
- [ ] 所有涉及修改的操作示例均包含 `editor { ... }`。
- [ ] API 签名（泛型与参数）与 `lko-ecs` 源码保持一致。
