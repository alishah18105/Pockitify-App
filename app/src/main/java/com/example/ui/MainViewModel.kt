package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.model.Account
import com.example.data.model.Category
import com.example.data.model.TrackingPeriod
import com.example.data.model.Transaction
import com.example.data.model.NotificationItem
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.preferences.UserProfileState
import com.example.data.repository.FinanceRepository
import com.example.util.CurrencyFormatter
import com.example.util.NotificationHelper
import com.example.util.SecurityUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DashboardUiState(
    val userProfile: UserProfileState = UserProfileState(),
    val accounts: List<Account> = emptyList(),
    val totalBalancePaisa: Long = 0L,
    val recentTransactions: List<Transaction> = emptyList(),
    val activePeriod: TrackingPeriod? = null,
    val periodTransactions: List<Transaction> = emptyList(),
    val periodCreditPaisa: Long = 0L,
    val periodExpensePaisa: Long = 0L,
    val periodCreditCount: Int = 0,
    val periodExpenseCount: Int = 0,
    val categories: List<Category> = emptyList(),
    val allPeriods: List<TrackingPeriod> = emptyList()
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    val database = AppDatabase.getDatabase(application, viewModelScope)
    val preferencesRepository = UserPreferencesRepository(application)
    val financeRepository = FinanceRepository(database)

    val notifications: StateFlow<List<NotificationItem>> = financeRepository.notificationsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val unreadNotificationCount: StateFlow<Int> = financeRepository.unreadNotificationsCountFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    private val _navigationTarget = MutableStateFlow<String?>(null)
    val navigationTarget: StateFlow<String?> = _navigationTarget.asStateFlow()

    fun navigateTo(target: String) {
        _navigationTarget.value = target
    }

    fun clearNavigationTarget() {
        _navigationTarget.value = null
    }

    fun markNotificationAsRead(id: Long) {
        viewModelScope.launch {
            financeRepository.markNotificationAsRead(id)
        }
    }

    fun markAllNotificationsAsRead() {
        viewModelScope.launch {
            financeRepository.markAllNotificationsAsRead()
        }
    }

    fun deleteNotification(id: Long) {
        viewModelScope.launch {
            financeRepository.deleteNotification(id)
        }
    }

    fun clearAllNotifications() {
        viewModelScope.launch {
            financeRepository.clearAllNotifications()
        }
    }

    val userProfile: StateFlow<UserProfileState> = preferencesRepository.userProfileFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserProfileState()
        )

    val accounts: StateFlow<List<Account>> = financeRepository.accountsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allTransactions: StateFlow<List<Transaction>> = financeRepository.allTransactionsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val categories: StateFlow<List<Category>> = financeRepository.categoriesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allPeriods: StateFlow<List<TrackingPeriod>> = financeRepository.allPeriodsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val activePeriod: StateFlow<TrackingPeriod?> = financeRepository.activePeriodFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val activePeriodTransactions: StateFlow<List<Transaction>> = financeRepository.activePeriodTransactionsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Combined Dashboard state
    val dashboardState: StateFlow<DashboardUiState> = combine(
        combine(userProfile, accounts, categories) { profile, accs, cats ->
            Triple(profile, accs, cats)
        },
        combine(financeRepository.recentTransactionsFlow, activePeriod, activePeriodTransactions) { recents, activeP, periodTxs ->
            Triple(recents, activeP, periodTxs)
        }
    ) { (profile, accList, cats), (recents, activeP, periodTxs) ->
        val totalBal = accList.sumOf { it.balancePaisa }
        var creditPaisa = 0L
        var expensePaisa = 0L
        var creditCount = 0
        var expenseCount = 0

        for (tx in periodTxs) {
            when (tx.type) {
                "CREDIT" -> {
                    creditPaisa += tx.amountPaisa
                    creditCount++
                }
                "EXPENSE" -> {
                    expensePaisa += tx.amountPaisa
                    expenseCount++
                }
            }
        }

        DashboardUiState(
            userProfile = profile,
            accounts = accList,
            totalBalancePaisa = totalBal,
            recentTransactions = recents,
            activePeriod = activeP,
            periodTransactions = periodTxs,
            periodCreditPaisa = creditPaisa,
            periodExpensePaisa = expensePaisa,
            periodCreditCount = creditCount,
            periodExpenseCount = expenseCount,
            categories = cats
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState()
    )

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    init {
        NotificationHelper.createNotificationChannel(application)
        viewModelScope.launch {
            preferencesRepository.setLocked(true)
            financeRepository.ensureDefaultDataLoaded()
        }
    }

    fun clearMessage() {
        _message.value = null
    }

    // Onboarding Setup
    fun completeOnboarding(
        userName: String,
        pin: String,
        ablOpeningPaisa: Long,
        ublOpeningPaisa: Long,
        walletOpeningPaisa: Long
    ) {
        viewModelScope.launch {
            val pinHash = SecurityUtil.hashPin(pin)
            preferencesRepository.completeSetup(userName.trim(), pinHash)
            financeRepository.initializeOpeningBalances(ablOpeningPaisa, ublOpeningPaisa, walletOpeningPaisa)
            _message.value = "Welcome to Pockitify, $userName!"
        }
    }

    // PIN Authentication
    fun verifyPin(pin: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val profile = preferencesRepository.getUserProfile()
            val now = System.currentTimeMillis()
            if (profile.lockoutUntilTimestamp > now) {
                val remainingSec = ((profile.lockoutUntilTimestamp - now) / 1000).coerceAtLeast(1)
                onError("Too many incorrect attempts. Please wait $remainingSec seconds.")
                return@launch
            }

            if (SecurityUtil.verifyPin(pin, profile.pinHash)) {
                preferencesRepository.resetFailedAttempts()
                preferencesRepository.setLocked(false)
                onSuccess()
            } else {
                preferencesRepository.recordFailedAttempt()
                val updatedProfile = preferencesRepository.getUserProfile()
                if (updatedProfile.lockoutUntilTimestamp > now) {
                    onError("Account locked for 30 seconds due to repeated failed attempts.")
                } else {
                    val remaining = (5 - updatedProfile.failedPinAttempts).coerceAtLeast(1)
                    onError("Incorrect PIN. $remaining attempts remaining.")
                }
            }
        }
    }

    fun upgradePin(currentPin: String, newPin: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val profile = preferencesRepository.getUserProfile()
            val now = System.currentTimeMillis()
            if (profile.lockoutUntilTimestamp > now) {
                val remainingSec = ((profile.lockoutUntilTimestamp - now) / 1000).coerceAtLeast(1)
                onError("Too many incorrect attempts. Please wait $remainingSec seconds.")
                return@launch
            }

            if (!SecurityUtil.verifyPin(currentPin, profile.pinHash)) {
                preferencesRepository.recordFailedAttempt()
                onError("Current PIN is incorrect.")
                return@launch
            }

            if (newPin.length != 6) {
                onError("New PIN must be exactly 6 digits.")
                return@launch
            }

            val newHash = SecurityUtil.hashPin(newPin)
            preferencesRepository.updatePin(newHash)
            preferencesRepository.setPinUpgraded(true)
            preferencesRepository.resetFailedAttempts()
            preferencesRepository.setLocked(false)
            _message.value = "PIN upgraded to 6 digits successfully!"
            onSuccess()
        }
    }

    fun logout() {
        viewModelScope.launch {
            preferencesRepository.logout()
            _navigationTarget.value = null
        }
    }

    fun lockApp() {
        viewModelScope.launch {
            preferencesRepository.setLocked(true)
        }
    }

    fun toggleHideBalance() {
        viewModelScope.launch {
            preferencesRepository.toggleBalanceHidden()
        }
    }

    // Transactions
    fun addCredit(
        amountPaisa: Long,
        accountId: String,
        category: String,
        note: String,
        timestamp: Long = System.currentTimeMillis(),
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val res = financeRepository.addCredit(amountPaisa, accountId, category, note, timestamp)
            if (res.isSuccess) {
                val txId = res.getOrNull() ?: System.currentTimeMillis()
                val accName = financeRepository.getAccountDisplayName(accountId)
                val formatted = CurrencyFormatter.formatPaisa(amountPaisa, includePrefix = false, showDecimals = false)
                NotificationHelper.showTransactionNotification(
                    context = getApplication(),
                    notificationId = (txId % Int.MAX_VALUE).toInt(),
                    title = "Credit Added Successfully",
                    message = "Rs. $formatted has been added to your $accName account."
                )
                _message.value = "Credit of ${CurrencyFormatter.formatPaisa(amountPaisa)} added successfully!"
                onSuccess()
            } else {
                _message.value = res.exceptionOrNull()?.message ?: "Failed to add credit"
            }
        }
    }

    fun addExpense(
        amountPaisa: Long,
        accountId: String,
        category: String,
        note: String,
        timestamp: Long = System.currentTimeMillis(),
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val res = financeRepository.addExpense(amountPaisa, accountId, category, note, timestamp)
            if (res.isSuccess) {
                val txId = res.getOrNull() ?: System.currentTimeMillis()
                val accName = financeRepository.getAccountDisplayName(accountId)
                val formatted = CurrencyFormatter.formatPaisa(amountPaisa, includePrefix = false, showDecimals = false)
                NotificationHelper.showTransactionNotification(
                    context = getApplication(),
                    notificationId = (txId % Int.MAX_VALUE).toInt(),
                    title = "Expense Recorded",
                    message = "Rs. $formatted has been deducted from your $accName for $category."
                )
                _message.value = "Expense of ${CurrencyFormatter.formatPaisa(amountPaisa)} saved!"
                onSuccess()
            } else {
                _message.value = res.exceptionOrNull()?.message ?: "Failed to save expense"
            }
        }
    }

    fun addTransfer(
        amountPaisa: Long,
        fromAccountId: String,
        toAccountId: String,
        note: String,
        timestamp: Long = System.currentTimeMillis(),
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val res = financeRepository.addTransfer(amountPaisa, fromAccountId, toAccountId, note, timestamp)
            if (res.isSuccess) {
                val txId = res.getOrNull() ?: System.currentTimeMillis()
                val fromName = financeRepository.getAccountDisplayName(fromAccountId)
                val toName = financeRepository.getAccountDisplayName(toAccountId)
                val formatted = CurrencyFormatter.formatPaisa(amountPaisa, includePrefix = false, showDecimals = false)
                NotificationHelper.showTransactionNotification(
                    context = getApplication(),
                    notificationId = (txId % Int.MAX_VALUE).toInt(),
                    title = "Transfer Successful",
                    message = "Rs. $formatted transferred from $fromName to $toName successfully."
                )
                _message.value = "Transferred ${CurrencyFormatter.formatPaisa(amountPaisa)} successfully!"
                onSuccess()
            } else {
                _message.value = res.exceptionOrNull()?.message ?: "Transfer failed"
            }
        }
    }

    fun deleteTransaction(txId: Long, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            val res = financeRepository.deleteTransaction(txId)
            if (res.isSuccess) {
                _message.value = "Transaction deleted and balances recalculated."
                onDone()
            } else {
                _message.value = res.exceptionOrNull()?.message ?: "Failed to delete transaction"
            }
        }
    }

    fun updateTransaction(
        oldTxId: Long,
        newType: String,
        newAmountPaisa: Long,
        newFromAccountId: String?,
        newToAccountId: String?,
        newCategory: String,
        newNote: String,
        newTimestamp: Long,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val res = financeRepository.updateTransaction(
                oldTxId, newType, newAmountPaisa, newFromAccountId, newToAccountId, newCategory, newNote, newTimestamp
            )
            if (res.isSuccess) {
                _message.value = "Transaction updated and balances synchronized."
                onSuccess()
            } else {
                _message.value = res.exceptionOrNull()?.message ?: "Update failed"
            }
        }
    }

    // Manual Monthly Reset
    fun resetMonth(customName: String? = null, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            val res = financeRepository.manualResetMonth(customName)
            if (res.isSuccess) {
                _message.value = "Month reset successfully! Previous period archived. Balances preserved."
                onDone()
            } else {
                _message.value = res.exceptionOrNull()?.message ?: "Reset failed"
            }
        }
    }

    // Account Balance Manual Adjustment
    fun adjustBalance(accountId: String, newBalancePaisa: Long, note: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val res = financeRepository.adjustAccountBalance(accountId, newBalancePaisa, note)
            if (res.isSuccess) {
                _message.value = "Account balance updated successfully."
                onSuccess()
            } else {
                _message.value = res.exceptionOrNull()?.message ?: "Balance adjustment failed"
            }
        }
    }

    // Category Management
    fun addCategory(name: String, type: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val res = financeRepository.addCategory(name, type)
            if (res.isSuccess) {
                _message.value = "Category '$name' created."
                onSuccess()
            } else {
                _message.value = res.exceptionOrNull()?.message ?: "Failed to add category"
            }
        }
    }

    fun updateCategory(category: Category, onSuccess: () -> Unit) {
        viewModelScope.launch {
            financeRepository.updateCategory(category)
            _message.value = "Category updated."
            onSuccess()
        }
    }

    fun deleteCategory(category: Category, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val res = financeRepository.deleteCategory(category)
            if (res.isSuccess) {
                _message.value = "Category '${category.name}' deleted."
                onSuccess()
            } else {
                _message.value = res.exceptionOrNull()?.message ?: "Cannot delete default category"
            }
        }
    }

    // Profile updates
    fun updateUserName(name: String) {
        viewModelScope.launch {
            if (name.isNotBlank()) {
                preferencesRepository.updateUserName(name.trim())
                _message.value = "User name updated to $name"
            }
        }
    }

    fun changePin(currentPin: String, newPin: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val profile = preferencesRepository.getUserProfile()
            if (!SecurityUtil.verifyPin(currentPin, profile.pinHash)) {
                onError("Current PIN is incorrect.")
                return@launch
            }
            if (newPin.length != 6) {
                onError("New PIN must be exactly 6 digits.")
                return@launch
            }
            val newHash = SecurityUtil.hashPin(newPin)
            preferencesRepository.updatePin(newHash)
            _message.value = "PIN changed successfully."
            onSuccess()
        }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            preferencesRepository.setThemeMode(mode)
        }
    }
}
