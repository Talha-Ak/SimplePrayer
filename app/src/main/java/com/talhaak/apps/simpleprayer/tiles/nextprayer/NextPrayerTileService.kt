package com.talhaak.apps.simpleprayer.tiles.nextprayer

import androidx.wear.protolayout.ResourceBuilders.Resources
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.TileBuilders.Tile
import com.batoulapps.adhan2.Prayer
import com.google.android.horologist.tiles.SuspendingTileService
import com.talhaak.apps.simpleprayer.MyApplication
import com.talhaak.apps.simpleprayer.data.location.LocationRepository
import com.talhaak.apps.simpleprayer.data.location.StoredLocation
import com.talhaak.apps.simpleprayer.data.prayer.getPrayerTimes
import com.talhaak.apps.simpleprayer.data.userprefs.UserPreferences
import com.talhaak.apps.simpleprayer.data.userprefs.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.days

class NextPrayerTileService : SuspendingTileService() {
    private lateinit var renderer: NextPrayerTileRenderer
    private lateinit var locationRepository: LocationRepository
    private lateinit var userPreferencesRepository: UserPreferencesRepository
    private lateinit var tileStateFlow: Flow<NextPrayerTileState>

    override fun onCreate() {
        super.onCreate()
        renderer = NextPrayerTileRenderer(this)
        locationRepository = (application as MyApplication).locationRepository
        userPreferencesRepository = (application as MyApplication).userPreferencesRepository
        tileStateFlow = combine(
            locationRepository.lastLocationFlow,
            userPreferencesRepository.userPrefsFlow,
        ) { location, prefs ->
            when (location) {
                is StoredLocation.None, is StoredLocation.Invalid -> NextPrayerTileState(emptyList())
                is StoredLocation.Valid ->
                    getPrayerList(Clock.System.now(), location, prefs)
            }
        }
    }

    override suspend fun tileRequest(requestParams: RequestBuilders.TileRequest): Tile {
        val tileState = tileStateFlow.firstOrNull()
        return renderer.renderTimeline(tileState, requestParams)
    }

    override suspend fun resourcesRequest(requestParams: RequestBuilders.ResourcesRequest): Resources =
        renderer.produceRequestedResources(Unit, requestParams)
}

fun getPrayerList(
    time: Instant,
    location: StoredLocation.Valid,
    prefs: UserPreferences
): NextPrayerTileState {
    val today = getPrayerTimes(time, location, prefs)
    val tomorrow = getPrayerTimes(time + 1.days, location, prefs)

    return NextPrayerTileState(
        listOf(
            NextPrayerTileEntry(Prayer.FAJR, time, today.fajr),
            NextPrayerTileEntry(Prayer.SUNRISE, today.fajr, today.sunrise),
            NextPrayerTileEntry(Prayer.DHUHR, today.sunrise, today.dhuhr),
            NextPrayerTileEntry(Prayer.ASR, today.dhuhr, today.asr),
            NextPrayerTileEntry(Prayer.MAGHRIB, today.asr, today.maghrib),
            NextPrayerTileEntry(Prayer.ISHA, today.maghrib, today.isha),
            NextPrayerTileEntry(Prayer.FAJR, today.isha, tomorrow.fajr)
        ).filter { it.dueAt > time }
    )
}