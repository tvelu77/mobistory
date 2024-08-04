package fr.uge.mobistory.localstorage

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.nio.file.Path
import java.util.Locale

class AppConfig(context: Context) {
    val context: Context
    private val settings: SharedPreferences
    private val editor: SharedPreferences.Editor
    init {
        this.context = context
        this.settings = context.getSharedPreferences(APP_CONFIG_KEY, 0)
        this.editor = settings.edit()
    }

    var language: String
        get() {
            return settings.getString("language", "") ?: ""
        }
        set(value) {
            editor.putString("language", value)
            editor.apply()
        }

    var lastEventUpdate: Long
        get() {
            return settings.getLong("lastEventUpdate", -1)
        }
        set(value) {
            editor.putLong("lastEventUpdate", value)
            editor.apply()
        }

    var enableEventLocation: Boolean
        get() {
            return settings.getBoolean("enableEventLocation", false)
        }
        set(value) {
            editor.putBoolean("enableEventLocation", value)
            editor.apply()
        }

    var enableDailyEvent: Boolean
        get() {
            return settings.getBoolean("enableDailyEvent", false)
        }
        set(value) {
            editor.putBoolean("enableDailyEvent", value)
            editor.apply()
        }

    var enableEventAutoUpdate: Boolean
        get() {
            return settings.getBoolean("enableEventAutoUpdate", false)
        }
        set(value) {
            editor.putBoolean("enableEventAutoUpdate", value)
            editor.apply()
        }

    var favoriteEvents: List<Int>
        get() {
            val content = settings.getString("favoriteEvents", "")
            if(content.isNullOrEmpty()){
                return ArrayList()
            }
            return Json.decodeFromString(content)
        }
        set(value) {
            val content = Json.encodeToString(value)
            editor.putString("favoriteEvents", content)
            editor.apply()
        }

    fun addToFavorites(id: Int) {
        val tmp = favoriteEvents.toMutableList()
        tmp.add(id)
        favoriteEvents = tmp
    }

    fun remove(id: Int) {
        val tmp = favoriteEvents.toMutableList()
        tmp.remove(id)
        favoriteEvents = tmp
    }

    fun isAlreadyAdded(id: Int): Boolean {
        val index = favoriteEvents.indexOf(id)
        if (index == -1) {
            return false
        }
        return true
    }

    companion object {
        private const val APP_CONFIG_KEY = "APP_CONFIG"

        /*
        fun loadConfig(context: Context): AppConfig {

            val settings = context.getSharedPreferences(APP_CONFIG_KEY, 0)
            val configJson = settings.getString(APP_CONFIG_KEY, "")
            try{
                if(configJson.isNullOrEmpty()){
                    val defaultConfig = createDefaultConfig()
                    defaultConfig.save(context)
                    return defaultConfig
                }
                return Json.decodeFromString(configJson)
            }catch (e: SerializationException){
                Log.w("CACHE CORRUPTED", "APP CACHE IS CORRUPTED, RELOADING A DEFAULT CONFIG")
                val defaultConfig = createDefaultConfig()
                defaultConfig.save(context)
                return defaultConfig
            }
        }

        private fun createDefaultConfig(): AppConfig{
            return AppConfig("",
                enableEventLocation = false,
                enableDailyEvent = false,
                enableEventAutoUpdate = false,
                lastEventUpdate = -1,
                favoriteEvents = ArrayList()
            )
        }

         */
    }
}