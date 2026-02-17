# 组件存储特化优化 - 实现计划

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**目标:** 为数值类型组件提供特化存储，消除装箱开销，提升大量实体数值更新场景的性能。

**架构:** 在 `createAddon` 时，通过 DSL 显式配置组件的存储类型。系统为数值类型组件选择对应的特化存储（如 IntFastList），避免基本类型装箱为对象。

**技术栈:** Kotlin Multiplatform, ECS (lko-ecs), FastList (lko-core)

---

## Task 1: 创建 IntComponentStore

**Files:**
- Create: `libs/lko-ecs/src/commonMain/kotlin/cn/jzl/ecs/component/IntComponentStore.kt`

**Step 1: 创建测试文件**

```kotlin
// libs/lko-ecs/src/commonTest/kotlin/cn/jzl/ecs/component/IntComponentStoreTest.kt
package cn.jzl.ecs.component

import cn.jzl.ecs.addon.createAddon
import cn.jzl.ecs.world.world
import kotlin.test.Test
import kotlin.test.assertEquals

class IntComponentStoreTest {

    private val testAddon = createAddon<Unit>("test") {
        components {
            world {
                world.componentId<Position> { it.store { intStore() } }
            }
        }
    }

    @Test
    fun testIntStoreCanBeConfigured() = world(testAddon) { world ->
        // Verify the component can be registered with int store
        val positionId = world.components.id<Position>()
        assertEquals(true, positionId.id > 0)
    }
}
```

**Step 2: 运行测试确认失败**

```bash
./gradlew :libs:lko-ecs:test --tests "cn.jzl.ecs.component.IntComponentStoreTest" 2>&1 | head -50
```

Expected: FAIL - `intStore()` is not defined

**Step 3: 创建 IntComponentStore**

```kotlin
// libs/lko-ecs/src/commonMain/kotlin/cn/jzl/ecs/component/IntComponentStore.kt
package cn.jzl.ecs.component

import cn.jzl.core.list.IntFastList
import kotlin.jvm.JvmInline

@JvmInline
value class IntComponentStore(
    private val components: IntFastList = IntFastList()
) : ComponentStore<Int> {
    override val size: Int get() = components.size

    override fun get(index: Int): Int = components[index]

    override fun set(index: Int, value: Int) {
        components.ensureCapacity(index + 1)
        components[index] = value
    }

    override fun add(value: Int) = components.insertLast(value)

    override fun removeAt(index: Int): Int = components.removeAt(index)
}
```

**Step 4: 创建 ComponentStores.kt 工厂函数**

```kotlin
// libs/lko-ecs/src/commonMain/kotlin/cn/jzl/ecs/component/ComponentStores.kt
package cn.jzl.ecs.component

fun intStore(): ComponentStore<Any> = IntComponentStore()
fun floatStore(): ComponentStore<Any> = FloatComponentStore()
fun longStore(): ComponentStore<Any> = LongComponentStore()
fun doubleStore(): ComponentStore<Any> = DoubleComponentStore()
fun objectStore(): ComponentStore<Any> = GeneralComponentStore()
```

**Step 5: 运行测试确认通过**

```bash
./gradlew :libs:lko-ecs:test --tests "cn.jzl.ecs.component.IntComponentStoreTest" 2>&1 | tail -20
```

Expected: PASS

**Step 6: 提交**

```bash
git add libs/lko-ecs/src/commonMain/kotlin/cn/jzl/ecs/component/IntComponentStore.kt
git add libs/lko-ecs/src/commonMain/kotlin/cn/jzl/ecs/component/ComponentStores.kt
git add libs/lko-ecs/src/commonTest/kotlin/cn/jzl/ecs/component/IntComponentStoreTest.kt
git commit -m "feat(ecs): 添加 IntComponentStore 和 intStore 工厂函数"
```

---

## Task 2: 创建 FloatComponentStore

**Files:**
- Create: `libs/lko-ecs/src/commonMain/kotlin/cn/jzl/ecs/component/FloatComponentStore.kt`
- Test: `libs/lko-ecs/src/commonTest/kotlin/cn/jzl/ecs/component/FloatComponentStoreTest.kt`

**Step 1: 创建测试文件**

```kotlin
// libs/lko-ecs/src/commonTest/kotlin/cn/jzl/ecs/component/FloatComponentStoreTest.kt
package cn.jzl.ecs.component

import cn.jzl.ecs.addon.createAddon
import cn.jzl.ecs.world.world
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FloatComponentStoreTest {

    private val testAddon = createAddon<Unit>("test") {
        components {
            world {
                world.componentId<Health> { it.store { floatStore() } }
            }
        }
    }

    @Test
    fun testFloatStoreCanBeConfigured() = world(testAddon) { world ->
        val healthId = world.components.id<Health>()
        assertTrue(healthId.id > 0)
    }
}
```

