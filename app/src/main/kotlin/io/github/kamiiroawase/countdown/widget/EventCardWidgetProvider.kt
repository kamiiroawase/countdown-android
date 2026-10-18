package io.github.kaimiroawase.countdown.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.view.View
import android.widget.RemoteViews
import io.github.kaimiroawase.countdown.R
import io.github.kaimiroawase.countdown.data.AppPrefs
import io.github.kaimiroawase.countdown.model.EventEntity
import io.github.kaimiroawase.countdown.model.daysCountdownTexts
import io.github.kaimiroawase.countdown.model.infoItem

class EventCardWidgetProvider : BaseEventWidgetProvider() {
    companion object {
        fun updateAllWidgets(
            context: Context,
            events: List<EventEntity>,
        ) {
            val appWidgetManager = AppWidgetManager.getInstance(context)

            WidgetScheduler
                .widgetIds(context, EventCardWidgetProvider::class.java)
                .forEach { appWidgetId ->
                    val eventId = AppPrefs.getEventWidgetCard(appWidgetId)

                    val event = events.firstOrNull { it.id == eventId }

                    if (eventId > 0 && event == null) AppPrefs.clearEventWidgetCard(appWidgetId)

                    updateWidget(context, appWidgetManager, appWidgetId, event)
                }
        }

        fun updateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
            event: EventEntity?,
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_event_card)

            if (event == null) {
                views.setViewVisibility(R.id.eventBand, View.GONE)
                views.setViewVisibility(R.id.countdownContainer, View.GONE)
                views.setViewVisibility(R.id.targetDateContainer, View.GONE)
                views.setViewVisibility(R.id.timeText, View.GONE)
                views.setViewVisibility(R.id.widgetEmptyView, View.VISIBLE)
            } else {
                val item = event.infoItem(context)

                views.setViewVisibility(R.id.eventBand, View.VISIBLE)
                views.setViewVisibility(R.id.countdownContainer, View.VISIBLE)
                views.setViewVisibility(R.id.targetDateContainer, View.VISIBLE)
                views.setViewVisibility(R.id.timeText, View.VISIBLE)
                views.setViewVisibility(R.id.widgetEmptyView, View.GONE)

                views.setInt(
                    R.id.eventBand,
                    "setBackgroundResource",
                    if (item.past) R.drawable.widget_band_past else R.drawable.widget_band_active,
                )

                views.setTextViewText(R.id.eventNameText, event.name)

                val days = event.daysCountdownTexts(context)

                views.setTextViewText(R.id.eventPrefixText, days?.first ?: item.prefix)
                views.setTextViewText(R.id.countdownValueText, days?.second ?: item.value)
                views.setTextViewText(R.id.countdownUnitText, days?.third ?: item.unit)

                views.setTextViewText(R.id.dateText, item.date)
                views.setTextViewText(R.id.weekText, item.week)
                views.setTextViewText(R.id.timeText, item.time)
            }

            views.setOnClickPendingIntent(
                R.id.widgetRoot,
                WidgetScheduler.openAppPendingIntent(context, appWidgetId),
            )

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    override fun renderWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        events: List<EventEntity>,
    ) {
        val eventId = AppPrefs.getEventWidgetCard(appWidgetId)

        val event = if (eventId > 0) events.firstOrNull { it.id == eventId } else null

        if (eventId > 0 && event == null) AppPrefs.clearEventWidgetCard(appWidgetId)

        updateWidget(context, appWidgetManager, appWidgetId, event)
    }

    override fun clearWidgetBinding(appWidgetId: Int) {
        AppPrefs.clearEventWidgetCard(appWidgetId)
    }
}
