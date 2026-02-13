---
description: 强制执行测试驱动开发工作流。首先搭建接口，生成测试，然后实现最小化代码以通过测试。确保 80%+ 覆盖率。
---

# TDD 命令

此命令调用 **tdd-guide** 代理来强制执行测试驱动开发方法。

## 此命令的作用

1. **搭建接口** - 首先定义类型/组件
2. **首先生成测试** - 编写失败的测试（红）
3. **实现最小化代码** - 编写刚好足够的代码以通过测试（绿）
4. **重构** - 改进代码，同时保持测试通过（重构）
5. **验证覆盖率** - 确保 80%+ 的测试覆盖率

## 何时使用

在以下情况下使用 `/tdd`：

* 实现新功能时
* 添加新服务/组件时
* 修复错误时（首先编写重现错误的测试）
* 重构现有代码时
* 构建关键业务逻辑时

## 工作原理

tdd-guide 代理将：

1. 为输入/输出**定义组件/服务接口**
2. **编写将会失败的测试**（因为代码尚不存在）
3. **运行测试**并验证它们因正确的原因而失败
4. **编写最小化实现**以使测试通过
5. **运行测试**并验证它们通过
6. **重构**代码，同时保持测试通过
7. **检查覆盖率**，如果低于 80% 则添加更多测试

## TDD 循环

```
红灯 → 绿灯 → 重构 → 重复

红灯：编写失败的测试
绿灯：编写最小化代码使测试通过
重构：改进代码，保持测试通过
重复：下一个功能/场景
```

## 使用示例

````
用户：/tdd 我需要一个处理实体伤害的服务

代理：
# TDD 会话：伤害处理服务

## 步骤 1：定义接口（搭建）

```kotlin
// HealthService.kt
data class Health(val current: Int, val max: Int)
sealed class DeadTag

class HealthService(private val world: World) : EntityRelationContext {
    override val world: World = world
    
    fun takeDamage(entity: Entity, damage: Int) {
        // TODO: Implementation
        throw NotImplementedError("takeDamage not implemented")
    }
}
````

## 步骤 2：编写失败的测试（红）

```kotlin
// HealthServiceTest.kt
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
        val entity = world.entity {
            it.addComponent(Health(100, 100))
        }
        
        val service = HealthService(world)
        service.takeDamage(entity, 30)
        
        assertEquals(Health(70, 100), entity.getComponent<Health>())
    }
    
    @Test
    fun `takeDamage does not reduce health below zero`() {
        val entity = world.entity {
            it.addComponent(Health(20, 100))
        }
        
        val service = HealthService(world)
        service.takeDamage(entity, 50)
        
        assertEquals(Health(0, 100), entity.getComponent<Health>())
    }
    
    @Test
    fun `takeDamage adds DeadTag when health reaches zero`() {
        val entity = world.entity {
            it.addComponent(Health(10, 100))
        }
        
        val service = HealthService(world)
        service.takeDamage(entity, 10)
        
        assertTrue(entity.hasComponent<DeadTag>())
    }
}
```

## 步骤 3：运行测试 - 验证失败

```bash
./gradlew :libs:lko-ecs:test --tests "HealthServiceTest"

HealthServiceTest > takeDamage reduces health by damage amount FAILED
    NotImplementedError at HealthService.kt:10

3 tests failed, 0 passed
```

✅ 测试按预期失败。准备实施。

## 步骤 4：实现最小化代码（绿）

```kotlin
// HealthService.kt
class HealthService(private val world: World) : EntityRelationContext {
    override val world: World = world
    
    fun takeDamage(entity: Entity, damage: Int) {
        val health = entity.getComponent<Health>()
        val newCurrent = maxOf(0, health.current - damage)
        
        entity.editor {
            it.addComponent(health.copy(current = newCurrent))
        }
        
        if (newCurrent == 0) {
            entity.editor {
                it.addTag<DeadTag>()
            }
        }
    }
}
```

## 步骤 5：运行测试 - 验证通过

```bash
./gradlew :libs:lko-ecs:test --tests "HealthServiceTest"

