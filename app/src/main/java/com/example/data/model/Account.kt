package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class Account(
    @PrimaryKey
    val id: String, // "abl", "ubl", "wallet"
    val name: String,
    val fullName: String,
    val accountNumberMasked: String,
    val balancePaisa: Long = 0L,
    val accountType: String = "BANK", // "BANK" or "WALLET"
    val colorHex: String = "#0C2B4E"
)
