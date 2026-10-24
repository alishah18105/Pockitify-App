package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val type: String, // "CREDIT", "EXPENSE", "TRANSFER"
    val amountPaisa: Long,
    val fromAccountId: String? = null, // Set for EXPENSE and TRANSFER
    val toAccountId: String? = null,   // Set for CREDIT and TRANSFER
    val category: String,
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val periodId: Long = 1L
)
