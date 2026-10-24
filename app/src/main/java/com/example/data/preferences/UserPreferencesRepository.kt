package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "pockitify_preferences")

data class UserProfileState(
    val isSetupCompleted: Boolean = false,
    val userName: String = "",
    val pinHash: String = "",
    val isLocked: Boolean = true,
    val isBalanceHidden: Boolean = false,
    val failedPinAttempts: Int = 0,
    val lockoutUntilTimestamp: Long = 0L,
    val isPinUpgradedTo6Digits: Boolean = false,
    val themeMode: String = "DARK"
)

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val IS_SETUP_COMPLETED = booleanPreferencesKey("is_setup_completed")
        val USER_NAME = stringPreferencesKey("user_name")
        val PIN_HASH = stringPreferencesKey("pin_hash")
        val IS_LOCKED = booleanPreferencesKey("is_locked")
        val IS_BALANCE_HIDDEN = booleanPreferencesKey("is_balance_hidden")
        val FAILED_PIN_ATTEMPTS = intPreferencesKey("failed_pin_attempts")
        val LOCKOUT_UNTIL_TIMESTAMP = longPreferencesKey("lockout_until_timestamp")
        val IS_PIN_UPGRADED_TO_6_DIGITS = booleanPreferencesKey("is_pin_upgraded_to_6_digits")
        val THEME_MODE = stringPreferencesKey("theme_mode")
    }

    val userProfileFlow: Flow<UserProfileState> = context.dataStore.data.map { preferences ->
        UserProfileState(
            isSetupCompleted = preferences[PreferencesKeys.IS_SETUP_COMPLETED] ?: false,
            userName = preferences[PreferencesKeys.USER_NAME] ?: "",
            pinHash = preferences[PreferencesKeys.PIN_HASH] ?: "",
            isLocked = preferences[PreferencesKeys.IS_LOCKED] ?: true,
            isBalanceHidden = preferences[PreferencesKeys.IS_BALANCE_HIDDEN] ?: false,
            failedPinAttempts = preferences[PreferencesKeys.FAILED_PIN_ATTEMPTS] ?: 0,
            lockoutUntilTimestamp = preferences[PreferencesKeys.LOCKOUT_UNTIL_TIMESTAMP] ?: 0L,
            isPinUpgradedTo6Digits = preferences[PreferencesKeys.IS_PIN_UPGRADED_TO_6_DIGITS] ?: false,
            themeMode = preferences[PreferencesKeys.THEME_MODE] ?: "DARK"
        )
    }

    suspend fun getUserProfile(): UserProfileState = userProfileFlow.first()

    suspend fun completeSetup(userName: String, pinHash: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_SETUP_COMPLETED] = true
            preferences[PreferencesKeys.USER_NAME] = userName
            preferences[PreferencesKeys.PIN_HASH] = pinHash
            preferences[PreferencesKeys.IS_LOCKED] = false
            preferences[PreferencesKeys.FAILED_PIN_ATTEMPTS] = 0
            preferences[PreferencesKeys.LOCKOUT_UNTIL_TIMESTAMP] = 0L
            preferences[PreferencesKeys.IS_PIN_UPGRADED_TO_6_DIGITS] = true
        }
    }

    suspend fun updateUserName(name: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_NAME] = name
        }
    }

    suspend fun updatePin(pinHash: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.PIN_HASH] = pinHash
            preferences[PreferencesKeys.FAILED_PIN_ATTEMPTS] = 0
            preferences[PreferencesKeys.LOCKOUT_UNTIL_TIMESTAMP] = 0L
            preferences[PreferencesKeys.IS_PIN_UPGRADED_TO_6_DIGITS] = true
        }
    }

    suspend fun setPinUpgraded(upgraded: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_PIN_UPGRADED_TO_6_DIGITS] = upgraded
        }
    }

    suspend fun logout() {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_LOCKED] = true
        }
    }

    suspend fun setLocked(locked: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_LOCKED] = locked
        }
    }

    suspend fun toggleBalanceHidden() {
        context.dataStore.edit { preferences ->
            val current = preferences[PreferencesKeys.IS_BALANCE_HIDDEN] ?: false
            preferences[PreferencesKeys.IS_BALANCE_HIDDEN] = !current
        }
    }

    suspend fun recordFailedAttempt() {
        context.dataStore.edit { preferences ->
            val attempts = (preferences[PreferencesKeys.FAILED_PIN_ATTEMPTS] ?: 0) + 1
            preferences[PreferencesKeys.FAILED_PIN_ATTEMPTS] = attempts
            if (attempts >= 5) {
                // Lockout for 30 seconds
                preferences[PreferencesKeys.LOCKOUT_UNTIL_TIMESTAMP] = System.currentTimeMillis() + 30_000L
            }
        }
    }

    suspend fun resetFailedAttempts() {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.FAILED_PIN_ATTEMPTS] = 0
            preferences[PreferencesKeys.LOCKOUT_UNTIL_TIMESTAMP] = 0L
        }
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = mode
        }
    }
}
