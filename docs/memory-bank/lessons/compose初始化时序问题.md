## Compose初始化时序问题

> 创建时间: 2026-02-23
> 分类: lessons
> 状态: active
> 标签: #compose #初始化 #时序

**问题/背景**:
ViewModel 依赖 World 实例，但 World 在 LaunchedEffect 中异步初始化，导致 ViewModel 创建时 World 为 null。

**错误做法**:
```kotlin
// ❌ LaunchedEffect 是异步的，ViewModel 创建时 World 可能未初始化
LaunchedEffect(Unit) {
    world = remember { World() }
}
val viewModel = ViewModel(world)  // world 可能是 null
```

**正确做法**:
```kotlin
// ✅ 使用 remember 同步初始化，确保顺序
val world = remember { World() }
val viewModel = remember { ViewModel(world) }
```

**教训**:
- LaunchedEffect 用于副作用（网络请求、动画），不适合初始化依赖
- 有依赖关系的对象要用同步方式初始化
- Compose 重组时 remember 保持状态，LaunchedEffect 会重新执行

**关联记忆**: 无
