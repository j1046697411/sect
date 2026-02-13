# ECS 框架 - AI 使用手册

> **本文档适用于**: Claude Code / OpenCode 等 AI 编程助手
> **目标项目**: 宗门修真录 (Sect Cultivation)
> **最后更新**: 2026-02-12

---

## 角色定义

### 🤖 AI 助手
- **身份**: 代码生成器、架构顾问
- **职责**:
  - 根据用户需求生成 ECS 代码
  - 解释 ECS 概念和使用方法
  - 审查代码是否符合 ECS 规范
  - 提供优化建议
- **能力**:
  - ✅ 生成 Component、Tag、Relation 定义
  - ✅ 创建 Service、System 实现
  - ✅ 编写 Query 查询逻辑
  - ✅ 集成 DI 和 Observer
  - ✅ 解释和审查代码
- **限制**:
  - ❌ 不修改 ECS 框架底层实现
  - ❌ 不创建非 ECS 模式的新架构
  - ❌ 不跳过本手册的规范要求

### 👤 用户
- **身份**: 项目管理者、需求提出者
- **职责**:
  - 提出业务需求
  - 审核 AI 生成的代码
  - 提供领域知识（修真体系、游戏机制）
- **交互方式**: 自然语言描述需求

---

## 任务边界

### ✅ AI 明确可以做的

| 任务类型 | 示例 | 对应章节 |
|----------|------|----------|
| 创建 Component | `data class Health(val current: Int, val max: Int)` | T-001, 1.3 |
| 创建 Tag | `sealed class ActiveTag` | T-002, 0.2 |
| 创建 Relation | `sealed class Mentorship` | T-003, 0.2 |
| 查询实体 | `world.query { DiscipleContext(world) }.filter { ... }` | T-004, 3.x |
| 修改组件 | `entity.editor { it.addComponent(health.copy(current = 50)) }` | T-005 |
| 添加关系 | `entity.addRelation<Mentorship>(target = mentor)` | T-003 |
| 创建 Service | `class HealthService : EntityRelationContext { }` | T-013, 4.x |
| 设置 Observer | `entity.observe<OnHealthChanged>().exec { ... }` | T-014, 5.x |
| 解释 ECS 概念 | 解释 Component vs Tag vs Relation 区别 | 0.x, 1.x |

### ❌ AI 明确不可以做的

| 禁止行为 | 原因 | 替代方案 |
|----------|------|----------|
| 修改 EntityQueryContext 底层实现 | 破坏框架一致性 | 使用现有 API |
| 创建非 ECS 模式的新类结构 | 违反项目架构 | 按模板生成 |
| 跳过组件原子化设计原则 | 影响性能和维护 | 拆分组件 |
| 使用 `has<>` 或 `where{}` | API 已废弃 | 使用 `component<>` 和 `filter{}` |
| 在 Service 中保存状态 | 违反无状态原则 | 使用 Component 存储 |
| 混合不同 ECS 概念 | 造成混乱 | 明确分类后使用 |

---

## 交互流程

```
1. 用户提出需求
   ↓
2. AI 映射到 ECS 概念 (查 0.1 决策树)
   ↓
3. AI 选择对应模板 (查 0.2 场景速查表)
   ↓
4. AI 按模板生成代码
   ↓
5. AI 标注文件路径
   ↓
6. 用户审核确认
   ↓
7. 完成
```

---

## -0. 输出格式规范

> 本章定义 AI 输出代码时的格式要求。**所有代码输出必须遵循此规范**。

### -0.1 代码块标记

#### Kotlin 代码块

```kotlin
// ✅ 正确：完整的 Kotlin 代码块
data class Health(val current: Int, val max: Int)

// ✅ 正确：带语言标注
```kotlin
data class Health(val current: Int, val max: Int)
```

#### Diff 变更块

```diff
// ✅ 正确：使用 diff 标注修改
- 错误代码
+ 正确代码
```

#### 多文件输出

```kotlin
// 文件: src/components/player/Health.kt
data class Health(val current: Int, val max: Int)
```

```kotlin
// 文件: src/tags/player/PlayerTags.kt
sealed class ActiveTag
```

### -0.2 注释规范

#### 正确/错误示例标记

```kotlin
// ✅ 正确示例：勾号 + 空格 + 说明
val health = entity.getComponent<Health>() ?: return

// ❌ 错误示例：叉号 + 空格 + 说明
val health = entity.getComponent<Health>()  // 可能为空！
```

#### AI 指导注释

```kotlin
/// T-001: 创建实体
///
/// ## AI 指导
/// 1. 确定实体类型名称
/// 2. 确定需要的 Component
/// 3. 确定是否需要 Tag
/// 4. 参考 0.2 场景速查表
///
```

#### 代码注释位置

```kotlin
// ✅ 正确：注释在上方
// 获取实体血量
val health = entity.getComponent<Health>() ?: return

// ❌ 错误：注释在代码后（过长）
val health = entity.getComponent<Health>() ?: return  // 获取实体血量，如果不存在则返回
```

### -0.3 文件路径标注

#### 必须标注完整路径

```kotlin
// 文件: src/components/disciple/DiscipleComponents.kt
data class DiscipleInfo(val name: String, val age: Int)
```

```kotlin
// 文件: src/services/disciple/DiscipleService.kt
class DiscipleService(override val world: World) : EntityRelationContext { }
```

#### 路径规范

| 类型 | 目录 | 示例 |
|------|------|------|
| Component | `src/components/[模块]/` | `src/components/disciple/Health.kt` |
| Tag | `src/tags/[模块]/` | `src/tags/disciple/StatusTags.kt` |
| Relation | `src/relations/[模块]/` | `src/relations/disciple/Mentorship.kt` |
| Service | `src/services/[模块]/` | `src/services/disciple/DiscipleService.kt` |
| Factory | `src/factories/[模块]/` | `src/factories/disciple/DiscipleFactory.kt` |

### -0.4 变更说明格式

#### 单次变更

```markdown
## 变更内容
- 新增 DiscipleFactory 用于创建弟子实体
- 新增 HealthComponent 存储血量数据
- 新增 HealthService 处理血量逻辑

## 涉及文件
- src/components/disciple/Health.kt
- src/factories/disciple/DiscipleFactory.kt
- src/services/disciple/HealthService.kt

## 验证方法
- 运行单元测试: `./gradlew test`
- 运行集成测试: `./gradlew integrationTest`
```

#### 多次变更

```markdown
## 变更内容

### 变更1: 新增弟子组件
- 新增 HealthComponent 存储血量
- 新增 ManaComponent 存储蓝量

### 变更2: 新增弟子工厂
- 新增 DiscipleFactory 创建弟子

### 变更3: 新增血量服务
- 新增 HealthService 处理伤害和治疗

## 涉及文件
...

## 验证方法
...
```

### -0.5 错误输出格式

#### 编译错误

```markdown
## ❌ 生成失败

### 错误信息
```
error: Val cannot be reassigned
entity.getComponent<Health>()!!.current = 50
                                  ^
```

### 原因
直接修改 Component 属性

### 修正方法
```kotlin
// ✅ 正确：使用 copy()
val health = entity.getComponent<Health>()!!
entity.editor {
    it.addComponent(health.copy(current = 50))
}
```

### 修正后代码
```kotlin
// 文件: src/services/combat/HealthService.kt
class HealthService(override val world: World) : EntityRelationContext {
    fun damage(entity: Entity, amount: Int) {
        val health = entity.getComponent<Health>()!!
        entity.editor {
            it.addComponent(health.copy(current = maxOf(0, health.current - amount)))
        }
    }
}
```

#### 逻辑错误

```markdown
## ⚠️ 潜在问题

### 问题描述
嵌套查询可能导致性能问题

### 问题代码
```kotlin
// ❌ 错误：嵌套查询
for (ctx in world.query { DiscipleContext(world) }) {
    if (ctx.entity.getRelation<Mentorship>(target = mentor) != null) {
        ...
    }
}
```

### 优化方案
```kotlin
// ✅ 正确：单次查询
class MentorContext(world: World) : EntityQueryContext(world) {
    val mentorship by component<Mentorship>()
    override fun FamilyBuilder.configure() {
        relation(relations.relation<Mentorship>(target = mentor))
    }
}
```

### 优化后代码
...
```

### -0.6 对话输出格式

#### 简短确认

```markdown
✅ 完成
- 新增弟子工厂: src/factories/disciple/DiscipleFactory.kt
```

#### 详细说明

```markdown
## ✅ 完成

### 新增文件
1. **src/components/disciple/Health.kt**
   - data class Health(val current: Int, val max: Int)

2. **src/factories/disciple/DiscipleFactory.kt**
   - class DiscipleFactory : EntityRelationContext
   - fun createDisciple(name: String, age: Int): Entity

### 修改文件
- 无

### 验证
- 代码编译通过 ✅
- 单元测试通过 ✅
```

---

## -1. 代码生成流程

> 本章描述 AI 生成 ECS 代码的完整流程。**所有代码生成必须遵循此流程**。

### 1.1 整体流程概览

