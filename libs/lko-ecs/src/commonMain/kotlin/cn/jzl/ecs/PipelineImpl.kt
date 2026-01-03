package cn.jzl.ecs

import cn.jzl.ecs.addon.Phase

class PipelineImpl(override val world: World) : Pipeline, WorldOwner {
    private val tasks = mutableMapOf<Phase, MutableList<WorldOwner.() -> Unit>>()

    override fun runOnOrAfter(phase: Phase, task: WorldOwner.() -> Unit) {
        val tasks = tasks.getOrPut(phase) { mutableListOf() }
        tasks.add(task)
    }

    override fun runStartupTasks() {
        Phase.entries.forEach {
            val tasks = this.tasks[it] ?: return@forEach
            for (task in tasks) task()
        }
    }
}