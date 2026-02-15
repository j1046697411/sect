package cn.jzl.sect.core.tags

// 生命周期标签 - 使用 sealed class
sealed class Alive
sealed class Dead

// 行为状态标签
sealed class Idle
sealed class Cultivating
sealed class Working
