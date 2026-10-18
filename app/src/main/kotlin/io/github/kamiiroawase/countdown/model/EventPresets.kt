package io.github.kaimiroawase.countdown.model

import android.content.Context
import io.github.kaimiroawase.countdown.AppContainer
import io.github.kaimiroawase.countdown.R
import io.github.kamiiroawase.nexttime.RepeatUnit
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.ZonedDateTime

object EventPresets {
    private val presets =
        listOf(
            Preset(R.string.event_preset_spring_festival, true, LocalDate.of(2026, 2, 17)),
            Preset(R.string.event_preset_chongyang, true, LocalDate.of(2026, 10, 18)),
        )

    fun presetEvents(context: Context): List<EventEntity> {
        val launchMoment = ZonedDateTime.now(AppContainer.zoneId)

        val meetYou =
            EventEntity(
                name = context.getString(R.string.event_preset_meet_you),
                order = 1,
                lunar = false,
                leapCount = false,
                notify = false,
                targetDay =
                    launchMoment
                        .toLocalDate()
                        .atStartOfDay(ZoneOffset.UTC)
                        .toInstant()
                        .toEpochMilli(),
                targetHour = launchMoment.hour,
                targetMinute = launchMoment.minute,
                targetSecond = launchMoment.second,
                repeatInterval = 0,
                repeatUnit = RepeatUnit.NONE,
            )

        return listOf(meetYou) +
            presets.mapIndexed { _, preset ->
                EventEntity(
                    name = context.getString(preset.nameRes),
                    order = 0,
                    lunar = preset.lunar,
                    leapCount = false,
                    notify = false,
                    targetDay =
                        preset.date
                            .atStartOfDay(ZoneOffset.UTC)
                            .toInstant()
                            .toEpochMilli(),
                    targetHour = 0,
                    targetMinute = 0,
                    targetSecond = 0,
                    repeatInterval = 1,
                    repeatUnit = RepeatUnit.YEAR,
                )
            }
    }

    private data class Preset(
        val nameRes: Int,
        val lunar: Boolean,
        val date: LocalDate,
    )
}
