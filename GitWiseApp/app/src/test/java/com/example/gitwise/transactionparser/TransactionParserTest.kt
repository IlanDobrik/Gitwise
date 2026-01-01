package com.example.gitwise.transactionparser

import com.example.gitwise.datatypes.Person
import com.example.gitwise.datatypes.Transaction
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.io.File
import kotlin.collections.get

class TransactionParserTest {

    private lateinit var testFile: File

    @Before
    fun setUp() {
        testFile = File.createTempFile("test_transactions", ".json")
    }

    @After
    fun tearDown() {
        testFile.delete()
    }

    @Test
    fun testWriteAndReadTransactions() {
        val transactions = listOf(
            Transaction(Person("Alice"), Person("Bob"), 100uL),
            Transaction(Person("Bob"), Person("Charlie"), 50uL)
        )

        writeTransactions(testFile, transactions)

        val readTransactions = readTransactions(testFile)

        Assert.assertEquals(transactions.size, readTransactions.size)
        Assert.assertEquals(transactions[0].payer.name, readTransactions[0].payer.name)
        Assert.assertEquals(transactions[0].ower.name, readTransactions[0].ower.name)
        Assert.assertEquals(transactions[0].sum, readTransactions[0].sum)
        Assert.assertEquals(transactions[1].payer.name, readTransactions[1].payer.name)
        Assert.assertEquals(transactions[1].ower.name, readTransactions[1].ower.name)
        Assert.assertEquals(transactions[1].sum, readTransactions[1].sum)
    }

    @Test
    fun testReadEmptyFile() {
        testFile.delete() // Ensure file doesn't exist
        val readTransactions = readTransactions(testFile)
        Assert.assertEquals(0, readTransactions.size)
    }
}