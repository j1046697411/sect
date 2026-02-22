package cn.jzl.sect.core.config

import cn.jzl.sect.core.cultivation.Realm
import cn.jzl.sect.core.sect.SectPositionType

/**
 * 游戏配置管理器
 * 管理所有游戏数值配置
 */
class GameConfig {

    // 俸禄配置
    val salary: SalaryConfig = SalaryConfig()

    // 修炼配置
    val cultivation: CultivationConfig = CultivationConfig()

    // 资源产出配置
    val resource: ResourceConfig = ResourceConfig()

    // 设施配置
    val facility: FacilityConfig = FacilityConfig()

    // 忠诚度配置
    val loyalty: LoyaltyConfig = LoyaltyConfig()

    companion object {
        @Volatile
        private var instance: GameConfig? = null

        /**
         * 获取单例实例
         */
        fun getInstance(): GameConfig {
            return instance ?: synchronized(this) {
                instance ?: GameConfig().also { instance = it }
            }
        }

        /**
         * 重置单例（用于测试）
         */
        fun resetInstance() {
            instance = null
        }
    }
}

/**
 * 俸禄配置
 */
class SalaryConfig {
    /**
     * 获取指定职位的月俸
     */
    fun getMonthlySalary(position: SectPositionType): Long {
        return when (position) {
            SectPositionType.LEADER -> 500L
            SectPositionType.ELDER -> 300L
            SectPositionType.DISCIPLE_INNER -> 80L
            SectPositionType.DISCIPLE_OUTER -> 30L
        }
    }
}

/**
 * 修炼配置
 */
class CultivationConfig {
    // 基础修为增长率（每小时）
    val baseCultivationGainPerHour: Long = 10L

    // 境界最大层数
    val maxLayerPerRealm: Int = 9

    /**
     * 获取境界最大修为值
     */
    fun getMaxCultivation(realm: Realm, layer: Int): Long {
        return when (realm) {
            Realm.MORTAL -> 1000L * layer
            Realm.QI_REFINING -> 5000L * layer
            Realm.FOUNDATION -> 10000L * layer
            Realm.GOLDEN_CORE -> 50000L * layer
            Realm.NASCENT_SOUL -> 100000L * layer
            Realm.SOUL_TRANSFORMATION -> 500000L * layer
            Realm.TRIBULATION -> 1000000L * layer
            Realm.IMMORTAL -> Long.MAX_VALUE
        }
    }

    /**
     * 获取境界突破基础成功率
     */
    fun getBaseBreakthroughSuccessRate(realm: Realm): Double {
        return when (realm) {
            Realm.MORTAL -> 1.0
            Realm.QI_REFINING -> 0.8
            Realm.FOUNDATION -> 0.6
            Realm.GOLDEN_CORE -> 0.5
            Realm.NASCENT_SOUL -> 0.4
            Realm.SOUL_TRANSFORMATION -> 0.3
            Realm.TRIBULATION -> 0.2
            Realm.IMMORTAL -> 0.0
        }
    }

    /**
     * 福缘对突破成功率的影响系数
     */
    val fortuneEffectOnBreakthrough: Double = 0.002
}

/**
 * 资源产出配置
 */
class ResourceConfig {
    // 灵石矿脉基础产出（每天）
    val spiritStoneBaseOutput: Long = 50L

    // 草药基础产出（每天）
    val herbBaseOutput: Long = 30L

    // 矿石基础产出（每天）
    val oreBaseOutput: Long = 40L

    // 粮食基础产出（每天）
    val foodBaseOutput: Long = 60L

    /**
     * 根据季节获取资源产出倍率
     */
    fun getSeasonMultiplier(season: cn.jzl.sect.core.time.Season): Float {
        return when (season) {
            cn.jzl.sect.core.time.Season.SPRING -> 1.2f  // 春季产出增加
            cn.jzl.sect.core.time.Season.SUMMER -> 1.0f
            cn.jzl.sect.core.time.Season.AUTUMN -> 1.1f  // 秋季产出略增
            cn.jzl.sect.core.time.Season.WINTER -> 0.8f  // 冬季产出减少
        }
    }
}

/**
 * 设施配置
 */
class FacilityConfig {
    /**
     * 计算设施维护费
     */
    fun calculateMaintenanceCost(level: Int, efficiency: Float): Long {
        return (level * 10L * efficiency).toLong()
    }

    // 设施最大等级
    val maxFacilityLevel: Int = 10

    // 升级费用系数
    val upgradeCostMultiplier: Long = 100L
}

/**
 * 忠诚度配置
 */
class LoyaltyConfig {
    // 正常发放俸禄时忠诚度增加量
    val loyaltyIncreaseOnPayment: Int = 1

    // 拖欠俸禄时忠诚度减少量
    val loyaltyDecreaseOnUnpaid: Int = 10

    // 连续未发俸禄的最大容忍月数
    val maxConsecutiveUnpaidMonths: Int = 6

    // 忠诚度等级阈值
    val devotedThreshold: Int = 80
    val loyalThreshold: Int = 60
    val neutralThreshold: Int = 40
    val discontentThreshold: Int = 20

    /**
     * 判断是否可能叛逃
     */
    fun mayDefect(loyaltyValue: Int, consecutiveUnpaidMonths: Int): Boolean {
        return loyaltyValue <= 10 || consecutiveUnpaidMonths >= maxConsecutiveUnpaidMonths
    }
}
