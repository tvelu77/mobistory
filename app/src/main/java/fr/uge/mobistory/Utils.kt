package fr.uge.mobistory

import android.util.Log
import fr.uge.mobistory.database.DatabaseManager
import fr.uge.mobistory.database.entity.Event
import fr.uge.mobistory.localstorage.AppData
import java.time.LocalDate
import java.util.Random

class Utils {
    companion object{

        fun getDailyEvent(dbManager: DatabaseManager, currentDate: LocalDate): Event?{
            val allDailyEvents = getAllDailyEvents(dbManager, currentDate)
            if(allDailyEvents.isEmpty()){
                return null
            }
            val event = allDailyEvents.minBy { ev -> ev.popularity } // Get event with better popularity
            Log.i("DAILY EVENT", event.toString())
            return event
        }

        private fun getAllDailyEvents(dbManager: DatabaseManager, currentDate: LocalDate): List<Event>{
            val events = dbManager.getDb().eventDao().getAll()
            if(events.isEmpty()){
                return ArrayList()
            }
            val ev = events.filter { event ->
                val eventBeginDate = event.beginDate
                val modifiedCurrentDate = LocalDate.of(eventBeginDate.year, currentDate.monthValue,currentDate.dayOfMonth)
                eventBeginDate.isEqual(modifiedCurrentDate)
            }
            return ev
        }

    }
}