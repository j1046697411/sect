# cultivation 模块突破事件化重构 实现计划

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 将 cultivation 模块的突破事件改为使用 ECS 观察者系统发布/订阅模式。

**Architecture:** 定义两个独立事件类（成功/失败），修改 CultivationService 使用 `world.emit()` 发送事件，移除返回值。

**Tech Stack:** Kotlin, ECS 框架观察者系统, TDD

---

## Task 1: 创建突破成功事件类

**Files:**
- Create: `business-modules/business-cultivation/src/commonMain/kotlin/cn/jzl/sect/cultivation/events/BreakthroughSuccessEvent.kt`

**Step 1: 创建事件类文件**

```kotlin
package cn.jzl.sect.cultivation.events

import cn.jzl.sect.core.cultivation.Realm

data class BreakthroughSuccessEvent(
    val oldRealm: Realm,
    val oldLayer: Int,
    val newRealm: Realm,
    val newLayer: Int
)
```

**Step 2: 提交**

```bash
git add business-modules/business-cultivation/src/commonMain/kotlin/cn/jzl/sect/cultivation/events/BreakthroughSuccessEvent.kt
git commit -m "$(cat <<'EOF'
feat: 添加 BreakthroughSuccessEvent 事件类

## 做了什么
- 创建突破成功事件类
- 包含境界和层次变化数据

## 遇到的问题
- 无

## 解决方案
- 纯数据类，不包含实体引用
EOF
)"
```

---

## Task 2: 创建突破失败事件类

**Files:**
- Create: `business-modules/business-cultivation/src/commonMain/kotlin/cn/jzl/sect/cultivation/events/BreakthroughFailedEvent.kt`

**Step 1: 创建事件类文件**

```kotlin
package cn.jzl.sect.cultivation.events

import cn.jzl.sect.core.cultivation.Realm

data class BreakthroughFailedEvent(
    val currentRealm: Realm,
    val currentLayer: Int,
    val attemptedRealm: Realm,
    val attemptedLayer: Int
)
```

**Step 2: 提交**

```bash
git add business-modules/business-cultivation/src/commonMain/kotlin/cn/jzl/sect/cultivation/events/BreakthroughFailedEvent.kt
git commit -m "$(cat <<'EOF'
feat: 添加 BreakthroughFailedEvent 事件类

## 做了什么
- 创建突破失败事件类
- 包含当前境界和尝试突破的境界数据

## 遇到的问题
- 无

## 解决方案
- 纯数据类，不包含实体引用
EOF
)"
```

---

## Task 3: 编写突破事件测试用例

**Files:**
- Create: `business-modules/business-cultivation/src/commonTest/kotlin/cn/jzl/sect/cultivation/events/BreakthroughEventTest.kt`

**Step 1: 编写测试用例**

```kotlin
package cn.jzl.sect.cultivation.events

import cn.jzl.ecs.World
import cn.jzl.ecs.addon.createAddon
import cn.jzl.ecs.component.componentId
import cn.jzl.ecs.entity.EntityRelationContext
import cn.jzl.ecs.observer.observeWithData
import cn.jzl.ecs.world
import cn.jzl.sect.core.config.GameConfig
import cn.jzl.sect.core.cultivation.Realm
import cn.jzl.sect.core.sect.SectPositionInfo
import cn.jzl.sect.core.sect.SectPositionType
import cn.jzl.sect.core.vitality.Spirit
import cn.jzl.sect.core.vitality.Vitality
import cn.jzl.sect.cultivation.components.CultivationProgress
import cn.jzl.sect.cultivation.components.Talent
import cn.jzl.sect.cultivation.services.CultivationService
import kotlin.test.*

class BreakthroughEventTest : EntityRelationContext {
    
    override lateinit var world: World
    private lateinit var cultivationService: CultivationService
    
    @BeforeTest
    fun setup() {
        world = world {
            install(testCultivationAddon)
        }
        cultivationService = CultivationService(world)
    }
    
    private val testCultivationAddon = createAddon("testCultivation") {
        components {
            world.componentId<CultivationProgress>()
            world.componentId<Talent>()
            world.componentId<SectPositionInfo>()
            world.componentId<Vitality>()
            world.componentId<Spirit>()
        }
    }
    
    @Test
    fun testBreakthroughSuccessEventIsEmitted() {
        var receivedEvent: BreakthroughSuccessEvent? = null
        var receivedEntity: cn.jzl.ecs.entity.Entity? = null
        
        world.observeWithData<BreakthroughSuccessEvent>().exec {
            receivedEvent = this.event
            receivedEntity = this.entity
        }
        
        val entity = world.entity {
            it.addComponent(CultivationProgress(
                realm = Realm.MORTAL,
                layer = GameConfig.cultivation.maxLayerPerRealm,
                cultivation = GameConfig.cultivation.getMaxCultivation(Realm.MORTAL, GameConfig.cultivation.maxLayerPerRealm) - 1,
                maxCultivation = GameConfig.cultivation.getMaxCultivation(Realm.MORTAL, GameConfig.cultivation.maxLayerPerRealm)
            ))
            it.addComponent(Talent(comprehension = 100, physique = 100, fortune = 100))
            it.addComponent(SectPositionInfo(SectPositionType.DISCIPLE_OUTER))
            it.addComponent(Vitality(100, 100))
            it.addComponent(Spirit(100, 100))
        }
        
        cultivationService.update(1000)
        
        assertNotNull(receivedEvent, "BreakthroughSuccessEvent should be emitted")
        assertEquals(entity, receivedEntity, "Event should contain correct entity")
        assertEquals(Realm.MORTAL, receivedEvent!!.oldRealm)
        assertEquals(Realm.QI_REFINING, receivedEvent!!.newRealm)
    }
    
    @Test
    fun testBreakthroughFailedEventIsEmittedOnFailure() {
        var failedEventReceived = false
        
        world.observeWithData<BreakthroughFailedEvent>().exec {
            failedEventReceived = true
        }
        
        world.entity {
            it.addComponent(CultivationProgress(
                realm = Realm.MORTAL,
                layer = GameConfig.cultivation.maxLayerPerRealm,
                cultivation = GameConfig.cultivation.getMaxCultivation(Realm.MORTAL, GameConfig.cultivation.maxLayerPerRealm) - 1,
                maxCultivation = GameConfig.cultivation.getMaxCultivation(Realm.MORTAL, GameConfig.cultivation.maxLayerPerRealm)
            ))
            it.addComponent(Talent(comprehension = 1, physique = 1, fortune = 1))
            it.addComponent(SectPositionInfo(SectPositionType.DISCIPLE_OUTER))
            it.addComponent(Vitality(100, 100))
            it.addComponent(Spirit(100, 100))
        }
        
        cultivationService.update(1000)
        
        assertTrue(failedEventReceived, "BreakthroughFailedEvent should be emitted on failure")
    }
}
```

