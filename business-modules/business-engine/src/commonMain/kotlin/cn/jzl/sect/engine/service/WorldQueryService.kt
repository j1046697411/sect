package cn.jzl.sect.engine.service

import cn.jzl.ecs.World
import cn.jzl.ecs.entity.id
import cn.jzl.ecs.query
import cn.jzl.ecs.query.EntityQueryContext
import cn.jzl.ecs.query.forEach
import cn.jzl.sect.core.cultivation.CultivationProgress
import cn.jzl.sect.core.cultivation.Realm
import cn.jzl.sect.core.disciple.Age
import cn.jzl.sect.core.facility.Facility
import cn.jzl.sect.core.resource.ResourceProduction
import cn.jzl.sect.core.resource.ResourceType
import cn.jzl.sect.core.sect.Sect
import cn.jzl.sect.core.sect.SectPositionInfo
import cn.jzl.sect.core.sect.SectPositionType
import cn.jzl.sect.core.sect.SectTreasury
import cn.jzl.sect.core.time.GameTime
import cn.jzl.sect.core.vitality.Spirit
import cn.jzl.sect.core.vitality.Vitality

/**
 * ECS世界查询服务
 * 为UI层提供查询ECS世界数据的接口
 */
class WorldQueryService(private val world: World) {

    /**
     * 宗门信息DTO
     */
    data class SectInfo(
        val name: String,
        val leaderId: Long,
        val foundedYear: Int,
        val spiritStones: Long,
        val contributionPoints: Long,
        val currentYear: Int,
        val currentMonth: Int,
        val currentDay: Int
    )

    /**
     * 弟子信息DTO
     */
    data class DiscipleInfo(
        val id: Long,
        val position: SectPositionType,
        val realm: Realm,
        val layer: Int,
        val cultivation: Long,
        val maxCultivation: Long,
        val age: Int,
        val health: Int,
        val maxHealth: Int,
        val spirit: Int,
        val maxSpirit: Int
    )

    /**
     * 设施信息DTO
     */
    data class FacilityInfo(
        val id: Long,
        val type: String,
        val level: Int,
        val capacity: Int,
        val efficiency: Float
    )

    /**
     * 资源生产信息DTO
     */
    data class ResourceProductionInfo(
        val id: Long,
        val type: ResourceType,
        val baseOutput: Long,
        val efficiency: Float,
        val isActive: Boolean
    )

    /**
     * 查询宗门信息
     */
    fun querySectInfo(): SectInfo? {
        val query = world.query { SectQueryContext(world) }
        var sectInfo: SectInfo? = null

        query.forEach { ctx ->
            sectInfo = SectInfo(
                name = ctx.sect.name,
                leaderId = ctx.sect.leaderId,
                foundedYear = ctx.sect.foundedYear,
                spiritStones = ctx.treasury.spiritStones,
                contributionPoints = ctx.treasury.contributionPoints,
                currentYear = ctx.gameTime.year,
                currentMonth = ctx.gameTime.month,
                currentDay = ctx.gameTime.day
            )
        }

        return sectInfo
    }

    /**
     * 查询所有弟子
     */
    fun queryAllDisciples(): List<DiscipleInfo> {
        val query = world.query { DiscipleQueryContext(world) }
        val disciples = mutableListOf<DiscipleInfo>()

        query.forEach { ctx ->
            disciples.add(
                DiscipleInfo(
                    id = ctx.entity.id.toLong(),
                    position = ctx.position.position,
                    realm = ctx.cultivation.realm,
                    layer = ctx.cultivation.layer,
                    cultivation = ctx.cultivation.cultivation,
                    maxCultivation = ctx.cultivation.maxCultivation,
                    age = ctx.age.age,
                    health = ctx.vitality.currentHealth,
                    maxHealth = ctx.vitality.maxHealth,
                    spirit = ctx.spirit.currentSpirit,
                    maxSpirit = ctx.spirit.maxSpirit
                )
            )
        }

        return disciples
    }

    /**
     * 按职务筛选弟子
     */
    fun queryDisciplesByPosition(position: SectPositionType): List<DiscipleInfo> {
        return queryAllDisciples().filter { it.position == position }
    }

    /**
     * 按境界筛选弟子
     */
    fun queryDisciplesByRealm(realm: Realm): List<DiscipleInfo> {
        return queryAllDisciples().filter { it.realm == realm }
    }

    /**
     * 查询弟子统计
     */
    fun queryDiscipleStatistics(): DiscipleStatistics {
        val allDisciples = queryAllDisciples()

        return DiscipleStatistics(
            totalCount = allDisciples.size,
            innerCount = allDisciples.count { it.position == SectPositionType.DISCIPLE_INNER },
            outerCount = allDisciples.count { it.position == SectPositionType.DISCIPLE_OUTER },
            elderCount = allDisciples.count { it.position == SectPositionType.ELDER },
            qiRefiningCount = allDisciples.count { it.realm == Realm.QI_REFINING },
            foundationCount = allDisciples.count { it.realm == Realm.FOUNDATION }
        )
    }

    /**
     * 查询所有设施
     */
    fun queryAllFacilities(): List<FacilityInfo> {
        val query = world.query { FacilityQueryContext(world) }
        val facilities = mutableListOf<FacilityInfo>()

        query.forEach { ctx ->
            facilities.add(
                FacilityInfo(
                    id = ctx.entity.id.toLong(),
                    type = ctx.facility.type.name,
                    level = ctx.facility.level,
                    capacity = ctx.facility.capacity,
                    efficiency = ctx.facility.efficiency
                )
            )
        }

        return facilities
    }

    /**
     * 查询资源生产设施
     */
    fun queryResourceProductions(): List<ResourceProductionInfo> {
        val query = world.query { ResourceProductionQueryContext(world) }
        val productions = mutableListOf<ResourceProductionInfo>()

        query.forEach { ctx ->
            productions.add(
                ResourceProductionInfo(
                    id = ctx.entity.id.toLong(),
                    type = ctx.production.type,
                    baseOutput = ctx.production.baseOutput,
                    efficiency = ctx.production.efficiency,
                    isActive = ctx.production.isActive
                )
            )
        }

        return productions
    }

    /**
     * 查询上下文 - 宗门
     */
    class SectQueryContext(world: World) : EntityQueryContext(world) {
        val sect: Sect by component()
        val treasury: SectTreasury by component()
        val gameTime: GameTime by component()
    }

    /**
     * 查询上下文 - 弟子
     */
    class DiscipleQueryContext(world: World) : EntityQueryContext(world) {
        val position: SectPositionInfo by component()
        val cultivation: CultivationProgress by component()
        val age: Age by component()
        val vitality: Vitality by component()
        val spirit: Spirit by component()
    }

    /**
     * 查询上下文 - 设施
     */
    class FacilityQueryContext(world: World) : EntityQueryContext(world) {
        val facility: Facility by component()
    }

    /**
     * 查询上下文 - 资源生产
     */
    class ResourceProductionQueryContext(world: World) : EntityQueryContext(world) {
        val production: ResourceProduction by component()
    }

    /**
     * 弟子统计数据
     */
    data class DiscipleStatistics(
        val totalCount: Int,
        val innerCount: Int,
        val outerCount: Int,
        val elderCount: Int,
        val qiRefiningCount: Int,
        val foundationCount: Int
    )
}
