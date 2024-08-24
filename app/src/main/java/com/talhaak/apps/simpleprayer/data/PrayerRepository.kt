package com.talhaak.apps.simpleprayer.data

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.batoulapps.adhan2.CalculationMethod
import com.batoulapps.adhan2.Coordinates
import com.batoulapps.adhan2.Madhab
import com.batoulapps.adhan2.PrayerTimes
import com.batoulapps.adhan2.data.DateComponents
import com.google.android.gms.tasks.CancellationToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant

class PrayerRepository(
    private val locationLocalDataSource: DataStore<Preferences>,
    private val locationRemoteDataSource: LocationRemoteDataSource
) {
    private companion object {
        val LOCATION_LATITUDE = doublePreferencesKey("location_Lat")
        val LOCATION_LONGITUDE = doublePreferencesKey("location_long")
        val LOCATION_AREA = stringPreferencesKey("location_area")
        val LAST_UPDATED = longPreferencesKey("last_updated")
    }

    val lastLocationFlow: Flow<DeviceLocation> = locationLocalDataSource.data
        .catch { exception ->
            if (exception is IOException) {
                Log.e("PrayerRepository", "Error reading preferences", exception)
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preferences ->
            DeviceLocation(
                preferences[LOCATION_LATITUDE]!!,
                preferences[LOCATION_LONGITUDE]!!,
                preferences[LOCATION_AREA].orEmpty(),
                Instant.fromEpochMilliseconds(preferences[LAST_UPDATED]!!)
            )
        }

    suspend fun updateLocation(
        cancellationToken: CancellationToken,
        onCompletion: (Boolean) -> Unit
    ) {
        // set flow to updating location
        val result = locationRemoteDataSource.getLocation(cancellationToken)
        if (result != null) {
            locationLocalDataSource.edit {
                it[LOCATION_LATITUDE] = result.latitude
                it[LOCATION_LONGITUDE] = result.longitude
                it[LAST_UPDATED] = result.time
            }
            onCompletion(true)
        } else {
            onCompletion(false)
        }
    }

    fun calculatePrayers(date: Instant): PrayerTimes {
        val coordinates = Coordinates(51.51388889, 0.05027778)
        val params =
            CalculationMethod.MOON_SIGHTING_COMMITTEE.parameters.copy(madhab = Madhab.HANAFI)

        return PrayerTimes(
            coordinates,
            DateComponents.from(date),
            params
        )
    }
}

