package com.example.gitwise.gitmanager

import android.content.Context
import android.widget.Toast
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

    private val _isFetchSuccessful = MutableStateFlow(true)
    val isFetchSuccessful: StateFlow<Boolean> = _isFetchSuccessful.asStateFlow()

    private val _repoMembers = MutableStateFlow<List<String>>(emptyList())
    val repoMembers: StateFlow<List<String>> = _repoMembers.asStateFlow()

    fun loadTransactions(context: Context) {
        viewModelScope.launch {
            _isLoading.value = true
            _isFetchSuccessful.value = false
            
            // First load from disk immediately if possible
            val repoBase = getRepoBase(context)
            val dataDirectory = getDataDirectory(repoBase)
            
            if (dataDirectory.exists()) {
                val cachedTransactions = withContext(Dispatchers.IO) {
                    readTransactions(dataDirectory).filter { it.isValid }
                }
                _transactions.value = cachedTransactions
                
                // Also load cached members if repo exists
                // Run in background to avoid blocking main thread or delaying transaction fetch
                viewModelScope.launch(Dispatchers.IO) {
                    try {
                        val gitManager = getGitManager(repoBase, getConfig(context).commit)
                        val members = gitManager.getRepoMembers()
                        _repoMembers.value = members
                    } catch (e: Exception) {
                        // Ignore if git manager fails here
                    }
                }
            }
            
            // Then fetch from remote
            val newTransactions = withContext(Dispatchers.IO) {
                var success = false
                var members: List<String> = emptyList()
                
                try {
                    // pull() now returns a boolean: true for success, false for reset performed
                    val gitManager = getGitManager(repoBase, getConfig(context).commit)
                    success = gitManager.pull()
                    
                    // After pull, fetch members
                    members = gitManager.getRepoMembers()
                    
                    // If pull succeeded (either normally or via reset), we consider fetch successful
                    _isFetchSuccessful.value = true
                } catch (e: Exception) {
                    e.printStackTrace()
                    _isFetchSuccessful.value = false
                }
                
                // If success is false, it means a reset happened inside pull()
                if (!success && _isFetchSuccessful.value) {
                     withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Sync failed. Reset to remote version.", Toast.LENGTH_LONG).show()
                     }
                }
                
                if (_isFetchSuccessful.value) {
                    _repoMembers.value = members
                }
                
                readTransactions(dataDirectory).filter { it.isValid }
            }
            
            _transactions.value = newTransactions
            _isLoading.value = false
        }
    }

    fun saveTransaction(context: Context, transaction: Transaction, oldTransactionId: UUID?, onComplete: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val repoBase = getRepoBase(context)
            val dataDirectory = getDataDirectory(repoBase)
            
            // Read all transactions, including invalid ones (we need to invalidate old ones)
            val allTransactions = readTransactions(dataDirectory).toMutableList()
            
            // If we are editing (oldTransactionId is provided), find the old one and invalidate it
            if (oldTransactionId != null) {
                val oldIndex = allTransactions.indexOfFirst { it.id == oldTransactionId }
                if (oldIndex != -1) {
                    val oldTransaction = allTransactions[oldIndex]
                    allTransactions[oldIndex] = oldTransaction.copy(isValid = false)
                }
            }
            
            // Add the new valid transaction
            allTransactions.add(transaction)

            writeTransactions(dataDirectory, allTransactions)
            
            // Update local state (only valid ones)
            _transactions.value = allTransactions.filter { it.isValid }
            
            val config = getConfig(context)
            if (config.commit) {
                try {
                    val gitManager = getGitManager(repoBase, true)
                    val message = if (oldTransactionId != null) {
                        "Update transaction: $oldTransactionId -> ${transaction.id}"
                    } else {
                        "Add transaction: ${transaction.id}"
                    }

                    gitManager.commit(message)
                    gitManager.push()
                    
                    _isFetchSuccessful.value = true
                    
                    // Update members list as current user might be new
                    val members = gitManager.getRepoMembers()
                    _repoMembers.value = members
                } catch (e: Exception) {
                    e.printStackTrace()
                    _isFetchSuccessful.value = false
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
            val dataDirectory = getDataDirectory(repoBase)

            val allTransactions = readTransactions(dataDirectory).toMutableList()
            val index = allTransactions.indexOfFirst { it.id == transactionId }
            
            if (index != -1) {
                // Instead of removing, we invalidate it
                val transaction = allTransactions[index]
                allTransactions[index] = transaction.copy(isValid = false)
                
                writeTransactions(dataDirectory, allTransactions)
                
                // Update local state (only valid ones)
                _transactions.value = allTransactions.filter { it.isValid }
                
                val config = getConfig(context)
                if (config.commit) {
                    try {
                        val gitManager = getGitManager(repoBase, true)
                        gitManager.commitPush("Invalidate transaction: $transactionId")
                        _isFetchSuccessful.value = true
                    } catch (e: Exception) {
                        e.printStackTrace()
                        _isFetchSuccessful.value = false
                    }
                }
            }
            
            withContext(Dispatchers.Main) {
                onComplete()
            }
        }
    }
}
