package fr.uge.mobistory.database

import androidx.room.TypeConverter
import java.time.LocalDate

class LocalDateConverter {

    @TypeConverter
    fun toDate(dateLong: Long?): LocalDate? {
        return if (dateLong == null) null else LocalDate.ofEpochDay(dateLong)
    }

    @TypeConverter
    fun fromDate(date: LocalDate?): Long? {
        return date?.toEpochDay()
    }
}