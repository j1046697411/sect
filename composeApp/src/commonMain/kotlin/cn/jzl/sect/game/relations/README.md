# 关系系统 (Relations)

本模块定义实体之间的关联关系。

## 设计原则

1. **可复用性**：同一关系类型可用于多种场景
2. **可选数据**：关系可以带数据，也可以不带
3. **方向性**：明确关系的源和目标

## 关系类型

### 1. 师徒关系 (Mentorship)

```kotlin
sealed class Mentorship

data class MentorshipData(
    val startYear: Int,        // 拜师年份
    val intimacy: Float,       // 亲密度 0.0-1.0
    val isFormal: Boolean      // 是否正式弟子（vs记名弟子）
)
```

**使用场景**：
- 弟子 → 师父：`addRelation<Mentorship>(target = master)`
- 师父 → 弟子：`addRelation<Mentorship>(target = disciple)`

### 2. 门派归属 (SectMembership)

```kotlin
sealed class SectMembership

data class SectMembershipData(
    val joinDate: Long,        // 入门时间
    val generation: Int,       // 第几代弟子
    val position: Position,    // 当前职位
    val totalContribution: Int // 累计贡献
)
```

**使用场景**：
- 弟子 → 门派：`addRelation<SectMembership>(target = sectEntity)`

### 3. 所有权 (Ownership)

通用所有权关系，可复用于多种场景。

```kotlin
sealed class Ownership

data class OwnershipData(
    val acquireTime: Long,     // 获得时间
    val acquireMethod: String  // 获得方式（购买/任务/赠送）
)
```

**使用场景**：
- 道具 → 所有者
- 设施 → 门派
- 资源点 → 占领者

### 4. 亲属关系 (Family)

```kotlin
sealed class ParentOf      // 父母
sealed class ChildOf       // 子女
sealed class SiblingOf     // 兄弟姐妹
sealed class SpouseOf      // 配偶/道侣

data class FamilyData(
    val relationType: FamilyRelationType,
    val bondStrength: Float   // 羁绊强度
)

enum class FamilyRelationType {
    BLOOD,           // 血缘
    ADOPTED,         // 收养
    MARRIAGE,        // 婚姻
    SWORN            // 结拜
}
```

### 5. 敌对关系 (Hostility)

```kotlin
sealed class EnemyOf

data class HostilityData(
    val reason: String,        // 敌对原因
    val severity: Int,         // 严重程度 1-10
    val startTime: Long        // 开始时间
)
```

### 6. 层级关系 (Hierarchy)

```kotlin
sealed class ChildOf         // 子实体（用于UI层级、设施部件等）
sealed class ManagerOf       // 管理关系
sealed class SubordinateOf   // 从属关系
```

## 关系服务

### MentorshipService

```kotlin
class MentorshipService(override val world: World) : EntityRelationContext {
    
    /// 建立师徒关系
    fun establishMentorship(
        master: Entity,
        disciple: Entity,
        isFormal: Boolean = true
    ) {
        val data = MentorshipData(
            startYear = world.getSingleton<GameTime>().year,
            intimacy = 0.5f,
            isFormal = isFormal
        )
        
        // 双向关系
        disciple.editor {
            it.addRelation<MentorshipData>(target = master, data = data)
        }
        master.editor {
            it.addRelation<Mentorship>(target = disciple)
        }
    }
    
    /// 获取师父
    fun getMaster(disciple: Entity): Entity? {
        return disciple.getRelation<Mentorship>()?.target
    }
    
    /// 获取所有徒弟
    fun getDisciples(master: Entity): List<Entity> {
        return world.query {
            entityFilter {
                relation(relations.relation<Mentorship>(target = master))
            }
        }.map { it.entity }
    }
    
    /// 提升亲密度
    fun increaseIntimacy(disciple: Entity, amount: Float) {
        val master = getMaster(disciple) ?: return
        val relation = disciple.getRelation<MentorshipData>(target = master) ?: return
        
        disciple.editor {
            it.addRelation(
                target = master,
                data = relation.copy(intimacy = (relation.intimacy + amount).coerceIn(0f, 1f))
            )
        }
    }
}
```

### SectMembershipService

```kotlin
class SectMembershipService(override val world: World) : EntityRelationContext {
    
    /// 弟子入门
    fun joinSect(disciple: Entity, sect: Entity, generation: Int) {
        val data = SectMembershipData(
            joinDate = System.currentTimeMillis(),
            generation = generation,
            position = Position.OUTER_DISCIPLE,
            totalContribution = 0
        )
        
        disciple.editor {
            it.addRelation<SectMembershipData>(target = sect, data = data)
        }
        
        // 添加门派标签
        disciple.editor {
            it.addTag<InSectTag>()
        }
    }
    
    /// 提升职位
    fun promote(disciple: Entity, newPosition: Position) {
        val sect = getSect(disciple) ?: return
        val membership = disciple.getRelation<SectMembershipData>(target = sect) ?: return
        
        disciple.editor {
            it.addRelation(
                target = sect,
                data = membership.copy(position = newPosition)
            )
        }
        
        // 添加职位组件
        disciple.editor {
            it.addComponent(newPosition)
        }
    }
    
    /// 获取门派所有成员
    fun getSectMembers(sect: Entity): List<Entity> {
        return world.query {
            entityFilter {
                relation(relations.relation<SectMembership>(target = sect))
            }
        }.map { it.entity }
    }
    
    /// 按职位筛选成员
    fun getMembersByPosition(sect: Entity, position: Position): List<Entity> {
        return getSectMembers(sect).filter { member ->
            val membership = member.getRelation<SectMembershipData>(target = sect)
            membership?.position == position
        }
    }
}
```

## 使用示例

### 创建完整的师徒体系

```kotlin
val mentorshipService = MentorshipService(world)
val sectService = SectMembershipService(world)

// 创建门派
val sect = createSect("青云宗")

// 创建掌门
val leader = createDisciple("李掌门")
sectService.joinSect(leader, sect, generation = 1)
sectService.promote(leader, Position.SECT_LEADER)

// 创建长老
val elder1 = createDisciple("张长老")
sectService.joinSect(elder1, sect, generation = 2)
sectService.promote(elder1, Position.ELDER)

// 创建弟子并拜师
val disciple1 = createDisciple("王弟子")
sectService.joinSect(disciple1, sect, generation = 3)
mentorshipService.establishMentorship(elder1, disciple1)

// 查询某长老的所有徒弟
val disciples = mentorshipService.getDisciples(elder1)
println("张长老有 ${disciples.size} 个徒弟")
```

## 性能考虑

1. **关系缓存**：频繁查询的关系考虑缓存
2. **双向维护**：重要关系需要双向维护（师徒）
3. **批量查询**：使用 Query 批量获取关系实体

## 注意事项

1. **关系有效性**：确保目标实体存在且有效
2. **关系唯一性**：某些关系应该是唯一的（如师徒）
3. **级联删除**：删除实体时考虑是否级联删除关系
