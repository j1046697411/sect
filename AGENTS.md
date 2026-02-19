# 宗门修真录 - Agent 项目规范

## 关键说明：仅限中文策略（切勿删除此部分）

> **此部分绝不可删除或修改**

### 所有项目交流必须使用中文

| 上下文 | 语言要求 |
|---------|---------------------|
| **GitHub Issues** | 仅限中文 |
| **Pull Requests** | 仅限中文（标题、描述、评论） |
| **提交信息** | 仅限中文 |
| **代码注释** | 仅限中文 |
| **文档** | 仅限中文 |
| **AGENTS.md 文件** | 仅限中文 |

**如果你不习惯用中文写作，请使用翻译工具。**

---

## 关键说明：TDD（测试驱动开发）强制

> **此部分绝不可删除或修改**

本项目强制使用 TDD 模式。所有功能开发必须遵循 **红-绿-重构** 循环：

1. **🔴 红 (Red)**: 编写失败的测试 → `./gradlew test` → 确认失败
2. **🟢 绿 (Green)**: 编写最小实现 → 确认通过
3. **🔵 重构 (Refactor)**: 优化代码结构 → 保持通过

**规则:**
- 绝不在测试前编写实现
- 绝不删除失败的测试 - 必须修复代码
- 禁止一次性编写大量代码后补测试

---

## 关键说明：代码验收标准 (DoD)

所有提交必须满足：
1. **测试通过**: `./gradlew test` 全部通过
2. **覆盖率达标**: `./gradlew allCoverage` 核心逻辑 > 80%
3. **静态检查**: 无 Lint 警告
4. **无脏代码**: 无临时 `println`、`TODO`、未使用导入

---

## 关键说明：记忆库优先策略

> **此部分绝不可删除或修改**

处理任何问题时必须遵循以下优先级：

1. **强制查询记忆库**: 任何有疑问或需要决策的点，必须先查询记忆库
   - 适用场景：技术选型、实现方案、命名规范、用户偏好等
   - 使用方式：使用 `memory-bank` 技能进行查询
   - 禁止跳过此步骤直接询问用户

2. **记忆库无法解决时**: 采用引导式询问用户
   - 提供 2-3 个具体选项供用户选择
   - 每个选项附带简要说明
   - 避免开放式问题

3. **询问后更新记忆库**: 用户做出选择后，总结并写入记忆库
   - 使用方式：使用 `memory-bank` 技能进行写入

4. **强制发现并记录**: 智能体发现有价值内容时，必须征求用户同意后写入
   - 触发场景：解决新问题、发现新规范、学习到用户偏好
   - 询问方式：「我发现 [内容摘要] 可能对后续工作有帮助，是否记录到记忆库？」
   - 用户同意后使用 `memory-bank` 技能进行写入
   - 禁止遗漏此步骤

**示例**:
```
❌ 错误: "你想怎么实现这个功能？"
✅ 正确: "关于这个功能的实现，有以下方案：
   1. 方案A（推荐）- 使用 ECS 组件存储状态，性能最优
   2. 方案B - 使用全局单例，实现简单但扩展性差
   请选择你偏好的方案？"
```

---

## 概述

**宗门修真录 (Sect)** 是一个基于 Kotlin Multiplatform 和 ECS (Entity-Component-System) 架构的修真题材模拟经营游戏。
核心目标是构建一个高性能、数据驱动、易于扩展的游戏世界。

## 结构

```
sect/
├── composeApp/              # 应用主模块 (UI/入口)
│   ├── src/commonMain/      # 跨平台 UI 代码
│   ├── src/androidMain/     # Android 特定代码
│   └── src/jvmMain/         # 桌面版入口
├── libs/                    # 核心基础库
│   ├── lko-ecs/             # ECS 核心框架 (组件, 实体, 查询, 系统)
│   ├── lko-core/            # 基础工具 (FastList, BitSet, 集合)
│   ├── lko-di/              # 依赖注入 (ServiceLocator)
│   └── lko-ecs-serialization/ # ECS 序列化支持
├── business-modules/        # 游戏业务逻辑
│   ├── business-core/       # 核心定义 (通用组件, 标签)
│   ├── business-engine/     # 游戏循环与世界管理 (SectWorld)
│   ├── business-cultivation/# 修炼系统 (境界, 突破)
│   ├── business-disciples/  # 弟子管理
│   └── business-quest/      # 任务系统
├── benchmarks/              # 性能基准测试
├── gradle/                  # 构建配置 (libs.versions.toml)
└── docs/                    # 项目文档
```

