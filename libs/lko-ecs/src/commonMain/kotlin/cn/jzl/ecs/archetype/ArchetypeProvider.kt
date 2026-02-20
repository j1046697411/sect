package cn.jzl.ecs.archetype

import cn.jzl.ecs.relation.EntityType

/**
 * 原型提供者接口
 *
 * ArchetypeProvider 定义了获取或创建原型的能力。
 * 实现此接口的类负责管理原型的生命周期，确保相同组件组合的实体共享同一个原型实例。
 *
 * ## 主要功能
 * - 获取或创建指定组件组合的原型
 * - 提供根原型（无任何组件的原型）
 *
 * ## 实现要求
 * - 必须缓存已创建的原型，避免重复创建
 * - 必须保证相同 [EntityType] 返回同一个 [Archetype] 实例
 *
 * @see ArchetypeService 默认实现
 */
interface ArchetypeProvider {

    /**
     * 根原型
     *
     * 没有任何组件的原型，是所有实体的初始原型
     */
    val rootArchetype: Archetype

    /**
     * 获取或创建指定类型的原型
     *
     * 如果该类型的原型已存在，返回缓存的实例；
     * 否则创建新原型并缓存。
     *
     * @param entityType 实体类型（组件组合）
     * @return 原型实例
     */
    fun getArchetype(entityType: EntityType): Archetype
}
