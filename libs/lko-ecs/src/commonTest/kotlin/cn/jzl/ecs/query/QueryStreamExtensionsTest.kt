package cn.jzl.ecs.query

import cn.jzl.ecs.World
import cn.jzl.ecs.family.Family
import kotlin.test.*

class QueryStreamExtensionsTest {
    
    // Simple test data class
    data class TestData(val value: Int, val name: String)

    // Helper function to create a QueryStream from a list
    private fun <T> createTestStream(data: List<T>): QueryStream<T> {
        return object : QueryStream<T>, QueryStreamScope {
            override val world: World get() = TODO("Not yet implemented")
            override val family: Family get() = TODO("Not yet implemented")
            override fun collect(collector: QueryCollector<T>) = with(collector) {
                runCatching {
                    data.forEach { value -> emit(value) }
                }.recoverCatching { if (it !is AbortQueryException) throw it }.getOrThrow()
            }

            override fun close() {
            }
        }
    }

    @Test
    fun testMapOperation() {
        val stream = createTestStream(listOf(TestData(1, "a"), TestData(2, "b")))
        val result = stream.map { it.value * 2 }.toList()

        assertEquals(2, result.size)
        assertEquals(2, result[0])
        assertEquals(4, result[1])
    }

    @Test
    fun testMapEmptyStream() {
        val stream = createTestStream(emptyList<TestData>())
        val result = stream.map { it.value * 2 }.toList()

        assertEquals(0, result.size)
    }

    @Test
    fun testMapSingleElement() {
        val stream = createTestStream(listOf(TestData(1, "a")))
        val result = stream.map { it.name.uppercase() }.toList()

        assertEquals(1, result.size)
        assertEquals("A", result[0])
    }

    @Test
    fun testMapNotNullOperation() {
        val stream = createTestStream(listOf(TestData(1, "a"), TestData(2, "b"), TestData(3, "c")))
        val result = stream.mapNotNull { if (it.value % 2 == 0) it.name else null }.toList()

        assertEquals(1, result.size)
        assertEquals("b", result[0])
    }

    @Test
    fun testMapNotNullAllNulls() {
        val stream = createTestStream(listOf(TestData(1, "a"), TestData(3, "c"), TestData(5, "e")))
        val result = stream.mapNotNull { if (it.value % 2 == 0) it.name else null }.toList()

        assertEquals(0, result.size)
    }

    @Test
    fun testMapNotNullSingleNonNull() {
        val stream = createTestStream(listOf(TestData(1, "a"), TestData(2, "b")))
        val result = stream.mapNotNull { if (it.value == 2) it.name else null }.toList()

        assertEquals(1, result.size)
        assertEquals("b", result[0])
    }

    @Test
    fun testFlatMapOperation() {
        val stream = createTestStream(listOf(TestData(1, "a"), TestData(2, "b")))
        val result = stream.flatMap { createTestStream(listOf(it.value, it.value * 10)) }.toList()

        assertEquals(4, result.size)
        assertEquals(1, result[0])
        assertEquals(10, result[1])
        assertEquals(2, result[2])
        assertEquals(20, result[3])
    }

    @Test
    fun testFlatMapEmptyNestedStreams() {
        val stream = createTestStream(listOf(TestData(1, "a"), TestData(2, "b")))
        val result = stream.flatMap { createTestStream(emptyList<Int>()) }.toList()

        assertEquals(0, result.size)
    }

    @Test
    fun testFlatMapSingleElement() {
        val stream = createTestStream(listOf(TestData(1, "a")))
        val result = stream.flatMap { createTestStream(listOf(it.name, it.name.reversed())) }.toList()

        assertEquals(2, result.size)
        assertEquals("a", result[0])
        assertEquals("a", result[1])
    }

    @Test
    fun testFilterOperation() {
        val stream = createTestStream(listOf(TestData(1, "a"), TestData(2, "b"), TestData(3, "c")))
        val result = stream.filter { it.value > 1 }.toList()

        assertEquals(2, result.size)
        assertEquals(2, result[0].value)
        assertEquals(3, result[1].value)
    }

    @Test
    fun testFilterNoMatches() {
        val stream = createTestStream(listOf(TestData(1, "a"), TestData(2, "b")))
        val result = stream.filter { it.value > 5 }.toList()

        assertEquals(0, result.size)
    }

    @Test
    fun testFilterAllMatches() {
        val stream = createTestStream(listOf(TestData(1, "a"), TestData(2, "b")))
        val result = stream.filter { it.value > 0 }.toList()

        assertEquals(2, result.size)
    }

