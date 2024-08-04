package fr.uge.mobistory.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import fr.uge.mobistory.database.LocalDateConverter
import java.sql.Date
import java.time.LocalDate

@Entity(tableName = "event_table")
data class Event(
    @PrimaryKey
    val id: Int,
    @ColumnInfo()
    val title: String,
    @ColumnInfo()
    val description: String,
    @ColumnInfo()
    val wikipedia: String,
    @ColumnInfo()
    val popularity: Int,
    @ColumnInfo()
    val beginDate: LocalDate,
    @ColumnInfo()
    val endDate: LocalDate
)