```
┌─────────────────────────────────────────────────────────────────┐
│                     代码生成流程                                  │
├─────────────────────────────────────────────────────────────────┤
│  ┌──────────┐   ┌──────────┐   ┌──────────┐   ┌──────────┐     │
│  │ 1.理解   │ → │ 2.决策   │ → │ 3.选模板 │ → │ 4.填充   │     │
│  │ 需求     │   │ 类型     │   │ 代码     │   │ 参数     │     │
│  └──────────┘   └──────────┘   └──────────┘   └──────────┘     │
│                                                 ↓               │
│  ┌──────────┐   ┌──────────┐   ┌──────────┐   ┌──────────┐     │
│  │ 7.完成   │ ← │ 6.验证   │ ← │ 5.补充   │ ← │ 4.填充   │     │
│  │ 输出结果 │   │ 检查     │   │ 代码     │   │ 参数     │     │
│  └──────────┘   └──────────┘   └──────────┘   └──────────┘     │
└─────────────────────────────────────────────────────────────────┘
```

### 1.2 步骤详解

#### 步骤1: 理解需求

**目标**: 将用户自然语言需求映射到 ECS 概念

**输入示例**:
```
用户: "弟子有血量和蓝量，需要能被打"
```

**分析过程**:
| 用户描述 | ECS 概念 | 说明 |
|----------|----------|------|
| "弟子" | Entity | 实体类型 |
| "血量" | Component | 实体属性，需要 data class |
| "蓝量" | Component | 实体属性，需要 data class |
| "被打" | Service | 业务逻辑，需要 damage() 方法 |

**输出**:
```
- Entity: Disciple
- Component: Health, Mana
- Tag: 可能需要 ActiveTag/DeadTag
- Service: CombatService 或 HealthService
```

#### 步骤2: 决策类型

**目标**: 使用决策树确定具体类型

**查 0.1 核心决策树**:
```
需要存储数据？是
  ├─ 数据属于实体自身属性？是
  │    ├─ 单个属性？→ value class
  │    └─ 多个属性？→ data class Component
  └─ 关联到其他实体？→ Relation
```

**确定**:
- Health: `data class Health(val current: Int, val max: Int)` ✓
- Mana: `data class Mana(val current: Int, val max: Int)` ✓

#### 步骤3: 选择模板

**目标**: 查 0.2 场景速查表找到对应模板

| 用户需求 | 对应模板 |
|----------|----------|
| "创建弟子实体" | T-001: 创建实体 |
| "弟子受伤扣血" | T-005: 更新组件 |
| "查询受伤弟子" | T-004: 查询实体 |

#### 步骤4: 填充参数

**目标**: 替换模板中的占位符

**占位符说明**:
| 占位符 | 含义 | 示例 |
|--------|------|------|
| `[EntityType]` | 实体类型名 | `Disciple`, `Item`, `Building` |
| `[Component]` | 组件名 | `Health`, `Mana`, `Position` |
| `[Tag]` | 标签名 | `ActiveTag`, `DeadTag` |
| `[Relation]` | 关系类型名 | `Mentorship`, `Ownership` |
| `[Service]` | 服务名 | `HealthService`, `CombatService` |

**填充示例**:
```kotlin
// 模板
class [EntityType]Factory(override val world: World) : EntityRelationContext {
    fun create[EntityType](name: String): Entity { ... }
}

// 填充后
class DiscipleFactory(override val world: World) : EntityRelationContext {
    fun createDisciple(name: String): Entity { ... }
}
```

#### 步骤5: 补充代码

**目标**: 添加必要的前置依赖和导入

**检查清单**:
- [ ] 需要的 Component 是否已定义？
- [ ] 需要的 Tag 是否已定义？
- [ ] 需要的 Relation 是否已定义？
- [ ] World 创建时是否注册了组件ID？
- [ ] Service 是否继承 EntityRelationContext？

#### 步骤6: 验证检查

**目标**: 对照验证清单检查（详见附录B）

**快速检查**:
```kotlin
// ✅ 正确
data class Health(val current: Int, val max: Int)
sealed class ActiveTag
class DiscipleContext(world: World) : EntityQueryContext(world) {
    val health by component<Health>()
}

// ❌ 错误
data class Level(val value: Int)  // 单属性应该用 value class
has<Health>()  // 应该用 component<>()
```

#### 步骤7: 输出结果

**目标**: 按格式输出结果

**输出格式**:
```
## 变更内容
- 新增 DiscipleFactory 创建弟子实体
- 新增 HealthComponent 存储血量数据

## 涉及文件
- src/components/disciple/DiscipleComponents.kt
- src/factories/disciple/DiscipleFactory.kt

## 代码

```kotlin
// 文件路径
```

## 验证方法
- 运行 `world.query { DiscipleContext(world) }` 应返回所有弟子
```

---

### 1.3 流程速查表

| 步骤 | 输入 | 处理 | 输出 |
|------|------|------|------|
| 1 | 用户需求 | 自然语言分析 | ECS 概念映射 |
| 2 | ECS 概念 | 查决策树 0.1 | 确定类型 |
| 3 | 类型 | 查场景表 0.2 | 选择模板 |
| 4 | 模板 | 替换占位符 | 代码框架 |
| 5 | 框架 | 补充依赖 | 完整代码 |
| 6 | 完整代码 | 验证清单 | 检查结果 |
| 7 | 检查结果 | 按格式输出 | 最终结果 |

---

> **AI 指令**：本文档是宗门修真录项目的 ECS 框架完整参考。当用户要求你操作实体、组件、关系时，**必须先查看第 0 章决策树，再使用第 2 章代码模板**。

---

## -0. 输出格式规范

### 0.1 核心决策树

```
需要存储数据？
  │
  ├─ 是 ──→ 数据属于**实体自身属性**？
  │           │
  │           ├─ 是 ──→ 属性数量？
  │           │        │
  │           │        ├─ 单个 ──→ @JvmInline value class
  │           │        │        └── 示例: @JvmInline value class Level(val value: Int)
  │           │        │
  │           │        └─ 多个 ──→ data class Component
  │           │                   └── 示例: data class Health(val current: Int, val max: Int)
  │           │
  │           └─ 否 ──→ 数据属于**另一实体**？
  │                    │
  │                    ├─ 是 ──→ Relation（可带data）
  │                    │        │
  │                    │        ├─ 需要存储关系数据？───→ data class RelationData
  │                    │        │                            └── 示例: data class MentorshipData(val year: Int)
  │                    │        │
  │                    │        └─ 不需要存储关系数据？──→ sealed class Relation
  │                    │                                   └── 示例: sealed class Mentorship
  │                    │
  │                    └─ 否 ──→ ⚠️ 考虑拆分为多个 Component 或使用 Tag
  │
  └─ 否 ──→ 仅标记/关联？
           │
           ├─ 关联到**目标实体** ──→ Relation（sealed class，可复用）
           │        └── 示例: sealed class Ownership 用于道具→玩家、建筑→玩家
           │
           └─ 仅标记**状态** ──→ Tag（sealed class）
                       └── 示例: sealed class ActiveTag, sealed class DeadTag
```

### 0.1.1 决策树使用说明

**判断流程**:
1. **第一步**: 是否需要存储数据？
   - 是 → 进入 Component/Relation 判断
   - 否 → 进入 Tag 判断

2. **第二步**: 数据属于谁？
   - 实体自身 → Component
   - 另一实体 → Relation

3. **第三步**: 选择具体类型
   - 单属性 → value class
   - 多属性 → data class
   - 需要关联目标 → Relation

4. **第四步**: 确认是否可以复用
   - Relation 默认可复用
   - Component 通常不可复用（除非设计为可复用）

### 0.1.2 决策示例

| 需求 | 判断路径 | 结果 |
|------|----------|------|
| "弟子有血量" | 是→实体自身→多个属性 | `data class Health` |
| "装备有等级" | 是→实体自身→单个属性 | `@JvmInline value class Level` |
| "弟子拜师" | 是→另一实体→无数据 | `sealed class Mentorship` |
| "师徒关系带年份" | 是→另一实体→有数据 | `data class MentorshipData` |
| "道具属于玩家" | 是→另一实体→可复用 | `sealed class Ownership` |
| "标记死亡" | 否→标记状态 | `sealed class DeadTag` |

### 0.2 场景速查表

| 用户说 | 应该使用 | 数据类型 | 查看模板 |
|--------|----------|----------|----------|
| "存储血量" | Component | data class Health | [T-001] |
| "标记死亡" | Tag | sealed class DeadTag | [T-002] |
| "关联师徒关系（弟子→师父）" | Relation | sealed class Mentorship | [T-003] |
| "关联带年份的师徒关系" | Relation | MentorshipData | [T-003] |
| "关联所有关系（道具/建筑→玩家）" | Relation（复用） | sealed class Ownership | [T-003] |
| "定义单值属性" | Component | @JvmInline value class | [T-001] + 1.3节 |
| "原子化拆分组件" | Component | data class | [T-001] + 1.3节 |
| "创建弟子" | Entity + Component | | [T-001] |
| "查询实体" | Query | | [T-004] |
| "修改属性" | Component copy | | [T-005] |
| "创建层级（子实体→父实体）" | Relation childOf | | [T-006] |
| "检查状态" | Tag检查 | | [T-007] |
| "批量处理" | Query + forEach | | [T-008] |

### 0.3 性能等级

| 操作 | 复杂度 | 说明 |
|------|--------|------|
| hasTag/Component | O(1) | 🟢 极快 |
| getComponent | O(1) | 🟢 极快 |
| world.query | O(n) | 🟡 首次查询 |
| 缓存后Query | O(1) | 🟢 后续查询 |
| 嵌套查询 | O(n²) | 🔴 避免！ |

