# World 配置 (World Configuration)

本模块负责 ECS World 的初始化、配置和管理。

## 核心功能

1. **World 初始化**：创建和配置 ECS World
2. **组件注册**：注册所有 Component ID
3. **系统注册**：注册所有 System
4. **依赖注入**：配置 DI 容器

## SectWorld

```kotlin
object SectWorld {
    
    lateinit var world: World
        private set
    
    /// 初始化 World
    fun initialize() {
        world = createWorld {
            // 1. 配置依赖注入
            injects {
                // Service 注册
                bind singleton { new(::TimeService) }
                bind singleton { new(::AIDecisionService) }
                bind singleton { new(::CultivationService) }
                bind singleton { new(::DiscipleFactory) }
                bind singleton { new(::DiscipleQueryService) }
                bind singleton { new(::DiscipleManagementService) }
                bind singleton { new(::EventService) }
                bind singleton { new(::MentorshipService) }
                bind singleton { new(::SectMembershipService) }
                // ... 其他 Service
            }
            
            // 2. 注册组件 ID
            components {
                // 基础组件
                componentId<EntityName>()
                componentId<Age>()
                componentId<Lifespan>()
                componentId<SpiritStone>()
                componentId<ContributionPoints>()
                componentId<Reputation>()
                componentId<Location>()
                
                // 弟子组件
                componentId<InnateTalent>()
                componentId<CombatStats>()
                componentId<Personality>()
                componentId<Health>()
                componentId<SpiritPower>()
                componentId<Injury>()
                componentId<Position>()
                componentId<SectId>()
                componentId<TotalContribution>()
                
                // 技能组件
                componentId<AlchemySkill>()
                componentId<ForgeSkill>()
                componentId<TalismanSkill>()
                componentId<FormationSkill>()
                componentId<HerbSkill>()
                
                // 修炼组件
                componentId<CultivationRealm>()
                componentId<CultivationProgress>()
                componentId<BreakthroughAttempt>()
                componentId<BreakthroughRecord>()
                componentId<TechniqueProgress>()
                componentId<CultivationTechnique>()
                componentId<CultivationBonus>()
                componentId<ActiveCultivationAid>()
                
                // 设施组件
                componentId<FacilityInfo>()
                componentId<ConstructionInfo>()
                componentId<MaintenanceCost>()
                componentId<CultivationFacilityEffect>()
                componentId<ProductionFacilityEffect>()
                componentId<StorageFacilityEffect>()
                
                // 资源组件
                componentId<ResourceInventory>()
                componentId<ResourceStack>()
                componentId<Pill>()
                componentId<PillEffect>()
                componentId<Equipment>()
                componentId<EquipmentStats>()
                
                // 时间组件
                componentId<GameTime>()
                
                // 事件组件
                componentId<DemonBeastAttackEvent>()
                componentId<ResourceDiscoveryEvent>()
                componentId<DiscipleRequestEvent>()
                componentId<DiplomaticEvent>()
                componentId<InternalEvent>()
                
                // 标签（标记为 tag）
                componentId<Male> { it.tag() }
                componentId<Female> { it.tag() }
                
                componentId<AliveTag> { it.tag() }
                componentId<DeadTag> { it.tag() }
                componentId<DyingTag> { it.tag() }
                componentId<ActiveTag> { it.tag() }
                componentId<InactiveTag> { it.tag() }
                componentId<PlayerTag> { it.tag() }
                componentId<NPCTag> { it.tag() }
                
                componentId<IdleTag> { it.tag() }
                componentId<CultivatingTag> { it.tag() }
                componentId<WorkingTag> { it.tag() }
                componentId<RestingTag> { it.tag() }
                componentId<TravelingTag> { it.tag() }
                componentId<FightingTag> { it.tag() }
                componentId<MeditatingTag> { it.tag() }
                componentId<SocializingTag> { it.tag() }
                componentId<InBreakthroughTag> { it.tag() }
                componentId<InSeclusionTag> { it.tag() }
                
                componentId<InjuredTag> { it.tag() }
                componentId<PoisonedTag> { it.tag() }
                componentId<SickTag> { it.tag() }
                componentId<DisabledTag> { it.tag() }
                
                componentId<InSectTag> { it.tag() }
                componentId<ExiledTag> { it.tag() }
                componentId<BetrayedTag> { it.tag() }
                componentId<RetiredTag> { it.tag() }
                
                componentId<ConstructingTag> { it.tag() }
                componentId<OperatingTag> { it.tag() }
                componentId<MaintainingTag> { it.tag() }
                componentId<FacilityIdleTag> { it.tag() }
                componentId<DamagedTag> { it.tag() }
                
                componentId<MainTechnique> { it.tag() }
                componentId<Equipped> { it.tag() }
            }
            
            // 3. 注册系统（按执行顺序）
            systems {
                // 输入处理阶段
                addSystem<InputSystem>()
                
                // 时间更新阶段
                addSystem<TimeSystem>()
                
                // AI 决策阶段（分批处理）
                addSystem<AIDecisionService>()
                
                // 行为执行阶段
                addSystem<BehaviorExecutionSystem>()
                addSystem<CultivationSystem>()
                addSystem<CombatSystem>()
                addSystem<SocialSystem>()
                
                // 资源更新阶段
                addSystem<ResourceSystem>()
                addSystem<FacilitySystem>()
                
                // 事件处理阶段
                addSystem<EventService>()
                addSystem<MissionSystem>()
                
                // UI 更新阶段
                addSystem<UISystem>()
            }
            
            // 4. 配置事件总线
            events {
                register<HourChangedEvent>()
                register<DayChangedEvent>()
                register<MonthChangedEvent>()
                register<YearChangedEvent>()
                
                register<BreakthroughSuccessEvent>()
                register<BreakthroughFailureEvent>()
                
                register<DiscipleJoinedEvent>()
                register<DisciplePromotedEvent>()
                register<DiscipleExpelledEvent>()
                register<DiscipleDeathEvent>()
                
                register<EntityDeathEvent>()
                register<EventAddedEvent>()
            }
        }
        
        // 初始化游戏数据
        initializeGameData()
    }
    
    /// 初始化游戏数据
    private fun initializeGameData() {
        // 1. 创建初始门派
        val sect = createInitialSect()
        
        // 2. 创建初始弟子
        createInitialDisciples(sect)
        
        // 3. 创建初始设施
        createInitialFacilities(sect)
        
        // 4. 初始化游戏时间
        world.setSingleton(GameTime(
            year = 1,
            month = 1,
            day = 1,
            hour = 6,
            minute = 0,
            totalMinutes = 0
        ))
    }
    
    private fun createInitialSect(): Entity {
        return world.entity {
            it.addComponent(EntityName("青云宗"))
            it.addComponent(SectLevel(1))
            it.addComponent(SectFunds(10000))
            it.addComponent(SectReputation(100))
        }
    }
    
    private fun createInitialDisciples(sect: Entity) {
        val factory = world.di.instance<DiscipleFactory>()
        
        // 创建掌门
        val leader = factory.createDisciple(
            name = "李掌门",
            age = 45,
            gender = Male::class,
            sect = sect,
            generation = 1,
            position = Position.SECT_LEADER,
            customTalent = InnateTalent(80, 75, 70, 85, 60)
        )
        leader.editor {
            it.addComponent(CultivationRealm.GoldenCore.Early)
            it.addComponent(CultivationProgress(30f))
        }
        
        // 创建长老
        repeat(3) { i ->
            val elder = factory.createRandomDisciple(sect, generation = 2)
            factory.world.entity(elder) {
                it.addComponent(Position.ELDER)
                it.addComponent(CultivationRealm.Foundation.Late)
            }
        }
        
        // 创建一批内门弟子
        repeat(10) {
            val disciple = factory.createRandomDisciple(sect, generation = 3)
            factory.world.entity(disciple) {
                it.addComponent(Position.INNER_DISCIPLE)
            }
        }
        
        // 创建一批外门弟子
        repeat(20) {
            factory.createRandomDisciple(sect, generation = 4)
        }
    }
    
    private fun createInitialFacilities(sect: Entity) {
        // 创建修炼室
        repeat(5) {
            world.entity {
                it.addComponent(FacilityInfo(
                    name = "修炼室${it + 1}",
                    type = FacilityType.CULTIVATION_ROOM,
                    level = 1,
                    maxLevel = 5
                ))
                it.addComponent(CultivationFacilityEffect(
                    speedBonus = 0.1f,
                    breakthroughBonus = 0f,
                    capacity = 5
                ))
                it.addRelation<Ownership>(target = sect)
                it.addTag<IdleTag>()
            }
        }
        
        // 创建炼丹房
        world.entity {
            it.addComponent(FacilityInfo(
                name = "初级炼丹房",
                type = FacilityType.ALCHEMY_LAB,
                level = 1,
                maxLevel = 5
            ))
            it.addComponent(ProductionFacilityEffect(
                outputRate = 10f,
                qualityBonus = 0f,
                workerCapacity = 3
            ))
            it.addRelation<Ownership>(target = sect)
            it.addTag<IdleTag>()
        }
        
        // ... 其他初始设施
    }
    
    /// 获取 World 实例
    fun get(): World = world
    
    /// 获取 Service 实例
    inline fun <reified T> getService(): T = world.di.instance()
}
```

## 使用示例

### 在 Application 中初始化

```kotlin
class SectApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // 初始化 World
        SectWorld.initialize()
    }
}
```

### 在 Composable 中获取数据

```kotlin
@Composable
fun OverviewScreen() {
    val world = remember { SectWorld.get() }
    val sectQueryService = remember { SectWorld.getService<SectQueryService>() }
    
    val sectInfo by produceState<SectInfo?>(initialValue = null) {
        value = sectQueryService.getSectInfo()
    }
    
    // 显示数据...
}
```

## 依赖关系

- **依赖**：所有组件、所有 Service
- **被依赖**：应用入口、UI 层
