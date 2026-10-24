package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import com.example.data.repository.FinanceRepository
import com.example.util.CurrencyFormatter
import com.example.util.SecurityUtil
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class FinanceCalculationTest {

    private lateinit var db: AppDatabase
    private lateinit var repo: FinanceRepository

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repo = FinanceRepository(db)
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun testCurrencyFormatting() {
        // 450,000 PKR = 45,000,000 paisa
        val paisa = 45000000L
        val formatted = CurrencyFormatter.formatPaisa(paisa)
        assertEquals("PKR 450,000.00", formatted)

        val parsed = CurrencyFormatter.parseToPaisa("450000")
        assertEquals(45000000L, parsed)
    }

    @Test
    fun testPinSecurityHashing() {
        val pin = "123456"
        val hash = SecurityUtil.hashPin(pin)
        assertTrue(SecurityUtil.verifyPin("123456", hash))
        assertFalse(SecurityUtil.verifyPin("654321", hash))
        assertFalse(SecurityUtil.verifyPin("12345", hash))
    }

    @Test
    fun testOpeningBalancesAndTotalBalance() = runBlocking {
        // ABL: 450,000 PKR, UBL: 280,500 PKR, Wallet: 5,000 PKR
        val ablPaisa = 45000000L
        val ublPaisa = 28050000L
        val walletPaisa = 500000L

        repo.initializeOpeningBalances(ablPaisa, ublPaisa, walletPaisa)

        val accounts = repo.accountsFlow.first()
        val abl = accounts.first { it.id == "abl" }
        val ubl = accounts.first { it.id == "ubl" }
        val wallet = accounts.first { it.id == "wallet" }

        assertEquals(ablPaisa, abl.balancePaisa)
        assertEquals(ublPaisa, ubl.balancePaisa)
        assertEquals(walletPaisa, wallet.balancePaisa)

        val total = accounts.sumOf { it.balancePaisa }
        assertEquals(73550000L, total)
    }

    @Test
    fun testTransferConsistency() = runBlocking {
        repo.initializeOpeningBalances(1000000L, 500000L, 100000L) // 10k, 5k, 1k
        val initialTotal = repo.accountsFlow.first().sumOf { it.balancePaisa }

        // Transfer 2,000 PKR (200,000 paisa) from ABL to Wallet
        val res = repo.addTransfer(200000L, "abl", "wallet", "ATM withdrawal")
        assertTrue(res.isSuccess)

        val accounts = repo.accountsFlow.first()
        val abl = accounts.first { it.id == "abl" }
        val wallet = accounts.first { it.id == "wallet" }

        assertEquals(800000L, abl.balancePaisa)
        assertEquals(300000L, wallet.balancePaisa)

        // Total balance must be exactly preserved!
        val newTotal = accounts.sumOf { it.balancePaisa }
        assertEquals(initialTotal, newTotal)
    }

    @Test
    fun testTransactionDeletionRollback() = runBlocking {
        repo.initializeOpeningBalances(1000000L, 0L, 0L) // ABL has 10,000 PKR

        // Add expense of 1,500 PKR from ABL
        val txId = repo.addExpense(150000L, "abl", "Grocery", "Weekly groceries").getOrThrow()
        var abl = repo.accountsFlow.first().first { it.id == "abl" }
        assertEquals(850000L, abl.balancePaisa)

        // Delete the transaction -> balance must be restored to 10,000 PKR
        val delRes = repo.deleteTransaction(txId)
        assertTrue(delRes.isSuccess)

        abl = repo.accountsFlow.first().first { it.id == "abl" }
        assertEquals(1000000L, abl.balancePaisa)
    }

    @Test
    fun testManualMonthlyResetPreservesBalances() = runBlocking {
        repo.initializeOpeningBalances(5000000L, 3000000L, 200000L) // 50k, 30k, 2k
        val initialAccounts = repo.accountsFlow.first()

        // Record some expenses and credits
        repo.addCredit(1000000L, "abl", "Salary", "Bonus")
        repo.addExpense(500000L, "wallet", "Grocery", "Pharmacy")

        val accountsBeforeReset = repo.accountsFlow.first()

        // Perform manual month reset
        val resetRes = repo.manualResetMonth("Next Month Cycle")
        assertTrue(resetRes.isSuccess)

        // Balances must be 100% identical before and after reset!
        val accountsAfterReset = repo.accountsFlow.first()
        for (i in accountsBeforeReset.indices) {
            assertEquals(accountsBeforeReset[i].id, accountsAfterReset[i].id)
            assertEquals(accountsBeforeReset[i].balancePaisa, accountsAfterReset[i].balancePaisa)
        }

        // Active period transactions should now be 0 for the fresh period
        val activePeriodTxs = repo.activePeriodTransactionsFlow.first()
        assertEquals(0, activePeriodTxs.size)

        // But all transactions must still exist in historical database!
        val allTxs = repo.allTransactionsFlow.first()
        assertTrue(allTxs.size >= 4) // 3 opening + 2 actions
    }

    @Test
    fun testCategoryDeduplication() = runBlocking {
        // Seed default categories
        repo.ensureDefaultDataLoaded()

        val initialCats = repo.categoriesFlow.first()
        val salaryCount = initialCats.count { it.name.equals("Salary", ignoreCase = true) }
        assertEquals(1, salaryCount)

        // Adding duplicate "Salary" should not add a new row
        val addRes = repo.addCategory("salary", "CREDIT")
        assertTrue(addRes.isSuccess)

        val updatedCats = repo.categoriesFlow.first()
        val newSalaryCount = updatedCats.count { it.name.equals("Salary", ignoreCase = true) }
        assertEquals(1, newSalaryCount)

        // Adding a new unique custom category should succeed
        val newCatRes = repo.addCategory("Freelance", "CREDIT")
        assertTrue(newCatRes.isSuccess)
        val withFreelance = repo.categoriesFlow.first()
        assertTrue(withFreelance.any { it.name == "Freelance" })

        // Verify expense categories only keep Utility Bills, Mobile Package, Grocery, Other (no Cat Food, Medicines, Monthly Expenditure, Birds, Bird Food)
        val expenseCats = withFreelance.filter { it.type == "EXPENSE" }
        assertFalse(expenseCats.any { it.name.equals("Cat Food", ignoreCase = true) })
        assertFalse(expenseCats.any { it.name.equals("Medicines", ignoreCase = true) })
        assertFalse(expenseCats.any { it.name.equals("Medicine", ignoreCase = true) })
        assertFalse(expenseCats.any { it.name.equals("Monthly Expenditure", ignoreCase = true) })
        assertFalse(expenseCats.any { it.name.equals("Bird Food", ignoreCase = true) })
        assertFalse(expenseCats.any { it.name.equals("Birds", ignoreCase = true) })
        assertTrue(expenseCats.any { it.name.equals("Utility Bills", ignoreCase = true) })
        assertTrue(expenseCats.any { it.name.equals("Mobile Package", ignoreCase = true) })
        assertTrue(expenseCats.any { it.name.equals("Grocery", ignoreCase = true) })
        assertTrue(expenseCats.any { it.name.equals("Other", ignoreCase = true) })
    }

    @Test
    fun testPdfSummaryCalculationExcludesTransfers() = runBlocking {
        repo.initializeOpeningBalances(0L, 0L, 0L)

        // Credit 50,000 PKR (5,000,000 paisa)
        repo.addCredit(5000000L, "abl", "Salary", "Monthly Salary")
        // Expense 10,000 PKR (1,000,000 paisa)
        repo.addExpense(1000000L, "abl", "Grocery", "Weekly Grocery")
        // Transfer 5,000 PKR (500,000 paisa) - Transfers MUST NOT affect income/expense summary
        repo.addTransfer(500000L, "abl", "wallet", "Pocket cash")

        val transactions = repo.allTransactionsFlow.first()

        val totalCredits = transactions.filter { it.type == "CREDIT" }.sumOf { it.amountPaisa }
        val totalExpenses = transactions.filter { it.type == "EXPENSE" }.sumOf { it.amountPaisa }
        val netCashFlow = totalCredits - totalExpenses

        assertEquals(5000000L, totalCredits)
        assertEquals(1000000L, totalExpenses)
        assertEquals(4000000L, netCashFlow)
        assertEquals(3, transactions.size) // 1 credit + 1 expense + 1 transfer
    }
}