HealthServiceTest > takeDamage reduces health by damage amount PASSED
HealthServiceTest > takeDamage does not reduce health below zero PASSED
HealthServiceTest > takeDamage adds DeadTag when health reaches zero PASSED

3 tests passed
```

✅ 所有测试通过！

## 步骤 6：重构（改进）

```kotlin
// HealthService.kt - 重构后，更好的可读性
class HealthService(private val world: World) : EntityRelationContext {
    override val world: World = world
    
    companion object {
        private const val MIN_HEALTH = 0
    }
    
    fun takeDamage(entity: Entity, damage: Int) {
        require(damage >= 0) { "Damage cannot be negative" }
        
        val health = entity.getComponent<Health>()
        val newCurrent = (health.current - damage).coerceAtLeast(MIN_HEALTH)
        
        updateHealth(entity, health.copy(current = newCurrent))
        
        if (isDead(newCurrent)) {
            markAsDead(entity)
        }
    }
    
    private fun updateHealth(entity: Entity, health: Health) {
        entity.editor { it.addComponent(health) }
    }
    
    private fun isDead(health: Int): Boolean = health == MIN_HEALTH
    
    private fun markAsDead(entity: Entity) {
        entity.editor { it.addTag<DeadTag>() }
    }
}
```

## 步骤 7：验证测试仍然通过

```bash
./gradlew :libs:lko-ecs:test --tests "HealthServiceTest"

HealthServiceTest > takeDamage reduces health by damage amount PASSED
HealthServiceTest > takeDamage does not reduce health below zero PASSED
HealthServiceTest > takeDamage adds DeadTag when health reaches zero PASSED

3 tests passed
```

✅ 重构完成，测试仍然通过！

## 步骤 8：检查覆盖率

```bash
./gradlew :libs:lko-ecs:koverVerify

Kover: coverage verification passed
Line coverage: 100.00% (threshold: 80.00%)
```

✅ TDD 会话完成！

```

## TDD 最佳实践

**应该：**
- ✅ 首先编写测试，在任何实现之前
- ✅ 运行测试并验证它们在实现前失败
- ✅ 编写最小化代码使测试通过
- ✅ 只有在测试通过后才重构
- ✅ 添加边界情况和错误场景
- ✅ 目标覆盖率 80%+（关键代码 100%）

**不应该：**
- ❌ 在测试之前编写实现
- ❌ 跳过每次更改后运行测试
- ❌ 一次编写太多代码
- ❌ 忽略失败的测试
- ❌ 测试实现细节（应该测试行为）
- ❌ 遍历查询结果时修改实体

## 测试类型

**单元测试**（函数级别）：
- 正常路径场景
- 边界情况（空值、null、最大值）
- 错误条件
- 边界值

**集成测试**（服务级别）：
- 服务交互
- 实体操作
- 查询系统
- 关系处理

## 覆盖率要求

**强制规则：每个包覆盖率 >= 80%，而非总覆盖率**

- 所有包**最低 80%**
- 以下情况**必须 100%**：
  - 核心游戏逻辑
  - 实体生命周期管理
  - 组件操作
  - 查询系统

**验证方式**：
```bash
# 验证每个包的覆盖率
./gradlew :<module>:koverVerify

# 查看详细报告
./gradlew :<module>:koverHtmlReport
open <module>/build/reports/kover/htmlJvm/index.html
```

## 重要说明

**强制要求**：测试必须在实现之前编写。TDD 循环是：

1. **红灯** - 编写失败的测试
2. **绿灯** - 实现以通过测试
3. **重构** - 改进代码

永远不要跳过红灯阶段。永远不要在测试之前编写代码。

## 与其他命令的集成

- 先使用 `/plan` 了解要构建什么
- 使用 `/tdd` 进行测试驱动实现
- 如果出现构建错误，使用 `/build-fix`
- 使用 `/code-review` 审查实现
- 使用 `/verify` 验证整体质量

## 相关代理

此命令调用位于以下位置的 `tdd-guide` 代理：
`.opencode/prompts/agents/tdd-guide.md`

可以参考以下位置的 `tdd-workflow` 技能：
`.opencode/skills/tdd-workflow/`
