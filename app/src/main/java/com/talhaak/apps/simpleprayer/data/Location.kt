package com.talhaak.apps.simpleprayer.data

import kotlinx.datetime.Instant

data class DeviceLocation(
    val lat: Double,
    val long: Double,
    val area: String,
    val lastUpdated: Instant
)