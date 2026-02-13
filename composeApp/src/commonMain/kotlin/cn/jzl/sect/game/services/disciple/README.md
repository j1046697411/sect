# 弟子服务 (Disciple Service)

本模块实现弟子的创建、管理和查询功能。

## 核心功能

1. **弟子创建**：生成具有随机属性的新弟子
2. **属性管理**：查询和更新弟子信息
3. **生命周期**：入门、晋升、离开等

## DiscipleFactory

```kotlin
class DiscipleFactory(override val world: World) : EntityRelationContext {
    
    /// 创建随机弟子
    fun createRandomDisciple(
        sect: Entity? = null,
        generation: Int = 1
    ): Entity {
        val gender = if (Random.nextBoolean()) Male::class else Female::class
        val familyNames = listOf("张", "李", "王", "赵", "刘", "陈", "杨", "黄")
        val givenNames = listOf("三", "四", "五", "文", "武", "明", "华", "强")
        
        val name = familyNames.random() + givenNames.random()
        val age = Random.nextInt(16, 26)
        
        return createDisciple(
            name = name,
            age = age,
            gender = gender,
            sect = sect,
            generation = generation
        )
    }
    
    /// 创建指定参数的弟子
    fun createDisciple(
        name: String,
        age: Int,
        gender: KClass<*>,
        sect: Entity? = null,
        generation: Int = 1,
        position: Position = Position.OUTER_DISCIPLE,
        customTalent: InnateTalent? = null
    ): Entity {
        return world.entity {
            // 基础信息
            it.addComponent(EntityName(name))
            it.addComponent(Age(age))
            it.addTag(gender)
            
            // 资质（随机或自定义）
            val talent = customTalent ?: generateRandomTalent()
            it.addComponent(talent)
            
            // 性格
            it.addComponent(generateRandomPersonality())
            
            // 战斗属性
            it.addComponent(generateCombatStats(talent))
            
            // 生命状态
            val maxHealth = talent.constitution * 10f
            it.addComponent(Health(maxHealth, maxHealth))
            it.addComponent(SpiritPower(100f, 100f))
            it.addComponent(Lifespan(80 + talent.constitution / 2))
            
            // 修炼状态
            it.addComponent(CultivationRealm.QiRefining.L1)
            it.addComponent(CultivationProgress(0f))
            
            // 门派相关
            if (sect != null) {
                it.addRelation<SectMembershipData>(
                    target = sect,
                    data = SectMembershipData(
                        joinDate = System.currentTimeMillis(),
                        generation = generation,
                        position = position,
                        totalContribution = 0
                    )
                )
                it.addTag<InSectTag>()
            }
            it.addComponent(position)
            it.addComponent(ContributionPoints(0))
            
            // 初始资源
            it.addComponent(ResourceInventory(
                items = mutableMapOf(ResourceType.SPIRIT_STONE to 10),
                maxCapacity = 100
            ))
            
            // 生命周期标签
            it.addTag<AliveTag>()
            it.addTag<ActiveTag>()
            it.addTag<NPCTag>()
            it.addTag<IdleTag>()
        }
    }
    
    private fun generateRandomTalent(): InnateTalent {
        return InnateTalent(
            rootBone = Random.nextInt(20, 80),
            understanding = Random.nextInt(20, 80),
            luck = Random.nextInt(20, 80),
            constitution = Random.nextInt(20, 80),
            charm = Random.nextInt(20, 80)
        )
    }
    
    private fun generateRandomPersonality(): Personality {
        return Personality(
            ambition = Random.nextInt(20, 80),
            diligence = Random.nextInt(20, 80),
            loyalty = Random.nextInt(20, 80),
            greed = Random.nextInt(20, 80),
            kindness = Random.nextInt(20, 80),
            caution = Random.nextInt(20, 80)
        )
    }
    
    private fun generateCombatStats(talent: InnateTalent): CombatStats {
        return CombatStats(
            strength = talent.rootBone / 2,
            agility = talent.rootBone / 3,
            intelligence = talent.understanding / 2,
            endurance = talent.constitution / 2
        )
    }
}
```