### 0.4 禁止清单

❌ **不要在Service中保存状态**
```kotlin
// ❌ 错误
class BadService { private var counter = 0 }
```

❌ **不要直接修改Component**
```kotlin
// ❌ 错误
entity.getComponent<Health>()!!.current = 50

// ✅ 正确
val health = entity.getComponent<Health>()!!
entity.editor { it.addComponent(health.copy(current = 50)) }
```

❌ **不要忘记注册ComponentId**
```kotlin
// ✅ 正确
val world = world {
    components {
        world.componentId<Health>()
        world.componentId<ActiveTag> { it.tag() }
    }
}
```

❌ **不要在循环中重复查询**
```kotlin
// ❌ 错误
for (i in 0..100) { val result = world.query { ... } }

// ✅ 正确
val result = world.query { ... }
for (i in 0..100) { /* 使用result */ }
```

❌ **不要定义混合职责的组件**
```kotlin
// ❌ 错误：混合了属性、位置、状态
data class PlayerAllInOne(
    val health: Int,
    val positionX: Float,
    val positionY: Float,
    val level: Int,
    val exp: Long
)

// ✅ 正确：原子化拆分
data class Health(val current: Int, val max: Int)
data class Position(val x: Float, val y: Float)
@JvmInline value class Level(val value: Int)
data class Experience(val current: Long, val max: Long)
```

❌ **单属性组件不要使用 data class**
```kotlin
// ❌ 不好：单属性使用 data class
data class Level(val value: Int)
data class Exp(val value: Long)

// ✅ 正确：单属性使用 value class
@JvmInline value class Level(val value: Int)
@JvmInline value class Exp(val value: Long)
```

❌ **避免使用可空字段**
```kotlin
// ❌ 不好：使用可空字段
data class BadData(val name: String, val optional: String?)

// ✅ 正确：拆分为可选组件
data class RequiredData(val name: String)
data class OptionalData(val value: Int)
```

---

## 1. 核心概念

### 1.1 五核心概念

| 概念 | 定义 | 示例 |
|------|------|------|
| **Entity** | 游戏对象，只有ID | 弟子、物品 |
| **Component** | 属性数据 | Health(100), Position(x,y) |
| **Tag** | 状态标记（无数据） | ActiveTag, DeadTag |
| **Relation** | 实体关联 | Mentorship(师徒) |
| **System** | 处理逻辑 | HealthService |

### 1.x 命名规范

**Component命名**：名词，描述属性
```kotlin
data class Health(val current: Int, val max: Int)
data class Position(val x: Float, val y: Float)
```

**Tag命名**：形容词/状态 + Tag
```kotlin
sealed class ActiveTag
sealed class DeadTag
sealed class StunnedTag
```

**Relation命名**：名词，描述关系
```kotlin
sealed class Mentorship
sealed class Ownership
sealed class ChildOf
```

**Data类命名**：RelationType + Data后缀
```kotlin
data class MentorshipData(val startYear: Int, val intimacy: Float)
data class OwnershipData(val acquireTime: Long)
```

**value class命名**：名词
```kotlin
@JvmInline value class Level(val value: Int)
@JvmInline value class Experience(val value: Long)
```

**Service命名**：[功能] + Service
```kotlin
class HealthService
class CombatService
```

**Context命名**：[实体类型] + Context
```kotlin
class DiscipleContext
class ItemContext
```

**Component字段命名**：小驼峰
```kotlin
data class Health(val current: Int, val max: Int)
data class Position(val x: Float, val y: Float)
```

### 1.2 Component vs Tag vs Relation

| 特性 | Component | Tag | Relation |
|------|-----------|-----|----------|
| 数据 | ✅ 有 | ❌ 无 | ✅ 可选 |
| 用途 | 属性值 | 状态标记 | 实体关联 |

---

### 1.3 组件设计原则

**核心原则1：组件定义要尽可能原子化**

```kotlin
// ✅ 正确：单一职责，便于复用
data class Health(val current: Int, val max: Int)
data class Mana(val current: Int, val max: Int)
data class Position(val x: Float, val y: Float)

// ❌ 错误：混合职责，难以复用
data class PlayerStats(
    val health: Int,
    val mana: Int,
    val positionX: Float,
    val positionY: Float,
    val level: Int,
    val exp: Long
)
```

**原子化优势**：
1. **便于替换**：单独替换某组件不影响其他
2. **便于复用**：相同组件可在不同实体间复用
3. **查询高效**：只需查询需要的组件

**核心原则2：单属性组件使用 @JvmInline value class**

```kotlin
// ✅ 正确：单属性使用 value class（性能更好）
@JvmInline
value class Level(val value: Int)
@JvmInline
value class Exp(val value: Long)

// ❌ 错误：单属性使用普通 data class
data class Level(val value: Int)
data class Exp(val value: Long)
```

**value class优势**：
1. **性能更高**：避免装箱拆箱开销
2. **语义清晰**：表明这是单一不可变的值
3. **类型安全**：编译时类型检查

**Attribute拆分示例**：
```kotlin
// ✅ 正确：原子化 + value class
@JvmInline value class Strength(val value: Int)
@JvmInline value class Constitution(val value: Int)
@JvmInline value class Intelligence(val value: Int)
@JvmInline value class Agility(val value: Int)

// ✅ 正确：多属性使用 data class
data class Health(val current: Int, val max: Int)

// ❌ 错误：全部放一起
data class Attributes(
    val strength: Int,
    val constitution: Int,
    val intelligence: Int,
    val agility: Int)
```

### 1.4 代码格式规范

**缩进与空格**
```kotlin
// ✅ 正确：4空格缩进
data class Health(
    val current: Int,
    val max: Int)

// ✅ 正确：单行组件简短定义
data class Position(val x: Float, val y: Float)

// ❌ 错误：混合缩进
data class Bad(
  val a: Int,
    val b: Int)
```

**空行规范**
```kotlin
// ✅ 正确：类定义间空一行
class HealthService : EntityRelationContext {
    fun damage(entity: Entity, amount: Int) {
        val health = entity.getComponent<Health>()!!
        entity.editor {
            it.addComponent(health.copy(current = health.current - amount))
        }
    }
}

class CombatService : EntityRelationContext {
    fun attack(target: Entity, damage: Int) {
        healthService.damage(target, damage)
    }
}

// ❌ 错误：连续定义无空行
class A : EntityRelationContext { }
class B : EntityRelationContext { }
```

**KDoc注释规范**
```kotlin
/**
 * Service功能简述
 *
 * @property dependency 依赖说明
 * @constructor 创建Service
 */
class MyService(
    override val world: World,
    private val dependency: OtherService
) : EntityRelationContext { }

// 行内注释
val health = entity.getComponent<Health>() ?: return // 无血量时返回
```

**组件声明顺序**
```kotlin
// ✅ 正确：Component → Tag → Relation → Data → value class
data class Health(val current: Int, val max: Int)
sealed class ActiveTag
sealed class Mentorship
data class MentorshipData(val startYear: Int, val intimacy: Float)
@JvmInline value class Level(val value: Int)
```

**lambda表达式格式**
```kotlin
// ✅ 正确：复杂lambda多行
world.query { DiscipleContext(world) }
    .filter { ctx ->
        ctx.health.current > 0 &&
        ctx.entity.hasTag<ActiveTag>()
    }
    .toList()

// ✅ 正确：简单lambda单行
world.query { it.hasTag<ActiveTag>() }
```

---


## 2. 代码模板库

> 本章提供 ECS 代码生成模板。每个模板包含：
> - **模板说明**: 用途和使用场景
> - **AI 指导**: 生成代码时的注意事项
> - **参数替换表**: 占位符说明
> - **完整代码**: 可直接使用的代码
> - **使用示例**: 实际调用示例

---

### T-001: 创建实体

**用途**: 创建包含 Component 和 Tag 的实体

**AI 指导**:
1. 先确定实体类型名称
2. 确定需要的 Component（属性数据）
3. 确定是否需要 Tag（状态标记）
4. 参考 0.2 场景速查表

**参数替换表**:
| 占位符 | 含义 | 示例 |
|--------|------|------|
| `[EntityType]` | 实体类型名 | `Disciple`, `Item`, `Building` |
| `[Component1]` | 组件1 | `BasicInfo`, `Health`, `Position` |
| `[Tag1]` | 标签 | `ActiveTag`, `DeadTag` |

**代码**:
```kotlin
// 文件: src/factories/[entity-type-lowercase]/[EntityType]Factory.kt
class [EntityType]Factory(override val world: World) : EntityRelationContext {
    fun create[EntityType](
        name: String,
        age: Int
    ): Entity {
        return world.entity {
            it.addComponent(BasicInfo(name, age))
            it.addComponent(Health(100, 100))
            it.addTag<ActiveTag>()
        }
    }
}
```

**使用示例**:
```kotlin
val factory = DiscipleFactory(world)
val disciple = factory.createDisciple("张三", 18)
```

---

### T-002: Tag 操作

**用途**: 添加、移除、切换 Tag（状态标记）

**AI 指导**:
1. Tag 是无数据的状态标记
2. 使用 sealed class 定义
3. 添加 Tag 使用 `addTag<T>()`
4. 移除 Tag 使用 `removeTag<T>()`
5. 检查 Tag 使用 `hasTag<T>()`