    @Test
    fun testFilterNotOperation() {
        val stream = createTestStream(listOf(TestData(1, "a"), TestData(2, "b"), TestData(3, "c")))
        val result = stream.filterNot { it.value > 1 }.toList()

        assertEquals(1, result.size)
        assertEquals(1, result[0].value)
    }

    @Test
    fun testFilterNotNoMatches() {
        val stream = createTestStream(listOf(TestData(1, "a"), TestData(2, "b")))
        val result = stream.filterNot { it.value > 5 }.toList()

        assertEquals(2, result.size)
    }

    @Test
    fun testTakeOperation() {
        val stream = createTestStream(listOf(TestData(1, "a"), TestData(2, "b"), TestData(3, "c")))
        val result = stream.take(2).toList()

        assertEquals(2, result.size)
        assertEquals(1, result[0].value)
        assertEquals(2, result[1].value)
    }

    @Test
    fun testTakeZero() {
        val stream = createTestStream(listOf(TestData(1, "a"), TestData(2, "b")))
        val result = stream.take(0).toList()

        assertEquals(0, result.size)
    }

    @Test
    fun testTakeMoreThanSize() {
        val stream = createTestStream(listOf(TestData(1, "a"), TestData(2, "b")))
        val result = stream.take(5).toList()

        assertEquals(2, result.size)
    }

    @Test
    fun testDropOperation() {
        val stream = createTestStream(listOf(TestData(1, "a"), TestData(2, "b"), TestData(3, "c")))
        val result = stream.drop(1).toList()

        assertEquals(2, result.size)
        assertEquals(2, result[0].value)
        assertEquals(3, result[1].value)
    }

    @Test
    fun testDropAll() {
        val stream = createTestStream(listOf(TestData(1, "a"), TestData(2, "b")))
        val result = stream.drop(2).toList()

        assertEquals(0, result.size)
    }

    @Test
    fun testDropMoreThanSize() {
        val stream = createTestStream(listOf(TestData(1, "a"), TestData(2, "b")))
        val result = stream.drop(5).toList()

        assertEquals(0, result.size)
    }

    @Test
    fun testTakeWhileOperation() {
        val stream = createTestStream(listOf(TestData(1, "a"), TestData(2, "b"), TestData(3, "c")))
        val result = stream.takeWhile { it.value < 3 }.toList()

        assertEquals(2, result.size)
        assertEquals(1, result[0].value)
        assertEquals(2, result[1].value)
    }

    @Test
    fun testTakeWhileAlwaysFalse() {
        val stream = createTestStream(listOf(TestData(1, "a"), TestData(2, "b")))
        val result = stream.takeWhile { it.value > 5 }.toList()

        assertEquals(0, result.size)
    }

    @Test
    fun testTakeWhileAlwaysTrue() {
        val stream = createTestStream(listOf(TestData(1, "a"), TestData(2, "b")))
        val result = stream.takeWhile { it.value > 0 }.toList()

        assertEquals(2, result.size)
    }

    @Test
    fun testDropWhileOperation() {
        val stream = createTestStream(listOf(TestData(1, "a"), TestData(2, "b"), TestData(3, "c")))
        val result = stream.dropWhile { it.value < 2 }.toList()

        assertEquals(2, result.size)
        assertEquals(2, result[0].value)
        assertEquals(3, result[1].value)
    }

    @Test
    fun testDropWhileAlwaysFalse() {
        val stream = createTestStream(listOf(TestData(1, "a"), TestData(2, "b")))
        val result = stream.dropWhile { it.value > 5 }.toList()

        assertEquals(2, result.size)
    }

    @Test
    fun testDropWhileAlwaysTrue() {
        val stream = createTestStream(listOf(TestData(1, "a"), TestData(2, "b")))
        val result = stream.dropWhile { it.value > 0 }.toList()

        assertEquals(0, result.size)
    }

    @Test
    fun testDistinctOperation() {
        val stream = createTestStream(
            listOf(
                TestData(1, "a"),
                TestData(2, "b"),
                TestData(1, "a"),
                TestData(3, "c")
            )
        )
        val result = stream.distinct().toList()

        assertEquals(3, result.size)
    }

    @Test
    fun testDistinctEmpty() {
        val stream = createTestStream(emptyList<TestData>())
        val result = stream.distinct().toList()

        assertEquals(0, result.size)
    }

