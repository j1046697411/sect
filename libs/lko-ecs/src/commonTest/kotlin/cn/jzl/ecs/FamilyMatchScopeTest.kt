package cn.jzl.ecs

import cn.jzl.ecs.addon.components
import cn.jzl.ecs.addon.createAddon
import cn.jzl.ecs.component.componentId
import cn.jzl.ecs.entity.EntityRelationContext
import cn.jzl.ecs.family.FamilyMatchScope
import cn.jzl.ecs.relation.Relation
import cn.jzl.ecs.relation.component
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class FamilyMatchScopeTest : EntityRelationContext {
    
    override lateinit var world: World
    
    @BeforeTest
    fun setup() {
        world = world {
            install(testAddon)
        }
    }

    private val testAddon = createAddon<Unit>("test") {
        components {
            world.componentId<Position>()
            world.componentId<Velocity>()
        }
    }

    @Test
    fun testFamilyMatchScope_allArchetypeBits() {
        val familyService = world.familyService
        
        val allBits = familyService.allArchetypeBits
        
        assertNotNull(allBits)
    }

    @Test
    fun testFamilyMatchScope_getArchetypeBits() {
        val familyService = world.familyService
        
        val positionId = world.components.id<Position>()
        val positionRelation = Relation(positionId, world.components.componentOf)
        
        val archetypeBits = familyService.getArchetypeBits(positionRelation)
        
        assertNotNull(archetypeBits)
    }

    @Test
    fun testFamilyMatchScope_getArchetypeBitsForUnknownRelation() {
        val familyService = world.familyService
        
        val velocityId = world.components.id<Velocity>()
        val unknownRelation = Relation(velocityId, world.components.componentOf)
        
        val archetypeBits = familyService.getArchetypeBits(unknownRelation)
        
        assertNotNull(archetypeBits)
    }

    @Test
    fun testFamilyMatchScope_implementsInterface() {
        val familyService: FamilyMatchScope = world.familyService
        
        assertNotNull(familyService.allArchetypeBits)
        assertNotNull(familyService.getArchetypeBits(Relation(world.components.id<Position>(), world.components.componentOf)))
    }

    private data class Position(val x: Int, val y: Int)
    private data class Velocity(val dx: Int, val dy: Int)
}
