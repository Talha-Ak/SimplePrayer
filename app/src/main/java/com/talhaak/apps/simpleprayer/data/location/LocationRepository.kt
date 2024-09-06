package com.talhaak.apps.simpleprayer.data.location

import android.location.Location
import androidx.wear.tiles.TileUpdateRequester
import com.google.android.gms.tasks.CancellationToken
import com.talhaak.apps.simpleprayer.tiles.requestAllUpdates
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.datetime.Clock
import kotlin.time.Duration.Companion.minutes

class LocationRepository(
    private val locationLocalDataSource: LocationLocalDataSource,
    private val locationRemoteDataSource: LocationRemoteDataSource,
    private val tileUpdateRequester: TileUpdateRequester
) {
    val lastLocationFlow: Flow<StoredLocation> = locationLocalDataSource.locationFlow

    suspend fun updateLocation(
        cancellationToken: CancellationToken,
    ): Boolean {
        if (locationIsFresh()) {
            return true
        }

        val result = locationRemoteDataSource.getLocation(cancellationToken)
        triggerUpdate(result, "")
        val area = result?.let { locationRemoteDataSource.getArea(it) }
        triggerUpdate(result, area.orEmpty())

        return result != null
    }

    private suspend fun locationIsFresh(): Boolean {
        val locationData = lastLocationFlow.firstOrNull()

        return locationData is StoredLocation.Valid && locationData.lastUpdated.let {
            Clock.System.now() < it + 10.minutes
        }
    }

    private suspend fun triggerUpdate(location: Location?, area: String) {
        locationLocalDataSource.updateLocation(location, area)
        location?.let { tileUpdateRequester.requestAllUpdates() }
    }
}
