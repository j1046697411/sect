package cn.jzl.ecs.serialization.addon

import cn.jzl.di.instance
import cn.jzl.ecs.World

/**
 * 获取 World 的序列化模块扩展属性
 *
 * 通过 DI 获取已安装的 SerializationModule
 */
val World.serialization: SerializationModule
    get() = di.instance<SerializationModule>().invoke()
