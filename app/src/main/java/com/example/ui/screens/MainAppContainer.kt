package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Transaction
import com.example.ui.MainViewModel
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.MidnightNavy
import com.example.ui.theme.MintGreen
import com.example.ui.theme.TextGray
import com.example.ui.theme.TextWhite

enum class AppScreen {
    HOME,
    ACCOUNTS,
    ADD_TRANSACTION,
    HISTORY,
    REPORTS,
    SETTINGS,
    NOTIFICATIONS
}

@Composable
fun MainAppContainer(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val dashboardState by viewModel.dashboardState.collectAsStateWithLifecycle()
    val allTransactions by viewModel.allTransactions.collectAsStateWithLifecycle()
    val allPeriods by viewModel.allPeriods.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val unreadNotificationCount by viewModel.unreadNotificationCount.collectAsStateWithLifecycle()
    val navigationTarget by viewModel.navigationTarget.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()

    var currentScreen by remember { mutableStateOf(AppScreen.HOME) }
    var initialAddType by remember { mutableStateOf("CREDIT") }

    // Request notification permission gracefully on Android 13+
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val permissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { /* Handled gracefully, in-app notifications still work if denied */ }

        LaunchedEffect(Unit) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    LaunchedEffect(navigationTarget) {
        if (navigationTarget == "notifications") {
            currentScreen = AppScreen.NOTIFICATIONS
            viewModel.clearNavigationTarget()
        }
    }

    LaunchedEffect(message) {
        message?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearMessage()
        }
    }

    // Handle back button on sub-screens
    BackHandler(enabled = currentScreen != AppScreen.HOME) {
        currentScreen = AppScreen.HOME
    }

    val userProfile = dashboardState.userProfile

    // 1. If Setup not yet completed -> Show Onboarding
    if (!userProfile.isSetupCompleted) {
        OnboardingSetupScreen(
            onCompleteSetup = { name, pin, abl, ubl, wallet ->
                viewModel.completeOnboarding(name, pin, abl, ubl, wallet)
            }
        )
        return
    }

    // 2. If Locked -> Show PIN Lock Screen
    if (userProfile.isLocked) {
        PinLockScreen(
            userName = userProfile.userName,
            onVerifyPin = { pin, onSuccess, onError ->
                viewModel.verifyPin(pin, onSuccess, onError)
            }
        )
        return
    }

    // 3. Unlocked Main Application
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MidnightNavy,
        bottomBar = {
            // Only show bottom navigation on primary screens
            if (currentScreen != AppScreen.ADD_TRANSACTION && currentScreen != AppScreen.SETTINGS && currentScreen != AppScreen.NOTIFICATIONS) {
                PockitifyBottomBar(
                    currentScreen = currentScreen,
                    onNavigate = { screen ->
                        if (screen == AppScreen.ADD_TRANSACTION) {
                            initialAddType = "CREDIT"
                        }
                        currentScreen = screen
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                AppScreen.HOME -> {
                    DashboardScreen(
                        state = dashboardState,
                        unreadNotificationCount = unreadNotificationCount,
                        onToggleHideBalance = { viewModel.toggleHideBalance() },
                        onNavigateToAddTransaction = { defaultType ->
                            initialAddType = defaultType
                            currentScreen = AppScreen.ADD_TRANSACTION
                        },
                        onNavigateToAccounts = { currentScreen = AppScreen.ACCOUNTS },
                        onNavigateToHistory = { currentScreen = AppScreen.HISTORY },
                        onNavigateToSettings = { currentScreen = AppScreen.SETTINGS },
                        onNavigateToNotifications = { currentScreen = AppScreen.NOTIFICATIONS },
                        onSelectTransaction = { tx ->
                            currentScreen = AppScreen.HISTORY
                        },
                        onConfirmResetMonth = { newPeriodName ->
                            viewModel.resetMonth(newPeriodName)
                        }
                    )
                }

                AppScreen.ACCOUNTS -> {
                    AccountsScreen(
                        accounts = dashboardState.accounts,
                        allTransactions = allTransactions,
                        onBack = { currentScreen = AppScreen.HOME },
                        onAdjustBalance = { accountId, newBalance, note ->
                            viewModel.adjustBalance(accountId, newBalance, note) {}
                        },
                        onSelectTransaction = { currentScreen = AppScreen.HISTORY },
                        onNavigateToAddTransaction = { defaultType ->
                            initialAddType = defaultType
                            currentScreen = AppScreen.ADD_TRANSACTION
                        }
                    )
                }

                AppScreen.ADD_TRANSACTION -> {
                    AddTransactionScreen(
                        initialType = initialAddType,
                        accounts = dashboardState.accounts,
                        categories = dashboardState.categories,
                        onBack = { currentScreen = AppScreen.HOME },
                        onSaveCredit = { amt, acc, cat, note, time ->
                            viewModel.addCredit(amt, acc, cat, note, time) {
                                currentScreen = AppScreen.HOME
                            }
                        },
                        onSaveExpense = { amt, acc, cat, note, time ->
                            viewModel.addExpense(amt, acc, cat, note, time) {
                                currentScreen = AppScreen.HOME
                            }
                        },
                        onSaveTransfer = { amt, from, to, note, time ->
                            viewModel.addTransfer(amt, from, to, note, time) {
                                currentScreen = AppScreen.HOME
                            }
                        },
                        onSaveCustomCategory = { name, type ->
                            viewModel.addCategory(name, type) {}
                        }
                    )
                }

                AppScreen.HISTORY -> {
                    HistoryScreen(
                        transactions = allTransactions,
                        accounts = dashboardState.accounts,
                        categories = dashboardState.categories,
                        userName = userProfile.userName,
                        onBack = { currentScreen = AppScreen.HOME },
                        onDeleteTransaction = { txId ->
                            viewModel.deleteTransaction(txId)
                        }
                    )
                }

                AppScreen.REPORTS -> {
                    ReportsScreen(
                        activePeriod = dashboardState.activePeriod,
                        allPeriods = allPeriods,
                        transactions = allTransactions,
                        accounts = dashboardState.accounts,
                        onBack = { currentScreen = AppScreen.HOME }
                    )
                }

                AppScreen.SETTINGS -> {
                    SettingsScreen(
                        userProfile = userProfile,
                        categories = dashboardState.categories,
                        financeRepository = viewModel.financeRepository,
                        onBack = { currentScreen = AppScreen.HOME },
                        onUpdateUserName = { name -> viewModel.updateUserName(name) },
                        onChangePin = { cur, new, onS, onE -> viewModel.changePin(cur, new, onS, onE) },
                        onAddCategory = { name, type -> viewModel.addCategory(name, type) {} },
                        onDeleteCategory = { cat -> viewModel.deleteCategory(cat) {} },
                        onResetMonthClicked = {
                            viewModel.resetMonth()
                        },
                        onLockApp = { viewModel.lockApp() }
                    )
                }

                AppScreen.NOTIFICATIONS -> {
                    NotificationsScreen(
                        notifications = notifications,
                        unreadCount = unreadNotificationCount,
                        onBack = { currentScreen = AppScreen.HOME },
                        onMarkAsRead = { id -> viewModel.markNotificationAsRead(id) },
                        onMarkAllAsRead = { viewModel.markAllNotificationsAsRead() },
                        onDeleteNotification = { id -> viewModel.deleteNotification(id) }
                    )
                }
            }
        }
    }
}