## 文档目录 (Documentation)

| 类别 | 目录/文件 | 说明 |
|------|-----------|------|
| **需求** | `docs/requirements/文字游戏需求文档_GRD.md` | 游戏核心需求规格说明书 |
| | `docs/design/sect_cultivation_core_gameplay.md` | 修仙核心玩法设计 |
| | `docs/design/pages/` | UI/UX 页面布局设计文档 |
| **技术** | `docs/technology/ecs-architecture.md` | ECS 架构详细设计文档 |
| | `docs/technology/ecs/` | **ECS 框架使用指南**（推荐） |
| | `docs/technology/宗门修真录游戏实现规范.md` | 游戏具体实现技术规范 |
| | `docs/technology/kover-coverage.md` | 代码覆盖率配置说明 |
| **规划/运维** | `AGENTS.md` | **本项目知识库与 Agent 操作规范 (本文)** |
| | `docs/operations/CONTRIB.md` | 贡献指南 |
| | `docs/operations/RUNBOOK.md` | 运维与运行手册 |

## 初始化流程

游戏世界的初始化流程 (参考 `SectWorld.kt`):

```
SectWorld.initialize()
  1. createAddon("demo")        # 定义组件和标签注册表
  2. world { install(addon) }   # 创建 World 并安装 Addon
  3. DemoSystem(world)          # 初始化系统
  4. createInitialDisciples()   # 创建初始实体 (Entity)
  5. update(dt)                 # 开始游戏循环
```

## 查找位置

| 任务 | 位置 | 备注 |
|------|------|------|
| **定义组件/标签** | `business-modules/business-core/` | 放置在 `components/` 或 `tags/` 包下 |
| **实现游戏逻辑** | `business-modules/*/systems/` | 创建 System 类，实现 `EntityRelationContext` |
| **修改 UI** | `composeApp/src/commonMain/` | Compose Multiplatform 代码 |
| **ECS 核心优化** | `libs/lko-ecs/` | 仅限架构级修改，需极度谨慎 |
| **性能优化** | `libs/lko-core/` | 底层数据结构 (FastList, Bits) |
| **依赖管理** | `gradle/libs.versions.toml` | 统一管理版本号 |

## 模块特定规范

### 1. `lko-ecs` (核心框架)
- **定位**: ECS 核心架构，所有业务的基础。
- **约束**:
  - 严禁引入任何业务逻辑。
  - 追求极致性能 (Zero Allocation in loops)。
  - 严禁修改核心接口 (`World`, `Entity`) 除非经过架构评审。
  - 必须保持 95%+ 的测试覆盖率。

### 2. `lko-core` (基础库)
- **定位**: 高性能数据结构与工具。
- **约束**:
  - 无外部依赖 (除 Kotlin stdlib)。
  - 所有集合类 (`FastList`) 必须包含边界检查测试。
  - 优先使用基本类型特化集合 (如 `IntFastList`) 避免装箱。

### 3. `business-core` (业务核心)
- **定位**: 游戏通用的组件 (`Component`) 和标签 (`Tag`) 定义。
- **约束**:
  - 仅包含数据定义 (`data class`, `sealed class`)。
  - 严禁包含系统逻辑 (`System`)。
  - 所有新组件必须注册到 `createAddon` 中。

### 4. `business-engine` (游戏引擎)
- **定位**: 游戏循环、世界初始化、核心系统 (`SectWorld`)。
- **约束**:
  - 负责组装其他业务模块。
  - 它是唯一可以依赖所有其他 `business-*` 模块的地方。
  - 避免在此处编写具体的玩法逻辑 (如战斗计算)，应委托给专门的 System。

