package io.github.kaimiroawase.countdown.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import io.github.kaimiroawase.countdown.model.EventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {
    @Insert
    suspend fun insert(event: EventEntity): Long

    @Insert
    suspend fun insertAll(events: List<EventEntity>)

    @Update
    suspend fun update(event: EventEntity)

    @Query("UPDATE event SET background = :background WHERE id = :id")
    suspend fun updateBackground(
        id: Long,
        background: String,
    )

    @Delete
    suspend fun delete(event: EventEntity)

    @Query("DELETE FROM event WHERE parent_id = :parentId")
    suspend fun deleteByParentId(parentId: Long)

    @Transaction
    suspend fun deleteWithChildren(event: EventEntity) {
        deleteByParentId(event.id)
        delete(event)
    }

    @Query("SELECT * FROM event ORDER BY sort_order DESC")
    fun observeAll(): Flow<List<EventEntity>>

    @Query("SELECT * FROM event ORDER BY sort_order DESC")
    suspend fun getAll(): List<EventEntity>

    @Query("SELECT * FROM event WHERE parent_id = 0 ORDER BY sort_order DESC")
    suspend fun getTopLevel(): List<EventEntity>

    @Query("SELECT * FROM event WHERE id = :id")
    fun observeById(id: Long): Flow<EventEntity?>

    @Query("SELECT * FROM event WHERE parent_id = :parentId")
    fun observeByParentId(parentId: Long): Flow<List<EventEntity>>

    @Query("SELECT * FROM event WHERE id = :id")
    suspend fun getById(id: Long): EventEntity?

    @Query("SELECT * FROM event WHERE parent_id = :parentId")
    suspend fun getByParentId(parentId: Long): List<EventEntity>
}