@Composable
fun PockitifyBottomBar(
    currentScreen: AppScreen,
    onNavigate: (AppScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .clip(RoundedCornerShape(26.dp))
                .background(DarkSurface)
                .border(1.dp, Color(0xFF1E3557), RoundedCornerShape(26.dp))
                .padding(horizontal = 8.dp)
                .testTag("bottom_nav_bar")
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Home Tab
                BottomNavItem(
                    icon = Icons.Default.Home,
                    label = "Home",
                    isSelected = currentScreen == AppScreen.HOME,
                    onClick = { onNavigate(AppScreen.HOME) },
                    testTag = "nav_home"
                )

                // Accounts Tab
                BottomNavItem(
                    icon = Icons.Default.AccountBalance,
                    label = "Accounts",
                    isSelected = currentScreen == AppScreen.ACCOUNTS,
                    onClick = { onNavigate(AppScreen.ACCOUNTS) },
                    testTag = "nav_accounts"
                )

                // Center Raised Add Button (+)
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(listOf(EmeraldGreen, MintGreen))
                        )
                        .clickable { onNavigate(AppScreen.ADD_TRANSACTION) }
                        .testTag("nav_add_center_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Transaction",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                // History Tab
                BottomNavItem(
                    icon = Icons.Default.History,
                    label = "History",
                    isSelected = currentScreen == AppScreen.HISTORY,
                    onClick = { onNavigate(AppScreen.HISTORY) },
                    testTag = "nav_history"
                )

                // Reports Tab
                BottomNavItem(
                    icon = Icons.Default.Analytics,
                    label = "Reports",
                    isSelected = currentScreen == AppScreen.REPORTS,
                    onClick = { onNavigate(AppScreen.REPORTS) },
                    testTag = "nav_reports"
                )
            }
        }
    }
}

@Composable
fun BottomNavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) MintGreen else TextGray,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                color = if (isSelected) MintGreen else TextGray,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 10.sp
            )
        )
    }
}
