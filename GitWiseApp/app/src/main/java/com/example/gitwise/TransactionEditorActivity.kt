package com.example.gitwise

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.gitwise.datatypes.Person
import com.example.gitwise.datatypes.Transaction
import com.example.gitwise.gitmanager.GitViewModel
import java.util.UUID

@Composable
fun TransactionEditorScreen(
    navController: NavController,
    initialTransaction: Transaction?,
    viewModel: GitViewModel = viewModel()
) {
    val context = LocalContext.current

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            TransactionEditor(
                initialTransaction = initialTransaction,
                onSave = { savedTransaction ->
                    viewModel.saveTransaction(context, savedTransaction) {
                        navController.popBackStack()
                    }
                },
                onDelete = {
                    if (initialTransaction != null) {
                        viewModel.deleteTransaction(context, initialTransaction.id) {
                            navController.popBackStack()
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun TransactionEditor(
    modifier: Modifier = Modifier,
    initialTransaction: Transaction?,
    onSave: (Transaction) -> Unit,
    onDelete: () -> Unit
) {
    // If we're editing, use the transaction's ID. If new, create a new ID.
    val transactionId = initialTransaction?.id ?: UUID.randomUUID()
    
    var payer by remember { mutableStateOf(initialTransaction?.payer?.name ?: "") }
    var ower by remember { mutableStateOf(initialTransaction?.ower?.name ?: "") }
    var sum by remember { mutableStateOf(initialTransaction?.sum?.toString() ?: "") }
    val context = LocalContext.current

    Column(modifier = modifier.padding(16.dp)) {
        TextField(
            value = payer,
            onValueChange = { payer = it },
            label = { Text("Payer") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = ower,
            onValueChange = { ower = it },
            label = { Text("Ower") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = sum,
            onValueChange = { sum = it },
            label = { Text("Amount") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                val sumLong = sum.toLongOrNull()
                if (payer.isNotBlank() && ower.isNotBlank() && sumLong != null && sumLong > 0) {
                    onSave(
                        Transaction(
                            payer = Person(payer),
                            ower = Person(ower),
                            sum = sumLong.toULong(),
                            id = transactionId
                        )
                    )
                } else {
                    Toast.makeText(context, "Invalid input", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save")
        }

        if (initialTransaction != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onDelete,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Delete")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TransactionEditorScreenPreview() {
    com.example.gitwise.ui.theme.GitwiseTheme {
        TransactionEditor(
            initialTransaction = Transaction("tacos", Person("Payer"), Person("Ower"), 100uL),
            onSave = {},
            onDelete = {}
        )
    }
}
