package cn.jzl.sect.core.relation

import kotlin.math.max
import kotlin.math.min

/**
 * 关系组件
 * 存储两个角色之间的关系信息
 *
 * @property sourceId 关系发起者ID
 * @property targetId 关系目标ID
 * @property type 关系类型
 * @property level 关系等级(0-100)，数值越高关系越好
 * @property establishedTime 关系建立时间戳
 */
data class Relationship(
    val sourceId: Long = 0L,
    val targetId: Long = 0L,
    val type: RelationshipType = RelationshipType.FRIENDLY,
    val level: Int = 50,
    val establishedTime: Long = 0L
) {

    /**
     * 判断是否为正面关系类型
     * 正面关系包括：师徒、同门、合作、友好
     *
     * @return 如果是正面关系返回true
     */
    fun isPositive(): Boolean {
        return type == RelationshipType.MASTER_APPRENTICE ||
               type == RelationshipType.PEER ||
               type == RelationshipType.COOPERATOR ||
               type == RelationshipType.FRIENDLY
    }

    /**
     * 判断是否为负面关系类型
     * 负面关系包括：竞争、敌对
     *
     * @return 如果是负面关系返回true
     */
    fun isNegative(): Boolean {
        return type == RelationshipType.COMPETITOR ||
               type == RelationshipType.HOSTILE
    }

    /**
     * 获取关系效果加成值
     * 根据关系等级计算加成百分比
     *
     * @return 加成值(等级/5)
     */
    fun getEffectBonus(): Int {
        return level / 5
    }

    /**
     * 改善关系
     * 增加关系等级，最高不超过100
     *
     * @param amount 增加的等级数值
     * @return 新的Relationship实例
     */
    fun improve(amount: Int): Relationship {
        return copy(level = min(100, level + amount))
    }

    /**
     * 恶化关系
     * 降低关系等级，最低不低于0
     *
     * @param amount 降低的等级数值
     * @return 新的Relationship实例
     */
    fun worsen(amount: Int): Relationship {
        return copy(level = max(0, level - amount))
    }
}
