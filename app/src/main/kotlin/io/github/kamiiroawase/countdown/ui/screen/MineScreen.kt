package io.github.kaimiroawase.countdown.ui.screen

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import io.github.kaimiroawase.countdown.AppContainer
import io.github.kaimiroawase.countdown.R
import io.github.kaimiroawase.countdown.data.AppPrefs
import io.github.kaimiroawase.countdown.model.sortedForDisplay
import io.github.kaimiroawase.countdown.notify.NotifyScheduler
import io.github.kaimiroawase.countdown.ui.components.RowDivider
import io.github.kaimiroawase.countdown.ui.theme.BrandOrange
import io.github.kaimiroawase.countdown.ui.theme.CountdownTheme
import io.github.kaimiroawase.countdown.ui.theme.Divider
import io.github.kaimiroawase.countdown.ui.theme.IconChevron
import io.github.kaimiroawase.countdown.ui.theme.OnPrimary
import io.github.kaimiroawase.countdown.ui.theme.PageBackground
import io.github.kaimiroawase.countdown.ui.theme.SurfaceWhite
import io.github.kaimiroawase.countdown.ui.theme.TextPrimary
import io.github.kaimiroawase.countdown.ui.theme.TextSecondary
import io.github.kaimiroawase.countdown.ui.theme.TextStrong
import io.github.kaimiroawase.countdown.ui.theme.TextTertiary
import io.github.kaimiroawase.countdown.widget.EventCardWidgetProvider
import io.github.kaimiroawase.countdown.widget.EventListWidgetProvider
import io.github.kaimiroawase.countdown.widget.WidgetScheduler
import io.github.kamiiroawase.zonepicker.ZonePickerContract
import io.github.kamiiroawase.zonepicker.ZonePickerRequest
import io.github.kamiiroawase.zonepicker.ZonePickerResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.util.Locale
import java.util.TimeZone
import kotlin.math.abs

@Composable
fun MineScreen(innerPadding: PaddingValues) {
    val context = LocalContext.current

    val zone = remember(AppContainer.zoneId) { AppContainer.zoneId }

    val scope = rememberCoroutineScope()

    fun applyZone(value: String?) {
        AppPrefs.setZoneId(value)

        AppContainer.resetZoneId(value)

        val appContext = context.applicationContext

        scope.launch(Dispatchers.IO) {
            runCatching { NotifyScheduler.syncAll(appContext, AppContainer.eventDao.getAll()) }
        }

        scope.launch(Dispatchers.IO) {
            runCatching {
                val events = AppContainer.eventDao.getTopLevel().sortedForDisplay()

                EventCardWidgetProvider.updateAllWidgets(appContext, events)

                EventListWidgetProvider.updateAllWidgets(appContext, events)

                WidgetScheduler.scheduleAllUpdates(
                    appContext,
                    WidgetScheduler.nextRefreshMillis(events),
                )
            }
        }
    }

    val zonePicker =
        rememberLauncherForActivityResult(ZonePickerContract()) { result ->
            when (result) {
                is ZonePickerResult.Selected -> {
                    applyZone(result.zoneId)
                }

                ZonePickerResult.FollowSystem -> {
                    applyZone(null)
                }

                ZonePickerResult.Canceled -> {
                }
            }
        }

    var notice by remember { mutableStateOf<InfoDialogKind?>(null) }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(PageBackground),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(BrandOrange)
                    .padding(top = innerPadding.calculateTopPadding())
                    .height(54.dp),
        ) {
            Text(
                text = stringResource(R.string.mine_title),
                color = OnPrimary,
                fontSize = 20.sp,
                modifier =
                    Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 16.dp, end = 14.dp),
            )
        }

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(top = 12.dp, bottom = 24.dp),
        ) {
            Text(
                text = stringResource(R.string.mine_section_general),
                fontSize = 13.sp,
                color = TextSecondary,
                modifier = Modifier.padding(start = 20.dp, top = 12.dp, bottom = 8.dp),
            )

            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(SurfaceWhite),
            ) {
                val zonePickerTitle = stringResource(R.string.mine_title_zone_picker)

                // app 只有浅色模式,但选择器页面跟随系统昼夜:四个颜色参数全部钉死主题色,
                // 系统深色下也保持品牌观感;显式 markColor 走「传什么用什么」契约,不落对比度回退
                val themeColor = BrandOrange.toArgb()
                SettingsRow(
                    label = stringResource(R.string.mine_label_zone),
                    value = zoneLabel(zone),
                    onClick = {
                        zonePicker.launch(
                            ZonePickerRequest(
                                selectedZoneId = AppPrefs.getZoneId(),
                                accentColor = themeColor,
                                accentColorDark = themeColor,
                                markColor = themeColor,
                                markColorDark = themeColor,
                                title = zonePickerTitle,
                            ),
                        )
                    },
                )
                RowDivider()
                SettingsRow(
                    label = stringResource(R.string.mine_label_notify_notice),
                    onClick = { notice = InfoDialogKind.Notify },
                )
                RowDivider()
                SettingsRow(
                    label = stringResource(R.string.mine_label_widget_notice),
                    onClick = { notice = InfoDialogKind.Widget },
                )
                RowDivider()
                SettingsRow(
                    label = stringResource(R.string.mine_label_backup_notice),
                    onClick = { notice = InfoDialogKind.Backup },
                )
            }
        }
    }

    notice?.let { kind ->
        NoticeDialog(kind = kind, onDismiss = { notice = null })
    }
}

