# Kotlin Multiplatform FastList 测试模式

**提取时间：** 2026-02-14
**适用场景：** 为 lko-core 中的 FastList 系列集合编写测试时

## 问题描述

在编写 LongFastList 等 FastList 类型的测试时，尝试使用 `safeInsert` 和 `safeInsertLast` 的回调参数时遇到了编译错误：

```kotlin
// 错误写法
list.safeInsertLast(3) {
    insert(1L)  // 编译错误！找不到 insert 方法
    insert(2L)
    insert(3L)
}
```

## 解决方案

FastList 使用 `ListEditor` 接口，需要调用 `unsafeInsert` 方法：

```kotlin
// 正确写法
list.safeInsertLast(3) {
    unsafeInsert(1L)
    unsafeInsert(2L)
    unsafeInsert(3L)
}

// 对于 safeInsert 同样
list.safeInsert(1, 2) {
    unsafeInsert(2L)
    unsafeInsert(3L)
}
```

`ListEditor` 接口定义：
```kotlin
fun interface ListEditor<T> {
    fun unsafeInsert(element: T)
}
```

## 使用时机

当为以下类型编写测试时：
- `LongFastList`
- `IntFastList`
- `FloatFastList`
- `DoubleFastList`
- `ObjectFastList`
- `ShortFastList`
- `ByteFastList`
- `CharFastList`

需要使用 `safeInsert` 或 `safeInsertLast` 的回调时，必须使用 `unsafeInsert` 方法。
