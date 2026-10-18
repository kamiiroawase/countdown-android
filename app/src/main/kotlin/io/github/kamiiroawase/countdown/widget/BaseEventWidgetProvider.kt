package io.github.kaimiroawase.countdown.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import io.github.kaimiroawase.countdown.AppContainer
import io.github.kaimiroawase.countdown.model.EventEntity
import io.github.kaimiroawase.countdown.model.sortedForDisplay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.ZonedDateTime

abstract class BaseEventWidgetProvider : AppWidgetProvider() {
    companion object {
        private val updateScope = ProcessLifecycleOwner.get().lifecycleScope

        private val updateMutex = Mutex()
    }

    protected abstract fun renderWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        events: List<EventEntity>,
    )

    protected abstract fun clearWidgetBinding(appWidgetId: Int)

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        updateWidgets(context, appWidgetManager, appWidgetIds, goAsync())
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)

        updateAllWidgets(context, goAsync())
    }

    override fun onDisabled(context: Context) {
        WidgetScheduler.cancelUpdates(context, javaClass)

        super.onDisabled(context)
    }

    override fun onDeleted(
        context: Context,
        appWidgetIds: IntArray,
    ) {
        appWidgetIds.forEach { clearWidgetBinding(it) }

        super.onDeleted(context, appWidgetIds)
    }

    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        super.onReceive(context, intent)

        when (intent.action) {
            WidgetScheduler.updateAction(javaClass) -> {
                val appWidgetIds = WidgetScheduler.widgetIds(context, javaClass)

                if (appWidgetIds.isEmpty()) {
                    WidgetScheduler.cancelUpdates(context, javaClass)
                } else {
                    updateWidgets(
                        context,
                        AppWidgetManager.getInstance(context),
                        appWidgetIds,
                        goAsync(),
                    )
                }
            }
        }
    }

    fun updateAllWidgets(
        context: Context,
        pendingResult: PendingResult? = null,
    ) {
        val appWidgetManager = AppWidgetManager.getInstance(context)

        updateWidgets(context, appWidgetManager, WidgetScheduler.widgetIds(context, javaClass), pendingResult)
    }

    private fun updateWidgets(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
        pendingResult: PendingResult?,
    ) {
        if (appWidgetIds.isEmpty()) {
            pendingResult?.finish()

            return
        }

        val appContext = context.applicationContext

        val providerClass = javaClass

        updateScope.launch(Dispatchers.IO) {
            try {
                updateMutex.withLock {
                    val zone = AppContainer.zoneId

                    val now = ZonedDateTime.now(zone)

                    val events = AppContainer.eventDao.getTopLevel().sortedForDisplay()

                    for (appWidgetId in appWidgetIds) {
                        renderWidget(appContext, appWidgetManager, appWidgetId, events)
                    }

                    WidgetScheduler.scheduleUpdates(
                        appContext,
                        providerClass,
                        WidgetScheduler.nextRefreshMillis(events, zone, now),
                    )
                }
            } catch (_: Exception) {
                WidgetScheduler.scheduleUpdates(
                    appContext,
                    providerClass,
                    System.currentTimeMillis() + WidgetScheduler.SELF_CHECK_MILLIS,
                )
            } finally {
                pendingResult?.finish()
            }
        }
    }
}
