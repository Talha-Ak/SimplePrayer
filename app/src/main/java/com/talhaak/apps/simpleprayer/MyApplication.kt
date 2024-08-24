package com.talhaak.apps.simpleprayer

import android.app.Application
import android.content.Context
import android.location.Geocoder
import androidx.datastore.preferences.preferencesDataStore
import com.google.android.gms.location.LocationServices
import com.talhaak.apps.simpleprayer.data.LocationRemoteDataSource
import com.talhaak.apps.simpleprayer.data.PrayerRepository

private const val APP_PREFERENCES_NAME = "app_preferences"

private val Context.datastore by preferencesDataStore(
    name = APP_PREFERENCES_NAME
)

class MyApplication : Application() {
    lateinit var prayerRepository: PrayerRepository

    override fun onCreate() {
        super.onCreate()
        val locationClient = LocationServices.getFusedLocationProviderClient(this)
        val dataSource = LocationRemoteDataSource(locationClient, Geocoder(this))
        prayerRepository = PrayerRepository(datastore, dataSource)
    }
}