package io.github.kaimiroawase.countdown.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import io.github.kaimiroawase.countdown.R
import io.github.kaimiroawase.countdown.ui.theme.BrandOrange
import io.github.kaimiroawase.countdown.ui.theme.Divider
import io.github.kaimiroawase.countdown.ui.theme.Grabber
import io.github.kaimiroawase.countdown.ui.theme.OnPrimary
import io.github.kaimiroawase.countdown.ui.theme.SurfaceWhite
import io.github.kaimiroawase.countdown.ui.theme.TextStrong
import io.github.kaimiroawase.countdown.ui.theme.TextTertiary

@Composable
fun OrangeHeader(
    title: String,
    modifier: Modifier = Modifier,
    centered: Boolean = false,
    background: Color = BrandOrange,
    navigationIcon: (@Composable () -> Unit)? = null,
    actions: (@Composable RowScope.() -> Unit) = {},
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .background(background)
                .statusBarsPadding()
                .height(54.dp),
    ) {
        navigationIcon?.let { icon ->
            Box(modifier = Modifier.align(Alignment.CenterStart)) { icon() }
        }

        Text(
            text = title,
            color = OnPrimary,
            fontSize = if (centered) 18.sp else 20.sp,
            fontWeight = if (centered) FontWeight.Bold else null,
            textAlign = TextAlign.Center,
            modifier =
                if (centered) {
                    Modifier.align(Alignment.Center)
                } else {
                    Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 16.dp)
                },
        )

        Row(
            modifier = Modifier.align(Alignment.CenterEnd),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            actions()
        }
    }
}

@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    confirmColor: Color = TextTertiary,
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 48.dp)
                    .background(SurfaceWhite)
                    .padding(top = 24.dp),
        ) {
            Text(
                text = title,
                color = TextStrong,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
            )
            Text(
                text = message,
                color = TextTertiary,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 10.dp),
            )
            HorizontalDivider(thickness = 0.5.dp, color = Divider)
            Row(modifier = Modifier.fillMaxWidth().height(52.dp)) {
                Box(
                    modifier =
                        Modifier
                            .weight(1f)
                            .height(52.dp)
                            .clickable(onClick = onDismiss),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(R.string.action_cancel),
                        color = TextTertiary,
                        fontSize = 16.sp,
                    )
                }
                Box(
                    modifier =
                        Modifier
                            .width(0.5.dp)
                            .height(52.dp)
                            .background(Divider),
                )
                Box(
                    modifier =
                        Modifier
                            .weight(1f)
                            .height(52.dp)
                            .clickable(onClick = onConfirm),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = confirmText,
                        color = confirmColor,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Composable
fun SheetGrabber() {
    Box(
        modifier =
            Modifier
                .padding(top = 10.dp)
                .width(36.dp)
                .height(4.dp)
                .background(Grabber),
    )
}
