package com.talhaak.apps.simpleprayer.data.userprefs

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.batoulapps.adhan2.CalculationMethod
import com.batoulapps.adhan2.HighLatitudeRule
import com.batoulapps.adhan2.Madhab
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

class UserPreferencesRepository(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        val IS_HANAFI = booleanPreferencesKey("is_hanafi")
        val CALC_METHOD = stringPreferencesKey("calculation_method")
        val HIGH_LATITUDE = stringPreferencesKey("high_latitude")
    }

    val userPrefsFlow: Flow<UserPreferences> = dataStore.data.catch { exception ->
        if (exception is IOException) {
            Log.e("UserPreferencesRepo", "Error reading preferences.", exception)
            emit(emptyPreferences())
        }
    }.map { preferences ->
        UserPreferences(
            madhab = when (preferences[IS_HANAFI]) {
                true -> Madhab.HANAFI
                false -> Madhab.SHAFI
                else -> Madhab.SHAFI
            },
            method = CalculationMethod.valueOf(
                preferences[CALC_METHOD] ?: CalculationMethod.MUSLIM_WORLD_LEAGUE.name
            ),
            highLatitudeRule = preferences[HIGH_LATITUDE]?.let {
                HighLatitudeRule.valueOf(it)
            }
        )
    }

    suspend fun updateMadhab(madhab: Madhab) {
        dataStore.edit { preferences ->
            preferences[IS_HANAFI] = madhab == Madhab.HANAFI
        }
    }

    suspend fun updateCalculationMethod(method: CalculationMethod) {
        dataStore.edit { preferences ->
            preferences[CALC_METHOD] = method.name
        }
    }

    suspend fun updateHighLatitudeRule(rule: HighLatitudeRule?) {
        dataStore.edit { preferences ->
            if (rule == null) {
                preferences -= HIGH_LATITUDE
            } else {
                preferences[HIGH_LATITUDE] = rule.name
            }
        }
    }
}