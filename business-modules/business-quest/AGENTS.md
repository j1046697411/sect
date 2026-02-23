# business-quest - 任务系统

## 模块定位
任务域核心概念与逻辑模块，负责宗门任务、团队组建、弟子晋升、长老评估等功能。

## 核心职责
- 任务执行管理（成功率计算、伤亡计算）
- 团队组建（1长老 + 3-5内门 + 10-20外门）
- 弟子晋升管理（外门晋升内门）
- 长老评估系统（根据性格评估弟子）
- 选拔任务管理（周期检测、名额计算）
- 政策配置管理（选拔周期、比例、资源分配）

## 模块层级
```
┌─────────────────────────────────────────────────────────┐
│  垂直业务域层 (business-quest)                           │
│  - 依赖 business-core, business-disciples, business-combat │
│  - 禁止依赖其他垂直业务域                                 │
└─────────────────────────────────────────────────────────┘
```

## 目录结构
```
business-quest/src/commonMain/kotlin/cn/jzl/sect/quest/
├── QuestAddon.kt          # 模块入口
├── components/            # 组件定义
│   ├── QuestComponent.kt  # 任务组件
│   ├── QuestExecutionComponent.kt  # 任务执行组件
│   ├── PolicyComponent.kt # 政策组件
│   ├── EvaluationComponent.kt  # 评估组件
│   └── ElderPersonality.kt  # 长老性格
└── services/              # 服务实现
    ├── QuestExecutionService.kt  # 任务执行服务
    ├── TeamFormationService.kt   # 团队组建服务
    ├── PromotionService.kt       # 晋升服务
    ├── ElderEvaluationService.kt # 长老评估服务
    ├── SelectionTaskService.kt   # 选拔任务服务
    └── PolicyService.kt          # 政策服务
```

## 关键 API

### 组件
| 组件 | 用途 | 说明 |
|------|------|------|
| `QuestComponent` | 任务数据 | 任务基础信息 |
| `QuestExecutionComponent` | 任务执行 | 执行状态和结果 |
| `PolicyComponent` | 政策配置 | 选拔周期、比例等 |
| `EvaluationComponent` | 评估数据 | 长老评估结果 |
| `ElderPersonality` | 长老性格 | 性格偏好配置 |

### 服务
| 服务 | 用途 | 核心方法 |
|------|------|----------|
| `QuestExecutionService` | 任务执行 | `executeQuest()`, `calculateSuccess()` |
| `TeamFormationService` | 团队组建 | `formTeam()`, `validateTeam()` |
| `PromotionService` | 弟子晋升 | `promoteDisciple()`, `checkPromotion()` |
| `ElderEvaluationService` | 长老评估 | `evaluate()`, `getEvaluationResult()` |
| `SelectionTaskService` | 选拔管理 | `checkSelection()`, `calculateSlots()` |
| `PolicyService` | 政策管理 | `getPolicy()`, `updatePolicy()` |

## 使用方式

```kotlin
// 1. 安装 Addon
world.install(questAddon)

// 2. 获取服务
val questExecutionService by world.di.instance<QuestExecutionService>()
val teamFormationService by world.di.instance<TeamFormationService>()
val promotionService by world.di.instance<PromotionService>()
val elderEvaluationService by world.di.instance<ElderEvaluationService>()
val selectionTaskService by world.di.instance<SelectionTaskService>()
val policyService by world.di.instance<PolicyService>()

// 3. 组建团队
val team = teamFormationService.formTeam(questId)

// 4. 执行任务
val result = questExecutionService.executeQuest(questId)

// 5. 晋升弟子
val promotionResult = promotionService.promoteDisciple(discipleId)

// 6. 长老评估弟子
val evaluation = elderEvaluationService.evaluate(elderId, discipleId)

// 7. 检查选拔任务
val selectionResult = selectionTaskService.checkSelection()

// 8. 获取/更新政策
val policy = policyService.getPolicy()
policyService.updatePolicy(newPolicy)
```

## 团队组建规则

```
标准任务团队结构：
┌─────────────────────────────────────────┐
│  长老 × 1                                │  负责指挥和决策
├─────────────────────────────────────────┤
│  内门弟子 × 3-5                          │  核心战力
├─────────────────────────────────────────┤
│  外门弟子 × 10-20                        │  辅助和支援
└─────────────────────────────────────────┘
```

## 依赖关系

```kotlin
// build.gradle.kts
dependencies {
    implementation(projects.businessModules.businessCore)
    implementation(projects.businessModules.businessDisciples)
    implementation(projects.businessModules.businessCombat)
    implementation(projects.libs.lkoEcs)
    implementation(projects.libs.lkoDi)
}
```

```
依赖图：
business-quest
    ├── business-disciples
    │       └── business-cultivation
    │               └── business-resource
    │                       └── business-core
    └── business-combat
            └── business-skill
                    └── business-disciples
```

## AI 开发指引

### 开发原则
- **数据驱动**: 任务逻辑应尽量通过组件配置而非硬编码
- **模块解耦**: 通过 `World` 的组件状态与其他业务模块通信
- **规则可配置**: 选拔比例、周期等应通过政策组件配置

### 任务成功计算
```kotlin
// 成功率计算（示例）
val successRate = teamPower / taskDifficulty * teamSynergy

// 伤亡计算（示例）
val casualties = (1 - successRate) * teamSize * difficultyFactor
```

### 添加新功能检查清单
- [ ] 是否需要新的组件？
- [ ] 是否需要新的服务？
- [ ] 是否影响团队组建规则？
- [ ] 测试覆盖率是否达标？
- [ ] 是否遵循 TDD 开发模式？

## 禁止事项
- ❌ 禁止硬编码任务参数
- ❌ 禁止直接修改其他模块的实体
- ❌ 禁止跳过团队验证

## 测试要求
- 任务执行测试
- 团队组建测试
- 晋升逻辑测试
- 长老评估测试
- 政策配置测试
- 选拔周期测试
