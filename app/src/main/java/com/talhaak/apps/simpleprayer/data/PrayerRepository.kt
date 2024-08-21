package com.talhaak.apps.simpleprayer.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.batoulapps.adhan2.CalculationMethod
import com.batoulapps.adhan2.Coordinates
import com.batoulapps.adhan2.Madhab
import com.batoulapps.adhan2.PrayerTimes
import com.batoulapps.adhan2.data.DateComponents
import kotlinx.datetime.Instant

class PrayerRepository(
    private val datastore: DataStore<Preferences>
) {
    fun calculatePrayers(date: Instant): PrayerTimes {
        val coordinates = Coordinates(51.51388889, 0.05027778)
        val params =
            CalculationMethod.MOON_SIGHTING_COMMITTEE.parameters.copy(madhab = Madhab.HANAFI)

        return PrayerTimes(
            coordinates,
            DateComponents.from(date),
            params
        )
    }
}