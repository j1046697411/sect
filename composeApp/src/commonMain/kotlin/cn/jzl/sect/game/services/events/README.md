# 事件服务 (Event Service)

本模块实现游戏事件的生成和处理系统。

## 核心功能

1. **随机事件生成**：根据游戏状态生成各种事件
2. **事件处理**：AI自动处理或玩家决策
3. **事件影响**：执行事件结果

## 事件架构

```
EventSystem
    ├── 随机事件生成器
    │       └── 定时检查生成条件
    ├── 事件队列
    │       └── 存储待处理事件
    └── 事件处理器
            ├── AI处理器（普通事件）
            └── 玩家决策器（重大事件）
```

## 事件定义

```kotlin
// 事件优先级
enum class EventPriority {
    NORMAL,      // 普通：AI自动处理
    IMPORTANT,   // 重要：通知玩家
    CRITICAL     // 重大：必须玩家决策，暂停游戏
}

// 事件基类
sealed class GameEvent(
    open val id: String,
    open val priority: EventPriority,
    open val triggerTime: Long,
    open val title: String,
    open val description: String
)

// 具体事件类型

/// 妖兽袭击
@JvmInline value class DemonBeastId(val value: Long)

data class DemonBeastAttackEvent(
    override val id: String,
    val beastId: DemonBeastId,
    val beastName: String,
    val beastLevel: Int,
    val targetRegion: Long,
    val targetRegionName: String,
    override val triggerTime: Long = System.currentTimeMillis()
) : GameEvent(
    id = id,
    priority = EventPriority.CRITICAL,
    triggerTime = triggerTime,
    title = "妖兽袭击警报",
    description = "一只${beastLevel}级妖兽'${beastName}'正在袭击${targetRegionName}！"
)

/// 资源发现
data class ResourceDiscoveryEvent(
    override val id: String,
    val resourceType: ResourceType,
    val quantity: Int,
    val location: String,
    val discoveryDifficulty: Int,
    override val triggerTime: Long = System.currentTimeMillis()
) : GameEvent(
    id = id,
    priority = EventPriority.NORMAL,
    triggerTime = triggerTime,
    title = "资源发现",
    description = "在${location}发现了${quantity}单位的${resourceType}！"
)

/// 弟子请求
data class DiscipleRequestEvent(
    override val id: String,
    val discipleId: Long,
    val discipleName: String,
    val requestType: RequestType,
    val requestContent: String,
    val deadline: Long? = null,
    override val triggerTime: Long = System.currentTimeMillis()
) : GameEvent(
    id = id,
    priority = EventPriority.IMPORTANT,
    triggerTime = triggerTime,
    title = "${discipleName}的请求",
    description = requestContent
)

enum class RequestType {
    PROMOTION,      // 请求晋升
    RESOURCE,       // 请求资源
    LEAVE,          // 请求离开
    MENTORSHIP,     // 请求拜师
    EQUIPMENT,      // 请求装备
    MISSION         // 请求任务
}

/// 外交事件
data class DiplomaticEvent(
    override val id: String,
    val otherSectId: Long,
    val otherSectName: String,
    val eventType: DiplomaticType,
    override val triggerTime: Long = System.currentTimeMillis()
) : GameEvent(
    id = id,
    priority = EventPriority.CRITICAL,
    triggerTime = triggerTime,
    title = "外交事件",
    description = "${otherSectName} ${eventType.description}"
)

enum class DiplomaticType(val description: String) {
    ALLIANCE_REQUEST("请求结盟"),
    WAR_DECLARATION("宣战"),
    TRADE_PROPOSAL("提议贸易"),
    TERRITORY_DISPUTE("领土争端")
}

/// 内部事件
data class InternalEvent(
    override val id: String,
    val eventType: InternalEventType,
    val involvedParties: List<Long>,
    override val triggerTime: Long = System.currentTimeMillis()
) : GameEvent(
    id = id,
    priority = EventPriority.IMPORTANT,
    triggerTime = triggerTime,
    title = "内部事件",
    description = generateDescription(eventType)
)

enum class InternalEventType {
    CONFLICT,       // 弟子冲突
    BETRAYAL,       // 背叛
    THEFT,          // 盗窃
    ROMANCE,        // 恋情
    BREAKTHROUGH    // 某人突破（庆祝）
}
```