    @Test
    fun testDistinctSingleElement() {
        val stream = createTestStream(listOf(TestData(1, "a")))
        val result = stream.distinct().toList()

        assertEquals(1, result.size)
    }

    @Test
    fun testDistinctAllSame() {
        val stream = createTestStream(
            listOf(
                TestData(1, "a"),
                TestData(1, "a"),
                TestData(1, "a")
            )
        )
        val result = stream.distinct().toList()

        assertEquals(1, result.size)
    }

    @Test
    fun testDistinctByOperation() {
        val stream = createTestStream(
            listOf(
                TestData(1, "a"),
                TestData(2, "b"),
                TestData(1, "c"),
                TestData(3, "d")
            )
        )
        val result = stream.distinctBy { it.value }.toList()

        assertEquals(3, result.size)
    }

    @Test
    fun testDistinctByEmpty() {
        val stream = createTestStream(emptyList<TestData>())
        val result = stream.distinctBy { it.value }.toList()

        assertEquals(0, result.size)
    }

    @Test
    fun testDistinctByDifferentKeyTypes() {
        val stream = createTestStream(
            listOf(
                TestData(1, "a"),
                TestData(1, "b"),
                TestData(2, "c"),
                TestData(2, "d")
            )
        )
        val result = stream.distinctBy { it.name.length }.toList()

        assertEquals(1, result.size) // All names have length 1
    }

    @Test
    fun testOnEachOperation() {
        val captured = mutableListOf<TestData>()
        val stream = createTestStream(listOf(TestData(1, "a"), TestData(2, "b")))
        val result = stream.onEach { captured.add(it) }.toList()

        assertEquals(2, result.size)
        assertEquals(2, captured.size)
        assertEquals(TestData(1, "a"), captured[0])
        assertEquals(TestData(2, "b"), captured[1])
    }

    @Test
    fun testOnEachEmpty() {
        val captured = mutableListOf<TestData>()
        val stream = createTestStream(emptyList<TestData>())
        val result = stream.onEach { captured.add(it) }.toList()

        assertEquals(0, result.size)
        assertEquals(0, captured.size)
    }

    @Test
    fun testForEachOperation() {
        val captured = mutableListOf<TestData>()
        val stream = createTestStream(listOf(TestData(1, "a"), TestData(2, "b")))
        stream.forEach { captured.add(it) }

        assertEquals(2, captured.size)
        assertEquals(TestData(1, "a"), captured[0])
        assertEquals(TestData(2, "b"), captured[1])
    }

    @Test
    fun testForEachEmpty() {
        val captured = mutableListOf<TestData>()
        val stream = createTestStream(emptyList<TestData>())
        stream.forEach { captured.add(it) }

        assertEquals(0, captured.size)
    }

    @Test
    fun testCountOperation() {
        val stream = createTestStream(listOf(TestData(1, "a"), TestData(2, "b"), TestData(3, "c")))
        val count = stream.count()

        assertEquals(3, count)
    }

    @Test
    fun testCountEmpty() {
        val stream = createTestStream(emptyList<TestData>())
        val count = stream.count()

        assertEquals(0, count)
    }

    @Test
    fun testFoldOperation() {
        val stream = createTestStream(listOf(TestData(1, "a"), TestData(2, "b"), TestData(3, "c")))
        val sum = stream.fold(0) { acc, data -> acc + data.value }

        assertEquals(6, sum)
    }

    @Test
    fun testFoldEmpty() {
        val stream = createTestStream(emptyList<TestData>())
        val result = stream.fold("initial") { acc, data -> acc + data.name }

        assertEquals("initial", result)
    }

    @Test
    fun testReduceOperation() {
        val stream = createTestStream(listOf(TestData(1, "a"), TestData(2, "b"), TestData(3, "c")))
        val sum = stream.map { it.value }.reduce { acc, value -> acc + value }

        assertEquals(6, sum)
    }

    @Test
    fun testReduceSingleElement() {
        val stream = createTestStream(listOf(TestData(42, "test")))
        val result = stream.map { it.value }.reduce { acc, value -> acc + value }

        assertEquals(42, result)
    }

    @Test
    fun testReduceEmptyThrowsException() {
        val stream = createTestStream(emptyList<TestData>())
        assertFailsWith<NoSuchElementException> {
            stream.map { it.value }.reduce { acc, value -> acc + value }
        }
    }

    @Test
    fun testAnyOperation() {
        val stream = createTestStream(listOf(TestData(1, "a"), TestData(2, "b"), TestData(3, "c")))
        val result = stream.any { it.value > 2 }

        assertTrue(result)
    }

