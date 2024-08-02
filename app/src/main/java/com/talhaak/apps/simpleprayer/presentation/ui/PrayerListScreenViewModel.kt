package com.talhaak.apps.simpleprayer.presentation.ui

import android.os.CountDownTimer
import androidx.lifecycle.ViewModel
import com.batoulapps.adhan2.Prayer
import com.talhaak.apps.simpleprayer.data.PrayerRepository
import com.talhaak.apps.simpleprayer.presentation.PrayerDay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes

class PrayerListScreenViewModel : ViewModel() {
    private var timer: CountDownTimer
    private val _uiState: MutableStateFlow<PrayerListScreenState> =
        MutableStateFlow(PrayerListScreenState.Calculating)
    val uiState = _uiState.asStateFlow()

    init {
        val now = Clock.System.now()
        val times = PrayerRepository.calculatePrayers(now)

        val timesTomorrow = PrayerRepository.calculatePrayers(now.plus(1.days))
        val nextPrayer = times.timeForPrayer(times.nextPrayer(now)) ?: timesTomorrow.fajr

        timer = object :
            CountDownTimer(nextPrayer.minus(now).inWholeMilliseconds, MSECS_IN_SEC * SECS_IN_MIN) {
            override fun onTick(millisUntilFinished: Long) = update()
            override fun onFinish() = update()

            fun update() {
                val now = Clock.System.now()
                val nextPrayer = times.timeForPrayer(times.nextPrayer(now)) ?: timesTomorrow.fajr
                _uiState.value = PrayerListScreenState.Success(
                    "Shadwell",
                    times.currentPrayer(now),
                    nextPrayer.minus(now).inWholeMinutes.minutes.toString(),
                    PrayerDay(
                        date = now,
                        fajr = times.fajr,
                        sunrise = times.sunrise,
                        dhuhr = times.dhuhr,
                        asr = times.asr,
                        maghrib = times.maghrib,
                        isha = times.isha
                    )
                )
            }
        }.start()
    }

    companion object {
        const val SECS_IN_MIN = 60L
        const val MSECS_IN_SEC = 1000L
    }
}

sealed interface PrayerListScreenState {
    data object Calculating : PrayerListScreenState
    data class Success(
        val location: String,
        val currentPrayer: Prayer,
        val currentPrayerMinutesLeft: String,
        val prayers: PrayerDay
    ) : PrayerListScreenState
}