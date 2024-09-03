package com.talhaak.apps.simpleprayer.data.location

import androidx.wear.tiles.TileUpdateRequester
import com.google.android.gms.tasks.CancellationToken
import com.talhaak.apps.simpleprayer.tiles.nextprayer.NextPrayerTileService
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
        val area = result?.let { locationRemoteDataSource.getArea(it) }
        locationLocalDataSource.updateLocation(result, area.orEmpty())

        if (result != null) {
            tileUpdateRequester.requestUpdate(NextPrayerTileService::class.java)
            return true
        } else {
            return false
        }
    }

    private suspend fun locationIsFresh(): Boolean {
        val locationData = lastLocationFlow.firstOrNull()

        return locationData is StoredLocation.Valid && locationData.lastUpdated.let {
            Clock.System.now() < it + 10.minutes
        }
    }
}
