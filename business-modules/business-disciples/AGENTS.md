# business-disciples - 弟子系统

## 模块定位
弟子管理、属性成长、角色关系管理模块，负责宗门弟子相关的所有功能。

## 核心职责
- 弟子信息查询和管理
- 角色关系管理（友好、敌对、竞争等）
- 师徒关系管理（拜师、收徒）
- 弟子属性查询

## 模块层级
```
┌─────────────────────────────────────────────────────────┐
│  垂直业务域层 (business-disciples)                       │
│  - 依赖 business-core, business-cultivation             │
│  - 禁止依赖其他垂直业务域                                 │
└─────────────────────────────────────────────────────────┘
```

## 目录结构
```
business-disciples/src/commonMain/kotlin/cn/jzl/sect/disciples/
├── DisciplesAddon.kt      # 模块入口
├── components/            # 组件定义
│   └── Relationship.kt    # 关系组件
└── services/              # 服务实现
    ├── DiscipleInfoService.kt      # 弟子信息服务
    ├── RelationshipService.kt      # 关系管理服务
    └── MasterApprenticeService.kt  # 师徒关系服务
```

## 关键 API

### 组件
| 组件 | 用途 | 属性 |
|------|------|------|
| `Relationship` | 关系数据 | sourceId, targetId, type, level |

### 关系类型 (RelationshipType)
| 类型 | 说明 | 效果 |
|------|------|------|
| `MASTER_APPRENTICE` | 师徒关系 | 正面，传承加成 |
| `PEER` | 同门关系 | 正面，协作加成 |
| `COMPETITOR` | 竞争关系 | 负面，竞争压力 |
| `COOPERATOR` | 合作关系 | 正面，效率加成 |
| `FRIENDLY` | 友好关系 | 正面，基础加成 |
| `HOSTILE` | 敌对关系 | 负面，冲突风险 |

### 服务
| 服务 | 用途 | 核心方法 |
|------|------|----------|
| `DiscipleInfoService` | 弟子信息查询 | `getAllDisciples()`, `getDiscipleById()` |
| `RelationshipService` | 关系管理 | `getRelationships()`, `addRelationship()` |
| `MasterApprenticeService` | 师徒管理 | `apprenticeToMaster()`, `getApprentices()` |

## 使用方式

```kotlin
// 1. 安装 Addon
world.install(disciplesAddon)

// 2. 获取服务
val discipleInfoService by world.di.instance<DiscipleInfoService>()
val relationshipService by world.di.instance<RelationshipService>()
val masterApprenticeService by world.di.instance<MasterApprenticeService>()

// 3. 获取所有弟子信息
val disciples = discipleInfoService.getAllDisciples()

// 4. 建立师徒关系
masterApprenticeService.apprenticeToMaster(apprenticeId, masterId)

// 5. 查询关系
val relationships = relationshipService.getRelationships(entityId)

// 6. 改善/恶化关系
val improved = relationship.improve(10)
val worsened = relationship.worsen(5)

// 7. 判断关系类型
if (relationship.isPositive()) { /* 正面关系 */ }
if (relationship.isNegative()) { /* 负面关系 */ }
```

## 依赖关系

```kotlin
// build.gradle.kts
dependencies {
    implementation(projects.businessModules.businessCore)
    implementation(projects.businessModules.businessCultivation)
    implementation(projects.libs.lkoEcs)
    implementation(projects.libs.lkoDi)
}
```

```
依赖图：
business-disciples
    └── business-cultivation
            └── business-resource
                    └── business-core
```

## AI 开发指引

### 开发原则
- **关系动态**: 关系等级应随时间和事件变化
- **双向对称**: A→B 和 B→A 的关系应保持一致
- **效果计算**: 关系效果应影响协作效率

### 关系效果计算
```kotlin
// 关系效果加成（示例）
val bonus = relationship.getEffectBonus() // level / 5

// 正面关系：效率 +bonus%
// 负面关系：冲突风险增加
```

### 添加新功能检查清单
- [ ] 是否影响关系平衡？
- [ ] 是否需要双向更新关系？
- [ ] 测试覆盖率是否达标？
- [ ] 是否遵循 TDD 开发模式？

## 禁止事项
- ❌ 禁止直接调用任务系统、战斗系统等
- ❌ 禁止创建循环依赖
- ❌ 禁止在组件中包含业务逻辑

## 测试要求
- 师徒关系测试
- 关系等级变化测试
- 关系效果计算测试
- 边界条件测试（最大等级、最小等级等）
