package com.talhaak.apps.simpleprayer.data

import com.batoulapps.adhan2.CalculationMethod
import com.batoulapps.adhan2.Coordinates
import com.batoulapps.adhan2.Madhab
import com.batoulapps.adhan2.PrayerTimes
import com.batoulapps.adhan2.data.DateComponents
import kotlinx.datetime.Instant

object PrayerRepository {
    fun calculatePrayers(date: Instant): PrayerTimes {
        val coordinates = Coordinates(51.51388889, 0.05027778)
        val params =
            CalculationMethod.MOON_SIGHTING_COMMITTEE.parameters.copy(madhab = Madhab.HANAFI)
        return PrayerTimes(coordinates, DateComponents.from(date), params)
    }
}