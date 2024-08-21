package com.talhaak.apps.simpleprayer

import android.app.Application
import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import com.talhaak.apps.simpleprayer.data.PrayerRepository

private const val APP_PREFERENCES_NAME = "app_preferences"

private val Context.datastore by preferencesDataStore(
    name = APP_PREFERENCES_NAME
)

class MyApplication : Application() {
    lateinit var prayerRepository: PrayerRepository

    override fun onCreate() {
        super.onCreate()
        prayerRepository = PrayerRepository(datastore)
    }
}