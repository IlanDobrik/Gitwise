package com.example.gitwise.transactionparser

import android.content.ContentValues.TAG
import android.util.Log
import com.example.gitwise.datatypes.Transaction
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

private val gson = Gson()

fun writeTransactions(file: File, transactions: List<Transaction>) {
    Log.i(TAG, "saving transactions")
    val json = gson.toJson(transactions)
    file.writeText(json)
    Log.i(TAG, "saved successfully")
}

fun readTransactions(file: File): List<Transaction> {
    Log.i(TAG, "reading transactions")
    if (!file.exists()) {
        Log.i(TAG, "file does not exists. Returing empty")
        return emptyList()
    }
    val json = file.readText()
    val type = object : TypeToken<List<Transaction>>() {}.type
    Log.i(TAG, "returning transuctions")

    try {
        return gson.fromJson(json, type)
    } catch (e: Exception) {
        Log.e(TAG, "Error reading transactions", e)
        return emptyList()
    }
}
