package cn.jzl.sect.core.relation

/**
 * 关系类型枚举
 * 定义角色之间可能存在的各种关系类型
 *
 * @property displayName 关系类型的显示名称
 */
enum class RelationshipType(val displayName: String) {
    MASTER_APPRENTICE("师徒"),   // 师父与徒弟的关系
    PEER("同门"),               // 同门师兄弟关系
    COMPETITOR("竞争"),         // 竞争关系
    COOPERATOR("合作"),         // 合作关系
    FRIENDLY("友好"),           // 友好关系
    HOSTILE("敌对")             // 敌对关系
}
