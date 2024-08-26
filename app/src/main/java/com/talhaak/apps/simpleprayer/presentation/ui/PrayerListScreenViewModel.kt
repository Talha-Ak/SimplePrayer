package com.talhaak.apps.simpleprayer.presentation.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.google.android.gms.tasks.CancellationTokenSource
import com.talhaak.apps.simpleprayer.MyApplication
import com.talhaak.apps.simpleprayer.data.ClockBroadcastReceiver
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
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

class PrayerListScreenViewModel(
    private val prayerRepository: PrayerRepository,
    private val clockBroadcastReceiver: ClockBroadcastReceiver
) : ViewModel() {
    val locationCancelSource = CancellationTokenSource()
    val isLoadingState: MutableStateFlow<Boolean> = MutableStateFlow(false)

    val uiState: StateFlow<PrayerListScreenState> = combine(
        isLoadingState,
        prayerRepository.lastLocationFlow,
        clockBroadcastReceiver.minuteTickFlow
    ) { isLoading, location, yunit ->
        Log.d("PrayerListScreenViewModel", "isLoading: $isLoading, location: $location")
        combinedStateFlows(isLoading, location)
    }.stateIn(
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
                Log.d("PrayerListScreenViewModel", "updateLocation: $it")
                isLoadingState.value = false
                Log.d("PrayerListScreenViewModel", "isLoadingState: ${isLoadingState.value}")
            }
        }
    }

    private fun combinedStateFlows(
        isLoading: Boolean,
        location: DeviceLocation,
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
        val apiTimes = prayerRepository.calculatePrayers(now)
        val times = apiTimes.toAppPrayerDay()

        val currentPrayer = apiTimes.currentPrayer(now).toAppPrayer()
        val nextPrayer = if (currentPrayer != null) {
            currentPrayer.next()?.let {
                val timeTo = times[it] + 1.minutes - now
                PrayerListScreenState.NextPrayer(it, timeTo.inWholeMinutes.minutes)
            }
        } else {
            val timeTo = times[Prayer.FAJR] + 1.minutes - now
            PrayerListScreenState.NextPrayer(Prayer.FAJR, timeTo.inWholeMinutes.minutes)
        }

        return PrayerListScreenState.ScreenState(
            location = location.area,
            currentPrayer = currentPrayer,
            nextPrayer = nextPrayer,
            prayers = times
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
                val clockBroadcastReceiver =
                    ClockBroadcastReceiver((this[APPLICATION_KEY] as MyApplication).applicationContext)
                PrayerListScreenViewModel(prayerRepository, clockBroadcastReceiver)
            }
        }
    }
}

sealed interface PrayerListScreenState {
    data class NextPrayer(val prayer: Prayer, val timeTo: Duration)
    data class ScreenState(
        val location: String,
        val currentPrayer: Prayer?,
        val nextPrayer: NextPrayer?,
        val prayers: PrayerDay
    )

    data object NoLocation : PrayerListScreenState
    data class UpdatingLocation(val state: ScreenState?) : PrayerListScreenState
    data object FailedLocation : PrayerListScreenState
    data class FoundLocation(val state: ScreenState) : PrayerListScreenState
}