**参数替换表**:
| 占位符 | 含义 | 示例 |
|--------|------|------|
| `[State]` | 状态名 | `Active`, `Dead`, `Stunned` |
| `[StateTag]` | 标签类名 | `ActiveTag`, `DeadTag` |
| `[OppositeTag]` | 相反状态 | `ActiveTag` ↔ `InactiveTag` |

**代码**:
```kotlin
// 文件: src/services/[module]/StatusService.kt
class StatusService(override val world: World) : EntityRelationContext {

    /// 添加状态
    fun add[State](entity: Entity) {
        entity.editor {
            it.addTag<[StateTag]>()
        }
    }

    /// 移除状态
    fun remove[State](entity: Entity) {
        entity.editor {
            it.removeTag<[StateTag]>()
        }
    }

    /// 切换状态
    fun toggle[State](entity: Entity) {
        entity.editor {
            if (entity.hasTag<[StateTag]>()) {
                it.removeTag<[StateTag]>()
            } else {
                it.addTag<[StateTag]>()
            }
        }
    }

    /// 检查状态
    fun has[State](entity: Entity): Boolean {
        return entity.hasTag<[StateTag]>()
    }
}
```

**使用示例**:
```kotlin
val statusService = StatusService(world)
statusService.addActive(entity)
if (statusService.hasActive(entity)) {
    println("实体已激活")
}
```

---

### T-003: 创建关系

**用途**: 建立实体间的关联（可带数据，可复用）

**AI 指导**:
1. RelationType 是 sealed class（用于标识关系类型）
2. Data 是 data class（用于存储关系数据，可选）
3. addRelation<K> 的泛型 K 和 data 参数类型必须一致
4. Relation默认可复用（同一类型可关联多个目标）

**参数替换表**:
| 占位符 | 含义 | 示例 |
|--------|------|------|
| `[Relation]` | 关系类型 | `Mentorship`, `Ownership` |
| `[RelationData]` | 关系数据 | `MentorshipData` |
| `[SourceEntity]` | 源实体 | `Disciple`, `Item` |
| `[TargetEntity]` | 目标实体 | `Master`, `Player` |

**代码**:
```kotlin
// 文件: src/relations/[module]/[Relation]s.kt
/// Relation 用于建立实体间的关联
/// 1. RelationType 是 sealed class（标识关系类型）
/// 2. Data 是 data class（存储关系数据，可选）
/// 3. 同一 RelationType 可关联到不同目标（可复用）

sealed class [Relation]

data class [RelationData](
    val startYear: Int,
    val intimacy: Float
)

class [Relation]Service(override val world: World) : EntityRelationContext {

    /// 无数据关系 - 泛型是 RelationType
    fun establish(source: [SourceEntity], target: [TargetEntity]) {
        source.editor {
            it.addRelation<[Relation]>(target = target)
        }
    }

    /// 有数据关系 - 泛型和 data 都是 Data 类型
    fun establishWithData(
        source: [SourceEntity],
        target: [TargetEntity],
        data: [RelationData]
    ) {
        source.editor {
            it.addRelation<[RelationData]>(target = target, data = data)
        }
    }

    /// 检查关系是否存在
    fun hasRelation(from: Entity, to: Entity): Boolean {
        return from.getRelation<[Relation]>(target = to) != null
    }

    /// 获取关系数据
    fun getRelationData(from: Entity, to: Entity): [RelationData]? {
        return from.getRelation<[RelationData]>(target = to)
    }
}

/// 可复用的 Relation 示例
sealed class Ownership

class OwnershipService(override val world: World) : EntityRelationContext {

    /// 道具属于玩家
    fun setItemOwner(item: Entity, owner: Entity) {
        item.editor {
            it.addRelation<Ownership>(target = owner)
        }
    }

    /// 建筑属于玩家
    fun setBuildingOwner(building: Entity, owner: Entity) {
        building.editor {
            it.addRelation<Ownership>(target = owner)
        }
    }

    /// 查询某玩家拥有的所有实体
    fun getOwnedEntities(owner: Entity): List<Entity> {
        class OwnedContext(world: World) : EntityQueryContext(world) {
            val ownership by component<Ownership>()
            override fun FamilyBuilder.configure() {
                relation(relations.relation<Ownership>(target = owner))
            }
        }
        return world.query { OwnedContext(world) }.toList()
    }
}
```

**使用示例**:
```kotlin
val relationService = MentorshipService(world)
relationService.establishWithData(disciple, mentor, MentorshipData(2024, 0.5f))

val ownershipService = OwnershipService(world)
ownershipService.setItemOwner(item, player)
```

---

### T-004: 查询实体

**用途**: 根据条件查询实体集合

**AI 指导**:
1. EntityQueryContext 必须继承 EntityQueryContext(world)
2. 使用 `component<>()` 声明必须存在的组件
3. 使用 `filter{}` 进行运行时条件过滤
4. Tag 检查在 filter 中使用 `entity.hasTag<T>()`
5. 返回 List<EntityQueryContext> 类型

**代码**:
```kotlin
// 文件: src/services/[module]/[EntityType]QueryService.kt
/// EntityQueryContext 规则：
/// - 必须继承 EntityQueryContext 才能使用 component<>
/// - 内置 entity 属性，无需重复定义

class [EntityType]Context(world: World) : EntityQueryContext(world) {
    val basicInfo by component<BasicInfo>()   // ✅ 必须存在
    val health by component<Health>()           // ✅ 必须存在
    val position by component<Position?>()      // ✅ 可选组件
}

class [EntityType]QueryService(override val world: World) : EntityRelationContext {

    /// 查询所有实体
    fun getAll(): List<[EntityType]Context> {
        return world.query { [EntityType]Context(world) }.toList()
    }

    /// 条件查询 - filter 用于运行时条件
    fun getByCondition(condition: ([EntityType]Context) -> Boolean): List<[EntityType]Context> {
        return world.query { [EntityType]Context(world) }
            .filter(condition)
            .toList()
    }

    /// Tag 检查在 filter 中进行
    fun getActive(): List<[EntityType]Context> {
        return world.query { [EntityType]Context(world) }
            .filter { ctx -> ctx.entity.hasTag<ActiveTag>() }
            .toList()
    }

    /// 组合条件查询
    fun getActiveWithHealthAbove(minHealth: Int): List<[EntityType]Context> {
        return world.query { [EntityType]Context(world) }
            .filter { ctx ->
                ctx.entity.hasTag<ActiveTag>() &&
                ctx.health.current >= minHealth
            }
            .toList()
    }
}
```

**使用示例**:
```kotlin
val queryService = DiscipleQueryService(world)
val allDisciples = queryService.getAll()
val activeDisciples = queryService.getActive()
val healthyDisciples = queryService.getActiveWithHealthAbove(50)
```

---

### T-005: 更新组件

**用途**: 修改实体的组件数据

**AI 指导**:
1. Component 是不可变数据
2. 必须使用 `copy()` 创建新实例
3. 通过 `editor{}` 块进行修改
4. 不要直接修改属性值

**代码**:
```kotlin
// 文件: src/services/[module]/[Component]Service.kt
class [Component]Service(override val world: World) : EntityRelationContext {

    /// 更新属性值
    fun update[Property](entity: Entity, newValue: Int) {
        val component = entity.getComponent<[Component]>()!!
        entity.editor {
            it.addComponent(component.copy([property] = newValue))
        }
    }

    /// 批量更新
    fun batchUpdate(entities: List<Entity>, newValue: Int) {
        entities.forEach { entity ->
            val component = entity.getComponent<[Component]>()!!
            entity.editor {
                it.addComponent(component.copy([property] = newValue))
            }
        }
    }

    /// 基于当前值更新
    fun increment[Property](entity: Entity, amount: Int) {
        val component = entity.getComponent<[Component]>()!!
        entity.editor {
            it.addComponent(component.copy([property] = component.[property] + amount))
        }
    }
}
```

**使用示例**:
```kotlin
val healthService = HealthService(world)
healthService.damage(disciple, 10)
healthService.heal(disciple, 5)
```

---

### T-006: 层级关系

**用途**: 创建父子层级的实体关系

**AI 指导**:
1. 使用 `world.childOf(parent)` 创建子实体
2. 子实体自动关联 ChildOf Relation
3. 查询子实体使用 EntityQueryContext

**代码**:
```kotlin
// 文件: src/services/[module]/HierarchyService.kt
class HierarchyService(override val world: World) : EntityRelationContext {

    /// 创建子实体
    fun createChild(parent: Entity, config: Config): Entity {
        return world.childOf(parent) {
            it.addComponent(ChildComponent(config))
        }
    }

    /// 获取所有子实体
    fun getChildren(parent: Entity): List<Entity> {
        class ChildContext(world: World) : EntityQueryContext(world) {
            val childOf by component<ChildOf>()
        }
        return world.query { ChildContext(world) }
            .filter { ctx -> ctx.childOf.target == parent }
            .toList()
    }

    /// 获取根实体（无父实体）
    fun getRootEntities(): List<Entity> {
        class RootContext(world: World) : EntityQueryContext(world) {
            override fun FamilyBuilder.configure() {
                relation(relations.component<ChildOf>(optional = OptionalGroup.Zero))
            }
        }
        return world.query { RootContext(world) }.toList()
    }
}
```

---

### T-007: Tag 检查

**用途**: 检查实体是否具有特定状态

