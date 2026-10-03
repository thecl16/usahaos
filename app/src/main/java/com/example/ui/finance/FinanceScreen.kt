package com.example.ui.finance

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.ExpenseEntity
import com.example.ui.MainAppViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceScreen(
    viewModel: MainAppViewModel,
    initialTab: Int = 0,
    onNavigateBack: () -> Unit
) {
    val cashBalance by viewModel.cashBalance.collectAsState()
    val expenses by viewModel.expenses.collectAsState()

    var selectedTab by remember { mutableStateOf(initialTab.coerceIn(0, 1)) }
    var showExpenseDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Keuangan & Kas") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Saldo Kas")
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                formatRupiah(cashBalance),
                            )
                        }

                        Icon(
                            Icons.Default.AccountBalance,
                            contentDescription = null
                        )
                    }
                }
            }

            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Kas") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Pengeluaran") }
                )
            }

            when (selectedTab) {
                0 -> CashOverview(
                    cashBalance = cashBalance,
                    onAddExpense = { showExpenseDialog = true }
                )

                1 -> ExpenseList(
                    expenses = expenses,
                    onAddExpense = { showExpenseDialog = true }
                )
            }
        }
    }

    if (showExpenseDialog) {
        ExpenseDialog(
            onDismiss = { showExpenseDialog = false },
            onSubmit = { category, description, amount, paymentMethod, reference, notes ->
                viewModel.createExpense(
                    category = category,
                    description = description,
                    amount = amount,
                    paymentMethod = paymentMethod,
                    referenceNumber = reference,
                    notes = notes
                ) {
                    showExpenseDialog = false
                }
            }
        )
    }
}

@Composable
private fun CashOverview(
    cashBalance: Long,
    onAddExpense: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Saldo kas saat ini")
        Spacer(modifier = Modifier.height(8.dp))
        Text(formatRupiah(cashBalance))

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onAddExpense,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.padding(4.dp))
            Text("Catat Pengeluaran")
        }
    }
}

@Composable
private fun ExpenseList(
    expenses: List<ExpenseEntity>,
    onAddExpense: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Button(onClick = onAddExpense) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.padding(4.dp))
                Text("Pengeluaran")
            }
        }

        if (expenses.isEmpty()) {
            Text(
                "Belum ada pengeluaran.",
                modifier = Modifier.padding(16.dp)
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(expenses, key = { it.id }) { expense ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(expense.description)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(expense.category)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(formatRupiah(expense.amount))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "${expense.paymentMethod} • ${
                                    formatDate(expense.createdAt)
                                }"
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpenseDialog(
    onDismiss: () -> Unit,
    onSubmit: (
        String,
        String,
        Long,
        String,
        String?,
        String
    ) -> Unit
) {
    var category by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var reference by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var paymentExpanded by remember { mutableStateOf(false) }
    var paymentMethod by remember { mutableStateOf("CASH") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Catat Pengeluaran") },
        text = {
            Column {
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Kategori") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Deskripsi") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it.filter(Char::isDigit) },
                    label = { Text("Jumlah (Rp)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = { paymentExpanded = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Metode: $paymentMethod")
                }

                DropdownMenu(
                    expanded = paymentExpanded,
                    onDismissRequest = { paymentExpanded = false }
                ) {
                    listOf("CASH", "BANK_TRANSFER", "QRIS", "OTHER").forEach { method ->
                        DropdownMenuItem(
                            text = { Text(method) },
                            onClick = {
                                paymentMethod = method
                                paymentExpanded = false
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = reference,
                    onValueChange = { reference = it },
                    label = { Text("Nomor Referensi (opsional)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Catatan (opsional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsedAmount = amount.toLongOrNull() ?: 0L
                    if (
                        category.isNotBlank() &&
                        description.isNotBlank() &&
                        parsedAmount > 0L
                    ) {
                        onSubmit(
                            category,
                            description,
                            parsedAmount,
                            paymentMethod,
                            reference.ifBlank { null },
                            notes
                        )
                    }
                }
            ) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

private fun formatRupiah(amount: Long): String {
    return NumberFormat
        .getCurrencyInstance(Locale("id", "ID"))
        .format(amount)
        .replace(",00", "")
}

private fun formatDate(timestamp: Long): String {
    return SimpleDateFormat(
        "dd MMM yyyy, HH:mm",
        Locale("id", "ID")
    ).format(Date(timestamp))
}