**Step 2: 运行测试确认失败**

```bash
./gradlew :libs:lko-ecs:test --tests "cn.jzl.ecs.component.FloatComponentStoreTest" 2>&1 | head -30
```

Expected: FAIL - `FloatComponentStore` is not defined

**Step 3: 创建 FloatComponentStore**

```kotlin
// libs/lko-ecs/src/commonMain/kotlin/cn/jzl/ecs/component/FloatComponentStore.kt
package cn.jzl.ecs.component

import cn.jzl.core.list.FloatFastList
import kotlin.jvm.JvmInline

@JvmInline
value class FloatComponentStore(
    private val components: FloatFastList = FloatFastList()
) : ComponentStore<Float> {
    override val size: Int get() = components.size

    override fun get(index: Int): Float = components[index]

    override fun set(index: Int, value: Float) {
        components.ensureCapacity(index + 1)
        components[index] = value
    }

    override fun add(value: Float) = components.insertLast(value)

    override fun removeAt(index: Int): Float = components.removeAt(index)
}
```

**Step 4: 运行测试确认通过**

```bash
./gradlew :libs:lko-ecs:test --tests "cn.jzl.ecs.component.FloatComponentStoreTest" 2>&1 | tail -10
```

Expected: PASS

**Step 5: 提交**

```bash
git add libs/lko-ecs/src/commonMain/kotlin/cn/jzl/ecs/component/FloatComponentStore.kt
git add libs/lko-ecs/src/commonTest/kotlin/cn/jzl/ecs/component/FloatComponentStoreTest.kt
git commit -m "feat(ecs): 添加 FloatComponentStore 和 floatStore 工厂函数"
```

---

## Task 3: 创建 LongComponentStore 和 DoubleComponentStore

**Files:**
- Create: `libs/lko-ecs/src/commonMain/kotlin/cn/jzl/ecs/component/LongComponentStore.kt`
- Create: `libs/lko-ecs/src/commonMain/kotlin/cn/jzl/ecs/component/DoubleComponentStore.kt`

**Step 1: 创建 LongComponentStore**

```kotlin
// libs/lko-ecs/src/commonMain/kotlin/cn/jzl/ecs/component/LongComponentStore.kt
package cn.jzl.ecs.component

import cn.jzl.core.list.LongFastList
import kotlin.jvm.JvmInline

@JvmInline
value class LongComponentStore(
    private val components: LongFastList = LongFastList()
) : ComponentStore<Long> {
    override val size: Int get() = components.size

    override fun get(index: Int): Long = components[index]

    override fun set(index: Int, value: Long) {
        components.ensureCapacity(index + 1)
        components[index] = value
    }

    override fun add(value: Long) = components.insertLast(value)

    override fun removeAt(index: Int): Long = components.removeAt(index)
}
```

**Step 2: 创建 DoubleComponentStore**

```kotlin
// libs/lko-ecs/src/commonMain/kotlin/cn/jzl/ecs/component/DoubleComponentStore.kt
package cn.jzl.ecs.component

import cn.jzl.core.list.DoubleFastList
import kotlin.jvm.JvmInline

@JvmInline
value class DoubleComponentStore(
    private val components: DoubleFastList = DoubleFastList()
) : ComponentStore<Double> {
    override val size: Int get() = components.size

    override fun get(index: Int): Double = components[index]

    override fun set(index: Int, value: Double) {
        components.ensureCapacity(index + 1)
        components[index] = value
    }

    override fun add(value: Double) = components.insertLast(value)

    override fun removeAt(index: Int): Double = components.removeAt(index)
}
```

**Step 3: 提交**

```bash
git add libs/lko-ecs/src/commonMain/kotlin/cn/jzl/ecs/component/LongComponentStore.kt
git add libs/lko-ecs/src/commonMain/kotlin/cn/jzl/ecs/component/DoubleComponentStore.kt
git commit -m "feat(ecs): 添加 LongComponentStore 和 DoubleComponentStore"
```

---

## Task 4: 添加 store() DSL

**Files:**
- Modify: `libs/lko-ecs/src/commonMain/kotlin/cn/jzl/ecs/component/ComponentConfigureContext.kt`

**Step 1: 添加 store() 函数到 ComponentConfigureContext**

```kotlin
// 在 ComponentConfigureContext.kt 文件末尾添加

context(context: ComponentConfigureContext)
fun ComponentId.store(factory: () -> ComponentStore<Any>): Unit = with(context) {
    world.componentService.configureStoreType(this@store, factory)
}
```

**Step 2: 添加 configureStoreType 方法到 ComponentService**

```kotlin
// 在 ComponentService.kt 中添加

fun configureStoreType(componentId: ComponentId, factory: () -> ComponentStore<Any>) {
    // Store the factory for later use when creating component storage
    // This requires adding a new field to store custom factories
}
```

