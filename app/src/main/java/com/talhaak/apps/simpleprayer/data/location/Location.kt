package com.talhaak.apps.simpleprayer.data.location

import kotlinx.datetime.Instant

data class DeviceCoordinates(
    val lat: Double,
    val long: Double
)

sealed interface StoredLocation {
    data object None : StoredLocation

    data class Invalid(
        val lastUpdated: Instant
    ) : StoredLocation

    data class Valid(
        val coords: DeviceCoordinates,
        val area: String,
        val lastUpdated: Instant
    ) : StoredLocation
}