**AI 指导**:
1. 使用 `entity.hasTag<T>()` 检查 Tag
2. 返回 Boolean 类型
3. 可组合多个检查条件

**代码**:
```kotlin
// 文件: src/services/[module]/StatusCheckService.kt
class StatusCheckService(override val world: World) : EntityRelationContext {

    /// 检查是否可以行动
    fun canAct(entity: Entity): Boolean {
        return !entity.hasTag<StunnedTag>() &&
               !entity.hasTag<DeadTag>() &&
               !entity.hasTag<FrozenTag>()
    }

    /// 检查是否已死亡
    fun isDead(entity: Entity): Boolean {
        return entity.hasTag<DeadTag>()
    }

    /// 检查是否处于异常状态
    fun hasAbnormalStatus(entity: Entity): Boolean {
        return entity.hasTag<StunnedTag>() ||
               entity.hasTag<FrozenTag>() ||
               entity.hasTag<PoisonedTag>()
    }

    /// 检查是否处于某种状态
    fun isInState(entity: Entity, tag: Class<*>): Boolean {
        return when (tag) {
            ActiveTag::class.java -> entity.hasTag<ActiveTag>()
            DeadTag::class.java -> entity.hasTag<DeadTag>()
            else -> false
        }
    }
}
```

---

### T-008: 批量处理

**用途**: 对大量实体进行批量操作

**AI 指导**:
1. 查询结果缓存，避免循环中重复查询
2. 使用 `forEach` 遍历处理
3. 注意性能影响

**代码**:
```kotlin
// 文件: src/services/[module]/BatchService.kt
class BatchService(override val world: World) : EntityRelationContext {

    /// 批量处理所有实体
    fun processAll() {
        val entities = world.query { [EntityType]Context(world) }.toList()
        entities.forEach { ctx -> process(ctx.entity) }
    }

    /// 带缓存的批量查询
    private var cached: List<[EntityType]Context>? = null
    private var lastTime: Long = 0

    fun getCached(): List<[EntityType]Context> {
        val now = System.currentTimeMillis()
        if (cached == null || now - lastTime > 5000) {
            cached = world.query { [EntityType]Context(world) }.toList()
            lastTime = now
        }
        return cached!!
    }

    /// 条件批量更新
    fun batchUpdate(condition: ([EntityType]Context) -> Boolean, newValue: Int) {
        world.query { [EntityType]Context(world) }
            .filter(condition)
            .toList()
            .forEach { ctx ->
                val component = ctx.entity.getComponent<[Component]>()!!
                ctx.entity.editor {
                    it.addComponent(component.copy([property] = newValue))
                }
            }
    }
}
```

---

### T-009: 条件查询

**用途**: 基于多个条件组合查询实体

**AI 指导**:
1. 在 filter 中组合多个条件
2. 使用 && / || 连接条件
3. 可提取公共条件为函数

**代码**:
```kotlin
// 文件: src/services/[module]/[Module]QueryService.kt
class [Module]QueryService(override val world: World) : EntityRelationContext {

    /// 单一条件查询
    fun findByCondition(
        minValue: Int = 0,
        includeInactive: Boolean = false
    ): List<[EntityType]Context> {
        return world.query { [EntityType]Context(world) }
            .filter { ctx ->
                ctx.[property] >= minValue &&
                (includeInactive || ctx.entity.hasTag<ActiveTag>())
            }
            .toList()
    }

    /// 复杂条件查询
    fun findByComplexCondition(
        minLevel: Int,
        maxLevel: Int,
        hasTag: Boolean = true
    ): List<[EntityType]Context> {
        return world.query { [EntityType]Context(world) }
            .filter { ctx ->
                ctx.level.value in minLevel..maxLevel &&
                (!hasTag || ctx.entity.hasTag<ActiveTag>()) &&
                ctx.health.current > 0
            }
            .toList()
    }

    /// 可选条件查询
    fun findWithOptionalFilters(
        level: Int? = null,
        tag: ActiveTag? = null,
        health: Int? = null
    ): List<[EntityType]Context> {
        return world.query { [EntityType]Context(world) }
            .filter { ctx ->
                (level == null || ctx.level.value >= level) &&
                (tag == null || ctx.entity.hasTag(tag::class)) &&
                (health == null || ctx.health.current >= health)
            }
            .toList()
    }
}
```

---

### T-010: 删除实体

**用途**: 软删除或清理实体

**AI 指导**:
1. 软删除：标记状态而非真正删除
2. 使用 component<> 检查标记组件
3. filter 中使用 ctx.entity.hasTag<T>()

**代码**:
```kotlin
// 文件: src/services/[module]/DeletionService.kt
class DeletionService(override val world: World) : EntityRelationContext {

    /// 软删除
    fun softDelete(entity: Entity) {
        entity.editor {
            it.removeTag<ActiveTag>()
            it.addTag<DeletedTag>()
            it.addComponent(DeletionInfo(System.currentTimeMillis()))
        }
    }

    /// 恢复已删除实体
    fun restore(entity: Entity) {
        entity.editor {
            it.removeTag<DeletedTag>()
            it.addTag<ActiveTag>()
            it.removeComponent<DeletionInfo>()
        }
    }

    /// 清理过期实体
    fun cleanup(beforeTimestamp: Long): List<Entity> {
        class DeletionContext(world: World) : EntityQueryContext(world) {
            val deletedTag by component<DeletedTag>()
            val deletionInfo by component<DeletionInfo>()
        }
        return world.query { DeletionContext(world) }
            .filter { ctx -> ctx.deletionInfo.deletedAt < beforeTimestamp }
            .toList()
    }

    /// 彻底删除（仅限已清理的实体）
    fun permanentDelete(entity: Entity) {
        world.removeEntity(entity)
    }
}
```

---

### T-011: 组件存在性

**用途**: 检查和获取组件，处理可选组件

**AI 指导**:
1. 使用 `getComponent<C>()` 获取组件
2. 使用 `hasComponent<C>()` 检查存在性
3. 使用安全访问 `?.` 处理可空组件

**代码**:
```kotlin
// 文件: src/services/[module]/SafeService.kt
class SafeService(override val world: World) : EntityRelationContext {

    /// 获取组件，不存在则返回默认值
    fun getOrDefault(entity: Entity, default: Health): Health {
        return entity.getComponent<Health>() ?: default
    }

    /// 确保组件存在，不存在则添加
    fun ensureExists(entity: Entity) {
        if (!entity.hasComponent<Health>()) {
            entity.editor {
                it.addComponent(Health(100, 100))
            }
        }
    }

    /// 组件存在时执行操作
    fun ifExists(entity: Entity, action: (Health) -> Unit) {
        entity.getComponent<Health>()?.let(action)
    }

    /// 获取可选组件
    fun getOptional(entity: Entity): Health? {
        return entity.getComponent<Health>()
    }

    /// 安全更新（组件存在时才更新）
    fun safeUpdate(entity: Entity, newHealth: Health) {
        entity.getComponent<Health>()?.let { current ->
            entity.editor {
                it.addComponent(current.copy(
                    current = newHealth.current,
                    max = newHealth.max
                ))
            }
        }
    }
}
```

---

### T-012: 实体复制

**用途**: 基于现有实体创建新实体

**AI 指导**:
1. 使用 `world.instanceOf(source)` 复制实体
2. 可在复制过程中修改组件
3. 复制的是组件数据，不是实体引用

**代码**:
```kotlin
// 文件: src/services/[module]/CloneService.kt
class CloneService(override val world: World) : EntityRelationContext {

    /// 简单复制
    fun clone(source: Entity): Entity {
        return world.instanceOf(source) {
            // 可选：添加或修改组件
        }
    }

    /// 复制并修改
    fun cloneWithModification(source: Entity, newName: String): Entity {
        return world.instanceOf(source) {
            it.addComponent(BasicInfo(newName, 18))
        }
    }

    /// 复制并排除某些 Tag
    fun cloneWithoutTags(source: Entity, vararg tags: KClass<*>): Entity {
        return world.instanceOf(source) {
            tags.forEach { tag ->
                when (tag) {
                    SpecialTag::class.java -> it.removeTag<SpecialTag>()
                    ActiveTag::class.java -> it.removeTag<ActiveTag>()
                    else -> { /* 忽略其他 Tag */ }
                }
            }
        }
    }

    /// 深复制（手动复制所有组件）
    fun deepClone(source: Entity): Entity {
        return world.entity {
            // 手动获取并添加所有组件
            source.getComponent<BasicInfo>()?.let {
                it.addComponent(it)
            }
            source.getComponent<Health>()?.let {
                it.addComponent(it)
            }
            // Tag 不需要复制，根据需要添加
            it.addTag<ActiveTag>()
        }
    }
}
```

---

### T-013: DI 依赖注入

**用途**: Service 之间的依赖管理

**AI 指导**:
1. Service 必须继承 EntityRelationContext
2. 使用 injects 注册依赖
3. 通过构造函数注入其他 Service

