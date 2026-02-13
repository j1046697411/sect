# 生命周期标签 (Lifecycle Tags)

本模块定义实体生命周期相关的标签。

## 设计原则

1. **互斥性**：`Alive` 和 `Dead` 互斥
2. **不可逆性**：死亡是单向转换（除非有特殊复活机制）
3. **基础标记**：这些是所有实体最基础的标签

## 标签列表

### 1. 生命状态

```kotlin
sealed class AliveTag      // 存活状态
sealed class DeadTag       // 死亡状态
sealed class DyingTag      // 濒死状态（即将死亡）
sealed class SpiritTag     // 元神状态（仅元神存在）
```

### 2. 存在状态

```kotlin
sealed class ActiveTag     // 活跃状态（正常参与游戏）
sealed class InactiveTag   // 非活跃（暂时不处理）
sealed class DeletedTag    // 已删除（软删除标记）
sealed class ArchivedTag   // 已归档（转为历史记录）
```

### 3. 游戏阶段状态

```kotlin
sealed class PlayerTag           // 玩家控制的角色
sealed class NPCTag              // NPC（AI控制）
sealed class ProtagonistTag      // 主角（特殊标记）
sealed class HistoricalTag       // 历史人物（只读）
```

### 4. 门派生命周期

```kotlin
sealed class InSectTag           // 在门派中
sealed class ExiledTag           // 被流放
sealed class BetrayedTag         // 叛出门派
sealed class RetiredTag          // 退休/养老
```

## 生命周期转换图

```
创建实体
   ↓
[ActiveTag] + [AliveTag]  ← 正常游戏状态
   ↓
可选转换:
   ├─ 受伤 → [DyingTag]（濒死）
   │          ↓
   │       [DeadTag]（死亡）
   │          ↓
   │       [ArchivedTag]（归档为历史）
   │          ↓
   │       最终删除实体
   │
   ├─ 离开 → [ExiledTag] / [BetrayedTag]
   │          ↓
   │       [InactiveTag]（不再处理）
   │
   └─ 退休 → [RetiredTag]
              ↓
           [InactiveTag]
```

## 使用示例

### 创建新弟子

```kotlin
fun createDisciple(world: World, name: String): Entity {
    return world.entity {
        // 基础信息
        it.addComponent(EntityName(name))
        it.addComponent(Age(18))
        
        // 生命周期标签
        it.addTag<AliveTag>()
        it.addTag<ActiveTag>()
        it.addTag<NPCTag>()  // AI控制
        it.addTag<InSectTag>()  // 在门派中
        
        // 行为标签
        it.addTag<IdleTag>()
    }
}
```

### 死亡处理

```kotlin
fun onEntityDeath(entity: Entity, cause: String) {
    // 1. 切换生命标签
    entity.editor {
        it.removeTag<AliveTag>()
        it.removeTag<ActiveTag>()
        it.addTag<DeadTag>()
    }
    
    // 2. 记录死亡信息
    entity.editor {
        it.addComponent(DeathRecord(
            timestamp = System.currentTimeMillis(),
            cause = cause,
            age = entity.getComponent<Age>()?.years ?: 0,
            realm = entity.getComponent<CultivationRealm>()?.displayName ?: "未知"
        ))
    }
    
    // 3. 触发死亡事件
    world.eventBus.emit(EntityDeathEvent(entity, cause))
    
    // 4. 延迟归档（死亡7天后）
    scheduleArchive(entity, delayDays = 7)
}
```

### 归档处理

```kotlin
fun archiveEntity(entity: Entity) {
    entity.editor {
        it.removeTag<DeadTag>()
        it.addTag<ArchivedTag>()
        it.addTag<InactiveTag>()
    }
    
    // 移除所有行为标签和组件，只保留基础信息
    // 转为只读历史记录
}
```

## 查询场景

```kotlin
// 查询所有活着的弟子
val aliveDisciples = world.query {
    entityFilter {
        hasTag<AliveTag>()
        hasComponent<InnateTalent>()
    }
}

// 查询所有活跃的实体
val activeEntities = world.query {
    entityFilter {
        hasTag<ActiveTag>()
    }
}

// 查询死亡待归档的实体
val deadEntities = world.query {
    entityFilter {
        hasTag<DeadTag>()
        !hasTag<ArchivedTag>()
    }
}

// 查询门派成员（不包括已离开）
val sectMembers = world.query {
    entityFilter {
        hasTag<InSectTag>()
        hasTag<AliveTag>()
    }
}
```

## 性能考虑

1. **标签数量**：避免给实体添加过多标签（建议 < 20）
2. **批量处理**：死亡/归档操作批量进行，减少 Archetype 切换开销
3. **定期清理**：定期清理已归档实体，释放内存

## 与其他模块的关系

- **被使用**：所有系统都需要检查 `AliveTag`
- **与状态标签**：`Alive` + `Cultivating` 表示正常修炼
- **与事件系统**：生命周期转换触发事件（死亡事件、加入门派事件等）
