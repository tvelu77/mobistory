package fr.uge.mobistory.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "event_location_table")
data class EventLocation(
    @PrimaryKey
    val eventId: Int,
    @ColumnInfo()
    val longitude: Double,
    @ColumnInfo()
    val latitude: Double
)