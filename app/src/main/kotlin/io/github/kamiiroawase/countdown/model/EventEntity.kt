package io.github.kaimiroawase.countdown.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import io.github.kamiiroawase.nexttime.RepeatUnit
import io.github.kamiiroawase.nexttime.Schedule
import io.github.kamiiroawase.nexttime.anchor
import io.github.kamiiroawase.nexttime.nextTarget
import io.github.kamiiroawase.nexttime.previousTarget
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Instant as KxInstant
import kotlinx.datetime.TimeZone as KxTimeZone

@Entity(tableName = "event")
data class EventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    @ColumnInfo(name = "sort_order")
    val order: Int,
    @ColumnInfo(name = "parent_id")
    val parentId: Long = 0,
    val lunar: Boolean,
    val leapCount: Boolean,
    val notify: Boolean,
    val targetDay: Long,
    @ColumnInfo(name = "target_time_hour")
    val targetHour: Int,
    @ColumnInfo(name = "target_time_minute")
    val targetMinute: Int,
    @ColumnInfo(name = "target_time_second")
    val targetSecond: Int,
    val repeatInterval: Int,
    val repeatUnit: Int,
    @ColumnInfo(name = "ceil_future")
    val ceilFuture: Boolean = true,
    @ColumnInfo(name = "background")
    val background: String = "",
    @ColumnInfo(name = "repeat_end_day")
    val repeatEndDay: Long = -1L,
    @ColumnInfo(name = "repeat_end_hour")
    val repeatEndHour: Int = -1,
    @ColumnInfo(name = "repeat_end_minute")
    val repeatEndMinute: Int = -1,
    @ColumnInfo(name = "repeat_end_second")
    val repeatEndSecond: Int = -1,
) {
    companion object {
        private val targetCache = ConcurrentHashMap<Long, TargetCache>()

        internal fun resolveTarget(
            event: EventEntity,
            now: ZonedDateTime,
            zone: ZoneId,
        ): Pair<ZonedDateTime?, ZonedDateTime?> {
            if (event.id == 0L) return event.nextBoundedTarget(now, zone) to null

            val cached = targetCache[event.id]

            if (cached != null &&
                cached.source == event &&
                cached.zone == zone &&
                (cached.upcoming == null || cached.upcoming.isAfter(now))
            ) {
                return cached.upcoming to cached.ended
            }

            val upcoming = event.nextBoundedTarget(now, zone)

            val ended = if (upcoming == null) event.lastBoundedTarget(zone) else null

            targetCache[event.id] = TargetCache(event, zone, upcoming, ended)

            return upcoming to ended
        }

        internal fun evictTarget(eventId: Long) {
            targetCache.remove(eventId)
        }
    }

    private fun toSchedule(): Schedule =
        Schedule(
            lunar = lunar,
            leapCount = leapCount,
            targetDay = targetDay,
            targetHour = targetHour,
            targetMinute = targetMinute,
            targetSecond = targetSecond,
            repeatInterval = repeatInterval,
            repeatUnit = repeatUnit,
        )

    private fun repeatEndInstant(zone: ZoneId): ZonedDateTime? {
        if (repeatEndDay <= 0 || !isRepeating()) return null

        val date = Instant.ofEpochMilli(repeatEndDay).atZone(ZoneOffset.UTC).toLocalDate()

        val time =
            if (repeatEndHour >= 0 && repeatEndMinute >= 0 && repeatEndSecond >= 0) {
                LocalTime.of(repeatEndHour, repeatEndMinute, repeatEndSecond)
            } else {
                LocalTime.MIN
            }

        return date.atTime(time).atZone(zone)
    }

    private fun isRepeating(): Boolean = repeatInterval > 0 && repeatUnit != RepeatUnit.NONE

    private fun nextBoundedTarget(
        now: ZonedDateTime,
        zone: ZoneId,
    ): ZonedDateTime? =
        toSchedule()
            .nextTarget(
                now.toKxInstant(),
                zone.toKxZone(),
                until = repeatEndInstant(zone)?.toKxInstant(),
            )?.toZdt(zone)

    private fun lastBoundedTarget(zone: ZoneId): ZonedDateTime? {
        val end = repeatEndInstant(zone) ?: return null

        return (
            toSchedule().previousTarget(end.toKxInstant(), zone.toKxZone())
                ?: toSchedule().anchor(zone.toKxZone())
        )?.toZdt(zone)
    }

    private class TargetCache(
        val source: EventEntity,
        val zone: ZoneId,
        val upcoming: ZonedDateTime?,
        val ended: ZonedDateTime?,
    )
}

internal fun ZonedDateTime.toKxInstant(): KxInstant = KxInstant.fromEpochMilliseconds(toInstant().toEpochMilli())

internal fun KxInstant.toZdt(zone: ZoneId): ZonedDateTime = Instant.ofEpochMilli(toEpochMilliseconds()).atZone(zone)

internal fun ZoneId.toKxZone(): KxTimeZone = KxTimeZone.of(id)
