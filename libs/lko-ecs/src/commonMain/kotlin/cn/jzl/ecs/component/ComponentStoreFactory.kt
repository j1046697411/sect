package cn.jzl.ecs.component

import cn.jzl.ecs.relation.Relation

/**
 * 组件存储工厂函数式接口
 *
 * ComponentStoreFactory 负责根据关系创建对应的组件存储实例。
 * 不同的组件类型可以使用不同的存储策略，通过注册自定义工厂实现。
 *
 * ## 使用场景
 * - 为特定组件类型创建优化的存储（如 IntArray、FloatArray）
 * - 自定义组件序列化/反序列化逻辑
 * - 实现特殊的组件存储行为
 *
 * ## 默认实现
 * 默认使用 [GeneralComponentStore]，适用于任意类型的组件。
 *
 * ## 使用示例
 * ```kotlin
 * // 注册自定义存储工厂
 * world.componentService.configureStoreType(
 *     componentId,
 *     ComponentStoreFactory { relation ->
 *         IntComponentStore()  // 使用特化的 Int 存储
 *     }
 * )
 * ```
 *
 * @param C 存储的组件类型
 * @see ComponentService.configureStoreType
 */
fun interface ComponentStoreFactory<C> {
    /**
     * 创建组件存储实例
     *
     * @param relation 关系对象，包含组件类型信息
     * @return 组件存储实例
     */
    fun create(relation: Relation): ComponentStore<C>

    /**
     * 默认组件存储工厂
     *
     * 使用 [GeneralComponentStore] 作为默认实现
     */
    companion object : ComponentStoreFactory<Any> {
        override fun create(relation: Relation): ComponentStore<Any> = GeneralComponentStore()
    }
}
