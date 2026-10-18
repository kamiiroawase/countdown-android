package io.github.kaimiroawase.countdown.notify

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import io.github.kaimiroawase.countdown.AppContainer
import io.github.kaimiroawase.countdown.R
import io.github.kaimiroawase.countdown.model.EventEntity
import io.github.kaimiroawase.countdown.ui.activity.EventDetailActivity
import io.github.kaimiroawase.countdown.ui.activity.SubEventDetailActivity
import io.github.kaimiroawase.countdown.ui.components.showToast
import java.time.ZonedDateTime

object NotifyScheduler {
    private const val CHANNEL_EVENT = "event_notify"

    fun syncAll(
        context: Context,
        events: List<EventEntity>,
    ) {
        events.forEach { syncEvent(context, it) }
    }

    fun syncEvent(
        context: Context,
        event: EventEntity,
    ) {
        val appContext = context.applicationContext

        val alarmManager = appContext.getSystemService(AlarmManager::class.java)

        val pendingIntent = notifyPendingIntent(appContext, event.id)

        val zone = AppContainer.zoneId

        val now = ZonedDateTime.now(zone)

        val target = if (event.notify) EventEntity.resolveTarget(event, now, zone).first else null

        if (target == null || !target.isAfter(now)) {
            alarmManager.cancel(pendingIntent)

            return
        }

        val triggerAtMillis = target.toInstant().toEpochMilli()

        if (!alarmManager.canScheduleExactAlarms()) {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent,
            )
        } else {
            // setAlarmClock 被系统当作用户闹钟,Doze 与厂商省电策略都不会延迟它
            alarmManager.setAlarmClock(
                AlarmManager.AlarmClockInfo(triggerAtMillis, null),
                pendingIntent,
            )
        }
    }

    fun cancel(
        context: Context,
        eventId: Long,
    ) {
        context.applicationContext
            .getSystemService(AlarmManager::class.java)
            .cancel(notifyPendingIntent(context.applicationContext, eventId))
    }

    fun postEventNotification(
        context: Context,
        event: EventEntity,
    ) {
        val appContext = context.applicationContext

        if (!NotificationManagerCompat.from(appContext).areNotificationsEnabled()) return

        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(
                appContext,
                Manifest.permission.POST_NOTIFICATIONS,
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        ensureChannel(appContext)

        val contentIntent =
            PendingIntent.getActivity(
                appContext,
                event.id.toInt(),
                Intent(
                    appContext,
                    if (event.parentId == 0L) {
                        EventDetailActivity::class.java
                    } else {
                        SubEventDetailActivity::class.java
                    },
                ).putExtra(EventDetailActivity.EXTRA_ID, event.id),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )

        val notification =
            NotificationCompat
                .Builder(appContext, CHANNEL_EVENT)
                .setSmallIcon(R.drawable.ic_notify)
                .setContentTitle(event.name)
                .setContentText(appContext.getString(R.string.notify_arrived))
                .setAutoCancel(true)
                .setContentIntent(contentIntent)
                .build()

        NotificationManagerCompat.from(appContext).notify(event.id.toInt(), notification)
    }

    fun ensureChannel(context: Context) {
        val channel =
            NotificationChannel(
                CHANNEL_EVENT,
                context.getString(R.string.notify_channel_name),
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = context.getString(R.string.notify_channel_desc)
            }

        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    fun requestExactAlarmIfNeeded(context: Context) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)

        if (alarmManager.canScheduleExactAlarms()) return

        showToast(context, context.getString(R.string.toast_exact_alarm_required))

        val intent =
            Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, "package:${context.packageName}".toUri())

        if (runCatching { context.startActivity(intent) }.isFailure) {
            context.startActivity(
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, "package:${context.packageName}".toUri()),
            )
        }
    }

    private fun notifyPendingIntent(
        context: Context,
        eventId: Long,
    ): PendingIntent =
        PendingIntent.getBroadcast(
            context,
            eventId.toInt(),
            Intent(context, NotifyReceiver::class.java)
                .setAction(NotifyReceiver.ACTION_NOTIFY)
                .putExtra(NotifyReceiver.EXTRA_EVENT_ID, eventId),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
}
