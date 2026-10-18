package io.github.kaimiroawase.countdown.model

import android.content.Context
import com.tyme.solar.SolarDay
import io.github.kaimiroawase.countdown.AppContainer
import io.github.kaimiroawase.countdown.R
import io.github.kamiiroawase.nexttime.CountdownUnit
import io.github.kamiiroawase.nexttime.Rounding
import io.github.kamiiroawase.nexttime.countdown
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

fun EventEntity.infoItem(
    context: Context,
    zone: ZoneId = AppContainer.zoneId,
    now: ZonedDateTime = ZonedDateTime.now(zone),
): EventItem {
    val (upcoming, ended) = EventEntity.resolveTarget(this, now, zone)

    val target =
        upcoming
            ?: ended?.let { end -> return endedItem(context, end, now) }
            ?: return EventItem(this, "", "", "", "", "", "", false)

    val countdown =
        countdown(
            target.toKxInstant(),
            now.toKxInstant(),
            if (ceilFuture) Rounding.CEIL_FUTURE else Rounding.TRUNCATE,
        )

    return EventItem(
        event = this,
        date = dateText(context, target),
        time = timeText(target),
        week = target.format(AppContainer.weekFormatter),
        prefix =
            context.getString(
                if (countdown.past) R.string.countdown_prefix_past else R.string.countdown_prefix_future,
            ),
        value = countdown.value.toString(),
        unit = unitText(context, countdown.unit),
        daysValue = if (countdown.unit == CountdownUnit.DAYS) countdown.value else 0L,
        targetEpochDay = target.toLocalDate().toEpochDay(),
        past = countdown.past,
    )
}

fun EventEntity.daysCountdownTexts(
    context: Context,
    zone: ZoneId = AppContainer.zoneId,
    now: ZonedDateTime = ZonedDateTime.now(zone),
): Triple<String, String, String>? {
    val (upcoming, ended) = EventEntity.resolveTarget(this, now, zone)

    val target = upcoming ?: ended ?: return null

    val duration = Duration.between(now.toInstant(), target.toInstant())

    val countdown =
        countdown(
            target.toKxInstant(),
            now.toKxInstant(),
            if (ceilFuture) Rounding.CEIL_FUTURE else Rounding.TRUNCATE,
        )

    val days =
        if (countdown.unit == CountdownUnit.DAYS) countdown.value else duration.abs().toDays()

    return Triple(
        context.getString(
            if (countdown.past) R.string.countdown_prefix_past else R.string.countdown_prefix_future,
        ),
        days.toString(),
        context.getString(R.string.countdown_unit_day),
    )
}

fun List<EventEntity>.sortedForDisplay(
    zone: ZoneId = AppContainer.zoneId,
    now: ZonedDateTime = ZonedDateTime.now(zone),
): List<EventEntity> {
    val manual =
        this
            .filter { it.order > 0 }
            .sortedWith(compareByDescending<EventEntity> { it.order }.thenByDescending { it.id })

    val auto =
        this
            .filter { it.order == 0 }
            .map {
                val (upcoming, ended) = EventEntity.resolveTarget(it, now, zone)

                it to (upcoming ?: ended)
            }

    val upcoming =
        auto
            .filter { it.second != null && !it.second!!.isBefore(now) }
            .sortedBy { it.second }

    val elapsed =
        auto
            .filter { it.second == null || it.second!!.isBefore(now) }
            .sortedByDescending { it.second }

    return manual + upcoming.map { it.first } + elapsed.map { it.first }
}

fun lunarDateText(
    context: Context,
    date: LocalDate,
): String {
    val lunarDay =
        SolarDay.fromYmd(date.year, date.monthValue, date.dayOfMonth).getLunarDay()

    val month = lunarDay.getLunarMonth().getName().removeSuffix("月")

    return context.getString(
        R.string.event_lunar_date,
        lunarDay.year.toString(),
        when {
            month.endsWith("十一") -> month.removeSuffix("十一") + "冬"
            month.endsWith("十二") -> month.removeSuffix("十二") + "腊"
            else -> month
        },
        lunarDay.getName(),
    )
}

private fun EventEntity.endedItem(
    context: Context,
    end: ZonedDateTime,
    now: ZonedDateTime,
): EventItem {
    val countdown = countdown(end.toKxInstant(), now.toKxInstant())

    return EventItem(
        event = this,
        date = dateText(context, end),
        time = timeText(end),
        week = end.format(AppContainer.weekFormatter),
        prefix = context.getString(R.string.countdown_prefix_past),
        value = countdown.value.toString(),
        unit = unitText(context, countdown.unit),
        daysValue = if (countdown.unit == CountdownUnit.DAYS) countdown.value else 0L,
        targetEpochDay = end.toLocalDate().toEpochDay(),
        past = true,
    )
}

private fun EventEntity.timeText(target: ZonedDateTime): String {
    if (targetHour < 0 || targetMinute < 0 || targetSecond < 0) return ""

    return AppContainer.TIME_TEXT_FORMAT.format(target.hour, target.minute, target.second)
}

private fun EventEntity.unitText(
    context: Context,
    unit: CountdownUnit,
): String =
    context.getString(
        when (unit) {
            CountdownUnit.DAYS -> R.string.countdown_unit_day
            CountdownUnit.HOURS -> R.string.countdown_unit_hour
            CountdownUnit.MINUTES -> R.string.countdown_unit_minute
            CountdownUnit.SECONDS -> R.string.countdown_unit_second
        },
    )

private fun EventEntity.dateText(
    context: Context,
    target: ZonedDateTime,
): String {
    if (!lunar) return target.toLocalDate().format(AppContainer.dateFormatter)

    return lunarDateText(context, target.toLocalDate())
}
