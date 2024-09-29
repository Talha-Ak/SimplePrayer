package com.talhaak.apps.simpleprayer.tiles.nextprayer

import com.batoulapps.adhan2.Prayer
import kotlinx.datetime.Instant

data class NextPrayerTileState(
    val prayers: List<NextPrayerTileEntry>,
    val loc: String
)

data class NextPrayerTileEntry(
    val prayer: Prayer,
    val startFrom: Instant,
    val dueAt: Instant,
)