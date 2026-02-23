# business-skill - 功法系统

## 模块定位
功法管理模块，负责功法学习、效果计算和传承机制。

## 核心职责
- 功法学习管理（学习条件、成功率）
- 功法效果计算（效果值、加成）
- 功法传承机制（师徒传承）
- 功法熟练度管理

## 模块层级
```
┌─────────────────────────────────────────────────────────┐
│  垂直业务域层 (business-skill)                           │
│  - 依赖 business-core, business-disciples               │
│  - 禁止依赖其他垂直业务域                                 │
└─────────────────────────────────────────────────────────┘
```

## 目录结构
```
business-skill/src/commonMain/kotlin/cn/jzl/sect/skill/
├── SkillAddon.kt          # 模块入口
├── components/            # 组件定义
│   ├── Skill.kt           # 功法基础信息
│   ├── SkillLearned.kt    # 已学习功法
│   ├── SkillEffect.kt     # 功法效果
│   ├── SkillEffectType.kt # 效果类型
│   ├── SkillType.kt       # 功法类型
│   └── SkillRarity.kt     # 功法稀有度
└── services/              # 服务实现
    ├── SkillLearningService.kt    # 功法学习服务
    ├── SkillEffectService.kt      # 功法效果服务
    └── SkillInheritanceService.kt # 功法传承服务
```

## 关键 API

### 组件
| 组件 | 用途 | 属性 |
|------|------|------|
| `Skill` | 功法基础 | 名称、类型、稀有度、要求 |
| `SkillLearned` | 已学功法 | 功法ID、熟练度、等级 |
| `SkillEffect` | 功法效果 | 类型、数值、条件 |
| `SkillType` | 功法类型 | 攻击、防御、辅助、被动 |
| `SkillRarity` | 稀有度 | 普通、稀有、史诗、传说 |

### 服务
| 服务 | 用途 | 核心方法 |
|------|------|----------|
| `SkillLearningService` | 功法学习 | `canLearnSkill()`, `learnSkill()` |
| `SkillEffectService` | 效果计算 | `applyEffect()`, `calculateBonus()` |
| `SkillInheritanceService` | 功法传承 | `canInherit()`, `inheritSkill()` |

## 使用方式

```kotlin
// 1. 安装 Addon
world.install(skillAddon)

// 2. 获取服务
val skillLearningService by world.di.instance<SkillLearningService>()
val skillEffectService by world.di.instance<SkillEffectService>()
val skillInheritanceService by world.di.instance<SkillInheritanceService>()

// 3. 检查是否可以学习功法
val canLearn = skillLearningService.canLearnSkill(skill, realm, talent, learnedIds)

// 4. 学习功法
val learnResult = skillLearningService.learnSkill(discipleId, skillId)

// 5. 计算功法效果
val effectValue = skillEffectService.applyEffect(effect, proficiency)

// 6. 检查是否可以传承功法
val canInherit = skillInheritanceService.canInherit(skill, learned, masterRealm, apprenticeRealm)

// 7. 传承功法
val inheritResult = skillInheritanceService.inheritSkill(masterId, apprenticeId, skillId)

// 8. 创建功法实体
world.entity {
    it.addComponent(Skill(
        name = "太极拳",
        type = SkillType.COMBAT,
        rarity = SkillRarity.RARE,
        requiredRealm = Realm.QI_REFINING
    ))
}
```

## 功法类型

| 类型 | 说明 | 效果 |
|------|------|------|
| `COMBAT` | 战斗功法 | 增加攻击力 |
| `DEFENSE` | 防御功法 | 增加防御力 |
| `SUPPORT` | 辅助功法 | 治疗、增益 |
| `PASSIVE` | 被动功法 | 永久属性加成 |

## 稀有度

| 稀有度 | 说明 | 学习难度 |
|--------|------|----------|
| `COMMON` | 普通 | 容易 |
| `RARE` | 稀有 | 中等 |
| `EPIC` | 史诗 | 困难 |
| `LEGENDARY` | 传说 | 极难 |

## 依赖关系

```kotlin
// build.gradle.kts
dependencies {
    implementation(projects.businessModules.businessCore)
    implementation(projects.businessModules.businessDisciples)
    implementation(projects.libs.lkoEcs)
    implementation(projects.libs.lkoDi)
}
```

```
依赖图：
business-skill
    └── business-disciples
            └── business-cultivation
                    └── business-resource
                            └── business-core
```

## AI 开发指引

### 开发原则
- **学习门槛**: 功法学习应有明确的条件和限制
- **效果平衡**: 功法效果应与稀有度匹配
- **传承机制**: 师徒传承应有合理的规则

### 功法效果计算
```kotlin
// 效果值（示例）
val effectValue = baseEffect * (1 + proficiencyBonus) * realmMultiplier

// 学习成功率（示例）
val successRate = baseRate + talentBonus - difficultyPenalty
```

### 添加新功能检查清单
- [ ] 是否需要新的功法类型？
- [ ] 是否需要新的效果类型？
- [ ] 是否影响学习平衡？
- [ ] 测试覆盖率是否达标？
- [ ] 是否遵循 TDD 开发模式？

## 禁止事项
- ❌ 禁止直接修改弟子系统数据
- ❌ 禁止硬编码功法数值
- ❌ 禁止跳过学习验证

## 测试要求
- 功法学习测试
- 效果计算测试
- 传承机制测试
- 边界条件测试（熟练度上限、效果边界等）
