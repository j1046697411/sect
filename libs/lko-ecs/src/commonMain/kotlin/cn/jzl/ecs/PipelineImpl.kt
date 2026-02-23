package cn.jzl.ecs

import cn.jzl.di.allInstance
import cn.jzl.ecs.addon.Phase
import kotlin.time.Duration

class PipelineImpl(override val world: World) : Pipeline, WorldOwner {
    private val tasks = mutableMapOf<Phase, MutableList<WorldOwner.() -> Unit>>()
    private val updaters: List<Updatable>

    init {
        val updaters by world.di.allInstance<Updatable>()
        this.updaters = updaters.toList()
    }


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

    override fun update(delta: Duration) {
        updaters.forEach { it.update(delta) }
    }
}