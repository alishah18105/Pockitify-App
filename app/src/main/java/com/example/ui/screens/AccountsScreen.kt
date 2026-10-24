package com.example.ui.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Account
import com.example.data.model.Transaction
import com.example.ui.components.TransactionItemView
import com.example.ui.theme.AblBlue
import com.example.ui.theme.AblGold
import com.example.ui.theme.AblOrange
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.MidnightNavy
import com.example.ui.theme.MintGreen
import com.example.ui.theme.TextGray
import com.example.ui.theme.TextWhite
import com.example.ui.theme.UblBlue
import com.example.ui.theme.UblGold
import com.example.ui.theme.WalletGreenDark
import com.example.ui.theme.WalletGreenMedium
import com.example.util.CurrencyFormatter

@Composable
fun AccountsScreen(
    accounts: List<Account>,
    allTransactions: List<Transaction>,
    onBack: () -> Unit,
    onAdjustBalance: (accountId: String, newBalancePaisa: Long, note: String) -> Unit,
    onSelectTransaction: (Transaction) -> Unit,
    onNavigateToAddTransaction: (type: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf("All") } // "All", "ABL", "UBL", "Wallet"
    var accountToAdjust by remember { mutableStateOf<Account?>(null) }
    var adjustAmountInput by remember { mutableStateOf("") }
    var adjustReasonInput by remember { mutableStateOf("") }

    val filteredAccounts = when (selectedFilter) {
        "ABL" -> accounts.filter { it.id == "abl" }
        "UBL" -> accounts.filter { it.id == "ubl" }
        "Wallet" -> accounts.filter { it.id == "wallet" }
        else -> accounts
    }

    val filteredTransactions = when (selectedFilter) {
        "ABL" -> allTransactions.filter { it.fromAccountId == "abl" || it.toAccountId == "abl" }
        "UBL" -> allTransactions.filter { it.fromAccountId == "ubl" || it.toAccountId == "ubl" }
        "Wallet" -> allTransactions.filter { it.fromAccountId == "wallet" || it.toAccountId == "wallet" }
        else -> allTransactions
    }

    if (accountToAdjust != null) {
        AlertDialog(
            onDismissRequest = { accountToAdjust = null },
            containerColor = DarkSurface,
            title = {
                Text(
                    text = "Adjust ${accountToAdjust?.name} Balance",
                    color = TextWhite,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "Current balance: ${CurrencyFormatter.formatPaisa(accountToAdjust?.balancePaisa ?: 0L)}",
                        color = TextGray,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = adjustAmountInput,
                        onValueChange = { adjustAmountInput = it },
                        label = { Text("New Balance (PKR)", color = TextGray) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedBorderColor = EmeraldGreen,
                            unfocusedBorderColor = Color(0xFF263C5F)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = adjustReasonInput,
                        onValueChange = { adjustReasonInput = it },
                        label = { Text("Reason (e.g. Audit, cash reconciliation)", color = TextGray) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedBorderColor = EmeraldGreen,
                            unfocusedBorderColor = Color(0xFF263C5F)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val paisa = CurrencyFormatter.parseToPaisa(adjustAmountInput)
                        accountToAdjust?.let { acc ->
                            onAdjustBalance(acc.id, paisa, adjustReasonInput)
                        }
                        accountToAdjust = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                ) {
                    Text("Save Adjustment", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { accountToAdjust = null }) {
                    Text("Cancel", color = TextGray)
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MidnightNavy)
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .testTag("accounts_screen")
    ) {
        // Top Header
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
                    text = "Accounts",
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = TextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                )
            }

            // Quick Add Transaction button
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(DarkSurfaceElevated)
                    .clickable { onNavigateToAddTransaction("CREDIT") }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add",
                    tint = MintGreen,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Add",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = TextWhite,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Filter Pills: All, ABL, UBL, Wallet
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("All", "ABL", "UBL", "Wallet").forEach { filter ->
                val isSelected = selectedFilter == filter
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) EmeraldGreen else DarkSurfaceCard)
                        .clickable { selectedFilter = filter },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = filter,
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = if (isSelected) Color.White else TextGray,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(filteredAccounts) { acc ->
                AccountDetailedCard(
                    account = acc,
                    onAdjustBalance = {
                        accountToAdjust = acc
                        adjustAmountInput = (acc.balancePaisa / 100.0).toString()
                        adjustReasonInput = ""
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = if (selectedFilter == "All") "All Account Activity" else "$selectedFilter Activity",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = TextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                )
            }

            if (filteredTransactions.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No transactions found for this account filter.",
                            style = MaterialTheme.typography.bodyMedium.copy(color = TextGray)
                        )
                    }
                }
            } else {
                items(filteredTransactions) { tx ->
                    TransactionItemView(
                        transaction = tx,
                        onClick = { onSelectTransaction(tx) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }
}

@Composable
fun AccountDetailedCard(
    account: Account,
    onAdjustBalance: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isAbl = account.id == "abl"
    val isUbl = account.id == "ubl"
    val isWallet = account.id == "wallet"

    val cardBrush = when {
        isAbl -> Brush.linearGradient(listOf(AblBlue, Color(0xFF091E36)))
        isUbl -> Brush.linearGradient(listOf(UblBlue, Color(0xFF073874)))
        else -> Brush.linearGradient(listOf(WalletGreenDark, WalletGreenMedium, Color(0xFF0F766E)))
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(138.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(cardBrush)
            .border(
                1.dp,
                if (isAbl) Color(0xFF1E385D) else if (isUbl) Color(0xFF19559B) else Color(0xFF0D9488),
                RoundedCornerShape(22.dp)
            )
            .padding(16.dp)
    ) {
        // Decorative ribbons
        if (isAbl) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val p = Path().apply {
                    moveTo(size.width * 0.6f, 0f)
                    cubicTo(size.width * 0.7f, size.height * 0.3f, size.width * 0.75f, size.height * 0.7f, size.width, size.height * 0.85f)
                    lineTo(size.width, 0f)
                    close()
                }
                drawPath(p, Brush.linearGradient(listOf(AblOrange, AblGold)))
            }
        } else if (isUbl) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val p = Path().apply {
                    moveTo(size.width * 0.65f, 0f)
                    cubicTo(size.width * 0.6f, size.height * 0.4f, size.width * 0.85f, size.height * 0.5f, size.width, size.height * 0.8f)
                    lineTo(size.width, 0f)
                    close()
                }
                drawPath(p, Brush.linearGradient(listOf(UblGold, Color(0xFFFDE047))))
            }
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isAbl) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF11223A)),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.size(16.dp)) {
                                val p = Path().apply {
                                    moveTo(size.width * 0.5f, 0f)
                                    lineTo(size.width, size.height)
                                    lineTo(0f, size.height)
                                    close()
                                }
                                drawPath(p, color = AblOrange)
                            }
                        }
                    } else if (isUbl) {
                        Box(
                            modifier = Modifier
                                .size(width = 38.dp, height = 22.dp)
                                .clip(RoundedCornerShape(11.dp))
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "UBL",
                                color = UblBlue,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 10.sp
                            )
                        }
                    } else {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            tint = MintGreen,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = account.name,
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = TextWhite,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = account.fullName,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextGray,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                // Adjust balance quick action button
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x40000000))
                        .clickable(onClick = onAdjustBalance)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Adjust",
                        tint = TextWhite,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Adjust",
                        color = TextWhite,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = account.accountNumberMasked,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xCCFFFFFF),
                        letterSpacing = 1.sp,
                        fontSize = 13.sp
                    )
                )

                Text(
                    text = CurrencyFormatter.formatPaisa(account.balancePaisa),
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = TextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                )
            }
        }
    }
}
