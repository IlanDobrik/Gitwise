package com.example.gitwise

import android.os.Bundle
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

class TransactionEditorActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val payerName = intent.getStringExtra("payer") ?: ""
        val owerName = intent.getStringExtra("ower") ?: ""
        val sumValue = intent.getLongExtra("sum", 0L)
        val isEdit = intent.getBooleanExtra("is_edit", false)
        val originalPayer = if (isEdit) payerName else null
        val originalOwer = if (isEdit) owerName else null
        val originalSum = if (isEdit) sumValue else null

        setContent {
            GitwiseTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    TransactionEditor(
                        modifier = Modifier.padding(innerPadding),
                        initialPayer = payerName,
                        initialOwer = owerName,
                        initialSum = if (sumValue > 0) sumValue.toString() else "",
                        onSave = { payer, ower, sum ->
                            saveTransaction(payer, ower, sum.toULong(), originalPayer, originalOwer, originalSum?.toULong())
                        }
                    )
                }
            }
        }
    }

    private fun saveTransaction(payer: String, ower: String, sum: ULong, oldPayer: String?, oldOwer: String?, oldSum: ULong?) {
        val repoPath = "C:\\Projects\\GitwiseData"
        val dataFilePath = repoPath + "\\data.json"
        val file = File(dataFilePath)
        
        val transactions = readTransactions(file).toMutableList()
        
        if (oldPayer != null && oldOwer != null && oldSum != null) {
            val index = transactions.indexOfFirst { 
                it.payer.name == oldPayer && it.ower.name == oldOwer && it.sum == oldSum 
            }
            if (index != -1) {
                transactions[index] = Transaction(Person(payer), Person(ower), sum)
            } else {
                transactions.add(Transaction(Person(payer), Person(ower), sum))
            }
        } else {
            transactions.add(Transaction(Person(payer), Person(ower), sum))
        }

        writeTransactions(file, transactions)
        finish()
    }
}

@Composable
fun TransactionEditor(
    modifier: Modifier = Modifier,
    initialPayer: String,
    initialOwer: String,
    initialSum: String,
    onSave: (String, String, Long) -> Unit
) {
    var payer by remember { mutableStateOf(initialPayer) }
    var ower by remember { mutableStateOf(initialOwer) }
    var sum by remember { mutableStateOf(initialSum) }
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
                    onSave(payer, ower, sumLong)
                } else {
                    Toast.makeText(context, "Invalid input", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TransactionEditPreview() {
    val payerName = "payer"
    val owerName = "ower"
    val sumValue = 130

    GitwiseTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            TransactionEditor(
                modifier = Modifier.padding(innerPadding),
                initialPayer = payerName,
                initialOwer = owerName,
                initialSum = if (sumValue > 0) sumValue.toString() else "",
                onSave = { payer, ower, sum ->
                    {}
                }
            )
        }
    }

}
