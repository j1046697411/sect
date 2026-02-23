# disciples 模块事件化重构 实现计划

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 将 disciples 模块改为订阅 cultivation 模块突破事件，实现领域间事件驱动通信。

**Architecture:** 订阅 BreakthroughSuccessEvent，在回调中处理弟子晋升等业务逻辑。

**Tech Stack:** Kotlin, ECS 框架观察者系统, TDD

---

## Task 1: 创建弟子晋升事件类

**Files:**
- Create: `business-modules/business-disciples/src/commonMain/kotlin/cn/jzl/sect/disciples/events/DisciplePromotedEvent.kt`

**Step 1: 创建事件类文件**

```kotlin
package cn.jzl.sect.disciples.events

import cn.jzl.ecs.entity.Entity
import cn.jzl.sect.core.sect.SectPositionType

data class DisciplePromotedEvent(
    val entity: Entity,
    val oldPosition: SectPositionType,
    val newPosition: SectPositionType
)
```

**Step 2: 提交**

```bash
git add business-modules/business-disciples/src/commonMain/kotlin/cn/jzl/sect/disciples/events/DisciplePromotedEvent.kt
git commit -m "$(cat <<'EOF'
feat: 添加 DisciplePromotedEvent 事件类

## 做了什么
- 创建弟子晋升事件类

## 遇到的问题
- 无

## 解决方案
- 纯数据类
EOF
)"
```

---

## Task 2: 修改 DisciplesAddon 添加事件订阅

**Files:**
- Modify: `business-modules/business-disciples/src/commonMain/kotlin/cn/jzl/sect/disciples/DisciplesAddon.kt`

**Step 1: 添加导入和事件订阅**

在 Addon 中添加：
```kotlin
import cn.jzl.sect.cultivation.events.BreakthroughSuccessEvent
import cn.jzl.sect.cultivation.events.BreakthroughFailedEvent
import cn.jzl.sect.disciples.events.DisciplePromotedEvent

// 在 on(Phase.ENABLE) 中添加事件订阅
on(Phase.ENABLE) {
    world.observeWithData<BreakthroughSuccessEvent>().exec { event ->
        handleBreakthroughSuccess(event.entity, event.event)
    }
}
```

**Step 2: 添加事件处理方法**

添加一个处理方法：
```kotlin
private fun handleBreakthroughSuccess(entity: Entity, event: BreakthroughSuccessEvent) {
    // TODO: 根据境界判断是否晋升
    // 例如：炼气期可晋升为内门弟子
}
```

**Step 3: 注册事件组件**

在 components 块中添加：
```kotlin
world.componentId<DisciplePromotedEvent>()
```

**Step 4: 提交**

```bash
git add business-modules/business-disciples/src/commonMain/kotlin/cn/jzl/sect/disciples/DisciplesAddon.kt
git commit -m "$(cat <<'EOF'
feat: 在 DisciplesAddon 中添加突破事件订阅

## 做了什么
- 订阅 BreakthroughSuccessEvent
- 添加事件处理方法框架

## 遇到的问题
- 无

## 解决方案
- 实现领域间事件驱动通信
EOF
)"
```

---

## Task 3: 编写测试用例

**Files:**
- Create: `business-modules/business-disciples/src/commonTest/kotlin/cn/jzl/sect/disciples/events/DiscipleEventTest.kt`

**Step 1: 编写测试用例**

