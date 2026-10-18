package io.github.kaimiroawase.countdown.ui.screen

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import io.github.kaimiroawase.countdown.AppContainer
import io.github.kaimiroawase.countdown.R
import io.github.kaimiroawase.countdown.model.EventEntity
import io.github.kaimiroawase.countdown.notify.NotifyScheduler
import io.github.kaimiroawase.countdown.ui.components.ConfirmDialog
import io.github.kaimiroawase.countdown.ui.components.DayPickerDialog
import io.github.kaimiroawase.countdown.ui.components.FormCard
import io.github.kaimiroawase.countdown.ui.components.FormPickRow
import io.github.kaimiroawase.countdown.ui.components.InputRow
import io.github.kaimiroawase.countdown.ui.components.OrangeHeader
import io.github.kaimiroawase.countdown.ui.components.RepeatWheelDialog
import io.github.kaimiroawase.countdown.ui.components.RowDivider
import io.github.kaimiroawase.countdown.ui.components.SectionTitle
import io.github.kaimiroawase.countdown.ui.components.SwitchRow
import io.github.kaimiroawase.countdown.ui.components.TimeWheelDialog
import io.github.kaimiroawase.countdown.ui.components.showToast
import io.github.kaimiroawase.countdown.ui.theme.BrandOrange
import io.github.kaimiroawase.countdown.ui.theme.DangerRed
import io.github.kaimiroawase.countdown.ui.theme.Divider
import io.github.kaimiroawase.countdown.ui.theme.OnPrimary
import io.github.kaimiroawase.countdown.ui.theme.PageBackground
import io.github.kaimiroawase.countdown.ui.theme.SegmentChecked
import io.github.kaimiroawase.countdown.ui.theme.SegmentCheckedBg
import io.github.kaimiroawase.countdown.ui.theme.SegmentCheckedStroke
import io.github.kaimiroawase.countdown.ui.theme.SurfaceWhite
import io.github.kaimiroawase.countdown.ui.theme.TextPlaceholder
import io.github.kaimiroawase.countdown.ui.theme.TextPrimary
import io.github.kaimiroawase.countdown.ui.theme.TextTertiary
import kotlinx.coroutines.launch

