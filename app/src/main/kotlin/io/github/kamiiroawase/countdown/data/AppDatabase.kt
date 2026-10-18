package io.github.kaimiroawase.countdown.data

import androidx.room.Database
import androidx.room.RoomDatabase
import io.github.kaimiroawase.countdown.model.EventEntity

@Database(entities = [EventEntity::class], version = 1, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {
    abstract fun eventDao(): EventDao
}
