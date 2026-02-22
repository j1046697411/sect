package cn.jzl.sect.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.jzl.ecs.World
import cn.jzl.sect.engine.*
import cn.jzl.sect.engine.WorldProvider
import cn.jzl.sect.engine.service.WorldQueryService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 游戏视图模型
 * 管理游戏循环、时间流逝和玩家交互
 */
class GameViewModel : ViewModel() {

    // 通过WorldProvider获取World实例
    private val world: World = WorldProvider.world
    private val systems: SectSystems = SectWorld.getSystems(world)
    private val queryService = WorldQueryService(world)

    // 游戏循环
    private val gameLoop: GameLoop = GameLoop(world, systems)

    // 游戏状态
    val gameState: StateFlow<GameState> = gameLoop.gameState

    // 游戏速度
    val gameSpeed: StateFlow<GameSpeed> = gameLoop.gameSpeed

    // 当前游戏时间
    private val _currentTime = MutableStateFlow("第1年1月1日")
    val currentTime: StateFlow<String> = _currentTime.asStateFlow()

    init {
        // 启动游戏循环
        gameLoop.start()

        // 定期更新UI
        viewModelScope.launch {
            while (true) {
                updateCurrentTime()
                kotlinx.coroutines.delay(1000)
            }
        }
    }

    /**
     * 更新当前时间显示
     */
    private fun updateCurrentTime() {
        val info = queryService.querySectInfo()
        if (info != null) {
            _currentTime.value = "第${info.currentYear}年${info.currentMonth}月${info.currentDay}日"
        }
    }

    /**
     * 暂停游戏
     */
    fun pauseGame() {
        gameLoop.pause()
    }

    /**
     * 恢复游戏
     */
    fun resumeGame() {
        gameLoop.resume()
    }

    /**
     * 设置游戏速度
     */
    fun setGameSpeed(speed: GameSpeed) {
        gameLoop.setSpeed(speed)
    }

    override fun onCleared() {
        super.onCleared()
        gameLoop.dispose()
    }
}
