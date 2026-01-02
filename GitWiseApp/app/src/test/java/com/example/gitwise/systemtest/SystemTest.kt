package com.example.gitwise.systemtest

import com.example.gitwise.NaiveSimplifier.NaiveSimplifier
import com.example.gitwise.datatypes.Person
import com.example.gitwise.datatypes.Transaction
import com.example.gitwise.gitmanager.GitManager
import com.example.gitwise.transactionparser.readTransactions
import com.example.gitwise.transactionparser.writeTransactions
import org.junit.Test

import org.junit.Assert.*
import java.io.File


val repoPath = "C:\\Projects\\GitwiseData"
val dataFilePath = repoPath + "\\data.json"
val dataFile = File(dataFilePath)

class SystemTest {
    @Test
    fun writeSampleTransactions() {
        val transactions = listOf(
            Transaction(Person("Alice"), Person("Bob"), 100uL),
            Transaction(Person("Bob"), Person("Charlie"), 50uL)
        )

        writeTransactions(dataFile, transactions)
    }

    @Test
    fun addTransactionWithGit() {
        val gitManager = GitManager(
            File(repoPath),
            null,
            null
        )
        gitManager.pull()
        gitManager.checkout("data")

        var transactions = readTransactions(dataFile)
        transactions = transactions.plus(Transaction(Person("Alice"), Person("Bob"), 100uL))

        writeTransactions(dataFile, transactions)

        gitManager.commit("Add transaction")
    }

    @Test
    fun main() {
        val gitManager = GitManager(
            File(repoPath),
            null,
            null
        )
        gitManager.pull()
        gitManager.checkout("data")

        val transactions = readTransactions(dataFile)

        val simplifier = NaiveSimplifier()
        val simplifiedTransactions = simplifier.simplifiy(transactions)

        println(simplifiedTransactions)
    }
}