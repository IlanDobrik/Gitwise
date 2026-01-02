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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.gitwise.datatypes.Transaction
import com.example.gitwise.datatypes.Person
import com.example.gitwise.gitmanager.GitManager
import com.example.gitwise.transactionparser.readTransactions
import com.example.gitwise.ui.theme.GitwiseTheme
import java.io.File

class TransactionListActivity : ComponentActivity() {
    private val rawTransactionsState = mutableStateOf<List<Transaction>>(emptyList())

    fun getTransactions(context : Context) : List<Transaction> {
        // TODO dynamic
        val repoPath = context.filesDir.absolutePath + "\\GitWise"
        val dataFilePath = "$repoPath\\data.json"
        
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
        rawTransactionsState.value = getTransactions(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        refreshData(this)

        setContent {
            GitwiseTheme {
                var isSimplified by remember { mutableStateOf(false) }
                val context = LocalContext.current

                val displayedTransactions = remember(rawTransactionsState.value, isSimplified) {
                    if (isSimplified) {
                        val nativeSimplifier = com.example.gitwise.NaiveSimplifier.NaiveSimplifier()
                        nativeSimplifier.simplifiy(rawTransactionsState.value)
                    } else {
                        rawTransactionsState.value
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    floatingActionButton = {
                        if (!isSimplified) {
                            FloatingActionButton(onClick = {
                                val intent = Intent(context, TransactionEditorActivity::class.java)
                                context.startActivity(intent)
                            }) {
                                Icon(Icons.Filled.Add, contentDescription = "Add Transaction")
                            }
                        }
                    }
                ) { innerPadding ->
                    Column(modifier = Modifier.padding(innerPadding)) {
                        Button(
                            onClick = { isSimplified = !isSimplified },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        ) {
                            Text(if (isSimplified) "Show All Transactions" else "Show Simplified")
                        }

                        TransactionList(
                            transactions = displayedTransactions,
                            showEditButton = !isSimplified,
                            onEditClick = { transaction ->
                                val intent = Intent(context, TransactionEditorActivity::class.java).apply {
                                    putExtra("transaction", transaction)
                                }
                                context.startActivity(intent)
                            }
                        )
                    }
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
    showEditButton: Boolean,
    modifier: Modifier = Modifier,
    onEditClick: (Transaction) -> Unit
) {
    LazyColumn(modifier = modifier) {
        items(transactions) { transaction ->
            TransactionItem(
                transaction = transaction,
                showEditButton = showEditButton,
                onEdit = { onEditClick(transaction) }
            )
        }
    }
}

@Composable
fun TransactionItem(
    transaction: Transaction,
    showEditButton: Boolean,
    onEdit: () -> Unit
) {
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
        if (showEditButton) {
            IconButton(onClick = onEdit) {
                Icon(Icons.Filled.Edit, contentDescription = "Edit Transaction")
            }
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
        TransactionList(
            transactions = transactions,
            showEditButton = true,
            onEditClick = {}
        )
    }
}
