# business-combat - 战斗系统

## 模块定位
战斗管理模块，负责战斗核心逻辑、战斗结算和战斗实力评估。

## 核心职责
- 战斗核心逻辑（伤害计算、暴击、闪避）
- 战斗结算（奖励计算、评价）
- 战斗实力评估
- 战斗属性管理

## 模块层级
```
┌─────────────────────────────────────────────────────────┐
│  垂直业务域层 (business-combat)                          │
│  - 依赖 business-core, business-skill                   │
│  - 禁止依赖其他垂直业务域                                 │
└─────────────────────────────────────────────────────────┘
```

## 目录结构
```
business-combat/src/commonMain/kotlin/cn/jzl/sect/combat/
├── CombatAddon.kt         # 模块入口
├── components/            # 组件定义
│   ├── Combatant.kt       # 战斗参与者
│   ├── CombatStats.kt     # 战斗属性
│   └── CombatActionType.kt  # 战斗行动类型
└── services/              # 服务实现
    ├── CombatService.kt   # 战斗核心服务
    ├── CombatSettlementService.kt  # 战斗结算服务
    └── CombatPowerService.kt  # 战斗实力服务
```

## 关键 API

### 组件
| 组件 | 用途 | 属性 |
|------|------|------|
| `Combatant` | 战斗参与者 | 阵营、状态、目标 |
| `CombatStats` | 战斗属性 | 攻击、防御、暴击、闪避 |
| `CombatActionType` | 行动类型 | 攻击、技能、防御、逃跑 |

### 服务
| 服务 | 用途 | 核心方法 |
|------|------|----------|
| `CombatService` | 战斗逻辑 | `executeAttack()`, `executeSkill()` |
| `CombatSettlementService` | 战斗结算 | `generateSettlementReport()`, `calculateRewards()` |
| `CombatPowerService` | 实力评估 | `calculateCombatPower()`, `comparePower()` |

## 使用方式

```kotlin
// 1. 安装 Addon
world.install(combatAddon)

// 2. 获取服务
val combatService by world.di.instance<CombatService>()
val settlementService by world.di.instance<CombatSettlementService>()
val powerService by world.di.instance<CombatPowerService>()

// 3. 执行攻击
val result = combatService.executeAttack(attacker, attackerStats, defender, defenderStats)

// 4. 结算战斗
val report = settlementService.generateSettlementReport(combatResult)

// 5. 计算战斗实力
val power = powerService.calculateCombatPower(realm, stats)

// 6. 创建战斗实体
world.entity {
    it.addComponent(Combatant(faction = Faction.ALLY))
    it.addComponent(CombatStats(
        attack = 100,
        defense = 50,
        criticalRate = 0.2,
        dodgeRate = 0.1
    ))
}
```

## 战斗流程

```
战斗开始
    │
    ├── 1. 初始化战斗状态
    │       创建战斗实体、设置阵营
    │
    ├── 2. 战斗循环
    │       ├── 选择行动（攻击/技能/防御/逃跑）
    │       ├── 执行行动
    │       ├── 计算伤害/效果
    │       ├── 更新战斗状态
    │       └── 检查战斗结束条件
    │
    ├── 3. 战斗结束
    │       判定胜负
    │
    └── 4. 战斗结算
            计算奖励、生成报告
```

## 伤害计算

```kotlin
// 基础伤害（示例）
val baseDamage = attackerStats.attack - defenderStats.defense

// 暴击判定
val isCritical = random() < attackerStats.criticalRate

// 闪避判定
val isDodged = random() < defenderStats.dodgeRate

// 最终伤害
val finalDamage = when {
    isDodged -> 0
    isCritical -> baseDamage * 1.5
    else -> baseDamage
}
```

## 依赖关系

```kotlin
// build.gradle.kts
dependencies {
    implementation(projects.businessModules.businessCore)
    implementation(projects.businessModules.businessSkill)
    implementation(projects.libs.lkoEcs)
    implementation(projects.libs.lkoDi)
}
```

```
依赖图：
business-combat
    └── business-skill
            └── business-disciples
                    └── business-cultivation
                            └── business-resource
                                    └── business-core
```

## AI 开发指引

### 开发原则
- **数值平衡**: 伤害计算需保持平衡
- **策略深度**: 提供多种战斗策略选择
- **状态清晰**: 战斗状态应有清晰表示

### 战斗实力计算
```kotlin
// 战斗实力（示例）
val combatPower = basePower * realmMultiplier * skillBonus * equipmentBonus
```

### 添加新功能检查清单
- [ ] 是否影响战斗平衡？
- [ ] 是否需要新的战斗属性？
- [ ] 是否需要新的行动类型？
- [ ] 测试覆盖率是否达标？
- [ ] 是否遵循 TDD 开发模式？

## 禁止事项
- ❌ 禁止直接修改弟子系统数据
- ❌ 禁止硬编战斗数值
- ❌ 禁止跳过结算流程

## 测试要求
- 攻击计算测试
- 暴击/闪避测试
- 战斗结算测试
- 战斗实力测试
- 边界条件测试（伤害上限、下限等）