@Composable
fun EventEditScreen(
    eventId: Long,
    parentEventId: Long,
    onClose: () -> Unit,
) {
    val context = LocalContext.current

    val scope = rememberCoroutineScope()

    val isEdit = eventId > 0

    var editEvent by remember { mutableStateOf<EventEntity?>(null) }

    var filled by rememberSaveable { mutableStateOf(false) }

    var name by rememberSaveable { mutableStateOf("") }

    var orderText by rememberSaveable { mutableStateOf("") }

    var targetDayMillis by rememberSaveable { mutableLongStateOf(-1L) }

    var targetHour by rememberSaveable { mutableIntStateOf(-1) }

    var targetMinute by rememberSaveable { mutableIntStateOf(-1) }

    var targetSecond by rememberSaveable { mutableIntStateOf(-1) }

    var repeatInterval by rememberSaveable { mutableIntStateOf(0) }

    var repeatUnit by rememberSaveable { mutableIntStateOf(0) }

    var repeatEndDayMillis by rememberSaveable { mutableLongStateOf(-1L) }

    var repeatEndHour by rememberSaveable { mutableIntStateOf(-1) }

    var repeatEndMinute by rememberSaveable { mutableIntStateOf(-1) }

    var repeatEndSecond by rememberSaveable { mutableIntStateOf(-1) }

    var lunar by rememberSaveable { mutableStateOf(false) }

    var leapCount by rememberSaveable { mutableStateOf(true) }

    var notify by rememberSaveable { mutableStateOf(false) }

    var ceilFuture by rememberSaveable { mutableStateOf(true) }

    var showTargetDayPicker by remember { mutableStateOf(false) }

    var showTargetTimePicker by remember { mutableStateOf(false) }

    var showRepeatPicker by remember { mutableStateOf(false) }

    var showRepeatEndDayPicker by remember { mutableStateOf(false) }

    var showRepeatEndTimePicker by remember { mutableStateOf(false) }

    var showLeaveConfirm by remember { mutableStateOf(false) }

    var showDeleteConfirm by remember { mutableStateOf(false) }

    var deleteSubCount by remember { mutableIntStateOf(0) }

    var editLoadFinished by remember { mutableStateOf(false) }

    LaunchedEffect(eventId) {
        if (eventId > 0) {
            editEvent = AppContainer.eventDao.getById(eventId)

            editLoadFinished = true
        }
    }

    LaunchedEffect(editLoadFinished, editEvent) {
        if (editLoadFinished && editEvent == null) {
            showToast(context, context.getString(R.string.toast_event_not_found))
            onClose()
        }
    }

    LaunchedEffect(editEvent, filled) {
        val event = editEvent ?: return@LaunchedEffect

        if (filled) return@LaunchedEffect

        filled = true

        name = event.name
        orderText = if (event.order > 0) event.order.toString() else ""
        targetDayMillis = event.targetDay
        targetHour = event.targetHour
        targetMinute = event.targetMinute
        targetSecond = event.targetSecond
        repeatInterval = event.repeatInterval
        repeatUnit = event.repeatUnit
        repeatEndDayMillis = event.repeatEndDay
        repeatEndHour = event.repeatEndHour
        repeatEndMinute = event.repeatEndMinute
        repeatEndSecond = event.repeatEndSecond
        lunar = event.lunar
        leapCount = event.leapCount
        notify = event.notify
        ceilFuture = event.ceilFuture
    }

    LaunchedEffect(isEdit) {
        if (!isEdit) {
            targetHour = 0
            targetMinute = 0
            targetSecond = 0
        }
    }

    var finishAfterPermissionResult by rememberSaveable { mutableStateOf(false) }

    var submitted by rememberSaveable { mutableStateOf(false) }

    val notifyPermissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (!granted) {
                notify = false

                showToast(context, context.getString(R.string.toast_notify_permission_denied))
            }

            if (finishAfterPermissionResult) {
                finishAfterPermissionResult = false

                if (granted) NotifyScheduler.requestExactAlarmIfNeeded(context)

                onClose()
            }
        }

    val dirty =
        isDirty(
            editEvent,
            name,
            orderText,
            targetDayMillis,
            targetHour,
            targetMinute,
            targetSecond,
            repeatInterval,
            repeatUnit,
            repeatEndDayMillis,
            repeatEndHour,
            repeatEndMinute,
            repeatEndSecond,
            lunar,
            leapCount,
            notify,
            ceilFuture,
        )

    BackHandler(enabled = dirty) {
        showLeaveConfirm = true
    }

    val intervalLabels =
        remember {
            listOf(context.getString(R.string.repeat_none)) +
                (1..99).map { context.getString(R.string.repeat_interval_format, it) }
        }

    val unitLabels =
        remember {
            listOf(
                context.getString(R.string.repeat_none),
                context.getString(R.string.repeat_unit_day),
                context.getString(R.string.repeat_unit_week),
                context.getString(R.string.repeat_unit_month),
                context.getString(R.string.repeat_unit_year),
                context.getString(R.string.repeat_unit_hour),
            )
        }

    Column(modifier = Modifier.fillMaxSize().background(PageBackground)) {
        OrangeHeader(
            title = stringResource(if (isEdit) R.string.edit_title_edit else R.string.edit_title_create),
            centered = true,
            navigationIcon = {
                Icon(
                    painter = painterResource(R.drawable.ic_back),
                    contentDescription = null,
                    tint = OnPrimary,
                    modifier =
                        Modifier
                            .clickable {
                                if (dirty) showLeaveConfirm = true else onClose()
                            }.padding(horizontal = 12.dp, vertical = 16.dp),
                )
            },
        )

        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(top = 12.dp, bottom = 24.dp),
        ) {
            SectionTitle(stringResource(R.string.edit_section_basic), Modifier.padding(start = 20.dp, top = 12.dp, bottom = 8.dp))

            FormCard {
                InputRow(
                    label = stringResource(R.string.edit_label_name),
                    value = name,
                    onValueChange = { name = it },
                    hint = stringResource(R.string.edit_hint_name),
                )
                RowDivider()
                InputRow(
                    label = stringResource(R.string.edit_label_order),
                    value = orderText,
                    onValueChange = { orderText = it.filter(Char::isDigit).take(4) },
                    hint = stringResource(R.string.edit_hint_order),
                    keyboardType = KeyboardType.Number,
                )
            }

            SectionTitle(stringResource(R.string.edit_section_time), Modifier.padding(start = 20.dp, top = 24.dp, bottom = 8.dp))

            FormCard {
                FormPickRow(
                    label = stringResource(R.string.edit_label_target_day),
                    value =
                        if (targetDayMillis == -1L) {
                            stringResource(R.string.hint_pick)
                        } else {
                            formatTargetDay(context, targetDayMillis, lunar)
                        },
                    valueColor = if (targetDayMillis == -1L) TextPlaceholder else TextPrimary,
                    onClick = {
                        showTargetDayPicker = true
                    },
                )
                RowDivider()
                FormPickRow(
                    label = stringResource(R.string.edit_label_target_time),
                    value =
                        if (targetHour < 0 || targetMinute < 0 || targetSecond < 0) {
                            stringResource(R.string.hint_pick)
                        } else {
                            AppContainer.TIME_TEXT_FORMAT.format(targetHour, targetMinute, targetSecond)
                        },
                    valueColor = if (targetHour < 0 || targetMinute < 0 || targetSecond < 0) TextPlaceholder else TextPrimary,
                    onClick = { showTargetTimePicker = true },
                )
                RowDivider()
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .heightIn(min = 54.dp)
                            .padding(horizontal = 20.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.edit_label_calendar),
                        fontSize = 16.sp,
                        color = TextPrimary,
                    )
                    Spacer(Modifier.weight(1f))
                    SingleChoiceSegmentedButtonRow {
                        SegmentedButton(
                            selected = !lunar,
                            onClick = { lunar = false },
                            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                            colors =
                                SegmentedButtonDefaults.colors(
                                    activeContainerColor = SegmentCheckedBg,
                                    activeContentColor = SegmentChecked,
                                    activeBorderColor = SegmentCheckedStroke,
                                    inactiveContainerColor = SurfaceWhite,
                                    inactiveContentColor = TextTertiary,
                                    inactiveBorderColor = Divider,
                                ),
                        ) {
                            Text(stringResource(R.string.calendar_solar), fontSize = 14.sp)
                        }
                        SegmentedButton(
                            selected = lunar,
                            onClick = { lunar = true },
                            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                            colors =
                                SegmentedButtonDefaults.colors(
                                    activeContainerColor = SegmentCheckedBg,
                                    activeContentColor = SegmentChecked,
                                    activeBorderColor = SegmentCheckedStroke,
                                    inactiveContainerColor = SurfaceWhite,
                                    inactiveContentColor = TextTertiary,
                                    inactiveBorderColor = Divider,
                                ),
                        ) {
                            Text(stringResource(R.string.calendar_lunar), fontSize = 14.sp)
                        }
                    }
                }
                if (lunar) {
                    RowDivider()
                    SwitchRow(
                        label = stringResource(R.string.edit_label_leap),
                        checked = leapCount,
                        onCheckedChange = { leapCount = it },
                    )
                }
                RowDivider()
                FormPickRow(
                    label = stringResource(R.string.edit_label_repeat),
                    value = repeatText(intervalLabels, unitLabels, repeatInterval, repeatUnit),
                    valueColor = TextPrimary,
                    onClick = { showRepeatPicker = true },
                )
            }

            SectionTitle(stringResource(R.string.edit_section_advanced), Modifier.padding(start = 20.dp, top = 24.dp, bottom = 8.dp))

            FormCard {
                SwitchRow(
                    label = stringResource(R.string.edit_label_notify),
                    checked = notify,
                    onCheckedChange = { checked ->
                        notify = checked

                        if (checked &&
                            Build.VERSION.SDK_INT >= 33 &&
                            ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.POST_NOTIFICATIONS,
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            notifyPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    },
                )
                RowDivider()
                FormPickRow(
                    label = stringResource(R.string.edit_label_repeat_end),
                    value = repeatEndText(repeatEndDayMillis, repeatEndHour, repeatEndMinute, repeatEndSecond),
                    valueColor = TextPrimary,
                    valueFontSize = 13.sp,
                    onClick = { showRepeatEndDayPicker = true },
                ) {
                    if (repeatEndDayMillis != -1L) {
                        Text(
                            text = stringResource(R.string.action_clear),
                            color = BrandOrange,
                            fontSize = 14.sp,
                            modifier =
                                Modifier
                                    .padding(start = 8.dp)
                                    .clickable {
                                        repeatEndDayMillis = -1L
                                        repeatEndHour = -1
                                        repeatEndMinute = -1
                                        repeatEndSecond = -1
                                    },
                        )
                    }
                }
                RowDivider()
                SwitchRow(
                    label = stringResource(R.string.edit_label_ceil),
                    checked = ceilFuture,
                    onCheckedChange = { ceilFuture = it },
                )
            }

            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 32.dp)
                        .clip(RoundedCornerShape(50))
                        .background(BrandOrange)
                        .clickable {
                            if (submitted) return@clickable
                            submitted = true
                            submit(
                                context,
                                scope,
                                isEdit,
                                editEvent,
                                parentEventId,
                                name,
                                orderText,
                                targetDayMillis,
                                targetHour,
                                targetMinute,
                                targetSecond,
                                repeatInterval,
                                repeatUnit,
                                lunar,
                                leapCount,
                                notify,
                                ceilFuture,
                                repeatEndDayMillis,
                                repeatEndHour,
                                repeatEndMinute,
                                repeatEndSecond,
                                onInvalid = { submitted = false },
                                onSaved = { saved ->
                                    NotifyScheduler.syncEvent(context.applicationContext, saved)

                                    scope.launch {
                                        if (saved.notify &&
                                            Build.VERSION.SDK_INT >= 33 &&
                                            ContextCompat.checkSelfPermission(
                                                context,
                                                Manifest.permission.POST_NOTIFICATIONS,
                                            ) != PackageManager.PERMISSION_GRANTED
                                        ) {
                                            finishAfterPermissionResult = true

                                            notifyPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                        } else {
                                            if (saved.notify) NotifyScheduler.requestExactAlarmIfNeeded(context)

                                            onClose()
                                        }
                                    }
                                },
                            )
                        },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.action_save),
                    color = OnPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 15.dp),
                )
            }

            if (isEdit) {
                Text(
                    text = stringResource(R.string.action_delete),
                    color = DangerRed,
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center,
                    modifier =
                        Modifier
                            .align(Alignment.CenterHorizontally)
                            .clickable {
                                queryAndConfirmDelete(scope, eventId) { count ->
                                    deleteSubCount = count
                                    showDeleteConfirm = true
                                }
                            }.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
        }
    }

    if (showTargetDayPicker) {
        DayPickerDialog(
            title = stringResource(R.string.picker_title_target_day),
            initialMillis = targetDayMillis,
            onConfirm = { millis ->
                targetDayMillis = millis
                showTargetDayPicker = false
            },
            onDismiss = { showTargetDayPicker = false },
        )
    }

    if (showTargetTimePicker) {
        TimeWheelDialog(
            initialHour = targetHour,
            initialMinute = targetMinute,
            initialSecond = targetSecond,
            onConfirm = { h, m, s ->
                targetHour = h
                targetMinute = m
                targetSecond = s
                showTargetTimePicker = false
            },
            onDismiss = { showTargetTimePicker = false },
        )
    }

    if (showRepeatPicker) {
        RepeatWheelDialog(
            initialInterval = repeatInterval,
            initialUnit = repeatUnit,
            intervalLabels = intervalLabels,
            unitLabels = unitLabels,
            onConfirm = { interval, unit ->
                repeatInterval = interval
                repeatUnit = unit
                showRepeatPicker = false
            },
            onDismiss = { showRepeatPicker = false },
        )
    }

    if (showRepeatEndDayPicker) {
        DayPickerDialog(
            title = stringResource(R.string.picker_title_repeat_end),
            initialMillis = repeatEndDayMillis,
            onConfirm = { millis ->
                repeatEndDayMillis = millis
                showRepeatEndDayPicker = false
                showRepeatEndTimePicker = true
            },
            onDismiss = { showRepeatEndDayPicker = false },
        )
    }

    if (showRepeatEndTimePicker) {
        TimeWheelDialog(
            initialHour = repeatEndHour,
            initialMinute = repeatEndMinute,
            initialSecond = repeatEndSecond,
            onConfirm = { h, m, s ->
                repeatEndHour = h
                repeatEndMinute = m
                repeatEndSecond = s
                showRepeatEndTimePicker = false
            },
            onDismiss = { showRepeatEndTimePicker = false },
        )
    }

    if (showLeaveConfirm) {
        ConfirmDialog(
            title = stringResource(R.string.edit_leave_title),
            message = stringResource(R.string.edit_leave_message),
            confirmText = stringResource(R.string.action_leave),
            confirmColor = DangerRed,
            onConfirm = {
                showLeaveConfirm = false
                onClose()
            },
            onDismiss = { showLeaveConfirm = false },
        )
    }

    if (showDeleteConfirm) {
        ConfirmDialog(
            title = stringResource(R.string.edit_delete_title),
            message =
                if (deleteSubCount > 0) {
                    stringResource(R.string.edit_delete_message_with_sub, deleteSubCount)
                } else {
                    stringResource(R.string.edit_delete_message)
                },
            confirmText = stringResource(R.string.action_delete),
            confirmColor = DangerRed,
            onConfirm = {
                showDeleteConfirm = false
                deleteEvent(scope, context, editEvent, onClose)
            },
            onDismiss = { showDeleteConfirm = false },
        )
    }
}
