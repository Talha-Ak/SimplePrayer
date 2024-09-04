package com.talhaak.apps.simpleprayer.tiles.prayerlist

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.wear.protolayout.ActionBuilders
import androidx.wear.protolayout.ColorBuilders.argb
import androidx.wear.protolayout.LayoutElementBuilders.Column
import androidx.wear.protolayout.LayoutElementBuilders.HORIZONTAL_ALIGN_END
import androidx.wear.protolayout.LayoutElementBuilders.HORIZONTAL_ALIGN_START
import androidx.wear.protolayout.LayoutElementBuilders.Layout
import androidx.wear.protolayout.LayoutElementBuilders.LayoutElement
import androidx.wear.protolayout.ModifiersBuilders
import androidx.wear.protolayout.ResourceBuilders
import androidx.wear.protolayout.TimelineBuilders.TimeInterval
import androidx.wear.protolayout.TimelineBuilders.Timeline
import androidx.wear.protolayout.TimelineBuilders.TimelineEntry
import androidx.wear.protolayout.TypeBuilders.StringLayoutConstraint
import androidx.wear.protolayout.TypeBuilders.StringProp
import androidx.wear.protolayout.expression.DynamicBuilders.DynamicInstant
import androidx.wear.protolayout.expression.DynamicBuilders.DynamicString
import androidx.wear.protolayout.material.ChipColors
import androidx.wear.protolayout.material.CompactChip
import androidx.wear.protolayout.material.Text
import androidx.wear.protolayout.material.Typography
import androidx.wear.protolayout.material.layouts.MultiSlotLayout
import androidx.wear.protolayout.material.layouts.PrimaryLayout
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.TileBuilders
import androidx.wear.tiles.tooling.preview.Preview
import androidx.wear.tiles.tooling.preview.TilePreviewData
import androidx.wear.tiles.tooling.preview.TilePreviewHelper
import androidx.wear.tooling.preview.devices.WearDevices
import com.batoulapps.adhan2.Prayer
import com.google.android.horologist.tiles.render.PERMANENT_RESOURCES_VERSION
import com.google.android.horologist.tiles.render.TileLayoutRenderer
import com.talhaak.apps.simpleprayer.R
import com.talhaak.apps.simpleprayer.data.prayer.PrayerDay
import com.talhaak.apps.simpleprayer.data.prayer.allPrayers
import com.talhaak.apps.simpleprayer.data.prayer.getLabelFor
import com.talhaak.apps.simpleprayer.data.prayer.next
import com.talhaak.apps.simpleprayer.data.prayer.toFormattedString
import com.talhaak.apps.simpleprayer.presentation.theme.SimplePrayerTheme
import com.talhaak.apps.simpleprayer.tiles.countdown
import com.talhaak.apps.simpleprayer.tiles.nextMidnight
import kotlinx.datetime.Clock
import kotlinx.datetime.toJavaInstant
import java.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

private val DEFAULT_FRESHNESS_INTERVAL_MILLIS = 1.hours.inWholeMilliseconds

class PrayerListTileRenderer(
    private val context: Context
) : TileLayoutRenderer<PrayerListTileState, Unit> {

    override fun produceRequestedResources(
        resourceState: Unit, requestParams: RequestBuilders.ResourcesRequest
    ): ResourceBuilders.Resources =
        ResourceBuilders.Resources.Builder()
            .setVersion(PERMANENT_RESOURCES_VERSION)
            .build()

    override fun renderTimeline(
        state: PrayerListTileState, requestParams: RequestBuilders.TileRequest
    ): TileBuilders.Tile {
        val timeline = Timeline.Builder()
        timeline.addTimelineEntry(
            TimelineEntry.fromLayoutElement(getLocationMissingLayout(requestParams, context))
        )

        if (state is PrayerListTileState.Valid) {
            allPrayers().forEach { prayer ->
                timeline.addTimelineEntry(
                    TimelineEntry.Builder()
                        .setLayout(
                            mainLayout(
                                state.prayers, state.tomorrow, prayer, requestParams, context
                            )
                        ).setValidity(
                            TimeInterval.Builder()
                                .setEndMillis(
                                    if (prayer == Prayer.ISHA)
                                        nextMidnight().toEpochMilliseconds()
                                    else
                                        state.prayers[prayer.next()].toEpochMilliseconds()
                                )
                                .build()
                        ).build()
                )
            }
        }

        return TileBuilders.Tile.Builder().setResourcesVersion(PERMANENT_RESOURCES_VERSION)
            .setTileTimeline(timeline.build())
            .setFreshnessIntervalMillis(calculateFreshnessIntervalMillis(state))
            .build()
    }
}

