package com.example.ui.screens

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.data.model.Account
import com.example.data.model.Category
import com.example.data.model.Transaction
import com.example.ui.components.TransactionItemView
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.MidnightNavy
import com.example.ui.theme.MintGreen
import com.example.ui.theme.TextGray
import com.example.ui.theme.TextGrayLight
import com.example.ui.theme.TextWhite
import com.example.util.CurrencyFormatter
import com.example.util.PdfReportGenerator
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    transactions: List<Transaction>,
    accounts: List<Account>,
    categories: List<Category> = emptyList(),
    userName: String = "User",
    onBack: () -> Unit,
    onDeleteTransaction: (txId: Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var searchQuery by remember { mutableStateOf("") }
    var selectedTypeFilter by remember { mutableStateOf("All Types") } // "All Types", "Credit", "Expense", "Transfer"
    var selectedAccountFilter by remember { mutableStateOf("All Accounts") } // "All Accounts", "ABL", "UBL", "Wallet"
    var selectedCategoryFilter by remember { mutableStateOf("All Categories") }
    var selectedDateRangeFilter by remember { mutableStateOf("All Time") } // "All Time", "Today", "This Month"

    var selectedTxForDetails by remember { mutableStateOf<Transaction?>(null) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var showPdfSuccessDialog by remember { mutableStateOf(false) }
    var generatedPdfFile by remember { mutableStateOf<File?>(null) }

    // Calender ranges
    val now = System.currentTimeMillis()
    val cal = Calendar.getInstance()
    cal.timeInMillis = now
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    val startOfToday = cal.timeInMillis

    cal.set(Calendar.DAY_OF_MONTH, 1)
    val startOfMonth = cal.timeInMillis

    // Distinct unique categories from both transactions and categories list (excluding "All Categories" and "Other")
    val distinctCategories = remember(transactions, categories) {
        val fromDb = categories.map { it.name.trim() }
        val fromTxs = transactions.map { it.category.trim() }
        (listOf("All Categories") + (fromDb + fromTxs)
            .filter { it.isNotBlank() && !it.equals("All Categories", ignoreCase = true) && !it.equals("All", ignoreCase = true) }
            .distinctBy { it.lowercase() }
            .sorted()
        ).distinct()
    }

    // Filter transactions
    val filteredList = transactions.filter { tx ->
        val matchesType = when (selectedTypeFilter) {
            "Credit" -> tx.type == "CREDIT"
            "Expense" -> tx.type == "EXPENSE"
            "Transfer" -> tx.type == "TRANSFER"
            else -> true
        }
        val matchesAccount = when (selectedAccountFilter) {
            "ABL" -> tx.fromAccountId == "abl" || tx.toAccountId == "abl"
            "UBL" -> tx.fromAccountId == "ubl" || tx.toAccountId == "ubl"
            "Wallet" -> tx.fromAccountId == "wallet" || tx.toAccountId == "wallet"
            else -> true
        }
        val matchesCategory = when (selectedCategoryFilter) {
            "All Categories" -> true
            else -> tx.category.equals(selectedCategoryFilter, ignoreCase = true)
        }
        val matchesDateRange = when (selectedDateRangeFilter) {
            "Today" -> tx.timestamp >= startOfToday
            "This Month" -> tx.timestamp >= startOfMonth
            else -> true
        }
        val matchesSearch = searchQuery.isBlank() ||
                tx.category.contains(searchQuery, ignoreCase = true) ||
                tx.note.contains(searchQuery, ignoreCase = true)

        matchesType && matchesAccount && matchesCategory && matchesDateRange && matchesSearch
    }

    // PDF filename generation helper
    val suggestedPdfName = remember(selectedAccountFilter) {
        val accTag = when (selectedAccountFilter) {
            "ABL" -> "ABL"
            "UBL" -> "UBL"
            "Wallet" -> "Wallet"
            else -> "All"
        }
        val dateTag = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        "Pockitify_Transactions_${accTag}_$dateTag.pdf"
    }

    // Storage Access Framework launcher to save PDF directly
    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    val filterInfo = PdfReportGenerator.ReportFilterInfo(
                        userName = userName,
                        accountFilter = selectedAccountFilter,
                        typeFilter = selectedTypeFilter,
                        categoryFilter = selectedCategoryFilter,
                        searchQuery = searchQuery,
                        dateRangeText = selectedDateRangeFilter
                    )
                    PdfReportGenerator.generatePdf(
                        context = context,
                        transactions = filteredList,
                        accounts = accounts,
                        filterInfo = filterInfo,
                        outputStream = out
                    )
                }
                Toast.makeText(context, "PDF saved successfully!", Toast.LENGTH_SHORT).show()

                // Also prepare local cache file for immediate open/share
                val filterInfo = PdfReportGenerator.ReportFilterInfo(
                    userName = userName,
                    accountFilter = selectedAccountFilter,
                    typeFilter = selectedTypeFilter,
                    categoryFilter = selectedCategoryFilter,
                    searchQuery = searchQuery,
                    dateRangeText = selectedDateRangeFilter
                )
                generatedPdfFile = PdfReportGenerator.saveTempPdfFile(context, filteredList, accounts, filterInfo)
                showPdfSuccessDialog = true
            } catch (e: Exception) {
                Toast.makeText(context, "Error saving PDF: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Detail dialog
    if (selectedTxForDetails != null) {
        val tx = selectedTxForDetails!!
        AlertDialog(
            onDismissRequest = { selectedTxForDetails = null },
            containerColor = DarkSurface,
            title = {
                Text(
                    text = "Transaction Details",
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = TextWhite,
                        fontWeight = FontWeight.Bold
                    )
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Type:", color = TextGray)
                        Text(text = tx.type, color = TextWhite, fontWeight = FontWeight.Bold)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Amount:", color = TextGray)
                        Text(
                            text = CurrencyFormatter.formatPaisa(tx.amountPaisa),
                            color = if (tx.type == "CREDIT") EmeraldGreen else if (tx.type == "EXPENSE") ExpenseRed else Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Category:", color = TextGray)
                        Text(text = tx.category, color = TextWhite)
                    }
                    if (tx.fromAccountId != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "From Account:", color = TextGray)
                            Text(text = tx.fromAccountId.uppercase(), color = TextWhite)
                        }
                    }
                    if (tx.toAccountId != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "To Account:", color = TextGray)
                            Text(text = tx.toAccountId.uppercase(), color = TextWhite)
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Date & Time:", color = TextGray)
                        Text(text = CurrencyFormatter.formatDateTime(tx.timestamp), color = TextWhite)
                    }
                    if (tx.note.isNotBlank()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Note:", color = TextGray)
                            Text(text = tx.note, color = TextWhite)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed)
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { selectedTxForDetails = null }) {
                    Text("Close", color = TextGray)
                }
            }
        )
    }

    // Delete Confirmation dialog
    if (showDeleteConfirmDialog && selectedTxForDetails != null) {
        val tx = selectedTxForDetails!!
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            containerColor = DarkSurface,
            title = { Text("Confirm Deletion", color = TextWhite, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "Are you sure you want to delete this transaction of ${CurrencyFormatter.formatPaisa(tx.amountPaisa)}? Affected account balances will be automatically recalculated.",
                    color = TextGrayLight,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteTransaction(tx.id)
                        showDeleteConfirmDialog = false
                        selectedTxForDetails = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed)
                ) {
                    Text("Delete and Recalculate", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel", color = TextGray)
                }
            }
        )
    }

    // PDF Success / Action Dialog
    if (showPdfSuccessDialog && generatedPdfFile != null) {
        val file = generatedPdfFile!!
        AlertDialog(
            onDismissRequest = { showPdfSuccessDialog = false },
            containerColor = DarkSurface,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(EmeraldGlow),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = null, tint = EmeraldGreen)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(text = "PDF Report Generated", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            },
            text = {
                Column {
                    Text(
                        text = "Your transaction history report has been prepared (${filteredList.size} transactions).",
                        color = TextGrayLight,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "File: ${file.name}",
                        color = MintGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            try {
                                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    setDataAndType(uri, "application/pdf")
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(intent, "Open PDF Report"))
                            } catch (e: Exception) {
                                Toast.makeText(context, "No PDF viewer found on device", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                    ) {
                        Icon(imageVector = Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Open", color = Color.White)
                    }

                    Button(
                        onClick = {
                            try {
                                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "application/pdf"
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(intent, "Share PDF Report"))
                            } catch (e: Exception) {
                                Toast.makeText(context, "Error sharing PDF: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated)
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = TextWhite, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Share", color = TextWhite)
                    }
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showPdfSuccessDialog = false }) {
                    Text("Close", color = TextGray)
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MidnightNavy)
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .testTag("history_screen")
    ) {
        // Top Header with Back, Title, and PDF Export Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextWhite
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Transaction History",
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = TextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                )
            }

            // PDF Download Icon Button
            IconButton(
                onClick = {
                    val filterInfo = PdfReportGenerator.ReportFilterInfo(
                        userName = userName,
                        accountFilter = selectedAccountFilter,
                        typeFilter = selectedTypeFilter,
                        categoryFilter = selectedCategoryFilter,
                        searchQuery = searchQuery,
                        dateRangeText = selectedDateRangeFilter
                    )
                    // Generate temp file in cache first
                    generatedPdfFile = PdfReportGenerator.saveTempPdfFile(context, filteredList, accounts, filterInfo)
                    // Trigger SAF save dialog
                    createDocumentLauncher.launch(suggestedPdfName)
                },
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(DarkSurfaceElevated)
                    .testTag("download_pdf_button")
            ) {
                Icon(
                    imageVector = Icons.Default.PictureAsPdf,
                    contentDescription = "Download PDF Report",
                    tint = MintGreen,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search by category, note...", color = TextGray) },
            leadingIcon = {
                Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = TextGray)
            },
            trailingIcon = {
                if (searchQuery.isNotBlank()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Clear", tint = TextGray)
                    }
                }
            },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextWhite,
                unfocusedTextColor = TextWhite,
                focusedContainerColor = DarkSurfaceCard,
                unfocusedContainerColor = DarkSurfaceCard,
                focusedBorderColor = EmeraldGreen,
                unfocusedBorderColor = Color(0xFF1B2E4E)
            ),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("history_search_field")
        )

        Spacer(modifier = Modifier.height(12.dp))

        // FILTER 1: Account Filter ("All Accounts" only appears ONCE, with distinct options)
        Text(
            text = "Account Filter",
            style = MaterialTheme.typography.labelSmall.copy(
                color = TextGray,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val accountOptions = listOf("All Accounts", "ABL", "UBL", "Wallet").distinct()
            accountOptions.forEach { acc ->
                val isSelected = selectedAccountFilter == acc
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) Color(0xFF1E3A60) else DarkSurfaceCard)
                        .border(1.dp, if (isSelected) EmeraldGreen else Color.Transparent, RoundedCornerShape(10.dp))
                        .clickable { selectedAccountFilter = acc },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (acc == "All Accounts") "All" else acc,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (isSelected) Color.White else TextGray,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // FILTER 2: Type Filter ("All Types" only appears ONCE, with distinct options)
        Text(
            text = "Transaction Type",
            style = MaterialTheme.typography.labelSmall.copy(
                color = TextGray,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val typeOptions = listOf("All Types", "Credit", "Expense", "Transfer").distinct()
            typeOptions.forEach { type ->
                val isSelected = selectedTypeFilter == type
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) EmeraldGreen else DarkSurfaceElevated)
                        .clickable { selectedTypeFilter = type },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (type == "All Types") "All" else type,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (isSelected) Color.White else TextGray,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // FILTER 3: Category & Date Range filters row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Category Dropdown
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(DarkSurfaceCard)
                    .border(1.dp, if (selectedCategoryFilter != "All Categories") EmeraldGreen else Color(0xFF1B2E4E), RoundedCornerShape(10.dp))
                    .clickable { categoryDropdownExpanded = true }
                    .padding(horizontal = 10.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (selectedCategoryFilter == "All Categories") "Category: All" else selectedCategoryFilter,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (selectedCategoryFilter != "All Categories") MintGreen else TextGray,
                            fontWeight = FontWeight.Medium
                        ),
                        maxLines = 1
                    )
                }

                DropdownMenu(
                    expanded = categoryDropdownExpanded,
                    onDismissRequest = { categoryDropdownExpanded = false },
                    modifier = Modifier.background(DarkSurfaceElevated)
                ) {
                    distinctCategories.forEach { catName ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = catName,
                                    color = if (catName == selectedCategoryFilter) MintGreen else TextWhite
                                )
                            },
                            onClick = {
                                selectedCategoryFilter = catName
                                categoryDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            // Date Range Quick Filter: All Time / Today / This Month
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf("All Time", "Today", "This Month").forEach { dateOpt ->
                    val isSelected = selectedDateRangeFilter == dateOpt
                    val label = when (dateOpt) {
                        "All Time" -> "All"
                        "This Month" -> "Month"
                        else -> dateOpt
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) Color(0xFF1E3A60) else DarkSurfaceCard)
                            .border(1.dp, if (isSelected) EmeraldGreen else Color.Transparent, RoundedCornerShape(10.dp))
                            .clickable { selectedDateRangeFilter = dateOpt },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (isSelected) Color.White else TextGray,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Status / Count Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${filteredList.size} Transactions",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = TextGray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            )

            if (selectedAccountFilter != "All Accounts" || selectedTypeFilter != "All Types" || selectedCategoryFilter != "All Categories" || selectedDateRangeFilter != "All Time" || searchQuery.isNotBlank()) {
                Text(
                    text = "Clear All Filters",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MintGreen,
                        fontWeight = FontWeight.SemiBold
                    ),
                    modifier = Modifier.clickable {
                        selectedAccountFilter = "All Accounts"
                        selectedTypeFilter = "All Types"
                        selectedCategoryFilter = "All Categories"
                        selectedDateRangeFilter = "All Time"
                        searchQuery = ""
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "No matching transactions found.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextGray)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Try adjusting or clearing your filters.",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextGrayLight)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(filteredList) { tx ->
                    TransactionItemView(
                        transaction = tx,
                        onClick = { selectedTxForDetails = tx }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}