## EventService

```kotlin
class EventService(override val world: World) : EntityRelationContext, System {
    
    private val eventQueue = mutableListOf<GameEvent>()
    private var checkTimer = 0f
    private val checkInterval = 30f  // 每30秒检查一次
    
    private val random = Random.Default
    
    override fun update(deltaTime: Float) {
        checkTimer += deltaTime
        
        // 定时检查生成随机事件
        if (checkTimer >= checkInterval) {
            checkTimer = 0f
            generateRandomEvents()
        }
        
        // 处理队列中的事件
        processEventQueue()
    }
    
    /// 生成随机事件
    private fun generateRandomEvents() {
        val roll = random.nextFloat()
        
        when {
            roll < 0.001f -> generateDemonBeastAttack()
            roll < 0.005f -> generateResourceDiscovery()
            roll < 0.01f -> generateDiscipleRequest()
            roll < 0.015f -> generateInternalEvent()
            // ... 其他事件类型
        }
    }
    
    private fun generateDemonBeastAttack() {
        val regions = getOccupiedRegions()
        if (regions.isEmpty()) return
        
        val targetRegion = regions.random()
        val beastLevel = calculateBeastLevel()
        
        val event = DemonBeastAttackEvent(
            id = "beast_${System.currentTimeMillis()}",
            beastId = DemonBeastId(random.nextLong()),
            beastName = generateBeastName(),
            beastLevel = beastLevel,
            targetRegion = targetRegion.id,
            targetRegionName = targetRegion.name
        )
        
        addEvent(event)
    }
    
    private fun generateResourceDiscovery() {
        val location = getExplorableLocation()
        val resourceType = ResourceType.values().random()
        val quantity = random.nextInt(10, 100)
        
        val event = ResourceDiscoveryEvent(
            id = "resource_${System.currentTimeMillis()}",
            resourceType = resourceType,
            quantity = quantity,
            location = location.name,
            discoveryDifficulty = random.nextInt(1, 10)
        )
        
        addEvent(event)
    }
    
    private fun generateDiscipleRequest() {
        val disciples = getActiveDisciples()
        if (disciples.isEmpty()) return
        
        val disciple = disciples.random()
        val requestType = RequestType.values().random()
        
        val event = DiscipleRequestEvent(
            id = "request_${System.currentTimeMillis()}",
            discipleId = disciple.id,
            discipleName = disciple.getComponent<EntityName>()?.value ?: "无名",
            requestType = requestType,
            requestContent = generateRequestContent(requestType, disciple),
            deadline = System.currentTimeMillis() + 24 * 60 * 60 * 1000  // 1天后
        )
        
        addEvent(event)
    }
    
    /// 添加事件到队列
    fun addEvent(event: GameEvent) {
        eventQueue.add(event)
        
        // 触发事件添加通知
        world.eventBus.emit(EventAddedEvent(event))
        
        // 如果是重大事件，暂停游戏
        if (event.priority == EventPriority.CRITICAL) {
            val time = world.getSingleton<GameTime>()
            world.setSingleton(time.copy(isPaused = true))
        }
    }
    
    /// 处理事件队列
    private fun processEventQueue() {
        val iterator = eventQueue.iterator()
        while (iterator.hasNext()) {
            val event = iterator.next()
            
            when (event.priority) {
                EventPriority.NORMAL -> {
                    // AI自动处理
                    aiHandleEvent(event)
                    iterator.remove()
                }
                EventPriority.IMPORTANT -> {
                    // 等待玩家查看，不自动处理
                    // 超时后AI处理
                    if (isEventExpired(event)) {
                        aiHandleEvent(event)
                        iterator.remove()
                    }
                }
                EventPriority.CRITICAL -> {
                    // 必须玩家决策，不自动处理
                    // 如果玩家超时未处理，AI给出建议
                }
            }
        }
    }
    
    /// AI处理普通事件
    private fun aiHandleEvent(event: GameEvent) {
        when (event) {
            is ResourceDiscoveryEvent -> {
                // 自动派遣弟子采集
                autoDispatchGathering(event)
            }
            is InternalEvent -> {
                // 根据类型处理
                handleInternalEvent(event)
            }
            // ... 其他事件类型
        }
    }
    
    /// 玩家处理事件
    fun playerHandleEvent(eventId: String, decision: EventDecision) {
        val event = eventQueue.find { it.id == eventId } ?: return
        
        // 执行决策
        applyDecision(event, decision)
        
        // 从队列移除
        eventQueue.remove(event)
        
        // 如果是重大事件，恢复游戏
        if (event.priority == EventPriority.CRITICAL) {
            val time = world.getSingleton<GameTime>()
            world.setSingleton(time.copy(isPaused = false))
        }
    }
    
    /// 获取待处理事件
    fun getPendingEvents(): List<GameEvent> = eventQueue.toList()
    
    /// 获取重大事件
    fun getCriticalEvents(): List<GameEvent> {
        return eventQueue.filter { it.priority == EventPriority.CRITICAL }
    }
}
```