@Composable
private fun SettingsRow(
    label: String,
    onClick: () -> Unit,
    value: String? = null,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .heightIn(min = 54.dp)
                .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            fontSize = 16.sp,
            color = TextPrimary,
        )
        if (value != null) {
            Text(
                text = value,
                fontSize = 15.sp,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.End,
                modifier =
                    Modifier
                        .weight(1f)
                        .padding(start = 16.dp),
            )
        } else {
            Spacer(Modifier.weight(1f))
        }
        Icon(
            painter = painterResource(R.drawable.ic_chevron_right),
            contentDescription = null,
            tint = IconChevron,
            modifier =
                Modifier
                    .padding(start = 6.dp)
                    .size(20.dp),
        )
    }
}

@Preview
@Composable
private fun MineScreenPreview() {
    CountdownTheme {
        Scaffold { innerPadding ->
            MineScreen(innerPadding)
        }
    }
}

private fun zoneLabel(zone: ZoneId): String = offsetLabel(zone) + " " + zoneDisplayName(zone)

private fun offsetLabel(zone: ZoneId): String {
    val seconds = zone.rules.getOffset(Instant.now()).totalSeconds
    val sign = if (seconds < 0) "-" else "+"
    val absSeconds = abs(seconds)
    return "GMT$sign%02d:%02d".format(absSeconds / 3600, absSeconds % 3600 / 60)
}

private fun zoneDisplayName(zone: ZoneId): String = TimeZone.getTimeZone(zone.id).getDisplayName(false, TimeZone.LONG, Locale.getDefault())

internal enum class InfoDialogKind { Notify, Widget, Backup }

@Composable
internal fun NoticeDialog(
    kind: InfoDialogKind,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 48.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(SurfaceWhite),
        ) {
            Text(
                text = stringResource(kind.titleResId()),
                color = TextStrong,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 24.dp),
            )
            Text(
                text = stringResource(kind.messageResId()),
                color = TextTertiary,
                fontSize = 14.sp,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
            )
            if (kind == InfoDialogKind.Notify) {
                Text(
                    text = stringResource(R.string.notice_notify_mitigation_title),
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(start = 24.dp, top = 14.dp, end = 24.dp),
                )
                Text(
                    text = stringResource(R.string.notice_notify_mitigation_items),
                    color = TextTertiary,
                    fontSize = 14.sp,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(start = 24.dp, top = 6.dp, end = 24.dp, bottom = 20.dp),
                )
            } else {
                Spacer(Modifier.height(20.dp))
            }
            HorizontalDivider(thickness = 0.5.dp, color = Divider)
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clickable(onClick = onDismiss),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.action_got_it),
                    color = BrandOrange,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

private fun InfoDialogKind.titleResId(): Int =
    when (this) {
        InfoDialogKind.Notify -> R.string.notice_notify_title
        InfoDialogKind.Widget -> R.string.notice_widget_title
        InfoDialogKind.Backup -> R.string.notice_backup_title
    }

private fun InfoDialogKind.messageResId(): Int =
    when (this) {
        InfoDialogKind.Notify -> R.string.notice_notify_message
        InfoDialogKind.Widget -> R.string.notice_widget_message
        InfoDialogKind.Backup -> R.string.notice_backup_message
    }