private fun getLocationMissingLayout(
    request: RequestBuilders.TileRequest, context: Context
): LayoutElement =
    PrimaryLayout.Builder(request.deviceConfiguration)
        .setResponsiveContentInsetEnabled(true)
        .setPrimaryLabelTextContent(
            Text.Builder(context, context.getString(R.string.no_location))
                .setTypography(Typography.TYPOGRAPHY_TITLE3)
                .setColor(argb(Color.White.toArgb()))
                .build()
        ).setContent(
            Text.Builder(context, context.getString(R.string.prayer_list_tile_no_location))
                .setTypography(Typography.TYPOGRAPHY_BODY2)
                .setMaxLines(3)
                .setColor(argb(Color.White.toArgb()))
                .build()
        ).setPrimaryChipContent(
            CompactChip.Builder(
                context, ModifiersBuilders.Clickable.Builder()
                    .setOnClick(
                        ActionBuilders.LaunchAction.Builder()
                            .setAndroidActivity(
                                ActionBuilders.AndroidActivity.Builder()
                                    .setPackageName("com.talhaak.apps.simpleprayer")
                                    .setClassName("com.talhaak.apps.simpleprayer.presentation.MainActivity")
                                    .build()
                            ).build()
                    ).build(), request.deviceConfiguration
            ).setTextContent(context.getString(R.string.open))
                .setChipColors(ChipColors.primaryChipColors(SimplePrayerTheme.tileColors))
                .build()
        ).build()

private fun mainLayout(
    prayers: PrayerDay,
    tomorrowPrayers: PrayerDay,
    current: Prayer,
    request: RequestBuilders.TileRequest,
    context: Context
): Layout = Layout.fromLayoutElement(
    PrimaryLayout.Builder(request.deviceConfiguration)
        .setResponsiveContentInsetEnabled(true)
        .setPrimaryLabelTextContent(
            Text.Builder(context, context.getString(R.string.prayer_times))
                .setTypography(Typography.TYPOGRAPHY_CAPTION1)
                .setColor(argb(Color.White.toArgb()))
                .build()
        ).apply {
            if (request.deviceConfiguration.screenHeightDp >= 225) {
                val next = current.next()
                setPrimaryChipContent(
                    nextPrayer(
                        context,
                        next,
                        (if (next == Prayer.FAJR) tomorrowPrayers.fajr else prayers[next])
                    )
                )
            }
        }.setContent(
            MultiSlotLayout.Builder()
                .setHorizontalSpacerWidth(16f)
                .addSlotContent(
                    Column.Builder()
                        .setHorizontalAlignment(HORIZONTAL_ALIGN_END)
                        .addContent(
                            contentText(
                                context,
                                context.getString(getLabelFor(Prayer.FAJR)),
                                current == Prayer.FAJR
                            )
                        ).addContent(
                            contentText(
                                context,
                                context.getString(getLabelFor(Prayer.SUNRISE)),
                                current == Prayer.SUNRISE
                            )
                        ).addContent(
                            contentText(
                                context,
                                context.getString(getLabelFor(Prayer.DHUHR)),
                                current == Prayer.DHUHR
                            )
                        ).addContent(
                            contentText(
                                context,
                                context.getString(getLabelFor(Prayer.ASR)),
                                current == Prayer.ASR
                            )
                        ).addContent(
                            contentText(
                                context,
                                context.getString(getLabelFor(Prayer.MAGHRIB)),
                                current == Prayer.MAGHRIB
                            )
                        ).addContent(
                            contentText(
                                context,
                                context.getString(getLabelFor(Prayer.ISHA)),
                                current == Prayer.ISHA
                            )
                        ).build()
                ).addSlotContent(
                    Column.Builder()
                        .setHorizontalAlignment(HORIZONTAL_ALIGN_START)
                        .addContent(
                            contentText(
                                context, prayers.fajr.toFormattedString(), current == Prayer.FAJR
                            )
                        ).addContent(
                            contentText(
                                context,
                                prayers.sunrise.toFormattedString(),
                                current == Prayer.SUNRISE
                            )
                        ).addContent(
                            contentText(
                                context, prayers.dhuhr.toFormattedString(), current == Prayer.DHUHR
                            )
                        ).addContent(
                            contentText(
                                context, prayers.asr.toFormattedString(), current == Prayer.ASR
                            )
                        ).addContent(
                            contentText(
                                context,
                                prayers.maghrib.toFormattedString(),
                                current == Prayer.MAGHRIB
                            )
                        ).addContent(
                            contentText(
                                context, prayers.isha.toFormattedString(), current == Prayer.ISHA
                            )
                        ).build()
                ).build()
        ).build()
)

