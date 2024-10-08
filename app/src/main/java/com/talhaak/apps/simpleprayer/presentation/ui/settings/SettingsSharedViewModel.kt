package com.talhaak.apps.simpleprayer.presentation.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.batoulapps.adhan2.CalculationMethod
import com.batoulapps.adhan2.HighLatitudeRule
import com.batoulapps.adhan2.Madhab
import com.batoulapps.adhan2.Prayer
import com.talhaak.apps.simpleprayer.MyApplication
import com.talhaak.apps.simpleprayer.data.userprefs.UserPrayerAdjustments
import com.talhaak.apps.simpleprayer.data.userprefs.UserPreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsSharedViewModel(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val uiState: StateFlow<SettingsState> =
        userPreferencesRepository.userPrefsFlow.map {
            SettingsState.Success(
                madhab = it.madhab,
                method = it.method,
                highLatitudeRule = it.highLatitudeRule,
                customAngles = it.customAngles,
                prayerAdjustments = it.prayerAdjustments
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SettingsState.Loading
        )

    fun updateMadhab(madhab: Madhab) {
        viewModelScope.launch {
            userPreferencesRepository.updateMadhab(madhab)
        }
    }

    fun updateMethod(method: CalculationMethod) {
        viewModelScope.launch {
            userPreferencesRepository.updateCalculationMethod(method)
        }
    }

    fun updateHighLatitudeRule(rule: HighLatitudeRule?) {
        viewModelScope.launch {
            userPreferencesRepository.updateHighLatitudeRule(rule)
        }
    }

    fun updateCustomAngles(fajrAngle: Double, ishaAngle: Double) {
        viewModelScope.launch {
            userPreferencesRepository.updateCustomAngles(fajrAngle, ishaAngle)
        }
    }

    fun updatePrayerOffset(prayer: Prayer, offset: Int) {
        viewModelScope.launch {
            userPreferencesRepository.updatePrayerOffset(prayer, offset)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val userPreferencesRepository =
                    (this[APPLICATION_KEY] as MyApplication).userPreferencesRepository
                SettingsSharedViewModel(userPreferencesRepository)
            }
        }
    }
}

sealed interface SettingsState {
    data object Loading : SettingsState
    data class Success(
        val madhab: Madhab,
        val method: CalculationMethod,
        val highLatitudeRule: HighLatitudeRule? = null,
        val customAngles: Pair<Double?, Double?>,
        val prayerAdjustments: UserPrayerAdjustments
    ) : SettingsState
}