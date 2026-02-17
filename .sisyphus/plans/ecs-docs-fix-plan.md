# ECS 文档修改计划

## 修改目标

修正 ECS 文档中违背核心观点的地方，确保文档与实际 API 一致。

## 核心观点（需确保文档一致）

1. **查询方式**：使用 `world.query { }` + EntityQueryContext，不是 `familyService.family`
2. **relation 查询**：需要 target 参数 `relation<K>(target)`
3. **单属性优先**：使用 value class 而不是 data class
4. **查询上下文四种声明方式**：
   - 基础组件：`val x: T by component<T>()`
   - 可选组件：`val x: T? by component<T>()`
   - 可选组：`val x: T? by component<T>(OptionalGroup.One)`
   - 可写组件：`var x: T by component<T>()`

---

## 需要修改的文件

### 1. docs/technology/ecs/00-quick-start.md

**当前问题**：使用 `world.familyService.family`

**修改位置**：第 81-98 行

**当前代码**：
```kotlin
// ✅ 正确：基础查询
val query = world.familyService.family { component<Health>() }
val filtered = query.filter { it.current > 50 }
val result = world.familyService.family { component<Health>() }
    .filter { it.current > 0 }
    .map { it.current }
    .toList()
```

**修改为**：
```kotlin
import cn.jzl.ecs.query.EntityQueryContext
import cn.jzl.ecs.family.FamilyBuilder
import cn.jzl.ecs.family.component

// ✅ 正确：定义查询上下文类
class HealthContext(world: World) : EntityQueryContext(world) {
    val health: Health by component()
}

// ✅ 正确：执行查询
val query = world.query { HealthContext(this) }

// ✅ 正确：遍历
query.forEach { ctx, entity ->
    println("生命值: ${ctx.health.current}")
}

// ✅ 正确：过滤
val filtered = world.query { HealthContext(this) }
    .filter { it.health.current > 50 }

// ✅ 正确：链式操作
val result = world.query { HealthContext(this) }
    .filter { it.health.current > 0 }
    .map { it.health.current }
    .toList()
```

**同时修改**：第 186-193 行的快速参考卡

---

### 2. docs/technology/ecs/01-core-concepts.md

**当前问题**：
- 第 230 行：`world.familyService.family { component<Health>() }`
- 缺少查询上下文四种声明方式说明

**需要添加**：查询上下文声明方式说明

**修改位置**：第 264-279 行（QueryContext 部分）

**添加内容**：
```kotlin
// 查询上下文四种声明方式

// 1. 基础组件 - 必须存在
class PositionContext(world: World) : EntityQueryContext(world) {
    val position: Position by component()  // 必须有 Position
}

// 2. 可选组件 - 可以不存在
class OptionalContext(world: World) : EntityQueryContext(world) {
    val nickname: Nickname? by component()  // 可以不存在
}

// 3. 可选组 - 同组至少满足一个
class OptionalGroupContext(world: World) : EntityQueryContext(world) {
    val weapon: Weapon? by component(OptionalGroup.One)
    val armor: Armor? by component(OptionalGroup.One)
    // weapon 或 armor 至少有一个
}

// 4. 可写组件 - 遍历中可修改
class WritableContext(world: World) : EntityQueryContext(world) {
    var velocity: Velocity by component()
    // 遍历中可修改 ctx.velocity = ...
}
```

---

### 3. docs/technology/ecs/02-patterns.md

**当前问题**：
- 第 372 行：`world.family { component<OwnerBy>() }`
- 第 408 行：`world.family { component<ChildOf>() }`
- 第 418 行：`world.family { component<Transform>() and component<ChildOf>() }`
- 第 458 行：`world.family { component<InstanceOf>() }`
- 第 526 行：`world.family { component<SharedOf>() }`

**修改方案**：所有 `world.family { }` 改为 `world.query { }` + EntityQueryContext

---

### 4. docs/technology/ecs/05-relation-system.md

**当前问题**：
- 第 320 行：`world.family { component<OwnerBy>() }`
- 第 358 行：`world.family { component<ChildOf>() }`
- 第 368 行：`world.family { component<Transform>() and component<ChildOf>() }`
- 第 404 行：`world.family { component<InstanceOf>() }`
- 第 462-479 行：多处 `world.family { }`
- 第 528, 598 行：`world.family { component<OwnerBy>() }`

**同时需要添加**：
- relation 查询需要 target 参数的说明
- 查询上下文四种声明方式

---

### 5. docs/technology/ecs/CHEATSHEET.md

**当前问题**：
- 第 64 行：`world.familyService.family { component<Health>() }`
- 第 65 行：多组件查询
- 第 68 行：链式操作
- 第 101, 149, 174 行：`world.familyService.family`

**修改为 query 方式**

---

## 修改优先级

| 优先级 | 文件 | 修改量 |
|--------|------|--------|
| 高 | 00-quick-start.md | 小 |
| 高 | 01-core-concepts.md | 中（需添加查询上下文说明）|
| 中 | CHEATSHEET.md | 小 |
| 低 | 02-patterns.md | 大 |
| 低 | 05- 大 |

---

##relation-system.md | 执行步骤

1. 先修改 `00-quick-start.md`（基础指南）
2. 修改 `01-core-concepts.md`（核心概念，添加查询上下文说明）
3. 修改 `CHEATSHEET.md`（速查表）
4. 修改 `02-patterns.md`（设计模式）
5. 修改 `05-relation-system.md`（关系系统）

---

## 验证方法

修改完成后，检查所有 `.md` 文件中不再包含：
- `world.familyService.family`
- `world.family {`

全部替换为：
- `world.query { XxxContext(this) }`
