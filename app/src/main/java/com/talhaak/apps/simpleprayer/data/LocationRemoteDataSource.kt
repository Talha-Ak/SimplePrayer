package com.talhaak.apps.simpleprayer.data

import android.location.Geocoder
import android.location.Location
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority.PRIORITY_BALANCED_POWER_ACCURACY
import com.google.android.gms.tasks.CancellationToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class LocationRemoteDataSource(
    val locationClient: FusedLocationProviderClient,
    val geocoder: Geocoder
) {
    suspend fun getLocation(cancellationToken: CancellationToken): Location? {
        return withContext(Dispatchers.IO) {
            val locationRequest = CurrentLocationRequest.Builder()
                .setMaxUpdateAgeMillis(10 * 60 * 1000)
                .setPriority(PRIORITY_BALANCED_POWER_ACCURACY)
                .build()
            return@withContext locationClient.getCurrentLocation(locationRequest, cancellationToken)
                .await()
        }
    }
}