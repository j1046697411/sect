@file:Suppress("NOTHING_TO_INLINE")

package cn.jzl.ecs.query

fun QueryStreamScope.abort(): Nothing = throw AbortQueryException(this)

@PublishedApi
internal inline fun <T, R> QueryStream<T>.unsafeFlow(
    crossinline block: (QueryCollector<R>) -> Unit
): QueryStream<R> = object : QueryStream<R> {
    override fun collect(collector: QueryCollector<R>): Unit = block(collector)
    override fun close(): Unit = this@unsafeFlow.close()
}

@PublishedApi
internal inline fun <T, R> QueryStream<T>.transform(
    crossinline transform: QueryCollector<R>.(QueryStreamScope, T) -> Unit
): QueryStream<R> = unsafeFlow { collector -> collect { collector.transform(this, it) } }

// 转换操作
inline fun <T, R> QueryStream<T>.map(
    crossinline transform: QueryStreamScope.(T) -> R
): QueryStream<R> = transform { scope, value -> scope.emit(scope.transform(value)) }

inline fun <T, R> QueryStream<T>.mapNotNull(
    crossinline transform: QueryStreamScope.(T) -> R?
): QueryStream<R> = transform { scope, value -> scope.emit(scope.transform(value) ?: return@transform) }

inline fun <T, R> QueryStream<T>.flatMap(
    crossinline transform: QueryStreamScope.(T) -> QueryStream<R>
): QueryStream<R> = transform { scope, value -> scope.transform(value).forEach { scope.emit(it) } }

// 过滤操作
inline fun <T> QueryStream<T>.filter(crossinline predicate: QueryStreamScope.(T) -> Boolean): QueryStream<T> =
    transform { scope, value ->
        if (scope.predicate(value)) scope.emit(value)
    }

inline fun <T> QueryStream<T>.filterNot(crossinline predicate: QueryStreamScope.(T) -> Boolean): QueryStream<T> =
    transform { scope, value ->
        if (!scope.predicate(value)) scope.emit(value)
    }

inline fun <T> QueryStream<T>.take(n: Int): QueryStream<T> = unsafeFlow { collector ->
    var count = 0
    collect { value ->
        if (count < n) {
            collector.run { emit(value) }
            count++
            if (count >= n) abort()
        }
    }
}

inline fun <T> QueryStream<T>.drop(n: Int): QueryStream<T> = unsafeFlow { collector ->
    var count = 0
    collect { value ->
        if (count >= n) {
            collector.run { emit(value) }
        } else {
            count++
        }
    }
}

inline fun <T> QueryStream<T>.takeWhile(
    crossinline predicate: QueryStreamScope.(T) -> Boolean
): QueryStream<T> = unsafeFlow { collector ->
    collect { value ->
        if (predicate(value)) {
            collector.run { emit(value) }
        } else {
            abort()
        }
    }
}

inline fun <T> QueryStream<T>.dropWhile(
    crossinline predicate: QueryStreamScope.(T) -> Boolean
): QueryStream<T> = unsafeFlow { collector ->
    var dropping = true
    collect { value ->
        if (!dropping || !predicate(value)) {
            dropping = false
            collector.run { emit(value) }
        }
    }
}

inline fun <T> QueryStream<T>.distinct(): QueryStream<T> = unsafeFlow { collector ->
    val seen = mutableSetOf<T>()
    collect { value ->
        if (value !in seen) {
            seen.add(value)
            collector.run { emit(value) }
        }
    }
}

inline fun <T, K> QueryStream<T>.distinctBy(
    crossinline selector: QueryStreamScope.(T) -> K
): QueryStream<T> = unsafeFlow { collector ->
    val seen = mutableSetOf<K>()
    collect { value ->
        val key = selector(value)
        if (key !in seen) {
            seen.add(key)
            collector.run { emit(value) }
        }
    }
}

