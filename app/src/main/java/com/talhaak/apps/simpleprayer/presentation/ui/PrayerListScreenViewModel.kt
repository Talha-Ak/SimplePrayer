package com.talhaak.apps.simpleprayer.presentation.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.google.android.gms.tasks.CancellationTokenSource
import com.talhaak.apps.simpleprayer.MyApplication
import com.talhaak.apps.simpleprayer.data.DeviceLocation
import com.talhaak.apps.simpleprayer.data.Prayer
import com.talhaak.apps.simpleprayer.data.PrayerDay
import com.talhaak.apps.simpleprayer.data.PrayerRepository
import com.talhaak.apps.simpleprayer.data.toAppPrayer
import com.talhaak.apps.simpleprayer.data.toAppPrayerDay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes

class PrayerListScreenViewModel(
    private val prayerRepository: PrayerRepository
) : ViewModel() {
    val locationCancelSource = CancellationTokenSource()
    val isLoadingState: MutableStateFlow<Boolean> = MutableStateFlow(false)

    val uiState: StateFlow<PrayerListScreenState> = combine(
        isLoadingState,
        prayerRepository.lastLocationFlow,
        ::combinedStateFlows
    ).stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        PrayerListScreenState.UpdatingLocation(null)
    )

    init {
        updateLocation()
    }

    fun updateLocation() {
        viewModelScope.launch {
            isLoadingState.value = true
            prayerRepository.updateLocation(locationCancelSource.token) {
                isLoadingState.value = false
            }
        }
    }

    private fun combinedStateFlows(
        isLoading: Boolean,
        location: DeviceLocation
    ): PrayerListScreenState = when {
        isLoading -> PrayerListScreenState.UpdatingLocation(location.coords?.let {
            getScreenStateFrom(location)
        })

        location.lastUpdated == null -> PrayerListScreenState.NoLocation
        location.coords == null -> PrayerListScreenState.FailedLocation
        else -> PrayerListScreenState.FoundLocation(getScreenStateFrom(location))
    }

    private fun getScreenStateFrom(location: DeviceLocation): PrayerListScreenState.ScreenState {
        val now = Clock.System.now()
        val times = prayerRepository.calculatePrayers(now)
        val timesTomorrow = prayerRepository.calculatePrayers(now + 1.days)
        val nextPrayer = times.timeForPrayer(times.nextPrayer(now)) ?: timesTomorrow.fajr

        return PrayerListScreenState.ScreenState(
            location = location.area,
            currentPrayer = times.currentPrayer(now).toAppPrayer(),
            currentPrayerMinutesLeft = nextPrayer.minus(now).inWholeMinutes.minutes.toString(),
            prayers = times.toAppPrayerDay()
        )
    }

    override fun onCleared() {
        super.onCleared()
        locationCancelSource.cancel()
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val prayerRepository = (this[APPLICATION_KEY] as MyApplication).prayerRepository
                PrayerListScreenViewModel(prayerRepository)
            }
        }
    }
}

sealed interface PrayerListScreenState {
    data class ScreenState(
        val location: String,
        val currentPrayer: Prayer,
        val currentPrayerMinutesLeft: String,
        val prayers: PrayerDay
    )

    data object NoLocation : PrayerListScreenState
    data class UpdatingLocation(val state: ScreenState?) : PrayerListScreenState
    data object FailedLocation : PrayerListScreenState
    data class FoundLocation(val state: ScreenState) : PrayerListScreenState
}