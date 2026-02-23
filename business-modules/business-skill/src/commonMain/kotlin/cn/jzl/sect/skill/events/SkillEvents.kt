package cn.jzl.sect.skill.events

import cn.jzl.sect.skill.components.Skill

data class SkillLearnedEvent(
    val discipleId: Long,
    val skill: Skill,
    val proficiencyLevel: Int
)

data class SkillInheritedEvent(
    val masterId: Long,
    val apprenticeId: Long,
    val skill: Skill
)
