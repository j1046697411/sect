package cn.jzl.di

import org.kodein.type.erased
import org.kodein.type.generic
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

// Test interfaces for type hierarchy
private interface Flyable {
    fun fly()
}

private interface Bird : Flyable

class TypeCheckerTest {

    // Test classes for type hierarchy
    open class Animal
    class Dog : Animal()
    class Cat : Animal()

    @Test
    fun testUpCheckerAllowsAssignableTypes() {
        val animalType = erased<Animal>()
        val checker = TypeChecker.Up(animalType)
        
        assertTrue(checker.check(erased<Animal>()))
        assertTrue(checker.check(erased<Dog>()))
        assertTrue(checker.check(erased<Cat>()))
    }

    @Test
    fun testUpCheckerRejectsUnrelatedTypes() {
        val checker = TypeChecker.Up(erased<Animal>())
        
        assertFalse(checker.check(erased<String>()))
        assertFalse(checker.check(erased<Int>()))
    }

    @Test
    fun testDownCheckerAllowsSupertypes() {
        val checker = TypeChecker.Down(erased<Dog>())
        
        assertTrue(checker.check(erased<Dog>()))
        assertTrue(checker.check(erased<Animal>()))
    }

    @Test
    fun testDownCheckerRejectsUnrelatedTypes() {
        val checker = TypeChecker.Down(erased<Dog>())
        
        assertFalse(checker.check(erased<Cat>()))
        assertFalse(checker.check(erased<String>()))
    }

    @Test
    fun testUpCheckerWithGenericType() {
        // Test with a generic type like List<Animal>
        val listAnimalType = generic<List<Animal>>()
        val checker = TypeChecker.Up(listAnimalType)
        
        // Should check assignability
        assertTrue(checker.check(generic<List<Animal>>()))
    }

    @Test
    fun testDownCheckerWithGenericType() {
        val listAnimalType = generic<List<Animal>>()
        val checker = TypeChecker.Down(listAnimalType)
        
        assertTrue(checker.check(generic<List<Animal>>()))
    }

    @Test
    fun testUpCheckerToString() {
        val checker = TypeChecker.Up(erased<Animal>())
        val str = checker.toString()
        assertTrue(str.contains("Up"))
    }

    @Test
    fun testDownCheckerToString() {
        val checker = TypeChecker.Down(erased<Dog>())
        val str = checker.toString()
        assertTrue(str.contains("Down"))
    }

    @Test
    fun testUpCheckerWithInterface() {
        val checker = TypeChecker.Up(erased<Flyable>())
        
        assertTrue(checker.check(erased<Flyable>()))
        assertTrue(checker.check(erased<Bird>()))
    }

    @Test
    fun testDownCheckerWithInterface() {
        val checker = TypeChecker.Down(erased<Bird>())
        
        assertTrue(checker.check(erased<Bird>()))
        assertTrue(checker.check(erased<Flyable>()))
    }
}
