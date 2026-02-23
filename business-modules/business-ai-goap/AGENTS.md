# business-ai-goap - AI 决策系统

## 模块定位
基于 GOAP (Goal-Oriented Action Planning) 的自主决策 AI 系统，为游戏实体提供智能行为规划能力。

## 核心职责
- 提供 AI 决策规划核心算法（A* 搜索）
- 管理动作库和世界状态表示
- 支持动作链搜索与优化
- 执行规划好的动作序列

## 模块层级
```
┌─────────────────────────────────────────────────────────┐
│  垂直业务域层 (business-ai-goap)                         │
│  - 依赖 business-core                                    │
│  - 禁止依赖其他垂直业务域                                 │
└─────────────────────────────────────────────────────────┘
```

## 目录结构
```
business-ai-goap/src/commonMain/kotlin/cn/jzl/sect/ai/goap/
├── Action.kt              # GOAP 动作实现
├── ActionEffect.kt        # 动作效果
├── ActionProvider.kt      # 动作提供者接口
├── AgentState.kt          # 智能体状态
├── GOAPAction.kt          # GOAP 动作接口
├── GOAPBuilder.kt         # GOAP 构建器
├── GOAPGoal.kt            # GOAP 目标
├── Goal.kt                # 目标接口
├── GoalProvider.kt        # 目标提供者接口
├── Plan.kt                # 规划结果
├── Planner.kt             # A* 规划器核心
├── Planning.kt            # 规划配置
├── PlanningExecuteService.kt  # 规划执行服务
├── PlanningRegistry.kt    # 规划注册表
├── PlanningService.kt     # 规划服务
├── Precondition.kt        # 前置条件
├── StateKey.kt            # 状态键
├── StateResolver.kt       # 状态解析器接口
├── StateResolverRegistry.kt  # 状态解析器注册表
├── WorldState.kt          # 世界状态接口
├── WorldStateImpl.kt      # 世界状态实现
├── WorldStateReader.kt    # 世界状态读取器
└── WorldStateWriter.kt    # 世界状态写入器
```

## 关键 API

### 核心接口
| 接口/类 | 用途 | 说明 |
|---------|------|------|
| `Planner` | GOAP 规划器 | A* 搜索算法核心 |
| `GOAPAction` | 动作接口 | 定义可执行动作 |
| `Action` | 动作实现 | 具体动作类 |
| `WorldState` | 世界状态 | AI 感知的世界状态 |
| `Plan` | 规划结果 | 动作序列 |

### 创建动作
```kotlin
Action(
    name = "采集资源",
    cost = 1.0,
    preconditions = sequenceOf(
        Precondition { state, agent -> state.getValue(agent, HasTool) }
    ),
    effects = sequenceOf(
        ActionEffect { state, agent -> state.setValue(ResourceCount, 10) }
    ),
    task = ActionTask {
        delay(1.seconds)
        // 执行采集逻辑
    }
)
```

### 创建目标
```kotlin
GOAPGoal(
    name = "收集资源",
    priority = 1.0,
    conditions = sequenceOf(
        Condition { state, agent -> state.getValue(agent, ResourceCount) >= 10 }
    )
)
```

## 使用方式

```kotlin
// 1. 安装 Addon（通过其他模块依赖，通常不需要直接安装）

// 2. 创建规划器
val planner = Planner(actions, maxDepth = 10)

// 3. 执行规划
val plan = planner.plan(worldState, goal)

// 4. 执行动作序列
plan?.actions?.forEach { action ->
    action.task.execute()
}
```

## AI 开发指引

### 开发原则
- **性能优先**: GOAP 搜索需要限制深度和时间，避免性能瓶颈
- **状态简洁**: 世界状态表示应尽量紧凑高效
- **动作原子化**: 每个动作应只完成单一目标
- **可调试性**: 动作和目标应有清晰的名称

### 性能优化
```kotlin
// 限制搜索深度
Planner(actions, maxDepth = 10)

// 使用高效的状态表示
value class StateKey(val id: Int)
```

### 添加新动作检查清单
- [ ] 动作是否有清晰的名称？
- [ ] 动作成本是否合理？
- [ ] 前置条件是否完整？
- [ ] 效果是否准确？
- [ ] 任务是否可中断？

## 依赖关系

```kotlin
// build.gradle.kts
dependencies {
    implementation(projects.businessModules.businessCore)
    implementation(projects.libs.lkoEcs)
    implementation(projects.libs.lkoDi)
}
```

## 测试要求
- A* 搜索算法测试
- 动作执行测试
- 前置条件验证测试
- 世界状态读写测试
- 规划结果验证测试
