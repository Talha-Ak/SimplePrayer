package com.talhaak.apps.simpleprayer.tiles.prayerlist

import androidx.lifecycle.lifecycleScope
import androidx.wear.protolayout.ResourceBuilders.Resources
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.TileBuilders.Tile
import com.google.android.horologist.tiles.SuspendingTileService
import com.talhaak.apps.simpleprayer.MyApplication
import com.talhaak.apps.simpleprayer.data.location.LocationRepository
import com.talhaak.apps.simpleprayer.data.location.StoredLocation
import com.talhaak.apps.simpleprayer.data.prayer.getPrayerTimes
import com.talhaak.apps.simpleprayer.data.prayer.toPrayerDay
import com.talhaak.apps.simpleprayer.data.userprefs.UserPreferences
import com.talhaak.apps.simpleprayer.data.userprefs.UserPreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.Clock
import kotlin.time.Duration.Companion.days

class PrayerListTileService : SuspendingTileService() {
    private lateinit var renderer: PrayerListTileRenderer
    private lateinit var locationRepository: LocationRepository
    private lateinit var userPreferencesRepository: UserPreferencesRepository
    private lateinit var tileStateFlow: StateFlow<PrayerListTileState?>

    override fun onCreate() {
        super.onCreate()
        renderer = PrayerListTileRenderer(this)
        locationRepository = (application as MyApplication).locationRepository
        userPreferencesRepository = (application as MyApplication).userPreferencesRepository
        tileStateFlow = combine(
            locationRepository.lastLocationFlow,
            userPreferencesRepository.userPrefsFlow,
            ::getPrayers
        ).stateIn(
            lifecycleScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
    }

    private fun getPrayers(
        location: StoredLocation,
        prefs: UserPreferences
    ): PrayerListTileState {
        return when (location) {
            is StoredLocation.None, is StoredLocation.Invalid -> PrayerListTileState.None
            is StoredLocation.Valid -> {
                val now = Clock.System.now()
                PrayerListTileState.Valid(
                    prayers = getPrayerTimes(now, location, prefs).toPrayerDay(),
                    tomorrow = getPrayerTimes(now + 1.days, location, prefs).toPrayerDay()
                )
            }
        }
    }

    override suspend fun tileRequest(requestParams: RequestBuilders.TileRequest): Tile {
        val tileState = tileStateFlow.filterNotNull().first()
        return renderer.renderTimeline(tileState, requestParams)
    }

    override suspend fun resourcesRequest(requestParams: RequestBuilders.ResourcesRequest): Resources =
        renderer.produceRequestedResources(Unit, requestParams)
}