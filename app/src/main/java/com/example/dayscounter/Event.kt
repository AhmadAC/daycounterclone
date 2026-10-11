package com.example.dayscounter

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "events")
data class Event(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val epochDay: Long, // LocalDate.toEpochDay()
    val createdAt: Long = System.currentTimeMillis(),
    val colorHex: Long = 0xFFF16529, // Default Orange
    val displayMode: String = "DAYS" // "DAYS" or "YMD"
)

@Dao
interface EventDao {
    @Query("SELECT * FROM events")
    fun getAllEvents(): Flow<List<Event>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(event: Event)

    @Delete
    suspend fun delete(event: Event)
}

@Database(entities = [Event::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun eventDao(): EventDao
}
