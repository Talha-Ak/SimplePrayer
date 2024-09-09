package com.talhaak.apps.simpleprayer.complications

import android.graphics.drawable.Icon
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.wear.protolayout.expression.DynamicBuilders.DynamicInstant
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationText
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.CountDownTimeReference
import androidx.wear.watchface.complications.data.LongTextComplicationData
import androidx.wear.watchface.complications.data.MonochromaticImage
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.RangedValueComplicationData
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import androidx.wear.watchface.complications.data.TimeDifferenceComplicationText
import androidx.wear.watchface.complications.data.TimeDifferenceStyle
import androidx.wear.watchface.complications.datasource.ComplicationDataTimeline
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingTimelineComplicationDataSourceService
import androidx.wear.watchface.complications.datasource.TimeInterval
import androidx.wear.watchface.complications.datasource.TimelineEntry
import com.batoulapps.adhan2.Prayer
import com.talhaak.apps.simpleprayer.MyApplication
import com.talhaak.apps.simpleprayer.R
import com.talhaak.apps.simpleprayer.data.location.LocationRepository
import com.talhaak.apps.simpleprayer.data.location.StoredLocation
import com.talhaak.apps.simpleprayer.data.prayer.allPrayers
import com.talhaak.apps.simpleprayer.data.prayer.getLabelFor
import com.talhaak.apps.simpleprayer.data.prayer.getPrayerTimes
import com.talhaak.apps.simpleprayer.data.prayer.getShortLabelFor
import com.talhaak.apps.simpleprayer.data.prayer.prev
import com.talhaak.apps.simpleprayer.data.prayer.toPrayerDay
import com.talhaak.apps.simpleprayer.data.userprefs.UserPreferences
import com.talhaak.apps.simpleprayer.data.userprefs.UserPreferencesRepository
import com.talhaak.apps.simpleprayer.presentation.openActivity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toKotlinInstant
import java.time.Instant
import kotlin.time.Duration.Companion.days

class NextPrayerComplicationService : SuspendingTimelineComplicationDataSourceService() {
    private lateinit var locationRepository: LocationRepository
    private lateinit var userPreferencesRepository: UserPreferencesRepository
    private lateinit var tileStateFlow: Flow<NextPrayerComplicationState>

    override fun onCreate() {
        super.onCreate()
        locationRepository = (application as MyApplication).locationRepository
        userPreferencesRepository = (application as MyApplication).userPreferencesRepository
        tileStateFlow = combine(
            locationRepository.lastLocationFlow, userPreferencesRepository.userPrefsFlow
        ) { location, prefs ->
            when (location) {
                is StoredLocation.None, is StoredLocation.Invalid -> {
                    NextPrayerComplicationState(
                        emptyList()
                    )
                }

                is StoredLocation.Valid -> getPrayerList(Instant.now(), location, prefs)
            }
        }
    }

    override fun getPreviewData(type: ComplicationType): ComplicationData? {
        return when (type) {
            ComplicationType.SHORT_TEXT ->
                shortText(
                    title = getString(getShortLabelFor(Prayer.DHUHR)),
                    text = "1h 43m"
                ).build()

            ComplicationType.LONG_TEXT ->
                longText(
                    title = getString(
                        R.string.now_prayer,
                        getString(getLabelFor(Prayer.SUNRISE))
                    ),
                    text = getString(
                        R.string.prayer_colon_time,
                        getString(getLabelFor(Prayer.DHUHR)),
                        "1h 43m"
                    )
                ).build()

            ComplicationType.RANGED_VALUE ->
                rangedText(
                    title = getString(getShortLabelFor(Prayer.DHUHR)),
                    text = "1hr 43m",
                    noTimeProgress = 0.45f
                ).build()

            else -> {
                Log.e("NextPrayerComplicationService", "Unsupported complication type: $type")
                null
            }
        }
    }

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationDataTimeline? {
        val state = tileStateFlow.firstOrNull()
        if (state == null || state.prayers.isEmpty()) {
            return ComplicationDataTimeline(
                timelineEntries = emptyList(),
                defaultComplicationData = when (request.complicationType) {
                    ComplicationType.SHORT_TEXT ->
                        shortText(
                            text = getString(R.string.update),
                            icon = R.drawable.baseline_location_off_24
                        ).build()

                    ComplicationType.LONG_TEXT ->
                        longText(
                            title = getString(R.string.no_location),
                            text = getString(R.string.open_app_to_update),
                            icon = R.drawable.baseline_location_off_24
                        ).build()

                    ComplicationType.RANGED_VALUE ->
                        rangedText(
                            text = getString(R.string.update),
                            icon = R.drawable.baseline_location_off_24
                        ).build()

                    else -> throw IllegalArgumentException()
                }
            )
        }

        return when (request.complicationType) {
            ComplicationType.SHORT_TEXT -> ComplicationDataTimeline(
                defaultComplicationData = shortText(icon = R.drawable.ic_logo).build(),
                timelineEntries = state.prayers.map { current ->
                    TimelineEntry(
                        validity = current.timeInterval,
                        complicationData = shortText(
                            title = getString(getShortLabelFor(current.prayer)),
                            time = current.timeInterval.end
                        ).build()
                    )
                }
            )

            ComplicationType.LONG_TEXT -> ComplicationDataTimeline(
                defaultComplicationData = longText().build(),
                timelineEntries = state.prayers.map { current ->
                    TimelineEntry(
                        validity = current.timeInterval,
                        complicationData = longText(
                            title = getString(
                                R.string.now_prayer,
                                getString(getLabelFor(current.prayer.prev())),
                            ),
                            time = current.timeInterval.end,
                            text = getString(
                                R.string.prayer_colon_time,
                                getString(getLabelFor(current.prayer)),
                                "^1"
                            )
                        ).build(),
                    )
                }
            )

            ComplicationType.RANGED_VALUE -> ComplicationDataTimeline(
                defaultComplicationData = rangedText(icon = R.drawable.ic_logo).build(),
                timelineEntries = state.prayers.map { current ->
                    TimelineEntry(
                        validity = current.timeInterval,
                        complicationData = rangedText(
                            title = getString(getShortLabelFor(current.prayer)),
                            start = current.timeInterval.start,
                            end = current.timeInterval.end
                        ).build()
                    )
                }
            )

            else -> null
        }
    }

