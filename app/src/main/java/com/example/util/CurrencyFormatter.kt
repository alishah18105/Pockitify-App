package com.example.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CurrencyFormatter {
    private val formatter = DecimalFormat("#,##0.00", DecimalFormatSymbols(Locale.US))
    private val simpleFormatter = DecimalFormat("#,##0", DecimalFormatSymbols(Locale.US))

    /**
     * Formats paisa (minor unit: 1 PKR = 100 paisa) to formatted PKR string.
     * Example: 45000000L -> "PKR 450,000.00"
     */
    fun formatPaisa(paisa: Long, includePrefix: Boolean = true, showDecimals: Boolean = true): String {
        val amount = paisa / 100.0
        val formatted = if (showDecimals) formatter.format(amount) else simpleFormatter.format(amount)
        return if (includePrefix) "PKR $formatted" else formatted
    }

    /**
     * Parses user entered PKR string into paisa Long.
     * Example: "450" -> 45000L
     */
    fun parseToPaisa(input: String): Long {
        val sanitized = input.replace(",", "").trim()
        val value = sanitized.toDoubleOrNull() ?: 0.0
        return (value * 100).toLong()
    }

    /**
     * Formats date timestamp into readable string
     * Example: "17 Oct 2026, 10:30 AM"
     */
    fun formatDateTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    /**
     * Formats date timestamp into date string
     * Example: "17 Oct 2026"
     */
    fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    /**
     * Formats current month year
     * Example: "October 2026"
     */
    fun formatMonthYear(timestamp: Long = System.currentTimeMillis()): String {
        val sdf = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}
