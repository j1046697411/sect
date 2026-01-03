package cn.jzl.ecs.benchmarks.helpers

import cn.jzl.ecs.World
import cn.jzl.ecs.addon.WorldSetup
import cn.jzl.ecs.addon.components
import cn.jzl.ecs.addon.createAddon
import cn.jzl.ecs.component.componentId
import cn.jzl.ecs.entity.EntityRelationContext
import cn.jzl.ecs.world

private val benchmarkAddon = createAddon("BenchmarkAddon") {
    components {
        world.componentId<Component1>()
        world.componentId<Component2>()
        world.componentId<Component3>()
        world.componentId<Component4>()
        world.componentId<Component5>()
        world.componentId<Component6>()
    }
}

abstract class ECSBenchmark : EntityRelationContext {
    override val world: World by lazy {
        world {
            install(benchmarkAddon)
            setup()
        }
    }

    protected open fun WorldSetup.setup(): Unit = Unit

    companion object {
        const val ONE_MILLION = 1_000_000
        const val TEN_MILLION = 10_000_000
    }
}

