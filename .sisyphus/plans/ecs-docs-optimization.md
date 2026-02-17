# ECS 文档优化工作规划

## 背景

当前 ECS 框架文档 (`docs/technology/ecs-architecture.md`) 存在以下问题：

1. **过于冗长**: 单文件包含 1800+ 行，难以快速查找
2. **概念混杂**: 基础概念、API 参考、代码模板混在一起
3. **缺少实战指导**: 理论多，从实际测试/业务代码提取的模式少
4. **决策不清晰**: AI 面对需求时难以快速映射到正确模式

## 优化目标

1. **分层文档**: 将单文件拆分为 5 个分层文档
2. **模式驱动**: 基于测试代码提取 15+ 个实战模式
3. **决策清晰**: 建立明确的决策树和场景速查表
4. **快速上手**: 5 分钟快速开始文档覆盖 80% 使用场景

## 参考数据

基于对 `libs/lko-ecs/src/commonTest/kotlin/cn/jzl/ecs/` 19 个测试文件的分析：

### 已识别的关键模式

**组件定义模式** (4 种):
- data class Component (多属性)
- @JvmInline value class (单属性，性能优化)
- sealed class Tag (状态标记)
- createAddon { componentId<T>() } (注册方式)

**实体创建模式** (6 种):
- 空实体: `world.entity { }`
- 单组件: `world.entity { it.addComponent(...) }`
- 多组件+标签: 链式 addComponent + addTag
- 带关系: `addRelation<RelationType>(target)`
- 子实体: `parent.childOf { }`
- 实例化: `prefab.instanceOf { }`

**查询模式** (6 种):
- 基本查询: `world.query { Context(world) }`
- 多组件查询: QueryContext 中声明多个 `by component()`
- 标签过滤: `entity.hasTag<Tag>()`
- 条件过滤: `.filter { ... }`
- 遍历处理: `.forEach { ... }` 或 `.collect { ... }`
- 族查询: `world.familyService.family { }`

**修改模式** (5 种):
- 更新组件: `entity.editor { it.addComponent(copy(...)) }`
- 批量添加: editor 中链式 add
- 移除组件: `removeComponent<T>()`
- 移除标签: `removeTag<T>()`
- 服务配置: `entityService.configure()`

## 工作分解

### 阶段 1: 创建新文档结构

#### 任务 1.1: 创建文档目录
- **操作**: `mkdir -p docs/technology/ecs/`
- **验证**: 目录存在

#### 任务 1.2: 编写 00-quick-start.md
- **内容**:
  - 30 秒理解 ECS (Entity/Component/System)
  - 1 分钟决策树
  - 3 分钟四大操作模式 (定义/创建/查询/修改)
  - 1 分钟完整示例
  - 快速参考卡表格
- **长度**: 约 150 行
- **验收**: 包含所有基础使用场景的代码示例

#### 任务 1.3: 编写 01-core-concepts.md
- **章节**:
  - Entity (实体生命周期)
  - Component (3 种类型详解)
  - Tag (标记系统)
  - Relation (关系系统)
  - World (世界容器)
  - QueryContext vs EntityRelationContext (关键区别)
- **长度**: 约 200 行
- **验收**: 每个概念有定义、示例、常见误区

#### 任务 1.4: 编写 02-patterns.md
- **章节**:
  - 组件设计模式 (原子化、value class 使用)
  - Factory 模式 (实体创建)
  - Service 模式 (业务逻辑)
  - Query 模式 (过滤、缓存)
  - Batch 模式 (批量操作)
  - Observer 模式 (事件监听)
- **长度**: 约 250 行
- **验收**: 每个模式有场景、代码、最佳实践

#### 任务 1.5: 编写 03-anti-patterns.md
- **反模式**:
  - 在 query 中直接修改 (ConcurrentModificationException)
  - 忘记注册 ComponentId
  - 混合职责的大组件
  - 单属性使用 data class
  - 直接修改组件属性 (不使用 copy)
  - Service 中保存状态
- **长度**: 约 150 行
- **验收**: 每个反模式有错误示例、原因、正确做法

#### 任务 1.6: 编写 04-templates.md
- **模板**:
  - T-001: 创建实体
  - T-002: 定义 Component
  - T-003: 定义 Tag
  - T-004: 定义 Relation
  - T-005: 创建 Service
  - T-006: 创建 Factory
  - T-007: 基本查询
  - T-008: 条件查询
  - T-009: 批量更新
  - T-010: 设置 Observer
- **长度**: 约 300 行
- **验收**: 每个模板可直接复制使用

#### 任务 1.7: 编写 CHEATSHEET.md
- **内容**:
  - 一页纸速查表
  - 操作 ↔ 代码 对照表
  - 决策流程图 (文本版)
  - 常见 import 列表
- **长度**: 约 80 行
- **验收**: 可打印放在手边参考

### 阶段 2: 重构主文档

#### 任务 2.1: 重构 ecs-architecture.md
- **新结构**:
  - 简介 (保持)
  - 角色定义 (简化)
  - 任务边界 (简化)
  - 文档导航 (新增: 指向 7 个子文档)
  - 快速决策 (新增: 3 步决策流程)
  - 不再包含详细 API 和模板
- **长度**: 从 1800 行缩减到约 150 行
- **验收**: 作为索引文档，所有详细内容指向子文档

### 阶段 3: 创建代码示例

#### 任务 3.1: 提取测试代码示例
基于分析结果，创建可运行的代码示例：
- `examples/entity-creation.kt`
- `examples/component-patterns.kt`
- `examples/query-patterns.kt`
- `examples/batch-operations.kt`

#### 任务 3.2: 创建完整游戏示例
创建一个简化的完整示例：
- `examples/complete-game-example.kt`
- 演示：定义组件 → 创建实体 → 查询 → 修改的完整流程

## 文件清单

### 新建文件 (7 个)

```
docs/technology/ecs/
├── 00-quick-start.md          # 5分钟快速开始
├── 01-core-concepts.md        # 核心概念详解
├── 02-patterns.md             # 常见模式
├── 03-anti-patterns.md        # 反模式
├── 04-templates.md            # 代码模板
├── CHEATSHEET.md              # 一页纸速查表
└── examples/                  # 代码示例目录
    ├── entity-creation.kt
    ├── component-patterns.kt
    ├── query-patterns.kt
    ├── batch-operations.kt
    └── complete-game-example.kt
```

### 修改文件 (1 个)

```
docs/technology/ecs-architecture.md   # 重构为索引文档
```

## 质量标准

### 文档规范
- 所有代码示例必须通过语法检查
- 每个概念必须有 ✅ 正确示例和 ❌ 错误示例
- 文件路径使用相对路径
- 中文撰写，保持与项目一致

### 内容验证
- 所有代码模式基于实际测试代码提取
- 反模式基于常见测试失败场景
- 决策树覆盖 90%+ 使用场景

## 预期收益

1. **AI 使用效率**: 从阅读 1800 行到阅读 150 行快速开始
2. **错误减少**: 明确的反模式清单减少常见错误
3. **开发速度**: 可直接复制的代码模板加速开发
4. **维护性**: 分层结构便于单独更新某部分

## 执行命令

执行此规划：
```bash
/start-work ecs-docs-optimization
```

---

**规划创建时间**: 2026-02-16
**基于分析**: libs/lko-ecs/src/commonTest/kotlin/cn/jzl/ecs/ (19 个测试文件)
**预计工作量**: 中等 (约 4-6 小时)
