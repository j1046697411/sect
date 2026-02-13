# 资源组件 (Resource Components)

本模块定义游戏内所有资源相关的组件。

## 设计原则

1. **资源分类清晰**：基础资源、材料、丹药、装备等
2. **可堆叠性**：同类资源可合并存储
3. **品质系统**：支持不同品质等级

## 组件列表

### 1. 资源类型枚举

```kotlin
enum class ResourceType {
    // 基础资源
    SPIRIT_STONE,        // 灵石
    SPIRIT_COIN,         // 灵币
    CONTRIBUTION_POINT,  // 贡献点
    
    // 原材料
    WOOD,                // 木材
    STONE,               // 石材
    IRON_ORE,            // 铁矿
    COPPER_ORE,          // 铜矿
    SPIRIT_CRYSTAL,      // 灵晶
    
    // 灵草
    GINSENG,             // 人参
    SPIRIT_GRASS,        // 灵草
    HEAVENLY_LOTUS,      // 天莲
    DRAGON_BONE_GRASS,   // 龙骨草
    
    // 材料
    LEATHER,             // 皮革
    CLOTH,               // 布料
    JADE,                // 玉石
    SPIRIT_WOOD          // 灵木
}

enum class ItemQuality {
    COMMON,      // 普通（白色）
    UNCOMMON,    // 优秀（绿色）
    RARE,        // 稀有（蓝色）
    EPIC,        // 史诗（紫色）
    LEGENDARY,   // 传说（橙色）
    MYTHIC       // 神话（红色）
}
```

### 2. 资源堆叠

```kotlin
// 资源堆叠组件（用于存储）
data class ResourceStack(
    val type: ResourceType,
    val quantity: Int,
    val quality: ItemQuality = ItemQuality.COMMON
)

// 资源容器（实体拥有资源）
data class ResourceInventory(
    val items: MutableMap<ResourceType, Int>,
    val maxCapacity: Int
) {
    fun add(type: ResourceType, amount: Int) {
        items[type] = (items[type] ?: 0) + amount
    }
    
    fun remove(type: ResourceType, amount: Int): Boolean {
        val current = items[type] ?: return false
        if (current < amount) return false
        if (current == amount) items.remove(type)
        else items[type] = current - amount
        return true
    }
    
    fun getTotal(): Int = items.values.sum()
}
```

### 3. 丹药系统

```kotlin
// 丹药类型
enum class PillType {
    CULTIVATION,    // 修炼丹（加速修炼）
    HEALING,        // 疗伤丹（恢复气血）
    BREAKTHROUGH,   // 突破丹（提高成功率）
    DETOXIFICATION, // 解毒丹
    ENLIGHTENMENT,  // 悟道丹（临时提升悟性）
    REGENERATION    // 回灵丹（恢复灵力）
}

// 丹药效果
data class PillEffect(
    val type: PillType,
    val effectValue: Float,      // 效果数值
    val duration: Float?,        // 持续时间（秒），null表示永久/即时
    val sideEffect: String?      // 副作用描述
)

// 丹药实例
@JvmInline value class PillId(val value: Long)

data class Pill(
    val id: PillId,
    val name: String,
    val quality: ItemQuality,
    val effects: List<PillEffect>,
    val requiredRealm: CultivationRealm?  // 最低使用境界
)
```

### 4. 装备系统

```kotlin
enum class EquipmentType {
    WEAPON,      // 武器
    ARMOR,       // 护甲
    ACCESSORY,   // 饰品
    MOUNT,       // 坐骑
    TALISMAN     // 法宝
}

enum class EquipmentSlot {
    MAIN_HAND, OFF_HAND, HEAD, BODY, LEGS, FEET, RING, NECKLACE
}

data class EquipmentStats(
    val attack: Int = 0,
    val defense: Int = 0,
    val healthBonus: Int = 0,
    val spiritBonus: Int = 0,
    val speedBonus: Float = 0f,
    val specialEffects: List<String> = emptyList()
)

@JvmInline value class EquipmentId(val value: Long)

data class Equipment(
    val id: EquipmentId,
    val name: String,
    val type: EquipmentType,
    val slot: EquipmentSlot,
    val quality: ItemQuality,
    val level: Int,
    val stats: EquipmentStats,
    val durability: Int,         // 耐久度
    val maxDurability: Int
)

// 已装备标记
object Equipped : Component
```

## 使用场景

### 给弟子添加资源

```kotlin
world.entity {
    it.addComponent(ResourceInventory(
        items = mutableMapOf(
            ResourceType.SPIRIT_STONE to 100,
            ResourceType.GINSENG to 5
        ),
        maxCapacity = 1000
    ))
}
```

### 交易资源

```kotlin
fun transferResources(
    from: Entity,
    to: Entity,
    type: ResourceType,
    amount: Int
): Boolean {
    val fromInventory = from.getComponent<ResourceInventory>() ?: return false
    val toInventory = to.getComponent<ResourceInventory>() ?: return false
    
    if (!fromInventory.remove(type, amount)) return false
    if (toInventory.getTotal() + amount > toInventory.maxCapacity) {
        fromInventory.add(type, amount)  // 回滚
        return false
    }
    
    toInventory.add(type, amount)
    return true
}
```

## 依赖关系

- **依赖**：`core` 模块
- **依赖**：`cultivation` 模块（使用 CultivationRealm）
- **被依赖**：`facility` 模块（设施生产资源）
