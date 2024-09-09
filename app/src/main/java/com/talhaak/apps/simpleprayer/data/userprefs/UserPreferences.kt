package com.talhaak.apps.simpleprayer.data.userprefs

import com.batoulapps.adhan2.CalculationMethod
import com.batoulapps.adhan2.HighLatitudeRule
import com.batoulapps.adhan2.Madhab

data class UserPreferences(
    val madhab: Madhab,
    val method: CalculationMethod,
    val highLatitudeRule: HighLatitudeRule?,
    val customAngles: Pair<Double?, Double?>,
    val prayerAdjustments: UserPrayerAdjustments
)

data class UserPrayerAdjustments(
    val fajr: Int?,
    val sunrise: Int?,
    val dhuhr: Int?,
    val asr: Int?,
    val maghrib: Int?,
    val isha: Int?,
)