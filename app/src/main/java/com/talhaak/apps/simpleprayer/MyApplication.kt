package com.talhaak.apps.simpleprayer

import android.app.Application
import android.content.Context
import android.location.Geocoder
import androidx.datastore.preferences.preferencesDataStore
import com.google.android.gms.location.LocationServices
import com.talhaak.apps.simpleprayer.data.location.LocationLocalDataSource
import com.talhaak.apps.simpleprayer.data.location.LocationRemoteDataSource
import com.talhaak.apps.simpleprayer.data.location.LocationRepository
import com.talhaak.apps.simpleprayer.data.userprefs.UserPreferencesRepository

private const val LOCATION_DATASTORE_NAME = "location_datastore"
private const val USER_PREFS_DATASTORE_NAME = "user_prefs_datastore"

private val Context.locationDatastore by preferencesDataStore(
    name = LOCATION_DATASTORE_NAME
)

private val Context.userPrefsDatastore by preferencesDataStore(
    name = USER_PREFS_DATASTORE_NAME
)

class MyApplication : Application() {
    lateinit var locationRepository: LocationRepository
    lateinit var userPreferencesRepository: UserPreferencesRepository

    override fun onCreate() {
        super.onCreate()

        val locationClient = LocationServices.getFusedLocationProviderClient(this)
        val geocoder = if (Geocoder.isPresent()) Geocoder(this) else null
        val remoteDataSource = LocationRemoteDataSource(locationClient, geocoder)
        val localDataSource = LocationLocalDataSource(locationDatastore)
        locationRepository = LocationRepository(localDataSource, remoteDataSource)

        userPreferencesRepository = UserPreferencesRepository(userPrefsDatastore)
    }
}