private fun contentText(context: Context, text: String, current: Boolean): LayoutElement =
    Text.Builder(context, text)
        .setTypography(Typography.TYPOGRAPHY_BODY2)
        .setColor(
            argb(
                if (current) SimplePrayerTheme.tileColors.primary
                else Color.Gray.toArgb()
            )
        ).build()

private fun nextPrayer(
    context: Context,
    prayer: Prayer,
    time: kotlinx.datetime.Instant
): LayoutElement =
    Text.Builder(
        context, StringProp.Builder(
            context.getString(
                R.string.next,
                context.getString(getLabelFor(prayer))
            )
        ).setDynamicValue(
            DynamicString.constant(
                context.getString(
                    R.string.prayer_in,
                    context.getString(getLabelFor(prayer))
                )
            ).concat(
                countdown(
                    context,
                    DynamicInstant.platformTimeWithSecondsPrecision(),
                    DynamicInstant.withSecondsPrecision((time + 1.minutes).toJavaInstant())
                )
            )
        ).build(),
        StringLayoutConstraint.Builder("Maghrib in 88h 88m").build()
    ).setTypography(Typography.TYPOGRAPHY_CAPTION2)
        .setColor(argb(SimplePrayerTheme.tileColors.onSurface))
        .build()

private fun calculateFreshnessIntervalMillis(state: PrayerListTileState): Long {
    if (state !is PrayerListTileState.Valid) return DEFAULT_FRESHNESS_INTERVAL_MILLIS
    // otherwise freshness is based on next day at midnight
    return Duration.between(Clock.System.now().toJavaInstant(), nextMidnight().toJavaInstant())
        .toMillis()
}

@Preview(device = WearDevices.LARGE_ROUND)
@Preview(device = WearDevices.SMALL_ROUND)
@Preview(device = WearDevices.SQUARE)
private fun tilePreview(context: Context) = TilePreviewData(onTileRequest = { request ->
    TilePreviewHelper.singleTimelineEntryTileBuilder(
        mainLayout(
            PrayerDay(
                fajr = Clock.System.now() - 4.hours,
                sunrise = Clock.System.now() - 3.hours,
                dhuhr = Clock.System.now() - 2.hours,
                asr = Clock.System.now() - 5.minutes,
                maghrib = Clock.System.now() + 1.hours,
                isha = Clock.System.now() + 2.hours
            ), PrayerDay(
                fajr = Clock.System.now() - 4.hours,
                sunrise = Clock.System.now() - 3.hours,
                dhuhr = Clock.System.now() - 2.hours,
                asr = Clock.System.now() - 5.minutes,
                maghrib = Clock.System.now() + 1.hours,
                isha = Clock.System.now() + 2.hours
            ), Prayer.ASR, request, context
        )
    ).build()
})

@Preview(device = WearDevices.LARGE_ROUND)
@Preview(device = WearDevices.SMALL_ROUND)
@Preview(device = WearDevices.SQUARE)
private fun noLocationtilePreview(context: Context) = TilePreviewData(onTileRequest = { request ->
    TilePreviewHelper.singleTimelineEntryTileBuilder(
        getLocationMissingLayout(request, context)
    ).build()
})
