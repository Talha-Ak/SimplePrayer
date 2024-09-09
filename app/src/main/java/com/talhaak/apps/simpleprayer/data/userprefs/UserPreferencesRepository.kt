package com.talhaak.apps.simpleprayer.data.userprefs

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.batoulapps.adhan2.CalculationMethod
import com.batoulapps.adhan2.HighLatitudeRule
import com.batoulapps.adhan2.Madhab
import com.batoulapps.adhan2.Prayer
import com.batoulapps.adhan2.PrayerAdjustments
import com.talhaak.apps.simpleprayer.data.RemoteSurfaceUpdater
import com.talhaak.apps.simpleprayer.data.prayer.get
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException

private const val USER_PREFS_DATASTORE_NAME = "user_prefs_datastore"
val Context.userPrefsDatastore by preferencesDataStore(
    name = USER_PREFS_DATASTORE_NAME
)

class UserPreferencesRepository(
    private val dataStore: DataStore<Preferences>,
    private val remoteSurfaceUpdater: RemoteSurfaceUpdater
) {
    companion object {
        val IS_HANAFI = booleanPreferencesKey("is_hanafi")
        val CALC_METHOD = stringPreferencesKey("calculation_method")
        val HIGH_LATITUDE = stringPreferencesKey("high_latitude")
        val CUSTOM_ANGLE_FAJR = doublePreferencesKey("custom_angle_fajr")
        val CUSTOM_ANGLE_ISHA = doublePreferencesKey("custom_angle_isha")
        val FAJR_OFFSET = intPreferencesKey("fajr_offset")
        val SUNRISE_OFFSET = intPreferencesKey("sunrise_offset")
        val DHUHR_OFFSET = intPreferencesKey("dhuhr_offset")
        val ASR_OFFSET = intPreferencesKey("asr_offset")
        val MAGHRIB_OFFSET = intPreferencesKey("maghrib_offset")
        val ISHA_OFFSET = intPreferencesKey("isha_offset")
    }

    val userPrefsFlow: Flow<UserPreferences> = dataStore.data.catch { exception ->
        if (exception is IOException) {
            Log.e("UserPreferencesRepo", "Error reading preferences.", exception)
            emit(emptyPreferences())
        }
    }.map { preferences ->
        val madhab = when (preferences[IS_HANAFI]) {
            true -> Madhab.HANAFI
            false -> Madhab.SHAFI
            else -> Madhab.SHAFI
        }
        val method = CalculationMethod.valueOf(
            preferences[CALC_METHOD] ?: CalculationMethod.MUSLIM_WORLD_LEAGUE.name
        )
        val highLatitudeRule = preferences[HIGH_LATITUDE]?.let {
            HighLatitudeRule.valueOf(it)
        }
        val customAngles = Pair(
            preferences[CUSTOM_ANGLE_FAJR] ?: method.parameters.fajrAngle,
            preferences[CUSTOM_ANGLE_ISHA] ?: method.parameters.ishaAngle
        )
        val prayerAdjustments = with(method.parameters.methodAdjustments) {
            PrayerAdjustments(
                fajr = preferences[FAJR_OFFSET] ?: fajr,
                sunrise = preferences[SUNRISE_OFFSET] ?: sunrise,
                dhuhr = preferences[DHUHR_OFFSET] ?: dhuhr,
                asr = preferences[ASR_OFFSET] ?: asr,
                maghrib = preferences[MAGHRIB_OFFSET] ?: maghrib,
                isha = preferences[ISHA_OFFSET] ?: isha
            )
        }

        UserPreferences(madhab, method, highLatitudeRule, customAngles, prayerAdjustments)
    }

    suspend fun updateMadhab(madhab: Madhab) {
        dataStore.edit { preferences ->
            preferences[IS_HANAFI] = madhab == Madhab.HANAFI
        }
        refreshTile()
    }

    suspend fun updateCalculationMethod(method: CalculationMethod) {
        dataStore.edit { preferences ->
            preferences[CALC_METHOD] = method.name
        }
        refreshTile()
    }

    suspend fun updateHighLatitudeRule(rule: HighLatitudeRule?) {
        dataStore.edit { preferences ->
            if (rule == null) {
                preferences.remove(HIGH_LATITUDE)
            } else {
                preferences[HIGH_LATITUDE] = rule.name
            }
        }
        refreshTile()
    }

    suspend fun updateCustomAngles(fajrAngle: Double, ishaAngle: Double) {
        dataStore.edit { preferences ->
            if (fajrAngle == userPrefsFlow.first().method.parameters.fajrAngle) {
                preferences.remove(CUSTOM_ANGLE_FAJR)
            } else {
                preferences[CUSTOM_ANGLE_FAJR] = fajrAngle
            }

            if (ishaAngle == userPrefsFlow.first().method.parameters.ishaAngle) {
                preferences.remove(CUSTOM_ANGLE_ISHA)
            } else {
                preferences[CUSTOM_ANGLE_ISHA] = ishaAngle
            }
        }
        refreshTile()
    }

    suspend fun updatePrayerOffset(prayer: Prayer, offset: Int) {
        dataStore.edit { preferences ->
            val key = when (prayer) {
                Prayer.NONE -> throw IllegalArgumentException("Invalid prayer")
                Prayer.FAJR -> FAJR_OFFSET
                Prayer.SUNRISE -> SUNRISE_OFFSET
                Prayer.DHUHR -> DHUHR_OFFSET
                Prayer.ASR -> ASR_OFFSET
                Prayer.MAGHRIB -> MAGHRIB_OFFSET
                Prayer.ISHA -> ISHA_OFFSET
            }

            if (offset == userPrefsFlow.first().method.parameters.prayerAdjustments[prayer]) {
                preferences.remove(key)
            } else {
                preferences[key] = offset
            }
        }
        refreshTile()
    }

    private fun refreshTile() {
        remoteSurfaceUpdater.updateRemoteSurfaces()
    }
}