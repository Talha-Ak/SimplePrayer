package com.talhaak.apps.simpleprayer.data.prayer

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
