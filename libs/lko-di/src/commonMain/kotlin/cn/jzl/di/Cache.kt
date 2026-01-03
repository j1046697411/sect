package cn.jzl.di

import org.kodein.type.TypeToken
import org.kodein.type.erased
import org.kodein.type.generic
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

inline fun <reified V : Any> cacheProperty(tag: Any? = null): ReadOnlyProperty<DIAware, V> {
    return CacheReadOnlyProperty(generic(), tag)
}

@PublishedApi
internal class CacheReadOnlyProperty<V : Any>(val typeToken: TypeToken<V>, val tag: Any? = null) : ReadOnlyProperty<DIAware, V> {

    private var value: V? = null

    override fun getValue(thisRef: DIAware, property: KProperty<*>): V {
        return value ?: run {
            val contextType = erased(thisRef::class) as TypeToken<DIAware>
            val context = DIContext(contextType, thisRef)
            val directDI = thisRef.di.on(context)
            val newValue = directDI[TypeToken.Unit, typeToken, tag].invoke(Unit)
            this.value = newValue
            newValue
        }
    }
}