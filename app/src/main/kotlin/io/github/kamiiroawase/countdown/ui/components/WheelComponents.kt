package io.github.kaimiroawase.countdown.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import io.github.kaimiroawase.countdown.R
import io.github.kaimiroawase.countdown.ui.theme.BrandOrange
import io.github.kaimiroawase.countdown.ui.theme.SelectionBand
import io.github.kaimiroawase.countdown.ui.theme.SurfaceWhite
import io.github.kaimiroawase.countdown.ui.theme.TextSecondary
import io.github.kaimiroawase.countdown.ui.theme.TextStrong
import kotlinx.coroutines.flow.distinctUntilChanged

private const val WHEEL_ITEM_HEIGHT = 44

private const val WHEEL_VISIBLE_COUNT = 5

@Composable
fun WheelColumn(
    items: List<String>,
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()

    val itemHeightPx = with(LocalDensity.current) { WHEEL_ITEM_HEIGHT.dp.toPx() }

    val centeredIndex by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex +
                if (listState.firstVisibleItemScrollOffset * 1f / itemHeightPx > 0.5f) 1 else 0
        }
    }

    LaunchedEffect(listState) {
        snapshotFlow { centeredIndex }
            .distinctUntilChanged()
            .collect { index -> if (index in items.indices) onSelect(index) }
    }

    LaunchedEffect(items, selected) {
        if (selected in items.indices && centeredIndex != selected) {
            listState.scrollToItem(selected)
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier.height((WHEEL_ITEM_HEIGHT * WHEEL_VISIBLE_COUNT).dp),
        flingBehavior = rememberSnapFlingBehavior(lazyListState = listState),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(vertical = (WHEEL_ITEM_HEIGHT * (WHEEL_VISIBLE_COUNT / 2)).dp),
    ) {
        items(items.size) { index ->
            Box(
                modifier = Modifier.height(WHEEL_ITEM_HEIGHT.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = items[index],
                    color = if (index == centeredIndex) TextStrong else TextSecondary,
                    fontSize = 17.sp,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun WheelSheetDialog(
    title: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    content: @Composable () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .fillMaxSize(),
            contentAlignment = Alignment.BottomCenter,
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .background(SurfaceWhite, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                        .navigationBarsPadding()
                        .padding(bottom = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                SheetGrabber()
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.action_cancel),
                        color = TextSecondary,
                        fontSize = 15.sp,
                        modifier =
                            Modifier
                                .clickableNoRipple(onDismiss)
                                .padding(16.dp),
                    )
                    Text(
                        text = title,
                        color = TextStrong,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = stringResource(R.string.action_confirm),
                        color = BrandOrange,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        modifier =
                            Modifier
                                .clickableNoRipple(onConfirm)
                                .padding(16.dp),
                    )
                }
                content()
            }
        }
    }
}

@Composable
fun TimeWheelDialog(
    initialHour: Int,
    initialMinute: Int,
    initialSecond: Int,
    onConfirm: (hour: Int, minute: Int, second: Int) -> Unit,
    onDismiss: () -> Unit,
) {
    var hour by remember { mutableIntStateOf(initialHour.coerceIn(0, 23)) }

    var minute by remember { mutableIntStateOf(initialMinute.coerceIn(0, 59)) }

    var second by remember { mutableIntStateOf(initialSecond.coerceIn(0, 59)) }

    WheelSheetDialog(
        title = stringResource(R.string.picker_title_time),
        onDismiss = onDismiss,
        onConfirm = { onConfirm(hour, minute, second) },
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
        ) {
            Box(
                modifier =
                    Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth()
                        .height(WHEEL_ITEM_HEIGHT.dp)
                        .background(SelectionBand, RoundedCornerShape(12.dp)),
            )
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
                WheelColumn(
                    items = (0..23).map { stringResource(R.string.picker_hour_wheel, it) },
                    selected = hour,
                    onSelect = { hour = it },
                    modifier = Modifier.weight(1f),
                )
                WheelColumn(
                    items = (0..59).map { stringResource(R.string.picker_minute_wheel, it) },
                    selected = minute,
                    onSelect = { minute = it },
                    modifier = Modifier.weight(1f),
                )
                WheelColumn(
                    items = (0..59).map { stringResource(R.string.picker_second_wheel, it) },
                    selected = second,
                    onSelect = { second = it },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
fun RepeatWheelDialog(
    initialInterval: Int,
    initialUnit: Int,
    intervalLabels: List<String>,
    unitLabels: List<String>,
    onConfirm: (interval: Int, unit: Int) -> Unit,
    onDismiss: () -> Unit,
) {
    var interval by remember { mutableIntStateOf(initialInterval.coerceIn(0, intervalLabels.size - 1)) }

    var unit by remember { mutableIntStateOf(initialUnit.coerceIn(0, unitLabels.size - 1)) }

    WheelSheetDialog(
        title = stringResource(R.string.picker_title_repeat),
        onDismiss = onDismiss,
        onConfirm = { onConfirm(interval, unit) },
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
        ) {
            Box(
                modifier =
                    Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth()
                        .height(WHEEL_ITEM_HEIGHT.dp)
                        .background(SelectionBand, RoundedCornerShape(12.dp)),
            )
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
                WheelColumn(
                    items = intervalLabels,
                    selected = interval,
                    onSelect = { new ->
                        interval = new

                        if (new == 0) {
                            unit = 0
                        } else if (unit == 0) {
                            unit = 1
                        }
                    },
                    modifier = Modifier.weight(1f),
                )
                WheelColumn(
                    items = unitLabels,
                    selected = unit,
                    onSelect = { new ->
                        unit = new
                        if (new == 0) {
                            interval = 0
                        } else if (interval == 0) {
                            interval = 1
                        }
                    },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}
