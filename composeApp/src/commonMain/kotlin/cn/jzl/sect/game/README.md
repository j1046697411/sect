# 游戏核心模块 (Game Core)

本模块是《宗门修真录》的核心游戏逻辑层，基于 ECS（Entity-Component-System）架构实现。

## 架构概览

```
game/
├── components/          # 数据组件（Component）
│   ├── core/           # 基础组件
│   ├── disciple/       # 弟子组件
│   ├── cultivation/    # 修炼组件
│   ├── facility/       # 设施组件
│   └── resources/      # 资源组件
│
├── tags/               # 状态标签（Tag）
│   ├── status/         # 行为/健康状态
│   └── lifecycle/      # 生命周期状态
│
├── relations/          # 实体关系（Relation）
│
├── services/           # 业务系统（Service/System）
│   ├── time/          # 时间管理
│   ├── ai/            # AI 决策
│   ├── cultivation/   # 修炼系统
│   ├── disciple/      # 弟子管理
│   └── events/        # 事件系统
│
└── context/           # 查询上下文
```

## 核心概念

### 1. ECS 架构

| 概念 | 说明 | 示例 |
|------|------|------|
| **Entity** | 游戏对象，只有ID | 弟子、设施、物品 |
| **Component** | 数据组件 | Health、Position、CultivationRealm |
| **Tag** | 状态标记 | CultivatingTag、AliveTag |
| **Relation** | 实体关联 | Mentorship、SectMembership |
| **Service** | 业务逻辑 | CultivationService、AIDecisionService |

### 2. 设计原则

1. **数据与逻辑分离**：Component 存储数据，Service 处理逻辑
2. **原子化组件**：每个 Component 只包含一个概念的数据
3. **不可变性**：Component 不可变，通过 copy() 更新
4. **类型安全**：使用 Kotlin 类型系统确保编译时安全

### 3. 性能优化

- **Archetype 存储**：相同组件组合的实体连续存储
- **批量处理**：每帧处理 20 个 AI 实体
- **查询缓存**：Family 匹配结果缓存
- **value class**：单属性组件无装箱开销

## 模块间依赖关系

```
components/core       (基础，无依赖)
    ↑
components/disciple   (依赖 core)
components/cultivation (依赖 core, disciple)
components/resources   (依赖 core, cultivation)
components/facility    (依赖 core, resources)
    ↑
tags/status           (依赖 components)
tags/lifecycle        (依赖 components)
    ↑
relations             (依赖 components, tags)
    ↑
context               (依赖所有 components)
    ↑
services/*            (依赖所有下层模块)
    ↑
world                 (依赖所有模块)
```

## 快速开始

### 1. 创建新组件

参考决策树：
```
需要存储数据？
  ├─ 是 → 单属性？→ @JvmInline value class
  │              多属性？→ data class
  └─ 否 → Tag (sealed class)
```

```kotlin
// 文件: components/disciple/NewAttribute.kt
@JvmInline
value class NewAttribute(val value: Int)
```

### 2. 创建新 Service

```kotlin
// 文件: services/new/NewService.kt
class NewService(override val world: World) : EntityRelationContext, System {
    override fun update(deltaTime: Float) {
        // 业务逻辑
    }
}
```

### 3. 在 World 中注册

```kotlin
// 文件: world/SectWorld.kt
world = createWorld {
    components {
        componentId<NewAttribute>()
    }
    systems {
        addSystem<NewService>()
    }
}
```

## 开发规范

### 命名规范

| 类型 | 命名规则 | 示例 |
|------|----------|------|
| Component | 名词，描述属性 | Health, Position |
| Tag | 形容词/状态 + Tag | ActiveTag, DeadTag |
| Relation | 名词，描述关系 | Mentorship, Ownership |
| Service | [功能] + Service | HealthService |
| Context | [实体类型] + Context | DiscipleContext |

### 代码规范

1. **组件字段**：使用 `val`，不可变
2. **组件更新**：使用 `copy()` 创建新实例
3. **实体编辑**：使用 `entity.editor {}` 块
4. **查询条件**：在 `filter {}` 中组合

## 性能基准

| 操作 | 目标性能 | 测试方法 |
|------|----------|----------|
| 实体创建 | ≥100k/s | 批量创建10万个实体 |
| 组件查询 | ≥50k/ms | 查询包含指定组件的实体 |
| AI 决策 | 1000实体/秒 | 1000个实体一轮决策 |

## 调试工具

### 1. 实体浏览器

```kotlin
// 查看所有实体
fun debugPrintAllEntities() {
    world.query { EntityContext(world) }.forEach { ctx ->
        println("Entity ${ctx.entity.id}: ${ctx.components}")
    }
}
```

### 2. 性能监控

```kotlin
// 统计各类实体数量
fun debugEntityCounts() {
    val counts = mapOf(
        "Disciples" to world.query { DiscipleContext(world) }.count(),
        "Facilities" to world.query { FacilityContext(world) }.count(),
        "Events" to world.query { EventContext(world) }.count()
    )
    println(counts)
}
```

## 扩展指南

### 添加新功能模块

1. **在 components 下创建新目录**
2. **定义数据组件**（参考决策树）
3. **创建 Service**（继承 EntityRelationContext）
4. **创建 Context**（继承 EntityQueryContext）
5. **在 World 中注册**
6. **编写 README**（本文档格式）

### 示例：添加炼丹系统

```kotlin
// 1. components/alchemy/AlchemyComponents.kt
data class AlchemyRecipe(
    val ingredients: Map<ResourceType, Int>,
    val output: PillId,
    val successRate: Float
)

// 2. services/alchemy/AlchemyService.kt
class AlchemyService(override val world: World) : EntityRelationContext, System {
    override fun update(deltaTime: Float) {
        // 处理炼丹逻辑
    }
}

// 3. world/SectWorld.kt
world = createWorld {
    components {
        componentId<AlchemyRecipe>()
    }
    systems {
        addSystem<AlchemyService>()
    }
}
```

## 相关文档

- [ECS 架构规范](../../../../../docs/ecs-architecture.md) - AI 编程助手参考手册
- [核心需求文档](../../../../../docs/sect_cultivation_core_gameplay.md) - 游戏设计文档
- [实现规范](../../../../../docs/宗门修真录游戏实现规范.md) - 详细实现规范

## 开发路线图

### Phase 1: 基础框架 ✓
- [x] 组件定义
- [x] 标签定义
- [x] 关系定义
- [x] World 配置

### Phase 2: 核心系统
- [ ] TimeService - 时间流逝
- [ ] AIDecisionService - AI 决策
- [ ] CultivationService - 修炼系统

### Phase 3: 管理功能
- [ ] DiscipleService - 弟子管理
- [ ] FacilityService - 设施管理
- [ ] ResourceService - 资源管理

### Phase 4: 游戏循环
- [ ] EventService - 事件系统
- [ ] CombatService - 战斗系统
- [ ] SocialService - 社交系统

## 贡献指南

1. **遵循 ECS 规范**：参考 `ecs-architecture.md`
2. **保持原子化**：组件职责单一
3. **编写文档**：每个模块需要 README.md
4. **测试覆盖**：核心逻辑需要单元测试
