package com.talhaak.apps.simpleprayer.data.location

import android.content.Context
import android.location.Location
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant

private const val LOCATION_DATASTORE_NAME = "location_datastore"
val Context.locationDatastore by preferencesDataStore(
    name = LOCATION_DATASTORE_NAME
)

class LocationLocalDataSource(
    private val dataStore: DataStore<Preferences>
) {
    private companion object {
        val LOCATION_LATITUDE = doublePreferencesKey("location_Lat")
        val LOCATION_LONGITUDE = doublePreferencesKey("location_long")
        val LOCATION_AREA = stringPreferencesKey("location_area")
        val LAST_UPDATED = longPreferencesKey("last_updated")
    }

    val locationFlow: Flow<StoredLocation> = dataStore.data.catch {
        if (it is IOException) {
            Log.e("PrayerRepository", "Error reading preferences", it)
            emit(emptyPreferences())
        } else {
            throw it
        }
    }.map { preferences ->
        val lastUpdated = preferences[LAST_UPDATED]?.let {
            Instant.fromEpochMilliseconds(it)
        } ?: return@map StoredLocation.None

        val area = preferences[LOCATION_AREA].orEmpty()

        val locationLong = preferences[LOCATION_LONGITUDE]
        val locationLat = preferences[LOCATION_LATITUDE]
        if (locationLat == null || locationLong == null) {
            return@map StoredLocation.Invalid(lastUpdated)
        }

        StoredLocation.Valid(
            DeviceCoordinates(locationLat, locationLong),
            area,
            lastUpdated
        )
    }

    suspend fun updateLocation(location: Location?, area: String) {
        if (location == null) {
            dataStore.edit {
                it[LAST_UPDATED] = System.currentTimeMillis()
            }
        } else {
            dataStore.edit {
                it[LOCATION_LATITUDE] = location.latitude
                it[LOCATION_LONGITUDE] = location.longitude
                it[LOCATION_AREA] = area
                it[LAST_UPDATED] = location.time
            }
        }
    }
}