# ECS 框架 - Agent 操作指引

> 本文档指引 AI Agent 如何正确理解和使用 ECS 框架。

---

## 核心原则

1. **一切都是关系（Relation）** - 组件、标签、关系本质上都是 Relation
2. **数据驱动** - 状态存储在组件中，系统处理数据
3. **无状态系统** - System 应为无状态工具类，状态存组件
4. **类型安全** - 严格区分 Component、Tag、Relation
5. **结构修改受限** - 所有修改实体结构的操作（如添加/移除组件、标签、关系）必须在 `editor { ... }` 上下文内进行。

---

## 关键概念映射

| 概念 | 代码表示 | 说明 |
|------|----------|------|
| 组件 | `data class` / `value class` | 存储数据的容器 |
| 标签 | `sealed class` | 无数据的标记 |
| 关系 | `sealed class` + `target: Entity` | 实体间关联 |
| 实体 | `Entity` (Int ID) | 唯一标识符 |
| 世界 | `World` | 实体容器 |

---

## 常用 API

### 1. 创建 World

```kotlin
val world = world {
    install(gameAddon)
}
```

### 2. 创建实体

```kotlin
val entity = world.entity {
    it.addComponent(Health(100, 100))
    it.addComponent(Position(0, 0))
    it.addTag<PlayerTag>()
}
```

### 3. 查询

```kotlin
class HealthContext(world: World) : EntityQueryContext(world) {
    val health: Health by component()
}

world.query { HealthContext(this) }
    .filter { it.health.current > 0 }
    .forEach { ctx -> /* 处理 */ }
```

### 4. 关系

```kotlin
// 添加普通关系 (在 editor 上下文内)
world.editor(entity) {
    it.addRelation<OwnerBy>(player)
}

// 添加 Single Relation（单目标约束关系，旧目标自动移除）
// 假设 HeroHasWeapon 是一个 Single Relation 类型
world.editor(heroEntity) {
    // 第一次添加武器
    it.addRelation<HeroHasWeapon>(swordEntity)
    // 第二次添加武器，会自动移除 swordEntity，将 longbowEntity 设置为新目标
    it.addRelation<HeroHasWeapon>(longbowEntity)
}

// 获取关系
val owner = entity.getRelation<OwnerBy>(player) // 获取指定目标的关系数据
val weapon = heroEntity.getRelation<Weapon, HeroHasWeapon>() // 获取 Single Relation 的目标实体

// 父子关系
val child = parent.childOf { it.addComponent(Position(0, 0)) }

// 实例化
val instance = prefab.instanceOf { it.addComponent(Level(1)) }
```

---

## 重要文件位置

| 内容 | 位置 |
|------|------|
| 组件定义 | `business-modules/business-core/components/` |
| 标签定义 | `business-modules/business-core/tags/` |
| 系统实现 | `business-modules/*/systems/` |
| ECS 核心 | `libs/lko-ecs/src/commonMain/kotlin/cn/jzl/ecs/` |

---

## 文档索引

| 文档 | 用途 |
|------|------|
| [00-quick-start.md](00-quick-start.md) | 快速入门 |
| [01-core-concepts.md](01-core-concepts.md) | 核心概念 |
| [02-patterns.md](02-patterns.md) | 设计模式 |
| [03-anti-patterns.md](03-anti-patterns.md) | 反模式 |
| [04-templates.md](04-templates.md) | 代码模板 |
| [05-relation-system.md](05-relation-system.md) | 关系系统 |
| [06-component-store.md](06-component-store.md) | 组件存储特化 |
| [07-performance.md](07-performance.md) | 性能优化 |
| [08-testing.md](08-testing.md) | 测试指南 |

---

## 常见任务指引

### 添加新组件

1. 在 `business-modules/business-core/components/` 定义组件
2. 在 `createAddon` 中注册：`world.componentId<YourComponent>()`
3. 如需特化存储：`world.componentId<YourComponent> { it.store { intStore() } }`

### 添加新系统

1. 继承 `EntityRelationContext`
2. 使用 `world.query { }` 进行查询
3. 分离读写操作（先收集再修改）

### 添加 Relation

1. 定义 sealed class：`sealed class YourRelation`
2. 注册：`world.componentId<YourRelation>()`
3. 使用：
   - 对于普通关系：`world.editor(entity) { it.addRelation<YourRelation>(target) }`
   - 对于单目标约束关系：`world.editor(entity) { it.addRelation<YourSingleRelation>(target) }`
   - 获取单目标约束关系的数据：`entity.getRelation<DataType, YourSingleRelation>()`

---

## 性能注意事项

1. **使用 value class**：单属性组件使用 `@JvmInline value class`
2. **特化存储**：数值类型使用 `intStore()`、`floatStore()` 等
3. **缓存查询**：QueryContext 使用 `lazy` 缓存
4. **批量操作**：先收集到列表，再批量修改
5. **零分配**：核心循环中避免创建新对象

---

## 测试要求

- 使用 `createAddon` 快速构建测试 World
- 遵循 Given-When-Then 风格
- 运行 `./gradlew :libs:lko-ecs:test` 验证

---

## 禁止事项

- ❌ 在 query forEach 中直接修改实体结构 (必须在 `editor { ... }` 中进行)
- ❌ 在非 `editor` 上下文直接修改实体结构 (例如 `entity.addComponent(...)` 必须是 `world.editor(entity) { it.addComponent(...) }`)
- ❌ 直接修改组件属性（使用 `copy()`）
- ❌ 在 Service 中保存状态
- ❌ 忘记注册 Component/Tag/Relation
