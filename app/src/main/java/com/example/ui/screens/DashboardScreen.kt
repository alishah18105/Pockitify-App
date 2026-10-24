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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Remove
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Account
import com.example.data.model.Transaction
import com.example.ui.DashboardUiState
import com.example.ui.components.AblAccountCard
import com.example.ui.components.CashWalletCard
import com.example.ui.components.ResetMonthDialog
import com.example.ui.components.TotalBalanceCard
import com.example.ui.components.TransactionItemView
import com.example.ui.components.UblAccountCard
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.ExpenseRedSoft
import com.example.ui.theme.MidnightNavy
import com.example.ui.theme.MintGreen
import com.example.ui.theme.TextGray
import com.example.ui.theme.TextGrayLight
import com.example.ui.theme.TextWhite
import com.example.util.CurrencyFormatter

@Composable
fun DashboardScreen(
    state: DashboardUiState,
    unreadNotificationCount: Int = 0,
    onToggleHideBalance: () -> Unit,
    onNavigateToAddTransaction: (defaultType: String) -> Unit,
    onNavigateToAccounts: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToNotifications: () -> Unit = {},
    onSelectTransaction: (Transaction) -> Unit,
    onConfirmResetMonth: (newPeriodName: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showResetDialog by remember { mutableStateOf(false) }

    val ablAccount = state.accounts.firstOrNull { it.id == "abl" }
    val ublAccount = state.accounts.firstOrNull { it.id == "ubl" }
    val walletAccount = state.accounts.firstOrNull { it.id == "wallet" }

    val isHidden = state.userProfile.isBalanceHidden

    if (showResetDialog) {
        ResetMonthDialog(
            activePeriod = state.activePeriod,
            periodCreditPaisa = state.periodCreditPaisa,
            periodExpensePaisa = state.periodExpensePaisa,
            onDismiss = { showResetDialog = false },
            onConfirmReset = { name ->
                showResetDialog = false
                onConfirmResetMonth(name)
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MidnightNavy)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .testTag("dashboard_screen")
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                // "Pockitify" with stylized green dot
                Text(
                    text = buildAnnotatedString {
                        append("Pock")
                        withStyle(style = SpanStyle(color = MintGreen)) {
                            append("i")
                        }
                        append("tify")
                    },
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color = TextWhite,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 26.sp,
                        letterSpacing = (-0.5).sp
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Track every move your money makes.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextGray,
                        fontSize = 12.sp
                    )
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Notification Bell with badge
                Box(contentAlignment = Alignment.TopEnd) {
                    IconButton(
                        onClick = onNavigateToNotifications,
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("notification_bell_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = TextWhite,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    if (unreadNotificationCount > 0) {
                        Box(
                            modifier = Modifier
                                .padding(top = 4.dp, end = 4.dp)
                                .size(if (unreadNotificationCount > 9) 18.dp else 16.dp)
                                .clip(CircleShape)
                                .background(ExpenseRed),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (unreadNotificationCount > 9) "9+" else unreadNotificationCount.toString(),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = TextWhite,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                // User Avatar Circle
                val initial = state.userProfile.userName.take(1).uppercase().ifBlank { "U" }
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(listOf(Color(0xFF0F766E), Color(0xFF14B8A6)))
                        )
                        .clickable(onClick = onNavigateToSettings)
                        .testTag("user_avatar_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initial,
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = TextWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Total Available Balance Card
        TotalBalanceCard(
            totalBalancePaisa = state.totalBalancePaisa,
            isHidden = isHidden,
            onToggleVisibility = onToggleHideBalance
        )

        Spacer(modifier = Modifier.height(24.dp))

        // "Your Accounts" Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Your Accounts",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = TextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            )

            Text(
                text = "View All",
                style = MaterialTheme.typography.labelLarge.copy(
                    color = AccentBlue,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                ),
                modifier = Modifier
                    .clickable(onClick = onNavigateToAccounts)
                    .padding(4.dp)
                    .testTag("view_all_accounts")
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Side-by-side Bank Cards: ABL and UBL
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            AblAccountCard(
                account = ablAccount,
                isHidden = isHidden,
                onClick = onNavigateToAccounts,
                modifier = Modifier.weight(1f)
            )
            UblAccountCard(
                account = ublAccount,
                isHidden = isHidden,
                onClick = onNavigateToAccounts,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Full width Cash Wallet Card
        CashWalletCard(
            account = walletAccount,
            isHidden = isHidden,
            onClick = onNavigateToAccounts
        )

        Spacer(modifier = Modifier.height(24.dp))

        // "Current Tracking Period" Section (Manual Reset)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Current Tracking Period",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = TextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                )
                Text(
                    text = "(Manual Reset)",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextGray,
                        fontSize = 12.sp
                    )
                )
            }

            // "Reset Month" button
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(DarkSurfaceElevated)
                    .border(1.dp, Color(0xFF263C5F), RoundedCornerShape(20.dp))
                    .clickable { showResetDialog = true }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .testTag("reset_month_button"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = "Reset Month",
                    tint = TextWhite,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Reset Month",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = TextWhite,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Summary Cards: Total Credit & Total Expense
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Total Credit Card
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(18.dp))
                    .background(DarkSurfaceCard)
                    .border(1.dp, Color(0xFF1B2E4E), RoundedCornerShape(18.dp))
                    .padding(16.dp)
                    .testTag("total_credit_card")
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
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

                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = "Chart",
                            tint = EmeraldGreen,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Total Credit",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextGray,
                            fontSize = 12.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = CurrencyFormatter.formatPaisa(state.periodCreditPaisa),
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = TextWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "${state.periodCreditCount} transactions",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextGray,
                            fontSize = 11.sp
                        )
                    )
                }
            }

            // Total Expense Card
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(18.dp))
                    .background(DarkSurfaceCard)
                    .border(1.dp, Color(0xFF1B2E4E), RoundedCornerShape(18.dp))
                    .padding(16.dp)
                    .testTag("total_expense_card")
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
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

                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = "Chart",
                            tint = ExpenseRed,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Total Expense",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextGray,
                            fontSize = 12.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = CurrencyFormatter.formatPaisa(state.periodExpensePaisa),
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = TextWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "${state.periodExpenseCount} transactions",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextGray,
                            fontSize = 11.sp
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // "Quick Actions" Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = TextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            )

            Text(
                text = "See All",
                style = MaterialTheme.typography.labelLarge.copy(
                    color = AccentBlue,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                ),
                modifier = Modifier
                    .clickable(onClick = onNavigateToHistory)
                    .padding(4.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Three Big Rounded Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Add Credit
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(86.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFF0D5E4B))
                    .border(1.dp, Color(0xFF10B981), RoundedCornerShape(18.dp))
                    .clickable { onNavigateToAddTransaction("CREDIT") }
                    .testTag("quick_action_credit"),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(EmeraldGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Credit",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Add Credit",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = TextWhite,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                    )
                }
            }

            // Add Expense
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(86.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFF5A2222))
                    .border(1.dp, Color(0xFFEF4444), RoundedCornerShape(18.dp))
                    .clickable { onNavigateToAddTransaction("EXPENSE") }
                    .testTag("quick_action_expense"),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(ExpenseRed),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Add Expense",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Add Expense",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = TextWhite,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                    )
                }
            }

            // Transfer
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(86.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFF133E76))
                    .border(1.dp, Color(0xFF3B82F6), RoundedCornerShape(18.dp))
                    .clickable { onNavigateToAddTransaction("TRANSFER") }
                    .testTag("quick_action_transfer"),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(AccentBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CompareArrows,
                            contentDescription = "Transfer",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Transfer",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = TextWhite,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // "Recent Transactions" Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Recent Transactions",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = TextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            )

            Text(
                text = "View All",
                style = MaterialTheme.typography.labelLarge.copy(
                    color = AccentBlue,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                ),
                modifier = Modifier
                    .clickable(onClick = onNavigateToHistory)
                    .padding(4.dp)
                    .testTag("view_all_transactions")
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (state.recentTransactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No recent transactions yet.\nTap '+ Add' to record income or expenses.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = TextGray,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                )
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                state.recentTransactions.forEach { tx ->
                    TransactionItemView(
                        transaction = tx,
                        onClick = { onSelectTransaction(tx) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp)) // padding for bottom nav
    }
}