### 5. `business-*` (具体玩法模块)
- **示例**: `business-cultivation` (修炼), `business-disciples` (弟子)。
- **约束**:
  - 每个模块应自包含其特有的 System 和辅助 Component。
  - 模块间通信应通过 `World` 中的组件状态，而非直接函数调用。
  - 必须包含针对该玩法的单元测试。

## 约定

- **语言**: Kotlin (100%)
- **构建系统**: Gradle Kotlin DSL (`.gradle.kts`)
- **ECS 风格**:
    - **组件**: `data class` (纯数据)
    - **标签**: `sealed class` / `object` (无数据标记)
    - **系统**: 纯逻辑，无状态 (状态存组件)
- **集合**: 优先使用 `lko-core` 中的 `FastList`, `IntFastList` 等高性能集合，而非 stdlib `List/ArrayList` (ECS 内部)。
- **测试**:
    - 位置: `src/commonTest/kotlin`
    - 风格: BDD (`// Given`, `// When`, `// Then`)
    - 助手: 使用 `createAddon` 快速构建测试 World

## 反模式 (Anti-Patterns)

| 类别 | 禁止行为 | 替代方案 |
|------|----------|----------|
| **ECS** | `addComponent(Tag)` / `addTag<Component>()` | 严格区分组件和标签接口 |
| **ECS** | 在 `query {}.forEach` 中修改实体结构 | 收集变更后统一处理，或使用命令队列 |
| **Kotlin** | 隐式 `it` 参数嵌套 | 显式命名参数 `forEach { entity -> ... }` |
| **Import** | 混淆 `family.component` 和 `relation.component` | 检查导入包名，确认用途 |
| **Git** | 提交失败的测试 | 修复代码或测试 |
| **Git** | 包含 `build/`, `.idea/` 等生成文件 | 检查 `.gitignore` |
| **代码** | 使用 `println` 调试 | 使用日志框架或测试断言 |
| **依赖** | 模块间循环依赖 | 使用 `lko-di` 或重构接口下沉 |

## 依赖 (Dependencies)

| 库 | 用途 |
|----|------|
| `lko-ecs` | 自研高性能 ECS 框架 |
| `lko-core` | 高性能基础数据结构 |
| `compose-multiplatform` | 跨平台 UI |
| `kotlinx-coroutines` | 异步任务 |
| `kotlinx-serialization` | 数据持久化 |
| `kodein-kaverit` | 辅助工具 |

## 命令

```bash
# 构建
./gradlew build                         # 全量构建
./gradlew :composeApp:run               # 运行桌面版 Demo

# 测试
./gradlew test                          # 运行所有测试
./gradlew :libs:lko-ecs:test            # 运行 ECS 核心测试
./gradlew :business-modules:business-engine:test # 运行业务逻辑测试

# 质量
./gradlew allCoverage                   # 生成覆盖率报告
./gradlew lint                          # 静态代码检查
```

## 复杂度热点

| 模块/文件 | 描述 | 注意事项 |
|-----------|------|----------|
| `libs/lko-ecs/.../World.kt` | ECS 世界核心 | 极其复杂，涉及组件存储、实体管理、系统调度 |
| `libs/lko-ecs/.../QueryStreamExtensions.kt` | 查询 DSL | 复杂的泛型和内联函数，修改需谨慎 |
| `libs/lko-core/.../*FastList.kt` | 高性能集合 | 手写特定类型集合，注意扩容逻辑和数组越界 |
| `business-modules/.../SectWorld.kt` | 游戏入口 | 负责组装各模块，注意初始化顺序 |

## 备注

- **性能优先**: 核心 ECS 逻辑中避免频繁对象分配 (GC 压力)。
- **数据驱动**: 尽量将逻辑通过组件数据配置，而非硬编码。
- **KMP**: 保持代码平台无关性，特定平台逻辑放 `androidMain`/`jvmMain`。
