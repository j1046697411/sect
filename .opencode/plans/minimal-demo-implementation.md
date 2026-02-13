# 宗门修真录 - 最小可运行 Demo 实施计划

## 目标
创建一个可运行的最小 demo，演示 ECS 框架的核心功能：
1. World 初始化与配置
2. 创建随机弟子实体
3. 时间流逝系统
4. 修炼进度更新
5. 控制台输出展示

## 技术栈
- **平台**: JVM Desktop (composeApp:jvmMain)
- **架构**: ECS (Entity-Component-System)
- **UI**: 控制台输出（最简形式）
- **预计代码量**: 约 250 行

---

## 文件结构

```
composeApp/src/jvmMain/kotlin/cn/jzl/sect/demo/
├── components/
│   └── BasicInfo.kt          # 基础组件定义
├── tags/
│   └── StatusTags.kt         # 状态标签
├── services/
│   └── DemoSystem.kt         # 演示系统逻辑
├── SectWorld.kt              # World 配置
└── DemoMain.kt               # 主入口
```

---

## 详细实现

### 1. components/BasicInfo.kt

**内容**:
```kotlin
package cn.jzl.sect.demo.components

// 基础信息组件
@JvmInline
value class EntityName(val value: String)

@JvmInline
value class Age(val years: Int)

// 修炼组件
@JvmInline
value class CultivationProgress(val percentage: Float)

// 境界密封类
sealed class CultivationRealm(
    val level: Int,
    val displayName: String
) {
    object QiRefining1 : CultivationRealm(1, "炼气一层")
    object QiRefining5 : CultivationRealm(5, "炼气五层")
    object QiRefining9 : CultivationRealm(9, "炼气九层")
    object Foundation : CultivationRealm(10, "筑基期")
}
```

**说明**:
- 使用 `@JvmInline value class` 节省内存
- `CultivationRealm` 使用密封类表示层次结构

---

### 2. tags/StatusTags.kt

**内容**:
```kotlin
package cn.jzl.sect.demo.tags

// 生命周期标签（使用 object 作为标记）
object Alive
object Dead

// 行为状态标签
object Idle
object Cultivating
object Working
```

**说明**:
- 标签无数据，仅用于标记实体状态
- 使用 object 单例模式

---

### 3. services/DemoSystem.kt

