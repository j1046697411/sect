package cn.jzl.sect.demo.services

import cn.jzl.ecs.*
import cn.jzl.ecs.World
import cn.jzl.ecs.entity.Entity
import cn.jzl.ecs.entity.EntityRelationContext
import cn.jzl.ecs.entity.*
import cn.jzl.ecs.family.component
import cn.jzl.ecs.query.EntityQueryContext
import cn.jzl.ecs.query.*
import cn.jzl.ecs.family.FamilyBuilder
import cn.jzl.sect.demo.components.Age
import cn.jzl.sect.demo.components.CultivationProgress
import cn.jzl.sect.demo.components.CultivationRealm
import cn.jzl.sect.demo.components.EntityName
import cn.jzl.sect.demo.tags.Cultivating
import kotlin.random.Random

// 时间数据类
data class GameTime(
    val year: Int = 1,
    val month: Int = 1,
    val day: Int = 1,
    val hour: Int = 6,
    val totalTicks: Long = 0
) {
    fun toDisplayString(): String {
        return "修真纪元${year}年 ${month}月${day}日 ${hour}时"
    }
}

// 演示系统
class DemoSystem(override val world: World) : EntityRelationContext {
    
    private var tickAccumulator = 0f
    private val ticksPerHour = 60f
    private var currentTime = GameTime()

    private val cultivators by lazy { world.query { CultivatingContext(this) } }
    
    fun update(deltaTime: Float) {
        tickAccumulator += deltaTime * ticksPerHour
        
        if (tickAccumulator >= 1f) {
            val ticks = tickAccumulator.toInt()
            tickAccumulator -= ticks
            
            advanceTime(ticks)
            processCultivation(ticks)
        }
    }
    
    private fun advanceTime(ticks: Int) {
        var newTotal = currentTime.totalTicks + ticks
        var newYear = currentTime.year
        var newMonth = currentTime.month
        var newDay = currentTime.day
        var newHour = currentTime.hour + ticks
        
        while (newHour >= 24) {
            newHour -= 24
            newDay += 1
        }
        while (newDay > 30) {
            newDay -= 30
            newMonth += 1
        }
        while (newMonth > 12) {
            newMonth -= 12
            newYear += 1
        }
        
        currentTime = GameTime(newYear, newMonth, newDay, newHour, newTotal)
    }
    
    private fun processCultivation(ticks: Int) {
        // 使用 FamilyService 查询正在修炼的弟子
        cultivators.forEach { context ->
            val currentProgress = context.progress.percentage
            val increase = Random.nextFloat() * 0.5f * ticks
            val newProgress = (currentProgress + increase).coerceAtMost(100f)
            context.progress = CultivationProgress(newProgress)

            if (newProgress >= 100f) {
                attemptBreakthrough(context.entity, context.realm)
            }
        }
    }
    
    private fun attemptBreakthrough(
        entity: Entity,
        currentRealm: CultivationRealm
    ) {
        val success = Random.nextFloat() < 0.3f
        
        if (success) {
            val nextRealm = when (currentRealm) {
                is CultivationRealm.QiRefining1 -> CultivationRealm.QiRefining5
                is CultivationRealm.QiRefining5 -> CultivationRealm.QiRefining9
                is CultivationRealm.QiRefining9 -> CultivationRealm.Foundation
                else -> currentRealm
            }
            
            entity.editor {
                it.addComponent<CultivationRealm>(nextRealm)
                it.addComponent(CultivationProgress(0f))
            }
            
            val name = entity.getComponent<EntityName>().value
            println("🎉 $name 成功突破至 ${nextRealm.displayName}！")
        } else {
            entity.editor {
                it.addComponent(CultivationProgress(50f))
            }
        }
    }
    
    fun printStatus() {
        println("\n=== ${currentTime.toDisplayString()} ===")
        
        // 使用 FamilyService 查询所有有名字的实体
        val disciples = world.query { DiscipleContext(this) }
        
        if (disciples.count() == 0) {
            println("没有找到弟子")
        } else {
            disciples.forEach { entity ->
                val name = entity.name
                val age = entity.age.years
                val realm = entity.realm
                val progress = entity.progress.percentage ?: 0f
                val hasCultivating = entity.entity.hasTag<Cultivating>()
                val state = if (hasCultivating) "修炼中" else "空闲"
                println("$name | ${age}岁 | $realm | 进度:${"%.1f".format(progress)}% | $state")
            }
        }
    }
}

// 查询上下文（保留但不使用）
class DiscipleContext(world: World) : EntityQueryContext(world) {
    val name by component<EntityName>()
    val age by component<Age>()
    val realm by component<CultivationRealm>()
    val progress by component<CultivationProgress>()
}

class CultivatingContext(world: World) : EntityQueryContext(world) {
    var progress by component<CultivationProgress>()
    val realm by component<CultivationRealm>()
    
    override fun FamilyBuilder.configure() {
        component<Cultivating>()
    }
}
