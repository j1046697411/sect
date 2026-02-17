# ECS 框架补充计划：Phase/执行顺序 + 定时器系统

## TL;DR

> **快速 summary**: 为 ECS 框架补充运行时系统调度（Phase）和定时器组件，支撑游戏循环控制和修炼/技能冷却功能。
> 
> **核心交付**:
> - 运行时的 Phase 系统（UPDATE_PHASE 枚举 + Pipeline 增强）
> - 定时器组件（TimerComponent + TimerService）
> - 单元测试覆盖
> 
> **预估工作量**: 中等（Medium）
> **并行执行**: YES - 两个功能可并行开发
> **关键路径**: Phase 系统 → 定时器系统

---

## Context

### 原始需求

用户明确指出 ECS 框架缺少两个关键功能：

1. **Phase/执行顺序** - 游戏循环核心
   - 现状：只有启动阶段的 Phase (ADDONS_CONFIGURED, INIT_COMPONENTS 等)
   - 问题：缺少运行时每帧的系统执行顺序控制

2. **定时器系统** - 修炼/技能冷却需要
   - 现状：完全没有定时器相关实现
   - 问题：无法实现修炼进度、技能冷却、持续效果等时间相关逻辑

### 用户决策

| 功能 | 选择 | 说明 |
|------|------|------|
| Phase 复杂度 | 基础版 | 简单固定阶段，执行顺序明确 |
| 定时器实现 | 组件式 | 定时器作为组件附加在实体上 |
| 开发顺序 | 先 Phase 后定时器 | Phase 是基础设施 |
| 系统注册方式 | Addon 内注册 → 依赖注入 | 自动发现所有实现类 |
| 阶段指定方式 | - | 属性方式（每个实现类设置 phase 属性） |

### 技术约束

- 必须遵循现有 ECS 架构风格（无状态 Service、组件不可变）
- 需要与现有 Pipeline 集成
- 需要保持 95%+ 测试覆盖率（lko-ecs 核心要求）
- 需要支持 KMP 多平台（JVM/Android/JS/Wasm）

---

## Work Objectives

### 核心目标

1. **Phase/执行顺序系统**
   - 添加运行时游戏循环的阶段枚举（UPDATE_PHASE）
   - 增强 Pipeline 支持每帧系统调度
   - 提供简洁的 API 供业务系统使用

2. **定时器系统**
   - 设计 TimerComponent（计时器组件）
   - 实现 TimerService（定时器服务）
   - 支持一次性定时器、循环定时器、冷却时间

### 具体交付物

| 文件 | 说明 |
|------|------|
| `libs/lko-ecs/src/commonMain/kotlin/.../addon/UpdatePhase.kt` | 运行阶段枚举 |
| `libs/lko-ecs/src/commonMain/kotlin/.../Pipeline.kt` | 增强 Pipeline 接口 |
| `libs/lko-ecs/src/commonMain/kotlin/.../PipelineImpl.kt` | 增强 Pipeline 实现 |
| `libs/lko-ecs/src/commonMain/kotlin/.../Updatable.kt` | Updatable 接口 |
| `libs/lko-ecs/src/commonMain/kotlin/.../Updatable.kt` | Updatable 接口 |
| `libs/lko-ecs/src/commonMain/kotlin/.../component/TimerComponent.kt` | 定时器组件 |
| `libs/lko-ecs/src/commonMain/kotlin/.../service/TimerService.kt` | 定时器服务 |
| `libs/lko-ecs/src/commonTest/kotlin/.../PipelineUpdateTest.kt` | Phase 测试 |
| `libs/lko-ecs/src/commonTest/kotlin/.../TimerServiceTest.kt` | 定时器测试 |

### API 设计（依赖注入 + 属性指定阶段）