## DiscipleQueryService

```kotlin
class DiscipleQueryService(override val world: World) : EntityRelationContext {
    
    /// 查询所有弟子
    fun getAllDisciples(): List<DiscipleInfo> {
        return world.query {
            entityFilter {
                hasComponent<InnateTalent>()
                hasTag<AliveTag>()
            }
        }.map { ctx ->
            extractDiscipleInfo(ctx)
        }
    }
    
    /// 查询门派成员
    fun getSectMembers(sect: Entity): List<DiscipleInfo> {
        return world.query {
            entityFilter {
                relation(relations.relation<SectMembership>(target = sect))
                hasTag<AliveTag>()
            }
        }.map { extractDiscipleInfo(it) }
    }
    
    /// 按职位查询
    fun getByPosition(sect: Entity, position: Position): List<DiscipleInfo> {
        return getSectMembers(sect).filter { it.position == position }
    }
    
    /// 按境界查询
    fun getByRealm(realm: CultivationRealm): List<DiscipleInfo> {
        return world.query {
            entityFilter {
                hasComponent<CultivationRealm> { it == realm }
                hasTag<AliveTag>()
            }
        }.map { extractDiscipleInfo(it) }
    }
    
    /// 查询可收徒的长老
    fun getAvailableMasters(sect: Entity): List<Entity> {
        return world.query {
            entityFilter {
                relation(relations.relation<SectMembership>(target = sect))
                hasComponent<Position> { it.rank >= Position.ELDER.rank }
                hasTag<AliveTag>()
            }
        }.filter { ctx ->
            // 检查弟子数量是否已满
            val discipleCount = getDiscipleCount(ctx.entity)
            discipleCount < 5  // 最多5个徒弟
        }.map { it.entity }
    }
    
    /// 获取弟子详情
    fun getDiscipleDetail(entity: Entity): DiscipleDetail? {
        if (!entity.hasComponent<InnateTalent>()) return null
        
        return DiscipleDetail(
            basicInfo = BasicInfo(
                name = entity.getComponent<EntityName>()?.value ?: "无名",
                age = entity.getComponent<Age>()?.years ?: 0,
                gender = if (entity.hasTag<Male>()) "男" else "女",
                position = entity.getComponent<Position>()?.name ?: "未知"
            ),
            realmInfo = RealmInfo(
                currentRealm = entity.getComponent<CultivationRealm>()?.displayName ?: "未知",
                progress = entity.getComponent<CultivationProgress>()?.percentage ?: 0f
            ),
            talent = entity.getComponent<InnateTalent>()!!,
            personality = entity.getComponent<Personality>()!!,
            stats = entity.getComponent<CombatStats>()!!,
            health = entity.getComponent<Health>()!!,
            master = getMaster(entity)?.getComponent<EntityName>()?.value,
            disciples = getDisciples(entity).map { it.getComponent<EntityName>()?.value ?: "无名" }
        )
    }
    
    private fun extractDiscipleInfo(ctx: DiscipleContext): DiscipleInfo {
        return DiscipleInfo(
            entity = ctx.entity,
            name = ctx.name.value,
            realm = ctx.realm.displayName,
            position = ctx.position.name,
            age = ctx.age.years,
            contribution = ctx.contribution.amount,
            state = getCurrentState(ctx.entity)
        )
    }
    
    /// 获取师父
    fun getMaster(disciple: Entity): Entity? {
        return disciple.getRelation<Mentorship>()?.target
    }
    
    /// 获取徒弟
    fun getDisciples(master: Entity): List<Entity> {
        return world.query {
            entityFilter {
                relation(relations.relation<Mentorship>(target = master))
            }
        }.map { it.entity }
    }
    
    /// 统计弟子数量
    fun countDisciples(sect: Entity): Map<String, Int> {
        val members = getSectMembers(sect)
        return mapOf(
            "total" to members.size,
            "outer" to members.count { it.position == Position.OUTER_DISCIPLE.name },
            "inner" to members.count { it.position == Position.INNER_DISCIPLE.name },
            "core" to members.count { it.position == Position.CORE_DISCIPLE.name },
            "elder" to members.count { it.position.rank >= Position.ELDER.rank }
        )
    }
}

// 查询上下文
class DiscipleContext(world: World) : EntityQueryContext(world) {
    val name by component<EntityName>()
    val age by component<Age>()
    val realm by component<CultivationRealm>()
    val position by component<Position>()
    val contribution by component<ContributionPoints>()
    val talent by component<InnateTalent>()
}

// 数据传输对象
data class DiscipleInfo(
    val entity: Entity,
    val name: String,
    val realm: String,
    val position: String,
    val age: Int,
    val contribution: Int,
    val state: String
)

data class DiscipleDetail(
    val basicInfo: BasicInfo,
    val realmInfo: RealmInfo,
    val talent: InnateTalent,
    val personality: Personality,
    val stats: CombatStats,
    val health: Health,
    val master: String?,
    val disciples: List<String>
)

data class BasicInfo(
    val name: String,
    val age: Int,
    val gender: String,
    val position: String
)

data class RealmInfo(
    val currentRealm: String,
    val progress: Float
)
```

