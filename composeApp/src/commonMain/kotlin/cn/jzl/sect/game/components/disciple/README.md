# 弟子组件 (Disciple Components)

本模块定义弟子（角色）相关的所有组件，包括资质、性格、能力等属性。

## 设计原则

1. **属性分类**：按功能维度拆分，而非按实体类型
2. **原子化**：每个组件代表一个独立的属性维度
3. **可成长性**：属性设计支持随时间变化

## 组件分类

### 1. 先天资质 (Innate Talent)

出生即确定，基本不变（除非特殊奇遇）

```kotlin
data class InnateTalent(
    val rootBone: Int,        // 根骨 1-100，影响修炼速度
    val understanding: Int,   // 悟性 1-100，影响功法领悟
    val luck: Int,            // 福缘 1-100，影响奇遇概率
    val constitution: Int,    // 体质 1-100，影响气血上限
    val charm: Int            // 魅力 1-100，影响社交效果
)
```

### 2. 后天属性 (Acquired Attributes)

可通过修炼、丹药、事件等提升

```kotlin
// 战斗属性
data class CombatStats(
    val strength: Int,        // 力量 - 物理攻击、采集效率
    val agility: Int,         // 敏捷 - 速度、闪避
    val intelligence: Int,    // 智力 - 法术伤害、炼丹/炼器成功率
    val endurance: Int        // 耐力 - 防御、战斗持久
)

// 技能熟练度（独立组件，可按需添加）
@JvmInline value class AlchemySkill(val level: Int)      // 炼丹
@JvmInline value class ForgeSkill(val level: Int)        // 炼器
@JvmInline value class TalismanSkill(val level: Int)     // 符箓
@JvmInline value class FormationSkill(val level: Int)    // 阵法
@JvmInline value class HerbSkill(val level: Int)         // 采药
```

### 3. 性格属性 (Personality)

影响 AI 决策倾向

```kotlin
data class Personality(
    val ambition: Int,        // 野心：争取晋升、承担风险
    val diligence: Int,       // 勤勉：增加修炼时间
    val loyalty: Int,         // 忠诚：服从管理、降低叛离概率
    val greed: Int,           // 贪婪：注重收益、可能囤货
    val kindness: Int,        // 善良：帮助他人、社交频率
    val caution: Int          // 谨慎：避免危险、预留资源
) {
    // 计算行为倾向分数（供 AI 使用）
    fun cultivationPriority(): Float = 
        diligence * 0.6f + ambition * 0.3f + caution * 0.1f
    
    fun riskTolerance(): Float =
        ambition * 0.5f + greed * 0.3f - caution * 0.4f
    
    fun socialFrequency(): Float =
        kindness * 0.5f + charm * 0.3f
}
```

### 4. 生命状态

```kotlin
// 当前气血
data class Health(
    val current: Float,
    val max: Float
)

// 当前灵力
data class SpiritPower(
    val current: Float,
    val max: Float
)

// 受伤详情（带数据组件）
data class Injury(
    val severity: Int,        // 1-10，影响恢复速度
    val recoveryRate: Float,  // 每小时恢复量
    val cause: String         // 受伤原因（突破失败/战斗/走火入魔）
)
```

### 5. 门派地位

```kotlin
// 职位（使用枚举）
enum class Position(val rank: Int, val monthlySalary: Int) {
    OUTER_DISCIPLE(1, 10),    // 外门弟子
    INNER_DISCIPLE(2, 30),    // 内门弟子
    CORE_DISCIPLE(3, 60),     // 亲传弟子
    ELDER(4, 150),            // 长老
    GRAND_ELDER(5, 300),      // 大长老
    SECT_LEADER(6, 500)       // 掌门
}

// 门派归属（使用关系或组件）
@JvmInline value class SectId(val value: Long)

// 个人贡献与俸禄
@JvmInline value class ContributionPoints(val amount: Int)
@JvmInline value class TotalContribution(val amount: Int)  // 累计贡献（不会减少）
```

## 组件使用场景

### 创建弟子

```kotlin
world.entity {
    // 基础信息
    it.addComponent(EntityName("李四"))
    it.addComponent(Age(20))
    
    // 资质（随机生成）
    it.addComponent(InnateTalent(
        rootBone = Random.nextInt(30, 80),
        understanding = Random.nextInt(30, 80),
        luck = Random.nextInt(30, 80),
        constitution = Random.nextInt(30, 80),
        charm = Random.nextInt(30, 80)
    ))
    
    // 性格（随机生成）
    it.addComponent(Personality(
        ambition = Random.nextInt(30, 80),
        diligence = Random.nextInt(30, 80),
        loyalty = Random.nextInt(30, 80),
        greed = Random.nextInt(30, 80),
        kindness = Random.nextInt(30, 80),
        caution = Random.nextInt(30, 80)
    ))
    
    // 战斗属性（基于根骨/体质）
    val talent = it.getComponent<InnateTalent>()!!
    it.addComponent(CombatStats(
        strength = talent.rootBone / 2,
        agility = talent.rootBone / 3,
        intelligence = talent.understanding / 2,
        endurance = talent.constitution / 2
    ))
    
    // 生命状态
    val maxHealth = talent.constitution * 10f
    it.addComponent(Health(current = maxHealth, max = maxHealth))
    it.addComponent(SpiritPower(current = 100f, max = 100f))
    
    // 门派地位
    it.addComponent(Position.OUTER_DISCIPLE)
    it.addComponent(ContributionPoints(0))
    
    // 初始标签
    it.addTag<AliveTag>()
    it.addTag<IdleTag>()
}
```

### 属性增长

```kotlin
// 突破成功后属性增长
fun onBreakthroughSuccess(entity: Entity, newRealm: CultivationRealm) {
    val talent = entity.getComponent<InnateTalent>()!!
    val stats = entity.getComponent<CombatStats>()!!
    
    // 增长战斗属性
    entity.editor {
        it.addComponent(stats.copy(
            strength = stats.strength + Random.nextInt(1, 3),
            intelligence = stats.intelligence + Random.nextInt(1, 3),
            endurance = stats.endurance + Random.nextInt(1, 3)
        ))
    }
    
    // 偶尔增长先天资质（突破大境界时）
    if (newRealm.isMajorRealm()) {
        entity.editor {
            it.addComponent(talent.copy(
                rootBone = talent.rootBone + if (Random.nextFloat() < 0.3f) 1 else 0,
                understanding = talent.understanding + if (Random.nextFloat() < 0.3f) 1 else 0
            ))
        }
    }
}
```

## 依赖关系

- **依赖**：`core` 模块（使用 `EntityName`, `Age` 等基础组件）
- **被依赖**：`services/disciple`, `services/cultivation` 等

## 扩展建议

如需添加新属性：
1. 判断是单属性还是多属性
2. 单属性 → `@JvmInline value class`
3. 多属性 → `data class`
4. 添加到本模块相应分类中
