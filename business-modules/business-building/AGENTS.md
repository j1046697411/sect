# business-building - 建筑系统

## 模块定位
宗门建筑管理模块，负责设施建造、产出和升级功能。

## 核心职责
- 设施建造管理（建造队列、资源消耗）
- 设施产出计算（产出效率、产出类型）
- 设施升级管理（升级条件、升级效果）

## 模块层级
```
┌─────────────────────────────────────────────────────────┐
│  垂直业务域层 (business-building)                        │
│  - 依赖 business-core, business-facility                │
│  - 禁止依赖其他垂直业务域                                 │
└─────────────────────────────────────────────────────────┘
```

## 目录结构
```
business-building/src/commonMain/kotlin/cn/jzl/sect/building/
├── BuildingAddon.kt       # 模块入口
├── components/            # 组件定义
│   └── FacilityBuildProgress.kt  # 建造进度
└── services/              # 服务实现
    ├── FacilityConstructionService.kt  # 设施建造服务
    ├── FacilityProductionService.kt    # 设施产出服务
    └── FacilityUpgradeService.kt       # 设施升级服务
```

## 关键 API

### 组件
| 组件 | 用途 | 属性 |
|------|------|------|
| `FacilityBuildProgress` | 建造进度 | 开始时间、预计完成、所需资源 |

### 服务
| 服务 | 用途 | 核心方法 |
|------|------|----------|
| `FacilityConstructionService` | 设施建造 | `build()`, `cancelBuild()`, `getBuildQueue()` |
| `FacilityProductionService` | 设施产出 | `calculateTotalProduction()`, `getFacilityOutput()` |
| `FacilityUpgradeService` | 设施升级 | `upgrade()`, `canUpgrade()`, `getUpgradeCost()` |

## 使用方式

```kotlin
// 1. 安装 Addon
world.install(buildingAddon)

// 2. 获取服务
val constructionService by world.di.instance<FacilityConstructionService>()
val productionService by world.di.instance<FacilityProductionService>()
val upgradeService by world.di.instance<FacilityUpgradeService>()

// 3. 建造设施
val result = constructionService.build("灵脉", FacilityType.SPIRIT_VEIN)

// 4. 取消建造
constructionService.cancelBuild(facilityId)

// 5. 计算总产出
val productions = productionService.calculateTotalProduction()

// 6. 获取设施产出
val output = productionService.getFacilityOutput(facilityId)

// 7. 检查是否可升级
val canUpgrade = upgradeService.canUpgrade(facilityEntity)

// 8. 升级设施
val upgradeResult = upgradeService.upgrade(facilityEntity)

// 9. 获取升级成本
val cost = upgradeService.getUpgradeCost(facilityEntity)
```

## 建造流程

```
开始建造
    │
    ├── 1. 检查条件
    │       ├── 资源是否足够
    │       ├── 建造队列是否已满
    │       └── 是否满足前置条件
    │
    ├── 2. 扣除资源
    │       消耗建造所需资源
    │
    ├── 3. 创建建造进度
    │       开始计时
    │
    ├── 4. 等待完成
    │       按时间推进
    │
    └── 5. 完成建造
            创建设施实体
```

## 升级规则

| 等级 | 建造时间 | 资源消耗 | 产出加成 |
|------|----------|----------|----------|
| 1 → 2 | 1天 | 100灵石 | +20% |
| 2 → 3 | 2天 | 200灵石 | +20% |
| 3 → 4 | 3天 | 400灵石 | +20% |
| ... | ... | ... | ... |

## 依赖关系

```kotlin
// build.gradle.kts
dependencies {
    implementation(projects.businessModules.businessCore)
    implementation(projects.businessModules.businessFacility)
    implementation(projects.libs.lkoEcs)
    implementation(projects.libs.lkoDi)
}
```

```
依赖图：
business-building
    └── business-facility
            └── business-resource
                    └── business-core
```

## AI 开发指引

### 开发原则
- **建造队列**: 应有合理的建造队列限制
- **资源平衡**: 建造消耗应与产出平衡
- **升级收益**: 升级应有明显的收益提升

### 产出计算
```kotlin
// 设施产出（示例）
val output = baseOutput * (1 + levelBonus) * efficiencyMultiplier

// 升级成本（示例）
val cost = baseCost * (1.5 ^ (level - 1))
```

### 添加新功能检查清单
- [ ] 是否需要新的设施类型？
- [ ] 是否需要新的建造规则？
- [ ] 是否影响产出平衡？
- [ ] 测试覆盖率是否达标？
- [ ] 是否遵循 TDD 开发模式？

## 禁止事项
- ❌ 禁止直接修改资源系统数据
- ❌ 禁止硬编建造数值
- ❌ 禁止跳过资源检查

## 测试要求
- 建造流程测试
- 产出计算测试
- 升级逻辑测试
- 边界条件测试（队列上限、等级上限等）
