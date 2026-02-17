# ECS Relation 文档完善计划

## TL;DR

> **目标**: 完善 ECS 框架中 Relation 系统的文档，填补当前仅 30 行简介的空白
> 
> **交付物**: 
> - 新建 `05-relation-system.md`（Relation 系统详解，约 300-400 行）
> - 更新 `01-core-concepts.md`（扩展 Relation 章节）
> - 更新 `02-patterns.md`（新增 Relation 模式）
> - 更新 `04-templates.md`（补充带数据 Relation 模板）
> 
> **预计工作量**: 中等（约 2-3 小时编写 + 1 小时审查）
> **并行执行**: NO（文档间有依赖关系）

---

## 上下文

### 原始需求
用户反馈 ESC 框架文档缺少 Relation 相关的使用和定义说明。

### 调研发现

**现有文档覆盖情况**:
| 文档 | Relation 内容 | 状态 |
|------|--------------|------|
| `01-core-concepts.md` 第 4 节 | 仅 17 行基础示例 | ⚠️ 过于简略 |
| `04-templates.md` T-004 | 仅 15 行定义模板 | ⚠️ 不完整 |
| **专门文档** | 不存在 | ❌ 缺失 |

**Relation 代码实现复杂度**:
- 核心定义: `Relation.kt` (value class, kind + target)
- 工厂方法: `Relations.kt` (10+ 个创建方法)
- 服务层: `RelationService.kt` (增删改查)
- 多种类型: 普通 Relation、Single Relation、Shared Component
- 内置类型: componentOf、sharedOf、childOf、instanceOf
- API: EntityCreateContext、EntityUpdateContext、EntityRelationContext

### Metis 审查反馈
1. **需要深化的内容**: 内置类型语义、componentOf vs 普通 Relation 区别
2. **潜在风险**: Single Relation 语义不清、Shared Component 生命周期
3. **遗漏方面**: Relation 与 Family 查询集成、Observer 事件、性能考虑

---

## 工作目标

### 核心目标
创建一份全面的 Relation 系统文档，使开发者能够：
1. 理解 Relation 与 Component/Tag 的本质区别
2. 掌握四种 Relation 类型的使用场景
3. 正确使用内置 Relation 类型（componentOf、sharedOf、childOf、instanceOf）
4. 应用 Relation 到实际业务场景（Ownership、Hierarchy、Prefab）

### 具体交付物
1. ✅ 新建 `docs/technology/ecs/05-relation-system.md`（主文档）
2. ✅ 更新 `docs/technology/ecs/01-core-concepts.md`（扩展第 4 节）
3. ✅ 更新 `docs/technology/ecs/02-patterns.md`（新增 Relation 模式章节）
4. ✅ 更新 `docs/technology/ecs/04-templates.md`（补充 T-004a 带数据 Relation）

### 定义完成标准
- [ ] 所有文档通过 `./gradlew build` 编译检查
- [ ] 文档中的代码示例可在项目中直接运行
- [ ] 现有文档的交叉引用已更新

### 必须包含的内容
- Relation 核心概念与架构设计
- 四种 Relation 类型详解（普通、Single、Shared、内置）
- 完整 API 参考（添加、删除、查询）
- 使用场景与业务示例
- 最佳实践与常见陷阱

### 明确排除的内容（Guardrails）
- ❌ 不修改 ECS 框架底层实现
- ❌ 不编写业务代码，仅提供文档
- ❌ 不覆盖已废弃的 API

---

## 验证策略

### 测试决策
- **Infrastructure exists**: YES（文档项目，无需测试框架）
- **Automated tests**: NO（文档无需单元测试）
- **Agent-Executed QA**: YES（人工审查验证）

### Agent-Executed QA Scenarios

每个文档任务完成后，执行以下验证：

**Scenario 1: 文档编译检查**
```
Tool: Bash
Steps:
  1. ./gradlew build
Expected Result: 构建成功，无错误
Failure Indicators: 编译失败、链接错误
Evidence: 构建输出日志
```

**Scenario 2: 代码示例验证**
```
Tool: 人工审查
Steps:
  1. 检查每个代码示例的语法正确性
  2. 验证引用的 API 存在且正确
  3. 检查导入路径正确
Expected Result: 所有示例可在项目中运行
Failure Indicators: 语法错误、API 不存在、导入错误
Evidence: 代码审查记录
```

**Scenario 3: 交叉链接验证**
```
Tool: Bash + 人工审查
Steps:
  1. 检查所有文档内链接是否有效
  2. 验证目录结构引用正确
Expected Result: 所有链接可正常访问
Failure Indicators: 404 链接、路径错误
Evidence: 链接检查报告
```

