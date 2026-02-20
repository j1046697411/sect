package cn.jzl.ecs.archetype

import cn.jzl.core.bits.fromLowHigh
import cn.jzl.core.bits.high
import cn.jzl.core.bits.low
import cn.jzl.ecs.entity.Entity
import kotlin.jvm.JvmInline

/**
 * 组件索引内联值类
 *
 * ComponentIndex 用于标识组件在原型表中的位置。
 * 它包含两个信息：实体（可能是预制体）和组件在表中的索引。
 *
 * ## 结构
 * - 低 32 位：实体 ID（可能是预制体实体或 ENTITY_INVALID）
 * - 高 32 位：组件在原型表中的索引
 *
 * ## 使用场景
 * - 快速定位组件数据在存储中的位置
 * - 支持预制体继承的组件查找
 * - 在查询中缓存组件访问位置
 *
 * @param data 内部存储的 Long 值，包含实体和索引
 * @property entity 组件所属的实体（可能是预制体）
 * @property index 组件在原型表中的索引
 */
@JvmInline
value class ComponentIndex private constructor(val data: Long) {
    companion object {
        /**
         * 创建组件索引
         *
         * @param prefabEntity 预制体实体（如果不是预制体组件则为 ENTITY_INVALID）
         * @param index 组件在原型表中的索引
         * @return 组件索引实例
         */
        operator fun invoke(
            prefabEntity: Entity,
            index: Int
        ): ComponentIndex = ComponentIndex(Long.fromLowHigh(prefabEntity.data, index))
    }
}

/**
 * 获取组件索引中的实体
 */
val ComponentIndex.entity: Entity get() = Entity(data.low)

/**
 * 获取组件索引中的索引值
 */
val ComponentIndex.index: Int get() = data.high
