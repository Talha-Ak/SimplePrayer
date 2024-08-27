package com.talhaak.apps.simpleprayer

import android.app.Application
import android.content.Context
import android.location.Geocoder
import androidx.datastore.preferences.preferencesDataStore
import com.google.android.gms.location.LocationServices
import com.talhaak.apps.simpleprayer.data.location.LocationLocalDataSource
import com.talhaak.apps.simpleprayer.data.location.LocationRemoteDataSource
import com.talhaak.apps.simpleprayer.data.location.LocationRepository

private const val APP_PREFERENCES_NAME = "app_preferences"

private val Context.datastore by preferencesDataStore(
    name = APP_PREFERENCES_NAME
)

class MyApplication : Application() {
    lateinit var locationRepository: LocationRepository

    override fun onCreate() {
        super.onCreate()

        val locationClient = LocationServices.getFusedLocationProviderClient(this)
        val geocoder = if (Geocoder.isPresent()) Geocoder(this) else null
        val remoteDataSource = LocationRemoteDataSource(locationClient, geocoder)
        val localDataSource = LocationLocalDataSource(datastore)
        locationRepository = LocationRepository(localDataSource, remoteDataSource)
    }
}