package com.example.data.repository

import androidx.room.withTransaction
import com.example.data.AppDatabase
import com.example.data.model.Account
import com.example.data.model.Category
import com.example.data.model.NotificationItem
import com.example.data.model.TrackingPeriod
import com.example.data.model.Transaction
import com.example.util.CurrencyFormatter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import org.json.JSONArray
import org.json.JSONObject

class FinanceRepository(private val database: AppDatabase) {

    private val accountDao = database.accountDao()
    private val transactionDao = database.transactionDao()
    private val categoryDao = database.categoryDao()
    private val trackingPeriodDao = database.trackingPeriodDao()
    private val notificationDao = database.notificationDao()

    val accountsFlow: Flow<List<Account>> = accountDao.getAllAccountsFlow()
    val allTransactionsFlow: Flow<List<Transaction>> = transactionDao.getAllTransactionsFlow()
    val recentTransactionsFlow: Flow<List<Transaction>> = transactionDao.getRecentTransactionsFlow(10)
    val categoriesFlow: Flow<List<Category>> = categoryDao.getAllCategoriesFlow()
    val activePeriodFlow: Flow<TrackingPeriod?> = trackingPeriodDao.getActivePeriodFlow()
    val allPeriodsFlow: Flow<List<TrackingPeriod>> = trackingPeriodDao.getAllPeriodsFlow()
    val notificationsFlow: Flow<List<NotificationItem>> = notificationDao.getAllNotificationsFlow()
    val unreadNotificationsCountFlow: Flow<Int> = notificationDao.getUnreadCountFlow()

    fun getAccountDisplayName(accountId: String): String = when (accountId.lowercase()) {
        "abl" -> "ABL"
        "ubl" -> "UBL"
        "wallet" -> "Wallet"
        else -> accountId.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }

    suspend fun markNotificationAsRead(id: Long) {
        notificationDao.markAsRead(id)
    }

    suspend fun markAllNotificationsAsRead() {
        notificationDao.markAllAsRead()
    }

    suspend fun deleteNotification(id: Long) {
        notificationDao.deleteNotification(id)
    }

