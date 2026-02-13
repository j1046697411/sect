# ECS Observer 正确使用模式

**提取时间：** 2026-02-14
**适用场景：** 使用 ECS Observer 系统监听实体事件变化

## 问题描述

Observer API 有多种变体，容易混淆：
1. `observe<T>()` - 无数据事件
2. `observeWithData<T>()` - 带数据事件
3. `exec { }` 中的参数访问方式不一致

常见错误：
```kotlin
// ❌ 错误：使用 it 访问带数据的observer
entity.observeWithData<String>().exec {
    receivedData = it  // 编译错误或不正确
}
```

## 解决方案

### 模式1：无数据 Observer
```kotlin
// 适用于 OnInserted, OnRemoved 等系统事件
entity.observe<OnInserted>().exec {
    println("Entity ${this.entity} inserted")
    // this 是 ObserverContext
}
```

### 模式2：带数据 Observer
```kotlin
// 适用于自定义数据事件
entity.observeWithData<String>().exec {
    receivedData = this.event  // ✅ 使用 this.event
    // this 是 ObserverContextWithData<E>
}

// 触发自定义事件
world.emit(entity, "test data")
```

### 模式3：带 Query 的 Observer
```kotlin
// 在observer中访问查询上下文
val query = world.query { PositionContext(this) }

entity.observe<OnInserted>().exec(query) { ctx ->
    // ctx 是 PositionContext
    println("Position: ${ctx.position}")
}
```

### 模式4：多 Query Observer
```kotlin
val posQuery = world.query { PositionContext(this) }
val healthQuery = world.query { HealthContext(this) }

entity.observe<OnInserted>().exec(posQuery, healthQuery) { pos, health ->
    // pos: PositionContext, health: HealthContext
    println("Pos: ${pos.position}, Health: ${health.health}")
}
```

## 上下文类型对照表

| Observer 类型 | 上下文类型 | 访问方式 |
|--------------|-----------|----------|
| `observe<T>()` | `ObserverContext` | `this.entity` |
| `observeWithData<E>()` | `ObserverContextWithData<E>` | `this.entity`, `this.event` |
| `exec(query) { }` | `EntityQueryContext` | 参数名（如 ctx）|

## 完整示例

```kotlin
class ObserverTest : EntityRelationContext {
    override val world: World by lazy { 
        world { install(testAddon) } 
    }
    
    @Test
    fun testObserverPatterns() {
        var receivedEvent = false
        var receivedData: String? = null
        
        val entity = world.entity {}
        
        // 1. 无数据observer
        entity.observe<OnInserted>().exec {
            receivedEvent = true
            assertNotNull(this.entity)  // 通过 this 访问
        }
        
        // 2. 带数据observer
        entity.observeWithData<String>().exec {
            receivedData = this.event   // 使用 this.event
        }
        
        // 触发事件
        entity.editor { it.addComponent(Position(10, 20)) }
        world.emit(entity, "test")
        
        assertTrue(receivedEvent)
        assertEquals("test", receivedData)
    }
}
```

## 常见陷阱

### 陷阱1：混淆 it 和 this
```kotlin
// ❌ 错误
entity.observeWithData<String>().exec {
    val data = it  // it 不存在或不是事件数据
}

// ✅ 正确
entity.observeWithData<String>().exec {
    val data = this.event
}
```

### 陷阱2：忘记触发事件
```kotlin
// 只设置了observer，但没有触发
entity.observe<OnInserted>().exec { 
    println("This will be called when component is added")
}
// 需要调用 entity.editor { it.addComponent(...) } 才能触发
```

### 陷阱3：在observer中修改实体
```kotlin
// ⚠️ 危险：可能导致递归或并发问题
entity.observe<OnInserted>().exec {
    this.entity.editor { 
        it.addComponent(AnotherComponent())  // 可能触发其他observer
    }
}
```

## 使用时机

- 需要监听实体生命周期事件（创建、更新、销毁）
- 实现组件变化时的回调机制
- 构建事件驱动的系统架构
- 调试实体状态变化