---

## 执行策略

### 顺序执行（Sequential）
由于文档间存在依赖关系，采用顺序执行：

```
Wave 1: 主文档
└── Task 1: 创建 05-relation-system.md

Wave 2: 更新现有文档（依赖 Wave 1）
├── Task 2: 更新 01-core-concepts.md
├── Task 3: 更新 02-patterns.md
└── Task 4: 更新 04-templates.md

Wave 3: 验证（依赖 Wave 2）
└── Task 5: 交叉引用和链接检查
```

### 依赖矩阵

| Task | Depends On | Blocks | Can Parallelize With |
|------|------------|--------|---------------------|
| 1 | None | 2, 3, 4 | None |
| 2 | 1 | 5 | 3, 4 |
| 3 | 1 | 5 | 2, 4 |
| 4 | 1 | 5 | 2, 3 |
| 5 | 2, 3, 4 | None | None |

### 代理分配建议

| Wave | Task | Recommended Agent |
|------|------|-------------------|
| 1 | 1 | category="writing", load_skills=["coding-standards"] |
| 2 | 2-4 | category="quick", load_skills=[] |
| 3 | 5 | category="quick", load_skills=[] |

---

## TODOs

---

### TODO-1: 创建 05-relation-system.md（Relation 系统详解）

**What to do**:
- [ ] 创建 `docs/technology/ecs/05-relation-system.md`
- [ ] 编写以下章节：
  1. **基础概念** - Relation 是什么、与 Component/Tag 的区别、设计原理
  2. **核心类型** - 普通 Relation、Single Relation、Shared Component、内置类型
  3. **完整 API** - EntityCreateContext（添加）、EntityUpdateContext（删除）、EntityRelationContext（查询）
  4. **使用场景** - Ownership、Hierarchy（父子）、Prefab（预制体）、Shared Component（全局配置）
  5. **进阶话题** - Family 查询 + Relation、Observer 事件、性能注意事项
  6. **最佳实践** - 命名规范、生命周期管理、常见陷阱
  7. **快速参考** - 速查表格式

**Must NOT do**:
- ❌ 不修改框架底层代码
- ❌ 不编写业务代码，仅提供文档
- ❌ 不覆盖已废弃 API

**Recommended Agent Profile**:
- **Category**: `writing`
  - Reason: 需要高质量的技术写作能力
- **Skills**: `coding-standards`
  - `coding-standards`: 确保代码示例符合项目规范

**Parallelization**:
- **Can Run In Parallel**: NO
- **Parallel Group**: Wave 1 (独立)
- **Blocks**: Task 2, 3, 4
- **Blocked By**: None

**References**:
- **Pattern References**:
  - `libs/lko-ecs/src/commonMain/kotlin/cn/jzl/ecs/relation/Relation.kt:11-27` - Relation 核心定义
  - `libs/lko-ecs/src/commonMain/kotlin/cn/jzl/ecs/relation/Relations.kt:9-19` - Relation 工厂方法
  - `libs/lko-ecs/src/commonMain/kotlin/cn/jzl/ecs/entity/EntityCreateContext.kt:16-74` - 添加 Relation API
  - `libs/lko-ecs/src/commonMain/kotlin/cn/jzl/ecs/entity/EntityUpdateContext.kt:9-50` - 删除 Relation API
  - `libs/lko-ecs/src/commonMain/kotlin/cn/jzl/ecs/entity/EntityRelationContext.kt:11-60` - 查询 Relation API
- **Test References**:
  - `libs/lko-ecs/src/commonTest/kotlin/cn/jzl/ecs/WorldTest.kt:77-250` - 使用示例（OwnerBy、childOf、instanceOf）
  - `libs/lko-ecs/src/commonTest/kotlin/cn/jzl/ecs/relation/RelationTest.kt:11-88` - Relation 单元测试
- **Documentation References**:
  - `docs/technology/ecs/01-core-concepts.md:89-106` - 现有 Relation 章节（需扩展）
  - `docs/technology/ecs/04-templates.md:66-83` - 现有 T-004 模板（需参考）

**Acceptance Criteria**:
- [ ] 文档创建成功：`docs/technology/ecs/05-relation-system.md` 存在
- [ ] 章节完整性：包含上述 7 个章节
- [ ] 代码示例：每个概念配有可运行的代码示例
- [ ] 字数要求：不少于 300 行（约 6000 字）
- [ ] 链接检查：所有交叉引用链接有效

**Agent-Executed QA Scenarios**:

