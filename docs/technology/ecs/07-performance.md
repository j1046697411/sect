# ECS 性能优化指南

> 深入优化 ECS 框架性能的最佳实践，涵盖内存、CPU 和架构层面的优化策略。

---

## 1. 核心原则

### 1.1 零分配原则（Zero Allocation）

ECS 核心循环中应避免内存分配：

```kotlin
// ❌ 错误：每次迭代创建新对象
world.query { HealthContext(this) }.forEach { ctx ->
    val message = "Health: ${ctx.health.current}"  // 字符串分配
    log(message)
}

// ✅ 正确：复用或延迟创建
class HealthSystem : EntityRelationContext {
    private val sb = StringBuilder()  // 复用
    
    fun log() {
        world.query { HealthContext(this) }.forEach { ctx ->
            sb.clear()
            sb.append("Health: ").append(ctx.health.current)
            logToFile(sb.toString())  // 或使用 logger
        }
    }
}
```

### 1.2 数据局部性

相关数据存储在连续内存中：

```kotlin
// ❌ 错误：跨组件查询
world.query { PositionContext(this) }.forEach { posCtx ->
    val health = entity.getComponent<Health>()  // 非局部访问
}

// ✅ 正确：在同一查询中处理
class PositionHealthContext(world: World) : EntityQueryContext(world) {
    val position: Position by component()
    val health: Health by component()
}

world.query { PositionHealthContext(this) }.forEach { ctx ->
    // position 和 health 都在缓存中
    process(ctx.position, ctx.health)
}
```

---

## 2. 查询优化

### 2.1 缓存查询上下文

```kotlin
class CombatSystem : EntityRelationContext {
    override lateinit var world: World
    
    // ✅ 正确：缓存上下文
    private val healthContext by lazy { HealthContext(world) }
    
    fun process() {
        world.query { healthContext }.forEach { }
    }
    
    // ❌ 错误：每次创建新实例
    fun badProcess() {
        world.query { HealthContext(world) }.forEach { }
    }
}
```

### 2.2 使用 Family 过滤

```kotlin
// ❌ 错误：内存中过滤
world.query { PositionContext(this) }
    .filter { it.position.y > 0 }  // 遍历所有实体
    
// ✅ 正确：使用 Family 预过滤
class GroundPositionContext(world: World) : EntityQueryContext(world) {
    val position: Position by component()
    
    override fun FamilyBuilder.configure() {
        component<Position>()
        // Family 引擎自动过滤
    }
}

world.query { GroundPositionContext(this) }  // 只返回符合条件的实体
```

### 2.3 批量操作

```kotlin
// ❌ 错误：逐个处理
entities.forEach { entity ->
    val health = entity.getComponent<Health>() ?: return@forEach
    entity.editor { it.addComponent(health.copy(current = health.current + 10)) }
}

// ✅ 正确：收集后批量处理
val targets = entities.toList()  // 收集到列表
targets.forEach { entity ->
    val health = entity.getComponent<Health>() ?: return@forEach
    entity.editor { it.addComponent(health.copy(current = health.current + 10)) }
}

// ✅ 最佳：使用 BatchEntityEditor
class BuffSystem : EntityRelationContext {
    private val editorPool by lazy { BatchEntityEditorPool(world) }
    
    fun batchAddBuff(entities: List<Entity>, buff: Buff) {
        val editor = editorPool.obtain()
        try {
            entities.forEach { entity ->
                editor.entity = entity
                editor.addComponent(buff)
            }
            editor.apply()
        } finally {
            editorPool.release(editor)
        }
    }
}
```

---

## 3. 组件设计优化

### 3.1 使用 value class

```kotlin
// ✅ 正确：单属性使用 value class（无装箱）
@JvmInline
value class Level(val value: Int)

@JvmInline
value class Experience(val xp: Long)

// ❌ 错误：单属性使用 data class（装箱开销）
data class Level(val value: Int)
```

### 3.2 组件存储特化

详见 [06-component-store.md](06-component-store.md)

```kotlin
world.componentId<Level> { it.store { intStore() } }
world.componentId<Health> { it.store { floatStore() } }
```

### 3.3 避免大组件

