# business-cultivation - 修炼系统

## 模块定位
修炼域核心概念、进度与境界管理模块，负责弟子修炼相关的所有逻辑。

## 核心职责
- 提供修炼进度、境界等领域模型
- 管理突破、感悟等修仙核心逻辑
- 处理弟子行为决策（修炼/休息/工作）
- 计算修为增长和突破成功率

## 模块层级
```
┌─────────────────────────────────────────────────────────┐
│  垂直业务域层 (business-cultivation)                     │
│  - 依赖 business-core, business-resource                │
│  - 禁止依赖其他垂直业务域                                 │
└─────────────────────────────────────────────────────────┘
```

## 目录结构
```
business-cultivation/src/commonMain/kotlin/cn/jzl/sect/cultivation/
├── CultivationAddon.kt    # 模块入口
├── components/            # 组件定义
│   ├── CultivationProgress.kt  # 修炼进度
│   └── Talent.kt          # 弟子天赋
└── services/              # 服务实现
    ├── CultivationService.kt    # 修炼逻辑服务
    └── SimpleBehaviorService.kt # 简单行为决策
```

## 关键 API

### 组件
| 组件 | 用途 | 属性 |
|------|------|------|
| `CultivationProgress` | 修炼进度 | 当前修为、突破次数等 |
| `Talent` | 弟子天赋 | 天赋值、修炼加成 |

### 服务
| 服务 | 用途 | 核心方法 |
|------|------|----------|
| `CultivationService` | 修炼逻辑 | `update(hours)`, `tryBreakthrough()` |
| `SimpleBehaviorService` | 行为决策 | `update(dt)` |

## 使用方式

```kotlin
// 1. 安装 Addon
world.install(cultivationAddon)

// 2. 获取服务
val cultivationService by world.di.instance<CultivationService>()
val behaviorService by world.di.instance<SimpleBehaviorService>()

// 3. 更新修炼状态（每帧调用）
val breakthroughs = cultivationService.update(hours)

// 4. 更新行为状态（每帧调用）
behaviorService.update(dt)

// 5. 创建修炼实体
world.entity {
    it.addComponent(CultivationProgress(...))
    it.addComponent(Talent(85))
}
```

## 依赖关系

```kotlin
// build.gradle.kts
dependencies {
    implementation(projects.businessModules.businessCore)
    implementation(projects.businessModules.businessResource)
    implementation(projects.libs.lkoEcs)
    implementation(projects.libs.lkoDi)
}
```

```
依赖图：
business-cultivation
    └── business-resource
            └── business-core
                    └── libs/lko-ecs
```

## AI 开发指引

### 开发原则
- **数值平衡**: 修炼进度的计算需严格遵循设计文档
- **状态同步**: 境界提升需触发相关的属性变更事件
- **行为多样化**: 行为决策应考虑多种因素（天赋、性格等）

### 修炼计算要点
```kotlin
// 修为增长公式（示例）
val cultivationGain = baseRate * (1 + talentBonus) * environmentMultiplier

// 突破成功率（示例）
val successRate = baseRate + talentBonus - realmPenalty
```

### 添加新功能检查清单
- [ ] 是否影响数值平衡？
- [ ] 是否需要同步更新其他模块？
- [ ] 测试覆盖率是否达标？
- [ ] 是否遵循 TDD 开发模式？

## 禁止事项
- ❌ 禁止直接调用弟子系统、任务系统等
- ❌ 禁止在组件中包含业务逻辑
- ❌ 禁止硬编码数值配置

## 测试要求
- 修为增长计算测试
- 境界突破逻辑测试
- 行为决策测试
- 边界条件测试（满修为、失败处理等）
