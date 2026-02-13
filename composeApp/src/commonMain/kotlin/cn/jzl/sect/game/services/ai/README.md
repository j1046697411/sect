# AI 决策服务 (AI Service)

本模块实现弟子的自主决策系统，采用分批处理策略。

## 设计原则

1. **分批处理**：每帧只处理 20 个实体，避免卡顿
2. **轮转队列**：所有实体轮流处理，保证公平性
3. **性格驱动**：决策基于性格属性
4. **状态分层**：行为 → 任务 → 具体动作

## 决策架构

```
AI 决策系统
    ├── 目标选择（Goal Selection）
    │       └── 根据时辰、性格、需求选择目标
    │
    ├── 行为选择（Behavior Selection）
    │       └── 根据目标选择行为（修炼/工作/休息）
    │
    └── 任务选择（Task Selection）
            └── 根据行为选择具体任务
```

## AIDecisionService

```kotlin
class AIDecisionService(override val world: World) : EntityRelationContext, System {
    
    companion object {
        const val BATCH_SIZE = 20  // 每帧处理20个
    }
    
    private val pendingEntities = mutableListOf<Entity>()
    private var currentIndex = 0
    private var lastUpdateTime = 0L
    
    override fun onEnable() {
        refreshEntityList()
    }
    
    override fun update(deltaTime: Float) {
        // 每秒钟刷新一次列表（可能有新实体或实体死亡）
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastUpdateTime > 1000) {
            lastUpdateTime = currentTime
            refreshEntityList()
        }
        
        if (pendingEntities.isEmpty()) return
        
        // 处理一批
        val endIndex = minOf(currentIndex + BATCH_SIZE, pendingEntities.size)
        val batch = pendingEntities.subList(currentIndex, endIndex)
        
        batch.forEach { entity ->
            processEntity(entity)
        }
        
        currentIndex = endIndex
        if (currentIndex >= pendingEntities.size) {
            currentIndex = 0  // 重新开始一轮
        }
    }
    
    private fun refreshEntityList() {
        pendingEntities.clear()
        
        world.query {
            entityFilter {
                hasTag<AliveTag>()
                hasTag<ActiveTag>()
                hasTag<NPCTag>()  // 只处理AI控制的实体
            }
        }.forEach { pendingEntities.add(it.entity) }
        
        // 随机打乱，避免总是先处理同一批
        pendingEntities.shuffle()
    }
    
    private fun processEntity(entity: Entity) {
        val context = DecisionContext(entity, world)
        
        // 1. 评估当前状态
        val currentState = getCurrentState(entity)
        
        // 2. 评估需求（优先级）
        val needs = evaluateNeeds(entity, context)
        
        // 3. 选择目标
        val goal = selectGoal(entity, needs, context)
        
        // 4. 规划行为
        val behavior = planBehavior(entity, goal, context)
        
        // 5. 执行状态转换
        if (behavior != currentState) {
            executeStateTransition(entity, currentState, behavior)
        }
        
        // 6. 执行当前行为的具体动作
        executeAction(entity, behavior, context)
    }
}
```

## 决策上下文

```kotlin
data class DecisionContext(
    val entity: Entity,
    val world: World,
    val gameTime: GameTime = world.getSingleton(),
    val personality: Personality = entity.getComponent()!!,
    val currentState: BehaviorState = getCurrentState(entity),
    val location: Location = entity.getComponent()!!,
    val health: Health = entity.getComponent()!!,
    val spiritPower: SpiritPower = entity.getComponent()!!,
    val realm: CultivationRealm = entity.getComponent()!!,
    val progress: CultivationProgress = entity.getComponent()!!
)
```

## 需求评估

```kotlin
fun evaluateNeeds(entity: Entity, context: DecisionContext): Needs {
    val needs = Needs()
    
    // 生存需求
    if (context.health.current < context.health.max * 0.3f) {
        needs.survival += 0.9f  // 低血量，急需治疗
    }
    if (context.health.current < context.health.max * 0.5f) {
        needs.survival += 0.5f
    }
    
    // 恢复需求
    if (context.spiritPower.current < context.spiritPower.max * 0.2f) {
        needs.rest += 0.7f
    }
    
    // 修炼需求
    val cultivationUrgency = when {
        context.progress.percentage > 90f -> 0.9f  // 快满100%，急需突破
        context.progress.percentage > 70f -> 0.6f
        context.personality.diligence > 70 -> 0.5f  // 勤勉型更想修炼
        context.personality.ambition > 70 -> 0.4f   // 野心型想提升
        else -> 0.2f
    }
    needs.cultivation = cultivationUrgency
    
    // 社交需求（基于性格和心情）
    needs.social = context.personality.kindness / 100f * 0.3f
    
    // 工作需求（贡献点不足时）
    val contribution = entity.getComponent<ContributionPoints>()
    if (contribution != null && contribution.amount < 100) {
        needs.work = 0.4f
    }
    
    return needs
}

data class Needs(
    var survival: Float = 0f,      // 生存（治疗）
    var rest: Float = 0f,          // 休息
    var cultivation: Float = 0f,   // 修炼
    var work: Float = 0f,          // 工作
    var social: Float = 0f         // 社交
)
```

## 目标选择

