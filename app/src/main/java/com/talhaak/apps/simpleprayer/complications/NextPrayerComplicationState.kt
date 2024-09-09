package com.talhaak.apps.simpleprayer.complications

import androidx.wear.watchface.complications.datasource.TimeInterval
import com.batoulapps.adhan2.Prayer

data class NextPrayerComplicationState(
    val prayers: List<NextPrayerComplicationEntry>
)

data class NextPrayerComplicationEntry(
    val prayer: Prayer,
    val timeInterval: TimeInterval
)
