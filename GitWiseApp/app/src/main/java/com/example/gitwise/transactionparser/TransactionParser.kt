package com.example.gitwise.transactionparser

import com.example.gitwise.datatypes.Transaction
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

private val gson = Gson()

fun writeTransactions(file: File, transactions: List<Transaction>) {
    val json = gson.toJson(transactions)
    file.writeText(json)
}

fun readTransactions(file: File): List<Transaction> {
    if (!file.exists()) return emptyList()
    val json = file.readText()
    val type = object : TypeToken<List<Transaction>>() {}.type
    return gson.fromJson(json, type)
}
