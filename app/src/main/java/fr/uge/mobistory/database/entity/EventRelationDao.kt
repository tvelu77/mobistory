package fr.uge.mobistory.database.entity

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface EventRelationDao {

    @Query("SELECT * FROM event_relation_table")
    fun getAll(): List<EventRelation>

    @Query("SELECT * FROM event_relation_table WHERE eventId == :eventId")
    fun findAll(eventId: Int): List<EventRelation>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(eventRelation: EventRelation)

    @Delete
    fun delete(eventRelation: EventRelation)
}