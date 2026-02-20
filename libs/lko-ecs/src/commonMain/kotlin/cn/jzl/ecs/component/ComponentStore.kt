package cn.jzl.ecs.component

/**
 * 组件存储接口，定义组件数据的存储和访问方式
 *
 * ComponentStore 是 ECS 框架中组件存储的抽象接口，
 * 不同的实现可以提供不同的存储策略（如数组、稀疏集等）。
 *
 * ## 实现类型
 * - [GeneralComponentStore]: 通用组件存储，适用于任意类型
 * - [IntComponentStore]: 特化的 Int 类型存储
 * - [LongComponentStore]: 特化的 Long 类型存储
 * - [FloatComponentStore]: 特化的 Float 类型存储
 * - [DoubleComponentStore]: 特化的 Double 类型存储
 *
 * ## 索引说明
 * 索引对应实体在 Archetype 表中的位置，而非实体 ID。
 * 当实体在 Archetype 之间移动时，索引会改变。
 *
 * @param C 存储的组件类型
 * @see ComponentStoreFactory 用于创建存储实例
 */
interface ComponentStore<C> {

    /**
     * 存储中的元素数量
     */
    val size: Int

    /**
     * 获取指定索引处的组件
     *
     * @param index 组件索引
     * @return 组件实例
     */
    operator fun get(index: Int): C

    /**
     * 设置指定索引处的组件
     *
     * @param index 组件索引
     * @param value 要设置的组件值
     */
    operator fun set(index: Int, value: C)

    /**
     * 添加组件到存储末尾
     *
     * @param value 要添加的组件值
     */
    fun add(value: C)

    /**
     * 移除指定索引处的组件
     *
     * @param index 要移除的组件索引
     * @return 被移除的组件值
     */
    fun removeAt(index: Int): C
}
