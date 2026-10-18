package io.github.kaimiroawase.countdown.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kaimiroawase.countdown.R
import io.github.kaimiroawase.countdown.ui.theme.BrandOrange
import io.github.kaimiroawase.countdown.ui.theme.SurfaceWhite
import io.github.kaimiroawase.countdown.ui.theme.TextStrong
import io.github.kaimiroawase.countdown.ui.theme.TextTertiary

@Composable
fun DayPickerDialog(
    title: String,
    initialMillis: Long,
    onConfirm: (Long) -> Unit,
    onDismiss: () -> Unit,
) {
    val state =
        rememberDatePickerState(
            initialSelectedDateMillis = if (initialMillis > 0) initialMillis else null,
        )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { state.selectedDateMillis?.let(onConfirm) }) {
                Text(stringResource(R.string.action_confirm), color = BrandOrange, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel), color = TextTertiary)
            }
        },
        colors = DatePickerDefaults.colors(containerColor = SurfaceWhite),
    ) {
        DatePicker(
            state = state,
            title = {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    color = TextStrong,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 24.dp, top = 16.dp),
                )
            },
            colors =
                DatePickerDefaults.colors(
                    selectedDayContainerColor = BrandOrange,
                    todayContentColor = BrandOrange,
                    todayDateBorderColor = BrandOrange,
                ),
        )
    }
}
