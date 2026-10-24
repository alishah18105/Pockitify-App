package com.example.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Account
import com.example.data.model.TrackingPeriod
import com.example.data.model.Transaction
import com.example.ui.components.CategoryDonutChart
import com.example.ui.components.ChartSlice
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.ExpenseRedSoft
import com.example.ui.theme.MidnightNavy
import com.example.ui.theme.TextGray
import com.example.ui.theme.TextGrayLight
import com.example.ui.theme.TextWhite
import com.example.util.CurrencyFormatter

@Composable
fun ReportsScreen(
    activePeriod: TrackingPeriod?,
    allPeriods: List<TrackingPeriod>,
    transactions: List<Transaction>,
    accounts: List<Account>,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedPeriodId by remember(activePeriod) {
        mutableStateOf(activePeriod?.id)
    }
    var periodDropdownExpanded by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf("Overview") } // "Overview", "Categories", "Accounts"

    val currentPeriod = allPeriods.firstOrNull { it.id == selectedPeriodId } ?: activePeriod

    // Filter transactions for this period
    val periodTxs = if (currentPeriod != null) {
        transactions.filter { it.periodId == currentPeriod.id }
    } else {
        transactions
    }

    var totalCreditPaisa = 0L
    var totalExpensePaisa = 0L
    val expenseByCategory = mutableMapOf<String, Long>()

    for (tx in periodTxs) {
        if (tx.type == "CREDIT") {
            totalCreditPaisa += tx.amountPaisa
        } else if (tx.type == "EXPENSE") {
            totalExpensePaisa += tx.amountPaisa
            expenseByCategory[tx.category] = (expenseByCategory[tx.category] ?: 0L) + tx.amountPaisa
        }
    }

    val chartColors = listOf(
        Color(0xFF10B981), // Emerald
        Color(0xFF3B82F6), // Blue
        Color(0xFFF59E0B), // Amber
        Color(0xFF8B5CF6), // Purple
        Color(0xFFEC4899), // Pink
        Color(0xFF06B6D4), // Cyan
        Color(0xFF64748B)  // Gray / Others
    )

    val slices = expenseByCategory.entries.mapIndexed { index, entry ->
        val pct = if (totalExpensePaisa > 0) (entry.value.toFloat() / totalExpensePaisa.toFloat()) * 100f else 0f
        ChartSlice(
            label = entry.key,
            amountPaisa = entry.value,
            percentage = pct,
            color = chartColors[index % chartColors.size]
        )
    }.sortedByDescending { it.amountPaisa }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MidnightNavy)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .testTag("reports_screen")
    ) {
        // Header with Back and Period dropdown
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
                    text = "Reports",
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = TextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                )
            }

            // Period Selector Dropdown
            Box {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(DarkSurfaceElevated)
                        .clickable { periodDropdownExpanded = true }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .testTag("period_selector_button"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = currentPeriod?.name ?: "All Time",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = TextWhite,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Select",
                        tint = TextGray,
                        modifier = Modifier.size(16.dp)
                    )
                }

                DropdownMenu(
                    expanded = periodDropdownExpanded,
                    onDismissRequest = { periodDropdownExpanded = false },
                    modifier = Modifier.background(DarkSurfaceElevated)
                ) {
                    allPeriods.forEach { period ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(
                                        text = period.name,
                                        color = if (period.isActive) EmeraldGreen else TextWhite,
                                        fontWeight = if (period.isActive) FontWeight.Bold else FontWeight.Normal
                                    )
                                    if (period.isActive) {
                                        Text(text = "Active Tracking Period", color = TextGray, fontSize = 11.sp)
                                    } else {
                                        Text(text = "Archived Period", color = TextGray, fontSize = 11.sp)
                                    }
                                }
                            },
                            onClick = {
                                selectedPeriodId = period.id
                                periodDropdownExpanded = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Total Credit & Total Expense Summary Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Total Credit
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(18.dp))
                    .background(DarkSurfaceCard)
                    .border(1.dp, Color(0xFF1B2E4E), RoundedCornerShape(18.dp))
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(EmeraldGlow),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowDownward,
                            contentDescription = "Credit",
                            tint = EmeraldGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = "Total Credit",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextGray,
                                fontSize = 11.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = CurrencyFormatter.formatPaisa(totalCreditPaisa, includePrefix = false, showDecimals = false),
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = EmeraldGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        )
                    }
                }
            }

            // Total Expense
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(18.dp))
                    .background(DarkSurfaceCard)
                    .border(1.dp, Color(0xFF1B2E4E), RoundedCornerShape(18.dp))
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(ExpenseRedSoft),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowUpward,
                            contentDescription = "Expense",
                            tint = ExpenseRed,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = "Total Expense",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextGray,
                                fontSize = 11.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = CurrencyFormatter.formatPaisa(totalExpensePaisa, includePrefix = false, showDecimals = false),
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = ExpenseRed,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Sub-tabs: Overview, Categories, Accounts
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(DarkSurface)
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Overview", "Categories", "Accounts").forEach { tab ->
                val isSelected = selectedTab == tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) EmeraldGreen else Color.Transparent)
                        .clickable { selectedTab = tab },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tab,
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = if (isSelected) Color.White else TextGray,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        when (selectedTab) {
            "Overview", "Categories" -> {
                // "Expense by Category" Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(DarkSurfaceCard)
                        .border(1.dp, Color(0xFF1B2E4E), RoundedCornerShape(20.dp))
                        .padding(18.dp)
                ) {
                    Column {
                        Text(
                            text = "Expense by Category",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = TextWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        CategoryDonutChart(
                            slices = slices,
                            totalAmountPaisa = totalExpensePaisa
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Net Cashflow Card
                val netPaisa = totalCreditPaisa - totalExpensePaisa
                val isPositive = netPaisa >= 0
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(DarkSurfaceCard)
                        .border(1.dp, Color(0xFF1B2E4E), RoundedCornerShape(20.dp))
                        .padding(18.dp)
                ) {
                    Column {
                        Text(
                            text = "Period Net Cashflow",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = TextWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = CurrencyFormatter.formatPaisa(netPaisa),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                color = if (isPositive) EmeraldGreen else ExpenseRed,
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isPositive) "You are net cashflow positive for this period." else "Expenses exceed income for this period.",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextGray)
                        )
                    }
                }
            }

            "Accounts" -> {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    accounts.forEach { acc ->
                        val accCredits = periodTxs.filter { it.toAccountId == acc.id && it.type == "CREDIT" }.sumOf { it.amountPaisa }
                        val accExpenses = periodTxs.filter { it.fromAccountId == acc.id && it.type == "EXPENSE" }.sumOf { it.amountPaisa }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(DarkSurfaceCard)
                                .border(1.dp, Color(0xFF1B2E4E), RoundedCornerShape(16.dp))
                                .padding(16.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = acc.name,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            color = TextWhite,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    Text(
                                        text = "Current: ${CurrencyFormatter.formatPaisa(acc.balancePaisa)}",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = TextGrayLight,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = "Credits in Period:", color = TextGray, fontSize = 12.sp)
                                    Text(text = "+ ${CurrencyFormatter.formatPaisa(accCredits)}", color = EmeraldGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = "Expenses in Period:", color = TextGray, fontSize = 12.sp)
                                    Text(text = "- ${CurrencyFormatter.formatPaisa(accExpenses)}", color = ExpenseRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}
