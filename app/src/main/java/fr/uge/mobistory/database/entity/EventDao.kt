package fr.uge.mobistory.database.entity

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import java.time.LocalDate

@Dao
interface EventDao {

    @Query("SELECT * FROM event_table")
    fun getAll(): List<Event>

    @Query("SELECT * FROM event_table WHERE id == :id LIMIT 1")
    fun getById(id: Int): Event?

    @Query("SELECT * FROM event_table WHERE title LIKE :title LIMIT 1")
    fun findByName(title: String): Event

    @Query("SELECT * FROM event_table WHERE title LIKE :word")
    fun findAll(word: String): List<Event>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(event: Event)

    @Delete
    fun delete(user: Event)

}