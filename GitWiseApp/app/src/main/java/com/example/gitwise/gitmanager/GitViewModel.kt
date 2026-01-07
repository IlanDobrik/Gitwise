package com.example.gitwise.gitmanager

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gitwise.config.getConfig
import com.example.gitwise.datatypes.Transaction
import com.example.gitwise.transactionparser.readTransactions
import com.example.gitwise.transactionparser.writeTransactions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

class GitViewModel : ViewModel() {

    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadTransactions(context: Context) {
        viewModelScope.launch {
            _isLoading.value = true
            
            // First load from disk immediately if possible
            val repoBase = getRepoBase(context)
            val dataFile = getDataFile(repoBase)
            
            if (dataFile.exists()) {
                val cachedTransactions = withContext(Dispatchers.IO) {
                    readTransactions(dataFile)
                }
                _transactions.value = cachedTransactions
            }
            
            // Then fetch from remote
            val newTransactions = withContext(Dispatchers.IO) {
                try {
                    getGitManager(repoBase, getConfig(context).commit).pull()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                readTransactions(dataFile)
            }
            
            _transactions.value = newTransactions
            _isLoading.value = false
        }
    }

    fun saveTransaction(context: Context, transaction: Transaction, onComplete: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val repoBase = getRepoBase(context)
            val dataFile = getDataFile(repoBase)
            
            val currentList = readTransactions(dataFile).toMutableList()
            val existingIndex = currentList.indexOfFirst { it.id == transaction.id }
            if (existingIndex != -1) {
                currentList[existingIndex] = transaction
            } else {
                currentList.add(transaction)
            }

            writeTransactions(dataFile, currentList)
            // Update local state immediately
            _transactions.value = currentList
            
            val config = getConfig(context)
            if (config.commit) {
                try {
                    val gitManager = getGitManager(repoBase, true)
                    gitManager.commitPush("Update transaction: ${transaction.id}")
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            
            withContext(Dispatchers.Main) {
                onComplete()
            }
        }
    }

    fun deleteTransaction(context: Context, transactionId: UUID, onComplete: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val repoBase = getRepoBase(context)
            val dataFile = getDataFile(repoBase)

            val currentList = readTransactions(dataFile).toMutableList()
            val wasRemoved = currentList.removeIf { it.id == transactionId }

            if (wasRemoved) {
                writeTransactions(dataFile, currentList)
                // Update local state immediately
                _transactions.value = currentList
                
                val config = getConfig(context)
                if (config.commit) {
                    try {
                        val gitManager = getGitManager(repoBase, true)
                        gitManager.commitPush("Delete transaction: $transactionId")
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            
            withContext(Dispatchers.Main) {
                onComplete()
            }
        }
    }
}
