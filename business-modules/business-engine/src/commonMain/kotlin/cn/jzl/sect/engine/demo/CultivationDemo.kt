package cn.jzl.sect.engine.demo

import cn.jzl.ecs.World
import cn.jzl.sect.engine.SectWorld
import cn.jzl.sect.engine.SectSystems
import cn.jzl.sect.engine.systems.TimeSystem
import cn.jzl.sect.cultivation.systems.CultivationSystem
import cn.jzl.sect.disciples.systems.DiscipleInfoSystem
import cn.jzl.sect.resource.systems.ResourceProductionSystem
import cn.jzl.sect.resource.systems.ResourceConsumptionSystem
import cn.jzl.sect.facility.systems.SectStatusSystem
import cn.jzl.sect.facility.systems.SectStatus
import cn.jzl.sect.core.sect.SectPosition
import cn.jzl.sect.core.time.toDisplayString

/**
 * 宗门修真录 - 修炼系统Demo（纯自动运行版）
 *
 * 功能说明：
 * 1. 显示宗门概览信息
 * 2. 查看所有弟子状态
 * 3. 自动推进游戏时间（触发修炼更新）
 * 4. 自动处理境界突破
 * 5. 资源生产与消耗循环
 * 6. 弟子忠诚度管理
 * 7. 宗门财务状态监控
 */
class CultivationDemo {

    private lateinit var world: World
    private lateinit var systems: SectSystems
    private lateinit var timeSystem: TimeSystem

    fun initialize(sectName: String = "青云宗") {
        println("正在初始化宗门世界...")
        world = SectWorld.create(sectName)
        systems = SectWorld.getSystems(world)
        timeSystem = TimeSystem(world)
        println("宗门世界初始化完成！\n")
    }

    fun run() {
        println("╔════════════════════════════════════════════════╗")
        println("║          欢迎来到《宗门修真录》修炼系统Demo         ║")
        println("║               （纯自动运行模式）                  ║")
        println("╚════════════════════════════════════════════════╝")
        println()

        // 显示初始状态
        println("【初始状态】")
        showSectOverview()
        showFinancialSummary()
        showDiscipleList()

        // 模拟运行12个月
        println("══════════════════════════════════════════════════")
        println("开始自动运行模拟（12个月）...")
        println("══════════════════════════════════════════════════\n")

        var monthCount = 0
        while (monthCount < 12) {
            monthCount++
            println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            println("【第 $monthCount 个月】")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            // 检查宗门状态
            val sectStatus = systems.sectStatusSystem.checkSectStatus()
            if (!sectStatus.isOperational()) {
                println("\n💀 ${sectStatus.description}")
                println("宗门已解散，模拟结束！")
                break
            }

            if (sectStatus == SectStatus.CRITICAL) {
                println("\n⚠️ ${sectStatus.description}")
            }

            // 推进30天（一个月）
            advanceTimeLarge()

            // 每月显示一次弟子状态
            if (monthCount % 3 == 0 || sectStatus == SectStatus.WARNING) {
                println("\n【季度弟子状态报告】")
                showDiscipleList()
                showFinancialSummary()
            }
        }

        // 最终状态
        if (monthCount >= 12) {
            println("\n══════════════════════════════════════════════════")
            println("【模拟结束 - 最终状态】")
            println("══════════════════════════════════════════════════")
            showSectOverview()
            showFinancialSummary()
            showDiscipleList()
        }

        println("\n感谢观看《宗门修真录》修炼系统Demo！")
    }

    private fun showSectOverview() {
        println()
        val statistics = systems.discipleInfoSystem.getDiscipleStatistics()
        println(statistics.toDisplayString())
        println()
    }

    private fun showFinancialSummary() {
        val summary = systems.sectStatusSystem.getFinancialSummary()
        println(summary.toDisplayString())
        println()
    }

    private fun showDiscipleList() {
        println()
        val disciples = systems.discipleInfoSystem.getAllDisciples()

        println("╔══════════════════════════════════════════════════════════════════════════════════════╗")
        println("║                                    弟子列表                                           ║")
        println("╠══════════════════════════════════════════════════════════════════════════════════════╣")
        println("║ 职务   | 境界      | 修为      | 进度  | 进度条     | 年龄 | 忠诚 | 忠诚度状态        ║")
        println("╠══════════════════════════════════════════════════════════════════════════════════════╣")

        disciples.forEach { disciple ->
            println("║ ${disciple.toDisplayString()} ║")
        }

        println("╚══════════════════════════════════════════════════════════════════════════════════════╝")
        println("共 ${disciples.size} 名弟子\n")
    }

    private fun advanceTime() {
        println()
        println("正在推进时间...")

        // 推进24小时
        val timeInfo = timeSystem.advance(24)
        println(timeInfo.toDisplayString())

        // 触发修炼更新
        val breakthroughs = systems.cultivationSystem.update(24)

        // 显示突破信息
        if (breakthroughs.isNotEmpty()) {
            println()
            println("🎉 突破喜讯：")
            breakthroughs.forEach { event ->
                println("   ${event.toDisplayString()}")
            }
        }

        println()
    }

    private fun advanceTimeLarge() {
        val allBreakthroughs = mutableListOf<CultivationSystem.BreakthroughEvent>()

        // 先进行资源产出（30天）
        val productionSummary = systems.resourceProductionSystem.monthlyProduction()

        // 分30次推进，每次24小时
        repeat(30) {
            timeSystem.advance(24)
            val breakthroughs = systems.cultivationSystem.update(24)
            allBreakthroughs.addAll(breakthroughs)
        }

        // 进行资源消耗结算
        val consumptionResult = systems.resourceConsumptionSystem.monthlyConsumption()

        val currentTime = timeSystem.getCurrentTime()
        println("时间推进至：${currentTime?.toDisplayString()}")

        // 显示资源产出
        println()
        println("💰 本月资源产出：")
        println("   灵石：+${productionSummary.spiritStones}")

        // 显示资源消耗
        println()
        println(consumptionResult.toDisplayString())

        // 显示突破统计
        if (allBreakthroughs.isNotEmpty()) {
            println()
            println("🎉 本月突破统计：")

            // 按职务分组统计
            val byPosition = allBreakthroughs.groupBy { it.position }
            byPosition.forEach { (position, events) ->
                println("   ${position.displayName}：${events.size} 人次")
            }

            println()
            println("详细突破记录：")
            allBreakthroughs.forEach { event ->
                println("   ${event.toDisplayString()}")
            }
        }
    }
}

/**
 * 职务显示名称扩展
 */
private val SectPosition.displayName: String
    get() = when (this) {
        SectPosition.DISCIPLE_OUTER -> "外门弟子"
        SectPosition.DISCIPLE_INNER -> "内门弟子"
        SectPosition.ELDER -> "长老"
        SectPosition.LEADER -> "掌门"
    }

/**
 * Demo入口
 */
fun main() {
    val demo = CultivationDemo()
    demo.initialize("青云宗")
    demo.run()
}
