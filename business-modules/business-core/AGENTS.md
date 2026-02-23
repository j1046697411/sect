# business-core - 领域核心抽象

## 模块定位
领域核心抽象与基础服务模块，供其他业务域复用。是所有业务模块的**共享内核**。

## 核心职责
- 提供基础领域模型（通用组件、标签）
- 提供跨模块的公共服务
- 定义游戏核心配置

## 模块层级
```
┌─────────────────────────────────────────────────────────┐
│  共享内核层 (business-core)                              │
│  - 被所有业务模块依赖                                     │
│  - 禁止依赖任何其他业务模块                               │
└─────────────────────────────────────────────────────────┘
```

## 目录结构
```
business-core/src/commonMain/kotlin/cn/jzl/sect/core/
├── ai/                    # AI 相关组件
│   ├── Personality6.kt    # 六维性格
│   ├── Personality8.kt    # 八维性格
│   ├── BehaviorStateComponent.kt  # 行为状态
│   └── BehaviorType.kt    # 行为类型
├── attribute/             # 属性系统
│   └── AttributeBundle.kt # 属性组合
├── combat/                # 战斗属性
│   └── CombatAttribute.kt # 战斗属性定义
├── common/                # 通用组件
│   ├── Name.kt            # 名称组件
│   ├── Description.kt     # 描述组件
│   └── Level.kt           # 等级组件
├── config/                # 游戏配置
│   └── GameConfig.kt      # 全局游戏配置
├── cultivation/           # 修炼核心
│   ├── Realm.kt           # 境界定义
│   └── Talent.kt          # 天赋定义
├── demo/                  # 演示组件
│   └── DemoComponents.kt  # Demo 专用组件
├── disciple/              # 弟子相关
│   └── SectLoyaltyComponent.kt  # 宗门忠诚度
├── facility/              # 设施核心
│   └── FacilityType.kt    # 设施类型定义
├── sect/                  # 宗门相关
│   ├── SectComponent.kt   # 宗门组件
│   ├── SectPositionComponent.kt  # 宗门职位
│   └── SectResourceComponent.kt  # 宗门资源
├── time/                  # 时间组件
│   └── TimeComponent.kt   # 游戏时间
└── vitality/              # 生命相关
    ├── Vitality.kt        # 活力
    └── Spirit.kt          # 精神
```

## 关键 API

### 通用组件
| 组件 | 用途 | 示例 |
|------|------|------|
| `Name` | 实体名称 | `Name("张三")` |
| `Description` | 实体描述 | `Description("天才弟子")` |
| `Level` | 等级 | `Level(5)` |

### 修炼核心
| 组件 | 用途 | 说明 |
|------|------|------|
| `Realm` | 境界 | 练气、筑基、金丹等 |
| `Talent` | 天赋 | 修炼资质 |

### 宗门相关
| 组件 | 用途 | 说明 |
|------|------|------|
| `Sect` | 宗门标识 | 标记实体属于某宗门 |
| `SectPositionInfo` | 宗门职位 | 长老、内门弟子等 |
| `SectLoyalty` | 宗门忠诚度 | 忠诚度数值 |

### AI 相关
| 组件 | 用途 | 说明 |
|------|------|------|
| `Personality6` | 六维性格 | 勇敢、谨慎等 |
| `CurrentBehavior` | 当前行为 | 修炼、休息等 |
| `BehaviorType` | 行为类型枚举 | 行为类型定义 |

## 使用方式

```kotlin
// 依赖引入
implementation(projects.businessModules.businessCore)

// 组件使用
import cn.jzl.sect.core.common.Name
import cn.jzl.sect.core.cultivation.Realm

// 创建实体时使用
world.entity {
    it.addComponent(Name("李四"))
    it.addComponent(Realm.QI_REFINING)
}
```

## AI 开发指引

### 开发原则
- **高复用性**: 定义组件时需考虑通用性，避免为特定业务定制
- **纯数据**: 保持组件为纯 `data class` 或 `value class`，不包含业务逻辑
- **单一职责**: 每个组件只负责一个明确的数据领域

### 添加新组件检查清单
- [ ] 该组件是否会被多个模块使用？
- [ ] 该组件是否为纯数据定义？
- [ ] 该组件是否不依赖其他业务模块？
- [ ] 命名是否清晰表达数据含义？

### 禁止事项
- ❌ 禁止在 business-core 中添加业务逻辑
- ❌ 禁止依赖任何其他 business-* 模块
- ❌ 禁止定义特定业务域的专属组件（应放入对应模块）

## 依赖关系

```kotlin
// build.gradle.kts
dependencies {
    // 只依赖基础库
    implementation(projects.libs.lkoCore)
    implementation(projects.libs.lkoEcs)
}
```

## 测试要求
- 组件的纯数据测试
- 枚举值覆盖测试
- 工具函数测试（如有）
