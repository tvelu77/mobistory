package fr.uge.mobistory.database

import android.content.Context
import android.os.StrictMode
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.ui.platform.LocalContext
import androidx.room.Room
import fr.uge.mobistory.R
import fr.uge.mobistory.database.entity.Event
import fr.uge.mobistory.database.entity.EventLocation
import fr.uge.mobistory.database.entity.EventRelation
import fr.uge.mobistory.database.json.EventJSON
import fr.uge.mobistory.localstorage.AppConfig
import fr.uge.mobistory.localstorage.AppData
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.net.URL
import java.nio.charset.Charset
import java.time.LocalDate
import java.util.Locale
import java.util.zip.GZIPInputStream
import kotlin.streams.toList

class DatabaseManager(context: Context) {
    private val db: AppDatabase
    private val urlEventsZip: URL

    init {
        db = Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "event"
        ).allowMainThreadQueries().build()
        urlEventsZip = URL(context.getString(R.string.events_zip_url))
    }

    companion object{
        private const val DEFAULT_LANGUAGE = "en"
        private const val FORMAT_DATE_FIELD = "date:"
        private const val FORMAT_LOCATION_FIELD = "geo:"
    }

    /**
     * Return the app database
     */
    fun getDb(): AppDatabase{
        return db
    }

    @Composable
    fun LoadDatabase(appData: AppData) {
        val systemLanguage = Locale.getDefault().language
        // Load database if not the same language than previously
        if(appData.appConfig.language == "" || appData.appConfig.language != systemLanguage){
            if(systemLanguage == "fr"){
                appData.appConfig.language = systemLanguage
            }else{
                appData.appConfig.language = "en"
            }
            renewDatabase(appData.appConfig)
        }
    }

    private fun downloadEvents(): List<String> {
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        // Retrieve events from server
        val connection = urlEventsZip.openConnection()
        val stream = GZIPInputStream(connection.getInputStream())
        val reader = BufferedReader(
            stream.reader(
                Charset.forName("UTF-8")
            )
        )
        return reader.lines().toList()
    }

    /**
     * Create or Update all events in the database
     */
    fun updateDatabase(language: String, updatingEvents: MutableState<Boolean>?){
       Thread {
           downloadEvents().forEach { line ->
               val eventJSON = Json.decodeFromString<EventJSON>(line)
               loadEventDataInDatabase(language, eventJSON)
           }
           updatingEvents?.value = false
       }.start()
    }

    /**
     * If the device language changed
     */
    private fun renewDatabase(appConfig: AppConfig){
        // Clear database
        db.clearAllTables()

        //Load events
        downloadEvents().forEach { line ->
            val eventJSON = Json.decodeFromString<EventJSON>(line)
            loadEventDataInDatabase(appConfig.language, eventJSON)
        }
        //updateDatabase(appConfig, updatingEvents)
    }

    /**
     * Read JSON file of the event and put it into the database or update it if already exist
     */
    private fun loadEventDataInDatabase(language: String, eventJSON: EventJSON) {
        val eventDao = db.eventDao()
        val eventRelationDao = db.eventRelationDao()
        val eventLocationDao = db.eventLocationDao()

        // Load event claims
        var startTime : String? = null
        var endTime : String? = null
        var location : String? = null
        // Foreach claims retrieve information's
        for(claim in eventJSON.claims){
            // If there is a date then it is an event
            when (claim.id) {
                580 -> {
                    startTime = claim.value
                    continue
                }
                582 -> {
                    endTime = claim.value
                    continue
                }
                585 -> {
                    startTime = claim.value
                    continue
                }
                625 -> {
                    location = claim.value
                    continue
                }
            }

            if(claim.item != null){
                val relation = EventRelation(eventJSON.id, claim.item.id )
                eventRelationDao.insert(relation)
            }
        }

        // Add item location in event location database
        if(location != null){
            val eventLocation = parseGeolocation(eventJSON.id, location)
            eventLocationDao.insert(eventLocation)
        }



        // Create event there is a date
        if(startTime != null) {
            val startDate = toLocalDate(startTime)
            val endDate = toLocalDate(endTime ?: startTime)
            // Remove older event than -1 000 000
            if(startDate.year < -1_000_000 || endDate.year < -1_000_000 ){
                return
            }
            val event = Event(
                eventJSON.id,
                translate(language, eventJSON.label),
                translate(language, eventJSON.description),
                if (eventJSON.wikipedia != null) translate(language, eventJSON.wikipedia) else "",
                eventJSON.popularity.getOrDefault(language, Integer.MAX_VALUE),
                startDate,
                endDate
            )
            eventDao.insert(event)
        }
    }

     /**
     * Parses a string of the following format "date:year-month-day"
     * and returns a LocalDate.
     *
     * @param dateString String, the date in string.
     * @return LocalDate.
     */
    private fun toLocalDate(dateString: String): LocalDate {
        val date = dateString.substring(FORMAT_DATE_FIELD.length)
        val split = date.split("-")
        val year: Int
        var month = 1
        var day = 1
        var index = 0
        year = if (split.size == 4) { // The year is negative
            index++
            ("-" + split[index]).toInt()
        } else {
            split[index].toInt()
        }
        index++
        if (split[index] != "0") {
            month = split[index].toInt()
        }
        index++
        if (split[index] != "0") {
            day = split[index].toInt()
        }
        return LocalDate.of(year, month, day)
    }

    /**
     * Parse geolocation of the event
     */
    private fun parseGeolocation(eventId: Int, locationRaw: String): EventLocation{
        val location = locationRaw.substring(FORMAT_LOCATION_FIELD.length).split(",")
        val latitude = location[0].toDouble()
        val longitude = location[1].toDouble()
        return EventLocation(eventId,longitude, latitude)
    }

    /**
     * Translate strings depending on the language
     */
    private fun translate(language: String, text: String): String{
        val splitText = text.split("||")

        // If text is empty return empty string
        if(splitText.isEmpty()){
            return ""
        }

        // Search for the current language
        splitText.forEach() { entry ->
            entry.startsWith("$language:")
            if(entry.startsWith("$language:")){
                return entry.substring(language.length + 1)
            }
        }

        // If language not found use the english one
        splitText.forEach() { entry ->
            if(entry.startsWith("$DEFAULT_LANGUAGE:")){
                return entry.substring(DEFAULT_LANGUAGE.length + 1)
            }
        }

        // If default not found get the first language
        splitText.forEach() { entry ->
            val position = entry.indexOf(":")
            if(position >= 0){
                return entry.substring(position + 1)
            }
        }

        return ""
    }
}