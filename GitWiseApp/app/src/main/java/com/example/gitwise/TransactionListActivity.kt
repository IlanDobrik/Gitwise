package com.example.gitwise

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.gitwise.datatypes.Transaction
import com.example.gitwise.datatypes.Person
import com.example.gitwise.gitmanager.GitManager
import com.example.gitwise.transactionparser.readTransactions
import com.example.gitwise.ui.theme.GitwiseTheme
import java.io.File

class TransactionListActivity : ComponentActivity() {
    private val transactionsState = mutableStateOf<List<Transaction>>(emptyList())

    fun getTransactions(context : Context) : List<Transaction> {
        // TODO dynamic
        val repoPath = context.filesDir.absolutePath + "\\GitWise"
        val dataFilePath = "$repoPath\\data.json"
        val dataFile = File(dataFilePath)

        val gitManager = GitManager(
            File(repoPath),
            null,
            null
        )
        try {
            gitManager.pull()
            gitManager.checkout("data")
            gitManager.pull() // is needed?
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return readTransactions(File(dataFilePath))
    }

    private fun refreshData(context: Context) {
        val transactions = getTransactions(context)
        val nativeSimplifier = com.example.gitwise.NaiveSimplifier.NaiveSimplifier()
        transactionsState.value = nativeSimplifier.simplifiy(transactions)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        refreshData(this)

        setContent {
            GitwiseTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    floatingActionButton = {
                        FloatingActionButton(onClick = {
                            val intent = Intent(this, TransactionEditorActivity::class.java)
                            startActivity(intent)
                        }) {
                            Icon(Icons.Filled.Add, contentDescription = "Add Transaction")
                        }
                    }
                ) { innerPadding ->
                    TransactionList(
                        transactions = transactionsState.value,
                        modifier = Modifier.padding(innerPadding),
                        onEditClick = { transaction ->
                            val intent = Intent(this, TransactionEditorActivity::class.java).apply {
                                putExtra("transaction", transaction)
                            }
                            startActivity(intent)
                        }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        refreshData(this)
    }
}

@Composable
fun TransactionList(
    transactions: List<Transaction>,
    modifier: Modifier = Modifier,
    onEditClick: (Transaction) -> Unit
) {
    LazyColumn(modifier = modifier) {
        items(transactions) { transaction ->
            TransactionItem(transaction, onEdit = { onEditClick(transaction) })
        }
    }
}

@Composable
fun TransactionItem(transaction: Transaction, onEdit: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = "${transaction.payer.name} owes ${transaction.ower.name}")
            Text(text = "Amount: ${transaction.sum}")
        }
        IconButton(onClick = onEdit) {
            Icon(Icons.Filled.Edit, contentDescription = "Edit Transaction")
        }
    }
}


@Preview(showBackground = true)
@Composable
fun TransactionListPreview() {
    // Example data
    val transactions = listOf(
        Transaction(Person("Alice"), Person("Bob"), 100uL),
        Transaction(Person("Bob"), Person("Alice"), 100uL),
        Transaction(Person("Bob"), Person("Charlie"), 50uL)
    )

    GitwiseTheme {
        TransactionList(transactions, onEditClick = {})
    }
}
