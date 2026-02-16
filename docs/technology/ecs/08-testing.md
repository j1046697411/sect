# ECS 测试指南

> 使用 TDD 方式开发 ECS 功能，包含单元测试、集成测试和最佳实践。

---

## 1. 测试原则

### 1.1 TDD 流程

```
🔴 红 → 🟢 绿 → 🔵 重构
```

1. **红**：编写失败的测试
2. **绿**：编写最小实现让测试通过
3. **重构**：优化代码，保持测试通过

### 1.2 测试位置

```
libs/lko-ecs/src/commonTest/kotlin/
├── cn/jzl/ecs/
│   ├── ComponentTest.kt
│   ├── EntityTest.kt
│   ├── QueryTest.kt
│   └── component/
│       ├── IntComponentStoreTest.kt
│       └── FloatComponentStoreTest.kt
```

---

## 2. 单元测试

### 2.1 World 测试

```kotlin
class WorldTest {

    private val testAddon = createAddon<Unit>("test") {
        components {
            world.componentId<Position>()
            world.componentId<Velocity>()
        }
    }

    @Test
    fun testEntityCreation() = runTest {
        val world = world {
            install(testAddon)
        }
        
        // Given: 创建实体
        val entity = world.entity {
            it.addComponent(Position(10, 20))
            it.addComponent(Velocity(1, 2))
        }
        
        // When: 查询实体
        val position = entity.getComponent<Position>()
        val velocity = entity.getComponent<Velocity>()
        
        // Then: 验证结果
        assertEquals(Position(10, 20), position)
        assertEquals(Velocity(1, 2), velocity)
    }
}
```

### 2.2 ComponentStore 测试

```kotlin
class IntComponentStoreTest {

    @Test
    fun testSize() {
        val store = IntComponentStore()
        assertEquals(0, store.size)
        
        store.add(10)
        assertEquals(1, store.size)
    }

    @Test
    fun testAddAndGet() {
        val store = IntComponentStore()
        store.add(100)
        store.add(200)
        
        assertEquals(100, store.get(0))
        assertEquals(200, store.get(1))
    }

    @Test
    fun testSet() {
        val store = IntComponentStore()
        store.add(10)
        store.set(0, 999)
        
        assertEquals(999, store.get(0))
    }

    @Test
    fun testRemoveAt() {
        val store = IntComponentStore()
        store.add(10)
        store.add(20)
        store.add(30)
        
        val removed = store.removeAt(1)
        
        assertEquals(20, removed)
        assertEquals(2, store.size)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testSetNegativeIndex() {
        val store = IntComponentStore()
        store.set(-1, 100)
    }
}
```

### 2.3 查询测试

```kotlin
class QueryTest {

    private val testAddon = createAddon<Unit>("test") {
        components {
            world.componentId<Position>()
            world.componentId<Health>()
        }
    }

    @Test
    fun testQueryBasic() = runTest {
        val world = world {
            install(testAddon)
        }
        
        // 创建实体
        world.entity { it.addComponent(Position(0, 0)) }
        world.entity { it.addComponent(Position(10, 10)) }
        world.entity { it.addComponent(Position(20, 20)) }
        
        // 定义查询上下文
        class PositionContext(world: World) : EntityQueryContext(world) {
            val position: Position by component()
        }
        
        // 查询并验证
        val positions = world.query { PositionContext(this) }
            .map { it.position }
            .toList()
        
        assertEquals(3, positions.size)
    }

    @Test
    fun testQueryFilter() = runTest {
        val world = world {
            install(testAddon)
        }
        
        // 创建实体
        world.entity { it.addComponent(Position(0, 0)) }
        world.entity { it.addComponent(Position(10, 10)) }
        world.entity { it.addComponent(Position(20, 20)) }
        
        // 过滤查询
        class PositionContext(world: World) : EntityQueryContext(world) {
            val position: Position by component()
        }
        
        val filtered = world.query { PositionContext(this) }
            .filter { it.position.x > 5 }
            .map { it.position }
            .toList()
        
        assertEquals(2, filtered.size)
    }
}
```

---

## 3. 集成测试

### 3.1 系统测试

