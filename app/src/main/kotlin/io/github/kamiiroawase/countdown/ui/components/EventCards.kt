package io.github.kaimiroawase.countdown.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kaimiroawase.countdown.R
import io.github.kaimiroawase.countdown.model.EventItem
import io.github.kaimiroawase.countdown.ui.theme.BrandOrange
import io.github.kaimiroawase.countdown.ui.theme.CardFooter
import io.github.kaimiroawase.countdown.ui.theme.IconChevron
import io.github.kaimiroawase.countdown.ui.theme.OnPrimary
import io.github.kaimiroawase.countdown.ui.theme.SurfaceWhite
import io.github.kaimiroawase.countdown.ui.theme.TargetDayPast
import io.github.kaimiroawase.countdown.ui.theme.TextOnFooter
import io.github.kaimiroawase.countdown.ui.theme.TextPrimary
import io.github.kaimiroawase.countdown.ui.theme.TextSecondary
import io.github.kaimiroawase.countdown.ui.theme.TextStrong

@Composable
fun EventItem.metaText(): String =
    listOf(
        if (event.order > 0) {
            stringResource(R.string.event_meta_order, event.order)
        } else {
            stringResource(R.string.event_meta_order_default)
        },
        stringResource(
            if (event.parentId != 0L) R.string.event_meta_sub_event else R.string.event_meta_main_event,
        ),
        stringResource(if (event.notify) R.string.event_meta_notify_on else R.string.event_meta_notify_off),
    ).joinToString(stringResource(R.string.event_meta_separator))

@Composable
fun EventListCard(
    item: EventItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 64.dp)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.event.name,
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = item.metaText(),
                        color = TextSecondary,
                        fontSize = 13.sp,
                        maxLines = 1,
                    )
                    Icon(
                        painter = painterResource(R.drawable.ic_notify),
                        contentDescription = null,
                        tint = if (item.event.notify) BrandOrange else TextSecondary,
                        modifier =
                            Modifier
                                .padding(start = 4.dp)
                                .size(14.dp),
                    )
                }
                Text(
                    text = item.infoLineText(),
                    color = TextSecondary,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
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
}

@Composable
private fun EventItem.infoLineText(): String {
    val dateLine = if (time.isEmpty()) date else "$date $time"

    return stringResource(R.string.event_info_format, dateLine, prefix + value + unit)
}

@Composable
fun EventGridCard(
    item: EventItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier.padding(horizontal = 8.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
    ) {
        Column {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .background(if (item.past) TargetDayPast else BrandOrange)
                        .padding(horizontal = 6.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = item.event.name,
                        color = OnPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = item.prefix,
                        color = OnPrimary,
                        fontSize = 12.sp,
                        maxLines = 1,
                    )
                }
            }
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .background(SurfaceWhite)
                        .padding(vertical = 12.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = item.value,
                    color = TextStrong,
                    fontSize = 36.sp,
                    maxLines = 1,
                    modifier = Modifier.widthIn(max = 110.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = item.unit,
                    color = TextStrong,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .background(CardFooter)
                        .padding(top = 4.dp),
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = item.date,
                    color = TextOnFooter,
                    fontSize = 12.sp,
                    maxLines = 1,
                    modifier = Modifier.widthIn(max = 110.dp),
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = item.week,
                    color = TextOnFooter,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                text = item.time,
                color = TextOnFooter,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .background(CardFooter)
                        .padding(bottom = 4.dp),
            )
        }
    }
}
