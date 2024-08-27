package com.talhaak.apps.simpleprayer.presentation.ui.prayerlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.google.android.gms.tasks.CancellationTokenSource
import com.talhaak.apps.simpleprayer.MyApplication
import com.talhaak.apps.simpleprayer.data.ClockBroadcastReceiver
import com.talhaak.apps.simpleprayer.data.Prayer
import com.talhaak.apps.simpleprayer.data.PrayerDay
import com.talhaak.apps.simpleprayer.data.location.LocationRepository
import com.talhaak.apps.simpleprayer.data.location.StoredLocation
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
    private val locationRepository: LocationRepository,
    clockBroadcastReceiver: ClockBroadcastReceiver
) : ViewModel() {
    private val locationCancelSource = CancellationTokenSource()
    private val isLoadingState: MutableStateFlow<Boolean> = MutableStateFlow(false)

    val uiState: StateFlow<PrayerListScreenState> = combine(
        isLoadingState,
        locationRepository.lastLocationFlow,
        clockBroadcastReceiver.minuteTickFlow
    ) { isLoading, location, _ ->
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
            locationRepository.updateLocation(locationCancelSource.token) {
                isLoadingState.value = false
            }
        }
    }

    private fun combinedStateFlows(
        isLoading: Boolean,
        location: StoredLocation,
    ): PrayerListScreenState = when {
        isLoading -> PrayerListScreenState.UpdatingLocation(
            if (location is StoredLocation.Valid) getScreenStateFrom(location) else null
        )

        location is StoredLocation.None -> PrayerListScreenState.NoLocation
        location is StoredLocation.Invalid -> PrayerListScreenState.FailedLocation
        location is StoredLocation.Valid -> PrayerListScreenState.FoundLocation(
            getScreenStateFrom(location)
        )

        else -> throw IllegalStateException("Unknown location state")
    }

    private fun getScreenStateFrom(location: StoredLocation.Valid): PrayerListScreenState.ScreenState {
        val now = Clock.System.now()
        val apiTimes = locationRepository.calculatePrayers(now)
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
                val prayerRepository = (this[APPLICATION_KEY] as MyApplication).locationRepository
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