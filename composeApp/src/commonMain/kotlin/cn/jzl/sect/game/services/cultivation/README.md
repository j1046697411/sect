# 修炼服务 (Cultivation Service)

本模块实现修炼系统的核心逻辑，包括修炼进度计算、突破处理等。

## 核心功能

1. **修炼进度更新**：根据资质、功法、环境计算修炼速度
2. **突破处理**：境界突破的成功/失败逻辑
3. **效果计算**：丹药、设施、时辰等加成计算

## CultivationService

```kotlin
class CultivationService(override val world: World) : EntityRelationContext, System {
    
    override fun update(deltaTime: Float) {
        // 只查询正在修炼的实体
        world.query {
            entityFilter {
                hasTag<CultivatingTag>()
                hasComponent<CultivationRealm>()
                hasComponent<CultivationProgress>()
                hasComponent<InnateTalent>()
            }
        }.forEach { ctx ->
            processCultivation(ctx.entity, ctx, deltaTime)
        }
    }
    
    private fun processCultivation(
        entity: Entity,
        ctx: CultivationContext,
        deltaTime: Float
    ) {
        val realm = ctx.getComponent<CultivationRealm>()
        val progress = ctx.getComponent<CultivationProgress>()
        val talent = ctx.getComponent<InnateTalent>()
        
        // 计算修炼速度
        val baseSpeed = calculateBaseSpeed(talent, realm)
        val bonuses = calculateBonuses(entity)
        val finalSpeed = baseSpeed * bonuses.speedMultiplier
        
        // 更新进度
        val newProgress = progress.percentage + finalSpeed * deltaTime
        
        if (newProgress >= 100f) {
            // 达到100%，尝试突破
            attemptBreakthrough(entity, realm)
        } else {
            entity.editor {
                it.addComponent(CultivationProgress(newProgress))
            }
        }
    }
    
    /// 计算基础修炼速度
    fun calculateBaseSpeed(talent: InnateTalent, realm: CultivationRealm): Float {
        // 基础速度 = (根骨*0.5 + 悟性*0.3 + 福缘*0.2) / 100 * 基础系数
        val attributeFactor = (talent.rootBone * 0.5f + 
                              talent.understanding * 0.3f + 
                              talent.luck * 0.2f) / 100f
        
        // 境界越高修炼越慢（难度增加）
        val realmDifficulty = 1f - (realm.level * 0.02f)
        
        // 每小时进度百分比
        return attributeFactor * realmDifficulty * 2f  // 2%是基础速度
    }
    
    /// 计算各种加成
    fun calculateBonuses(entity: Entity): CultivationBonuses {
        var speedMultiplier = 1.0f
        var breakthroughBonus = 0.0f
        
        // 1. 时辰加成
        val gameTime = world.getSingleton<GameTime>()
        if (gameTime.hour == 23 || gameTime.hour == 0) {
            speedMultiplier += 0.2f  // 子时修炼+20%
        }
        
        // 2. 功法加成
        val technique = entity.getComponent<TechniqueProgress>()
        if (technique != null && entity.hasTag<MainTechnique>()) {
            val techniqueData = getTechniqueData(technique.techniqueId)
            speedMultiplier += techniqueData.effects["cultivation_speed"] ?: 0f
        }
        
        // 3. 设施加成
        val facility = getCurrentFacility(entity)
        if (facility != null) {
            val effect = facility.getComponent<CultivationFacilityEffect>()
            if (effect != null) {
                speedMultiplier += effect.speedBonus
                breakthroughBonus += effect.breakthroughBonus
            }
        }
        
        // 4. 丹药加成
        val activeAid = entity.getComponent<ActiveCultivationAid>()
        if (activeAid != null) {
            activeAid.pillsConsumed.forEach { pillId ->
                val pill = getPillData(pillId)
                if (pill.type == PillType.CULTIVATION) {
                    speedMultiplier += pill.effects.firstOrNull()?.effectValue ?: 0f
                }
            }
        }
        
        // 5. 阵法加成
        if (activeAid?.formationId != null) {
            val formation = world.getEntity(activeAid.formationId)
            val formationEffect = formation?.getComponent<FormationEffect>()
            speedMultiplier += formationEffect?.cultivationBonus ?: 0f
        }
        
        return CultivationBonuses(speedMultiplier, breakthroughBonus)
    }
    
    data class CultivationBonuses(
        val speedMultiplier: Float,
        val breakthroughBonus: Float
    )
}
```

## 突破处理

