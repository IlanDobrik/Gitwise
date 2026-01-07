package com.example.gitwise

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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.gitwise.datatypes.Transaction
import com.example.gitwise.datatypes.Person
import com.example.gitwise.gitmanager.GitViewModel
import com.google.gson.Gson
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun TransactionListScreen(
    navController: NavController,
    viewModel: GitViewModel = viewModel()
) {
    val context = LocalContext.current
    val rawTransactions by viewModel.transactions.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var isSimplified by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadTransactions(context)
    }

    val displayedTransactions = remember(rawTransactions, isSimplified) {
        if (isSimplified) {
            val nativeSimplifier = com.example.gitwise.NaiveSimplifier.NaiveSimplifier()
            nativeSimplifier.simplify(rawTransactions)
        } else {
            rawTransactions
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        floatingActionButton = {
            if (!isSimplified) {
                FloatingActionButton(onClick = {
                    navController.navigate("${AppDestinations.TRANSACTION_EDITOR}?transaction=")
                }) {
                    Icon(Icons.Filled.Add, contentDescription = "Add Transaction")
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
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
                        val gson = Gson()
                        val transactionJson = URLEncoder.encode(gson.toJson(transaction), StandardCharsets.UTF_8.toString())
                        navController.navigate("${AppDestinations.TRANSACTION_EDITOR}?transaction=$transactionJson")
                    }
                )
            }

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
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
fun TransactionListScreenPreview() {
    com.example.gitwise.ui.theme.GitwiseTheme {
        val transactions = listOf(
            Transaction("tacos", Person("Alice"), Person("Bob"), 100uL),
            Transaction("pizza", Person("Bob"), Person("Charlie"), 50uL)
        )
        
        Scaffold(
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            Column(modifier = Modifier.padding(innerPadding)) {
                Button(
                    onClick = { },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Text("Show All Transactions")
                }
                
                TransactionList(
                    transactions = transactions,
                    showEditButton = true,
                    onEditClick = {}
                )
            }
        }
    }
}
