package com.talhaak.apps.simpleprayer.presentation

import java.time.Instant
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

enum class Prayer {
    FAJR,
    SUNRISE,
    DHUHR,
    ASR,
    MAGHRIB,
    ISHA
}

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
        }
        val formatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
        return formatter.format(time)
    }
}