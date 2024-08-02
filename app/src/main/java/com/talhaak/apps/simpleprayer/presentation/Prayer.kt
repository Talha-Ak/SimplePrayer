package com.talhaak.apps.simpleprayer.presentation

import com.batoulapps.adhan2.Prayer
import com.talhaak.apps.simpleprayer.R
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalTime
import kotlinx.datetime.toLocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

data class PrayerDay(
    val date: Instant,
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
            else -> return ""
        }
        val formatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
        return time.toLocalDateTime(TimeZone.currentSystemDefault()).time.toJavaLocalTime()
            .format(formatter)
    }
}

val Prayer.label: Int
    get() = when (this) {
        Prayer.FAJR -> R.string.fajr
        Prayer.SUNRISE -> R.string.sunrise
        Prayer.DHUHR -> R.string.dhuhr
        Prayer.ASR -> R.string.asr
        Prayer.MAGHRIB -> R.string.maghrib
        Prayer.ISHA -> R.string.isha
        else -> throw IllegalArgumentException("Getting label of invalid prayer: $this")
    }