```kotlin
// Updatable 接口 - 不继承任何接口
interface Updatable {
    val phase: UpdatePhase  // 通过属性指定阶段
    fun update(dt: Duration)
}

// 具体系统实现 - 同时实现 Updatable 和 EntityRelationContext
// world 通过构造器注入
class MovementSystem(override val world: World) : Updatable, EntityRelationContext {
    override val phase: UpdatePhase = UpdatePhase.UPDATE
    
    override fun update(dt: Duration) {
        // 可以直接使用 world
        world.query { PositionContext(this) }.forEach { ctx ->
            // 移动逻辑
        }
    }
}

class AnimationSystem(override val world: World) : Updatable, EntityRelationContext {
    override val phase: UpdatePhase = UpdatePhase.LATE_UPDATE
    
    override fun update(dt: Duration) {
        // 动画逻辑
    }
}

// 使用依赖注入自动获取所有 Updatable
class PipelineImpl(override val world: World) : Pipeline, WorldOwner {
    // 通过 DI 获取所有 Updatable 实现
    private val updatables: List<Updatable> by di.instance()
    
    override fun update(dt: Duration) {
        // 按阶段顺序执行
        UpdatePhase.entries.forEach { phase ->
            updatables.filter { it.phase == phase }
                .forEach { it.update(dt) }
        }
    }
}

// 使用
val pipeline = world.pipeline
pipeline.update(16.milliseconds)  // 自动按阶段顺序执行所有 Updatable
```

### 定义完成

- [ ] 阶段枚举 UPDATE_PHASE 包含 INPUT, UPDATE, RENDER 等阶段
- [ ] Pipeline.update(deltaTime: Duration) 方法可按顺序执行各阶段系统
- [ ] Updatable 接口/基类支持 update(dt: Duration) 方法
- [ ] TimerComponent 包含 remainingTime, isRunning, isLooping 属性
- [ ] TimerService 提供 startTimer, cancelTimer, getTimers 等方法
- [ ] 所有新代码测试覆盖率 95%+

### Must Have

- 阶段执行顺序可控（通过枚举定义顺序）
- 定时器可附加到任意实体
- 定时器支持一次性触发和循环触发
- 定时器到期自动触发回调/事件

### Must NOT Have

- ❌ 复杂优先级系统（用户选择基础版）
- ❌ 全局定时器管理器（用户选择组件式）
- ❌ 破坏现有 API 兼容性

---

## 验证策略

### 测试决策

- **测试框架**: 项目使用 Kotlin Multiplatform (kotlin-test)
- **TDD**: 是，严格遵循红-绿-重构
- **覆盖率要求**: 95%+

### TDD 流程

**Phase/执行顺序:**

1. **RED**: 编写失败测试
   - 测试 `Pipeline.update(dt)` 按阶段顺序执行
   - 测试 `Updatable` 接口的 `update(dt)` 方法被正确调用

2. **GREEN**: 最小实现
   - 添加 `UpdatePhase` 枚举
   - 增强 `Pipeline` 接口和实现
   - 创建 `Updatable` 接口

3. **REFACTOR**: 重构优化
   - 提取公共逻辑
   - 添加更多测试边界情况

**定时器系统:**

1. **RED**: 编写失败测试
   - 测试 TimerComponent 数据结构
   - 测试 TimerService 启动/取消定时器

2. **GREEN**: 最小实现
   - 实现 TimerComponent
   - 实现 TimerService

3. **REFACTOR**: 重构优化
   - 支持循环定时器
   - 支持回调机制

### Agent-Executed QA Scenarios

由于这是框架层代码，主要通过单元测试验证：

```
Scenario: Pipeline 按阶段顺序执行系统
  Tool: kotlin-test
  Steps:
    1. 创建 World 和 Pipeline
    2. 注册两个系统到 UPDATE 阶段
    3. 调用 pipeline.update(16. milliseconds) 或 pipeline.update(0.016.seconds)
    4. 验证系统按注册顺序执行
  Expected Result: 系统执行顺序与注册顺序一致
  
Scenario: 定时器组件正确存储时间数据
  Tool: kotlin-test  
  Steps:
    1. 创建 TimerComponent(10f, false)
    2. 验证 remainingTime = 10f
    3. 验证 isRunning = true
    4. 验证 isLooping = false
  Expected Result: 组件数据正确
  
Scenario: TimerService 管理定时器生命周期
  Tool: kotlin-test
  Steps:
    1. 创建实体和 TimerService
    2. 启动 5 秒定时器
    3. 验证实体有 TimerComponent
    4. 取消定时器
    5. 验证 TimerComponent 被移除
  Expected Result: 定时器生命周期管理正确
```