    suspend fun clearAllNotifications() {
        notificationDao.clearAllNotifications()
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val activePeriodTransactionsFlow: Flow<List<Transaction>> = activePeriodFlow.flatMapLatest { period ->
        if (period != null) {
            transactionDao.getTransactionsByPeriodFlow(period.id)
        } else {
            flowOf(emptyList())
        }
    }

    suspend fun ensureDefaultDataLoaded() {
        AppDatabase.populateInitialData(database)
    }

    suspend fun getActivePeriodId(): Long {
        ensureDefaultDataLoaded()
        val active = trackingPeriodDao.getActivePeriod()
        if (active != null) return active.id

        val newPeriod = TrackingPeriod(
            name = CurrencyFormatter.formatMonthYear(),
            startedAt = System.currentTimeMillis(),
            isActive = true
        )
        return trackingPeriodDao.insertPeriod(newPeriod)
    }

    suspend fun initializeOpeningBalances(ablPaisa: Long, ublPaisa: Long, walletPaisa: Long) {
        database.withTransaction {
            ensureDefaultDataLoaded()
            val periodId = getActivePeriodId()

            // Update ABL
            accountDao.updateBalance("abl", ablPaisa)
            if (ablPaisa > 0) {
                transactionDao.insertTransaction(
                    Transaction(
                        type = "CREDIT",
                        amountPaisa = ablPaisa,
                        toAccountId = "abl",
                        category = "Salary",
                        note = "Opening Balance",
                        timestamp = System.currentTimeMillis(),
                        periodId = periodId
                    )
                )
            }

            // Update UBL
            accountDao.updateBalance("ubl", ublPaisa)
            if (ublPaisa > 0) {
                transactionDao.insertTransaction(
                    Transaction(
                        type = "CREDIT",
                        amountPaisa = ublPaisa,
                        toAccountId = "ubl",
                        category = "Salary",
                        note = "Opening Balance",
                        timestamp = System.currentTimeMillis(),
                        periodId = periodId
                    )
                )
            }

            // Update Wallet
            accountDao.updateBalance("wallet", walletPaisa)
            if (walletPaisa > 0) {
                transactionDao.insertTransaction(
                    Transaction(
                        type = "CREDIT",
                        amountPaisa = walletPaisa,
                        toAccountId = "wallet",
                        category = "Salary",
                        note = "Opening Balance",
                        timestamp = System.currentTimeMillis(),
                        periodId = periodId
                    )
                )
            }
        }
    }

    suspend fun addCredit(
        amountPaisa: Long,
        accountId: String,
        category: String,
        note: String,
        timestamp: Long = System.currentTimeMillis()
    ): Result<Long> {
        if (amountPaisa <= 0) return Result.failure(IllegalArgumentException("Amount must be greater than zero"))
        return database.withTransaction {
            val account = accountDao.getAccountById(accountId)
                ?: return@withTransaction Result.failure(IllegalArgumentException("Account not found"))
            val periodId = getActivePeriodId()

            val transaction = Transaction(
                type = "CREDIT",
                amountPaisa = amountPaisa,
                toAccountId = accountId,
                category = category,
                note = note,
                timestamp = timestamp,
                periodId = periodId
            )
            val txId = transactionDao.insertTransaction(transaction)
            accountDao.updateBalance(accountId, account.balancePaisa + amountPaisa)

            // Notification storage
            val accName = getAccountDisplayName(accountId)
            val formattedAmt = CurrencyFormatter.formatPaisa(amountPaisa, includePrefix = false, showDecimals = false)
            notificationDao.insertNotification(
                NotificationItem(
                    transactionId = txId,
                    type = "CREDIT",
                    title = "Credit Added Successfully",
                    message = "Rs. $formattedAmt has been added to your $accName account.",
                    timestamp = timestamp,
                    isRead = false
                )
            )

            Result.success(txId)
        }
    }

    suspend fun addExpense(
        amountPaisa: Long,
        accountId: String,
        category: String,
        note: String,
        timestamp: Long = System.currentTimeMillis()
    ): Result<Long> {
        if (amountPaisa <= 0) return Result.failure(IllegalArgumentException("Amount must be greater than zero"))
        return database.withTransaction {
            val account = accountDao.getAccountById(accountId)
                ?: return@withTransaction Result.failure(IllegalArgumentException("Account not found"))

            val periodId = getActivePeriodId()
            val transaction = Transaction(
                type = "EXPENSE",
                amountPaisa = amountPaisa,
                fromAccountId = accountId,
                category = category,
                note = note,
                timestamp = timestamp,
                periodId = periodId
            )
            val txId = transactionDao.insertTransaction(transaction)
            accountDao.updateBalance(accountId, account.balancePaisa - amountPaisa)

            // Notification storage
            val accName = getAccountDisplayName(accountId)
            val formattedAmt = CurrencyFormatter.formatPaisa(amountPaisa, includePrefix = false, showDecimals = false)
            notificationDao.insertNotification(
                NotificationItem(
                    transactionId = txId,
                    type = "EXPENSE",
                    title = "Expense Recorded",
                    message = "Rs. $formattedAmt has been deducted from your $accName for $category.",
                    timestamp = timestamp,
                    isRead = false
                )
            )

            Result.success(txId)
        }
    }

    suspend fun addTransfer(
        amountPaisa: Long,
        fromAccountId: String,
        toAccountId: String,
        note: String,
        timestamp: Long = System.currentTimeMillis()
    ): Result<Long> {
        if (amountPaisa <= 0) return Result.failure(IllegalArgumentException("Amount must be greater than zero"))
        if (fromAccountId == toAccountId) return Result.failure(IllegalArgumentException("Source and destination accounts must be different"))

        return database.withTransaction {
            val fromAccount = accountDao.getAccountById(fromAccountId)
                ?: return@withTransaction Result.failure(IllegalArgumentException("Source account not found"))
            val toAccount = accountDao.getAccountById(toAccountId)
                ?: return@withTransaction Result.failure(IllegalArgumentException("Destination account not found"))

            val periodId = getActivePeriodId()
            val transaction = Transaction(
                type = "TRANSFER",
                amountPaisa = amountPaisa,
                fromAccountId = fromAccountId,
                toAccountId = toAccountId,
                category = "Transfer",
                note = note,
                timestamp = timestamp,
                periodId = periodId
            )
            val txId = transactionDao.insertTransaction(transaction)

            accountDao.updateBalance(fromAccountId, fromAccount.balancePaisa - amountPaisa)
            accountDao.updateBalance(toAccountId, toAccount.balancePaisa + amountPaisa)

            // Notification storage
            val fromName = getAccountDisplayName(fromAccountId)
            val toName = getAccountDisplayName(toAccountId)
            val formattedAmt = CurrencyFormatter.formatPaisa(amountPaisa, includePrefix = false, showDecimals = false)
            notificationDao.insertNotification(
                NotificationItem(
                    transactionId = txId,
                    type = "TRANSFER",
                    title = "Transfer Successful",
                    message = "Rs. $formattedAmt transferred from $fromName to $toName successfully.",
                    timestamp = timestamp,
                    isRead = false
                )
            )

            Result.success(txId)
        }
    }

    suspend fun deleteTransaction(txId: Long): Result<Unit> {
        return database.withTransaction {
            val tx = transactionDao.getTransactionById(txId)
                ?: return@withTransaction Result.failure(IllegalArgumentException("Transaction not found"))

            // Atomically revert balance changes
            when (tx.type) {
                "CREDIT" -> {
                    tx.toAccountId?.let { accId ->
                        val acc = accountDao.getAccountById(accId)
                        if (acc != null) {
                            accountDao.updateBalance(accId, acc.balancePaisa - tx.amountPaisa)
                        }
                    }
                }
                "EXPENSE" -> {
                    tx.fromAccountId?.let { accId ->
                        val acc = accountDao.getAccountById(accId)
                        if (acc != null) {
                            accountDao.updateBalance(accId, acc.balancePaisa + tx.amountPaisa)
                        }
                    }
                }
                "TRANSFER" -> {
                    tx.fromAccountId?.let { fromId ->
                        val fromAcc = accountDao.getAccountById(fromId)
                        if (fromAcc != null) {
                            accountDao.updateBalance(fromId, fromAcc.balancePaisa + tx.amountPaisa)
                        }
                    }
                    tx.toAccountId?.let { toId ->
                        val toAcc = accountDao.getAccountById(toId)
                        if (toAcc != null) {
                            accountDao.updateBalance(toId, toAcc.balancePaisa - tx.amountPaisa)
                        }
                    }
                }
            }
            transactionDao.deleteTransaction(tx)
            Result.success(Unit)
        }
    }

    suspend fun updateTransaction(
        oldTxId: Long,
        newType: String,
        newAmountPaisa: Long,
        newFromAccountId: String?,
        newToAccountId: String?,
        newCategory: String,
        newNote: String,
        newTimestamp: Long
    ): Result<Unit> {
        return database.withTransaction {
            val oldTx = transactionDao.getTransactionById(oldTxId)
                ?: return@withTransaction Result.failure(IllegalArgumentException("Transaction not found"))

            // 1. Revert old balance effect
            when (oldTx.type) {
                "CREDIT" -> {
                    oldTx.toAccountId?.let { accId ->
                        val acc = accountDao.getAccountById(accId)
                        if (acc != null) accountDao.updateBalance(accId, acc.balancePaisa - oldTx.amountPaisa)
                    }
                }
                "EXPENSE" -> {
                    oldTx.fromAccountId?.let { accId ->
                        val acc = accountDao.getAccountById(accId)
                        if (acc != null) accountDao.updateBalance(accId, acc.balancePaisa + oldTx.amountPaisa)
                    }
                }
                "TRANSFER" -> {
                    oldTx.fromAccountId?.let { fromId ->
                        val fromAcc = accountDao.getAccountById(fromId)
                        if (fromAcc != null) accountDao.updateBalance(fromId, fromAcc.balancePaisa + oldTx.amountPaisa)
                    }
                    oldTx.toAccountId?.let { toId ->
                        val toAcc = accountDao.getAccountById(toId)
                        if (toAcc != null) accountDao.updateBalance(toId, toAcc.balancePaisa - oldTx.amountPaisa)
                    }
                }
            }

            // 2. Apply new balance effect
            when (newType) {
                "CREDIT" -> {
                    newToAccountId?.let { accId ->
                        val acc = accountDao.getAccountById(accId)
                        if (acc != null) accountDao.updateBalance(accId, acc.balancePaisa + newAmountPaisa)
                    }
                }
                "EXPENSE" -> {
                    newFromAccountId?.let { accId ->
                        val acc = accountDao.getAccountById(accId)
                        if (acc != null) accountDao.updateBalance(accId, acc.balancePaisa - newAmountPaisa)
                    }
                }
                "TRANSFER" -> {
                    newFromAccountId?.let { fromId ->
                        val fromAcc = accountDao.getAccountById(fromId)
                        if (fromAcc != null) accountDao.updateBalance(fromId, fromAcc.balancePaisa - newAmountPaisa)
                    }
                    newToAccountId?.let { toId ->
                        val toAcc = accountDao.getAccountById(toId)
                        if (toAcc != null) accountDao.updateBalance(toId, toAcc.balancePaisa + newAmountPaisa)
                    }
                }
            }

            // 3. Update transaction record
            val updated = oldTx.copy(
                type = newType,
                amountPaisa = newAmountPaisa,
                fromAccountId = newFromAccountId,
                toAccountId = newToAccountId,
                category = newCategory,
                note = newNote,
                timestamp = newTimestamp
            )
            transactionDao.updateTransaction(updated)
            Result.success(Unit)
        }
    }

    suspend fun manualResetMonth(customPeriodName: String? = null): Result<TrackingPeriod> {
        return database.withTransaction {
            val active = trackingPeriodDao.getActivePeriod()
            if (active != null) {
                val transactions = transactionDao.getTransactionsByPeriod(active.id)
                var totalCredit = 0L
                var totalExpense = 0L
                for (tx in transactions) {
                    if (tx.type == "CREDIT") totalCredit += tx.amountPaisa
                    else if (tx.type == "EXPENSE") totalExpense += tx.amountPaisa
                }

                // Close active period
                val closedPeriod = active.copy(
                    isActive = false,
                    closedAt = System.currentTimeMillis(),
                    archivedCreditPaisa = totalCredit,
                    archivedExpensePaisa = totalExpense
                )
                trackingPeriodDao.updatePeriod(closedPeriod)
            }

            // Start fresh tracking period. Note: Account balances stay 100% unchanged!
            val newName = customPeriodName?.takeIf { it.isNotBlank() } ?: CurrencyFormatter.formatMonthYear()
            val newPeriod = TrackingPeriod(
                name = newName,
                startedAt = System.currentTimeMillis(),
                isActive = true
            )
            val newId = trackingPeriodDao.insertPeriod(newPeriod)
            val created = trackingPeriodDao.getPeriodById(newId) ?: newPeriod
            Result.success(created)
        }
    }

    suspend fun adjustAccountBalance(accountId: String, newBalancePaisa: Long, reason: String): Result<Unit> {
        return database.withTransaction {
            val account = accountDao.getAccountById(accountId)
                ?: return@withTransaction Result.failure(IllegalArgumentException("Account not found"))

            val diff = newBalancePaisa - account.balancePaisa
            accountDao.updateBalance(accountId, newBalancePaisa)

            if (diff != 0L) {
                val periodId = getActivePeriodId()
                val isIncrease = diff > 0
                val absDiff = kotlin.math.abs(diff)
                val tx = Transaction(
                    type = if (isIncrease) "CREDIT" else "EXPENSE",
                    amountPaisa = absDiff,
                    fromAccountId = if (!isIncrease) accountId else null,
                    toAccountId = if (isIncrease) accountId else null,
                    category = "Balance Adjustment",
                    note = reason.ifBlank { "Manual Balance Adjustment" },
                    timestamp = System.currentTimeMillis(),
                    periodId = periodId
                )
                val txId = transactionDao.insertTransaction(tx)

                val accName = getAccountDisplayName(accountId)
                val formattedDiff = CurrencyFormatter.formatPaisa(absDiff, includePrefix = false, showDecimals = false)
                notificationDao.insertNotification(
                    NotificationItem(
                        transactionId = txId,
                        type = if (isIncrease) "CREDIT" else "EXPENSE",
                        title = if (isIncrease) "Credit Added Successfully" else "Expense Recorded",
                        message = if (isIncrease) {
                            "Rs. $formattedDiff has been added to your $accName account."
                        } else {
                            "Rs. $formattedDiff has been deducted from your $accName for Balance Adjustment."
                        },
                        timestamp = System.currentTimeMillis(),
                        isRead = false
                    )
                )
            }
            Result.success(Unit)
        }
    }

    suspend fun addCategory(name: String, type: String): Result<Long> {
        val trimmed = name.trim()
        if (trimmed.isBlank()) return Result.failure(IllegalArgumentException("Category name cannot be empty"))
        val existing = categoryDao.findCategoryByNameAndType(trimmed, type)
        if (existing != null) {
            return Result.success(existing.id)
        }
        val category = Category(name = trimmed, type = type, iconKey = "category", isDefault = false)
        val id = categoryDao.insertCategory(category)
        return Result.success(id)
    }

    suspend fun updateCategory(category: Category) {
        categoryDao.updateCategory(category)
    }

    suspend fun deleteCategory(category: Category): Result<Unit> {
        if (category.isDefault) {
            return Result.failure(IllegalStateException("Default categories cannot be deleted"))
        }
        categoryDao.deleteCategory(category)
        return Result.success(Unit)
    }

    suspend fun getTransactionsForPeriod(periodId: Long): List<Transaction> {
        return transactionDao.getTransactionsByPeriod(periodId)
    }

    suspend fun exportTransactionsCsv(): String {
        val txs = transactionDao.getAllTransactions()
        val accounts = accountDao.getAllAccounts().associateBy { it.id }
        val sb = StringBuilder()
        sb.append("ID,Date,Time,Type,Amount (PKR),Account / From,To Account,Category,Notes\n")
        for (tx in txs) {
            val date = CurrencyFormatter.formatDate(tx.timestamp)
            val time = CurrencyFormatter.formatDateTime(tx.timestamp).substringAfter(", ")
            val amount = tx.amountPaisa / 100.0
            val fromName = tx.fromAccountId?.let { accounts[it]?.name ?: it } ?: ""
            val toName = tx.toAccountId?.let { accounts[it]?.name ?: it } ?: ""
            sb.append("\"${tx.id}\",\"$date\",\"$time\",\"${tx.type}\",\"$amount\",\"$fromName\",\"$toName\",\"${tx.category}\",\"${tx.note.replace("\"", "\"\"")}\"\n")
        }
        return sb.toString()
    }

    suspend fun exportJsonBackup(): String {
        val root = JSONObject()
        val accountsArray = JSONArray()
        for (acc in accountDao.getAllAccounts()) {
            val obj = JSONObject()
            obj.put("id", acc.id)
            obj.put("name", acc.name)
            obj.put("fullName", acc.fullName)
            obj.put("accountNumberMasked", acc.accountNumberMasked)
            obj.put("balancePaisa", acc.balancePaisa)
            obj.put("accountType", acc.accountType)
            obj.put("colorHex", acc.colorHex)
            accountsArray.put(obj)
        }
        root.put("accounts", accountsArray)

        val txArray = JSONArray()
        for (tx in transactionDao.getAllTransactions()) {
            val obj = JSONObject()
            obj.put("id", tx.id)
            obj.put("type", tx.type)
            obj.put("amountPaisa", tx.amountPaisa)
            obj.put("fromAccountId", tx.fromAccountId ?: JSONObject.NULL)
            obj.put("toAccountId", tx.toAccountId ?: JSONObject.NULL)
            obj.put("category", tx.category)
            obj.put("note", tx.note)
            obj.put("timestamp", tx.timestamp)
            obj.put("periodId", tx.periodId)
            txArray.put(obj)
        }
        root.put("transactions", txArray)

        val periodsArray = JSONArray()
        for (p in trackingPeriodDao.getAllPeriods()) {
            val obj = JSONObject()
            obj.put("id", p.id)
            obj.put("name", p.name)
            obj.put("startedAt", p.startedAt)
            obj.put("closedAt", p.closedAt ?: JSONObject.NULL)
            obj.put("isActive", p.isActive)
            obj.put("archivedCreditPaisa", p.archivedCreditPaisa)
            obj.put("archivedExpensePaisa", p.archivedExpensePaisa)
            periodsArray.put(obj)
        }
        root.put("periods", periodsArray)

        val catArray = JSONArray()
        for (c in categoryDao.getAllCategories()) {
            val obj = JSONObject()
            obj.put("id", c.id)
            obj.put("name", c.name)
            obj.put("type", c.type)
            obj.put("iconKey", c.iconKey)
            obj.put("isDefault", c.isDefault)
            catArray.put(obj)
        }
        root.put("categories", catArray)

        return root.toString(2)
    }

    suspend fun restoreJsonBackup(jsonString: String): Result<Unit> {
        return try {
            val root = JSONObject(jsonString)
            database.withTransaction {
                if (root.has("accounts")) {
                    val accs = root.getJSONArray("accounts")
                    for (i in 0 until accs.length()) {
                        val obj = accs.getJSONObject(i)
                        accountDao.updateBalance(obj.getString("id"), obj.getLong("balancePaisa"))
                    }
                }
                if (root.has("categories")) {
                    val cats = root.getJSONArray("categories")
                    for (i in 0 until cats.length()) {
                        val obj = cats.getJSONObject(i)
                        val cat = Category(
                            name = obj.getString("name"),
                            type = obj.getString("type"),
                            iconKey = obj.optString("iconKey", "category"),
                            isDefault = obj.optBoolean("isDefault", false)
                        )
                        categoryDao.insertCategory(cat)
                    }
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