```kotlin
/// 尝试突破到下一境界
fun attemptBreakthrough(entity: Entity, currentRealm: CultivationRealm) {
    val nextRealm = currentRealm.next()
        ?: return  // 已达最高境界
    
    // 检查突破条件
    if (!checkBreakthroughRequirements(entity, nextRealm)) {
        // 条件不满足，保持满进度但不突破
        return
    }
    
    // 计算成功率
    val successRate = calculateBreakthroughSuccessRate(entity, currentRealm, nextRealm)
    
    // 尝试突破
    if (Random.nextFloat() < successRate) {
        onBreakthroughSuccess(entity, currentRealm, nextRealm)
    } else {
        onBreakthroughFailure(entity, currentRealm)
    }
}

/// 计算突破成功率
fun calculateBreakthroughSuccessRate(
    entity: Entity,
    from: CultivationRealm,
    to: CultivationRealm
): Float {
    // 基础成功率
    var rate = to.baseBreakthroughRate
    
    val talent = entity.getComponent<InnateTalent>()!!
    
    // 资质加成
    rate += (talent.rootBone + talent.understanding) / 1000f  // +0-20%
    rate += talent.luck / 500f  // 福缘额外加成
    
    // 功法加成
    val technique = entity.getComponent<TechniqueProgress>()
    if (technique != null) {
        rate += technique.mastery * 0.1f  // 功法熟练度
    }
    
    // 设施加成
    val bonuses = calculateBonuses(entity)
    rate += bonuses.breakthroughBonus
    
    // 突破丹加成
    val aid = entity.getComponent<ActiveCultivationAid>()
    aid?.pillsConsumed?.forEach { pillId ->
        val pill = getPillData(pillId)
        if (pill.type == PillType.BREAKTHROUGH) {
            rate += pill.effects.firstOrNull()?.effectValue ?: 0f
        }
    }
    
    // 限制在合理范围
    return rate.coerceIn(0.01f, 0.95f)  // 最低1%，最高95%
}

/// 突破成功处理
private fun onBreakthroughSuccess(
    entity: Entity,
    from: CultivationRealm,
    to: CultivationRealm
) {
    // 1. 更新境界
    entity.editor {
        it.addComponent(to)
        it.addComponent(CultivationProgress(0f))
    }
    
    // 2. 移除突破尝试标记
    entity.editor {
        it.removeTag<InBreakthroughTag>()
    }
    
    // 3. 增加属性
    increaseAttributesOnBreakthrough(entity, to)
    
    // 4. 增加寿命
    val currentLifespan = entity.getComponent<Lifespan>()
    if (currentLifespan != null) {
        entity.editor {
            it.addComponent(Lifespan(currentLifespan.years + to.lifespanBonus))
        }
    }
    
    // 5. 记录突破历史
    addBreakthroughRecord(entity, from, to, success = true)
    
    // 6. 触发事件
    world.eventBus.emit(BreakthroughSuccessEvent(entity, from, to))
    
    // 7. 转为空闲（需要稳固境界）
    entity.editor {
        it.removeTag<CultivatingTag>()
        it.addTag<IdleTag>()
    }
    
    // 8. 记录日志
    val name = entity.getComponent<EntityName>()?.value ?: "弟子"
    logActivity(entity, "$name 成功突破至 ${to.displayName}！")
}

/// 突破失败处理
private fun onBreakthroughFailure(entity: Entity, from: CultivationRealm) {
    // 1. 保留部分进度（50%）
    entity.editor {
        it.addComponent(CultivationProgress(50f))
    }
    
    // 2. 受伤
    val severity = Random.nextInt(1, 4)
    entity.editor {
        it.addComponent(Injury(
            severity = severity,
            recoveryRate = 5f,
            cause = "突破失败"
        ))
        it.addTag<InjuredTag>()
    }
    
    // 3. 记录突破历史
    val to = from.next()!!
    addBreakthroughRecord(entity, from, to, success = false)
    
    // 4. 触发事件
    world.eventBus.emit(BreakthroughFailureEvent(entity, from))
    
    // 5. 转为受伤状态
    entity.editor {
        it.removeTag<CultivatingTag>()
        it.removeTag<InBreakthroughTag>()
    }
    
    // 6. 记录日志
    val name = entity.getComponent<EntityName>()?.value ?: "弟子"
    logActivity(entity, "$name 突破失败，身受轻伤")
}

/// 突破时增加属性
private fun increaseAttributesOnBreakthrough(
    entity: Entity,
    newRealm: CultivationRealm
) {
    val talent = entity.getComponent<InnateTalent>()!!
    val stats = entity.getComponent<CombatStats>()!!
    
    // 战斗属性增长
    val growth = if (newRealm.isMajorRealm()) {
        Random.nextInt(2, 5)  // 大境界突破增长更多
    } else {
        Random.nextInt(1, 3)
    }
    
    entity.editor {
        it.addComponent(stats.copy(
            strength = stats.strength + growth,
            intelligence = stats.intelligence + growth,
            endurance = stats.endurance + growth
        ))
    }
    
    // 大境界突破可能增加先天资质（罕见）
    if (newRealm.isMajorRealm() && Random.nextFloat() < 0.3f) {
        entity.editor {
            it.addComponent(talent.copy(
                rootBone = talent.rootBone + if (Random.nextBoolean()) 1 else 0,
                understanding = talent.understanding + if (Random.nextBoolean()) 1 else 0
            ))
        }
    }
}
```

## 突破条件检查

```kotlin
fun checkBreakthroughRequirements(
    entity: Entity,
    targetRealm: CultivationRealm
): Boolean {
    // 1. 检查最低年龄（某些境界有年龄要求）
    val age = entity.getComponent<Age>()
    if (age != null && age.years < targetRealm.level * 5) {
        return false  // 年龄太小
    }
    
    // 2. 检查健康状况
    if (entity.hasTag<InjuredTag>()) {
        return false  // 受伤不能突破
    }
    
    // 3. 检查境界要求
    val currentRealm = entity.getComponent<CultivationRealm>()!!
    if (currentRealm.next() != targetRealm) {
        return false  // 不是连续突破
    }
    
    // 4. 检查资源（某些突破需要消耗资源）
    val resources = entity.getComponent<ResourceInventory>()
    if (resources != null) {
        // 检查是否有突破丹等
        val requiredItems = getBreakthroughRequirements(targetRealm)
        // ... 资源检查逻辑
    }
    
    return true
}
```

## 依赖关系

- **依赖**：`core`、`disciple`、`cultivation` 组件、`status` 标签
- **被依赖**：`ai` 服务（AI 决定何时修炼）
