package com.talhaak.apps.simpleprayer.presentation.ui.prayerlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.batoulapps.adhan2.Coordinates
import com.batoulapps.adhan2.Prayer
import com.batoulapps.adhan2.PrayerTimes
import com.batoulapps.adhan2.data.DateComponents
import com.google.android.gms.tasks.CancellationTokenSource
import com.talhaak.apps.simpleprayer.MyApplication
import com.talhaak.apps.simpleprayer.data.ClockBroadcastReceiver
import com.talhaak.apps.simpleprayer.data.location.LocationRepository
import com.talhaak.apps.simpleprayer.data.location.StoredLocation
import com.talhaak.apps.simpleprayer.data.prayer.PrayerDay
import com.talhaak.apps.simpleprayer.data.prayer.toPrayerDay
import com.talhaak.apps.simpleprayer.data.userprefs.UserPreferences
import com.talhaak.apps.simpleprayer.data.userprefs.UserPreferencesRepository
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
    userPreferencesRepository: UserPreferencesRepository,
    clockBroadcastReceiver: ClockBroadcastReceiver
) : ViewModel() {
    private val locationCancelSource = CancellationTokenSource()
    private val isLoadingState: MutableStateFlow<Boolean> = MutableStateFlow(false)

    val uiState: StateFlow<PrayerListScreenState> = combine(
        isLoadingState,
        locationRepository.lastLocationFlow,
        userPreferencesRepository.userPrefsFlow,
        clockBroadcastReceiver.minuteTickFlow
    ) { isLoading, location, userPrefs, _ ->
        combinedStateFlows(isLoading, location, userPrefs)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PrayerListScreenState.UpdatingLocation(null)
    )

    init {
        updateLocation()
    }

    fun updateLocation() {
        viewModelScope.launch {
            isLoadingState.value = true
            locationRepository.updateLocation(locationCancelSource.token)
            isLoadingState.value = false
        }
    }

    private fun combinedStateFlows(
        isLoading: Boolean,
        location: StoredLocation,
        userPrefs: UserPreferences
    ): PrayerListScreenState = when {
        isLoading -> PrayerListScreenState.UpdatingLocation(
            if (location is StoredLocation.Valid) getScreenStateFrom(location, userPrefs) else null
        )

        location is StoredLocation.None -> PrayerListScreenState.NoLocation
        location is StoredLocation.Invalid -> PrayerListScreenState.FailedLocation
        location is StoredLocation.Valid -> PrayerListScreenState.FoundLocation(
            getScreenStateFrom(location, userPrefs)
        )

        else -> throw IllegalStateException("Unknown location state")
    }

    private fun getScreenStateFrom(
        location: StoredLocation.Valid,
        userPrefs: UserPreferences
    ): PrayerListScreenState.ScreenState {
        val now = Clock.System.now()
        val apiTimes = PrayerTimes(
            Coordinates(location.coords.lat, location.coords.long),
            DateComponents.from(now),
            userPrefs.method.parameters.copy(
                madhab = userPrefs.madhab,
                highLatitudeRule = userPrefs.highLatitudeRule
            )
        )

        val currentPrayer = apiTimes.currentPrayer(now)
        val nextPrayer = apiTimes.nextPrayer(now)
        val timeTo = apiTimes.timeForPrayer(nextPrayer)?.plus(1.minutes)?.minus(now)

        return PrayerListScreenState.ScreenState(
            prayers = apiTimes.toPrayerDay(),
            location = location.area,
            currentPrayer = currentPrayer,
            nextPrayer = when (nextPrayer) {
                Prayer.NONE -> null
                else -> PrayerListScreenState.NextPrayer(
                    nextPrayer,
                    timeTo!!
                )
            },
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
                val userPreferencesRepository =
                    (this[APPLICATION_KEY] as MyApplication).userPreferencesRepository
                val clockBroadcastReceiver =
                    ClockBroadcastReceiver((this[APPLICATION_KEY] as MyApplication).applicationContext)

                PrayerListScreenViewModel(
                    prayerRepository,
                    userPreferencesRepository,
                    clockBroadcastReceiver
                )
            }
        }
    }
}

sealed interface PrayerListScreenState {
    data class NextPrayer(val prayer: Prayer, val timeTo: Duration)
    data class ScreenState(
        val prayers: PrayerDay,
        val location: String,
        val currentPrayer: Prayer,
        val nextPrayer: NextPrayer?,
    )

    data object NoLocation : PrayerListScreenState
    data class UpdatingLocation(val state: ScreenState?) : PrayerListScreenState
    data object FailedLocation : PrayerListScreenState
    data class FoundLocation(val state: ScreenState) : PrayerListScreenState
}