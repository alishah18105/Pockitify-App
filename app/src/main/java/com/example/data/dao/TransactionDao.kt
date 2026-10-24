package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC, id DESC")
    fun getAllTransactionsFlow(): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC, id DESC LIMIT :limit")
    fun getRecentTransactionsFlow(limit: Int): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE periodId = :periodId ORDER BY timestamp DESC, id DESC")
    fun getTransactionsByPeriodFlow(periodId: Long): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE periodId = :periodId")
    suspend fun getTransactionsByPeriod(periodId: Long): List<Transaction>

    @Query("SELECT * FROM transactions WHERE fromAccountId = :accountId OR toAccountId = :accountId ORDER BY timestamp DESC, id DESC")
    fun getTransactionsByAccountFlow(accountId: String): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun getTransactionById(id: Long): Transaction?

    @Query("SELECT * FROM transactions")
    suspend fun getAllTransactions(): List<Transaction>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction): Long

    @Update
    suspend fun updateTransaction(transaction: Transaction)

    @Delete
    suspend fun deleteTransaction(transaction: Transaction)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransactionById(id: Long)
}
