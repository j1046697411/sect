package cn.jzl.ecs

import cn.jzl.di.*
import cn.jzl.ecs.addon.Injector
import cn.jzl.ecs.addon.Phase
import cn.jzl.ecs.addon.WorldSetup
import cn.jzl.ecs.addon.createAddon
import cn.jzl.ecs.archetype.ArchetypeService
import cn.jzl.ecs.component.componentAddon
import cn.jzl.ecs.entity.*
import cn.jzl.ecs.family.FamilyService
import cn.jzl.ecs.observer.observeAddon
import cn.jzl.ecs.query.EntityQueryContext
import cn.jzl.ecs.query.Query
import cn.jzl.ecs.query.QueryService
import cn.jzl.ecs.relation.RelationProvider
import cn.jzl.ecs.relation.RelationService

fun World.isActive(entity: Entity): Boolean = entityService.isActive(entity)

fun World.entity(
    configuration: EntityCreateContext.(Entity) -> Unit
): Entity = entityService.create(true, configuration)

fun World.entity(
    entityId: Int,
    configuration: EntityCreateContext.(Entity) -> Unit
): Entity = entityService.create(entityId, true, configuration)

fun World.entity(entity: Entity, configuration: EntityUpdateContext.(Entity) -> Unit) {
    entityService.configure(entity, true, configuration)
}

context(worldOwner: WorldOwner)
fun Entity.editor(configuration: EntityUpdateContext.(Entity) -> Unit): Unit = with(worldOwner) {
    world.entity(this@editor, configuration)
}

context(worldOwner: WorldOwner)
fun Entity.childOf(configuration: EntityCreateContext.(Entity) -> Unit): Entity = with(worldOwner){
    return world.childOf(this@childOf, configuration)
}

fun World.childOf(parent: Entity, configuration: EntityCreateContext.(Entity) -> Unit): Entity = entity {
    configuration(it)
    it.parent(parent)
}

context(worldOwner: WorldOwner)
fun Entity.instanceOf(configuration: EntityCreateContext.(Entity) -> Unit): Entity {
    return worldOwner.world.instanceOf(this, configuration)
}

fun World.instanceOf(prefab: Entity, configuration: EntityCreateContext.(Entity) -> Unit): Entity = entity {
    configuration(it)
    it.addRelation(components.instanceOf, prefab)
}

fun World.destroy(entity: Entity) {
    TODO("Not yet implemented")
}

context(worldOwner: WorldOwner)
fun Entity.destroy(): Unit = with(worldOwner) {
    world.destroy(this@destroy)
}

fun <E : EntityQueryContext> World.query(
    factory: World.() -> E
): Query<E> {
    return queryService.query(factory)
}

val archetypeAddon = createAddon<Unit>("archetypeAddon") {
    injects {
        this bind singleton { new(::ArchetypeService) }
    }
}

val entityAddon = createAddon<Unit>("entityAddon") {
    injects {
        this bind singleton { new(::EntityStoreImpl) }
        this bind singleton { new(::EntityService) }
    }
}

val relationAddon = createAddon<Unit>("relationAddon") {
    injects {
        this bind singleton { new(::RelationService) }
        this bind singleton { new(::RelationProvider) }
    }
}

val familyAddon = createAddon("familyAddon") {
    injects { this bind singleton { new(::FamilyService) } }
}

val codeAddon = createAddon<Unit>("codeAddon") {
    install(archetypeAddon)
    install(componentAddon)
    install(relationAddon)
    install(familyAddon)
    install(entityAddon)
    install(observeAddon)
    injects {
        this bind singleton { World(di) }
        this bind singleton { new(::PipelineImpl) }
        this bind singleton { new(::QueryService) }
    }
}

fun world(configuration: WorldSetup.() -> Unit): World {
    val mainBuilder = DIMainBuilder("world")
    val injector = Injector { mainBuilder.it() }
    val phaseTasks = mutableListOf<Pair<Phase, WorldOwner.() -> Unit>>()
    val worldSetup = WorldSetup(injector) { _, phase, phaseTask ->
        phaseTasks.add(phase to phaseTask)
    }
    worldSetup.configuration()
    worldSetup.install(codeAddon)
    val di = DI(mainBuilder)
    val world by di.instance<World>()
    val pipeline = world.pipeline
    for ((phase, task) in phaseTasks) {
        pipeline.runOnOrAfter(phase, task)
    }
    pipeline.runStartupTasks()
    return world
}