    @Test
    fun testAnyEmpty() {
        val stream = createTestStream(emptyList<TestData>())
        val result = stream.any { it.value > 0 }

        assertFalse(result)
    }

    @Test
    fun testAnyFalse() {
        val stream = createTestStream(listOf(TestData(1, "a"), TestData(2, "b")))
        val result = stream.any { it.value > 5 }

        assertFalse(result)
    }

    @Test
    fun testAllOperation() {
        val stream = createTestStream(listOf(TestData(1, "a"), TestData(2, "b"), TestData(3, "c")))
        val result = stream.all { it.value > 0 }

        assertTrue(result)
    }

    @Test
    fun testAllEmpty() {
        val stream = createTestStream(emptyList<TestData>())
        val result = stream.all { it.value > 0 }

        assertTrue(result) // All elements (none) satisfy the condition
    }

    @Test
    fun testAllFalse() {
        val stream = createTestStream(listOf(TestData(1, "a"), TestData(2, "b")))
        val result = stream.all { it.value > 1 }

        assertFalse(result)
    }

    @Test
    fun testNoneOperation() {
        val stream = createTestStream(listOf(TestData(1, "a"), TestData(2, "b"), TestData(3, "c")))
        val result = stream.none { it.value > 3 }

        assertTrue(result)
    }

    @Test
    fun testNoneEmpty() {
        val stream = createTestStream(emptyList<TestData>())
        val result = stream.none { it.value > 0 }

        assertTrue(result) // No elements fail the condition
    }

    @Test
    fun testNoneFalse() {
        val stream = createTestStream(listOf(TestData(1, "a"), TestData(2, "b")))
        val result = stream.none { it.value > 1 }

        assertFalse(result)
    }

    @Test
    fun testFirstOperation() {
        val stream = createTestStream(listOf(TestData(1, "a"), TestData(2, "b")))
        val first = stream.first()

        assertNotNull(first)
        assertEquals(1, first.value)
    }

    @Test
    fun testFirstEmptyThrowsException() {
        val stream = createTestStream(emptyList<TestData>())
        assertFailsWith<NoSuchElementException> {
            stream.first()
        }
    }

    @Test
    fun testFirstOrNullOperation() {
        val stream = createTestStream(listOf(TestData(1, "a"), TestData(2, "b")))
        val first = stream.firstOrNull { it.value > 1 }
        val notFound = stream.firstOrNull { it.value > 3 }

        assertNotNull(first)
        assertEquals(2, first.value)
        assertNull(notFound)
    }

    @Test
    fun testFirstOrNullEmpty() {
        val stream = createTestStream(emptyList<TestData>())
        val result = stream.firstOrNull()

        assertNull(result)
    }

    @Test
    fun testLastOperation() {
        val stream = createTestStream(listOf(TestData(1, "a"), TestData(2, "b")))
        val last = stream.last()

        assertNotNull(last)
        assertEquals(2, last.value)
    }

    @Test
    fun testLastEmptyThrowsException() {
        val stream = createTestStream(emptyList<TestData>())
        assertFailsWith<NoSuchElementException> {
            stream.last()
        }
    }

    @Test
    fun testLastOrNullOperation() {
        val stream = createTestStream(listOf(TestData(1, "a"), TestData(2, "b"), TestData(3, "c")))
        val last = stream.lastOrNull { it.value < 3 }
        val notFound = stream.lastOrNull { it.value > 3 }

        assertNotNull(last)
        assertEquals(2, last.value)
        assertNull(notFound)
    }

    @Test
    fun testLastOrNullEmpty() {
        val stream = createTestStream(emptyList<TestData>())
        val result = stream.lastOrNull()

        assertNull(result)
    }

    @Test
    fun testToListOperation() {
        val stream = createTestStream(listOf(TestData(1, "a"), TestData(2, "b")))
        val list = stream.toList()

        assertEquals(2, list.size)
    }

    @Test
    fun testToListEmpty() {
        val stream = createTestStream(emptyList<TestData>())
        val list = stream.toList()

        assertEquals(0, list.size)
        assertTrue(list.isEmpty())
    }

    @Test
    fun testToSetOperation() {
        val stream = createTestStream(
            listOf(
                TestData(1, "a"),
                TestData(2, "b"),
                TestData(1, "a")
            )
        )
        val set = stream.toSet()

        assertEquals(2, set.size)
    }

