package com.talhaak.apps.simpleprayer.data.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.google.android.gms.tasks.CancellationTokenSource
import com.talhaak.apps.simpleprayer.MyApplication
import kotlinx.coroutines.CancellationException
import java.util.concurrent.TimeUnit

class LocationWorker(
    appContext: Context, workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {
    companion object {
        const val TAG = "LocationWorker"
    }

    override suspend fun doWork(): Result {
        if (!hasPermission()) {
            cancelBackgroundLocationUpdates(applicationContext)
            return Result.failure()
        }

        val cancellationTokenSource = CancellationTokenSource()
        return try {
            val result = (applicationContext as MyApplication).locationRepository.updateLocation(
                cancellationTokenSource.token
            )

            if (result) {
                Result.success()
            } else {
                Result.failure()
            }
        } catch (e: CancellationException) {
            cancellationTokenSource.cancel()
            Result.failure()
        }
    }

    private fun hasPermission(): Boolean {
        // Check for the required permission
        return ContextCompat.checkSelfPermission(
            applicationContext, Manifest.permission.ACCESS_BACKGROUND_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
}

fun scheduleBackgroundLocationUpdates(context: Context) {
    val permission =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
    if (permission != PackageManager.PERMISSION_GRANTED) {
        return
    }
    val updateLocationRequest = PeriodicWorkRequestBuilder<LocationWorker>(
        repeatInterval = 6,
        TimeUnit.HOURS
    ).setConstraints(
        Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .build()
    ).build()

    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        LocationWorker.TAG,
        ExistingPeriodicWorkPolicy.KEEP,
        updateLocationRequest
    )
}

public fun cancelBackgroundLocationUpdates(context: Context) {
    WorkManager.getInstance(context).cancelUniqueWork(LocationWorker.TAG)
}