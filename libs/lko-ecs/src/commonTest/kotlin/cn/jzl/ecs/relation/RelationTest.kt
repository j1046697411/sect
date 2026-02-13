package cn.jzl.ecs.relation

import cn.jzl.ecs.component.ComponentId
import cn.jzl.ecs.entity.Entity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class RelationTest {

    @Test
    fun testRelationCreation() {
        val kind = ComponentId(1)
        val target = Entity(100)
        
        val relation = Relation(kind, target)
        
        assertEquals(kind, relation.kind)
        assertEquals(target, relation.target)
    }

    @Test
    fun testRelationToString() {
        val kind = ComponentId(5)
        val target = Entity(200)
        
        val relation = Relation(kind, target)
        
        val str = relation.toString()
        assert(str.contains("kind"))
        assert(str.contains("target"))
    }

    @Test
    fun testRelationComparison() {
        val kind1 = ComponentId(1)
        val target1 = Entity(100)
        
        val kind2 = ComponentId(2)
        val target2 = Entity(200)
        
        val relation1 = Relation(kind1, target1)
        val relation2 = Relation(kind2, target2)
        
        assertTrue(relation1 < relation2)
    }

    @Test
    fun testRelationEquality() {
        val kind = ComponentId(5)
        val target = Entity(100)
        
        val relation1 = Relation(kind, target)
        val relation2 = Relation(kind, target)
        
        assertEquals(relation1, relation2)
    }

    @Test
    fun testRelationInequality() {
        val kind1 = ComponentId(5)
        val target1 = Entity(100)
        
        val kind2 = ComponentId(5)
        val target2 = Entity(200)
        
        val relation1 = Relation(kind1, target1)
        val relation2 = Relation(kind2, target2)
        
        assertNotEquals(relation1, relation2)
    }

    @Test
    fun testRelationWithDifferentKind() {
        val kind1 = ComponentId(1)
        val kind2 = ComponentId(2)
        val target = Entity(100)
        
        val relation1 = Relation(kind1, target)
        val relation2 = Relation(kind2, target)
        
        assertNotEquals(relation1, relation2)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testRelationWithInvalidTarget() {
        val kind = ComponentId(1)
        
        Relation(kind, Entity.ENTITY_INVALID)
    }
}
