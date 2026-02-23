# lko-ecs - ECS 框架核心

## 模块定位
高性能 Entity-Component-System (ECS) 游戏引擎核心框架，支持多平台（JVM、Android、JS、Wasm）。

**层级**: 核心逻辑层（依赖 lko-core, lko-di）

## 核心职责
- 实体（Entity）管理
- 组件（Component）存储与查询
- 族（Family）过滤与匹配
- 观察者（Observer）事件系统
- 关系（Relation）父子/实例系统
- 查询（Query）DSL
- 渲染管线（Pipeline）调度

## 目录结构
```
lko-ecs/src/commonMain/kotlin/cn/jzl/ecs/
├── World.kt               # ECS 世界容器
├── Entity.kt              # 实体操作接口
├── ECS.kt                 # ECS 定义
├── Pipeline.kt            # 渲染管线
├── Updatable.kt           # 更新接口
│
├── addon/                 # 插件系统
│   ├── Addon.kt           # 插件定义
│   ├── AddonInstaller.kt
│   ├── Phase.kt           # 生命周期阶段
│   ├── WorldSetup.kt
│   └── Injector.kt
│
├── component/             # 组件系统
│   ├── Components.kt
│   ├── ComponentService.kt
│   ├── ComponentStore.kt
│   ├── IntComponentStore.kt
│   ├── LongComponentStore.kt
│   └── GeneralComponentStore.kt
│
├── entity/                # 实体管理
│   ├── Entity.kt
│   ├── EntityService.kt
│   ├── EntityStore.kt
│   ├── EntityEditor.kt
│   └── EntityRelationContext.kt
│
├── family/                # 族系统
│   ├── Family.kt
│   ├── FamilyService.kt
│   └── FamilyMatcher.kt
│
├── query/                 # 查询系统
│   ├── Query.kt
│   ├── QueryService.kt
│   ├── EntityQueryContext.kt
│   ├── Accessor.kt
│   └── QueryStreamExtensions.kt
│
├── relation/              # 关系系统
│   ├── Relation.kt
│   ├── RelationService.kt
│   └── RelationProvider.kt
│
├── observer/              # 观察者系统
│   ├── Observer.kt
│   ├── ObserveService.kt
│   └── ObserverBuilder.kt
│
└── archetype/             # 原型系统
    ├── Archetype.kt
    ├── ArchetypeService.kt
    └── Table.kt
```

## 关键 API

### 世界创建
```kotlin
// 创建 World
val world = world {
    install(myAddon)
}

// 使用 Addon
val myAddon = createAddon("myAddon") {
    components {
        world.componentId<Position>()
        world.componentId<Velocity>()
    }
    injects {
        this bind singleton { new(::MyService) }
    }
}
```

### 实体操作
```kotlin
// 创建实体
val entity = world.entity {
    it.addComponent(Position(10, 20))
    it.addComponent(Velocity(1, 0))
}

// 编辑实体
entity.editor {
    it.addComponent(Health(100))
}

// 删除实体
entity.delete()
```

### 查询系统
```kotlin
// 定义查询上下文
class MyContext(world: World) : EntityQueryContext(world) {
    val position: Position by component()
    val velocity: Velocity by component()
}

// 执行查询
world.query { MyContext(world) }.forEach { ctx ->
    ctx.position.x += ctx.velocity.x
}
```

### 观察者系统
```kotlin
// 监听组件添加
entity.observe<OnComponentAdded<Position>>().exec {
    println("Position added to ${this.entity}")
}

// 发送事件
entity.emit<CustomEvent>()
```

### 关系系统
```kotlin
// 建立父子关系
parent.addChild(child)

// 查询子实体
entity.children.forEach { child ->
    // 处理子实体
}
```

## 依赖关系

```kotlin
// build.gradle.kts
dependencies {
    implementation(projects.libs.lkoCore)
    implementation(projects.libs.lkoDi)
}
```

## AI 开发指引

### 开发原则
- **性能优先**: 核心循环中避免内存分配
- **类型安全**: 严格区分组件和标签接口
- **测试强制**: 必须保持 95%+ 的测试覆盖率
- **代码风格**: 包名 `cn.jzl.ecs`，组件为名词，标签为形容词+Tag

### 组件 vs 标签
```kotlin
// 组件：有数据
data class Position(val x: Int, val y: Int)
data class Health(val value: Int)

// 标签：无数据，使用 sealed class 或 object
sealed class AliveTag
object PlayerTag
```

### 查询优化
```kotlin
// ✅ 推荐：复用查询上下文
class MyContext(world: World) : EntityQueryContext(world) {
    val position: Position by component()
}

// ❌ 避免：在循环中创建查询
while (true) {
    world.query { MyContext(world) }.forEach { ... }  // 每次创建新实例
}
```

### 禁止事项
- ❌ 禁止在 `query {}.forEach` 中修改实体结构
- ❌ 禁止混淆 `family.component` 和 `relation.component`
- ❌ 禁止跳过测试（必须 95%+ 覆盖率）

## 测试要求
- 实体生命周期测试
- 组件 CRUD 测试
- 查询系统测试
- 观察者系统测试
- 关系系统测试
- 性能基准测试

## 文档参考
详细使用指南请参阅: `docs/technology/ecs/AGENT.md`