```kotlin
// ❌ 错误：组件过大
data class PlayerAllData(
    val name: String,
    val level: Int,
    val exp: Long,
    val hp: Int, val maxHp: Int,
    val mp: Int, val maxMp: Int,
    val x: Float, val y: Float,
    val inventory: List<Item>,  // 嵌套集合
    val quests: List<Quest>
)

// ✅ 正确：拆分为原子组件
data class PlayerName(val value: String)
data class PlayerLevel(val value: Int)
data class PlayerExp(val xp: Long)
data class Health(val current: Int, val max: Int)
data class Mana(val current: Int, val max: Int)
data class Position(val x: Float, val y: Float)
// 复杂数据用 Relation 或外部系统管理
```

---

## 4. 系统设计优化

### 4.1 分离读写操作

```kotlin
// ❌ 错误：混合读写
class BadSystem : EntityRelationContext {
    fun process() {
        world.query { HealthContext(this) }.forEach { ctx ->
            if (ctx.health.current > 0) {
                ctx.entity.editor {  // 写入
                    it.addComponent(Health(ctx.health.current - 1, ctx.health.max))
                }
            }
        }
    }
}

// ✅ 正确：分离读写
class HealthSystem : EntityRelationContext {
    // 读取：收集需要处理的实体
    fun findLowHealth(): List<Entity> {
        return world.query { HealthContext(this) }
            .filter { it.health.current > 0 }
            .map { it.entity }
            .toList()
    }
    
    // 写入：批量处理
    fun applyDamage(entities: List<Entity>, damage: Int) {
        entities.forEach { entity ->
            val health = entity.getComponent<Health>() ?: return@forEach
            entity.editor {
                it.addComponent(health.copy(
                    current = maxOf(0, health.current - damage)
                ))
            }
        }
    }
}
```

### 4.2 增量更新

```kotlin
// ❌ 错误：每帧全量计算
class ExpensiveSystem : EntityRelationContext {
    fun update() {
        val allEntities = world.query { TransformContext(this) }.toList()
        allEntities.forEach { ctx ->
            // 复杂计算
        }
    }
}

// ✅ 正确：增量更新
class OptimizedSystem : EntityRelationContext {
    private val dirtyEntities = mutableSetOf<Entity>()
    
    fun markDirty(entity: Entity) {
        dirtyEntities.add(entity)
    }
    
    fun update() {
        val targets = dirtyEntities.toList()
        dirtyEntities.clear()
        
        targets.forEach { entity ->
            // 只处理变化的实体
            if (entity.isActive()) {
                process(entity)
            }
        }
    }
}
```

---

## 5. 内存优化

### 5.1 对象池

```kotlin
class ParticleSystem : EntityRelationContext {
    // 复用粒子对象
    private val particlePool = ObjectPool(1000) { Particle() }
    
    fun spawn(): Particle {
        return particlePool.obtain().also {
            it.reset()
        }
    }
    
    fun despawn(particle: Particle) {
        particlePool.release(particle)
    }
}
```

### 5.2 避免不必要的包装

```kotlin
// ❌ 错误：包装不必要的类型
data class Wrapper(val value: Int)

// ✅ 正确：直接使用
val health: Int = 100
```

---

## 6. 性能分析工具

### 6.1 使用 Kotlin Profiler

```bash
# 运行基准测试
./gradlew :benchmarks:lko-ecs-benchmarks:mainBenchmark
```

### 6.2 内存分析

```kotlin
// 添加内存追踪
class MemoryTrackedSystem : EntityRelationContext {
    fun debugMemory() {
        val runtime = Runtime.getRuntime()
        println("Used: ${runtime.totalMemory() - runtime.freeMemory()} bytes")
    }
}
```

---

## 7. 性能检查清单

| 优化点 | 检查项 |
|--------|--------|
| **零分配** | 核心循环中无 `new` 操作 |
| **缓存** | QueryContext 使用 lazy 缓存 |
| **批量** | 批量操作替代逐个处理 |
| **组件** | 单属性使用 value class |
| **特化** | 数值组件使用特化存储 |
| **分离** | 读写操作分离 |
| **Family** | 使用 Family 过滤替代 filter |

---

## 8. 下一步

- 组件存储特化：[06-component-store.md](06-component-store.md)
- 测试指南：[08-testing.md](08-testing.md)
- 常见模式：[02-patterns.md](02-patterns.md)
