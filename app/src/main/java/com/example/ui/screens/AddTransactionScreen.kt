package com.example.ui.screens

import android.app.DatePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Account
import com.example.data.model.Category
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.MidnightNavy
import com.example.ui.theme.TextGray
import com.example.ui.theme.TextGrayLight
import com.example.ui.theme.TextWhite
import com.example.util.CurrencyFormatter
import java.util.Calendar

@Composable
fun AddTransactionScreen(
    initialType: String = "CREDIT",
    accounts: List<Account>,
    categories: List<Category>,
    onBack: () -> Unit,
    onSaveCredit: (amountPaisa: Long, accountId: String, category: String, note: String, timestamp: Long) -> Unit,
    onSaveExpense: (amountPaisa: Long, accountId: String, category: String, note: String, timestamp: Long) -> Unit,
    onSaveTransfer: (amountPaisa: Long, fromAccountId: String, toAccountId: String, note: String, timestamp: Long) -> Unit,
    onSaveCustomCategory: (name: String, type: String) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedType by remember { mutableStateOf(initialType) } // "CREDIT", "EXPENSE", "TRANSFER"
    var amountInput by remember { mutableStateOf("") }
    var selectedAccountId by remember { mutableStateOf(accounts.firstOrNull()?.id ?: "abl") }
    var selectedToAccountId by remember { mutableStateOf(accounts.getOrNull(1)?.id ?: "wallet") }
    var selectedCategory by remember {
        mutableStateOf(if (initialType == "CREDIT") "Salary" else "Utility Bills")
    }
    var customCategoryInput by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var selectedTimestamp by remember { mutableLongStateOf(System.currentTimeMillis()) }

    var accountDropdownExpanded by remember { mutableStateOf(false) }
    var toAccountDropdownExpanded by remember { mutableStateOf(false) }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val currentAccount = accounts.firstOrNull { it.id == selectedAccountId }
    val toAccount = accounts.firstOrNull { it.id == selectedToAccountId }

    // Compute distinct category options for Credit vs Expense
    val displayCategories = remember(categories, selectedType) {
        if (selectedType == "CREDIT") {
            // Required default credit categories: 1. Salary, 2. Pocket Money, 3. Other
            val dbCreditCats = categories
                .filter { it.type == "CREDIT" || it.type == "BOTH" }
                .map { it.name.trim() }
                .filter { !it.equals("Other", ignoreCase = true) }

            val list = mutableListOf("Salary", "Pocket Money")
            for (cat in dbCreditCats) {
                if (!list.any { it.equals(cat, ignoreCase = true) }) {
                    list.add(cat)
                }
            }
            list.add("Other")
            list
        } else {
            // Required default expense categories: Utility Bills, Mobile Package, Grocery, Other
            val disallowedExpense = setOf("medicines", "medicine", "monthly expenditure", "bird food", "birds", "cat food", "other")
            val dbExpenseCats = categories
                .filter { it.type == "EXPENSE" || it.type == "BOTH" }
                .map { it.name.trim() }
                .filter { name ->
                    !disallowedExpense.contains(name.lowercase())
                }

            val list = mutableListOf("Utility Bills", "Mobile Package", "Grocery")
            for (cat in dbExpenseCats) {
                if (!list.any { it.equals(cat, ignoreCase = true) }) {
                    list.add(cat)
                }
            }
            list.add("Other")
            list
        }
    }

    // Auto-reset selected category when type changes if current category isn't valid
    LaunchedEffect(selectedType) {
        if (selectedType == "CREDIT") {
            if (!displayCategories.any { it.equals(selectedCategory, ignoreCase = true) }) {
                selectedCategory = "Salary"
            }
        } else if (selectedType == "EXPENSE") {
            if (!displayCategories.any { it.equals(selectedCategory, ignoreCase = true) }) {
                selectedCategory = "Utility Bills"
            }
        }
    }

    // Date picker dialog
    val calendar = Calendar.getInstance().apply { timeInMillis = selectedTimestamp }
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val newCal = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month)
                set(Calendar.DAY_OF_MONTH, dayOfMonth)
            }
            selectedTimestamp = newCal.timeInMillis
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MidnightNavy)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .testTag("add_transaction_screen")
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .testTag("back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextWhite
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = "Add Transaction",
                style = MaterialTheme.typography.titleLarge.copy(
                    color = TextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Type Segmented Pills
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(DarkSurface)
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Credit Pill
            val isCredit = selectedType == "CREDIT"
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isCredit) EmeraldGreen else Color.Transparent)
                    .clickable {
                        selectedType = "CREDIT"
                        selectedCategory = "Salary"
                        customCategoryInput = ""
                        errorMessage = null
                    }
                    .testTag("pill_credit"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Credit",
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = if (isCredit) Color.White else TextGray,
                        fontWeight = if (isCredit) FontWeight.Bold else FontWeight.Medium
                    )
                )
            }

            // Expense Pill
            val isExpense = selectedType == "EXPENSE"
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isExpense) ExpenseRed else Color.Transparent)
                    .clickable {
                        selectedType = "EXPENSE"
                        selectedCategory = "Grocery"
                        customCategoryInput = ""
                        errorMessage = null
                    }
                    .testTag("pill_expense"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Expense",
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = if (isExpense) Color.White else TextGray,
                        fontWeight = if (isExpense) FontWeight.Bold else FontWeight.Medium
                    )
                )
            }

            // Transfer Pill
            val isTransfer = selectedType == "TRANSFER"
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isTransfer) AccentBlue else Color.Transparent)
                    .clickable {
                        selectedType = "TRANSFER"
                        selectedCategory = "Transfer"
                        errorMessage = null
                    }
                    .testTag("pill_transfer"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Transfer",
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = if (isTransfer) Color.White else TextGray,
                        fontWeight = if (isTransfer) FontWeight.Bold else FontWeight.Medium
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Large Amount Display & Input
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(DarkSurfaceCard)
                .border(1.dp, Color(0xFF1B2E4E), RoundedCornerShape(20.dp))
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Enter Amount in PKR",
                style = MaterialTheme.typography.labelMedium.copy(
                    color = TextGray,
                    fontSize = 12.sp
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "PKR ",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        color = TextGrayLight,
                        fontWeight = FontWeight.Bold,
                        fontSize = 26.sp
                    )
                )

                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { input ->
                        if (input.all { it.isDigit() || it == '.' }) {
                            amountInput = input
                            errorMessage = null
                        }
                    },
                    placeholder = {
                        Text(
                            text = "0.00",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                color = Color(0xFF4A5568),
                                fontWeight = FontWeight.Bold,
                                fontSize = 32.sp
                            )
                        )
                    },
                    textStyle = MaterialTheme.typography.headlineLarge.copy(
                        color = TextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        cursorColor = EmeraldGreen
                    ),
                    modifier = Modifier
                        .width(220.dp)
                        .testTag("amount_input_field")
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Quick increment chips (+100, +500, +1,000, +5,000)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(100L, 500L, 1000L, 5000L).forEach { chipVal ->
                    val chipText = when (chipVal) {
                        1000L -> "+1,000"
                        5000L -> "+5,000"
                        else -> "+$chipVal"
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(DarkSurfaceElevated)
                            .border(1.dp, Color(0xFF233B5D), RoundedCornerShape(10.dp))
                            .clickable {
                                val current = amountInput.toDoubleOrNull() ?: 0.0
                                amountInput = (current + chipVal).toInt().toString()
                                errorMessage = null
                            }
                            .testTag("chip_$chipVal"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = chipText,
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = TextWhite,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Account Selection Dropdown
        val accountLabel = when (selectedType) {
            "CREDIT" -> "Account (Receive In)"
            "EXPENSE" -> "Account (Pay From)"
            else -> "From Account"
        }

        Text(
            text = accountLabel,
            style = MaterialTheme.typography.labelMedium.copy(
                color = TextGrayLight,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        )

        Spacer(modifier = Modifier.height(6.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(DarkSurfaceCard)
                .border(1.dp, Color(0xFF1B2E4E), RoundedCornerShape(14.dp))
                .clickable { accountDropdownExpanded = true }
                .padding(horizontal = 16.dp, vertical = 14.dp)
                .testTag("account_dropdown_trigger")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = currentAccount?.name ?: "Select Account",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = TextWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    )
                    Text(
                        text = "Available: ${CurrencyFormatter.formatPaisa(currentAccount?.balancePaisa ?: 0L)}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextGray,
                            fontSize = 12.sp
                        )
                    )
                }

                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Select",
                    tint = TextGray
                )
            }

            DropdownMenu(
                expanded = accountDropdownExpanded,
                onDismissRequest = { accountDropdownExpanded = false },
                modifier = Modifier.background(DarkSurfaceElevated)
            ) {
                accounts.forEach { acc ->
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(text = acc.name, color = TextWhite, fontWeight = FontWeight.Bold)
                                Text(
                                    text = "Balance: ${CurrencyFormatter.formatPaisa(acc.balancePaisa)}",
                                    color = TextGray,
                                    fontSize = 12.sp
                                )
                            }
                        },
                        onClick = {
                            selectedAccountId = acc.id
                            accountDropdownExpanded = false
                        }
                    )
                }
            }
        }

        // If Transfer, show Destination Account Dropdown
        if (selectedType == "TRANSFER") {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "To Account (Destination)",
                style = MaterialTheme.typography.labelMedium.copy(
                    color = TextGrayLight,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            )
            Spacer(modifier = Modifier.height(6.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(DarkSurfaceCard)
                    .border(1.dp, Color(0xFF1B2E4E), RoundedCornerShape(14.dp))
                    .clickable { toAccountDropdownExpanded = true }
                    .padding(horizontal = 16.dp, vertical = 14.dp)
                    .testTag("to_account_dropdown_trigger")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = toAccount?.name ?: "Select Destination",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = TextWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        )
                        Text(
                            text = "Current: ${CurrencyFormatter.formatPaisa(toAccount?.balancePaisa ?: 0L)}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextGray,
                                fontSize = 12.sp
                            )
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Select",
                        tint = TextGray
                    )
                }

                DropdownMenu(
                    expanded = toAccountDropdownExpanded,
                    onDismissRequest = { toAccountDropdownExpanded = false },
                    modifier = Modifier.background(DarkSurfaceElevated)
                ) {
                    accounts.filter { it.id != selectedAccountId }.forEach { acc ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(text = acc.name, color = TextWhite, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = "Balance: ${CurrencyFormatter.formatPaisa(acc.balancePaisa)}",
                                        color = TextGray,
                                        fontSize = 12.sp
                                    )
                                }
                            },
                            onClick = {
                                selectedToAccountId = acc.id
                                toAccountDropdownExpanded = false
                            }
                        )
                    }
                }
            }
        }

        // Category selection (only for Credit and Expense)
        if (selectedType != "TRANSFER") {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Category",
                style = MaterialTheme.typography.labelMedium.copy(
                    color = TextGrayLight,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            )
            Spacer(modifier = Modifier.height(6.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(DarkSurfaceCard)
                    .border(1.dp, Color(0xFF1B2E4E), RoundedCornerShape(14.dp))
                    .clickable { categoryDropdownExpanded = true }
                    .padding(horizontal = 16.dp, vertical = 14.dp)
                    .testTag("category_dropdown_trigger")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = selectedCategory,
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = TextWhite,
                            fontWeight = FontWeight.Medium,
                            fontSize = 15.sp
                        )
                    )

                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Select Category",
                        tint = TextGray
                    )
                }

                DropdownMenu(
                    expanded = categoryDropdownExpanded,
                    onDismissRequest = { categoryDropdownExpanded = false },
                    modifier = Modifier.background(DarkSurfaceElevated)
                ) {
                    displayCategories.forEach { catName ->
                        DropdownMenuItem(
                            text = { Text(text = catName, color = TextWhite) },
                            onClick = {
                                selectedCategory = catName
                                categoryDropdownExpanded = false
                                errorMessage = null
                            }
                        )
                    }
                }
            }

            // If "Other" is selected, show "Enter Category Name" input
            AnimatedVisibility(visible = selectedCategory == "Other") {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Enter Category Name",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = EmeraldGreen,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = customCategoryInput,
                        onValueChange = {
                            customCategoryInput = it
                            errorMessage = null
                        },
                        placeholder = {
                            Text(
                                text = if (selectedType == "CREDIT") "e.g. Freelancing, Gift, Bonus" else "e.g. Transport, Shopping, Education",
                                color = TextGray
                            )
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
                            .testTag("custom_category_input_field")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Date selection
        Text(
            text = "Date",
            style = MaterialTheme.typography.labelMedium.copy(
                color = TextGrayLight,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        )
        Spacer(modifier = Modifier.height(6.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(DarkSurfaceCard)
                .border(1.dp, Color(0xFF1B2E4E), RoundedCornerShape(14.dp))
                .clickable { datePickerDialog.show() }
                .padding(horizontal = 16.dp, vertical = 14.dp)
                .testTag("date_picker_trigger")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = CurrencyFormatter.formatDate(selectedTimestamp),
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = TextWhite,
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp
                    )
                )

                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = "Pick Date",
                    tint = TextGray
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Notes (Optional)
        Text(
            text = "Notes (Optional)",
            style = MaterialTheme.typography.labelMedium.copy(
                color = TextGrayLight,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        )
        Spacer(modifier = Modifier.height(6.dp))

        OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            placeholder = { Text("Add a note...", color = TextGray) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextWhite,
                unfocusedTextColor = TextWhite,
                focusedContainerColor = DarkSurfaceCard,
                unfocusedContainerColor = DarkSurfaceCard,
                focusedBorderColor = Color(0xFF263C5F),
                unfocusedBorderColor = Color(0xFF1B2E4E)
            ),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("note_input_field")
        )

        // Error message if any
        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = errorMessage ?: "",
                color = ExpenseRed,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                modifier = Modifier.testTag("add_transaction_error")
            )
        }

        Spacer(modifier = Modifier.height(26.dp))

        // Primary Save CTA Button
        val btnColor = when (selectedType) {
            "CREDIT" -> EmeraldGreen
            "EXPENSE" -> ExpenseRed
            else -> AccentBlue
        }
        val btnLabel = when (selectedType) {
            "CREDIT" -> "Save Credit"
            "EXPENSE" -> "Save Expense"
            else -> "Execute Transfer"
        }

        Button(
            onClick = {
                val paisa = CurrencyFormatter.parseToPaisa(amountInput)
                if (paisa <= 0) {
                    errorMessage = "Please enter an amount greater than zero"
                    return@Button
                }

                // Determine final category
                val finalCategory = if (selectedCategory == "Other") {
                    val custom = customCategoryInput.trim()
                    if (custom.isBlank()) {
                        errorMessage = "Please enter a category name"
                        return@Button
                    }
                    onSaveCustomCategory(custom, selectedType)
                    custom
                } else {
                    selectedCategory
                }

                if (selectedType == "EXPENSE") {
                    val available = currentAccount?.balancePaisa ?: 0L
                    if (paisa > available) {
                        errorMessage = "Insufficient balance in ${currentAccount?.name} (${CurrencyFormatter.formatPaisa(available)})"
                        return@Button
                    }
                    onSaveExpense(paisa, selectedAccountId, finalCategory, note, selectedTimestamp)
                } else if (selectedType == "CREDIT") {
                    onSaveCredit(paisa, selectedAccountId, finalCategory, note, selectedTimestamp)
                } else if (selectedType == "TRANSFER") {
                    if (selectedAccountId == selectedToAccountId) {
                        errorMessage = "Source and destination accounts must be different"
                        return@Button
                    }
                    val available = currentAccount?.balancePaisa ?: 0L
                    if (paisa > available) {
                        errorMessage = "Insufficient balance in ${currentAccount?.name} (${CurrencyFormatter.formatPaisa(available)})"
                        return@Button
                    }
                    onSaveTransfer(paisa, selectedAccountId, selectedToAccountId, note, selectedTimestamp)
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = btnColor),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("save_transaction_button")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = btnLabel,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}
