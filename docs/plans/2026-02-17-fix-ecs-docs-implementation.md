# 修复 ECS 关系系统文档错误 实施计划

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 修正 ECS 文档中关于 Single Relation 的错误定义、过时的 API 签名以及缺失的 editor 上下文。

**Architecture:** 文档修复任务，确保文档与 `libs/lko-ecs` 源码逻辑一致。

**Tech Stack:** Markdown, Kotlin (ECS API)

---

### Task 1: 修复核心关系系统文档 (05-relation-system.md)

**Files:**
- Modify: `docs/technology/ecs/05-relation-system.md`

**Step 1: Read existing content**
Run: `Read /Users/yoca-676/Documents/projects/sect/docs/technology/ecs/05-relation-system.md`

**Step 2: Apply corrections**
- Update Single Relation definition to "Single-Target Constraint Relation".
- Add `editor { ... }` to modification examples.
- Correct `addRelation` and `getRelation` signatures.

**Step 3: Commit**
```bash
git add docs/technology/ecs/05-relation-system.md
git commit -m "docs: 修正 05-relation-system.md 中的 Single Relation 定义和 API 示例"
```

### Task 2: 修复核心概念文档 (01-core-concepts.md)

**Files:**
- Modify: `docs/technology/ecs/01-core-concepts.md`

**Step 1: Read existing content**
Run: `Read /Users/yoca-676/Documents/projects/sect/docs/technology/ecs/01-core-concepts.md`

**Step 2: Apply corrections**
- Fix relation classification.
- Emphasize `editor` context.

**Step 3: Commit**
```bash
git add docs/technology/ecs/01-core-concepts.md
git commit -m "docs: 修正 01-core-concepts.md 中的关系分类与 editor 上下文说明"
```

### Task 3: 修复快速入门与速查表 (00-quick-start.md & CHEATSHEET.md)

**Files:**
- Modify: `docs/technology/ecs/00-quick-start.md`
- Modify: `docs/technology/ecs/CHEATSHEET.md`

**Step 1: Read and modify 00-quick-start.md**
- Update code snippets to use `editor`.

**Step 2: Read and modify CHEATSHEET.md**
- Update all API examples for relations.

**Step 3: Commit**
```bash
git add docs/technology/ecs/00-quick-start.md docs/technology/ecs/CHEATSHEET.md
git commit -m "docs: 更新快速入门和速查表中的代码示例"
```

### Task 4: 更新 AI 助手引导 (AGENT.md)

**Files:**
- Modify: `docs/technology/ecs/AGENT.md`

**Step 1: Update AGENT.md guidelines**
- Ensure AI logic for Single Relation is correct.

**Step 2: Commit**
```bash
git add docs/technology/ecs/AGENT.md
git commit -m "docs: 同步 AGENT.md 中的关系系统引导规范"
```