**内容**:
```kotlin
package cn.jzl.sect.demo.services

import cn.jzl.ecs.World
import cn.jzl.ecs.WorldOwner
import cn.jzl.ecs.archetype.FamilyBuilder
import cn.jzl.ecs.query.EntityQueryContext
import cn.jzl.ecs.query.QueryStream
import cn.jzl.ecs.query.forEach
import cn.jzl.sect.demo.components.Age
import cn.jzl.sect.demo.components.CultivationProgress
import cn.jzl.sect.demo.components.CultivationRealm
import cn.jzl.sect.demo.components.EntityName
import cn.jzl.sect.demo.tags.Cultivating
import kotlin.random.Random

// 简化的时间数据类
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
class DemoSystem(override val world: World) : WorldOwner {
    
    private var tickAccumulator = 0f
    private val ticksPerHour = 60f  // 每秒推进1游戏小时
    
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
        val current = world.getSingleton<GameTime>()
        var newTotal = current.totalTicks + ticks
        var newYear = current.year
        var newMonth = current.month
        var newDay = current.day
        var newHour = current.hour + ticks
        
        while (newHour >= 24) {
            newHour -= 24
            newDay++
        }
        while (newDay > 30) {
            newDay -= 30
            newMonth++
        }
        while (newMonth > 12) {
            newMonth -= 12
            newYear++
        }
        
        world.setSingleton(GameTime(newYear, newMonth, newDay, newHour, newTotal))
    }
    
    private fun processCultivation(ticks: Int) {
        world.query {
            CultivatingContext(world)
        }.forEach { ctx ->
            val currentProgress = ctx.progress.percentage
            val increase = Random.nextFloat() * 0.5f * ticks
            val newProgress = (currentProgress + increase).coerceAtMost(100f)
            
            ctx.entity.editor {
                it.addComponent(CultivationProgress(newProgress))
            }
            
            if (newProgress >= 100f) {
                attemptBreakthrough(ctx.entity, ctx.realm)
            }
        }
    }
    
    private fun attemptBreakthrough(
        entity: cn.jzl.ecs.entity.Entity,
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
                it.addComponent(nextRealm)
                it.addComponent(CultivationProgress(0f))
            }
            
            val name = entity.getComponent<EntityName>()?.value ?: "某人"
            println("🎉 $name 成功突破至 ${nextRealm.displayName}！")
        } else {
            entity.editor {
                it.addComponent(CultivationProgress(50f))
            }
        }
    }
    
    fun printStatus() {
        val time = world.getSingleton<GameTime>()
        println("\n=== ${time.toDisplayString()} ===")
        
        world.query {
            DiscipleContext(world)
        }.forEach { ctx ->
            val state = if (ctx.entity.hasComponent(Cultivating)) "修炼中" else "空闲"
            println("${ctx.name.value} | ${ctx.age.years}岁 | ${ctx.realm.displayName} | 进度:${"%.1f".format(ctx.progress.percentage)}% | $state")
        }
    }
}

// 查询上下文
class DiscipleContext(world: World) : EntityQueryContext(world) {
    val name by component<EntityName>()
    val age by component<Age>()
    val realm by component<CultivationRealm>()
    val progress by component<CultivationProgress>()
}

class CultivatingContext(world: World) : EntityQueryContext(world) {
    val progress by component<CultivationProgress>()
    val realm by component<CultivationRealm>()
    
    override fun FamilyBuilder.configure() {
        withComponent(Cultivating::class)
    }
}
```

**说明**:
- `update()` 每帧调用，处理时间和修炼逻辑
- `printStatus()` 打印当前游戏状态
- 使用 `EntityQueryContext` 进行类型安全的查询

---

### 4. SectWorld.kt

**内容**:
```kotlin
package cn.jzl.sect.demo

import cn.jzl.ecs.World
import cn.jzl.ecs.component.components
import cn.jzl.ecs.world
import cn.jzl.sect.demo.components.Age
import cn.jzl.sect.demo.components.CultivationProgress
import cn.jzl.sect.demo.components.CultivationRealm
import cn.jzl.sect.demo.components.EntityName
import cn.jzl.sect.demo.services.DemoSystem
import cn.jzl.sect.demo.services.GameTime
import cn.jzl.sect.demo.tags.Alive
import cn.jzl.sect.demo.tags.Cultivating
import cn.jzl.sect.demo.tags.Idle
import cn.jzl.sect.demo.tags.Working
import kotlin.random.Random

object SectWorld {
    lateinit var world: World
        private set
    
    lateinit var demoSystem: DemoSystem
        private set
    
    fun initialize() {
        world = createWorld {
            components {
                // 注册组件
                componentId<EntityName>()
                componentId<Age>()
                componentId<CultivationProgress>()
                componentId<CultivationRealm>()
                
                // 注册标签
                componentId<Alive>() { it.tag() }
                componentId<Idle>() { it.tag() }
                componentId<Cultivating>() { it.tag() }
                componentId<Working>() { it.tag() }
            }
        }
        
        demoSystem = DemoSystem(world)
        
        // 初始化游戏时间
        world.setSingleton(GameTime())
        
        // 创建初始弟子
        createInitialDisciples()
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
                it.addComponent(CultivationRealm.QiRefining1)
                it.addComponent(CultivationProgress(Random.nextFloat() * 50f))
                it.addComponent(Alive)
                
                if (shouldCultivate) {
                    it.addComponent(Cultivating)
                } else {
                    it.addComponent(Idle)
                }
            }
        }
    }
    
    fun update(deltaTime: Float) {
        demoSystem.update(deltaTime)
    }
}
```