    @Test
    fun testToSetEmpty() {
        val stream = createTestStream(emptyList<TestData>())
        val set = stream.toSet()

        assertEquals(0, set.size)
        assertTrue(set.isEmpty())
    }

    @Test
    fun testGroupByOperation() {
        val stream = createTestStream(
            listOf(
                TestData(1, "a"),
                TestData(2, "b"),
                TestData(1, "c"),
                TestData(3, "d")
            )
        )
        val grouped = stream.groupBy { it.value }

        assertEquals(3, grouped.size)
        assertTrue(grouped.containsKey(1))
        assertTrue(grouped.containsKey(2))
        assertTrue(grouped.containsKey(3))
        assertEquals(2, grouped[1]?.size)
        assertEquals(1, grouped[2]?.size)
        assertEquals(1, grouped[3]?.size)
    }

    @Test
    fun testGroupByComplexKeys() {
        val stream = createTestStream(
            listOf(
                TestData(1, "a"),
                TestData(2, "bb"),
                TestData(3, "ccc"),
                TestData(4, "bb")
            )
        )
        val grouped = stream.groupBy { it.name.length }

        assertEquals(3, grouped.size)
        assertEquals(1, grouped[1]?.size) // "a"
        assertEquals(2, grouped[2]?.size) // "bb", "bb"
        assertEquals(1, grouped[3]?.size) // "ccc"
    }

    @Test
    fun testGroupByEmpty() {
        val stream = createTestStream(emptyList<TestData>())
        val grouped = stream.groupBy { it.value }

        assertEquals(0, grouped.size)
        assertTrue(grouped.isEmpty())
    }

    @Test
    fun testJoinToStringOperation() {
        val stream = createTestStream(listOf(TestData(1, "a"), TestData(2, "b"), TestData(3, "c")))
        val result = stream.map { it.value }.joinToString(separator = ":", prefix = "[", postfix = "]")

        assertEquals("[1:2:3]", result)
    }

    @Test
    fun testJoinToStringLimit() {
        val stream = createTestStream(listOf(TestData(1, "a"), TestData(2, "b"), TestData(3, "c"), TestData(4, "d")))
        val result = stream.map { it.value }
            .joinToString(separator = ", ", prefix = "[", postfix = "]", limit = 2, truncated = "...")

        assertEquals("[1, 2, ...]", result)
    }

    @Test
    fun testJoinToStringEmpty() {
        val stream = createTestStream(emptyList<TestData>())
        val result = stream.map { it.value }
            .joinToString(separator = ", ", prefix = "[", postfix = "]")

        assertEquals("[]", result)
    }

    @Test
    fun testJoinToStringSingleElement() {
        val stream = createTestStream(listOf(TestData(42, "test")))
        val result = stream.map { it.value }
            .joinToString(separator = ", ", prefix = "[", postfix = "]")

        assertEquals("[42]", result)
    }

    @Test
    fun testJoinToStringNoSeparator() {
        val stream = createTestStream(listOf(TestData(1, "a"), TestData(2, "b"), TestData(3, "c")))
        val result = stream.map { it.value }
            .joinToString(separator = "", prefix = "", postfix = "")

        assertEquals("123", result)
    }

    @Test
    fun testCloseMethod() {
        val stream = createTestStream(listOf(TestData(1, "a"), TestData(2, "b")))
        // Just verify that close() can be called without throwing
        stream.close()
    }

    @Test
    fun testChainedOperations() {
        val stream = createTestStream(
            listOf(
                TestData(1, "a"),
                TestData(2, "b"),
                TestData(3, "c"),
                TestData(4, "d"),
                TestData(5, "e")
            )
        )
        
        val result = stream
            .filter { it.value % 2 == 0 }
            .map { it.value * 10 }
            .take(2)
            .toList()

        assertEquals(2, result.size)
        assertEquals(20, result[0])
        assertEquals(40, result[1])
    }

    @Test
    fun testComplexChainedOperations() {
        val stream = createTestStream(
            listOf(
                TestData(1, "apple"),
                TestData(2, "banana"),
                TestData(3, "cherry"),
                TestData(4, "date"),
                TestData(5, "elderberry"),
                TestData(6, "fig")
            )
        )
        
        val result = stream
            .filter { it.value > 2 }
            .filter { it.name.length <= 6 }
            .mapNotNull { if (it.value % 2 == 1) it.name.uppercase() else null }
            .distinct()
            .joinToString(separator = " | ")

        assertEquals("CHERRY", result) // Only cherry matches all conditions (length is 6, so use <= 6)
    }
}