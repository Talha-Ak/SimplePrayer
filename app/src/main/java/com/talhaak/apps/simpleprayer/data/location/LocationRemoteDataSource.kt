package com.talhaak.apps.simpleprayer.data.location

import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.util.Log
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority.PRIORITY_BALANCED_POWER_ACCURACY
import com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.tasks.CancellationToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.time.Duration.Companion.seconds

class LocationRemoteDataSource(
    private val locationClient: FusedLocationProviderClient,
    private val geocoder: Geocoder?
) {
    suspend fun getLocation(cancellationToken: CancellationToken): Location? {
        return getLocation(PRIORITY_BALANCED_POWER_ACCURACY, cancellationToken)
            ?: getLocation(PRIORITY_HIGH_ACCURACY, cancellationToken)
    }

    suspend fun getArea(location: Location): String =
        withContext(Dispatchers.IO) {
            withTimeoutOrNull(30.seconds) {
                if (geocoder == null) {
                    Log.e("LocationRemoteDataSource", "Geocoder is null")
                    return@withTimeoutOrNull ""
                }

                var area = ""
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        area = suspendCancellableCoroutine { continuation ->
                            geocoder.getFromLocation(location.latitude, location.longitude, 1) {
                                it.firstOrNull()?.let { address ->
                                    continuation.resume(getAreaFromAddress(address))
                                } ?: continuation.resume("")
                            }
                        }
                    } else {
                        val address =
                            geocoder.getFromLocation(location.latitude, location.longitude, 1)
                        if (!address.isNullOrEmpty()) area = getAreaFromAddress(address[0])
                    }
                } catch (e: IllegalArgumentException) {
                    Log.e("LocationRemoteDataSource", "Invalid coords for getting area", e)
                } catch (e: IOException) {
                    Log.e("LocationRemoteDataSource", "Error getting area", e)
                }
                area
            } ?: ""
        }

    private suspend fun getLocation(
        priority: Int,
        cancellationToken: CancellationToken
    ): Location? =
        withContext(Dispatchers.IO) {
            val locationRequest = CurrentLocationRequest.Builder()
                .setMaxUpdateAgeMillis(10 * 60 * 1000)
                .setDurationMillis(30 * 1000)
                .setPriority(priority)
                .build()
            try {
                val location = locationClient.getCurrentLocation(locationRequest, cancellationToken)
                location.await()
            } catch (e: SecurityException) {
                Log.e("LocationRemoteDataSource", "Missing location permission", e)
                null
            }
        }

    private fun getAreaFromAddress(address: Address): String =
        address.subAdminArea ?: address.locality ?: address.adminArea ?: ""
}