```kotlin
fun selectGoal(entity: Entity, needs: Needs, context: DecisionContext): Goal {
    return when {
        needs.survival > 0.7f -> Goal.HEAL           // 优先治疗
        needs.rest > 0.6f && isRestTime(context.gameTime) -> Goal.REST
        needs.cultivation > 0.7f -> Goal.BREAKTHROUGH  // 尝试突破
        needs.cultivation > 0.4f -> Goal.CULTIVATE     // 正常修炼
        needs.work > 0.5f -> Goal.WORK
        needs.social > 0.4f -> Goal.SOCIAL
        else -> Goal.IDLE
    }
}

enum class Goal {
    SURVIVE,        // 生存（濒死）
    HEAL,           // 治疗
    REST,           // 休息
    BREAKTHROUGH,   // 突破境界
    CULTIVATE,      // 修炼
    WORK,           // 工作
    SOCIAL,         // 社交
    IDLE            // 空闲
}
```

## 行为规划

```kotlin
fun planBehavior(entity: Entity, goal: Goal, context: DecisionContext): BehaviorState {
    return when (goal) {
        Goal.SURVIVE -> findHealerOrRest(entity)
        Goal.HEAL -> if (hasHealingPill(entity)) UsePill else GoToMedicalFacility
        Goal.REST -> BehaviorState.Resting
        Goal.BREAKTHROUGH -> if (canAttemptBreakthrough(context)) BehaviorState.Breakthrough else BehaviorState.Cultivating
        Goal.CULTIVATE -> selectCultivationBehavior(entity, context)
        Goal.WORK -> selectWorkTask(entity, context)
        Goal.SOCIAL -> selectSocialActivity(entity, context)
        Goal.IDLE -> BehaviorState.Idle
    }
}

fun selectCultivationBehavior(entity: Entity, context: DecisionContext): BehaviorState {
    val period = context.gameTime.getPeriod()
    
    return when (period) {
        DayPeriod.ZI -> BehaviorState.Meditating  // 子时冥想
        DayPeriod.MAO -> BehaviorState.MorningPractice  // 卯时晨练
        DayPeriod.SI, DayPeriod.WEI -> BehaviorState.Cultivating  // 巳时、未时修炼
        else -> {
            // 根据性格决定
            if (context.personality.diligence > 80) {
                BehaviorState.Cultivating  // 勤勉型继续修炼
            } else {
                BehaviorState.Idle
            }
        }
    }
}

fun selectWorkTask(entity: Entity, context: DecisionContext): BehaviorState {
    val skills = evaluateSkills(entity)
    
    return when {
        skills.alchemy > 50 -> BehaviorState.AlchemyWork
        skills.forge > 50 -> BehaviorState.ForgeWork
        skills.herb > 50 -> BehaviorState.GatherHerbs
        else -> BehaviorState.GeneralWork
    }
}
```

## 状态转换执行

```kotlin
fun executeStateTransition(
    entity: Entity,
    from: BehaviorState,
    to: BehaviorState
) {
    // 1. 清理旧状态
    when (from) {
        is BehaviorState.Cultivating -> stopCultivation(entity)
        is BehaviorState.Working -> stopWork(entity)
        is BehaviorState.Fighting -> endCombat(entity)
        else -> {}
    }
    
    // 2. 添加新状态标签
    entity.editor {
        // 移除旧的行为标签
        it.removeTag<CultivatingTag>()
        it.removeTag<WorkingTag>()
        it.removeTag<RestingTag>()
        
        // 添加新的
        when (to) {
            is BehaviorState.Cultivating -> it.addTag<CultivatingTag>()
            is BehaviorState.Working -> it.addTag<WorkingTag>()
            is BehaviorState.Resting -> it.addTag<RestingTag>()
            is BehaviorState.Meditating -> it.addTag<MeditatingTag>()
            else -> it.addTag<IdleTag>()
        }
    }
    
    // 3. 记录日志
    logActivity(entity, "从 $from 转为 $to")
}
```

## 性格影响

```kotlin
fun modifyDecisionByPersonality(
    baseDecision: Decision,
    personality: Personality
): Decision {
    var modified = baseDecision
    
    // 高野心：更倾向于高风险高回报
    if (personality.ambition > 70) {
        if (baseDecision == Goal.IDLE) {
            modified = Goal.CULTIVATE
        }
    }
    
    // 高勤勉：减少休息时间
    if (personality.diligence > 80) {
        if (baseDecision == Goal.REST && personality.diligence > 90) {
            modified = Goal.CULTIVATE  // 超级勤奋，不休息
        }
    }
    
    // 高谨慎：避免危险
    if (personality.caution > 70) {
        if (baseDecision == Goal.BREAKTHROUGH && personality.caution > 85) {
            modified = Goal.CULTIVATE  // 过于谨慎，不敢突破
        }
    }
    
    // 高贪婪：优先收益
    if (personality.greed > 70) {
        if (baseDecision == Goal.SOCIAL) {
            modified = Goal.WORK  // 不去社交，去赚钱
        }
    }
    
    return modified
}
```

## 性能优化

1. **分批处理**：每帧20个，1000个实体50帧处理一轮（约0.8秒@60fps）
2. **缓存决策**：短时间内（如1秒内）不重复决策
3. **事件触发**：特殊事件立即触发决策，不等待轮询
4. **优先级队列**：紧急需求（如濒死）优先处理

## 依赖关系

- **依赖**：`core`、`disciple`、`cultivation`、`status` 标签
- **被依赖**：所有行为执行系统
