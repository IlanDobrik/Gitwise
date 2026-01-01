package com.example.gitwise.simplifier

import com.example.gitwise.NaiveSimplifier.NaiveSimplifier
import com.example.gitwise.datatypes.Transaction
import com.example.gitwise.datatypes.Person
import org.junit.Assert.assertEquals
import org.junit.Test

class NaiveSimplifierTest {

    @Test
    fun testSimplifyNoTransactions() {
        val simplifier = NaiveSimplifier()
        val result = simplifier.simplifiy(emptyList())
        assertEquals(0, result.size)
    }

    @Test
    fun testSimplifySingleTransaction() {
        val alice = Person("Alice")
        val bob = Person("Bob")
        val transactions = listOf(Transaction(alice, bob, 100uL))

        val simplifier = NaiveSimplifier()
        val result = simplifier.simplifiy(transactions)

        assertEquals(1, result.size)
        assertEquals(alice, result[0].payer)
        assertEquals(bob, result[0].ower)
        assertEquals(100uL, result[0].sum)
    }

    @Test
    fun testSimplifySimpleOffset() {
        val alice = Person("Alice")
        val bob = Person("Bob")
        // Alice pays Bob 100, Bob pays Alice 50 -> Alice pays Bob 50
        val transactions = listOf(
            Transaction(alice, bob, 100uL),
            Transaction(bob, alice, 50uL)
        )

        val simplifier = NaiveSimplifier()
        val result = simplifier.simplifiy(transactions)

        assertEquals(1, result.size)
        assertEquals(alice, result[0].payer)
        assertEquals(bob, result[0].ower)
        assertEquals(50uL, result[0].sum)
    }

    @Test
    fun testSimplifyCircularDebt() {
        val alice = Person("Alice")
        val bob = Person("Bob")
        val charlie = Person("Charlie")
        // Alice pays Bob 100, Bob pays Charlie 100, Charlie pays Alice 100 -> All settled
        val transactions = listOf(
            Transaction(alice, bob, 100uL),
            Transaction(bob, charlie, 100uL),
            Transaction(charlie, alice, 100uL)
        )

        val simplifier = NaiveSimplifier()
        val result = simplifier.simplifiy(transactions)

        assertEquals(0, result.size)
    }
    
    @Test
    fun testSimplifyTransitiveDebt() {
        val alice = Person("Alice")
        val bob = Person("Bob")
        val charlie = Person("Charlie")
        // Alice pays Bob 100, Bob pays Charlie 50
        // Net: Bob gets 50 (from Alice), Charlie gets 50 (from Bob) -> Alice pays 100 total
        // Wait, logic:
        // Alice -> Bob 100. Bob net +100
        // Bob -> Charlie 50. Bob net -50. Charlie net +50.
        // Total Net: Alice -100, Bob +50, Charlie +50.
        // Result should be Alice -> Bob 50, Alice -> Charlie 50 (or similar combination)
        
        val transactions = listOf(
            Transaction(alice, bob, 100uL),
            Transaction(bob, charlie, 50uL)
        )

        val simplifier = NaiveSimplifier()
        val result = simplifier.simplifiy(transactions)
        
        // Check net balances indirectly by summing result
        val netBalances = mutableMapOf<Person, Long>()
        result.forEach { 
             netBalances[it.ower] = (netBalances[it.ower] ?: 0L) + it.sum.toLong()
             netBalances[it.payer] = (netBalances[it.payer] ?: 0L) - it.sum.toLong()
        }
        
        assertEquals(-100L, netBalances[alice])
        assertEquals(50L, netBalances[bob])
        assertEquals(50L, netBalances[charlie])
    }
}
