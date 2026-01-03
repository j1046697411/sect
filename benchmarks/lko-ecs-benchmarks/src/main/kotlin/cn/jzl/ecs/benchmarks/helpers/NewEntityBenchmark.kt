package cn.jzl.ecs.benchmarks.helpers

import cn.jzl.ecs.entity
import cn.jzl.ecs.entity.addComponent
import kotlinx.benchmark.Benchmark
import kotlinx.benchmark.Scope
import kotlinx.benchmark.Setup
import kotlinx.benchmark.State
import kotlin.time.measureTime

@State(Scope.Benchmark)
class NewEntityBenchmark : ECSBenchmark() {

    @Setup
    fun setLoggingLevel() {
    }

    @Benchmark
    fun create1MilEntitiesWith0Components() {
        repeat(ONE_MILLION) {
            world.entity {  }
        }
    }
    @Benchmark
    fun create1MilEntitiesWith1ComponentNoEvent() {
        repeat(ONE_MILLION) {index ->
            world.entity {
                it.addComponent(Component1(index))
            }
        }
    }

    @Benchmark
    fun create1MilEntitiesWith6Components() {
        repeat(20000) {index ->
            world.entity {
                it.addComponent(Component1(index))
                it.addComponent(Component2(index))
                it.addComponent(Component3(index))
                it.addComponent(Component4(index))
                it.addComponent(Component5(index))
                it.addComponent(Component6(index))
            }
        }
    }
}

fun main() {
    repeat(10) {
        val test = measureTime {
            val benchmark = NewEntityBenchmark()
            benchmark.create1MilEntitiesWith6Components()
        }
        println("create 1 million entities with 0 components cost $test")
    }
}