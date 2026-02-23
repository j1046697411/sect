# business-facility - 设施系统

## 模块定位
宗门设施管理和整体状态监控模块，负责设施信息、价值评估和使用管理。

## 核心职责
- 宗门状态检测（正常/警告/危急/解散）
- 设施价值评估（建设成本、维护成本、产出效率、战略价值）
- 设施使用管理（使用效果、使用成本、功能描述）
- 财务摘要统计

## 模块层级
```
┌─────────────────────────────────────────────────────────┐
│  垂直业务域层 (business-facility)                        │
│  - 依赖 business-core, business-resource                │
│  - 禁止依赖其他垂直业务域                                 │
└─────────────────────────────────────────────────────────┘
```

## 目录结构
```
business-facility/src/commonMain/kotlin/cn/jzl/sect/facility/
├── FacilityAddon.kt       # 模块入口
├── components/            # 组件定义
│   ├── FacilityComponents.kt   # 设施基础组件
│   └── SectStatusComponents.kt # 宗门状态组件
└── services/              # 服务实现
    ├── SectStatusService.kt    # 宗门状态服务
    ├── FacilityValueService.kt # 设施价值服务
    └── FacilityUsageService.kt # 设施使用服务
```

## 关键 API

### 组件
| 组件 | 用途 | 属性 |
|------|------|------|
| `Facility` | 设施基础信息 | 类型、等级、状态 |
| `FacilityStatus` | 设施状态 | 运行状态、效率 |

### 服务
| 服务 | 用途 | 核心方法 |
|------|------|----------|
| `SectStatusService` | 宗门状态 | `checkSectStatus()`, `getFinancialSummary()` |
| `FacilityValueService` | 设施价值 | `calculateFacilityValue()`, `calculateROI()` |
| `FacilityUsageService` | 设施使用 | `getFacilityEffect()`, `getUsageCost()` |

## 使用方式

```kotlin
// 1. 安装 Addon
world.install(facilityAddon)

// 2. 获取服务
val sectStatusService by world.di.instance<SectStatusService>()
val facilityValueService by world.di.instance<FacilityValueService>()
val facilityUsageService by world.di.instance<FacilityUsageService>()

// 3. 检查宗门状态
val status = sectStatusService.checkSectStatus()

// 4. 获取财务摘要
val summary = sectStatusService.getFinancialSummary()

// 5. 计算设施价值
val value = facilityValueService.calculateFacilityValue(
    constructionCost, maintenanceCost, efficiency, type
)

// 6. 获取设施效果
val effect = facilityUsageService.getFacilityEffect(facilityType)

// 7. 创建设施实体
world.entity {
    it.addComponent(Facility(
        type = FacilityType.SPIRIT_VEIN,
        level = 1,
        status = FacilityStatus.OPERATIONAL
    ))
}
```

## 宗门状态

| 状态 | 说明 | 触发条件 |
|------|------|----------|
| `NORMAL` | 正常 | 资源充足 |
| `WARNING` | 警告 | 资源紧张 |
| `CRITICAL` | 危急 | 资源严重不足 |
| `DISSOLVED` | 解散 | 无法维持 |

## 设施类型

| 类型 | 说明 | 效果 |
|------|------|------|
| 灵脉 | 灵石产出 | 增加灵石收入 |
| 药园 | 灵草产出 | 增加炼丹材料 |
| 矿脉 | 矿石产出 | 增加炼器材料 |
| 藏经阁 | 功法学习 | 加速功法学习 |
| 演武场 | 战斗训练 | 增加战斗经验 |

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
business-facility
    └── business-resource
            └── business-core
```

## AI 开发指引

### 开发原则
- **状态实时**: 宗门状态应实时反映当前状况
- **价值可评估**: 设施价值计算应有明确算法
- **效果可量化**: 设施效果应有数值化表示

### 设施价值计算
```kotlin
// 设施价值（示例）
val value = constructionCost + (outputValue - maintenanceCost) * expectedLifespan

// ROI 计算（示例）
val roi = (totalOutput - totalCost) / totalCost
```

### 添加新功能检查清单
- [ ] 是否需要新的设施类型？
- [ ] 是否影响宗门状态计算？
- [ ] 是否需要新的价值评估指标？
- [ ] 测试覆盖率是否达标？
- [ ] 是否遵循 TDD 开发模式？

## 禁止事项
- ❌ 禁止直接修改资源系统数据
- ❌ 禁止硬编码设施数值
- ❌ 禁止跳过状态检测

## 测试要求
- 宗门状态检测测试
- 设施价值计算测试
- 设施使用效果测试
- 边界条件测试（等级上限、效果边界等）
