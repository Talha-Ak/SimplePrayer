package com.talhaak.apps.simpleprayer.data.location

import android.util.Log
import com.google.android.gms.tasks.CancellationToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.datetime.Clock
import kotlin.time.Duration.Companion.minutes

class LocationRepository(
    private val locationLocalDataSource: LocationLocalDataSource,
    private val locationRemoteDataSource: LocationRemoteDataSource
) {
    val lastLocationFlow: Flow<StoredLocation> = locationLocalDataSource.locationFlow

    suspend fun updateLocation(
        cancellationToken: CancellationToken,
    ): Boolean {
        Log.d("PrayerRepository", "Updating location")
        if (locationIsFresh()) {
            Log.d("PrayerRepository", "Location is fresh")
            return true
        }

        val result = locationRemoteDataSource.getLocation(cancellationToken)
        val area = result?.let { locationRemoteDataSource.getArea(it) }
        locationLocalDataSource.updateLocation(result, area.orEmpty())
        Log.d("PrayerRepository", "Updated location")

        return result != null
    }

    private suspend fun locationIsFresh(): Boolean {
        val locationData = lastLocationFlow.firstOrNull()

        return locationData is StoredLocation.Valid && locationData.lastUpdated.let {
            Clock.System.now() < it + 10.minutes
        }
    }
}


