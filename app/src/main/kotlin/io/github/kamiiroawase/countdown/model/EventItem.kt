package io.github.kaimiroawase.countdown.model

data class EventItem(
    val event: EventEntity,
    val date: String,
    val time: String,
    val week: String,
    val prefix: String,
    val value: String,
    val unit: String,
    val past: Boolean,
    val daysValue: Long = 0L,
    val targetEpochDay: Long = 0L,
)
