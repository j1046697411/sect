---
name: tdd-workflow
description: 在编写新功能、修复错误或重构代码时使用此技能。强制执行测试驱动开发，包含单元测试和集成测试，覆盖率超过80%。
---

# 测试驱动开发工作流

此技能确保所有代码开发遵循TDD原则，并具备全面的测试覆盖率。

## 何时激活

* 编写新功能或功能
* 修复错误或问题
* 重构现有代码
* 添加新服务
* 创建新组件

## 核心原则

### 1. 测试优先于代码

始终先编写测试，然后实现代码以使测试通过。

### 2. 覆盖率要求

* 最低80%覆盖率（单元 + 集成）
* 覆盖所有边缘情况
* 测试错误场景
* 验证边界条件

### 3. 测试类型

#### 单元测试

* 单个函数和工具
* 组件逻辑
* 纯函数
* 辅助函数和工具

#### 集成测试

* 服务交互
* 实体操作
* 查询系统
* 关系处理

## TDD 工作流步骤

### 步骤 1: 编写用户旅程

```
作为 [角色]，我想要 [操作]，以便 [收益]

示例：
作为游戏开发者，我想要创建带有健康组件的实体，
以便在游戏中跟踪实体的生命值。
```

### 步骤 2: 生成测试用例

针对每个用户旅程，创建全面的测试用例：

```kotlin
class HealthServiceTest : EntityRelationContext {
    override val world: World by lazy {
        world { install(testAddon) }
    }
    
    private val testAddon = createAddon<Unit>("testAddon") {
        components {
            world.componentId<Health>()
            world.componentId<DeadTag>() { it.tag() }
        }
    }
    
    @Test
    fun `takeDamage reduces health by damage amount`() {
        // 测试实现
    }
    
    @Test
    fun `takeDamage handles zero health gracefully`() {
        // 测试边界情况
    }
    
    @Test
    fun `takeDamage sets DeadTag when health reaches zero`() {
        // 测试状态变化
    }
    
    @Test
    fun `takeDamage does not reduce health below zero`() {
        // 测试边界条件
    }
}
```

### 步骤 3: 运行测试（它们应该失败）

```bash
./gradlew :<module>:test --tests "<package>.<TestClassName>"
# 测试应该失败 - 我们还没有实现
```

### 步骤 4: 实现代码

编写最少的代码以使测试通过：

```kotlin
// 由测试指导的实现
class HealthService(private val world: World) : EntityRelationContext {
    override val world: World = world
    
    fun takeDamage(entity: Entity, damage: Int) {
        // 实现代码
    }
}
```

### 步骤 5: 再次运行测试

```bash
./gradlew :<module>:test --tests "<package>.<TestClassName>"
# 测试现在应该通过
```

### 步骤 6: 重构

在保持测试通过的同时提高代码质量：

* 消除重复
* 改进命名
* 优化性能
* 增强可读性

### 步骤 7: 验证覆盖率

```bash
./gradlew :<module>:koverVerify
# 验证达到 80%+ 覆盖率
```

## 测试模式

### 单元测试模式

```kotlin
class ComponentTest : EntityRelationContext {
    override val world: World by lazy {
        world { install(testAddon) }
    }
    
    private val testAddon = createAddon<Unit>("testAddon") {
        components {
            world.componentId<TestPosition>()
            world.componentId<TestActiveTag>() { it.tag() }
        }
    }
    
    @Test
    fun `addComponent stores component correctly`() {
        val entity = world.entity {
            it.addComponent(TestPosition(10, 20))
        }
        
        assertEquals(TestPosition(10, 20), entity.getComponent<TestPosition>())
    }
    
    @Test
    fun `addTag adds tag to entity`() {
        val entity = world.entity {
            it.addTag<TestActiveTag>()
        }
        
        assertTrue(entity.hasComponent<TestActiveTag>())
    }
    
    @Test
    fun `editor updates component correctly`() {
        val entity = world.entity {
            it.addComponent(TestPosition(10, 20))
        }
        
        entity.editor {
            it.addComponent(TestPosition(30, 40))
        }
        
        assertEquals(TestPosition(30, 40), entity.getComponent<TestPosition>())
    }
}
```

### 服务集成测试模式

```kotlin
class HealthServiceTest : EntityRelationContext {
    override val world: World by lazy {
        world { install(testAddon) }
    }
    
    private val testAddon = createAddon<Unit>("testAddon") {
        components {
            world.componentId<Health>()
            world.componentId<DeadTag>() { it.tag() }
        }
    }
    
    @Test
    fun `takeDamage reduces health successfully`() {
        val entity = world.entity {
            it.addComponent(Health(100, 100))
        }
        
        val service = HealthService(world)
        service.takeDamage(entity, 30)
        
        assertEquals(Health(70, 100), entity.getComponent<Health>())
    }
    
    @Test
    fun `takeDamage validates damage parameter`() {
        val entity = world.entity {
            it.addComponent(Health(100, 100))
        }
        
        val service = HealthService(world)
        
        assertFails<IllegalArgumentException> {
            service.takeDamage(entity, -10)
        }
    }
    
    @Test
    fun `takeDamage handles entity without health gracefully`() {
        val entity = world.entity {}
        
        val service = HealthService(world)
        
        assertFails<NoSuchElementException> {
            service.takeDamage(entity, 10)
        }
    }
}
```

### 查询测试模式

