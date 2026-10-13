package com.example.dayscounter

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Entity(tableName = "events")
data class Event(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val label: String = "Normal",
    val epochDay: Long,
    val createdAt: Long = System.currentTimeMillis(),
    val colorHex: Long = 0xFFF16529,
    val showYears: Boolean = false,
    val showMonths: Boolean = false,
    val showWeeks: Boolean = false,
    val showDays: Boolean = true
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

@Database(entities = [Event::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun eventDao(): EventDao
}

data class TimeDiff(val years: Long, val months: Long, val weeks: Long, val days: Long)

fun calculateTimeDiff(startDate: LocalDate, endDate: LocalDate, event: Event): TimeDiff {
    var current = startDate
    val years = if (event.showYears) ChronoUnit.YEARS.between(current, endDate).also { current = current.plusYears(it) } else 0L
    val months = if (event.showMonths) ChronoUnit.MONTHS.between(current, endDate).also { current = current.plusMonths(it) } else 0L
    val weeks = if (event.showWeeks) ChronoUnit.WEEKS.between(current, endDate).also { current = current.plusWeeks(it) } else 0L
    val days = if (event.showDays) ChronoUnit.DAYS.between(current, endDate) else 0L
    return TimeDiff(years, months, weeks, days)
}