package io.github.kaimiroawase.countdown.ui.screen

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kaimiroawase.countdown.AppContainer
import io.github.kaimiroawase.countdown.R
import io.github.kaimiroawase.countdown.data.AppPrefs
import io.github.kaimiroawase.countdown.model.EventEntity
import io.github.kaimiroawase.countdown.model.infoItem
import io.github.kaimiroawase.countdown.model.sortedForDisplay
import io.github.kaimiroawase.countdown.ui.activity.EventDetailActivity
import io.github.kaimiroawase.countdown.ui.activity.EventEditActivity
import io.github.kaimiroawase.countdown.ui.activity.SubEventDetailActivity
import io.github.kaimiroawase.countdown.ui.components.EventGridCard
import io.github.kaimiroawase.countdown.ui.components.EventListCard
import io.github.kaimiroawase.countdown.ui.components.OrangeHeader
import io.github.kaimiroawase.countdown.ui.components.clickableNoRipple
import io.github.kaimiroawase.countdown.ui.theme.CountdownTheme
import io.github.kaimiroawase.countdown.ui.theme.OnPrimary
import io.github.kaimiroawase.countdown.ui.theme.PageBackground
import io.github.kaimiroawase.countdown.ui.theme.TextSecondary
import io.github.kamiiroawase.nexttime.RepeatUnit
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOf
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.ZonedDateTime
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun EventScreen(
    innerPadding: PaddingValues,
    previewEvents: List<EventEntity>? = null,
    previewListMode: Boolean = false,
) {
    val context = LocalContext.current

    var gridMode by remember {
        mutableStateOf(
            if (previewListMode) false else AppPrefs.getEventViewStyle() == AppPrefs.VIEW_STYLE_GRID,
        )
    }

    val events by
        (previewEvents?.let { flowOf(it) } ?: AppContainer.eventDao.observeAll())
            .collectAsState(initial = previewEvents ?: emptyList())

    var tick by remember { mutableLongStateOf(0L) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000.milliseconds)
            tick++
        }
    }

    val zone = remember(AppContainer.zoneId) { AppContainer.zoneId }

    val now = remember(tick) { ZonedDateTime.now(zone) }

    val listItems =
        remember(events, now) {
            events
                .sortedForDisplay(zone, now)
                .map { it.infoItem(context, zone, now) }
        }

    val gridItems = listItems.filter { it.event.parentId == 0L }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(PageBackground),
    ) {
        OrangeHeader(
            title = stringResource(R.string.app_name),
            actions = {
                HeaderIcon(
                    resId = R.drawable.ic_add,
                    onClick = {
                        context.startActivity(
                            Intent(
                                context,
                                EventEditActivity::class.java,
                            ),
                        )
                    },
                )
                HeaderIcon(
                    resId = if (gridMode) R.drawable.ic_view_grid else R.drawable.ic_view_list,
                    onClick = {
                        gridMode = !gridMode
                        AppPrefs.setEventViewStyle(
                            if (gridMode) AppPrefs.VIEW_STYLE_GRID else AppPrefs.VIEW_STYLE_LIST,
                        )
                    },
                )
            },
        )

        Box(
            modifier =
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(PageBackground)
                    .padding(bottom = innerPadding.calculateBottomPadding()),
            contentAlignment = Alignment.TopCenter,
        ) {
            Box(
                modifier =
                    Modifier
                        .widthIn(max = 488.dp)
                        .fillMaxSize(),
            ) {
                val isEmpty = if (gridMode) gridItems.isEmpty() else listItems.isEmpty()

                if (gridMode) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(top = 8.dp, end = 8.dp, bottom = 42.dp, start = 8.dp),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        items(gridItems.size) { index ->
                            val item = gridItems[index]

                            EventGridCard(
                                item = item,
                                onClick = { openDetail(context, item.event.id) },
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(top = 8.dp, bottom = 42.dp),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        items(listItems.size) { index ->
                            val item = listItems[index]

                            EventListCard(
                                item = item,
                                onClick = {
                                    openDetail(
                                        context,
                                        item.event.id,
                                        item.event.parentId,
                                    )
                                },
                            )
                        }
                    }
                }

                if (isEmpty) {
                    Text(
                        text = stringResource(R.string.event_list_empty),
                        color = TextSecondary,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.align(Alignment.Center),
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun EventScreenGridPreview() {
    CountdownTheme {
        Scaffold { innerPadding ->
            EventScreen(innerPadding, previewEvents = PreviewEvents.list)
        }
    }
}

@Preview
@Composable
private fun EventScreenListPreview() {
    CountdownTheme {
        Scaffold { innerPadding ->
            EventScreen(innerPadding, previewEvents = PreviewEvents.list, previewListMode = true)
        }
    }
}

private fun openDetail(
    context: Context,
    eventId: Long,
    parentId: Long = 0L,
) {
    val detailClass =
        if (parentId == 0L) {
            EventDetailActivity::class.java
        } else {
            SubEventDetailActivity::class.java
        }

    context.startActivity(
        Intent(context, detailClass).putExtra(
            EventDetailActivity.EXTRA_ID,
            eventId,
        ),
    )
}

@Composable
private fun HeaderIcon(
    resId: Int,
    onClick: () -> Unit,
) {
    Box(
        modifier =
            Modifier
                .width(50.dp)
                .clickableNoRipple(onClick = onClick)
                .padding(horizontal = 12.dp, vertical = 14.dp),
    ) {
        Icon(
            painter = painterResource(resId),
            contentDescription = null,
            tint = OnPrimary,
        )
    }
}

internal object PreviewEvents {
    val list: List<EventEntity> =
        listOf(
            event(
                id = 1,
                name = "遇见你",
                order = 1,
                day = previewDay(2024, 5, 20),
                hour = 13,
                minute = 14,
            ),
            event(
                id = 2,
                name = "国庆节",
                day = previewDay(2026, 10, 1),
                notify = true,
            ),
            event(
                id = 3,
                name = "重阳节",
                lunar = true,
                day = previewDay(2026, 10, 18),
                repeatInterval = 1,
                repeatUnit = RepeatUnit.YEAR,
            ),
            event(
                id = 4,
                name = "春节",
                lunar = true,
                day = previewDay(2026, 2, 17),
                notify = true,
                repeatInterval = 1,
                repeatUnit = RepeatUnit.YEAR,
            ),
            event(
                id = 5,
                name = "生日",
                day = previewDay(2027, 3, 8),
                hour = 9,
                minute = 30,
                repeatInterval = 1,
                repeatUnit = RepeatUnit.YEAR,
            ),
            event(
                id = 6,
                name = "准备年夜饭",
                parentId = 4,
                day = previewDay(2027, 2, 5),
                hour = 18,
                minute = 0,
            ),
        )

    private fun previewDay(
        year: Int,
        month: Int,
        day: Int,
    ): Long =
        LocalDate
            .of(year, month, day)
            .atStartOfDay(ZoneOffset.UTC)
            .toInstant()
            .toEpochMilli()

    private fun event(
        id: Long,
        name: String,
        order: Int = 0,
        parentId: Long = 0,
        lunar: Boolean = false,
        notify: Boolean = false,
        day: Long,
        hour: Int = 0,
        minute: Int = 0,
        repeatInterval: Int = 0,
        repeatUnit: Int = RepeatUnit.NONE,
    ): EventEntity =
        EventEntity(
            id = id,
            name = name,
            order = order,
            parentId = parentId,
            lunar = lunar,
            leapCount = false,
            notify = notify,
            targetDay = day,
            targetHour = hour,
            targetMinute = minute,
            targetSecond = 0,
            repeatInterval = repeatInterval,
            repeatUnit = repeatUnit,
        )
}