**说明**:
- 配置 World 并注册所有组件和标签
- 创建 5 个随机弟子
- 部分弟子初始为修炼状态

---

### 5. DemoMain.kt

**内容**:
```kotlin
package cn.jzl.sect.demo

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

fun main() {
    println("=== 宗门修真录 - 最小 Demo ===")
    println("正在初始化世界...")
    
    // 初始化
    SectWorld.initialize()
    
    println("✅ 初始化完成！\n")
    
    // 打印初始状态
    SectWorld.demoSystem.printStatus()
    
    println("\n开始游戏循环（每2秒更新一次，共运行30秒）...")
    println("按 Ctrl+C 退出\n")
    
    // 游戏循环
    runBlocking {
        var elapsedTime = 0f
        val targetTime = 30f  // 运行30秒
        val updateInterval = 2f  // 每2秒更新
        
        while (elapsedTime < targetTime) {
            delay((updateInterval * 1000).toLong())
            elapsedTime += updateInterval
            
            // 更新游戏状态
            SectWorld.update(updateInterval)
            
            // 打印状态
            SectWorld.demoSystem.printStatus()
        }
    }
    
    println("\n=== Demo 结束 ===")
}
```

**说明**:
- 使用 `runBlocking` 运行协程
- 每 2 秒更新一次游戏状态
- 总共运行 30 秒

---

## 运行方式

### 1. 创建文件
按上述结构创建所有文件

### 2. 运行命令
```bash
./gradlew :composeApp:run
```

### 3. 预期输出
```
=== 宗门修真录 - 最小 Demo ===
正在初始化世界...
✅ 初始化完成！

=== 修真纪元1年 1月1日 6时 ===
张三-1 | 18岁 | 炼气一层 | 进度:23.5% | 修炼中
李四-2 | 22岁 | 炼气一层 | 进度:45.2% | 空闲
王五-3 | 19岁 | 炼气一层 | 进度:12.8% | 修炼中
...

开始游戏循环（每2秒更新一次，共运行30秒）...
按 Ctrl+C 退出

=== 修真纪元1年 1月1日 8时 ===
张三-1 | 18岁 | 炼气一层 | 进度:45.3% | 修炼中
🎉 王五-3 成功突破至 炼气五层！
...

=== Demo 结束 ===
```

---

## 扩展计划

### Phase 2: 添加更多功能
- [ ] 弟子 aging（年龄增长）
- [ ] 寿命系统（达到寿命上限死亡）
- [ ] 招收新弟子
- [ ] 简单的事件系统

### Phase 3: 添加 UI
- [ ] Compose 界面
- [ ] 实时数据显示
- [ ] 交互操作

---

## 技术要点

1. **ECS 核心**:
   - `world.entity {}` 创建实体
   - `it.addComponent<T>()` 添加组件
   - `world.query {}` 查询实体
   - `ctx.entity.editor {}` 编辑实体

2. **性能优化**:
   - 使用 `value class` 节省内存
   - 使用 `object` 标签减少内存占用
   - Query 结果缓存

3. **Kotlin 特性**:
   - 密封类表示状态
   - 属性委托 `by component<>()`
   - 类型安全查询

---

## 风险评估

| 风险 | 概率 | 影响 | 缓解措施 |
|------|------|------|----------|
| ECS API 不兼容 | 中 | 高 | 根据实际 API 调整代码 |
| 依赖缺失 | 低 | 中 | 确保所有依赖已配置 |
| 性能问题 | 低 | 低 | 当前规模小，无性能问题 |

---

## 成功标准

- [ ] 代码编译通过
- [ ] 能创建 5 个弟子实体
- [ ] 时间正常流逝
- [ ] 修炼进度增加
- [ ] 突破成功时打印提示
- [ ] 控制台正确显示状态

---

**计划创建者**: Claude Code
**创建时间**: 2026-02-13
**预计实施时间**: 30 分钟