## 事件决策

```kotlin
// 决策选项
data class EventDecision(
    val optionId: String,
    val description: String,
    val effects: List<DecisionEffect>
)

// 决策效果
sealed class DecisionEffect {
    data class ResourceChange(val type: ResourceType, val amount: Int) : DecisionEffect()
    data class ReputationChange(val amount: Int) : DecisionEffect()
    data class RelationshipChange(val targetId: Long, val change: Float) : DecisionEffect()
    data class TriggerEvent(val eventType: String) : DecisionEffect()
    object Nothing : DecisionEffect()
}

// 生成决策选项
fun generateDecisionOptions(event: GameEvent): List<EventDecision> {
    return when (event) {
        is DemonBeastAttackEvent -> listOf(
            EventDecision(
                optionId = "fight",
                description = "派遣弟子讨伐妖兽",
                effects = listOf(
                    DecisionEffect.ResourceChange(ResourceType.SPIRIT_STONE, -100),
                    DecisionEffect.TriggerEvent("combat")
                )
            ),
            EventDecision(
                optionId = "defend",
                description = "固守宗门，加强防御",
                effects = listOf(
                    DecisionEffect.ResourceChange(ResourceType.SPIRIT_STONE, -50),
                    DecisionEffect.ReputationChange(-10)
                )
            ),
            EventDecision(
                optionId = "evacuate",
                description = "暂时撤离，避其锋芒",
                effects = listOf(
                    DecisionEffect.ReputationChange(-30),
                    DecisionEffect.RelationshipChange(event.targetRegion, -0.2f)
                )
            )
        )
        // ... 其他事件类型的决策选项
    }
}

// 执行决策
fun applyDecision(event: GameEvent, decision: EventDecision) {
    decision.effects.forEach { effect ->
        when (effect) {
            is DecisionEffect.ResourceChange -> {
                // 修改资源
                modifyResource(effect.type, effect.amount)
            }
            is DecisionEffect.ReputationChange -> {
                // 修改声望
                modifyReputation(effect.amount)
            }
            is DecisionEffect.RelationshipChange -> {
                // 修改关系
                modifyRelationship(effect.targetId, effect.change)
            }
            is DecisionEffect.TriggerEvent -> {
                // 触发新事件
                triggerEvent(effect.eventType)
            }
            DecisionEffect.Nothing -> {}
        }
    }
}
```

## 依赖关系

- **依赖**：`core`、`resources` 组件、所有标签和关系
- **被依赖**：UI 层（显示事件）、AI 服务（处理普通事件）