```kotlin
class QueryTest : EntityRelationContext {
    override val world: World by lazy {
        world { install(testAddon) }
    }
    
    private val testAddon = createAddon<Unit>("testAddon") {
        components {
            world.componentId<TestPosition>()
            world.componentId<TestName>()
        }
    }
    
    @Test
    fun `query returns all matching entities`() {
        world.entity { it.addComponent(TestPosition(1, 1)) }
        world.entity { it.addComponent(TestPosition(2, 2)) }
        world.entity { it.addComponent(TestName("Test")) }
        
        val query = world.query { PositionContext(this) }
        var count = 0
        query.collect { count++ }
        
        assertEquals(2, count)
    }
    
    @Test
    fun `query filters by condition correctly`() {
        world.entity { it.addComponent(TestPosition(10, 10)) }
        world.entity { it.addComponent(TestPosition(1, 1)) }
        
        val query = world.query { PositionContext(this) }
            .filter { it.pos.x > 5 }
        
        var count = 0
        query.collect { count++ }
        
        assertEquals(1, count)
    }
}

class PositionContext(world: World) : EntityQueryContext(world) {
    val pos: TestPosition by component()
}
```

## 测试文件组织

```
libs/lko-ecs/src/
├── commonMain/
│   └── kotlin/
│       └── cn/jzl/ecs/
│           ├── World.kt
│           ├── Entity.kt
│           └── Component.kt
└── commonTest/
    └── kotlin/
        └── cn/jzl/ecs/
            ├── WorldTest.kt           # World 单元测试
            ├── EntityTest.kt          # Entity 单元测试
            ├── ComponentTest.kt       # Component 单元测试
            ├── QueryTest.kt           # 查询集成测试
            └── RelationTest.kt        # 关系集成测试
```

## 测试数据类命名规范

使用模块前缀避免冲突：

```kotlin
// 在 ComponentTest 中
data class CompPosition(val x: Int, val y: Int)
sealed class CompActiveTag

// 在 QueryTest 中
data class QueryPosition(val x: Int, val y: Int)
data class QueryName(val value: String)

// 在 EntityLifecycleTest 中
@JvmInline value class LifecycleName(val value: String)
sealed class LifecycleOwner
```

## 测试覆盖率验证

### 运行覆盖率报告

```bash
# 生成 HTML 报告
./gradlew :<module>:koverHtmlReport

# 验证覆盖率阈值
./gradlew :<module>:koverVerify

# 运行所有模块
./gradlew allCoverage
```

### 覆盖率阈值

```kotlin
// build.gradle.kts
kover {
    coverageEngine = kover.api.CoverageEngine.INTELLIJ
    xmlReport {
        onCheck = true
    }
    htmlReport {
        onCheck = true
    }
    verify {
        onCheck = true
        rule {
            bound {
                minValue = 80
                metric = kover.api.VerificationMetric.LINE
            }
        }
    }
}
```

## 应避免的常见测试错误

### ❌ 错误：测试实现细节

```kotlin
// 不要测试内部状态
assertEquals(5, entity.internalState.count)
```

### ✅ 正确：测试可观察的行为

```kotlin
// 测试用户可见的结果
assertEquals(Health(50, 100), entity.getComponent<Health>())
```

### ❌ 错误：遍历时修改实体

```kotlin
// 会导致异常
world.query { Context(this) }.forEach {
    it.entity.editor { ... }  // ConcurrentModificationException!
}
```

### ✅ 正确：先收集再修改

```kotlin
// 正确的方式
val entities = world.query { Context(this) }
    .map { it.entity }
    .toList()
entities.forEach { it.editor { ... } }
```

### ❌ 错误：没有测试隔离

```kotlin
// 测试相互依赖
@Test
fun `creates entity`() { /* ... */ }

@Test
fun `updates same entity`() { /* 依赖前一个测试 */ }
```

### ✅ 正确：独立的测试

```kotlin
// 每个测试设置自己的数据
@Test
fun `creates entity`() {
    val entity = createTestEntity()
    // 测试逻辑
}

@Test
fun `updates entity`() {
    val entity = createTestEntity()
    // 更新逻辑
}
```

## 持续测试

### 开发期间的监视模式

```bash
./gradlew :<module>:test --continuous
# 文件更改时自动运行测试
```

### 预提交检查

```bash
# 每次提交前运行
./gradlew build
```

### CI/CD 集成

```yaml
# GitHub Actions
- name: Run Tests
  run: ./gradlew test
- name: Verify Coverage
  run: ./gradlew koverVerify
```

## 最佳实践

1. **先写测试** - 始终遵循TDD
2. **每个测试一个断言** - 专注于单一行为
3. **描述性的测试名称** - 解释测试内容
4. **组织-执行-断言** - 清晰的测试结构
5. **测试边缘情况** - Null、空、边界值、大量数据
6. **测试错误路径** - 不仅仅是正常路径
7. **保持测试快速** - 单元测试每个 < 50ms
8. **测试后清理** - 无副作用
9. **审查覆盖率报告** - 识别空白
10. **使用有意义的测试数据** - 避免魔法数字

## 成功指标

* 达到 80%+ 代码覆盖率
* 所有测试通过（绿色）
* 没有跳过或禁用的测试
* 快速测试执行（单元测试 < 30秒）
* 集成测试覆盖关键服务流程
* 测试在生产前捕获错误

***

**记住**：测试不是可选的。它们是安全网，能够实现自信的重构、快速的开发和生产的可靠性。
