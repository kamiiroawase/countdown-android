package io.github.kaimiroawase.countdown.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import io.github.kaimiroawase.countdown.AppContainer
import io.github.kaimiroawase.countdown.BuildConfig
import io.github.kaimiroawase.countdown.model.EventEntity
import io.github.kaimiroawase.countdown.ui.activity.MainActivity
import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime

internal object WidgetScheduler {
    const val SELF_CHECK_MILLIS = 60 * 60 * 1000L

    fun updateAction(providerClass: Class<*>): String = BuildConfig.APPLICATION_ID + ".action.UPDATE_WIDGETS.${providerClass.simpleName}"

    fun nextRefreshMillis(
        events: List<EventEntity>,
        zone: ZoneId = AppContainer.zoneId,
        now: ZonedDateTime = ZonedDateTime.now(zone),
    ): Long? {
        val next =
            events
                .mapNotNull { event ->
                    val (upcoming, ended) = EventEntity.resolveTarget(event, now, zone)

                    val target = upcoming ?: ended ?: return@mapNotNull null

                    nextDayBoundaryMillis(now, target)
                }.minOrNull() ?: return null

        return minOf(next, now.toInstant().toEpochMilli() + SELF_CHECK_MILLIS)
    }

    private fun nextDayBoundaryMillis(
        now: ZonedDateTime,
        target: ZonedDateTime,
    ): Long {
        val duration = Duration.between(now.toInstant(), target.toInstant())

        val boundary =
            if (!duration.isNegative) {
                val days = duration.toDays()

                val remainder = duration - Duration.ofDays(days)

                target
                    .minus(Duration.ofDays(days))
                    .plus(if (remainder.isZero) Duration.ofDays(1) else Duration.ZERO)
            } else {
                target.plus(Duration.ofDays(duration.negated().toDays() + 1))
            }

        return boundary.plusSeconds(1).toInstant().toEpochMilli()
    }

    fun scheduleUpdates(
        context: Context,
        providerClass: Class<*>,
        triggerAtMillis: Long?,
    ) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)

        val pendingIntent = updatePendingIntent(context, providerClass)

        if (triggerAtMillis == null) {
            alarmManager.cancel(pendingIntent)

            return
        }

        if (!alarmManager.canScheduleExactAlarms()) {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent,
            )
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent,
            )
        }
    }

    fun scheduleAllUpdates(
        context: Context,
        triggerAtMillis: Long?,
    ) {
        scheduleUpdates(context, EventCardWidgetProvider::class.java, triggerAtMillis)

        scheduleUpdates(context, EventListWidgetProvider::class.java, triggerAtMillis)
    }

    fun cancelUpdates(
        context: Context,
        providerClass: Class<*>,
    ) {
        context
            .getSystemService(AlarmManager::class.java)
            .cancel(updatePendingIntent(context, providerClass))
    }

    fun openAppPendingIntent(
        context: Context,
        appWidgetId: Int,
    ): PendingIntent =
        PendingIntent.getActivity(
            context,
            appWidgetId,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

    private fun updatePendingIntent(
        context: Context,
        providerClass: Class<*>,
    ): PendingIntent =
        PendingIntent.getBroadcast(
            context,
            providerClass.name.hashCode(),
            Intent(context, providerClass).setAction(updateAction(providerClass)),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

    fun widgetIds(
        context: Context,
        providerClass: Class<*>,
    ): IntArray =
        AppWidgetManager
            .getInstance(context)
            .getAppWidgetIds(ComponentName(context, providerClass))
}
