package com.talhaak.apps.simpleprayer.data

import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.util.Log
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.tasks.CancellationToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.IOException

class LocationRemoteDataSource(
    val locationClient: FusedLocationProviderClient,
    val geocoder: Geocoder?
) {
    suspend fun getLocation(cancellationToken: CancellationToken): Location? {
        Log.d("LocationRemoteDataSource", "Getting location")
        return withContext(Dispatchers.IO) {
            val locationRequest = CurrentLocationRequest.Builder()
                .setMaxUpdateAgeMillis(10 * 60 * 1000)
                .setPriority(PRIORITY_HIGH_ACCURACY)
                .build()
            try {
                val location = locationClient.getCurrentLocation(locationRequest, cancellationToken)
                return@withContext location.await()
            } catch (e: SecurityException) {
                Log.e("LocationRemoteDataSource", "Missing location permission", e)
                return@withContext null
            }
        }
    }

    suspend fun getArea(location: Location): String {
        return withContext(Dispatchers.IO) {
            if (geocoder == null) {
                // TODO: Use time?
                return@withContext ""
            }

            var area = ""
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    area = suspendCancellableCoroutine { continuation ->
                        geocoder.getFromLocation(location.latitude, location.longitude, 1) {
                            it.firstOrNull()?.let { address ->
                                continuation.resumeWith(Result.success(getAreaFromAddress(address)))
                            }
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
            return@withContext area
        }
    }

    private fun getAreaFromAddress(address: Address): String {
        return address.subAdminArea ?: address.locality ?: address.adminArea ?: ""
    }
}