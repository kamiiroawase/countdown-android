package io.github.kaimiroawase.countdown.ui.screen

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import io.github.kaimiroawase.countdown.AppContainer
import io.github.kaimiroawase.countdown.R
import io.github.kaimiroawase.countdown.data.EventBackgroundStore
import io.github.kaimiroawase.countdown.model.EventEntity
import io.github.kaimiroawase.countdown.model.lunarDateText
import io.github.kaimiroawase.countdown.notify.NotifyScheduler
import io.github.kaimiroawase.countdown.ui.components.showToast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.ZoneOffset

internal fun submit(
    context: Context,
    scope: CoroutineScope,
    isEdit: Boolean,
    editEvent: EventEntity?,
    parentEventId: Long,
    name: String,
    orderText: String,
    targetDayMillis: Long,
    targetHour: Int,
    targetMinute: Int,
    targetSecond: Int,
    repeatInterval: Int,
    repeatUnit: Int,
    lunar: Boolean,
    leapCount: Boolean,
    notify: Boolean,
    ceilFuture: Boolean,
    repeatEndDayMillis: Long,
    repeatEndHour: Int,
    repeatEndMinute: Int,
    repeatEndSecond: Int,
    onInvalid: () -> Unit,
    onSaved: (EventEntity) -> Unit,
) {
    if (isEdit && editEvent == null) {
        showToast(context, context.getString(R.string.toast_event_loading))
        onInvalid()
        return
    }

    val trimmedName = name.trim()

    if (trimmedName.isEmpty()) {
        showToast(context, context.getString(R.string.toast_name_required))
        onInvalid()
        return
    }

    val order = orderText.toIntOrNull() ?: 0

    if (orderText.isNotEmpty() && order !in 1..9999) {
        showToast(context, context.getString(R.string.toast_order_range))
        onInvalid()
        return
    }

    if (targetDayMillis == -1L) {
        showToast(context, context.getString(R.string.toast_target_day_required))
        onInvalid()
        return
    }

    if (targetHour < 0 || targetMinute < 0 || targetSecond < 0) {
        showToast(context, context.getString(R.string.toast_target_time_required))
        onInvalid()
        return
    }

    val event =
        EventEntity(
            id = editEvent?.id ?: 0L,
            name = trimmedName,
            order = order,
            parentId = editEvent?.parentId ?: parentEventId,
            lunar = lunar,
            leapCount = leapCount,
            notify = notify,
            targetDay = targetDayMillis,
            targetHour = targetHour,
            targetMinute = targetMinute,
            targetSecond = targetSecond,
            repeatInterval = repeatInterval,
            repeatUnit = repeatUnit,
            ceilFuture = ceilFuture,
            repeatEndDay = repeatEndDayMillis,
            repeatEndHour = repeatEndHour,
            repeatEndMinute = repeatEndMinute,
            repeatEndSecond = repeatEndSecond,
        )

    scope.launch(Dispatchers.IO) {
        val saved =
            if (editEvent == null) {
                event.copy(id = AppContainer.eventDao.insert(event))
            } else {
                AppContainer.eventDao.update(event)

                event
            }

        EventEntity.evictTarget(saved.id)

        withContext(Dispatchers.Main) {
            onSaved(saved)
        }
    }
}

internal fun queryAndConfirmDelete(
    scope: CoroutineScope,
    eventId: Long,
    onReady: (Int) -> Unit,
) {
    scope.launch(Dispatchers.IO) {
        val subCount = AppContainer.eventDao.getByParentId(eventId).size

        withContext(Dispatchers.Main) {
            onReady(subCount)
        }
    }
}

internal fun deleteEvent(
    scope: CoroutineScope,
    context: Context,
    editEvent: EventEntity?,
    onClose: () -> Unit,
) {
    val event = editEvent ?: return

    val appContext = context.applicationContext

    scope.launch(Dispatchers.IO) {
        val children = AppContainer.eventDao.getByParentId(event.id)

        AppContainer.eventDao.deleteWithChildren(event)

        listOf(event).plus(children).forEach { deleted ->
            NotifyScheduler.cancel(appContext, deleted.id)

            EventEntity.evictTarget(deleted.id)

            EventBackgroundStore.deleteFile(appContext, deleted.background)
        }

        withContext(Dispatchers.Main) {
            onClose()
        }
    }
}

internal fun formatTargetDay(
    context: Context,
    millis: Long,
    lunar: Boolean,
): String {
    val date = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()

    return if (lunar) {
        lunarDateText(context, date)
    } else {
        date.format(AppContainer.dateFormatter)
    }
}

internal fun repeatText(
    intervalLabels: List<String>,
    unitLabels: List<String>,
    interval: Int,
    unit: Int,
): String =
    if (interval == 0 || unit == 0) {
        intervalLabels[0]
    } else {
        intervalLabels.getOrNull(interval)?.plus(unitLabels.getOrNull(unit).orEmpty()) ?: intervalLabels[0]
    }

@Composable
internal fun repeatEndText(
    dayMillis: Long,
    hour: Int,
    minute: Int,
    second: Int,
): String {
    if (dayMillis == -1L) return stringResource(R.string.edit_repeat_end_none)

    val date =
        Instant
            .ofEpochMilli(dayMillis)
            .atZone(ZoneOffset.UTC)
            .toLocalDate()
            .format(AppContainer.dateFormatter)

    return date + " " + AppContainer.TIME_TEXT_FORMAT.format(hour.coerceAtLeast(0), minute.coerceAtLeast(0), second.coerceAtLeast(0))
}

internal fun isDirty(
    editEvent: EventEntity?,
    name: String,
    orderText: String,
    targetDayMillis: Long,
    targetHour: Int,
    targetMinute: Int,
    targetSecond: Int,
    repeatInterval: Int,
    repeatUnit: Int,
    repeatEndDayMillis: Long,
    repeatEndHour: Int,
    repeatEndMinute: Int,
    repeatEndSecond: Int,
    lunar: Boolean,
    leapCount: Boolean,
    notify: Boolean,
    ceilFuture: Boolean,
): Boolean {
    val baseline = editEvent

    fun current() =
        listOf(
            name,
            orderText,
            targetDayMillis.toString(),
            targetHour.toString(),
            targetMinute.toString(),
            targetSecond.toString(),
            repeatInterval.toString(),
            repeatUnit.toString(),
            repeatEndDayMillis.toString(),
            repeatEndHour.toString(),
            repeatEndMinute.toString(),
            repeatEndSecond.toString(),
            lunar.toString(),
            leapCount.toString(),
            notify.toString(),
            ceilFuture.toString(),
        )

    fun base() =
        if (baseline == null) {
            listOf(
                "",
                "",
                (-1L).toString(),
                "0",
                "0",
                "0",
                "0",
                "0",
                (-1L).toString(),
                (-1).toString(),
                (-1).toString(),
                (-1).toString(),
                "false",
                "true",
                "false",
                "true",
            )
        } else {
            listOf(
                baseline.name,
                if (baseline.order > 0) baseline.order.toString() else "",
                baseline.targetDay.toString(),
                baseline.targetHour.toString(),
                baseline.targetMinute.toString(),
                baseline.targetSecond.toString(),
                baseline.repeatInterval.toString(),
                baseline.repeatUnit.toString(),
                baseline.repeatEndDay.toString(),
                baseline.repeatEndHour.toString(),
                baseline.repeatEndMinute.toString(),
                baseline.repeatEndSecond.toString(),
                baseline.lunar.toString(),
                baseline.leapCount.toString(),
                baseline.notify.toString(),
                baseline.ceilFuture.toString(),
            )
        }

    return current() != base()
}
