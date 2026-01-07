package com.example.gitwise

import android.text.Editable
import android.text.TextWatcher
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
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
    val repoMembers by viewModel.repoMembers.collectAsState()

    LaunchedEffect(Unit) {
        if (repoMembers.isEmpty()) {
            viewModel.loadTransactions(context)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            TransactionEditor(
                initialTransaction = initialTransaction,
                repoMembers = repoMembers,
                onSave = { savedTransaction ->
                    // Pass the initial transaction's ID as the "old" one if we are editing
                    val oldTransactionId = initialTransaction?.id
                    viewModel.saveTransaction(context, savedTransaction, oldTransactionId) {
                        navController.popBackStack()
                    }
                    Toast.makeText(context, "Transaction pushed", Toast.LENGTH_SHORT).show()
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
    repoMembers: List<String> = emptyList(),
    onSave: (Transaction) -> Unit,
    onDelete: () -> Unit
) {
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
        Spacer(modifier = Modifier.height(32.dp))

        // Payer Selection
        DropdownTextField(
            label = "Payer",
            value = payer,
            suggestions = repoMembers,
            onValueChange = { payer = it }
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        // Ower Selection
        DropdownTextField(
            label = "Ower",
            value = ower,
            suggestions = repoMembers,
            onValueChange = { ower = it }
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
                            sum = sumLong.toULong()
                            // id is automatically generated
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

@Composable
fun DropdownTextField(
    label: String,
    value: String,
    suggestions: List<String>,
    onValueChange: (String) -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    
    // Theme colors matching M3 TextField
    val containerColor = MaterialTheme.colorScheme.surfaceVariant
    val focusedIndicatorColor = MaterialTheme.colorScheme.primary
    val unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurfaceVariant
    val indicatorColor = if (isFocused) focusedIndicatorColor else unfocusedIndicatorColor
    val labelColor = if (isFocused) focusedIndicatorColor else MaterialTheme.colorScheme.onSurfaceVariant
    val textColor = MaterialTheme.colorScheme.onSurface

    val shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
    val isLabelFloating = isFocused || value.isNotEmpty()
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(containerColor, shape)
            .drawBehind {
                val strokeWidth = if (isFocused) 2.dp.toPx() else 1.dp.toPx()
                val y = size.height - strokeWidth / 2
                drawLine(
                    color = indicatorColor,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = strokeWidth
                )
            }
    ) {
        // Label
        Text(
            text = label,
            style = if (isLabelFloating) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyLarge,
            color = labelColor,
            modifier = Modifier
                .padding(start = 16.dp)
                .align(if (isLabelFloating) Alignment.TopStart else Alignment.CenterStart)
                .padding(top = if (isLabelFloating) 8.dp else 0.dp)
        )
        
        // AutoCompleteTextView
        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = if (isLabelFloating) 18.dp else 0.dp)
                .align(Alignment.CenterStart),
            factory = { context ->
                AutoCompleteTextView(context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    background = null // Remove default underline
                    setPadding(0, 0, 0, 0)
                    threshold = 1
                    maxLines = 1
                    inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
                    
                    addTextChangedListener(object : TextWatcher {
                        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                        override fun afterTextChanged(s: Editable?) {
                            val newValue = s.toString()
                            if (newValue != value) {
                                onValueChange(newValue)
                            }
                        }
                    })
                    
                    setOnFocusChangeListener { _, hasFocus ->
                        isFocused = hasFocus
                    }
                }
            },
            update = { view ->
                if (view.text.toString() != value) {
                    view.setText(value)
                    // Only move cursor to end if focused, otherwise it might be disruptive
                    if (view.hasFocus()) {
                        view.setSelection(view.text.length)
                    }
                }
                
                // Update text color
                view.setTextColor(textColor.toArgb())
                
                // Update adapter
                val prevSuggestions = view.tag as? List<String>
                if (prevSuggestions !== suggestions) {
                    val adapter = ArrayAdapter(
                        view.context,
                        android.R.layout.simple_dropdown_item_1line,
                        suggestions
                    )
                    view.setAdapter(adapter)
                    view.tag = suggestions
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TransactionEditorScreenPreview() {
    com.example.gitwise.ui.theme.GitwiseTheme {
        TransactionEditor(
            initialTransaction = Transaction("tacos", Person("Payer"), Person("Ower"), 100uL),
            repoMembers = listOf("Alice", "Bob", "Charlie"),
            onSave = {},
            onDelete = {}
        )
    }
}
