package com.talhaak.apps.simpleprayer.data.userprefs

import com.batoulapps.adhan2.CalculationMethod
import com.batoulapps.adhan2.HighLatitudeRule
import com.batoulapps.adhan2.Madhab

data class UserPreferences(
    val madhab: Madhab,
    val method: CalculationMethod,
    val highLatitudeRule: HighLatitudeRule? = null
)