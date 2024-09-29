package com.talhaak.apps.simpleprayer.tiles.nextprayer

import android.content.Context
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.wear.protolayout.ActionBuilders
import androidx.wear.protolayout.ColorBuilders.argb
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.LayoutElementBuilders.Column
import androidx.wear.protolayout.LayoutElementBuilders.LayoutElement
import androidx.wear.protolayout.ModifiersBuilders
import androidx.wear.protolayout.ResourceBuilders
import androidx.wear.protolayout.TimelineBuilders.TimeInterval
import androidx.wear.protolayout.TimelineBuilders.Timeline
import androidx.wear.protolayout.TimelineBuilders.TimelineEntry
import androidx.wear.protolayout.TypeBuilders
import androidx.wear.protolayout.TypeBuilders.StringLayoutConstraint
import androidx.wear.protolayout.expression.DynamicBuilders.DynamicInstant
import androidx.wear.protolayout.material.ChipColors
import androidx.wear.protolayout.material.CircularProgressIndicator
import androidx.wear.protolayout.material.CompactChip
import androidx.wear.protolayout.material.ProgressIndicatorColors
import androidx.wear.protolayout.material.ProgressIndicatorDefaults
import androidx.wear.protolayout.material.Text
import androidx.wear.protolayout.material.Typography
import androidx.wear.protolayout.material.layouts.EdgeContentLayout
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
import com.talhaak.apps.simpleprayer.data.prayer.getLabelFor
import com.talhaak.apps.simpleprayer.data.prayer.toFormattedString
import com.talhaak.apps.simpleprayer.presentation.theme.SimplePrayerTheme
import com.talhaak.apps.simpleprayer.tiles.countdown
import kotlinx.datetime.Clock
import kotlinx.datetime.toJavaInstant
import java.time.Duration
import java.time.Instant
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

private val DEFAULT_FRESHNESS_INTERVAL_MILLIS = 1.hours.inWholeMilliseconds

class NextPrayerTileRenderer(
    private val context: Context
) : TileLayoutRenderer<NextPrayerTileState?, Unit> {

    override fun produceRequestedResources(
        resourceState: Unit,
        requestParams: RequestBuilders.ResourcesRequest
    ): ResourceBuilders.Resources =
        ResourceBuilders.Resources.Builder().setVersion(PERMANENT_RESOURCES_VERSION).build()

    override fun renderTimeline(
        state: NextPrayerTileState?,
        requestParams: RequestBuilders.TileRequest
    ): TileBuilders.Tile {
        val timeline = Timeline.Builder()
        timeline.addTimelineEntry(
            TimelineEntry.fromLayoutElement(getLocationMissingLayout(requestParams, context))
        )

        state?.prayers?.forEach { prayer ->
            Log.d("NextPrayerTileRenderer", "renderTimeline: $prayer")
            timeline.addTimelineEntry(
                TimelineEntry.Builder()
                    .setLayout(mainLayout(prayer, state.loc, requestParams, context))
                    .setValidity(
                        TimeInterval.Builder()
                            .setEndMillis(prayer.dueAt.toEpochMilliseconds())
                            .build()
                    ).build()
            )
        }

        return TileBuilders.Tile.Builder()
            .setResourcesVersion(PERMANENT_RESOURCES_VERSION)
            .setTileTimeline(timeline.build())
            .setFreshnessIntervalMillis(calculateFreshnessIntervalMillis(state))
            .build()
    }
}

private fun getLocationMissingLayout(
    request: RequestBuilders.TileRequest,
    context: Context
): LayoutElement =
    PrimaryLayout.Builder(request.deviceConfiguration)
        .setResponsiveContentInsetEnabled(true)
        .setPrimaryLabelTextContent(
            Text.Builder(context, context.getString(R.string.no_location))
                .setTypography(Typography.TYPOGRAPHY_TITLE3)
                .setColor(argb(Color.White.toArgb()))
                .build()
        )
        .setContent(
            Text.Builder(context, context.getString(R.string.next_prayer_tile_no_location))
                .setTypography(Typography.TYPOGRAPHY_BODY2)
                .setMaxLines(3)
                .setColor(argb(Color.White.toArgb()))
                .build()
        )
        .setPrimaryChipContent(
            CompactChip.Builder(
                context,
                ModifiersBuilders.Clickable.Builder().setOnClick(
                    ActionBuilders.LaunchAction.Builder().setAndroidActivity(
                        ActionBuilders.AndroidActivity.Builder()
                            .setPackageName("com.talhaak.apps.simpleprayer")
                            .setClassName("com.talhaak.apps.simpleprayer.presentation.MainActivity")
                            .build()
                    ).build()
                ).build(),
                request.deviceConfiguration
            )
                .setTextContent(context.getString(R.string.open))
                .setChipColors(ChipColors.primaryChipColors(SimplePrayerTheme.tileColors))
                .build()
        )
        .build()

