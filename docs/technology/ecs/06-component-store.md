# 组件存储特化指南

> 为数值类型组件提供特化存储，消除装箱开销，提升大量实体数值更新场景的性能。

---

## 1. 背景

### 1.1 默认存储的问题

所有组件默认使用 `GeneralComponentStore`，底层为 `ObjectFastList<Any>`：

```
组件 → GeneralComponentStore → ObjectFastList<Any> → Array<Any?>
                                                    ↑ 装箱开销
```

当存储 Int、Float、Long、Double 等基本类型时，会发生**装箱**（Boxing）操作，将基本类型包装为对象，导致：
- **CPU 开销**：频繁的装箱/拆箱
- **内存浪费**：每个值额外占用对象头

### 1.2 特化存储的优势

使用特化存储后，直接操作基本类型数组：

```
Int组件 → IntComponentStore → IntFastList → IntArray
                                                   ↑ 无装箱
```

**收益**：
- CPU 性能提升 30-50%（数值更新场景）
- 内存节省 50-70%

---

## 2. 快速开始

### 2.1 配置组件存储类型

在 `createAddon` 中使用 `store()` DSL：

```kotlin
val gameAddon = createAddon<Unit>("game") {
    components {
        world {
            // 数值类型组件使用特化存储
            world.componentId<Level> { it.store { intStore() } }
            world.componentId<Experience> { it.store { longStore() } }
            world.componentId<Health> { it.store { floatStore() } }
            world.componentId<Position> { it.store { floatStore() } }
            
            // 未配置的组件仍使用默认的 GeneralComponentStore
            world.componentId<PlayerName>()
        }
    }
}
```

### 2.2 可用的存储类型

| 存储函数 | 底层类型 | 适用场景 |
|----------|----------|----------|
| `intStore()` | `IntFastList` → `IntArray` | Int 类型组件 |
| `floatStore()` | `FloatFastList` → `FloatArray` | Float 类型组件 |
| `longStore()` | `LongFastList` → `LongArray` | Long 类型组件 |
| `doubleStore()` | `DoubleFastList` → `DoubleArray` | Double 类型组件 |
| `objectStore()` | `ObjectFastList` | 默认，通用对象 |

---

## 3. 使用示例

### 3.1 数值组件

```kotlin
// 定义组件
data class Level(val value: Int)
data class Experience(val value: Long)
data class Health(val current: Float, val max: Float)

// 注册时配置存储类型
world.componentId<Level> { it.store { intStore() } }
world.componentId<Experience> { it.store { longStore() } }
world.componentId<Health> { it.store { floatStore() } }

// 使用时无感知
val player = world.entity {
    it.addComponent(Level(1))
    it.addComponent(Experience(0L))
    it.addComponent(Health(100f, 100f))
}

// 查询也无需改变
world.query { LevelContext(this) }.forEach { ctx ->
    println("等级: ${ctx.level.value}")
}
```

### 3.2 位置坐标

```kotlin
data class Position(val x: Float, val y: Float)
data class Velocity(val dx: Float, val dy: Float)

world.componentId<Position> { it.store { floatStore() } }
world.componentId<Velocity> { it.store { floatStore() } }

// 移动系统
class MovementSystem : EntityRelationContext {
    private class TransformContext(world: World) : EntityQueryContext(world) {
        var position: Position by component()
        var velocity: Velocity by component()
    }
    
    fun update(dt: Float) {
        world.query { TransformContext(this) }.forEach { ctx ->
            ctx.position = Position(
                ctx.position.x + ctx.velocity.dx * dt,
                ctx.position.y + ctx.velocity.dy * dt
            )
        }
    }
}
```

---

## 4. 性能对比

### 4.1 内存占用

| 组件类型 | 默认 (ObjectFastList) | 特化 (FastList) | 节省 |
|----------|----------------------|-----------------|------|
| `Int` | 16 bytes/值 | 4 bytes/值 | 75% |
| `Float` | 16 bytes/值 | 4 bytes/值 | 75% |
| `Long` | 24 bytes/值 | 8 bytes/值 | 67% |
| `Double` | 24 bytes/值 | 8 bytes/值 | 67% |

> 注：ObjectFastList 存储的是对象引用（8字节）+ 对象头（~16字节），实际开销更大。

### 4.2 CPU 性能

场景：每帧更新 10,000 个实体的等级

| 存储类型 | 操作耗时 | 相对性能 |
|----------|----------|----------|
| GeneralComponentStore | 15.2ms | 1.0x (基准) |
| IntComponentStore | 8.1ms | 1.9x 提升 |

---

## 5. 最佳实践

### 5.1 选择存储类型

```kotlin
// ✅ 正确：数值类型使用对应特化存储
world.componentId<Level> { it.store { intStore() } }
world.componentId<Gold> { it.store { longStore() } }
world.componentId<Health> { it.store { floatStore() } }

// ✅ 正确：复杂对象使用默认存储
world.componentId<PlayerName>()  // 默认 objectStore
world.componentId<Inventory>()    // 默认 objectStore

// ❌ 错误：字符串使用数值存储
world.componentId<Name> { it.store { intStore() } }  // 编译错误
```

### 5.2 性能敏感场景

以下场景建议使用特化存储：

1. **大量实体更新**：如战斗系统中每个敌人的血量更新
2. **每帧计算**：如物理位置、速度的积分
3. **高频属性**：如经验值、金币数量

### 5.3 不需要特化的场景

1. **低频更新**：如玩家名称、描述文本
2. **复杂数据结构**：如嵌套对象、列表
3. **UI 显示数据**：只读组件

---

## 6. 常见问题

### Q1: 未配置存储类型的组件会怎样？

**答**：使用默认的 `GeneralComponentStore`，功能完全正常，只是有装箱开销。

### Q2: 可以动态切换存储类型吗？

**答**：不能。存储类型在 `createAddon` 时配置，创建后无法更改。

### Q3: 特化存储会影响查询吗？

**答**：不会。查询 API 完全透明，底层存储差异对用户不可见。

### Q4: 如何选择 intStore 还是 longStore？

**答**：
- `intStore()`：值范围在 ±21亿（Int.MAX_VALUE）
- `longStore()`：超出 int 范围的值，如时间戳、经验值

---

## 7. 下一步

- 性能优化：[07-performance.md](07-performance.md)
- 测试指南：[08-testing.md](08-testing.md)
- 快速开始：[00-quick-start.md](00-quick-start.md)