```
Scenario: 文档编译检查
  Tool: Bash
  Steps:
    1. ./gradlew build
  Expected Result: 构建成功，无错误
  Failure Indicators: 编译失败
  Evidence: 构建日志

Scenario: 代码示例语法检查
  Tool: 人工审查
  Steps:
    1. 检查所有 Kotlin 代码块的语法
    2. 验证 API 引用存在
  Expected Result: 无语法错误，API 存在
  Failure Indicators: 语法错误、API 不存在
  Evidence: 代码审查记录

Scenario: 链接有效性检查
  Tool: Bash (grep + 验证)
  Steps:
    1. 提取所有内部链接 `[text](./file.md)`
    2. 验证目标文件存在
  Expected Result: 所有链接指向存在的文件
  Failure Indicators: 404 链接
  Evidence: 链接检查报告
```

**Commit**: NO（最终统一提交）

---

### TODO-2: 更新 01-core-concepts.md（扩展 Relation 章节）

**What to do**:
- [ ] 扩展第 4 节 "Relation（关系系统）"
- [ ] 增加与 Component/Tag 的对比表格
- [ ] 添加四种 Relation 类型的简要说明
- [ ] 更新到 05-relation-system.md 的链接

**Must NOT do**:
- ❌ 不删除现有内容，只扩展
- ❌ 不重复 05-relation-system.md 的详细内容

**Recommended Agent Profile**:
- **Category**: `quick`
  - Reason: 小型编辑任务
- **Skills**: []

**Parallelization**:
- **Can Run In Parallel**: YES (与 Task 3, 4 并行)
- **Parallel Group**: Wave 2
- **Blocks**: Task 5
- **Blocked By**: Task 1

**References**:
- **Documentation References**:
  - `docs/technology/ecs/01-core-concepts.md:89-106` - 现有内容
  - `docs/technology/ecs/05-relation-system.md` - 新文档（Task 1 产出）

**Acceptance Criteria**:
- [ ] 第 4 节扩展至 50+ 行
- [ ] 包含 Component/Tag/Relation 对比表格
- [ ] 添加四种类型简介
- [ ] 包含指向 05-relation-system.md 的链接

**Agent-Executed QA Scenarios**:

```
Scenario: 章节扩展验证
  Tool: 人工审查
  Steps:
    1. 检查第 4 节行数
    2. 验证对比表格存在
    3. 检查链接有效
  Expected Result: 满足所有标准
  Evidence: 审查记录
```

**Commit**: NO（最终统一提交）

---

### TODO-3: 更新 02-patterns.md（新增 Relation 模式章节）

**What to do**:
- [ ] 新增第 8 节 "Relation 模式"
- [ ] 包含以下模式：
  - 8.1 Ownership 模式（OwnerBy）
  - 8.2 Hierarchy 模式（Parent/Child）
  - 8.3 Prefab 模式（InstanceOf）
  - 8.4 Shared Component 模式（全局配置）

**Must NOT do**:
- ❌ 不修改现有 1-7 节
- ❌ 不深入实现细节，聚焦模式

**Recommended Agent Profile**:
- **Category**: `quick`
  - Reason: 模式文档编写
- **Skills**: []

**Parallelization**:
- **Can Run In Parallel**: YES (与 Task 2, 4 并行)
- **Parallel Group**: Wave 2
- **Blocks**: Task 5
- **Blocked By**: Task 1

**References**:
- **Documentation References**:
  - `docs/technology/ecs/02-patterns.md` - 现有文档
  - `docs/technology/ecs/05-relation-system.md` - 新文档（Task 1 产出）
- **Pattern References**:
  - `libs/lko-ecs/src/commonTest/kotlin/cn/jzl/ecs/WorldTest.kt:77-250` - 使用示例

**Acceptance Criteria**:
- [ ] 新增第 8 节，包含 4 个子章节
- [ ] 每个模式配有完整代码示例
- [ ] 章节总计 80+ 行

**Agent-Executed QA Scenarios**:

```
Scenario: 模式章节验证
  Tool: 人工审查
  Steps:
    1. 检查第 8 节结构
    2. 验证 4 个子章节存在
    3. 检查代码示例完整性
  Expected Result: 满足所有标准
  Evidence: 审查记录
```

**Commit**: NO（最终统一提交）

---

### TODO-4: 更新 04-templates.md（补充 T-004a 带数据 Relation）

**What to do**:
- [ ] 在 T-004 后新增 T-004a：带数据的 Relation
- [ ] 展示 `addRelation<K, T>(data)` 用法
- [ ] 提供实际业务场景示例

**Must NOT do**:
- ❌ 不修改现有 T-001 到 T-010
- ❌ 不添加过多理论，聚焦模板