    private fun shortText(
        title: String = "",
        text: String = "",
        time: Instant? = null,
        @DrawableRes icon: Int? = R.drawable.ic_logo
    ): ShortTextComplicationData.Builder {
        val timeText = time?.let {
            TimeDifferenceComplicationText.Builder(
                style = TimeDifferenceStyle.SHORT_DUAL_UNIT,
                countDownTimeReference = CountDownTimeReference(it)
            ).build()
        }

        return ShortTextComplicationData.Builder(
            text = timeText ?: PlainComplicationText.Builder(text).build(),
            contentDescription = timeText ?: PlainComplicationText.Builder(text).build()
        )
            .setTapAction(this.openActivity())
            .apply {
                if (title.isNotEmpty()) setTitle(
                    PlainComplicationText.Builder(title).build()
                )
                if (icon != null) setMonochromaticImage(
                    MonochromaticImage.Builder(
                        Icon.createWithResource(this@NextPrayerComplicationService, icon)
                    ).build()
                )
            }
    }

    private fun longText(
        title: String = "",
        text: String = "",
        time: Instant? = null,
        @DrawableRes icon: Int = R.drawable.ic_logo
    ): LongTextComplicationData.Builder {
        val timeText = if (time != null) {
            TimeDifferenceComplicationText.Builder(
                style = TimeDifferenceStyle.SHORT_DUAL_UNIT,
                countDownTimeReference = CountDownTimeReference(time)
            )
                .setText(text)
                .build()
        } else null

        return LongTextComplicationData.Builder(
            text = timeText ?: PlainComplicationText.Builder(text).build(),
            contentDescription = timeText ?: PlainComplicationText.Builder(text).build()
        )
            .setTitle(
                PlainComplicationText.Builder(title).build()
            )
            .setTapAction(this.openActivity())
            .setMonochromaticImage(
                MonochromaticImage.Builder(
                    Icon.createWithResource(this, icon)
                ).build()
            )
    }

    private fun rangedText(
        title: String = "",
        text: String = "",
        start: Instant? = null,
        end: Instant? = null,
        noTimeProgress: Float = 1f,
        @DrawableRes icon: Int? = R.drawable.ic_logo
    ): RangedValueComplicationData.Builder {
        val builder = if (start != null && end != null) {
            val dynTime = DynamicInstant.platformTimeWithSecondsPrecision()
            val dynStart = DynamicInstant.withSecondsPrecision(start)
            val dynEnd = DynamicInstant.withSecondsPrecision(end)

            val timeText = TimeDifferenceComplicationText.Builder(
                style = TimeDifferenceStyle.SHORT_DUAL_UNIT,
                countDownTimeReference = CountDownTimeReference(end)
            ).build()

            RangedValueComplicationData.Builder(
                dynamicValue =
                (dynEnd.durationUntil(dynTime).toIntMinutes().asFloat() * -1f /
                        dynStart.durationUntil(dynEnd).toIntMinutes()).animate(),
                fallbackValue = 1f,
                min = 0f,
                max = 1f,
                contentDescription = timeText
            ).setText(timeText)
        } else {
            RangedValueComplicationData.Builder(
                value = noTimeProgress,
                min = 0f,
                max = 1f,
                contentDescription = ComplicationText.EMPTY
            ).setText(PlainComplicationText.Builder(text).build())
        }

        return builder
            .setTapAction(this.openActivity())
            .apply {
                if (title.isNotEmpty()) setTitle(
                    PlainComplicationText.Builder(title).build()
                )
                if (icon != null) setMonochromaticImage(
                    MonochromaticImage.Builder(
                        Icon.createWithResource(this@NextPrayerComplicationService, icon)
                    ).build()
                )
            }
    }
}

fun getPrayerList(
    time: Instant, location: StoredLocation.Valid, prefs: UserPreferences
): NextPrayerComplicationState {
    val kTime = time.toKotlinInstant()
    val today = getPrayerTimes(kTime, location, prefs).toPrayerDay()
    val tomorrow = getPrayerTimes(kTime + 1.days, location, prefs).toPrayerDay()

    val entries = mutableListOf<NextPrayerComplicationEntry>()
    allPrayers().forEach { current ->
        if (current == Prayer.FAJR) {
            if (today[Prayer.FAJR] > kTime) entries.add(
                NextPrayerComplicationEntry(
                    current,
                    TimeInterval(time, today.fajr.toJavaInstant())
                )
            )
            entries.add(
                NextPrayerComplicationEntry(
                    current,
                    TimeInterval(today.isha.toJavaInstant(), tomorrow.fajr.toJavaInstant())
                )
            )
        } else {
            entries.add(
                NextPrayerComplicationEntry(
                    current,
                    TimeInterval(
                        today[current.prev()].toJavaInstant(),
                        today[current].toJavaInstant()
                    )
                )
            )
            entries.add(
                NextPrayerComplicationEntry(
                    current,
                    TimeInterval(
                        tomorrow[current.prev()].toJavaInstant(),
                        tomorrow[current].toJavaInstant()
                    )
                )
            )
        }
    }

    return NextPrayerComplicationState(entries.filter { it.timeInterval.end > time })
}
