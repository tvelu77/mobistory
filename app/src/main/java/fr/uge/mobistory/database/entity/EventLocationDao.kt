package fr.uge.mobistory.database.entity

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface EventLocationDao {

    @Query("SELECT * FROM event_location_table")
    fun getAll(): List<EventLocation>

    @Query("SELECT * FROM event_location_table WHERE longitude >= :minLongitude AND longitude <= :maxLongitude AND latitude >= :minLatitude AND latitude <= :maxLatitude")
    fun findAllInRange(minLongitude: Double,  minLatitude: Double, maxLongitude: Double,  maxLatitude: Double): List<EventLocation>

    @Query("SELECT * FROM event_location_table WHERE eventId == :eventId LIMIT 1")
    fun findById(eventId: Int): EventLocation

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(eventLocation: EventLocation)

    @Delete
    fun delete(eventLocation: EventLocation)

}