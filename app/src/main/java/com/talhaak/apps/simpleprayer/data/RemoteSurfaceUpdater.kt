package com.talhaak.apps.simpleprayer.data

import android.content.ComponentName
import android.content.Context
import androidx.wear.tiles.TileService
import androidx.wear.tiles.TileUpdateRequester
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceUpdateRequester
import com.talhaak.apps.simpleprayer.complications.NextPrayerComplicationService
import com.talhaak.apps.simpleprayer.tiles.nextprayer.NextPrayerTileService
import com.talhaak.apps.simpleprayer.tiles.prayerlist.PrayerListTileService

class RemoteSurfaceUpdater(
    context: Context
) {
    private val tileUpdateRequester: TileUpdateRequester = TileService.getUpdater(context)
    private val complicationUpdateRequester: ComplicationDataSourceUpdateRequester =
        ComplicationDataSourceUpdateRequester.create(
            context = context,
            complicationDataSourceComponent = ComponentName(
                context,
                NextPrayerComplicationService::class.java
            )
        )

    fun updateRemoteSurfaces() {
        tileUpdateRequester.requestUpdate(NextPrayerTileService::class.java)
        tileUpdateRequester.requestUpdate(PrayerListTileService::class.java)

        complicationUpdateRequester.requestUpdateAll()
    }
}