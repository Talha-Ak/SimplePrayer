package com.talhaak.apps.simpleprayer.data

import kotlinx.datetime.Instant

data class DeviceCoordinates(
    val lat: Double,
    val long: Double
)

data class DeviceLocation(
    val coords: DeviceCoordinates?,
    val area: String,
    val lastUpdated: Instant?
)