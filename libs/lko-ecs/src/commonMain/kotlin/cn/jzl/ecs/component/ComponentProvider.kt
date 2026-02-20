package cn.jzl.ecs.component

import cn.jzl.ecs.WorldOwner
import kotlin.reflect.KClassifier

/**
 * 组件提供者接口
 *
 * ComponentProvider 定义了获取或注册组件类型实体 ID 的能力。
 * 每个组件类型（通过 KClassifier 标识）在 ECS 世界中对应一个唯一的实体 ID，
 * 用于在关系系统中标识组件类型。
 *
 * ## 使用场景
 * - 获取已注册组件类型的 ID
 * - 自动注册新的组件类型
 * - 在内部系统中管理组件类型映射
 *
 * @see ComponentService 默认实现
 */
interface ComponentProvider : WorldOwner {
    /**
     * 获取或注册组件类型的实体 ID
     *
     * 如果该组件类型已注册，返回对应的实体 ID；
     * 否则自动创建新实体并注册为该组件类型的 ID。
     *
     * @param classifier 组件类型的 KClassifier（通常是 KClass）
     * @return 组件类型对应的实体 ID
     */
    fun getOrRegisterEntityForClass(classifier: KClassifier): ComponentId
}
