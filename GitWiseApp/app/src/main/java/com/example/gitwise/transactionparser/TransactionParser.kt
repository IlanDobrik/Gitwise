package com.example.gitwise.transactionparser

import com.example.gitwise.logger.TAG
import android.util.Log
import com.example.gitwise.datatypes.Transaction
import com.google.gson.Gson
import java.io.File

private val gson = Gson()

fun writeTransactions(directory: File, transactions: List<Transaction>) {
    Log.i(TAG, "saving transactions to directory: ${directory.absolutePath}")

    if (directory.exists() && !directory.isDirectory) {
        Log.w(TAG, "Target path exists but is not a directory. Deleting to replace with directory.")
        directory.delete()
    }
    
    if (!directory.exists()) {
        if (!directory.mkdirs()) {
            Log.e(TAG, "Failed to create directory: ${directory.absolutePath}")
            return
        }
    }

    val activeFilenames = mutableSetOf<String>()

    for (transaction in transactions) {
        val filename = "${transaction.id}.json"
        val file = File(directory, filename)
        try {
            val json = gson.toJson(transaction)
            file.writeText(json)
            activeFilenames.add(filename)
        } catch (e: Exception) {
            Log.e(TAG, "Error writing transaction ${transaction.id}", e)
        }
    }

    // Delete files that are no longer present in the transaction list
    directory.listFiles()?.forEach { file ->
        if (file.isFile && file.name.endsWith(".json") && !activeFilenames.contains(file.name)) {
            Log.i(TAG, "Deleting removed transaction file: ${file.name}")
            file.delete()
        }
    }
    
    Log.i(TAG, "saved successfully")
}

fun readTransactions(directory: File): List<Transaction> {
    Log.i(TAG, "reading transactions from directory: ${directory.absolutePath}")
    
    if (!directory.exists() || !directory.isDirectory) {
        Log.w(TAG, "Directory does not exist or is not a directory. Returning empty.")
        return emptyList()
    }

    val files = directory.listFiles() ?: return emptyList()
    val transactions = mutableListOf<Transaction>()

    for (file in files) {
        if (file.isFile && file.name.endsWith(".json")) {
            try {
                val json = file.readText()
                val transaction = gson.fromJson(json, Transaction::class.java)
                transactions.add(transaction)
            } catch (e: Exception) {
                Log.w(TAG, "Error reading transaction from file: ${file.name}", e)
            }
        }
    }
    
    Log.i(TAG, "returning ${transactions.size} transactions")
    return transactions
}
