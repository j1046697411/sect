# 查询上下文 (Query Context)

本模块定义所有 EntityQueryContext，用于高效查询实体。

## 设计原则

1. **单一职责**：每个 Context 只查询特定类型的实体
2. **可组合**：可以在 filter 中组合多个条件
3. **类型安全**：编译时检查组件类型

## 核心 Context

### DiscipleContext

```kotlin
class DiscipleContext(world: World) : EntityQueryContext(world) {
    val name by component<EntityName>()
    val age by component<Age>()
    val realm by component<CultivationRealm>()
    val progress by component<CultivationProgress>()
    val position by component<Position>()
    val contribution by component<ContributionPoints>()
    val talent by component<InnateTalent>()
    val personality by component<Personality>()
    val health by component<Health>()
    val spiritPower by component<SpiritPower>()
    val combatStats by component<CombatStats>()
    
    // 可选组件
    val master by relation<Mentorship>()
    val sect by relation<SectMembership>()
}
```

### FacilityContext

```kotlin
class FacilityContext(world: World) : EntityQueryContext(world) {
    val info by component<FacilityInfo>()
    val location by component<Location>()
    val maintenance by component<MaintenanceCost>()
    
    // 效果组件（根据类型不同）
    val cultivationEffect by component<CultivationFacilityEffect?>()
    val productionEffect by component<ProductionFacilityEffect?>()
    val storageEffect by component<StorageFacilityEffect?>()
}
```

### ResourceContext

```kotlin
class ResourceContext(world: World) : EntityQueryContext(world) {
    val inventory by component<ResourceInventory>()
    
    // 获取特定资源数量
    fun getResourceAmount(type: ResourceType): Int {
        return inventory.items[type] ?: 0
    }
    
    // 获取总资源量
    fun getTotalAmount(): Int = inventory.getTotal()
}
```

### EventContext

```kotlin
class EventContext(world: World) : EntityQueryContext(world) {
    // 事件实体使用 event bus 管理，不需要 query context
    // 这里定义用于查询事件相关实体的 context
}
```

## 使用示例

### 基础查询

```kotlin
// 查询所有弟子
val allDisciples = world.query { DiscipleContext(world) }.toList()

// 查询所有设施
val allFacilities = world.query { FacilityContext(world) }.toList()
```

### 条件查询

```kotlin
// 查询炼气期弟子
val qiRefiningDisciples = world.query {
    DiscipleContext(world)
}.filter { ctx ->
    ctx.realm is CultivationRealm.QiRefining
}.toList()

// 查询受伤弟子
val injuredDisciples = world.query {
    DiscipleContext(world)
}.filter { ctx ->
    ctx.entity.hasTag<InjuredTag>()
}.toList()

// 查询高忠诚度弟子
val loyalDisciples = world.query {
    DiscipleContext(world)
}.filter { ctx ->
    ctx.personality.loyalty > 70
}.toList()
```

### 复合条件

```kotlin
// 查询可派遣执行任务的弟子
val availableDisciples = world.query {
    DiscipleContext(world)
}.filter { ctx ->
    ctx.entity.hasTag<IdleTag>() &&
    !ctx.entity.hasTag<InjuredTag>() &&
    ctx.health.current > ctx.health.max * 0.5f &&
    ctx.realm.level >= 5  // 炼气五层以上
}.toList()

// 查询需要关注的弟子（突破在即或受伤）
val needAttention = world.query {
    DiscipleContext(world)
}.filter { ctx ->
    (ctx.progress.percentage > 90f) ||  // 快突破了
    ctx.entity.hasTag<InjuredTag>() ||   // 受伤了
    ctx.health.current < ctx.health.max * 0.3f  // 血量低
}.toList()
```

### 关联查询

```kotlin
// 查询某长老的所有徒弟
fun getDisciplesOfMaster(master: Entity): List<DiscipleContext> {
    return world.query {
        DiscipleContext(world)
    }.filter { ctx ->
        ctx.master?.target == master
    }.toList()
}

// 查询某门派的所有成员
fun getMembersOfSect(sect: Entity): List<DiscipleContext> {
    return world.query {
        DiscipleContext(world)
    }.filter { ctx ->
        ctx.sect?.target == sect
    }.toList()
}
```

## 性能优化

### 缓存查询结果

```kotlin
class CachedQueryService(override val world: World) : EntityRelationContext {
    private var cachedDisciples: List<DiscipleContext>? = null
    private var lastUpdate = 0L
    private val cacheTimeout = 5000L  // 5秒缓存
    
    fun getAllDisciples(): List<DiscipleContext> {
        val now = System.currentTimeMillis()
        if (cachedDisciples == null || now - lastUpdate > cacheTimeout) {
            cachedDisciples = world.query { DiscipleContext(world) }.toList()
            lastUpdate = now
        }
        return cachedDisciples!!
    }
}
```

### 批量处理

```kotlin
// 批量更新弟子年龄
fun batchUpdateAge(years: Int) {
    world.query { DiscipleContext(world) }.forEach { ctx ->
        val newAge = ctx.age.copy(years = ctx.age.years + years)
        ctx.entity.editor {
            it.addComponent(newAge)
        }
    }
}
```

## 依赖关系

- **依赖**：所有组件模块
- **被依赖**：所有 Service 模块