**代码**:
```kotlin
// 文件: src/services/[module]/[Service]Module.kt
/// DI 规则：
/// - Service 必须继承 EntityRelationContext
/// - 使用 constructor 注入其他 Service
/// - 在 world 创建时注册

// 注册 Service
val world = world {
    injects {
        bind singleton { new(::HealthService) }
        bind singleton { new(::CombatService) }
        bind singleton { new(::InventoryService) }
    }
    components {
        world.componentId<Health>()
        world.componentId<ActiveTag>()
    }
}

// 使用 Service（构造函数注入，推荐）
class CombatService(
    override val world: World,
    private val healthService: HealthService,
    private val inventoryService: InventoryService
) : EntityRelationContext {
    fun attack(target: Entity, damage: Int) {
        healthService.damage(target, damage)
    }
}

// 延迟获取（依赖较多时使用）
class MyService(override val world: World) : EntityRelationContext {
    private val healthService: HealthService by world.di.instance()
}
```

---

### T-014: Observer 观察者

**用途**: 监听实体事件变化

**AI 指导**:
1. 定义事件（sealed class 无数据，data class 有数据）
2. 在 World 创建时注册事件类型
3. 使用 observe 监听事件
4. 使用 emit 触发事件

**代码**:
```kotlin
// 文件: src/events/[module]/[Event]Events.kt
/// Observer 规则：
/// 1. 定义事件（sealed class 无数据，data class 有数据）
/// 2. 在 World 创建时注册事件类型
/// 3. 创建观察者监听事件
/// 4. 触发事件

// 1. 定义事件
sealed class On[Entity][Property]Changed
data class [Property]ChangedEvent(
    val oldValue: Int,
    val newValue: Int
)

// 2. 创建带观察者的实体
class [Entity]Factory(override val world: World) : EntityRelationContext {
    fun create[Entity](): Entity {
        val entity = world.entity {
            it.addComponent([Component](...))
            it.addTag<ActiveTag>()
        }

        // 观察变化（无数据事件）
        entity.observe<On[Property]Changed>().exec {
            println("[Property] changed!")
        }

        // 观察变化（带数据事件）
        entity.observeWithData<[Property]ChangedEvent>().exec { event ->
            println("[Property]: ${event.oldValue} -> ${event.newValue}")
        }

        return entity
    }
}

// 3. 触发事件
class [Property]Service(override val world: World) : EntityRelationContext {
    fun update[Property](entity: Entity, newValue: Int) {
        val component = entity.getComponent<[Component]>()!!
        val oldValue = component.[property]
        entity.editor {
            it.addComponent(component.copy([property] = newValue))
        }
        entity.emit<On[Property]Changed>()
        entity.emit([Property]ChangedEvent(oldValue, newValue))
    }
}

// 4. World 创建时注册事件类型
val world = world {
    components {
        world.componentId<On[Property]Changed>()
        world.componentId<[Property]ChangedEvent>()
    }
}

// 带过滤条件的观察者
entity.observe<On[Property]Changed>().exec { ctx ->
    if (ctx.entity.hasTag<PlayerTag>()) {
        println("Player [Property] changed!")
    }
}
```

---

### 模板速查表

| 模板 | 名称 | 用途 |
|------|------|------|
| T-001 | 创建实体 | Entity + Component + Tag |
| T-002 | Tag 操作 | addTag, removeTag, toggle |
| T-003 | 创建关系 | addRelation, getRelation |
| T-004 | 查询实体 | EntityQueryContext + filter |
| T-005 | 更新组件 | copy() + editor |
| T-006 | 层级关系 | childOf, getChildren |
| T-007 | Tag 检查 | hasTag |
| T-008 | 批量处理 | query + forEach |
| T-009 | 条件查询 | filter 组合条件 |
| T-010 | 删除实体 | softDelete, cleanup |
| T-011 | 组件存在性 | getComponent, hasComponent |
| T-012 | 实体复制 | instanceOf |
| T-013 | DI 依赖注入 | injects + bind |
| T-014 | Observer 观察者 | observe, emit |

---

## 3. EntityQueryContext 完整指南

### 3.1 基本概念

`EntityQueryContext` 是用于定义查询条件的上下文类，**必须继承它**才能使用 `component<>()` 函数。

```kotlin
// ✅ 正确：继承EntityQueryContext
class MyContext(world: World) : EntityQueryContext(world) {
    val health by component<Health>()  // 可以使用component
}

// ❌ 错误：不能在其他类中使用component
class BadClass(world: World) {
    val health by component<Health>()  // 编译错误！
}
```

### 3.2 内置属性

```kotlin
open class EntityQueryContext(override val world: World) : AccesserOperations(), WorldOwner {
    // ✅ 内置属性，直接使用
    val entity: Entity get()           // 当前查询的实体
    val entityType: EntityType get()   // 当前实体类型
    
    // ❌ 不需要定义
    // val entity by component<BasicInfo>()  // 错误！
}
```

### 3.3 component<> 四大规则

```kotlin
class CharacterContext(world: World) : EntityQueryContext(world) {
    
    // ✅ 规则1：普通类型 = 组件**必须存在**
    val basicInfo by component<BasicInfo>()
    
    // ✅ 规则2：nullable类型 = 组件**可能不存在**
    val equipment by component<Equipment?>()
    
    // ✅ 规则3：optionalGroup = One = 同组中**至少一个存在**
    val healthA by component<HealthA>(optionalGroup = OptionalGroup.One)
    val healthB by component<HealthB>(optionalGroup = OptionalGroup.One)
    
    // ✅ 规则4：configure() = 更复杂的查询条件
    override fun FamilyBuilder.configure() {
        relation(relations.component<Test>())  // 必须有Test组件
        relation(relations.relation<Owner>(target = targetEntity))  // 必须有关系
    }
}
```

### 3.4 Tag检查方式

```kotlin
class MyContext(world: World) : EntityQueryContext(world) {
    val health by component<Health>()
}

// 方式1：在filter中使用entity.hasTag<T>()
world.query { MyContext(world) }
    .filter { ctx -> ctx.entity.hasTag<ActiveTag>() }
    .toList()

// 方式2：通过可选组件检查
val activeHealth: Health? by component<Health>(optionalGroup = OptionalGroup.One)
world.query { MyContext(world) }
    .filter { ctx -> ctx.activeHealth != null }
    .toList()
```

### 3.5 速查表

| 语法 | 含义 | 示例 |
|------|------|------|
| `component<Component>()` | 必须存在 | `val basicInfo by component<BasicInfo>()` |
| `component<Component?>()` | 可能不存在 | `val equipment by component<Equipment?>()` |
| `component<C>(optionalGroup = One)` | 同组至少一个 | `val h1 by component<Health1>(optionalGroup = One)` |
| `ctx.entity.hasTag<Tag>()` | Tag检查 | `ctx.entity.hasTag<ActiveTag>()` |
| `component<C>().value` | 访问组件值 | `ctx.health.current` |

### 3.6 常见错误

```kotlin
// ❌ 错误1：entity重复定义
class Bad(world: World) : EntityQueryContext(world) {
    val entity by component<BasicInfo>()  // 已有内置entity！
}

// ✅ 正确
class Good(world: World) : EntityQueryContext(world) {
    val basicInfo by component<BasicInfo>()
}
world.query { Good(world) }.forEach { ctx ->
    println(ctx.entity)  // 直接使用内置entity
}

// ❌ 错误2：忘记空值检查
world.query { Good(world) }.forEach { ctx ->
    println(ctx.equipment!!.name)  // 可能为空！
}

// ✅ 正确：安全访问
world.query { Good(world) }.forEach { ctx ->
    ctx.equipment?.let { println(it.name) }
}
```

---

## 4. DI依赖注入（T-013）

```kotlin
/**
 * DI规则：
 * - Service必须继承EntityRelationContext
 * - 使用constructor注入其他Service
 * - 在world创建时注册
 */

// 注册Service
val world = world {
    injects {
        bind singleton { new(::HealthService) }
        bind singleton { new(::CombatService) }
    }
    components { world.componentId<Health>() }
}

// 使用Service（构造函数注入，推荐）
class CombatService(
    override val world: World,
    private val healthService: HealthService  // 自动注入
) : EntityRelationContext {
    fun attack(target: Entity, damage: Int) {
        healthService.damage(target, damage)
    }
}

// 延迟获取（依赖较多时使用）
class MyService(override val world: World) : EntityRelationContext {
    private val healthService: HealthService by world.di.instance()
}
```

---

## 5. Observer观察者（T-014）

```kotlin
/**
 * Observer规则：
 * 1. 定义事件（sealed class无数据，data class有数据）
 * 2. 在World创建时注册事件类型
 * 3. 创建观察者监听事件
 * 4. 触发事件
 */

// 1. 定义事件
sealed class OnHealthChanged
data class HealthChangedEvent(val old: Int, val new: Int)

// 2. 创建带观察者的实体
class CharacterFactory(override val world: World) : EntityRelationContext {
    fun createCharacter(): Entity {
        val entity = world.entity {
            it.addComponent(Health(100, 100))
            it.addTag<ActiveTag>()
        }
        // 观察血量变化（无数据事件）
        entity.observe<OnHealthChanged>().exec {
            println("Entity ${this.entity.id} health changed!")
        }
        // 观察血量变化（带数据事件）
        entity.observeWithData<HealthChangedEvent>().exec { event ->
            println("Health: ${event.old} -> ${event.new}")
        }
        return entity
    }
}

// 3. 触发事件
class HealthService(override val world: World) : EntityRelationContext {
    fun damage(entity: Entity, amount: Int) {
        val health = entity.getComponent<Health>()!!
        val newHealth = health.copy(current = maxOf(0, health.current - amount))
        entity.editor { it.addComponent(newHealth) }
        entity.emit<OnHealthChanged>()  // 触发无数据事件
        entity.emit(HealthChangedEvent(health.current, newHealth.current))  // 触发有数据事件
    }
}

// 4. World创建时注册事件类型
val world = world {
    components {
        world.componentId<OnHealthChanged>()
        world.componentId<HealthChangedEvent>()
    }
}

// 带过滤条件的观察者（简化写法）
entity.observe<OnHealthChanged>().exec { ctx ->
    if (ctx.entity.hasTag<PlayerTag>()) {  // ✅ 在exec中直接检查
        println("Player health changed!")
    }
}
```