```kotlin
package cn.jzl.sect.disciples.events

import cn.jzl.ecs.ECSDsl
import cn.jzl.ecs.World
import cn.jzl.ecs.addon.Addon
import cn.jzl.ecs.addon.WorldSetup
import cn.jzl.ecs.addon.components
import cn.jzl.ecs.addon.createAddon
import cn.jzl.ecs.component.componentId
import cn.jzl.ecs.entity.EntityRelationContext
import cn.jzl.ecs.entity.addComponent
import cn.jzl.ecs.observer.observeAddon
import cn.jzl.ecs.observer.observeWithData
import cn.jzl.ecs.world
import cn.jzl.log.logAddon
import cn.jzl.sect.core.ai.CurrentBehavior
import cn.jzl.sect.core.ai.BehaviorType
import cn.jzl.sect.core.cultivation.Realm
import cn.jzl.sect.core.sect.SectPositionInfo
import cn.jzl.sect.core.sect.SectPositionType
import cn.jzl.sect.core.vitality.Spirit
import cn.jzl.sect.core.vitality.Vitality
import cn.jzl.sect.cultivation.cultivationAddon
import cn.jzl.sect.cultivation.components.CultivationProgress
import cn.jzl.sect.cultivation.components.Talent
import cn.jzl.sect.cultivation.services.CultivationService
import cn.jzl.sect.disciples.DisciplesAddon
import cn.jzl.sect.disciples.events.DisciplePromotedEvent
import kotlin.test.*

class DiscipleEventTest : EntityRelationContext {
    
    override lateinit var world: World
    private lateinit var cultivationService: CultivationService
    
    @OptIn(ECSDsl::class)
    private fun createTestWorld(): World {
        return world {
            WorldSetupInstallHelper.install(this, cultivationAddon)
            WorldSetupInstallHelper.install(this, DisciplesAddon)
            WorldSetupInstallHelper.install(this, observeAddon)
            WorldSetupInstallHelper.install(this, logAddon)
        }
    }
    
    @BeforeTest
    fun setup() {
        world = createTestWorld()
        cultivationService = CultivationService(world)
    }
    
    @Test
    fun testBreakthroughTriggersPromotionCheck() {
        var promotionEventReceived = false
        
        world.observeWithData<DisciplePromotedEvent>().exec {
            promotionEventReceived = true
        }
        
        // 创建一个即将突破到炼气期的弟子
        val config = cn.jzl.sect.core.config.GameConfig
        val maxLayer = config.cultivation.maxLayerPerRealm
        
        world.entity {
            it.addComponent(CultivationProgress(
                realm = Realm.MORTAL,
                layer = maxLayer,
                cultivation = config.cultivation.getMaxCultivation(Realm.MORTAL, maxLayer) - 1,
                maxCultivation = config.cultivation.getMaxCultivation(Realm.MORTAL, maxLayer)
            ))
            it.addComponent(Talent(comprehension = 100, physique = 100, fortune = 100))
            it.addComponent(SectPositionInfo(SectPositionType.DISCIPLE_OUTER))
            it.addComponent(Vitality(100, 100))
            it.addComponent(Spirit(100, 100))
            it.addComponent(CurrentBehavior(type = BehaviorType.CULTIVATE))
        }
        
        cultivationService.update(1)
        
        // 验证收到了晋升事件（因为境界突破到炼气期）
        assertTrue(promotionEventReceived, "Should receive promotion event when breakthrough to QI_REFINING")
    }
    
    private object WorldSetupInstallHelper {
        @Suppress("UNCHECKED_CAST")
        fun install(ws: WorldSetup, addon: Addon<*, *>) {
            ws.install(addon as Addon<Any, Any>) {}
        }
    }
}
```

**Step 2: 提交**

```bash
git add business-modules/business-disciples/src/commonTest/kotlin/cn/jzl/sect/disciples/events/DiscipleEventTest.kt
git commit -m "$(cat <<'EOF'
test: 添加弟子事件测试用例

## 做了什么
- 添加突破触发晋升检查测试

## 遇到的问题
- 无

## 解决方案
- TDD 模式
EOF
)"
```

---

## Task 4: 实现晋升逻辑

**Files:**
- Modify: `business-modules/business-disciples/src/commonMain/kotlin/cn/jzl/sect/disciples/DisciplesAddon.kt`

**Step 1: 实现晋升判断逻辑**

```kotlin
private fun handleBreakthroughSuccess(entity: Entity, event: BreakthroughSuccessEvent) {
    // 根据新境界判断是否晋升职位
    val newPosition = when (event.newRealm) {
        Realm.QI_REFINING -> SectPositionType.DISCIPLE_INNER  // 炼气期晋升内门
        Realm.FOUNDATION -> SectPositionType.ELDER           // 筑基期晋升长老
        else -> null
    }
    
    if (newPosition != null) {
        // 获取当前职位
        val query = world.query { PositionQueryContext(world) }
        var currentPosition: SectPositionType? = null
        query.forEach { ctx ->
            if (ctx.entity == entity) {
                currentPosition = ctx.position.position
            }
        }
        
        // 如果可以晋升
        if (currentPosition != null && currentPosition!! < newPosition) {
            // 更新职位组件
            world.editor(entity) {
                it.addComponent(SectPositionInfo(newPosition))
            }
            
            // 发送晋升事件
            world.emit(entity, DisciplePromotedEvent(
                entity = entity,
                oldPosition = currentPosition!!,
                newPosition = newPosition
            ))
        }
    }
}
```

**Step 2: 添加辅助查询上下文**

在 Addon 文件中添加：
```kotlin
private class PositionQueryContext(world: World) : EntityQueryContext(world) {
    val position: SectPositionInfo by component()
}
```

**Step 3: 运行测试确认通过**

**Step 4: 提交**

---

## Task 5: 更新文档

**Files:**
- Modify: `business-modules/business-disciples/AGENTS.md`

**Step 1: 更新文档**

添加事件说明和使用示例。

**Step 2: 提交**

---

## Task 6: 运行全量测试验证

**Step 1: 运行测试**

```bash
./gradlew :business-modules:business-disciples:test
```

---

## 完成确认

- [ ] DisciplePromotedEvent 事件类已创建
- [ ] DisciplesAddon 已添加事件订阅
- [ ] 晋升逻辑已实现
- [ ] 测试通过
- [ ] 文档更新
