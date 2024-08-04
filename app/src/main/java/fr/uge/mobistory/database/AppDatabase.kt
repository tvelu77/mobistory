package fr.uge.mobistory.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import fr.uge.mobistory.database.entity.Event
import fr.uge.mobistory.database.entity.EventDao
import fr.uge.mobistory.database.entity.EventLocation
import fr.uge.mobistory.database.entity.EventLocationDao
import fr.uge.mobistory.database.entity.EventRelation
import fr.uge.mobistory.database.entity.EventRelationDao

@Database(entities = [Event::class, EventRelation::class, EventLocation::class], version = 1)
@TypeConverters(LocalDateConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun eventDao(): EventDao
    abstract fun eventRelationDao(): EventRelationDao
    abstract fun eventLocationDao(): EventLocationDao
}