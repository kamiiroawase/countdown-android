package io.github.kaimiroawase.countdown.ui.screen

import android.appwidget.AppWidgetManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kaimiroawase.countdown.AppContainer
import io.github.kaimiroawase.countdown.R
import io.github.kaimiroawase.countdown.data.AppPrefs
import io.github.kaimiroawase.countdown.model.EventEntity
import io.github.kaimiroawase.countdown.model.infoItem
import io.github.kaimiroawase.countdown.model.sortedForDisplay
import io.github.kaimiroawase.countdown.ui.components.EventGridCard
import io.github.kaimiroawase.countdown.ui.components.OrangeHeader
import io.github.kaimiroawase.countdown.ui.theme.BrandOrange
import io.github.kaimiroawase.countdown.ui.theme.OnPrimary
import io.github.kaimiroawase.countdown.ui.theme.PageBackground
import io.github.kaimiroawase.countdown.ui.theme.SurfaceWhite
import io.github.kaimiroawase.countdown.ui.theme.TextPrimary
import io.github.kaimiroawase.countdown.ui.theme.TextSecondary
import io.github.kaimiroawase.countdown.widget.EventCardWidgetProvider
import io.github.kaimiroawase.countdown.widget.EventListWidgetProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.ZonedDateTime

@Composable
fun WidgetConfigScreen(
    appWidgetId: Int,
    onClose: (resultOk: Boolean) -> Unit,
) {
    val context = LocalContext.current

    val providerClass =
        AppWidgetManager
            .getInstance(context)
            .getAppWidgetInfo(appWidgetId)
            ?.provider
            ?.className

    val isListMode = providerClass == EventListWidgetProvider::class.java.name

    val isCardMode = providerClass == EventCardWidgetProvider::class.java.name

    val events = remember { mutableStateOf<List<EventEntity>>(emptyList()) }

    val loaded = remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val topLevel = AppContainer.eventDao.getTopLevel()

        val zone = AppContainer.zoneId

        events.value = topLevel.sortedForDisplay(zone, ZonedDateTime.now(zone))

        loaded.value = true
    }

    var allMode by remember { mutableStateOf(true) }

    val pickedIds = remember { mutableStateOf(setOf<Long>()) }

    LaunchedEffect(Unit) {
        val binding = AppPrefs.getEventWidgetList(appWidgetId)

        allMode = binding == AppPrefs.WIDGET_BINDING_ALL

        if (binding != AppPrefs.WIDGET_BINDING_ALL) {
            pickedIds.value =
                binding.split(',').mapNotNull { it.toLongOrNull() }.toSet()
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(PageBackground)) {
        OrangeHeader(
            title = stringResource(R.string.widget_title),
            centered = true,
            navigationIcon = {
                Text(
                    text = stringResource(R.string.action_cancel),
                    color = OnPrimary,
                    fontSize = 15.sp,
                    modifier =
                        Modifier
                            .clickable { onClose(false) }
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                )
            },
        )

        Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
            when {
                loaded.value && events.value.isEmpty() -> {
                    Text(
                        text = stringResource(R.string.widget_no_events),
                        color = TextSecondary,
                        fontSize = 14.sp,
                        modifier = Modifier.align(Alignment.Center),
                    )
                }

                isCardMode -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(bottom = 24.dp),
                        modifier = Modifier.padding(top = 4.dp),
                    ) {
                        items(events.value.size) { index ->
                            val item = events.value[index].infoItem(context)

                            EventGridCard(item = item, onClick = {
                                val event = events.value[index]

                                AppPrefs.setEventWidgetCard(appWidgetId, event.id)

                                EventCardWidgetProvider.updateWidget(
                                    context.applicationContext,
                                    AppWidgetManager.getInstance(context.applicationContext),
                                    appWidgetId,
                                    event,
                                )

                                onClose(true)
                            })
                        }
                    }
                }

                isListMode -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 4.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SurfaceWhite)
                                    .heightIn(min = 54.dp)
                                    .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = stringResource(R.string.widget_select_all),
                                color = TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f),
                            )
                            Switch(
                                checked = allMode,
                                onCheckedChange = { allMode = it },
                                colors =
                                    SwitchDefaults.colors(
                                        checkedTrackColor = BrandOrange,
                                        checkedThumbColor = OnPrimary,
                                    ),
                            )
                        }

                        LazyColumn(
                            contentPadding = PaddingValues(bottom = 16.dp),
                            modifier = Modifier.weight(1f),
                        ) {
                            items(events.value.size) { index ->
                                val event = events.value[index]

                                Row(
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 4.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(SurfaceWhite)
                                            .clickable {
                                                pickedIds.value =
                                                    if (event.id in pickedIds.value) {
                                                        pickedIds.value - event.id
                                                    } else {
                                                        pickedIds.value + event.id
                                                    }
                                            }.heightIn(min = 54.dp)
                                            .padding(horizontal = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        text = event.name,
                                        color = TextPrimary,
                                        fontSize = 15.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f),
                                    )
                                    Switch(
                                        checked = event.id in pickedIds.value,
                                        onCheckedChange = { checked ->
                                            pickedIds.value =
                                                if (checked) pickedIds.value + event.id else pickedIds.value - event.id
                                        },
                                        colors =
                                            SwitchDefaults.colors(
                                                checkedTrackColor = BrandOrange,
                                                checkedThumbColor = OnPrimary,
                                            ),
                                    )
                                }
                            }
                        }

                        val scope = rememberCoroutineScope()

                        Box(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 16.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(BrandOrange)
                                    .clickable {
                                        val value =
                                            if (allMode || pickedIds.value.isEmpty()) {
                                                AppPrefs.WIDGET_BINDING_ALL
                                            } else {
                                                pickedIds.value.joinToString(",")
                                            }

                                        AppPrefs.setEventWidgetList(appWidgetId, value)

                                        val appContext = context.applicationContext

                                        scope.launch(Dispatchers.IO) {
                                            runCatching {
                                                EventListWidgetProvider.updateWidget(
                                                    appContext,
                                                    AppWidgetManager.getInstance(appContext),
                                                    appWidgetId,
                                                    EventListWidgetProvider.boundEvents(
                                                        AppContainer.eventDao.getTopLevel().sortedForDisplay(),
                                                        value,
                                                    ),
                                                )
                                            }

                                            withContext(Dispatchers.Main) {
                                                onClose(true)
                                            }
                                        }
                                    },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = stringResource(R.string.action_confirm),
                                color = OnPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 15.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}
