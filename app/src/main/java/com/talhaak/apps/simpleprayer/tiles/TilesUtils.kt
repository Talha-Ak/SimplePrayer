package com.talhaak.apps.simpleprayer.tiles

import android.content.Context
import androidx.wear.protolayout.expression.DynamicBuilders.DynamicInstant
import androidx.wear.protolayout.expression.DynamicBuilders.DynamicString
import com.talhaak.apps.simpleprayer.R
import kotlinx.datetime.DateTimeUnit.Companion.DAY
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import kotlinx.datetime.toKotlinLocalDateTime
import java.time.LocalDateTime

internal fun countdown(context: Context, from: DynamicInstant, to: DynamicInstant): DynamicString {
    val duration = from.durationUntil(to)
    val hours = duration.hoursPart.format().concat(DynamicString.constant("h "))
    val mins = duration.minutesPart.format().concat(DynamicString.constant("m"))

    return DynamicString.onCondition(duration.hoursPart.gt(0)).use(
        hours.concat(mins)
    ).elseUse(
        DynamicString.onCondition(duration.minutesPart.gt(0)).use(
            mins
        ).elseUse(
            context.getString(R.string.now)
        )
    )
}

internal fun nextMidnight() = LocalDateTime.now().toKotlinLocalDateTime().date.plus(1, DAY)
    .atStartOfDayIn(TimeZone.currentSystemDefault())