**Step 2: 运行测试确认失败**

Run: `./gradlew :business-modules:business-cultivation:test --tests "cn.jzl.sect.cultivation.events.BreakthroughEventTest"`
Expected: FAIL - 事件未被发送

**Step 3: 提交测试用例**

```bash
git add business-modules/business-cultivation/src/commonTest/kotlin/cn/jzl/sect/cultivation/events/BreakthroughEventTest.kt
git commit -m "$(cat <<'EOF'
test: 添加突破事件测试用例

## 做了什么
- 添加突破成功事件测试
- 添加突破失败事件测试

## 遇到的问题
- 无

## 解决方案
- 使用 TDD 模式，先编写失败测试
EOF
)"
```

---

## Task 4: 修改 CultivationService 发送事件

**Files:**
- Modify: `business-modules/business-cultivation/src/commonMain/kotlin/cn/jzl/sect/cultivation/services/CultivationService.kt`

**Step 1: 添加事件导入**

在文件顶部添加：
```kotlin
import cn.jzl.ecs.observer.emit
import cn.jzl.sect.cultivation.events.BreakthroughSuccessEvent
import cn.jzl.sect.cultivation.events.BreakthroughFailedEvent
```

**Step 2: 修改 update 方法签名**

```kotlin
fun update(hours: Int) {
    // 移除返回值
```

**Step 3: 修改突破成功逻辑**

在突破成功分支（约 91-103 行），将：
```kotlin
breakthroughs.add(
    BreakthroughEvent(
        entity = ctx.entity,
        oldRealm = cult.realm,
        oldLayer = cult.layer,
        newRealm = newRealm,
        newLayer = newLayer,
        position = pos.position
    )
)
```

改为：
```kotlin
world.emit(ctx.entity, BreakthroughSuccessEvent(
    oldRealm = cult.realm,
    oldLayer = cult.layer,
    newRealm = newRealm,
    newLayer = newLayer
))
log.info { "弟子突破成功！${cult.realm.displayName}${cult.layer}层 → ${newRealm.displayName}${newLayer}层" }
```

**Step 4: 添加突破失败事件发送**

在突破失败分支（约 85-88 行），添加：
```kotlin
} else {
    // 突破失败，发送失败事件
    val (attemptedRealm, attemptedLayer) = getNextRealmLayer(cult.realm, cult.layer)
    world.emit(ctx.entity, BreakthroughFailedEvent(
        currentRealm = cult.realm,
        currentLayer = cult.layer,
        attemptedRealm = attemptedRealm,
        attemptedLayer = attemptedLayer
    ))
    log.info { "弟子突破失败！${cult.realm.displayName}${cult.layer}层 → ${attemptedRealm.displayName}${attemptedLayer}层" }
    // 修为保留在瓶颈
    newCultivation = cult.maxCultivation - 1
    break
}
```

**Step 5: 移除 breakthroughs 列表和返回**

移除：
```kotlin
val breakthroughs = mutableListOf<BreakthroughEvent>()
```
和最后的：
```kotlin
return breakthroughs
```

**Step 6: 移除内部 BreakthroughEvent 类**

移除约 243-256 行的内部类定义。

**Step 7: 运行测试确认通过**

