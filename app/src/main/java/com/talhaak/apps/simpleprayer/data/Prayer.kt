package com.talhaak.apps.simpleprayer.data

import com.batoulapps.adhan2.PrayerTimes
import com.talhaak.apps.simpleprayer.R
import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import com.batoulapps.adhan2.Prayer as Adhan2Prayer

enum class Prayer(val label: Int) {
    FAJR(R.string.fajr),
    SUNRISE(R.string.sunrise),
    DHUHR(R.string.dhuhr),
    ASR(R.string.asr),
    MAGHRIB(R.string.maghrib),
    ISHA(R.string.isha)
}

fun Adhan2Prayer.toAppPrayer(): Prayer = when (this) {
    // TODO When before fajr, after midnight, show current day, "starts at"
    Adhan2Prayer.NONE -> Prayer.ISHA
    Adhan2Prayer.FAJR -> Prayer.FAJR
    Adhan2Prayer.SUNRISE -> Prayer.SUNRISE
    Adhan2Prayer.DHUHR -> Prayer.DHUHR
    Adhan2Prayer.ASR -> Prayer.ASR
    Adhan2Prayer.MAGHRIB -> Prayer.MAGHRIB
    Adhan2Prayer.ISHA -> Prayer.ISHA
}

data class PrayerDay(
    val fajr: Instant,
    val sunrise: Instant,
    val dhuhr: Instant,
    val asr: Instant,
    val maghrib: Instant,
    val isha: Instant
) {
    fun getTimeString(prayer: Prayer): String {
        val time = when (prayer) {
            Prayer.FAJR -> fajr
            Prayer.SUNRISE -> sunrise
            Prayer.DHUHR -> dhuhr
            Prayer.ASR -> asr
            Prayer.MAGHRIB -> maghrib
            Prayer.ISHA -> isha
        }

        val formatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
        return time.toJavaInstant().atZone(ZoneId.systemDefault()).format(formatter)
    }
}

fun PrayerTimes.toAppPrayerDay(): PrayerDay = PrayerDay(
    fajr = this.fajr,
    sunrise = this.sunrise,
    dhuhr = this.dhuhr,
    asr = this.asr,
    maghrib = this.maghrib,
    isha = this.isha
)
