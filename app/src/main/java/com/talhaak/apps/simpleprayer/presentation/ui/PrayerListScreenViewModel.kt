package com.talhaak.apps.simpleprayer.presentation.ui

import androidx.lifecycle.ViewModel
import com.talhaak.apps.simpleprayer.presentation.Prayer
import com.talhaak.apps.simpleprayer.presentation.PrayerDay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.Instant

class PrayerListScreenViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(
        PrayerListScreenState.Success(
            "Shadwell",
            Prayer.DHUHR,
            34,
            PrayerDay(
                Instant.now(),
                Instant.now().minusSeconds(3600),
                Instant.now().minusSeconds(600),
                Instant.now(),
                Instant.now().plusSeconds(600),
                Instant.now().plusSeconds(3600),
                Instant.now().plusSeconds(7200)
            )
        )
    )

    val uiState = _uiState.asStateFlow()
}

sealed interface PrayerListScreenState {
    data object Calculating : PrayerListScreenState
    data class Success(
        val location: String,
        val currentPrayer: Prayer,
        val currentPrayerMinutesLeft: Int,
        val prayers: PrayerDay
    ) : PrayerListScreenState
}