Wait, the ComponentService needs to store the factory. Let me check the current implementation again.

**Step 2 (revised): 查看 ComponentService 当前实现**

```kotlin
// 查看 ComponentService.kt
// 当前 componentStoreFactories 是 private val
// 需要添加一个 MutableMap 来存储用户配置的工厂
```

**Step 3: 修改 ComponentService 添加用户配置存储**

```kotlin
// 在 ComponentService.kt 中添加
private val userConfiguredFactories = mutableMapOf<Int, () -> ComponentStore<Any>>()

// 修改 create 方法
override fun create(relation: Relation): ComponentStore<Any> {
    // Check user configured factory first
    val userFactory = userConfiguredFactories[relation.kind.data]
    if (userFactory != null) {
        return userFactory()
    }
    
    val factory = componentStoreFactories.getOrPut(relation.kind.data) { ComponentStoreFactory.Companion }
    @Suppress("UNCHECKED_CAST")
    return factory.create(relation) as ComponentStore<Any>
}

// 添加新方法
fun configureStoreType(componentId: ComponentId, factory: () -> ComponentStore<Any>) {
    userConfiguredFactories[componentId.data] = factory
}
```

**Step 4: 运行现有测试确保没有破坏**

```bash
./gradlew :libs:lko-ecs:test 2>&1 | tail -20
```

Expected: All tests pass

**Step 5: 提交**

```bash
git add libs/lko-ecs/src/commonMain/kotlin/cn/jzl/ecs/component/ComponentConfigureContext.kt
git add libs/lko-ecs/src/commonMain/kotlin/cn/jzl/ecs/component/ComponentService.kt
git commit -m "feat(ecs): 添加 store() DSL 和 ComponentService 存储类型配置支持"
```

---

## Task 5: 集成测试

**Files:**
- Create: `libs/lko-ecs/src/commonTest/kotlin/cn/jzl/ecs/component/ComponentStoreIntegrationTest.kt`

**Step 1: 创建集成测试**

```kotlin
package cn.jzl.ecs.component

import cn.jzl.ecs.addon.createAddon
import cn.jzl.ecs.entity.entity
import cn.jzl.ecs.query.forEach
import cn.jzl.ecs.world.world
import kotlin.test.Test
import kotlin.test.assertEquals

data class TestLevel(val value: Int)
data class TestHealth(val current: Float, val max: Float)
data class TestExperience(val value: Long)

class ComponentStoreIntegrationTest {

    private val testAddon = createAddon<Unit>("test") {
        components {
            world {
                world.componentId<TestLevel> { it.store { intStore() } }
                world.componentId<TestHealth> { it.store { floatStore() } }
                world.componentId<TestExperience> { it.store { longStore() } }
            }
        }
    }

    @Test
    fun testEntitiesWithNumericStores() = world(testAddon) { world ->
        // Create entities with numeric components
        repeat(1000) {
            world.entity {
                it.addComponent(TestLevel(it))
                it.addComponent(TestHealth(it.toFloat(), 100f))
                it.addComponent(TestExperience(it.toLong() * 1000))
            }
        }

        // Query and verify
        var count = 0
        world.query { 
            context { entity, _ -> 
                val level = context.get<TestLevel>()
                val health = context.get<TestHealth>()
                val exp = context.get<TestExperience>()
                count++
            }
        }

        assertEquals(1000, count)
    }
}
```

**Step 2: 运行集成测试**

```bash
./gradlew :libs:lko-ecs:test --tests "cn.jzl.ecs.component.ComponentStoreIntegrationTest" 2>&1 | tail -20
```

Expected: PASS

**Step 3: 提交**

```bash
git add libs/lko-ecs/src/commonTest/kotlin/cn/jzl/ecs/component/ComponentStoreIntegrationTest.kt
git commit -m "test(ecs): 添加组件存储特化集成测试"
```

---

## Task 6: 业务模块集成（可选）

**Files:**
- Modify: `business-modules/business-core/` 中的组件注册

**Step 1: 在业务模块中应用优化（可选）**

根据实际需求，选择业务组件进行存储类型配置。

**Step 2: 提交**

```bash
git add business-modules/
git commit -m "feat(business): 应用组件存储特化优化"
```

---

## 完成检查

- [ ] 所有单元测试通过
- [ ] 集成测试通过
- [ ] 代码覆盖率达标 (>80% core logic)
- [ ] 无 Lint 警告

---

## 执行方式

**Plan complete and saved to `docs/plans/2026-02-16-component-store-optimization-design.md` and `docs/plans/2026-02-16-component-store-optimization.md`.**

**Two execution options:**

1. **Subagent-Driven (this session)** - I dispatch fresh subagent per task, review between tasks, fast iteration

2. **Parallel Session (separate)** - Open new session with executing-plans, batch execution with checkpoints

**Which approach?**