// 遍历操作
inline fun <T> QueryStream<T>.onEach(
    crossinline action: QueryStreamScope.(T) -> Unit
): QueryStream<T> = unsafeFlow { collector ->
    collect { value ->
        action(value)
        collector.run { emit(value) }
    }
}

inline fun <T> QueryStream<T>.forEach(
    crossinline action: QueryStreamScope.(T) -> Unit
): Unit = collect { action(it) }

// 聚合操作
inline fun <T> QueryStream<T>.count(): Int {
    var count = 0
    forEach { count++ }
    return count
}

inline fun <T, R> QueryStream<T>.fold(initial: R, crossinline operation: (acc: R, T) -> R): R {
    var accumulator = initial
    forEach { accumulator = operation(accumulator, it) }
    return accumulator
}

inline fun <T> QueryStream<T>.reduce(crossinline operation: (acc: T, T) -> T): T {
    var accumulator: T? = null
    forEach {
        if (accumulator == null) {
            accumulator = it
        } else {
            accumulator = operation(accumulator!!, it)
        }
    }
    return accumulator ?: throw NoSuchElementException("Reduce of empty stream")
}

inline fun <T> QueryStream<T>.any(crossinline predicate: QueryStreamScope.(T) -> Boolean): Boolean {
    var result = false
    collect { value ->
        if (predicate(value)) {
            result = true
            abort()
        }
    }
    return result
}

inline fun <T> QueryStream<T>.all(crossinline predicate: QueryStreamScope.(T) -> Boolean): Boolean {
    var result = true
    collect { value ->
        if (!predicate(value)) {
            result = false
            abort()
        }
    }
    return result
}

inline fun <T> QueryStream<T>.none(crossinline predicate: QueryStreamScope.(T) -> Boolean): Boolean {
    var result = true
    collect { value ->
        if (predicate(value)) {
            result = false
            abort()
        }
    }
    return result
}

// 查找操作
inline fun <T> QueryStream<T>.first(): T = firstOrNull() ?: throw NoSuchElementException("First of empty stream")

inline fun <T> QueryStream<T>.firstOrNull(crossinline predicate: QueryStreamScope.(T) -> Boolean = { true }): T? {
    var result: T? = null
    collect { value ->
        if (predicate(value)) {
            result = value
            abort()
        }
    }
    return result
}

inline fun <T> QueryStream<T>.last(): T = lastOrNull() ?: throw NoSuchElementException("Last of empty stream")

inline fun <T> QueryStream<T>.lastOrNull(crossinline predicate: QueryStreamScope.(T) -> Boolean = { true }): T? {
    var result: T? = null
    collect { value ->
        if (predicate(value)) {
            result = value
        }
    }
    return result
}

// 转换集合
inline fun <T> QueryStream<T>.toList(): List<T> = toCollection(mutableListOf())
inline fun <T> QueryStream<T>.toSet(): Set<T> = toCollection(mutableSetOf())

inline fun <T, C : MutableCollection<in T>> QueryStream<T>.toCollection(destination: C): C {
    forEach { destination.add(it) }
    return destination
}

inline fun <T, K> QueryStream<T>.groupBy(crossinline keySelector: QueryStreamScope.(T) -> K): Map<K, List<T>> {
    val result = mutableMapOf<K, MutableList<T>>()
    forEach {
        val key = keySelector(it)
        result.getOrPut(key) { mutableListOf() }.add(it)
    }
    return result
}

// 字符串操作
inline fun <T> QueryStream<T>.joinToString(
    separator: CharSequence = ", ",
    prefix: CharSequence = "",
    postfix: CharSequence = "",
    limit: Int = -1,
    truncated: CharSequence = "...",
    crossinline transform: QueryStreamScope.(T) -> CharSequence = { it.toString() }
): String = buildString {
    append(prefix)
    var count = 0
    collect { value ->
        if (count > 0) append(separator)
        if (limit !in 0..count) {
            append(transform(value))
            count++
        } else {
            append(truncated)
            abort()
        }
    }
    append(postfix)
}
