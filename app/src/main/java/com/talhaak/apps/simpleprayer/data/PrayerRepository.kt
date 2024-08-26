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
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.minutes

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
            val deviceCoords = preferences[LOCATION_LATITUDE]?.let { lat ->
                preferences[LOCATION_LONGITUDE]?.let { long ->
                    DeviceCoordinates(lat, long)
                }
            }

            DeviceLocation(
                deviceCoords,
                preferences[LOCATION_AREA].orEmpty(),
                preferences[LAST_UPDATED]?.let { Instant.fromEpochMilliseconds(it) }
            )
        }

    suspend fun updateLocation(
        cancellationToken: CancellationToken,
        onCompletion: (Boolean) -> Unit = {}
    ) {
        Log.d("PrayerRepository", "Updating location")
        if (locationIsFresh()) {
            Log.d("PrayerRepository", "Location is fresh")
            onCompletion(true)
            return
        }

        val result = locationRemoteDataSource.getLocation(cancellationToken)
        if (result != null) {
            val area = locationRemoteDataSource.getArea(result)
            locationLocalDataSource.edit {
                it[LOCATION_LATITUDE] = result.latitude
                it[LOCATION_LONGITUDE] = result.longitude
                it[LOCATION_AREA] = area
                it[LAST_UPDATED] = result.time
            }
            onCompletion(true)
        } else {
            onCompletion(false)
        }
        Log.d("PrayerRepository", "Updated location")
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

    private suspend fun locationIsFresh(): Boolean {
        val locationData = locationLocalDataSource.data.firstOrNull()

        val hasLocationData = locationData?.get(LOCATION_LATITUDE) != null
        val isRecentUpdate = locationData?.get(LAST_UPDATED)?.let {
            Clock.System.now() < Instant.fromEpochMilliseconds(it) + 10.minutes
        } ?: false
        return hasLocationData && isRecentUpdate
    }
}


