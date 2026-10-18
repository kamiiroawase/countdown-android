package io.github.kaimiroawase.countdown.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kaimiroawase.countdown.R
import io.github.kaimiroawase.countdown.ui.theme.BrandOrange
import io.github.kaimiroawase.countdown.ui.theme.CardFooter
import io.github.kaimiroawase.countdown.ui.theme.Divider
import io.github.kaimiroawase.countdown.ui.theme.IconChevron
import io.github.kaimiroawase.countdown.ui.theme.OnPrimary
import io.github.kaimiroawase.countdown.ui.theme.SurfaceWhite
import io.github.kaimiroawase.countdown.ui.theme.TextHint
import io.github.kaimiroawase.countdown.ui.theme.TextPrimary
import io.github.kaimiroawase.countdown.ui.theme.TextSecondary
import io.github.kaimiroawase.countdown.ui.theme.TextStrong
import io.github.kaimiroawase.countdown.ui.theme.TextTertiary

@Composable
fun SectionTitle(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(text = text, fontSize = 13.sp, color = TextSecondary, modifier = modifier)
}

@Composable
fun FormCard(content: @Composable () -> Unit) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceWhite),
    ) {
        content()
    }
}

@Composable
fun RowDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 20.dp),
        thickness = 1.dp,
        color = Divider,
    )
}

@Composable
fun InputRow(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    hint: String,
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .heightIn(min = 54.dp)
                .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = label, fontSize = 16.sp, color = TextPrimary, modifier = Modifier.width(72.dp))
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = TextStyle(fontSize = 15.sp, color = TextStrong, textAlign = TextAlign.End),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            decorationBox = { inner ->
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                    if (value.isEmpty()) {
                        Text(text = hint, fontSize = 15.sp, color = TextHint)
                    }
                    inner()
                }
            },
            modifier = Modifier.weight(1f).padding(vertical = 14.dp),
        )
    }
}

@Composable
fun FormPickRow(
    label: String,
    value: String,
    valueColor: Color,
    onClick: () -> Unit,
    valueFontSize: TextUnit = 15.sp,
    trailing: @Composable () -> Unit = {},
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
        Text(text = label, fontSize = 16.sp, color = TextPrimary, modifier = Modifier.width(72.dp))
        Text(
            text = value,
            fontSize = valueFontSize,
            color = valueColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f).padding(start = 16.dp, top = 14.dp, bottom = 14.dp),
        )
        trailing()
        Icon(
            painter = painterResource(R.drawable.ic_chevron_right),
            contentDescription = null,
            tint = IconChevron,
            modifier = Modifier.padding(start = 6.dp).size(20.dp),
        )
    }
}

@Composable
fun SwitchRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .heightIn(min = 54.dp)
                .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = label, fontSize = 16.sp, color = TextPrimary)
        Spacer(Modifier.weight(1f))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors =
                SwitchDefaults.colors(
                    checkedTrackColor = BrandOrange,
                    checkedThumbColor = OnPrimary,
                    uncheckedTrackColor = CardFooter,
                    uncheckedThumbColor = SurfaceWhite,
                    uncheckedBorderColor = Divider,
                ),
        )
    }
}
