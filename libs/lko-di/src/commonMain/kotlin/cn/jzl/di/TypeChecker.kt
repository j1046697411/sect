package cn.jzl.di

import org.kodein.type.TypeToken
import kotlin.jvm.JvmInline

internal sealed interface TypeChecker {

    val type: TypeToken<*>

    fun check(typeToken: TypeToken<*>): Boolean

    @JvmInline
    value class Up(override val type: TypeToken<*>) : TypeChecker {
        override fun check(typeToken: TypeToken<*>): Boolean {
            if (type.isWildcard() && type.isGeneric()) {
                return type.getRaw().isAssignableFrom(typeToken.getRaw())
            }
            return type.isAssignableFrom(typeToken)
        }

        override fun toString(): String {
            return "Up[${type.simpleDispString()}]"
        }
    }

    @JvmInline
    value class Down(override val type: TypeToken<*>) : TypeChecker {
        override fun check(typeToken: TypeToken<*>): Boolean {
            if (type.isWildcard() && type.isGeneric()) {
                return typeToken.getRaw().isAssignableFrom(type.getRaw())
            }
            return typeToken.isAssignableFrom(type)
        }

        override fun toString(): String {
            return "Down[${type.simpleDispString()}]"
        }
    }
}