package com.example.gitwise

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.gitwise.config.getConfig
import com.example.gitwise.datatypes.Debt
import com.example.gitwise.datatypes.Person
import com.example.gitwise.datatypes.Transaction
import com.example.gitwise.gitmanager.GitViewModel
import com.example.gitwise.simplifier.NaiveSimplifier
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
    val isFetchSuccessful by viewModel.isFetchSuccessful.collectAsState()
    var isSimplified by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadTransactions(context)
    }

    val displayedTransactions = remember(rawTransactions, isSimplified) {
        if (isSimplified) {
            val nativeSimplifier = NaiveSimplifier()
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

            // Connection status indicator
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
                    .size(12.dp)
                    .background(
                        color = if (isFetchSuccessful) Color.Green else Color.Red,
                        shape = CircleShape
                    )
            )

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
    val context = LocalContext.current
    val config = remember { getConfig(context) }
    val currentUser = config.person

    LazyColumn(modifier = modifier) {
        items(transactions) { transaction ->
            TransactionItem(
                transaction = transaction,
                showEditButton = showEditButton,
                currentUser = currentUser,
                onEdit = { onEditClick(transaction) }
            )
        }
    }
}

@Composable
fun TransactionItem(
    transaction: Transaction,
    showEditButton: Boolean,
    currentUser: Person?,
    onEdit: () -> Unit
) {
    val userOwes = transaction.debts.find { it.person == currentUser }?.amount?.toLong() ?: 0L
    val userIsPayer = transaction.payer == currentUser

    val balance = if (userIsPayer) transaction.sum.toLong() - userOwes else -userOwes

    val balanceColor = when {
        balance > 0 -> Color.Green
        balance < 0 -> Color.Red
        else -> Color.Gray
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = "${transaction.debts.joinToString { it.person.name }} owes ${transaction.payer.name}")
            Text(text = "Amount: ${transaction.sum}")
        }
        if (currentUser != null) {
            Text(text = "You owe: $balance", color = balanceColor)
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
            Transaction(
                "tacos",
                Person("Alice"),
                listOf(Debt(Person("Bob"), 100uL))
            ),
            Transaction(
                "pizza",
                Person("Bob"),
                listOf(Debt(Person("Charlie"), 50uL))
            )
        )

        Scaffold(
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
                Column {
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
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                        .size(12.dp)
                        .background(color = Color.Green, shape = CircleShape)
                )
            }
        }
    }
}
