package io.github.kaimiroawase.countdown.data

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

object AppPrefs {
    const val VIEW_STYLE_LIST = "list"

    const val VIEW_STYLE_GRID = "grid"

    const val WIDGET_BINDING_ALL = "all"

    private const val MAIN_PREFS_FILE = "countdown_prefs"

    private const val WIDGET_PREFS_FILE = "widget_prefs"

    private const val KEY_ZONE_ID = "zone_id"

    private const val KEY_EVENT_VIEW_STYLE = "event_view_style"

    private const val KEY_EVENT_PREFILLED = "event_prefilled"

    private const val KEY_WIDGET_CARD_PREFIX = "widget_card_"

    private const val KEY_WIDGET_LIST_PREFIX = "widget_list_"

    private var appPrefs: SharedPreferences? = null

    private var widgetPrefs: SharedPreferences? = null

    fun init(context: Application) {
        appPrefs = context.getSharedPreferences(MAIN_PREFS_FILE, Context.MODE_PRIVATE)
        widgetPrefs = context.getSharedPreferences(WIDGET_PREFS_FILE, Context.MODE_PRIVATE)
    }

    fun getZoneId(): String? = appPrefs?.getString(KEY_ZONE_ID, null)?.ifBlank { null }

    fun setZoneId(value: String?) {
        appPrefs?.edit(commit = true) { putString(KEY_ZONE_ID, value) }
    }

    fun getEventViewStyle(): String =
        appPrefs?.getString(KEY_EVENT_VIEW_STYLE, null)?.ifBlank { null }
            ?: VIEW_STYLE_GRID

    fun setEventViewStyle(value: String) {
        appPrefs?.edit(commit = true) { putString(KEY_EVENT_VIEW_STYLE, value) }
    }

    fun getEventPrefilled(): Boolean = appPrefs?.getBoolean(KEY_EVENT_PREFILLED, false) ?: false

    fun setEventPrefilled(value: Boolean) {
        appPrefs?.edit(commit = true) { putBoolean(KEY_EVENT_PREFILLED, value) }
    }

    fun getEventWidgetCard(appWidgetId: Int): Long = widgetPrefs?.getLong("${KEY_WIDGET_CARD_PREFIX}$appWidgetId", 0L) ?: 0L

    fun setEventWidgetCard(
        appWidgetId: Int,
        eventId: Long,
    ) {
        widgetPrefs?.edit(commit = true) { putLong("${KEY_WIDGET_CARD_PREFIX}$appWidgetId", eventId) }
    }

    fun clearEventWidgetCard(appWidgetId: Int) {
        widgetPrefs?.edit(commit = true) { remove("${KEY_WIDGET_CARD_PREFIX}$appWidgetId") }
    }

    fun getEventWidgetList(appWidgetId: Int): String =
        widgetPrefs?.getString("${KEY_WIDGET_LIST_PREFIX}$appWidgetId", null)?.ifBlank { null }
            ?: WIDGET_BINDING_ALL

    fun setEventWidgetList(
        appWidgetId: Int,
        value: String,
    ) {
        widgetPrefs?.edit(commit = true) { putString("${KEY_WIDGET_LIST_PREFIX}$appWidgetId", value) }
    }

    fun clearEventWidgetList(appWidgetId: Int) {
        widgetPrefs?.edit(commit = true) { remove("${KEY_WIDGET_LIST_PREFIX}$appWidgetId") }
    }
}
