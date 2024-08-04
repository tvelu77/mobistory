package fr.uge.mobistory.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity


@Entity(tableName = "event_relation_table", primaryKeys = ["eventId", "relatedEventId"])
data class EventRelation(
    val eventId: Int,
    val relatedEventId: Int,
)