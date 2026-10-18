package io.github.kaimiroawase.countdown.notify

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import io.github.kaimiroawase.countdown.AppContainer
import io.github.kaimiroawase.countdown.BuildConfig
import io.github.kaimiroawase.countdown.data.AppPrefs
import io.github.kaimiroawase.countdown.model.sortedForDisplay
import io.github.kaimiroawase.countdown.widget.EventCardWidgetProvider
import io.github.kaimiroawase.countdown.widget.EventListWidgetProvider
import io.github.kaimiroawase.countdown.widget.WidgetScheduler
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotifyReceiver : BroadcastReceiver() {
    private val receiverScope = ProcessLifecycleOwner.get().lifecycleScope

    companion object {
        const val ACTION_NOTIFY = BuildConfig.APPLICATION_ID + ".action.NOTIFY_EVENT"

        const val EXTRA_EVENT_ID = "event_id"
    }

    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        when (intent.action) {
            ACTION_NOTIFY -> {
                val eventId = intent.getLongExtra(EXTRA_EVENT_ID, -1L)

                if (eventId > 0) {
                    handleNotify(context, eventId)
                }
            }

            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,

            AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED,
            -> {
                rescheduleAll(context)
            }
        }
    }

    private fun handleNotify(
        context: Context,
        eventId: Long,
    ) {
        val pendingResult = goAsync()

        receiverScope.launch(Dispatchers.IO) {
            try {
                val event = AppContainer.eventDao.getById(eventId)

                if (event == null) {
                    NotifyScheduler.cancel(context, eventId)
                } else {
                    if (event.notify) {
                        NotifyScheduler.postEventNotification(context, event)
                    }

                    NotifyScheduler.syncEvent(context, event)
                }

                refreshAll(context)
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                //
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun rescheduleAll(context: Context) {
        AppContainer.resetZoneId(AppPrefs.getZoneId())

        val pendingResult = goAsync()

        receiverScope.launch(Dispatchers.IO) {
            try {
                NotifyScheduler.syncAll(context, AppContainer.eventDao.getAll())

                refreshAll(context)
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun refreshAll(context: Context) {
        val events = AppContainer.eventDao.getTopLevel().sortedForDisplay()

        EventCardWidgetProvider.updateAllWidgets(context, events)

        EventListWidgetProvider.updateAllWidgets(context, events)

        WidgetScheduler.scheduleAllUpdates(
            context,
            WidgetScheduler.nextRefreshMillis(events),
        )
    }
}
