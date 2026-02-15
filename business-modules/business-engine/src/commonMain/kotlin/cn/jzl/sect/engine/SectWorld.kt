package cn.jzl.sect.engine

import cn.jzl.ecs.*
import cn.jzl.ecs.World
import cn.jzl.ecs.addon.components
import cn.jzl.ecs.addon.createAddon
import cn.jzl.ecs.component.componentId
import cn.jzl.ecs.component.tag
import cn.jzl.ecs.entity.*
import cn.jzl.ecs.query.count
import cn.jzl.ecs.world
import cn.jzl.sect.core.components.Age
import cn.jzl.sect.core.components.CultivationProgress
import cn.jzl.sect.core.components.CultivationRealm
import cn.jzl.sect.core.components.EntityName
import cn.jzl.sect.engine.systems.DemoSystem
import cn.jzl.sect.engine.systems.DiscipleContext
import cn.jzl.sect.core.tags.Alive
import cn.jzl.sect.core.tags.Cultivating
import cn.jzl.sect.core.tags.Idle
import kotlin.random.Random

object SectWorld {
    lateinit var world: World
        private set
    
    lateinit var demoSystem: DemoSystem
        private set
    
    private val demoAddon = createAddon<Unit>("demo") {
        components {
            // 注册组件
            world.componentId<EntityName>()
            world.componentId<Age>()
            world.componentId<CultivationProgress>()
            world.componentId<CultivationRealm>()
            
            // 注册标签
            world.componentId<Alive> { it.tag() }
            world.componentId<Idle> { it.tag() }
            world.componentId<Cultivating> { it.tag() }
        }
    }
    
    fun initialize() {
        world = world {
            install(demoAddon)
        }
        
        demoSystem = DemoSystem(world)
        
        // 创建初始弟子
        createInitialDisciples()
        
        // 验证实体创建
        val allEntities = world.query { DiscipleContext(this) }
        println("✅ 创建了 ${allEntities.count()} 个弟子实体")
    }
    
    private fun createInitialDisciples() {
        val familyNames = listOf("张", "李", "王", "赵", "刘")
        val givenNames = listOf("三", "四", "文", "武", "明")
        
        repeat(5) { index ->
            val name = familyNames.random() + givenNames.random()
            val age = Random.nextInt(16, 26)
            val shouldCultivate = Random.nextBoolean()
            
            world.entity {
                it.addComponent(EntityName("$name-${index + 1}"))
                it.addComponent(Age(age))
                it.addComponent<CultivationRealm>(CultivationRealm.QiRefining1)
                it.addComponent(CultivationProgress(Random.nextFloat() * 50f))
                it.addTag<Alive>()
                
                if (shouldCultivate) {
                    it.addTag<Cultivating>()
                } else {
                    it.addTag<Idle>()
                }
            }
        }
    }
    
    fun update(deltaTime: Float) {
        demoSystem.update(deltaTime)
    }
}
