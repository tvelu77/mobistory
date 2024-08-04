package fr.uge.mobistory.wiki

import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.StrictMode
import android.util.Log
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import fr.uge.mobistory.database.entity.Event
import fr.uge.mobistory.localstorage.AppCache
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import kotlin.io.path.Path


class WikiAPI {

    companion object{
        private const val LOCAL_EVENT_IMAGES_PATH = "EventsImages"

        /***
         * Retrieve the wikipedia event main description
         */
        fun getEventDetails(event: Event, language: String, appCache: AppCache): String{
            // TODO like db use another thread to do this
            val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
            StrictMode.setThreadPolicy(policy)

            // Check if event description already exist in the cache
            val eventDescription = appCache.eventsDescription[event.id]
            if(!eventDescription.isNullOrEmpty()){
                Log.e("CACHE DESCR", "RETURN EVENT DESCRIPTION FROM CACHE")
                return eventDescription
            }

            // Request the event description to the API
            try {
                // Construct Media Wiki URL
                val apiUrl = "https://"+ language +".wikipedia.org/w/api.php?action=query&format=json&prop=extracts&explaintext=true&titles=" +
                        event.title.replace(" ", "%20")
                val url = URL(apiUrl)

                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"

                val reader = BufferedReader(InputStreamReader(conn.inputStream))
                val jsonElement = Json.parseToJsonElement(reader.readLines().joinToString())

                // Parse the JSON of the event details
                jsonElement.jsonObject.entries.forEach(){entry ->
                    if(entry.key == "query"){
                        entry.value.jsonObject.entries.forEach(){queryEntry ->
                            if(queryEntry.key == "pages"){
                                queryEntry.value.jsonObject.entries.first().value.jsonObject.entries.forEach(){ pageEntry ->
                                    if(pageEntry.key == "extract"){
                                        val result = pageEntry.value.toString().removeSurrounding("\"")
                                        appCache.eventsDescription[event.id] = result
                                        return result
                                    }
                                }
                            }
                        }
                    }
                }
                return ""
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return ""
        }

        /**
         * Update event images path in the local cache
         */
        fun updateEventImagesPath(event: Event, language: String, appCache: AppCache) {
            // TODO like db use another thread to do this
            val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
            StrictMode.setThreadPolicy(policy)
            try {
                val apiUrl = "https://"+ language +".wikipedia.org/w/api.php?action=query&titles=" + event.title.replace(" ", "%20") + "&format=json&prop=images"
                val url = URL(apiUrl)
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"

                val reader = BufferedReader(InputStreamReader(conn.inputStream))
                val imagesNames = ArrayList<String>()
                val jsonElement = Json.parseToJsonElement(reader.readLines().joinToString())

                // Retrieve images path from response
                jsonElement.jsonObject.entries.forEach(){ entry ->
                    if(entry.key == "continue"){
                        entry.value.jsonObject.entries.forEach(){continueEntry ->
                            if(continueEntry.key == "imcontinue"){
                                val split = continueEntry.value.toString().split("|")
                                if(split.size == 2){
                                    imagesNames.add(split[1].removeSuffix("\""))
                                }
                            }
                        }
                    }

                    //Refactoring with multiple functions
                    if(entry.key == "query"){
                        entry.value.jsonObject.entries.forEach(){pageListEntry ->
                            try{
                                pageListEntry.value.jsonObject.entries.forEach(){ pageEntry ->
                                    pageEntry.value.jsonObject.entries.forEach(){ pageFieldEntry ->
                                        if(pageFieldEntry.key == "images"){
                                            pageFieldEntry.value.jsonArray.forEach(){ imageEntry ->
                                                imageEntry.jsonObject.entries.forEach(){ imageFieldEntry ->
                                                    if(imageFieldEntry.key == "title"){
                                                        val imageNameEntry = imageFieldEntry.value.toString().removeSurrounding("\"","\"")
                                                        imagesNames.add(imageNameEntry.substringAfter(":")) //TODO refactore field
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                            }catch(e: SerializationException){
                                pageListEntry.value.jsonArray.forEach(){pageEntry ->
                                    pageEntry.jsonObject.entries.forEach(){ pageFieldEntry ->
                                        if(pageFieldEntry.key == "images"){
                                            pageFieldEntry.value.jsonArray.forEach(){ imageEntry ->
                                                imageEntry.jsonObject.entries.forEach(){ imageFieldEntry ->
                                                    if(imageFieldEntry.key == "title"){
                                                        val imageNameEntry = imageFieldEntry.value.toString().removeSurrounding("\"","\"")
                                                        if(imageNameEntry.startsWith("File:")){
                                                            imagesNames.add(imageNameEntry.substringAfter(":")) //TODO refactore field
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }catch (i: IllegalArgumentException){
                                pageListEntry.value.jsonArray.forEach(){pageEntry ->
                                    pageEntry.jsonObject.entries.forEach(){ pageFieldEntry ->
                                        if(pageFieldEntry.key == "images"){
                                            pageFieldEntry.value.jsonArray.forEach(){ imageEntry ->
                                                imageEntry.jsonObject.entries.forEach(){ imageFieldEntry ->
                                                    if(imageFieldEntry.key == "title"){
                                                        val imageNameEntry = imageFieldEntry.value.toString().removeSurrounding("\"","\"")
                                                        if(imageNameEntry.startsWith("File:")){
                                                            imagesNames.add(imageNameEntry.substringAfter(":")) //TODO refactore field
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                reader.close()

                // Retrieve image URL from image name and put it into the cache
                imagesNames.forEach(){imageName ->
                    val imageURL = getImageURL(imageName, language)
                    if(imageURL != null){
                        val eventImages = appCache.eventsImagesPath.putIfAbsent(event.id, ArrayList())
                        // Load image into the cache
                        val imagePath = ImagePath(imageName, imageURL)
                        if(eventImages != null && !eventImages.contains(imagePath)){
                            eventImages.add(imagePath)
                        }
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }


        /**
         * Retrieve the image URL from image name
         */
        private fun getImageURL(imageName: String, language: String): String? {
            val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
            StrictMode.setThreadPolicy(policy)
            val imageNameFormatted = imageName.replace(" ", "%20")
            val imageInfoAPIUrl = URL("https://$language.wikipedia.org/w/api.php?action=query&titles=File:$imageNameFormatted&prop=imageinfo&iilimit=1&iiprop=url&format=json")
            val conn = imageInfoAPIUrl.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"

            // Read wiki api response
            val reader = BufferedReader(InputStreamReader(conn.inputStream))
            val res = reader.readLines().joinToString()
            val jsonElement = Json.parseToJsonElement(res)

            // Retrieve image URL from response
            jsonElement.jsonObject.entries.forEach(){ entry ->
                if(entry.key == "query"){
                    entry.value.jsonObject.entries.forEach(){queryEntry ->
                        if(queryEntry.key == "pages"){
                            queryEntry.value.jsonObject.entries.first().value.jsonObject.entries.forEach(){ pageEntry ->
                                if(pageEntry.key == "imageinfo"){
                                    pageEntry.value.jsonArray.first().jsonObject.entries.forEach(){imageInfoEntry ->
                                        if(imageInfoEntry.key == "url"){
                                            return imageInfoEntry.value.toString().removeSurrounding("\"")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return null
        }


        /**
         * Retrieve the image from the local storage if it was already downloaded or download the image and save it in local storage
         */
        fun getOrDownloadImage(context: Context, imagePath: ImagePath): ImageBitmap? {
            // TODO like everywhere is needed use another thread to do the work
            val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
            StrictMode.setThreadPolicy(policy)
            val imageUrl = imagePath.imageURL

            //Skip unsupported format //TODO to be optimized
            if(!imageUrl.endsWith(".jpg") && !imageUrl.endsWith(".JPG") && !imageUrl.endsWith(".png")&& !imageUrl.endsWith(".PNG")){
                return null
            }

            val imageInfoAPIUrl = URL(imageUrl)
            val imageData = imageInfoAPIUrl.readBytes()

            val cw = ContextWrapper(context)
            val directory = cw.getDir(LOCAL_EVENT_IMAGES_PATH, Context.MODE_PRIVATE)
            // Load image from storage if found
            directory?.listFiles()?.forEach { file ->
                if(file.name == imagePath.imageName){
                    return loadImageFromStorage(imagePath.imageName, context)
                }
            }

            // Save file locally
            try {
                val outputStream = FileOutputStream(File(directory, imagePath.imageName))
                outputStream.write(imageData)
                outputStream.close()
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }

            // Decode image
            val b = BitmapFactory.decodeByteArray(imageData, 0, imageData.size)
            return if(b.width > 1920 && b.height > 1080){
                Bitmap.createScaledBitmap(b,1920,1080, true).asImageBitmap()
            }else if(b.width > 1920){
                Bitmap.createScaledBitmap(b,1920,b.height, true).asImageBitmap()
            }else if(b.height > 1080){
                Bitmap.createScaledBitmap(b,b.width,1080, true).asImageBitmap()
            }else{
                b.asImageBitmap()
            }
        }


        /**
         * Load image from local storage cache
         */
        private fun loadImageFromStorage(imageName: String, context: Context): ImageBitmap? {
            val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
            StrictMode.setThreadPolicy(policy)
            val cw = ContextWrapper(context)
            val directory = cw.getDir(LOCAL_EVENT_IMAGES_PATH, Context.MODE_PRIVATE)
            try {
                val f = File(directory, imageName)
                val b = BitmapFactory.decodeStream(FileInputStream(f))
                return if(b.width > 1920 && b.height > 1080){
                    Bitmap.createScaledBitmap(b,1920,1080, true).asImageBitmap()
                }else if(b.width > 1920){
                    Bitmap.createScaledBitmap(b,1920,b.height, true).asImageBitmap()
                }else if(b.height > 1080){
                    Bitmap.createScaledBitmap(b,b.width,1080, true).asImageBitmap()
                }else{
                    b.asImageBitmap()
                }
                //return Bitmap.createScaledBitmap(b,1920,1080, true).asImageBitmap()
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }
            return null
        }


    }

}