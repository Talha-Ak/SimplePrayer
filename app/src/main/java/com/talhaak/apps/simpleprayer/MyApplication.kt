package com.talhaak.apps.simpleprayer

import android.app.Application
import android.location.Geocoder
import com.google.android.gms.location.LocationServices
import com.talhaak.apps.simpleprayer.data.RemoteSurfaceUpdater
import com.talhaak.apps.simpleprayer.data.location.LocationLocalDataSource
import com.talhaak.apps.simpleprayer.data.location.LocationRemoteDataSource
import com.talhaak.apps.simpleprayer.data.location.LocationRepository
import com.talhaak.apps.simpleprayer.data.location.locationDatastore
import com.talhaak.apps.simpleprayer.data.userprefs.UserPreferencesRepository
import com.talhaak.apps.simpleprayer.data.userprefs.userPrefsDatastore

class MyApplication : Application() {
    lateinit var locationRepository: LocationRepository
    lateinit var userPreferencesRepository: UserPreferencesRepository

    override fun onCreate() {
        super.onCreate()

        val locationClient = LocationServices.getFusedLocationProviderClient(this)
        val geocoder = if (Geocoder.isPresent()) Geocoder(this) else null
        val remoteDataSource = LocationRemoteDataSource(locationClient, geocoder)
        val localDataSource = LocationLocalDataSource(locationDatastore)

        val remoteSurfaceUpdater = RemoteSurfaceUpdater(this)

        locationRepository =
            LocationRepository(localDataSource, remoteDataSource, remoteSurfaceUpdater)
        userPreferencesRepository =
            UserPreferencesRepository(userPrefsDatastore, remoteSurfaceUpdater)

    }
}