---

## Execution Strategy

### 任务分组

```
Wave 1 (Phase/执行顺序):
├── Task 1: 设计并实现 UpdatePhase 枚举
├── Task 2: 创建 Updatable 接口
├── Task 3: 增强 Pipeline 接口
├── Task 4: 实现 Pipeline.update() 方法
└── Task 5: 编写 Phase 相关测试

Wave 2 (定时器系统):
├── Task 6: 设计 TimerComponent 数据结构
├── Task 7: 实现 TimerService 服务
├── Task 8: 实现定时器更新逻辑（每帧减少时间）
├── Task 9: 实现定时器回调机制
└── Task 10: 编写定时器相关测试

Wave 3 (集成):
├── Task 11: 集成测试（定时器在游戏循环中工作）
└── Task 12: 文档更新（更新 ECS 文档）
```

### 依赖关系

| 任务 | 依赖 | 阻塞 |
|------|------|------|
| Task 1 (UpdatePhase) | None | 2, 3 |
| Task 2 (Updatable) | None | 3, 4 |
| Task 3 (Pipeline 接口) | 1 | 4 |
| Task 4 (Pipeline 实现) | 1, 3 | 5 |
| Task 5 (Phase 测试) | 4 | None |
| Task 6 (TimerComponent) | None | 7, 8 |
| Task 7 (TimerService) | 6 | 8 |
| Task 8 (定时器更新) | 7 | 9 |
| Task 9 (回调机制) | 8 | 10 |
| Task 10 (定时器测试) | 6, 7, 8, 9 | None |
| Task 11 (集成测试) | 5, 10 | None |
| Task 12 (文档更新) | 11 | None |

### Agent 推荐

| Wave | 推荐 Agent |
|------|-----------|
| 1 | ultrabrain (复杂框架设计) |
| 2 | ultrabrain (复杂框架设计) |
| 3 | quick (文档更新，任务简单) |

---

## TODOs

### Phase/执行顺序系统

- [ ] 1. 设计并实现 UpdatePhase 枚举

  **What to do**:
  - 定义运行时游戏循环阶段枚举（INPUT, UPDATE, Late_UPDATE, RENDER 等）
  - 确保顺序正确（INPUT → UPDATE → Late_UPDATE → RENDER）
  
  **Must NOT do**:
  - 不要破坏现有 Phase 枚举的兼容性
  
  **References**:
  - 现有 Phase: `libs/lko-ecs/src/commonMain/kotlin/cn/jzl/ecs/addon/Phase.kt`
  - 参考游戏引擎: Unity Update, Unity FixedUpdate, Godot _process
  
  **Acceptance Criteria**:
  - [ ] UpdatePhase 枚举包含至少 4 个阶段
  - [ ] 阶段顺序定义正确
  - [ ] 单元测试验证阶段顺序

- [ ] 2. 创建 Updatable 接口

  **What to do**:
  - 定义 `Updatable` 接口**，不继承任何接口**
  - 添加 `val phase: UpdatePhase` 属性
  - 添加 `fun update(dt: Duration)` 方法
  
  **实现类获取 World 的方式**:
  - 实现类同时实现 `Updatable` 和 `EntityRelationContext`
  - 通过**构造器注入**获取 world
  
  ```kotlin
  class MovementSystem(override val world: World) : Updatable, EntityRelationContext {
      override val phase: UpdatePhase = UpdatePhase.UPDATE
      override fun update(dt: Duration) { /* 使用 world */ }
  }
  ```
  
  **Must NOT do**:
  - Updatable 接口本身不要继承任何接口
  - 但实现类可以同时实现 Updatable + EntityRelationContext
  
  **References**:
  - ECS 最佳实践: docs/technology/ecs/02-patterns.md
  - EntityRelationContext: `libs/lko-ecs/src/commonMain/kotlin/cn/jzl/ecs/entity/EntityRelationContext.kt`
  
  **Acceptance Criteria**:
  - [ ] Updatable 接口定义完成（不继承任何接口）
  - [ ] phase 属性和 update 方法签名正确
  - [ ] 实现类可通过构造器注入 world 并实现 EntityRelationContext

