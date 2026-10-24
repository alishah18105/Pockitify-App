package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Account
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    @Query("SELECT * FROM accounts ORDER BY CASE WHEN id = 'abl' THEN 1 WHEN id = 'ubl' THEN 2 ELSE 3 END")
    fun getAllAccountsFlow(): Flow<List<Account>>

    @Query("SELECT * FROM accounts")
    suspend fun getAllAccounts(): List<Account>

    @Query("SELECT * FROM accounts WHERE id = :id LIMIT 1")
    suspend fun getAccountById(id: String): Account?

    @Query("SELECT * FROM accounts WHERE id = :id LIMIT 1")
    fun getAccountFlow(id: String): Flow<Account?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccounts(accounts: List<Account>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: Account)

    @Update
    suspend fun updateAccount(account: Account)

    @Query("UPDATE accounts SET balancePaisa = :balancePaisa WHERE id = :id")
    suspend fun updateBalance(id: String, balancePaisa: Long)
}
