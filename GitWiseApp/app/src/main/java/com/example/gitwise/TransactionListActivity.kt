package com.example.gitwise

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.gitwise.datatypes.Transaction
import com.example.gitwise.datatypes.Person
import com.example.gitwise.ui.theme.GitwiseTheme

class TransactionListActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Example data
        val transactions = listOf(
            Transaction(Person("Alice"), Person("Bob"), 100uL),
            Transaction(Person("Bob"), Person("Charlie"), 50uL)
        )

        setContent {
            GitwiseTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    TransactionList(
                        transactions = transactions,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun TransactionList(transactions: List<Transaction>, modifier: Modifier = Modifier) {
    LazyColumn(modifier = modifier) {
        items(transactions) { transaction ->
            TransactionItem(transaction)
        }
    }
}

@Composable
fun TransactionItem(transaction: Transaction) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "${transaction.payer.name} owes ${transaction.ower.name}")
        Text(text = "Amount: ${transaction.sum}")
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
        TransactionList(transactions)
    }
}
