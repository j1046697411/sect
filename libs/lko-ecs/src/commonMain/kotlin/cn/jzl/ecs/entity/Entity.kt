package cn.jzl.ecs.entity

import cn.jzl.core.bits.extract08
import cn.jzl.core.bits.extract24
import cn.jzl.core.bits.insert08
import kotlin.jvm.JvmInline

/**
 * 实体标识符，ECS 中游戏对象的唯一标识
 *
 * Entity 是一个轻量级的值类，内部使用 Int 存储实体 ID 和版本号。
 * 实体本身不包含任何数据，数据存储在关联的组件中。
 *
 * ## 实体结构
 * - 低 24 位：实体 ID（最大支持 16,777,215 个实体）
 * - 高 8 位：版本号（用于检测过期实体引用）
 *
 * ## 使用示例
 * ```kotlin
 * // 创建实体
 * val player = world.entity {
 *     it.addComponent(Name("Player"))
 *     it.addComponent(Health(100, 100))
 * }
 *
 * // 获取实体信息
 * println(player.id)      // 实体 ID
 * println(player.version) // 实体版本
 * ```
 *
 * @property data 内部存储的 Int 值，包含 ID 和版本号
 */
@JvmInline
value class Entity @PublishedApi internal constructor(val data: Int) {

    override fun toString(): String = "Entity(id=${id}, version=${version})"

    companion object {
        /**
         * 无效实体常量
         */
        val ENTITY_INVALID = Entity(-1)

        /**
         * 通过 ID 和版本号创建实体
         *
         * @param id 实体 ID（0-16,777,215）
         * @param version 版本号（0-255）
         * @return 实体实例
         */
        operator fun invoke(id: Int, version: Int): Entity = Entity(id.insert08(version, 24))
    }
}

/**
 * 获取实体的 ID（低 24 位）
 */
val Entity.id: Int get() = data.extract24(0)

/**
 * 获取实体的版本号（高 8 位）
 */
val Entity.version: Int get() = data.extract08(24)
