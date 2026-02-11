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
import com.example.gitwise.datatypes.Debt
import com.example.gitwise.datatypes.Person
import com.example.gitwise.datatypes.Transaction
import com.example.gitwise.gitmanager.GitViewModel

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
fun OwerItem(ower: String, amount: String, onAmountChange: (String) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(ower, modifier = Modifier.weight(1f))
        TextField(
            value = amount,
            onValueChange = onAmountChange,
            label = { Text("Amount") },
            modifier = Modifier.weight(1f)
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionEditor(
    modifier: Modifier = Modifier,
    initialTransaction: Transaction?,
    repoMembers: List<String> = emptyList(),
    onSave: (Transaction) -> Unit,
    onDelete: () -> Unit
) {
    var payer by remember { mutableStateOf(initialTransaction?.payer?.name ?: "") }
    val initialDebts = initialTransaction?.debts?.associate { it.person.name to it.amount.toString() } ?: emptyMap()
    var owers by remember { mutableStateOf(initialTransaction?.debts?.map { it.person.name } ?: emptyList<String>()) }
    var amounts by remember { mutableStateOf(initialDebts) }
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

        // Owers Selection
        MultiSelectDropdown(
            label = "Owers",
            selectedItems = owers,
            suggestions = repoMembers,
            onItemsSelected = { selectedOwers ->
                owers = selectedOwers
                val newAmounts = amounts.toMutableMap()
                selectedOwers.forEach { ower ->
                    if (!newAmounts.contains(ower)) {
                        newAmounts[ower] = ""
                    }
                }
                amounts = newAmounts.filterKeys { it in selectedOwers }
            }
        )
        Spacer(modifier = Modifier.height(8.dp))

        owers.forEach { ower ->
            OwerItem(
                ower = ower,
                amount = amounts[ower] ?: "",
                onAmountChange = { newAmount ->
                    amounts = amounts.toMutableMap().apply {
                        this[ower] = newAmount
                    }
                }
            )
        }


        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = {
                val debts = owers.mapNotNull { owerName ->
                    amounts[owerName]?.toULongOrNull()?.let { amount ->
                        Debt(Person(owerName), amount)
                    }
                }

                if (payer.isNotBlank() && debts.isNotEmpty() && debts.size == owers.size) {
                    onSave(
                        Transaction(
                            reason = reason,
                            payer = Person(payer),
                            debts = debts
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiSelectDropdown(
    label: String,
    selectedItems: List<String>,
    suggestions: List<String>,
    onItemsSelected: (List<String>) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        var customOwerName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Add Custom Ower") },
            text = {
                TextField(
                    value = customOwerName,
                    onValueChange = { customOwerName = it },
                    label = { Text("Ower Name") }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (customOwerName.isNotBlank() && !selectedItems.contains(customOwerName)) {
                            onItemsSelected(selectedItems + customOwerName)
                        }
                        showDialog = false
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                Button(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        TextField(
            readOnly = true,
            value = selectedItems.joinToString(),
            onValueChange = {},
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            suggestions.forEach { suggestion ->
                DropdownMenuItem(
                    text = { Text(suggestion) },
                    onClick = {
                        val newSelection = if (selectedItems.contains(suggestion)) {
                            selectedItems - suggestion
                        } else {
                            selectedItems + suggestion
                        }
                        onItemsSelected(newSelection)
                    }
                )
            }
            DropdownMenuItem(
                text = { Text("Add custom...") },
                onClick = {
                    showDialog = true
                    expanded = false
                }
            )
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

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun TransactionEditorScreenPreview() {
    com.example.gitwise.ui.theme.GitwiseTheme {
        TransactionEditor(
            initialTransaction = Transaction(
                "tacos",
                Person("Payer"),
                listOf(Debt(Person("Ower"), 100uL))
            ),
            repoMembers = listOf("Alice", "Bob", "Charlie"),
            onSave = {},
            onDelete = {}
        )
    }
}
