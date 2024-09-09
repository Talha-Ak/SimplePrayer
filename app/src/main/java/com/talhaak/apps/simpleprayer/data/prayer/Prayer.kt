package com.talhaak.apps.simpleprayer.data.prayer

import com.batoulapps.adhan2.CalculationMethod
import com.batoulapps.adhan2.Coordinates
import com.batoulapps.adhan2.HighLatitudeRule
import com.batoulapps.adhan2.Madhab
import com.batoulapps.adhan2.Prayer
import com.batoulapps.adhan2.PrayerAdjustments
import com.batoulapps.adhan2.PrayerTimes
import com.batoulapps.adhan2.data.DateComponents
import com.talhaak.apps.simpleprayer.R
import com.talhaak.apps.simpleprayer.data.location.StoredLocation
import com.talhaak.apps.simpleprayer.data.userprefs.UserPrayerAdjustments
import com.talhaak.apps.simpleprayer.data.userprefs.UserPreferences
import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

fun Prayer.next(): Prayer {
    return when (this) {
        Prayer.NONE -> throw IllegalStateException()
        Prayer.FAJR -> Prayer.SUNRISE
        Prayer.SUNRISE -> Prayer.DHUHR
        Prayer.DHUHR -> Prayer.ASR
        Prayer.ASR -> Prayer.MAGHRIB
        Prayer.MAGHRIB -> Prayer.ISHA
        Prayer.ISHA -> Prayer.FAJR
    }
}

fun Prayer.prev(): Prayer {
    return when (this) {
        Prayer.NONE -> throw IllegalStateException()
        Prayer.FAJR -> Prayer.ISHA
        Prayer.SUNRISE -> Prayer.FAJR
        Prayer.DHUHR -> Prayer.SUNRISE
        Prayer.ASR -> Prayer.DHUHR
        Prayer.MAGHRIB -> Prayer.ASR
        Prayer.ISHA -> Prayer.MAGHRIB
    }
}

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

fun getShortLabelFor(prayer: Prayer) = when (prayer) {
    Prayer.NONE -> throw IllegalStateException()
    Prayer.FAJR -> R.string.abbr_fajr
    Prayer.SUNRISE -> R.string.abbr_sunrise
    Prayer.DHUHR -> R.string.abbr_dhuhr
    Prayer.ASR -> R.string.abbr_asr
    Prayer.MAGHRIB -> R.string.abbr_maghrib
    Prayer.ISHA -> R.string.abbr_isha
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

fun getOffsetLabelFor(prayer: Prayer) = when (prayer) {
    Prayer.NONE -> throw IllegalStateException()
    Prayer.FAJR -> R.string.fajr_offset
    Prayer.SUNRISE -> R.string.sunrise_offset
    Prayer.DHUHR -> R.string.dhuhr_offset
    Prayer.ASR -> R.string.asr_offset
    Prayer.MAGHRIB -> R.string.maghrib_offset
    Prayer.ISHA -> R.string.isha_offset
}

fun getPrayerTimes(
    time: Instant,
    location: StoredLocation.Valid,
    prefs: UserPreferences
): PrayerTimes {
    return PrayerTimes(
        Coordinates(location.coords.lat, location.coords.long),
        DateComponents.from(time),
        prefs.method.parameters.copy(
            madhab = prefs.madhab,
            highLatitudeRule = prefs.highLatitudeRule,
            fajrAngle = prefs.customAngles.first ?: prefs.method.parameters.fajrAngle,
            ishaAngle = prefs.customAngles.second ?: prefs.method.parameters.ishaAngle,
            methodAdjustments = PrayerAdjustments(
                fajr = prefs.prayerAdjustments.fajr
                    ?: prefs.method.parameters.methodAdjustments.fajr,
                sunrise = prefs.prayerAdjustments.sunrise
                    ?: prefs.method.parameters.methodAdjustments.sunrise,
                dhuhr = prefs.prayerAdjustments.dhuhr
                    ?: prefs.method.parameters.methodAdjustments.dhuhr,
                asr = prefs.prayerAdjustments.asr ?: prefs.method.parameters.methodAdjustments.asr,
                maghrib = prefs.prayerAdjustments.maghrib
                    ?: prefs.method.parameters.methodAdjustments.maghrib,
                isha = prefs.prayerAdjustments.isha
                    ?: prefs.method.parameters.methodAdjustments.isha
            )
        )
    )
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

operator fun PrayerAdjustments.get(prayer: Prayer): Int = when (prayer) {
    Prayer.NONE -> throw IllegalStateException()
    Prayer.FAJR -> fajr
    Prayer.SUNRISE -> sunrise
    Prayer.DHUHR -> dhuhr
    Prayer.ASR -> asr
    Prayer.MAGHRIB -> maghrib
    Prayer.ISHA -> isha
}

operator fun UserPrayerAdjustments.get(prayer: Prayer): Int? = when (prayer) {
    Prayer.NONE -> throw IllegalStateException()
    Prayer.FAJR -> fajr
    Prayer.SUNRISE -> sunrise
    Prayer.DHUHR -> dhuhr
    Prayer.ASR -> asr
    Prayer.MAGHRIB -> maghrib
    Prayer.ISHA -> isha
}

fun Instant.toFormattedString(): String {
    val formatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
    return this.toJavaInstant().atZone(ZoneId.systemDefault()).format(formatter)
}