**Recommended Agent Profile**:
- **Category**: `quick`
  - Reason: 模板编写
- **Skills**: []

**Parallelization**:
- **Can Run In Parallel**: YES (与 Task 2, 3 并行)
- **Parallel Group**: Wave 2
- **Blocks**: Task 5
- **Blocked By**: Task 1

**References**:
- **Documentation References**:
  - `docs/technology/ecs/04-templates.md:66-83` - 现有 T-004
  - `docs/technology/ecs/05-relation-system.md` - 新文档（Task 1 产出）
- **Pattern References**:
  - `libs/lko-ecs/src/commonMain/kotlin/cn/jzl/ecs/entity/EntityCreateContext.kt:21-28` - 带数据 Relation API

**Acceptance Criteria**:
- [ ] 新增 T-004a 模板
- [ ] 包含带数据 Relation 的定义和使用
- [ ] 提供完整可复制的代码

**Agent-Executed QA Scenarios**:

```
Scenario: 模板验证
  Tool: 人工审查
  Steps:
    1. 检查 T-004a 存在
    2. 验证代码可复制运行
    3. 检查快速参考表格更新
  Expected Result: 满足所有标准
  Evidence: 审查记录
```

**Commit**: NO（最终统一提交）

---

### TODO-5: 交叉引用和链接检查（最终验证）

**What to do**:
- [ ] 验证所有文档间的交叉引用链接
- [ ] 检查 `ecs-architecture.md` 的文档导航部分
- [ ] 验证 CHEATSHEET.md 是否需要更新
- [ ] 执行统一提交

**Must NOT do**:
- ❌ 不修改文档内容，只验证链接

**Recommended Agent Profile**:
- **Category**: `quick`
  - Reason: 验证任务
- **Skills**: []

**Parallelization**:
- **Can Run In Parallel**: NO
- **Parallel Group**: Wave 3 (最终)
- **Blocks**: None
- **Blocked By**: Task 2, 3, 4

**References**:
- **Documentation References**:
  - `docs/technology/ecs-architecture.md:70-81` - 文档导航部分
  - `docs/technology/ecs/CHEATSHEET.md` - 速查表

**Acceptance Criteria**:
- [ ] 所有内部链接有效
- [ ] `ecs-architecture.md` 导航部分已更新
- [ ] `CHEATSHEET.md` 已检查（如需更新则更新）
- [ ] 统一提交所有变更

**Agent-Executed QA Scenarios**:

```
Scenario: 链接全面检查
  Tool: Bash 脚本
  Steps:
    1. 提取所有文档中的链接
    2. 验证目标文件存在
    3. 统计有效/无效链接
  Expected Result: 100% 链接有效
  Failure Indicators: 任何无效链接
  Evidence: 链接检查报告

Scenario: 导航更新检查
  Tool: 人工审查
  Steps:
    1. 检查 ecs-architecture.md 第 70-81 行
    2. 验证新增 05-relation-system.md 的链接存在
    3. 检查 CHEATSHEET.md 完整性
  Expected Result: 导航完整
  Evidence: 审查记录
```

**Commit**: YES
- Message: `docs(ecs): 完善 Relation 系统文档

- 新增 05-relation-system.md：Relation 系统详解
- 更新 01-core-concepts.md：扩展 Relation 章节
- 更新 02-patterns.md：新增 Relation 模式
- 更新 04-templates.md：补充 T-004a 带数据 Relation
- 更新 ecs-architecture.md：添加新文档导航`
- Files: 
  - `docs/technology/ecs/05-relation-system.md` (新增)
  - `docs/technology/ecs/01-core-concepts.md`
  - `docs/technology/ecs/02-patterns.md`
  - `docs/technology/ecs/04-templates.md`
  - `docs/technology/ecs-architecture.md`
- Pre-commit: `./gradlew build` 通过

---

## Commit Strategy

| After Task | Message | Files | Verification |
|------------|---------|-------|--------------|
| 5 (统一提交) | `docs(ecs): 完善 Relation 系统文档` | 所有文档变更 | ./gradlew build |

---

## Success Criteria

### 最终检查清单
- [x] `05-relation-system.md` 已创建，内容完整（7 个章节，300+ 行）
- [x] `01-core-concepts.md` 第 4 节已扩展（50+ 行）
- [x] `02-patterns.md` 新增第 8 节（4 个子章节，80+ 行）
- [x] `04-templates.md` 新增 T-004a
- [x] `ecs-architecture.md` 导航部分已更新
- [x] 所有内部链接有效
- [x] `./gradlew build` 通过
- [x] 所有代码示例可在项目中运行
