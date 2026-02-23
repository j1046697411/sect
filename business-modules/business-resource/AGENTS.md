# business-resource - 资源系统

## 模块定位
宗门资源管理模块，负责资源的生产、消耗和统计功能。

## 核心职责
- 资源生产（灵脉、矿脉等产出）
- 资源消耗（俸禄发放、设施维护）
- 资源统计与查询
- 资源平衡计算

## 模块层级
```
┌─────────────────────────────────────────────────────────┐
│  垂直业务域层 (business-resource)                        │
│  - 依赖 business-core                                    │
│  - 禁止依赖其他垂直业务域                                 │
└─────────────────────────────────────────────────────────┘
```

## 目录结构
```
business-resource/src/commonMain/kotlin/cn/jzl/sect/resource/
├── ResourceAddon.kt       # 模块入口
├── components/            # 组件定义
│   ├── ResourceProduction.kt   # 资源生产组件
│   └── ResourceConsumption.kt  # 资源消耗组件
└── services/              # 服务实现
    ├── ResourceProductionService.kt   # 资源生产服务
    └── ResourceConsumptionService.kt  # 资源消耗服务
```

## 关键 API

### 组件
| 组件 | 用途 | 属性 |
|------|------|------|
| `ResourceProduction` | 资源生产 | 产出类型、产出速率、产出源 |
| `ResourceConsumption` | 资源消耗 | 消耗类型、消耗速率、消耗原因 |

### 服务
| 服务 | 用途 | 核心方法 |
|------|------|----------|
| `ResourceProductionService` | 资源生产 | `dailyProduction()`, `calculateTotal()` |
| `ResourceConsumptionService` | 资源消耗 | `monthlyConsumption()`, `calculateCost()` |

## 使用方式

```kotlin
// 1. 安装 Addon
world.install(resourceAddon)

// 2. 获取服务
val productionService by world.di.instance<ResourceProductionService>()
val consumptionService by world.di.instance<ResourceConsumptionService>()

// 3. 执行每日资源产出
val records = productionService.dailyProduction()

// 4. 执行月度资源消耗结算
val result = consumptionService.monthlyConsumption()

// 5. 创建资源产出实体
world.entity {
    it.addComponent(ResourceProduction(
        type = ResourceType.SPIRIT_STONE,
        rate = 100,
        source = "灵脉"
    ))
}

// 6. 创建资源消耗实体
world.entity {
    it.addComponent(ResourceConsumption(
        type = ResourceType.SPIRIT_STONE,
        rate = 10,
        reason = "月俸"
    ))
}
```

## 资源类型

| 类型 | 说明 | 产出源 |
|------|------|--------|
| 灵石 | 主要货币 | 灵脉、任务奖励 |
| 灵草 | 炼丹材料 | 药园、采集 |
| 矿石 | 炼器材料 | 矿脉、任务 |
| 功德点 | 特殊货币 | 任务、贡献 |

## 依赖关系

```kotlin
// build.gradle.kts
dependencies {
    implementation(projects.businessModules.businessCore)
    implementation(projects.libs.lkoEcs)
    implementation(projects.libs.lkoDi)
}
```

## AI 开发指引

### 开发原则
- **平衡性**: 生产与消耗应保持动态平衡
- **可追溯**: 每笔资源变动应有记录
- **可配置**: 产出和消耗速率应可配置

### 资源计算
```kotlin
// 每日产出（示例）
val dailyProduction = baseRate * facilityLevel * efficiency

// 月度消耗（示例）
val monthlyConsumption = discipleCount * salaryPerDisciple + maintenanceCost
```

### 添加新功能检查清单
- [ ] 是否影响资源平衡？
- [ ] 是否需要新的资源类型？
- [ ] 是否有对应的产出/消耗记录？
- [ ] 测试覆盖率是否达标？
- [ ] 是否遵循 TDD 开发模式？

## 禁止事项
- ❌ 禁止直接修改其他模块的实体
- ❌ 禁止硬编码资源数值
- ❌ 禁止跳过消耗结算

## 测试要求
- 资源生产测试
- 资源消耗测试
- 资源统计测试
- 边界条件测试（负数、溢出等）
