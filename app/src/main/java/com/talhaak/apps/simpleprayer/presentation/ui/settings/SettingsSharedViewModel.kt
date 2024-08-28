package com.talhaak.apps.simpleprayer.presentation.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.batoulapps.adhan2.CalculationMethod
import com.batoulapps.adhan2.Madhab
import com.talhaak.apps.simpleprayer.MyApplication
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
                method = it.method
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
        val method: CalculationMethod
    ) : SettingsState
}