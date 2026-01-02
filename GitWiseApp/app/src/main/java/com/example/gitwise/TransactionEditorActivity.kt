package com.example.gitwise

import android.content.ContentValues.TAG
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.gitwise.datatypes.Person
import com.example.gitwise.datatypes.Transaction
import com.example.gitwise.transactionparser.readTransactions
import com.example.gitwise.transactionparser.writeTransactions
import com.example.gitwise.ui.theme.GitwiseTheme
import java.io.File
import java.util.UUID

class TransactionEditorActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO upgrade to TIRAMISU
        val transaction = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra("transaction", Transaction::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getSerializableExtra("transaction") as? Transaction
        }

        setContent {
            GitwiseTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    TransactionEditor(
                        modifier = Modifier.padding(innerPadding),
                        initialTransaction = transaction,
                        onSave = { savedTransaction ->
                            saveTransaction(this, savedTransaction)
                        },
                        onDelete = {
                            if (transaction != null) {
                                deleteTransaction(this, transaction.id)
                            }
                        }
                    )
                }
            }
        }
    }

    private fun saveTransaction(context: Context, transaction: Transaction) {
        Log.i(TAG, "saving transaction: $transaction")
        val repoPath = context.filesDir.absolutePath + "\\GitWise"
        val dataFilePath = "$repoPath\\data.json"
        val dataFile = File(dataFilePath)
        
        val transactions = readTransactions(dataFile).toMutableList()
        // If we have an existing transaction with the same ID, update it.
        // Otherwise, add it.
        val existingIndex = transactions.indexOfFirst { it.id == transaction.id }
        if (existingIndex != -1) {
            transactions[existingIndex] = transaction
        } else {
            transactions.add(transaction)
        }

        writeTransactions(dataFile, transactions)
        Log.i(TAG, "saved successfully")
        finish()
    }

    private fun deleteTransaction(context: Context, transactionId: UUID) {
        Log.i(TAG, "deleting transaction: $transactionId")
        val repoPath = context.filesDir.absolutePath + "\\GitWise"
        val dataFilePath = "$repoPath\\data.json"
        val dataFile = File(dataFilePath)

        val transactions = readTransactions(dataFile).toMutableList()
        val wasRemoved = transactions.removeIf { it.id == transactionId }

        if (wasRemoved) {
            writeTransactions(dataFile, transactions)
            Log.i(TAG, "deleted successfully")
        }
        finish()
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
    var reason by remember { mutableStateOf(initialTransaction?.reason ?: "") }
    val context = LocalContext.current

    Column(modifier = modifier.padding(16.dp)) {
        TextField(
            value = reason,
            onValueChange = { reason = it },
            label = { Text("Reason") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
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
        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                val sumLong = sum.toLongOrNull()
                if (payer.isNotBlank() && ower.isNotBlank() && sumLong != null && sumLong > 0) {
                    onSave(
                        Transaction(
                            reason = reason,
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
fun TransactionEditPreview() {
    val transaction = Transaction("Tacos",Person("Payer"), Person("Ower"), 100uL)

    GitwiseTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            TransactionEditor(
                modifier = Modifier.padding(innerPadding),
                initialTransaction = transaction,
                onSave = { _ -> {} },
                onDelete = {}
            )
        }
    }

}
