package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "legal_guardian_preferences")

class PreferenceManager(private val context: Context) {

    companion object {
        private val IS_PRO_KEY = booleanPreferencesKey("is_pro")
        private val DEFAULT_COUNTRY_KEY = stringPreferencesKey("default_country")
        private val BIOMETRIC_LOCK_KEY = booleanPreferencesKey("biometric_lock")
        private val DEFAULT_CONTRACT_TYPE_KEY = stringPreferencesKey("default_contract_type")
        private val LAST_SCAN_DATE_KEY = stringPreferencesKey("last_scan_date")
        private val SCANS_USED_TODAY_KEY = intPreferencesKey("scans_used_today")
        const val MAX_FREE_DAILY_SCANS = 3
    }

    val isPro: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[IS_PRO_KEY] ?: false
    }

    val defaultCountry: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[DEFAULT_COUNTRY_KEY] ?: "US"
    }

    val biometricLock: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[BIOMETRIC_LOCK_KEY] ?: false
    }

    val defaultContractType: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[DEFAULT_CONTRACT_TYPE_KEY] ?: "lease"
    }

    val freeScansRemaining: Flow<Int> = context.dataStore.data.map { preferences ->
        val lastDate = preferences[LAST_SCAN_DATE_KEY] ?: ""
        val today = LocalDate.now().toString()
        if (lastDate != today) {
            MAX_FREE_DAILY_SCANS
        } else {
            val used = preferences[SCANS_USED_TODAY_KEY] ?: 0
            (MAX_FREE_DAILY_SCANS - used).coerceAtLeast(0)
        }
    }

    suspend fun tryConsumeFreeScan(): Boolean {
        val today = LocalDate.now().toString()
        val preferences = context.dataStore.data.first()
        val lastDate = preferences[LAST_SCAN_DATE_KEY] ?: ""
        var used = preferences[SCANS_USED_TODAY_KEY] ?: 0

        if (lastDate != today) {
            used = 0
        }

        if (used < MAX_FREE_DAILY_SCANS) {
            context.dataStore.edit { mutablePrefs ->
                mutablePrefs[LAST_SCAN_DATE_KEY] = today
                mutablePrefs[SCANS_USED_TODAY_KEY] = used + 1
            }
            return true
        }
        return false
    }

    suspend fun setProStatus(isPro: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_PRO_KEY] = isPro
        }
    }

    suspend fun setDefaultCountry(country: String) {
        context.dataStore.edit { preferences ->
            preferences[DEFAULT_COUNTRY_KEY] = country
        }
    }

    suspend fun setBiometricLock(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[BIOMETRIC_LOCK_KEY] = enabled
        }
    }

    suspend fun setDefaultContractType(contractType: String) {
        context.dataStore.edit { preferences ->
            preferences[DEFAULT_CONTRACT_TYPE_KEY] = contractType
        }
    }
}
