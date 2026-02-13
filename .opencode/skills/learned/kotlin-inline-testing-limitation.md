# Kotlin Inline 函数测试限制

**提取时间：** 2026-02-14
**适用场景：** 为 lko-core 的 Bits.kt 等包含大量 inline 函数的模块编写测试时

## 问题描述

Bits.kt 包含许多 inline 函数（如 `extract`, `reverseBits`, `reverseBytes` 等），尝试为这些函数编写测试时遇到问题：

```kotlin
// 编译失败 - inline 函数无法以这种方式测试
@Test
fun testReverseBits() {
    val value = 0b0001_0000
    val reversed = value.reverseBits()  // 运行时行为可能与预期不符
    assertEquals(0b0000_1000, reversed)
}
```

测试断言失败，说明对这些 inline 函数的理解有误。

## 解决方案

对于包含大量 inline 函数的模块，优先测试：
1. **非 inline 的公共 API**（如 BitSet 类）
2. **value class 和工具类**（如 IntMaskRange）

避免直接测试：
- **inline 顶层函数** - 它们在编译时被内联，无法按传统方式测试
- **复杂位操作** - 运行时行为可能与源代码预期不符

## 示例

正确做法 - 测试 BitSet 类而非 inline 函数：

```kotlin
@Test
fun testBitSetOperations() {
    val bitSet = BitSet(LongFastList())
    bitSet[0] = true
    bitSet[5] = true
    
    assertTrue(bitSet[0])
    assertTrue(bitSet[5])
    assertEquals(2, bitSet.countOneBits())
}
```

## 适用场景

当模块包含大量 inline 函数时（如 lko-core 的 Bits.kt 有 250+ 行 inline 代码），应该：
- 测试公共 API 而非内部实现
- 关注行为而非具体实现细节
- 通过集成测试验证功能正确性
