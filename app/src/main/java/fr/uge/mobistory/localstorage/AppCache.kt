package fr.uge.mobistory.localstorage

import android.content.Context
import android.content.ContextWrapper
import android.graphics.BitmapFactory
import android.os.StrictMode
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import fr.uge.mobistory.wiki.ImagePath
import fr.uge.mobistory.wiki.WikiAPI
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.nio.file.Path

@Serializable
class AppCache(val eventsImagesPath: MutableMap<Int,MutableList<ImagePath>>, val eventsDescription: MutableMap<Int,String>) {

    fun save(context: Context){
        val appCacheJson = Json.encodeToString(this)
        saveCacheFile(context, appCacheJson)
    }

    private fun saveCacheFile(context: Context, content: String){
        val cw = ContextWrapper(context)
        val directory = cw.getDir(APP_CACHE_DIRECTORY, Context.MODE_PRIVATE)
        directory.listFiles()?.forEach { file ->
            if(file.name == APP_CACHE_FILE){
                File(directory, APP_CACHE_FILE).writeText(content)
            }
        }
    }

    companion object {
        private const val APP_CACHE_DIRECTORY = "cacheEventData"
        private const val APP_CACHE_FILE = "eventData"

        fun loadCache(context: Context): AppCache {
            val configJson = loadCacheFile(context)
            try{
                if(configJson.isNullOrEmpty()){
                    val defaultConfig = createDefaultConfig()
                    defaultConfig.save(context)
                    return defaultConfig
                }
                return Json.decodeFromString(configJson)
            }catch (e: SerializationException){
                Log.w("CACHE ERROR", "Reloading cache")
                val defaultConfig = createDefaultConfig()
                defaultConfig.save(context)
                return defaultConfig
            }
        }

        private fun loadCacheFile(context: Context): String?{
            val cw = ContextWrapper(context)
            val directory = cw.getDir(APP_CACHE_DIRECTORY, Context.MODE_PRIVATE)
            directory.listFiles()?.forEach { file ->
                if(file.name == APP_CACHE_FILE){
                    return File(directory, APP_CACHE_FILE).readText()
                }
            }
            return null
        }

        private fun createDefaultConfig(): AppCache{
            return AppCache(HashMap(), HashMap())
        }
    }
}