## DiscipleManagementService

```kotlin
class DiscipleManagementService(override val world: World) : EntityRelationContext {
    
    /// 弟子入门
    fun recruitDisciple(disciple: Entity, sect: Entity, generation: Int) {
        val membership = SectMembershipData(
            joinDate = System.currentTimeMillis(),
            generation = generation,
            position = Position.OUTER_DISCIPLE,
            totalContribution = 0
        )
        
        disciple.editor {
            it.addRelation<SectMembershipData>(target = sect, data = membership)
            it.addTag<InSectTag>()
            it.addComponent(Position.OUTER_DISCIPLE)
            it.addComponent(ContributionPoints(0))
        }
    }
    
    /// 晋升职位
    fun promote(disciple: Entity, newPosition: Position) {
        val sect = getSect(disciple) ?: return
        val membership = disciple.getRelation<SectMembershipData>(target = sect) ?: return
        
        disciple.editor {
            it.addRelation(
                target = sect,
                data = membership.copy(position = newPosition)
            )
            it.addComponent(newPosition)
        }
        
        logActivity(disciple, "晋升为 ${newPosition.name}")
    }
    
    /// 逐出宗门
    fun expel(disciple: Entity, reason: String) {
        val sect = getSect(disciple) ?: return
        
        disciple.editor {
            it.removeRelation<SectMembership>(target = sect)
            it.removeTag<InSectTag>()
            it.addTag<ExiledTag>()
        }
        
        logActivity(disciple, "被逐出宗门，原因：$reason")
        world.eventBus.emit(DiscipleExpelledEvent(disciple, reason))
    }
    
    /// 弟子死亡处理
    fun onDiscipleDeath(disciple: Entity, cause: String) {
        disciple.editor {
            it.removeTag<AliveTag>()
            it.removeTag<ActiveTag>()
            it.addTag<DeadTag>()
        }
        
        // 如果有徒弟，解除师徒关系
        val disciples = getDisciples(disciple)
        disciples.forEach { d ->
            d.editor {
                it.removeRelation<Mentorship>(target = disciple)
            }
        }
        
        logActivity(disciple, "死亡，原因：$cause")
    }
    
    private fun getSect(disciple: Entity): Entity? {
        return disciple.getRelation<SectMembership>()?.target
    }
    
    private fun getDisciples(master: Entity): List<Entity> {
        return world.query {
            entityFilter {
                relation(relations.relation<Mentorship>(target = master))
            }
        }.map { it.entity }
    }
}
```

## 依赖关系

- **依赖**：`core`、`disciple` 组件、所有标签、所有关系
- **被依赖**：UI 层、事件系统
