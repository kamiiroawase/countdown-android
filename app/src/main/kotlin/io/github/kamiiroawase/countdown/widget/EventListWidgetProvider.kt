package io.github.kaimiroawase.countdown.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import io.github.kaimiroawase.countdown.R
import io.github.kaimiroawase.countdown.data.AppPrefs
import io.github.kaimiroawase.countdown.model.EventEntity
import io.github.kaimiroawase.countdown.model.daysCountdownTexts
import io.github.kaimiroawase.countdown.model.infoItem

class EventListWidgetProvider : BaseEventWidgetProvider() {
    companion object {
        fun updateAllWidgets(
            context: Context,
            events: List<EventEntity>,
        ) {
            val appWidgetManager = AppWidgetManager.getInstance(context)

            WidgetScheduler
                .widgetIds(context, EventListWidgetProvider::class.java)
                .forEach { appWidgetId ->
                    updateWidget(
                        context,
                        appWidgetManager,
                        appWidgetId,
                        boundEvents(events, AppPrefs.getEventWidgetList(appWidgetId)),
                    )
                }
        }

        fun updateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
            events: List<EventEntity>,
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_event_list)

            val items =
                RemoteViews.RemoteCollectionItems
                    .Builder()
                    .setHasStableIds(true)
                    .apply {
                        events.forEach { addItem(it.id, rowViews(context, it)) }
                    }.build()

            views.setRemoteAdapter(R.id.eventListView, items)

            views.setEmptyView(R.id.eventListView, R.id.listEmptyView)

            views.setPendingIntentTemplate(
                R.id.eventListView,
                WidgetScheduler.openAppPendingIntent(context, appWidgetId),
            )

            views.setOnClickPendingIntent(
                R.id.widgetListRoot,
                WidgetScheduler.openAppPendingIntent(context, appWidgetId),
            )

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        fun boundEvents(
            events: List<EventEntity>,
            binding: String,
        ): List<EventEntity> {
            if (binding == AppPrefs.WIDGET_BINDING_ALL) return events

            val ids = binding.split(',').mapNotNull { it.toLongOrNull() }

            return events.filter { it.id in ids }
        }

        fun rowViews(
            context: Context,
            event: EventEntity,
        ): RemoteViews {
            val views = RemoteViews(context.packageName, R.layout.widget_event_row)

            val item = event.infoItem(context)

            val days = event.daysCountdownTexts(context)

            views.setTextViewText(R.id.rowName, event.name)

            views.setTextViewText(
                R.id.rowInfo,
                (days?.toList() ?: listOf(item.prefix, item.value, item.unit)).joinToString(""),
            )

            views.setOnClickFillInIntent(R.id.rowRoot, Intent())

            return views
        }
    }

    override fun renderWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        events: List<EventEntity>,
    ) {
        updateWidget(
            context,
            appWidgetManager,
            appWidgetId,
            boundEvents(events, AppPrefs.getEventWidgetList(appWidgetId)),
        )
    }

    override fun clearWidgetBinding(appWidgetId: Int) {
        AppPrefs.clearEventWidgetList(appWidgetId)
    }
}
