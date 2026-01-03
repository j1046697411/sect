package cn.jzl.ecs.observer

import cn.jzl.ecs.query.EntityQueryContext
import cn.jzl.ecs.query.Query

interface ExecutableObserver<Context> {
    fun filter(vararg query: Query<out EntityQueryContext>): ExecutableObserver<Context>

    fun exec(handle: Context.()-> Unit) : Observer
}