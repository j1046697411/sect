# ECS 测试最佳实践

**提取时间：** 2026-02-14
**适用场景：** 为 ECS 框架编写单元测试和集成测试

## 核心模式

### 模式1：测试专用 Addon 设置

所有 ECS 测试都需要注册组件ID，使用统一的addon模式：

```kotlin
class MyTest : EntityRelationContext {
    override val world: World by lazy {
        world { install(testAddon) }
    }
    
    private val testAddon = createAddon<Unit>("test") {
        components {
            // 组件
            world.componentId<Position>()
            world.componentId<Health>()
            
            // 单值组件使用 @JvmInline
            world.componentId<Level>()
            
            // 标签组件需要 .tag() 标记
            world.componentId<ActiveTag> { it.tag() }
            world.componentId<EnemyTag> { it.tag() }
        }
    }
}
```

### 模式2：测试数据类命名规范

为避免与生产代码冲突，测试数据类加前缀：

```kotlin
// ❌ 不推荐：可能与生产代码冲突
data class Position(val x: Int, val y: Int)

// ✅ 推荐：测试专用命名
private data class TestPosition(val x: Int, val y: Int)
private data class ArchePosition(val x: Int, val y: Int)
private data class ObserverPosition(val x: Int, val y: Int)
```

### 模式3：QueryContext 定义

为测试定义专用的 QueryContext：

```kotlin
// 基础组件查询
private class TestContext(world: World) : EntityQueryContext(world) {
    val position by component<TestPosition>()
    val health by component<TestHealth>()
}

// 可选组件
private class OptionalContext(world: World) : EntityQueryContext(world) {
    val position by component<TestPosition>()
    val equipment by component<Equipment?>()  // nullable = 可选
}

// 标签过滤在 configure 中
private class TaggedContext(world: World) : EntityQueryContext(world) {
    val position by component<TestPosition>()
    override fun FamilyBuilder.configure() {
        component<TestActiveTag>()
    }
}
```

### 模式4：实体创建辅助方法

```kotlin
// 创建基础实体
fun createEntity(): Entity = world.entity {}

// 创建带组件的实体
fun createEntityWithPos(x: Int, y: Int): Entity = 
    world.entity { it.addComponent(TestPosition(x, y)) }

// 批量创建
fun createEntities(count: Int): List<Entity> = 
    (0 until count).map { createEntity() }
```

### 模式5：Archetype 测试模式

```kotlin
// 获取实体所在archetype
private fun getEntityArchetype(entity: Entity): Archetype {
    return world.entityService.runOn(entity) { this }
}

// 测试archetype迁移
@Test
fun testArchetypeMigration() {
    val entity = world.entity {}
    val initialArchetype = getEntityArchetype(entity)
    
    entity.editor { it.addComponent(TestPosition(10, 20)) }
    val afterArchetype = getEntityArchetype(entity)
    
    assertNotEquals(initialArchetype.id, afterArchetype.id)
}
```

## 测试分类模板

### 组件测试 (ComponentTest)

```kotlin
class ComponentTest : EntityRelationContext {
    // 测试：组件添加/移除/获取
    // 测试：标签添加/移除/检查
    // 测试：组件替换（copy模式）
    // 测试：批量组件操作
}
```

### 实体生命周期测试 (EntityLifecycleTest)

```kotlin
class EntityLifecycleTest : EntityRelationContext {
    // 测试：实体创建
    // 测试：版本控制
    // 测试：实体激活检查
    // 测试：实体销毁
    // 测试：批量创建
}
```

### 查询系统测试 (QuerySystemTest)

```kotlin
class QuerySystemTest : EntityRelationContext {
    // 测试：基本查询
    // 测试：多组件查询
    // 测试：可选组件
    // 测试：标签过滤
    // 测试：查询缓存
}
```

### 观察者测试 (ObserverTest)

```kotlin
class ObserverTest : EntityRelationContext {
    // 测试：组件添加observer
    // 测试：自定义事件observer
    // 测试：多observer
    // 测试：带数据observer
    // 测试：带query observer
}
```

### 关系测试 (RelationTest)

```kotlin
class RelationTest : EntityRelationContext {
    // 测试：关系创建
    // 测试：关系移除
    // 测试：关系替换
    // 测试：带数据关系
    // 测试：多对多关系
}
```

## 常见陷阱与解决方案

### 陷阱1：忘记注册组件ID

```kotlin
// ❌ 错误
val world = world {}
world.entity { it.addComponent(Position(0, 0)) }  // 运行时错误

// ✅ 正确
val world = world {
    components { world.componentId<Position>() }
}
```

### 陷阱2：在lambda中使用 it 作为索引

```kotlin
// ❌ 错误：it 是 EntityCreateContext，不是索引
repeat(10) {
    world.entity { 
        it.addComponent(Position(it, it * 2))  // it 冲突
    }
}

// ✅ 正确：明确命名参数
repeat(10) { index ->
    world.entity { ctx ->
        ctx.addComponent(Position(index, index * 2))
    }
}
```

### 陷阱3：遍历查询时修改实体

```kotlin
// ❌ 错误：遍历时修改
world.query { TestContext(this) }.forEach { ctx ->
    ctx.entity.editor { it.addComponent(...) }  // 可能异常
}

// ✅ 正确：先收集再修改
val entities = world.query { TestContext(this) }
    .map { it.entity }
    .toList()
entities.forEach { entity ->
    entity.editor { it.addComponent(...) }
}
```

### 陷阱4：混淆 Tag 和 Component

```kotlin
// ❌ 错误
world.entity {
    it.addComponent(ActiveTag)  // Tag 不是 Component
}

// ✅ 正确
world.entity {
    it.addTag<ActiveTag>()  // 使用 addTag
}
```

## 测试执行

```bash
# 运行所有ECS测试
./gradlew :libs:lko-ecs:test

# 运行特定测试类
./gradlew :libs:lko-ecs:test --tests "cn.jzl.ecs.WorldTest"

# 运行特定测试方法
./gradlew :libs:lko-ecs:test --tests "cn.jzl.ecs.WorldTest.testBasicEntityCreation"

# 查看测试报告
cat libs/lko-ecs/build/reports/tests/testDebugUnitTest/index.html
```

## 使用时机

- 新功能开发时创建对应的测试文件
- 重构前确保有充分的测试覆盖
- 调试问题时通过测试复现场景
- 性能优化时建立基准测试
