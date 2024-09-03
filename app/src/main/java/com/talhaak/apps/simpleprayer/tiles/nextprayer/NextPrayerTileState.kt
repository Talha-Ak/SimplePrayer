package com.talhaak.apps.simpleprayer.tiles.nextprayer

import com.batoulapps.adhan2.Prayer
import kotlinx.datetime.Instant

// TODO yeah so you need to know start and end to have the progress bar work properly...
data class NextPrayerTileState(
    val prayers: List<NextPrayerTileEntry>
)

data class NextPrayerTileEntry(
    val prayer: Prayer,
    val startFrom: Instant,
    val dueAt: Instant,
)