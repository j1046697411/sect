package cn.jzl.ecs.query

/**
 * 访问器标记接口
 *
 * Accessor 是所有组件访问器的基接口，用于在 [EntityQueryContext] 中
 * 定义如何访问实体上的组件数据。
 *
 * ## 实现类型
 * - [ReadOnlyAccessor]: 只读访问器
 * - [ReadWriteAccessor]: 读写访问器
 * - [CachedAccessor]: 带缓存的访问器
 * - [RelationAccessor]: 关系访问器
 *
 * ## 使用方式
 * 通常不直接使用此接口，而是通过 [EntityQueryContext] 中的
 * [component] 委托属性来创建访问器。
 *
 * @see EntityQueryContext.component
 */
interface Accessor