private fun mainLayout(
    state: NextPrayerTileEntry,
    location: String,
    request: RequestBuilders.TileRequest,
    context: Context
): LayoutElementBuilders.Layout {
    val current = DynamicInstant.platformTimeWithSecondsPrecision()
    val start = DynamicInstant.withSecondsPrecision(state.startFrom.toJavaInstant())
    val dueAt = DynamicInstant.withSecondsPrecision((state.dueAt).toJavaInstant())
    val dueAtPlusOne =
        DynamicInstant.withSecondsPrecision((state.dueAt + 1.minutes).toJavaInstant())

    return LayoutElementBuilders.Layout.fromLayoutElement(
        EdgeContentLayout.Builder(request.deviceConfiguration)
            .setResponsiveContentInsetEnabled(true)
            .setEdgeContent(
                CircularProgressIndicator.Builder()
                    .setProgress(
                        TypeBuilders.FloatProp.Builder(0f)
                            .setDynamicValue(
                                (dueAt.durationUntil(current).toIntMinutes().asFloat() * -1f /
                                        start.durationUntil(dueAt).toIntMinutes()).animate()
                            ).build()
                    )
                    .setStartAngle(ProgressIndicatorDefaults.GAP_START_ANGLE)
                    .setEndAngle(ProgressIndicatorDefaults.GAP_END_ANGLE)
                    .setCircularProgressIndicatorColors(
                        ProgressIndicatorColors.progressIndicatorColors(SimplePrayerTheme.tileColors)
                    )
                    .build()
            )
            .setPrimaryLabelTextContent(
                Column.Builder().addContent(
                    Text.Builder(
                        context,
                        context.getString(
                            R.string.prayer_in,
                            context.getString(getLabelFor(state.prayer))
                        )
                    )
                        .setTypography(Typography.TYPOGRAPHY_CAPTION1)
                        .setColor(argb(Color.White.toArgb()))
                        .build()
                ).addContent(
                    Text.Builder(
                        context,
                        location
                    )
                        .setTypography(Typography.TYPOGRAPHY_CAPTION3)
                        .setColor(argb(Color.White.toArgb()))
                        .build()
                ).build()
            )
            .setContent(
                Text.Builder(
                    context,
                    TypeBuilders.StringProp.Builder(state.dueAt.toFormattedString())
                        .setDynamicValue(countdown(context, current, dueAtPlusOne))
                        .build(),
                    StringLayoutConstraint.Builder("88h 88m").build()
                )
                    .setTypography(
                        if (request.deviceConfiguration.screenWidthDp >= 225) {
                            Typography.TYPOGRAPHY_DISPLAY1
                        } else {
                            Typography.TYPOGRAPHY_DISPLAY2
                        }
                    )
                    .setColor(argb(Color.White.toArgb()))
                    .build()
            )
            .setSecondaryLabelTextContent(
                Text.Builder(context, state.dueAt.toFormattedString())
                    .setTypography(Typography.TYPOGRAPHY_CAPTION1)
                    .setColor(argb(Color.White.toArgb()))
                    .build()
            )
            .build()
    )
}

private fun calculateFreshnessIntervalMillis(state: NextPrayerTileState?): Long {
    return state?.prayers?.lastOrNull()?.let {
        Duration.between(Instant.now(), it.dueAt.toJavaInstant()).toMillis()
    } ?: DEFAULT_FRESHNESS_INTERVAL_MILLIS
}

@Preview(device = WearDevices.LARGE_ROUND)
@Preview(device = WearDevices.SMALL_ROUND)
@Preview(device = WearDevices.SQUARE)
private fun tilePreview(context: Context) = TilePreviewData(
    onTileRequest = { request ->
        TilePreviewHelper.singleTimelineEntryTileBuilder(
            mainLayout(
                NextPrayerTileEntry(
                    Prayer.MAGHRIB,
                    Clock.System.now() - 1.hours,
                    Clock.System.now() + 3.hours
                ), "London", request, context
            )
        ).build()
    }
)

@Preview(device = WearDevices.LARGE_ROUND)
@Preview(device = WearDevices.SMALL_ROUND)
@Preview(device = WearDevices.SQUARE)
private fun noLocationtilePreview(context: Context) = TilePreviewData(
    onTileRequest = { request ->
        TilePreviewHelper.singleTimelineEntryTileBuilder(
            getLocationMissingLayout(request, context)
        ).build()
    }
)
