package io.github.kaimiroawase.countdown.ui.screen

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import io.github.kaimiroawase.countdown.AppContainer
import io.github.kaimiroawase.countdown.R
import io.github.kaimiroawase.countdown.data.EventBackgroundStore
import io.github.kaimiroawase.countdown.model.EventEntity
import io.github.kaimiroawase.countdown.model.EventItem
import io.github.kaimiroawase.countdown.model.infoItem
import io.github.kaimiroawase.countdown.model.sortedForDisplay
import io.github.kaimiroawase.countdown.ui.activity.EventEditActivity
import io.github.kaimiroawase.countdown.ui.activity.SubEventDetailActivity
import io.github.kaimiroawase.countdown.ui.components.SheetGrabber
import io.github.kaimiroawase.countdown.ui.components.clickableNoRipple
import io.github.kaimiroawase.countdown.ui.theme.BrandOrange
import io.github.kaimiroawase.countdown.ui.theme.CardFooter
import io.github.kaimiroawase.countdown.ui.theme.Divider
import io.github.kaimiroawase.countdown.ui.theme.IconChevron
import io.github.kaimiroawase.countdown.ui.theme.OnPrimary
import io.github.kaimiroawase.countdown.ui.theme.SurfaceWhite
import io.github.kaimiroawase.countdown.ui.theme.TargetDayPast
import io.github.kaimiroawase.countdown.ui.theme.TextOnFooter
import io.github.kaimiroawase.countdown.ui.theme.TextPrimary
import io.github.kaimiroawase.countdown.ui.theme.TextSecondary
import io.github.kaimiroawase.countdown.ui.theme.TextStrong
import io.github.kaimiroawase.countdown.ui.theme.TextTertiary
import io.github.kamiiroawase.nexttime.calendarCountdown
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import java.time.LocalDate
import java.time.ZonedDateTime
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    eventId: Long,
    onClose: () -> Unit,
) {
    val context = LocalContext.current

    var event by remember { mutableStateOf<EventEntity?>(null) }

    var eventLoaded by remember { mutableStateOf(false) }

    var subEvents by remember { mutableStateOf<List<EventEntity>>(emptyList()) }

    var subEventsLoaded by remember { mutableStateOf(false) }

    LaunchedEffect(eventId) {
        AppContainer.eventDao.observeById(eventId).collect {
            eventLoaded = true
            event = it
        }
    }

    LaunchedEffect(eventId) {
        AppContainer.eventDao.observeByParentId(eventId).collect {
            subEventsLoaded = true
            subEvents = it
        }
    }

    var tick by remember { mutableStateOf(0L) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000.milliseconds)
            tick++
        }
    }

    val zone = remember(AppContainer.zoneId) { AppContainer.zoneId }

    val now = remember(tick) { ZonedDateTime.now(zone) }

    val item = remember(event, now) { event?.infoItem(context, zone, now) }

    val subItems =
        remember(subEvents, now) {
            subEvents
                .sortedForDisplay(zone, now)
                .map { it.infoItem(context, zone, now) }
        }

    LaunchedEffect(eventLoaded, event) {
        if (eventLoaded && event == null) onClose()
    }

    var fullscreen by rememberSaveable { mutableStateOf(false) }

    var hideSubEventsInFullscreen by rememberSaveable { mutableStateOf(false) }

    var statusBarHeightPx by rememberSaveable { mutableIntStateOf(0) }

    val view = LocalView.current

    val density = LocalDensity.current

    val scope = rememberCoroutineScope()

    SideEffect {
        if (view.isInEditMode) return@SideEffect

        val window = (view.context as Activity).window

        val controller = WindowCompat.getInsetsController(window, view)

        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        if (fullscreen) {
            controller.hide(WindowInsetsCompat.Type.statusBars())
        } else {
            controller.show(WindowInsetsCompat.Type.statusBars())

            val top =
                WindowInsetsCompat
                    .toWindowInsetsCompat(view.rootWindowInsets)
                    .getInsets(WindowInsetsCompat.Type.statusBars())
                    .top

            if (top > 0) statusBarHeightPx = top
        }
    }

    val statusBarHeight =
        statusBarHeightPx
            .takeIf { it >= 0 }
            ?: WindowInsets.statusBars.getTop(density)

    val statusBarDp = with(density) { statusBarHeight.toDp() }

    BackHandler {
        if (fullscreen) fullscreen = false else onClose()
    }

    var showActions by remember { mutableStateOf(false) }

    val backgroundName = event?.background ?: ""

    var backgroundBitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(backgroundName) {
        backgroundBitmap =
            if (backgroundName.isBlank()) {
                null
            } else {
                EventBackgroundStore.decode(context, backgroundName)
            }
    }

    val backgroundPicker =
        rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                val appContext = context.applicationContext

                scope.launch {
                    EventBackgroundStore.save(appContext, eventId, uri)
                }
            }
        }

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .pointerInput(fullscreen) {
                    detectTapGestures { if (fullscreen) fullscreen = false }
                },
    ) {
        val bitmap = backgroundBitmap

        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Image(
                painter = painterResource(R.drawable.ic_event_detail_bg),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }

        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(statusBarDp + 54.dp)
                        .padding(top = statusBarDp),
            ) {
                if (!fullscreen) {
                    Icon(
                        painter = painterResource(R.drawable.ic_back),
                        contentDescription = null,
                        tint = OnPrimary,
                        modifier =
                            Modifier
                                .align(Alignment.CenterStart)
                                .clickableNoRipple { onClose() }
                                .padding(horizontal = 12.dp, vertical = 16.dp),
                    )
                    Text(
                        text = stringResource(R.string.app_name),
                        color = OnPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.align(Alignment.Center),
                    )
                    Icon(
                        painter = painterResource(R.drawable.ic_edit),
                        contentDescription = null,
                        tint = OnPrimary,
                        modifier =
                            Modifier
                                .align(Alignment.CenterEnd)
                                .clickableNoRipple { showActions = true }
                                .padding(horizontal = 12.dp, vertical = 12.dp),
                    )
                }
            }

            Column(
                modifier =
                    Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .navigationBarsPadding(),
            ) {
                item?.let { currentItem ->
                    DetailCountdownCard(item = currentItem)

                    val hasSubEvents = subItems.isNotEmpty()

                    val showSubEvents = hasSubEvents && !hideSubEventsInFullscreen

                    if (showSubEvents) {
                        Text(
                            text = stringResource(R.string.detail_sub_events_title),
                            color = OnPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            modifier =
                                Modifier
                                    .padding(start = 36.dp, bottom = 8.dp),
                        )
                        Column(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 32.dp)
                                    .padding(bottom = 24.dp),
                        ) {
                            subItems.forEach { sub ->
                                SubEventRow(item = sub) {
                                    context.startActivity(
                                        Intent(context, SubEventDetailActivity::class.java)
                                            .putExtra(SubEventDetailActivity.EXTRA_ID, sub.event.id),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showActions) {
        ModalBottomSheet(
            onDismissRequest = { showActions = false },
            containerColor = SurfaceWhite,
            dragHandle = { SheetGrabber() },
        ) {
            val hasBackground = backgroundName.isNotBlank() && EventBackgroundStore.backgroundFile(context, backgroundName).exists()

            Spacer(modifier = Modifier.height(16.dp))

            ActionRow(
                text = stringResource(R.string.detail_action_fullscreen),
                showDivider = false,
            ) {
                showActions = false
                fullscreen = true
                hideSubEventsInFullscreen = false
            }
            if (event?.parentId == 0L) {
                ActionRow(text = stringResource(R.string.detail_action_fullscreen_hide_sub)) {
                    showActions = false
                    fullscreen = true
                    hideSubEventsInFullscreen = true
                }
            }
            if (event?.parentId == 0L) {
                ActionRow(text = stringResource(R.string.detail_action_add_sub_event)) {
                    showActions = false
                    context.startActivity(
                        Intent(context, EventEditActivity::class.java)
                            .putExtra(EventEditActivity.EXTRA_PARENT_ID, eventId),
                    )
                }
            }
            ActionRow(text = stringResource(R.string.detail_action_edit)) {
                showActions = false
                context.startActivity(
                    Intent(context, EventEditActivity::class.java)
                        .putExtra(EventEditActivity.EXTRA_ID, eventId),
                )
            }
            ActionRow(text = stringResource(R.string.detail_action_background)) {
                showActions = false
                backgroundPicker.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                )
            }
            if (hasBackground) {
                ActionRow(
                    text = stringResource(R.string.detail_action_background_reset),
                    color = TextTertiary,
                ) {
                    showActions = false
                    val appContext = context.applicationContext
                    scope.launch {
                        EventBackgroundStore.clear(appContext, eventId)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun DetailCountdownCard(item: EventItem) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 32.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(SurfaceWhite),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(if (item.past) TargetDayPast else BrandOrange)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center,
        ) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = item.event.name,
                    color = OnPrimary,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.widthIn(max = 260.dp),
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = item.prefix,
                    color = OnPrimary,
                    fontSize = 20.sp,
                    maxLines = 1,
                )
            }
        }
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(SurfaceWhite)
                    .padding(vertical = 32.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(text = item.value, color = TextStrong, fontSize = 72.sp)
            Spacer(Modifier.width(8.dp))
            Text(
                text = item.unit,
                color = TextStrong,
                fontSize = 24.sp,
                modifier = Modifier.padding(bottom = 8.dp),
            )
        }
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(CardFooter)
                    .padding(top = 12.dp),
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(text = item.date, color = TextOnFooter, fontSize = 16.sp, maxLines = 1)
            Spacer(Modifier.width(4.dp))
            Text(
                text = item.week,
                color = TextOnFooter,
                fontSize = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (item.time.isNotEmpty()) {
            Text(
                text = item.time,
                color = TextOnFooter,
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .background(CardFooter)
                        .padding(top = 4.dp, bottom = 12.dp),
            )
        }
    }
}

@Composable
private fun SubEventRow(
    item: EventItem,
    onClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceWhite)
                .clickable(onClick = onClick)
                .heightIn(min = 52.dp)
                .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = item.event.name,
            color = TextPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = subCountdownText(item),
            color = if (item.past) TargetDayPast else TextSecondary,
            fontSize = 13.sp,
            maxLines = 1,
            modifier = Modifier.padding(start = 8.dp),
        )
        Icon(
            painter = painterResource(R.drawable.ic_chevron_right),
            contentDescription = null,
            tint = IconChevron,
            modifier =
                Modifier
                    .padding(start = 4.dp)
                    .size(18.dp),
        )
    }
}

@Composable
private fun subCountdownText(item: EventItem): String {
    if (item.daysValue <= 365L) return item.prefix + item.value + item.unit

    val zone = AppContainer.zoneId

    val cc =
        calendarCountdown(
            Instant.fromEpochMilliseconds(
                LocalDate
                    .ofEpochDay(item.targetEpochDay)
                    .atStartOfDay(zone)
                    .toInstant()
                    .toEpochMilli(),
            ),
            Instant.fromEpochMilliseconds(
                LocalDate
                    .now(zone)
                    .atStartOfDay(zone)
                    .toInstant()
                    .toEpochMilli(),
            ),
            TimeZone.of(zone.id),
        )

    return buildString {
        append(item.prefix)
        append(cc.years)
        append(stringResource(R.string.countdown_unit_year))
        if (cc.months > 0) {
            append(cc.months)
            append(stringResource(R.string.countdown_unit_month))
        }
        if (cc.days > 0) {
            append(cc.days)
            append(stringResource(R.string.countdown_unit_day))
        }
    }
}

@Composable
private fun ActionRow(
    text: String,
    color: Color = TextStrong,
    showDivider: Boolean = true,
    onClick: () -> Unit,
) {
    if (showDivider) {
        HorizontalDivider(thickness = 0.5.dp, color = Divider)
    }

    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(54.dp)
                .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = text, color = color, fontSize = 16.sp)
    }
}