---

## 6. API速查

### EntityRelationContext（读取）

| API | 说明 |
|-----|------|
| `entity.getComponent<C>()` | 获取组件 |
| `entity.hasComponent<C>()` | 检查组件 |
| `entity.hasTag<T>()` | 检查Tag |
| `entity.getRelation<R>(target)` | 获取关系 |

### EntityCreateContext（创建）

| API | 说明 |
|-----|------|
| `entity.addComponent(c)` | 添加组件 |
| `entity.addTag<T>()` | 添加Tag |
| `entity.addRelation<R>(target)` | 添加关系 |

### World上下文

| API | 说明 |
|-----|------|
| `world.entity { }` | 创建实体 |
| `world.query { }` | 查询实体 |
| `world.childOf(parent) { }` | 创建子实体 |
| `world.editor(entity) { }` | 编辑实体 |

### EntityQueryContext（查询）

| 语法 | 说明 | 示例 |
|------|------|------|
| `component<C>()` | 必须存在 | `val health by component<Health>()` |
| `component<C?>()` | 可能不存在 | `val equipment by component<Equipment?>()` |
| `component<C>(optionalGroup = One)` | 同组至少一个 | `val h1 by component<Health1>(optionalGroup = One)` |
| `ctx.entity.hasTag<T>()` | Tag检查 | `ctx.entity.hasTag<ActiveTag>()` |
| `.filter { }` | 运行时过滤 | `.filter { it.health.current > 0 }` |
| `.toList()` | 转换为List | `world.query { Ctx(world) }.toList()` |

---

## 附录B: 验证清单

> 本附录提供代码生成前后的检查清单。**所有代码生成必须通过验证清单**。

### B.1 生成前检查

#### B.1.1 需求验证

- [ ] 用户需求是否清晰？
  ```
  用户: "弟子有血量"
  分析: 需要 Component Health
  ✅ 清晰
  ```

- [ ] 需求是否可映射到 ECS 概念？
  ```
  需求: "弟子能战斗"
  分析: 需要 CombatService + damage() 方法
  ✅ 可映射
  ```

- [ ] 是否需要新增 Component/Tag/Relation？
  - [ ] 新增 Component: 参考 T-001
  - [ ] 新增 Tag: 参考 T-002
  - [ ] 新增 Relation: 参考 T-003

#### B.1.2 类型验证

- [ ] 是否选择正确的 ECS 类型？

| 需求 | 判断 | 类型 | 验证 |
|------|------|------|------|
| 存储血量 | 数据+实体自身+多属性 | Component | ✅ |
| 标记死亡 | 无数据+状态标记 | Tag | ✅ |
| 关联师徒 | 数据+另一实体 | Relation | ✅ |

- [ ] Component 是否正确？

| 检查项 | 正确 | 错误 |
|--------|------|------|
| 单属性 | @JvmInline value class | data class |
| 多属性 | data class | 混合 |
| 原子化 | 单一职责 | 混合职责 |

- [ ] Tag 是否正确？
  - [ ] 使用 sealed class
  - [ ] 无数据字段
  - [ ] 命名符合规范（XxxTag）

- [ ] Relation 是否正确？
  - [ ] sealed class 定义关系类型
  - [ ] data class 定义关系数据（如果需要）
  - [ ] 可复用（同一类型可关联多个目标）

#### B.1.3 模板验证

- [ ] 是否找到对应模板？
  - [ ] T-001: 创建实体
  - [ ] T-002: Tag 操作
  - [ ] T-003: 创建关系
  - [ ] T-004: 查询实体
  - [ ] T-005: 更新组件
  - [ ] T-006: 层级关系
  - [ ] T-007: Tag 检查
  - [ ] T-008: 批量处理
  - [ ] T-009: 条件查询
  - [ ] T-010: 删除实体
  - [ ] T-011: 组件存在性
  - [ ] T-012: 实体复制
  - [ ] T-013: DI 依赖注入
  - [ ] T-014: Observer 观察者

- [ ] 占位符是否都已替换？
  - [ ] [EntityType]
  - [ ] [Component]
  - [ ] [Tag]
  - [ ] [Relation]
  - [ ] [Service]

### B.2 生成后检查

#### B.2.1 语法检查

- [ ] 无 `has<>` 语法（应使用 `component<>`）
- [ ] 无 `where{}` 语法（应使用 `filter{}`）
- [ ] 无直接修改 Component（应使用 `copy()`）
- [ ] Service 继承 `EntityRelationContext`
- [ ] EntityQueryContext 继承 `EntityQueryContext(world)`

#### B.2.2 语义检查

- [ ] Component 使用正确的类型（data class / value class）
- [ ] 单属性使用 `@JvmInline value class`
- [ ] 原子化拆分（无混合职责）
- [ ] Tag 无数据字段
- [ ] Relation 可复用

#### B.2.3 规范检查

- [ ] 命名符合规范
  - Component: 名词（Health, Position）
  - Tag: 形容词+Tag（ActiveTag, DeadTag）
  - Relation: 名词（Mentorship, Ownership）
  - Service: 功能+Service（HealthService）
  - value class: 名词（Level, Experience）

- [ ] 注释使用正确格式
  - [ ] `// ✅` 和 `// ❌`
  - [ ] 无过长注释
  - [ ] 关键逻辑有注释

- [ ] 文件路径标注正确
  - [ ] Component: `src/components/[模块]/`
  - [ ] Tag: `src/tags/[模块]/`
  - [ ] Relation: `src/relations/[模块]/`
  - [ ] Service: `src/services/[模块]/`

#### B.2.4 性能检查

- [ ] 无嵌套查询
- [ ] 无循环中重复查询
- [ ] 查询结果有缓存（如果需要）

### B.3 快速验证脚本

```bash
#!/bin/bash
# 验证 ECS 代码规范

echo "=== ECS 代码规范验证 ==="

# 检查 has<> 语法
if grep -r "has<" src/; then
    echo "❌ 发现 has<> 语法，应使用 component<>"
    exit 1
fi

# 检查 where{} 语法
if grep -r "where {" src/; then
    echo "❌ 发现 where{} 语法，应使用 filter{}"
    exit 1
fi

# 检查直接修改 Component
if grep -r "getComponent.*!!.*=" src/; then
    echo "❌ 发现直接修改 Component，应使用 copy() + editor"
    exit 1
fi

echo "✅ 所有检查通过"
```

### B.4 验证结果模板

```markdown
## 验证结果

### ✅ 通过项
- [x] Component 使用 data class
- [x] 单属性使用 value class
- [x] 无 has<> 语法
- [x] 无 where{} 语法
- [x] 命名符合规范

### ❌ 未通过项
- [ ]

### 需要修正
- [ ]
```

---

## 附录C: 常见错误与修正

> 本附录收集 ECS 开发中的常见错误，并提供修正方案。

---

### C.1 Query 系统错误

#### 错误 C1.1: 使用 `has<Component>()`

**错误代码**:
```kotlin
// ❌ 错误：has<> 不存在
class DiscipleContext(world: World) : EntityQueryContext(world) {
    val health by has<Health>()  // ❌ 不存在
}
```

**原因**:
- `has<Component>()` 不是有效 API
- EntityQueryContext 中应使用 `component<>()`

**正确代码**:
```kotlin
// ✅ 正确：使用 component<>()
class DiscipleContext(world: World) : EntityQueryContext(world) {
    val health by component<Health>()  // ✅ 必须存在
}
```

**错误代码**:
```kotlin
// ❌ 错误：has<> 用于查询
val entities = world.query { has<Health>() }
```

**正确代码**:
```kotlin
// ✅ 正确：使用 component<>()
class HealthContext(world: World) : EntityQueryContext(world) {
    val health by component<Health>()
}
val entities = world.query { HealthContext(world) }
```

#### 错误 C1.2: 使用 `where{}`

**错误代码**:
```kotlin
// ❌ 错误：where{} 不存在
val entities = world.query { DiscipleContext(world) }
    .where { it.health.current > 0 }
```

**原因**:
- `where{}` 不是有效 API
- 应使用 `filter{}` 进行运行时条件过滤

**正确代码**:
```kotlin
// ✅ 正确：使用 filter{}
val entities = world.query { DiscipleContext(world) }
    .filter { it.health.current > 0 }
    .toList()
```

#### 错误 C1.3: EntityQueryContext 中重复定义 `entity`

**错误代码**:
```kotlin
// ❌ 错误：entity 是内置属性
class BadContext(world: World) : EntityQueryContext(world) {
    val entity by component<BasicInfo>()  // ❌ entity 已存在
}
```

**原因**:
- EntityQueryContext 已有内置 `entity` 属性
- 重复定义会导致冲突

