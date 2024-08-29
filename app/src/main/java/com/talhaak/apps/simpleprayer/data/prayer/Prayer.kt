package com.talhaak.apps.simpleprayer.data.prayer

import com.batoulapps.adhan2.CalculationMethod
import com.batoulapps.adhan2.HighLatitudeRule
import com.batoulapps.adhan2.Madhab
import com.batoulapps.adhan2.Prayer
import com.batoulapps.adhan2.PrayerTimes
import com.talhaak.apps.simpleprayer.R
import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

fun allPrayers() = listOf(
    Prayer.FAJR,
    Prayer.SUNRISE,
    Prayer.DHUHR,
    Prayer.ASR,
    Prayer.MAGHRIB,
    Prayer.ISHA,
)

fun getLabelFor(prayer: Prayer) = when (prayer) {
    Prayer.NONE -> throw IllegalStateException()
    Prayer.FAJR -> R.string.fajr
    Prayer.SUNRISE -> R.string.sunrise
    Prayer.DHUHR -> R.string.dhuhr
    Prayer.ASR -> R.string.asr
    Prayer.MAGHRIB -> R.string.maghrib
    Prayer.ISHA -> R.string.isha
}

fun getLabelFor(madhab: Madhab) = when (madhab) {
    Madhab.SHAFI -> R.string.shafi
    Madhab.HANAFI -> R.string.hanafi
}

fun getLabelFor(method: CalculationMethod) = when (method) {
    CalculationMethod.MUSLIM_WORLD_LEAGUE -> R.string.method_muslim_world_league
    CalculationMethod.EGYPTIAN -> R.string.method_egyptian
    CalculationMethod.KARACHI -> R.string.method_karachi
    CalculationMethod.UMM_AL_QURA -> R.string.method_umm_al_qura
    CalculationMethod.DUBAI -> R.string.method_dubai
    CalculationMethod.MOON_SIGHTING_COMMITTEE -> R.string.method_moon_sighting_committee
    CalculationMethod.NORTH_AMERICA -> R.string.method_north_america
    CalculationMethod.KUWAIT -> R.string.method_kuwait
    CalculationMethod.QATAR -> R.string.method_qatar
    CalculationMethod.SINGAPORE -> R.string.method_singapore
    CalculationMethod.TURKEY -> R.string.method_turkey
    CalculationMethod.OTHER -> R.string.method_custom
}

fun getLabelFor(rule: HighLatitudeRule) = when (rule) {
    HighLatitudeRule.MIDDLE_OF_THE_NIGHT -> R.string.middle_of_the_night
    HighLatitudeRule.SEVENTH_OF_THE_NIGHT -> R.string.seventh_of_the_night
    HighLatitudeRule.TWILIGHT_ANGLE -> R.string.twilight_angle
}

data class PrayerDay(
    val fajr: Instant,
    val sunrise: Instant,
    val dhuhr: Instant,
    val asr: Instant,
    val maghrib: Instant,
    val isha: Instant
) {
    operator fun get(prayer: Prayer): Instant {
        return when (prayer) {
            Prayer.FAJR -> fajr
            Prayer.SUNRISE -> sunrise
            Prayer.DHUHR -> dhuhr
            Prayer.ASR -> asr
            Prayer.MAGHRIB -> maghrib
            Prayer.ISHA -> isha
            Prayer.NONE -> throw IllegalStateException()
        }
    }
}

fun PrayerTimes.toPrayerDay(): PrayerDay = PrayerDay(
    fajr = this.fajr,
    sunrise = this.sunrise,
    dhuhr = this.dhuhr,
    asr = this.asr,
    maghrib = this.maghrib,
    isha = this.isha
)

fun Instant.toFormattedString(): String {
    val formatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
    return this.toJavaInstant().atZone(ZoneId.systemDefault()).format(formatter)
}