```kotlin
class HealthSystemTest : EntityRelationContext {
    
    override lateinit var world: World
    
    private val testAddon = createAddon<Unit>("test") {
        components {
            world {
                world.componentId<Health>()
            }
        }
    }
    
    @BeforeTest
    fun setup() {
        world = world { install(testAddon) }
    }
    
    // 测试目标系统
    private class HealthContext(world: World) : EntityQueryContext(world) {
        val health: Health by component()
    }
    
    @Test
    fun testHeal() {
        // Given: 有受伤的实体
        val entity = world.entity {
            it.addComponent(Health(50, 100))
        }
        
        // When: 执行治疗
        val health = entity.getComponent<Health>()!!
        entity.editor {
            it.addComponent(health.copy(current = health.current + 30))
        }
        
        // Then: 验证治疗效果
        val result = entity.getComponent<Health>()!!
        assertEquals(80, result.current)  // 50 + 30 = 80
    }
    
    @Test
    fun testHealCapAtMax() {
        // Given: 满血实体
        val entity = world.entity {
            it.addComponent(Health(100, 100))
        }
        
        // When: 继续治疗
        val health = entity.getComponent<Health>()!!
        entity.editor {
            it.addComponent(health.copy(current = health.current + 30))
        }
        
        // Then: 不应超过最大值
        val result = entity.getComponent<Health>()!!
        assertEquals(100, result.current)  // capped at 100
    }
}
```

### 3.2 Relation 测试

```kotlin
class RelationTest {

    private val testAddon = createAddon<Unit>("test") {
        components {
            world {
                world.componentId<OwnerBy>()
                world.componentId<Name>()
            }
        }
    }

    @Test
    fun testAddRelation() = runTest {
        val world = world { install(testAddon) }
        
        // Given: 两个实体
        val player = world.entity { it.addComponent(Name("玩家")) }
        val sword = world.entity { it.addComponent(Name("剑")) }
        
        // When: 添加关系
        sword.editor {
            it.addRelation<OwnerBy>(player)
        }
        
        // Then: 关系存在
        val owner = sword.getRelation<OwnerBy, Name>()
        assertEquals(player, owner)
    }
}
```

---

## 4. BDD 风格

### 4.1 Given-When-Then

```kotlin
@Test
fun `given damaged entity when heal then health increases`() {
    // Given: 受伤的实体
    val entity = world.entity {
        it.addComponent(Health(50, 100))
    }
    
    // When: 治疗
    val health = entity.getComponent<Health>()!!
    entity.editor {
        it.addComponent(health.copy(current = health.current + 30))
    }
    
    // Then: 生命值增加
    val result = entity.getComponent<Health>()!!
    assertEquals(80, result.current)
}
```

---

## 5. 测试辅助

### 5.1 createAddon 助手

```kotlin
// 在测试中快速创建 World
private val testAddon = createAddon<Unit>("test") {
    components {
        world {
            world.componentId<Position>()
            world.componentId<Health>()
        }
    }
}

fun World.testWorld() = world {
    install(testAddon)
}

// 使用
@Test
fun testExample() = runTest {
    val world = world.testWorld()
    // ...
}
```

---

## 6. 运行测试

### 6.1 命令

```bash
# 运行所有测试
./gradlew test

# 运行 ECS 模块测试
./gradlew :libs:lko-ecs:test

# 运行特定测试类
./gradlew :libs:lko-ecs:test --tests "cn.jzl.ecs.WorldTest"

# 运行特定测试方法
./gradlew :libs:lko-ecs:test --tests "cn.jzl.ecs.WorldTest.testEntityCreation"

# 生成覆盖率报告
./gradlew allCoverage
```

### 6.2 覆盖率要求

| 模块 | 最低覆盖率 |
|------|-----------|
| lko-ecs | 95%+ |
| lko-core | 90%+ |
| business-* | 80%+ |

---

## 7. 最佳实践

### 7.1 测试命名

```kotlin
// ✅ 清晰描述
@Test
fun `given empty world when create entity then entity exists`()

// ❌ 模糊
@Test
fun testCreate()
```

### 7.2 测试单一职责

```kotlin
// ✅ 正确：一个测试只验证一件事
@Test
fun `given entity when add health then health increases`() {
    // 只测试添加
}

@Test
fun `given entity when remove health then health decreases`() {
    // 只测试移除
}

// ❌ 错误：混合多个场景
@Test
fun testHealth() {
    // 添加...
    // 移除...
    // 上限...
}
```

### 7.3 避免测试依赖

```kotlin
// ❌ 错误：测试依赖执行顺序
@Test
fun testA() { entity.addComponent(...) }
@Test
fun testB() { entity.getComponent(...) }  // 依赖 testA

// ✅ 正确：每个测试独立
@Test
fun `test add and get`() {
    val entity = world.entity { it.addComponent(Health(100, 100)) }
    assertEquals(100, entity.getComponent<Health>()!!.current)
}
```

---

## 8. 下一步

- 快速开始：[00-quick-start.md](00-quick-start.md)
- 常见模式：[02-patterns.md](02-patterns.md)
- 反模式：[03-anti-patterns.md](03-anti-patterns.md)