**正确代码**:
```kotlin
// ✅ 正确：直接使用内置 entity
class GoodContext(world: World) : EntityQueryContext(world) {
    val basicInfo by component<BasicInfo>()  // ✅ 组件名
}
world.query { GoodContext(world) }.forEach { ctx ->
    println(ctx.entity)  // ✅ 使用内置 entity
    println(ctx.basicInfo.name)  // ✅ 使用组件
}
```

#### 错误 C1.4: Tag 检查方式错误

**错误代码**:
```kotlin
// ❌ 错误：Tag 检查不应在 component<> 中
class BadContext(world: World) : EntityQueryContext(world) {
    val active by component<ActiveTag>()  // ❌ Tag 不是 Component
}
```

**正确代码**:
```kotlin
// ✅ 正确：Tag 检查在 filter 中
class GoodContext(world: World) : EntityQueryContext(world) {
    val health by component<Health>()
}
world.query { GoodContext(world) }
    .filter { ctx -> ctx.entity.hasTag<ActiveTag>() }
    .toList()
```

---

### C.2 Component 设计错误

#### 错误 C2.1: 单属性使用 data class

**错误代码**:
```kotlin
// ❌ 错误：单属性应使用 value class
data class Level(val value: Int)
data class Exp(val value: Long)
```

**原因**:
- value class 性能更好（无装箱拆箱）
- 语义更清晰（表明是单一值）

**正确代码**:
```kotlin
// ✅ 正确：单属性使用 @JvmInline value class
@JvmInline value class Level(val value: Int)
@JvmInline value class Exp(val value: Long)
```

#### 错误 C2.2: 混合职责的 Component

**错误代码**:
```kotlin
// ❌ 错误：混合了属性、位置、状态
data class PlayerAllInOne(
    val health: Int,
    val mana: Int,
    val positionX: Float,
    val positionY: Float,
    val level: Int,
    val exp: Long
)
```

**原因**:
- 难以复用（替换一个属性需要整个组件）
- 难以维护（修改一个属性影响所有使用点）
- 查询效率低（不需要的属性也会被加载）

**正确代码**:
```kotlin
// ✅ 正确：原子化拆分
data class Health(val current: Int, val max: Int)
data class Mana(val current: Int, val max: Int)
data class Position(val x: Float, val y: Float)
@JvmInline value class Level(val value: Int)
@JvmInline value class Exp(val value: Long)
```

#### 错误 C2.3: 使用可空字段代替可选组件

**错误代码**:
```kotlin
// ❌ 错误：使用可空字段
data class PlayerWithOptional(
    val name: String,
    val nickname: String?  // 可空字段
)
```

**正确代码**:
```kotlin
// ✅ 正确：拆分为可选组件
data class RequiredInfo(val name: String)
data class OptionalNickname(val nickname: String)
```

---

### C.3 Relation 使用错误

#### 错误 C3.1: Tag 带数据

**错误代码**:
```kotlin
// ❌ 错误：Tag 不能带数据
sealed class MarriedTag(val spouseName: String)  // ❌ Tag 无数据
```

**原因**:
- Tag 是无数据的状态标记
- 需要存储数据时使用 Relation

**正确代码**:
```kotlin
// ✅ 正确：Relation 带数据
sealed class Marriage
data class MarriageData(val spouseName: String, val weddingDate: Long)
```

#### 错误 C3.2: Relation 类型不一致

**错误代码**:
```kotlin
// ❌ 错误：addRelation 泛型和 data 类型不一致
sealed class Mentorship
data class MentorshipData(val year: Int)

// 错误：泛型是 Mentorship，data 是 MentorshipData
disciple.editor {
    it.addRelation<Mentorship>(target = mentor, data = MentorshipData(2024))
}
```

**正确代码**:
```kotlin
// ✅ 正确：泛型和 data 都是 MentorshipData
disciple.editor {
    it.addRelation<MentorshipData>(target = mentor, data = MentorshipData(2024))
}
```

#### 错误 C3.3: 不可复用的 Relation 设计

**错误代码**:
```kotlin
// ❌ 错误：每个关系都是新类型
sealed class PlayerItemRelation
sealed class PlayerBuildingRelation
sealed class PlayerPetRelation
```

**正确代码**:
```kotlin
// ✅ 正确：使用可复用的 Ownership
sealed class Ownership  // 定义一次，可复用
```

```kotlin
// 道具属于玩家
item.editor { it.addRelation<Ownership>(target = player) }

// 建筑属于玩家
building.editor { it.addRelation<Ownership>(target = player) }

// 宠物属于玩家
pet.editor { it.addRelation<Ownership>(target = player) }
```

---

### C.4 Service 错误

#### 错误 C4.1: Service 中保存状态

**错误代码**:
```kotlin
// ❌ 错误：Service 保存状态
class BadService(override val world: World) : EntityRelationContext {
    private var counter = 0  // ❌ 保存状态
    fun increment() { counter++ }
}
```

**原因**:
- Service 应该是无状态的
- 状态应该存储在 Component 中

**正确代码**:
```kotlin
// ✅ 正确：状态存储在 Component
data class Counter(val value: Int)

class GoodService(override val world: World) : EntityRelationContext {
    fun increment(entity: Entity) {
        val counter = entity.getComponent<Counter>()!!
        entity.editor {
            it.addComponent(counter.copy(value = counter.value + 1))
        }
    }
}
```

#### 错误 C4.2: Service 不继承 EntityRelationContext

**错误代码**:
```kotlin
// ❌ 错误：Service 未继承
class BadService(val w: World) {  // ❌ 未继承
    fun damage(entity: Entity, amount: Int) {
        ...
    }
}
```

**正确代码**:
```kotlin
// ✅ 正确：继承 EntityRelationContext
class GoodService(override val world: World) : EntityRelationContext {
    fun damage(entity: Entity, amount: Int) {
        ...
    }
}
```

---

### C.5 修改 Component 错误

#### 错误 C5.1: 直接修改 Component 属性

**错误代码**:
```kotlin
// ❌ 错误：直接修改
val health = entity.getComponent<Health>()!!
health.current = 50  // ❌ 不能直接修改
```

**原因**:
- Component 是不可变数据
- 需要通过 editor 修改

**正确代码**:
```kotlin
// ✅ 正确：使用 copy() + editor
val health = entity.getComponent<Health>()!!
entity.editor {
    it.addComponent(health.copy(current = 50))
}
```

#### 错误 C5.2: 忘记空值检查

**错误代码**:
```kotlin
// ❌ 错误：忘记空值检查
val health = entity.getComponent<Health>()!!  // 可能为空！
println(health.current)
```

**正确代码**:
```kotlin
// ✅ 正确：安全访问
val health = entity.getComponent<Health>()
if (health != null) {
    println(health.current)
}

// 或使用 let
entity.getComponent<Health>()?.let { health ->
    println(health.current)
}
```

---

### C.6 性能相关错误

#### 错误 C6.1: 循环中重复查询

**错误代码**:
```kotlin
// ❌ 错误：循环中重复查询
for (i in 0..100) {
    val entities = world.query { DiscipleContext(world) }.toList()
    // 使用 entities
}
```

**正确代码**:
```kotlin
// ✅ 正确：查询一次，缓存结果
val entities = world.query { DiscipleContext(world) }.toList()
for (i in 0..100) {
    // 使用 entities
}
```

#### 错误 C6.2: 嵌套查询

**错误代码**:
```kotlin
// ❌ 错误：嵌套查询 O(n²)
for (disciple in world.query { DiscipleContext(world) }) {
    val mentor = disciple.entity.getRelation<Mentorship>(target = target)
    // ...
}
```

**正确代码**:
```kotlin
// ✅ 正确：单次查询
class MentorContext(world: World) : EntityQueryContext(world) {
    val mentorship by component<Mentorship>()
    override fun FamilyBuilder.configure() {
        relation(relations.relation<Mentorship>(target = target))
    }
}
val results = world.query { MentorContext(world) }.toList()
```

---

### C.7 错误速查表

| 错误类型 | 错误写法 | 正确写法 | 页码 |
|----------|----------|----------|------|
| Query API | `has<Component>()` | `component<Component>()` | C1.1 |
| Query API | `where {}` | `filter {}` | C1.2 |
| Query Context | 重复定义 entity | 使用内置 entity | C1.3 |
| Tag 检查 | `component<Tag>()` | `entity.hasTag<Tag>()` | C1.4 |
| Component | `data class L(v: Int)` | `@JvmInline value class L(v: Int)` | C2.1 |
| Component | 混合职责 | 原子化拆分 | C2.2 |
| Component | 可空字段 | 拆分为可选组件 | C2.3 |
| Tag | Tag 带数据 | 改用 Relation | C3.1 |
| Relation | 类型不一致 | 泛型和 data 一致 | C3.2 |
| Service | 保存状态 | 状态在 Component | C4.1 |
| Service | 未继承 | 继承 EntityRelationContext | C4.2 |
| 修改组件 | 直接修改 | `copy() + editor` | C5.1 |
| 空值检查 | 忘记检查 | `?.` 或 `?.let{}` | C5.2 |
| 性能 | 循环中重复查询 | 查询一次 | C6.1 |
| 性能 | 单次查询 | 嵌套查询 | C6.2 |

---

## 附录：文件结构

```
src/
├── components/       # Component定义
│   ├── BasicComponents.kt
│   └── CombatComponents.kt
├── relations/        # Relation类型
├── services/        # Service实现
└── systems/         # System实现
```