- [ ] 3. 增强 Pipeline 接口

  **What to do**:
  - 在 Pipeline 接口添加 `update(deltaTime: Duration)` 方法
  - 移除 `registerSystem()` 方法（使用 DI 自动发现）
  
  **Must NOT do**:
  - 不要删除现有方法（保持兼容性）
  
  **References**:
  - 现有 Pipeline: `libs/lko-ecs/src/commonMain/kotlin/cn/jzl/ecs/Pipeline.kt`
  
  **Acceptance Criteria**:
  - [ ] Pipeline 接口新增 update 方法签名
  - [ ] 不需要 registerSystem 方法

- [ ] 4. 实现 Pipeline.update() 方法

  **What to do**:
  - 在 PipelineImpl 中实现 update 方法
  - **通过 DI 获取所有 Updatable 实现**（不使用手动注册）
  - 按 UpdatePhase 顺序执行各阶段的 Updatable
  - 处理 deltaTime 传递给系统
  
  **实现思路**:
  ```kotlin
  class PipelineImpl(override val world: World) : Pipeline, WorldOwner {
      // 通过 DI 自动获取所有 Updatable
      private val updatables: List<Updatable> by di.instance()
      
      override fun update(dt: Duration) {
          // 按阶段顺序执行
          UpdatePhase.entries.forEach { phase ->
              updatables.filter { it.phase == phase }
                  .forEach { it.update(dt) }
          }
      }
  }
  ```
  
  **Must NOT do**:
  - 不要跳过任何阶段
  - 不要改变系统执行顺序
  
  **References**:
  - PipelineImpl: `libs/lko-ecs/src/commonMain/kotlin/cn/jzl/ecs/PipelineImpl.kt`
  - DI 实例获取: libs/lko-di/
  
  **Acceptance Criteria**:
  - [ ] update(deltaTime) 按阶段顺序执行
  - [ ] deltaTime 正确传递给系统
  - [ ] 空阶段不执行（跳过）
  - [ ] 自动发现所有 Updatable 实现

- [ ] 5. 编写 Phase 相关测试

  **What to do**:
  - 创建 PipelineUpdateTest
  - 测试系统注册和执行顺序
  - 测试 deltaTime 传递
  - 测试多系统同阶段执行
  
  **Must NOT do**:
  - 不要只测试空实现
  
  **References**:
  - 现有测试: `libs/lko-ecs/src/commonTest/kotlin/cn/jzl/ecs/PipelineTest.kt`
  
  **Acceptance Criteria**:
  - [ ] 测试覆盖率 95%+
  - [ ] 所有关键路径覆盖

---

### 定时器系统

- [ ] 6. 设计 TimerComponent 数据结构

  **What to do**:
  - 设计定时器组件数据类
  - 包含：remainingTime（剩余时间）, duration（总时长）, isLooping（是否循环）, isRunning（是否运行）
  - 可选：callback（回调类型标识）
  
  **Must NOT do**:
  - 不要使用可变数据结构（组件不可变）
  
  **References**:
  - 组件设计原则: docs/technology/ecs/01-core-concepts.md
  - value class 使用场景: docs/technology/ecs/00-quick-start.md
  
  **Acceptance Criteria**:
  - [ ] TimerComponent 数据结构定义
  - [ ] 使用 data class 或 value class（根据属性数量）
  - [ ] 注册到 Addon

- [ ] 7. 实现 TimerService 服务

  **What to do**:
  - 创建 TimerService，继承 EntityRelationContext
  - 实现 startTimer(entity, duration, callback, isLooping)
  - 实现 cancelTimer(entity)
  - 实现 getTimers() 查询方法
  
  **Must NOT do**:
  - 不要在 Service 中保存状态（状态在组件中）
  
  **References**:
  - Service 模式: docs/technology/ecs/02-patterns.md
  - 现有 Service 示例: 02-patterns.md#3-service-模式
  
  **Acceptance Criteria**:
  - [ ] startTimer 正确添加 TimerComponent
  - [ ] cancelTimer 正确移除 TimerComponent
  - [ ] 查询方法返回正确结果

