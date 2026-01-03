package cn.jzl.ecs

import cn.jzl.ecs.addon.Phase

interface Pipeline {

    fun runOnOrAfter(phase: Phase, task: WorldOwner.() -> Unit)

    fun runStartupTasks()
}