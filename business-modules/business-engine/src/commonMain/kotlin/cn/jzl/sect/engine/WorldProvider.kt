package cn.jzl.sect.engine

import cn.jzl.ecs.World

/**
 * World提供者 - 单例管理整个游戏的World实例
 *
 * 确保整个应用只有一个World实例，所有系统和服务共享同一个World。
 * 使用方式：
 * 1. 应用启动时调用 WorldProvider.initialize("宗门名称")
 * 2. 其他组件通过 WorldProvider.world 获取World实例
 */
object WorldProvider {
    private var _world: World? = null

    /**
     * 获取World实例
     * @throws IllegalStateException 如果World未初始化
     */
    val world: World
        get() = _world ?: throw IllegalStateException("World not initialized. Call WorldProvider.initialize() first.")

    /**
     * 检查World是否已初始化
     */
    val isInitialized: Boolean
        get() = _world != null

    /**
     * 初始化World
     * @param sectName 宗门名称
     * @return 创建的World实例
     * @throws IllegalStateException 如果World已经初始化
     */
    fun initialize(sectName: String): World {
        if (_world != null) {
            throw IllegalStateException("World already initialized")
        }
        _world = SectWorld.create(sectName)
        return _world!!
    }

    /**
     * 重置World（用于测试或重新开始游戏）
     */
    fun reset() {
        _world = null
    }

    /**
     * 创建新的World实例（仅用于测试）
     * 此方法会重置现有的World并创建新的
     * @param sectName 宗门名称
     * @return 创建的World实例
     */
    fun createForTesting(sectName: String): World {
        reset()
        return initialize(sectName)
    }
}
