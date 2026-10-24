package com.example.util

import java.security.MessageDigest

object SecurityUtil {
    private const val SALT = "PockitifySecureOfflineFintechSalt2026"

    fun hashPin(pin: String): String {
        val input = "$pin:$SALT"
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun verifyPin(pin: String, storedHash: String): Boolean {
        if (storedHash.isBlank() || (pin.length != 4 && pin.length != 6)) return false
        val computed = hashPin(pin)
        return computed.equals(storedHash, ignoreCase = true)
    }
}
