package io.github.kaimiroawase.countdown

import android.app.Application
import androidx.room.Room
import io.github.kaimiroawase.countdown.data.AppDatabase
import io.github.kaimiroawase.countdown.data.EventDao
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object AppContainer {
    const val TIME_TEXT_FORMAT = "%02d:%02d:%02d"

    private var database: AppDatabase? = null

    var zoneId: ZoneId = ZoneId.systemDefault()

    val eventDao: EventDao get() = database!!.eventDao()

    val weekFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("EEEE")

    val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    fun init(app: Application) {
        database = Room.databaseBuilder(app, AppDatabase::class.java, "countdown.db").build()
    }

    fun resetZoneId(zoneId: String?) {
        this.zoneId = zoneId
            ?.let { runCatching { ZoneId.of(it) }.getOrNull() }
            ?: ZoneId.systemDefault()
    }
}
