package io.github.kaimiroawase.countdown.model

import android.content.Context
import com.tyme.solar.SolarDay
import io.github.kaimiroawase.countdown.AppContainer
import io.github.kaimiroawase.countdown.R
import io.github.kamiiroawase.nexttime.CountdownUnit
import io.github.kamiiroawase.nexttime.RepeatUnit
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
    val (upcoming, ended, previous) = EventEntity.resolveTarget(this, now, zone)

    val target =
        upcoming
            ?: ended?.let { end -> return endedItem(context, end, now) }
            ?: return EventItem(this, "", "", "", "", "", "", false)

    todayOccurrence(previous, upcoming, now)?.let { today ->
        return EventItem(
            event = this,
            date = dateText(context, today),
            time = timeText(today),
            week = today.format(AppContainer.weekFormatter),
            prefix = context.getString(R.string.countdown_prefix_future),
            value = "0",
            unit = context.getString(R.string.countdown_unit_day),
            daysValue = 0L,
            targetEpochDay = today.toLocalDate().toEpochDay(),
            past = false,
        )
    }

    calendarDayCount(target, now)?.let { days ->
        return EventItem(
            event = this,
            date = dateText(context, target),
            time = timeText(target),
            week = target.format(AppContainer.weekFormatter),
            prefix = context.getString(R.string.countdown_prefix_future),
            value = days.toString(),
            unit = context.getString(R.string.countdown_unit_day),
            daysValue = days,
            targetEpochDay = target.toLocalDate().toEpochDay(),
            past = false,
        )
    }

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
    val (upcoming, ended, previous) = EventEntity.resolveTarget(this, now, zone)

    val target = upcoming ?: ended ?: return null

    todayOccurrence(previous, upcoming, now)?.let {
        return Triple(
            context.getString(R.string.countdown_prefix_future),
            "0",
            context.getString(R.string.countdown_unit_day),
        )
    }

    calendarDayCount(target, now)?.let { days ->
        return Triple(
            context.getString(R.string.countdown_prefix_future),
            days.toString(),
            context.getString(R.string.countdown_unit_day),
        )
    }

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
                val (upcoming, ended, previous) = EventEntity.resolveTarget(it, now, zone)

                val today = todayOccurrence(previous, upcoming, now)

                AutoSortEntry(
                    event = it,
                    key = today ?: upcoming ?: ended,
                    pending = today != null || (upcoming != null && !upcoming.isBefore(now)),
                )
            }

    val upcomingEntries =
        auto
            .filter { it.key != null && it.pending }
            .sortedBy { it.key }

    val elapsed =
        auto
            .filter { it.key == null || !it.pending }
            .sortedByDescending { it.key }

    return manual + upcomingEntries.map { it.event } + elapsed.map { it.event }
}

private data class AutoSortEntry(
    val event: EventEntity,
    val key: ZonedDateTime?,
    val pending: Boolean,
)

/**
 * 日历天计数（ceilFuture 开启「不足一天算一天」时的展示口径）：目标日距今天
 * ≥1 个日历天时返回日期差 N，整天显示「还有N天」，不再随剩余时长逐小时滚动。
 * 关闭 ceilFuture 或小时/分钟重复（出现间隔不足一天）返回 null，走精确时长口径。
 */
internal fun EventEntity.calendarDayCount(
    target: ZonedDateTime,
    now: ZonedDateTime,
): Long? {
    if (!ceilFuture) return null
    if (repeatUnit == RepeatUnit.HOUR || repeatUnit == RepeatUnit.MINUTE) return null

    val dayDiff = target.toLocalDate().toEpochDay() - now.toLocalDate().toEpochDay()

    return if (dayDiff >= 1L) dayDiff else null
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