Run: `./gradlew :business-modules:business-cultivation:test --tests "cn.jzl.sect.cultivation.events.BreakthroughEventTest"`
Expected: PASS

**Step 8: 运行全量测试**

Run: `./gradlew :business-modules:business-cultivation:test`
Expected: PASS

**Step 9: 提交**

```bash
git add business-modules/business-cultivation/src/commonMain/kotlin/cn/jzl/sect/cultivation/services/CultivationService.kt
git commit -m "$(cat <<'EOF'
refactor: 修改 CultivationService 使用事件发送

## 做了什么
- 修改 update() 方法移除返回值
- 突破成功时发送 BreakthroughSuccessEvent
- 突破失败时发送 BreakthroughFailedEvent
- 移除内部 BreakthroughEvent 类

## 遇到的问题
- 无

## 解决方案
- 使用 ECS 观察者系统实现事件驱动
EOF
)"
```

---

## Task 5: 更新 CultivationAddon 注册事件类型

**Files:**
- Modify: `business-modules/business-cultivation/src/commonMain/kotlin/cn/jzl/sect/cultivation/CultivationAddon.kt`

**Step 1: 添加事件类型注册**

在 components 块中添加：
```kotlin
import cn.jzl.sect.cultivation.events.BreakthroughSuccessEvent
import cn.jzl.sect.cultivation.events.BreakthroughFailedEvent

// 在 components 块中
world.componentId<BreakthroughSuccessEvent>()
world.componentId<BreakthroughFailedEvent>()
```

**Step 2: 运行测试确认通过**

Run: `./gradlew :business-modules:business-cultivation:test`
Expected: PASS

**Step 3: 提交**

```bash
git add business-modules/business-cultivation/src/commonMain/kotlin/cn/jzl/sect/cultivation/CultivationAddon.kt
git commit -m "$(cat <<'EOF'
feat: 在 CultivationAddon 中注册事件类型

## 做了什么
- 注册 BreakthroughSuccessEvent 类型
- 注册 BreakthroughFailedEvent 类型

## 遇到的问题
- 无

## 解决方案
- 确保事件类型在 World 中可用
EOF
)"
```

---

## Task 6: 更新现有测试用例

**Files:**
- Modify: `business-modules/business-cultivation/src/commonTest/kotlin/cn/jzl/sect/cultivation/systems/CultivationSystemTest.kt`

**Step 1: 检查测试是否依赖返回值**

如果测试中有类似：
```kotlin
val breakthroughs = cultivationService.update(hours)
```

改为：
```kotlin
cultivationService.update(hours)
// 通过其他方式验证突破结果
```

**Step 2: 运行测试确认通过**

Run: `./gradlew :business-modules:business-cultivation:test`
Expected: PASS

**Step 3: 提交**

```bash
git add business-modules/business-cultivation/src/commonTest/kotlin/cn/jzl/sect/cultivation/
git commit -m "$(cat <<'EOF'
test: 更新测试用例适配事件机制

## 做了什么
- 移除对 update() 返回值的依赖
- 使用事件订阅验证突破结果

## 遇到的问题
- 无

## 解决方案
- 使用事件驱动测试方式
EOF
)"
```

---

## Task 7: 更新文档

**Files:**
- Modify: `business-modules/business-cultivation/AGENTS.md`

**Step 1: 更新使用示例**

将：
```kotlin
val breakthroughs = cultivationService.update(hours)
```

改为：
```kotlin
cultivationService.update(hours)
// 突破事件通过观察者订阅
world.observeWithData<BreakthroughSuccessEvent>().exec {
    println("${this.entity} 突破成功: ${this.event.oldRealm} → ${this.event.newRealm}")
}
```

**Step 2: 添加事件说明**

在文档中添加事件列表：
```markdown
### 事件
| 事件 | 触发时机 | 数据 |
|------|----------|------|
| `BreakthroughSuccessEvent` | 突破成功 | oldRealm, oldLayer, newRealm, newLayer |
| `BreakthroughFailedEvent` | 突破失败 | currentRealm, currentLayer, attemptedRealm, attemptedLayer |
```

**Step 3: 提交**

```bash
git add business-modules/business-cultivation/AGENTS.md
git commit -m "$(cat <<'EOF'
docs: 更新 cultivation 模块文档

## 做了什么
- 更新使用示例
- 添加事件说明

## 遇到的问题
- 无

## 解决方案
- 反映事件驱动的使用方式
EOF
)"
```

---

## Task 8: 运行全量测试验证

**Step 1: 运行全量测试**

Run: `./gradlew :business-modules:business-cultivation:test`
Expected: PASS

**Step 2: 运行覆盖率**

Run: `./gradlew :business-modules:business-cultivation:allCoverage`
Expected: 核心逻辑覆盖率 > 80%

---

## 完成确认

- [ ] BreakthroughSuccessEvent 事件类已创建
- [ ] BreakthroughFailedEvent 事件类已创建
- [ ] 测试用例通过
- [ ] CultivationService 已修改使用 emit
- [ ] CultivationAddon 已注册事件类型
- [ ] 现有测试已更新
- [ ] 文档已更新
- [ ] 全量测试通过
