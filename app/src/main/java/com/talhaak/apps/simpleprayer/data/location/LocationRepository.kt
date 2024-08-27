package com.talhaak.apps.simpleprayer.data.location

import android.util.Log
import com.batoulapps.adhan2.CalculationMethod
import com.batoulapps.adhan2.Coordinates
import com.batoulapps.adhan2.Madhab
import com.batoulapps.adhan2.PrayerTimes
import com.batoulapps.adhan2.data.DateComponents
import com.google.android.gms.tasks.CancellationToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.minutes

class LocationRepository(
    private val locationLocalDataSource: LocationLocalDataSource,
    private val locationRemoteDataSource: LocationRemoteDataSource
) {
    val lastLocationFlow: Flow<StoredLocation> = locationLocalDataSource.locationFlow

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
        val area = result?.let { locationRemoteDataSource.getArea(it) }
        locationLocalDataSource.updateLocation(result, area.orEmpty())
        onCompletion(result != null)

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
        val locationData = lastLocationFlow.firstOrNull()

        return locationData is StoredLocation.Valid && locationData.lastUpdated.let {
            Clock.System.now() < it + 10.minutes
        }
    }
}


