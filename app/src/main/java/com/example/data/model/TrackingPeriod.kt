package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tracking_periods")
data class TrackingPeriod(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String, // e.g., "October 2026 (Period 1)" or custom name
    val startedAt: Long = System.currentTimeMillis(),
    val closedAt: Long? = null,
    val isActive: Boolean = true,
    val archivedCreditPaisa: Long = 0L,
    val archivedExpensePaisa: Long = 0L
)