- [ ] 8. 实现定时器更新逻辑

  **What to do**:
  - 实现 updateTimers(deltaTime: Duration) 方法
  - 每帧减少 running 定时器的 remainingTime
  - 处理定时器到期逻辑
  
  **Must NOT do**:
  - 不要在迭代中修改实体（使用 toList 收集）
  
  **References**:
  - Batch 模式: docs/technology/ecs/02-patterns.md#5-batch-模式
  - Observer 模式: docs/technology/ecs/02-patterns.md#6-observer-模式
  
  **Acceptance Criteria**:
  - [ ] 定时器每帧正确递减
  - [ ] 到期定时器正确触发
  - [ ] 循环定时器正确重置

- [ ] 9. 实现定时器回调机制

  **What to do**:
  - 设计定时器到期时的回调机制
  - 可选方案：
    - 使用 Observer 观察 TimerComponent 变化
    - 在 TimerService 中存储回调函数
    - 产生事件（Event）
  
  **Must NOT do**:
  - 不要使用复杂的事件系统（基础版）
  
  **References**:
  - Observer 模式: docs/technology/ecs/02-patterns.md#6-observer-模式
  - 现有 Observer 实现: libs/lko-ecs/src/commonMain/kotlin/cn/jzl/ecs/observer/
  
  **Acceptance Criteria**:
  - [ ] 定时器到期可触发回调
  - [ ] 循环定时器每次循环都可触发

- [ ] 10. 编写定时器相关测试

  **What to do**:
  - 创建 TimerServiceTest
  - 测试启动/取消定时器
  - 测试时间递减
  - 测试到期逻辑
  - 测试循环逻辑
  
  **Must NOT do**:
  - 不要只测试简单场景
  
  **References**:
  - 现有测试结构: libs/lko-ecs/src/commonTest/kotlin/cn/jzl/ecs/
  
  **Acceptance Criteria**:
  - [ ] 测试覆盖率 95%+
  - [ ] 边界情况覆盖（0秒定时器、负数等）

---

### 集成与文档

- [ ] 11. 集成测试

  **What to do**:
  - 在游戏循环中测试定时器系统
  - 验证 Phase + Timer 协同工作
  
  **References**:
  - 集成测试示例: libs/lko-ecs-serialization/src/commonTest/kotlin/
  
  **Acceptance Criteria**:
  - [ ] 定时器在 pipeline.update() 中正确工作

- [ ] 12. 文档更新

  **What to do**:
  - 更新 ECS 文档：添加 Phase 使用说明
  - 添加定时器系统使用示例
  - 更新模板文档
  
  **References**:
  - 现有文档: docs/technology/ecs/
  
  **Acceptance Criteria**:
  - [ ] 文档包含新功能使用说明
  - [ ] 代码示例可运行

---

## 提交策略

| 任务 | 提交信息 | 包含文件 |
|------|----------|----------|
| Task 1-4 | feat(ecs): 添加运行时 Phase + Updatable 系统 | UpdatePhase.kt, Updatable.kt, Pipeline.kt, PipelineImpl.kt |
| Task 5 | test(ecs): Phase 系统测试 | PipelineUpdateTest.kt |
| Task 6-9 | feat(ecs): 添加定时器组件和服务 | TimerComponent.kt, TimerService.kt |
| Task 10 | test(ecs): 定时器系统测试 | TimerServiceTest.kt |
| Task 11-12 | refactor(ecs): 集成测试和文档更新 | 集成测试文件, 文档更新 |

---

## Success Criteria

### 验证命令

```bash
# 构建
./gradlew :libs:lko-ecs:compileKotlinJvm

# 测试
./gradlew :libs:lko-ecs:test

# 覆盖率
./gradlew :libs:lko-ecs:allCoverage
```

### 最终检查

- [ ] 所有 Must Have 特性已实现
- [ ] 所有 Must NOT Have 特性已避免
- [ ] 测试通过
- [ ] 覆盖率 95%+
- [ ] 文档已更新
- [ ] API 兼容（无破坏性变更）
