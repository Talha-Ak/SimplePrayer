package com.talhaak.apps.simpleprayer.tiles.prayerlist

import com.talhaak.apps.simpleprayer.data.prayer.PrayerDay

sealed interface PrayerListTileState {
    data object None : PrayerListTileState
    data class Valid(
        val prayers: PrayerDay,
        val tomorrow: PrayerDay
    ) : PrayerListTileState
}