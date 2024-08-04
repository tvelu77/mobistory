package fr.uge.mobistory.ui.screens

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import fr.uge.mobistory.R
import fr.uge.mobistory.database.entity.Event
import fr.uge.mobistory.localstorage.AppData
import fr.uge.mobistory.utils.returnLineFormat
import fr.uge.mobistory.utils.textCutter
import fr.uge.mobistory.utils.toCorrectFormat
import fr.uge.mobistory.wiki.WikiAPI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.Exception
import java.time.LocalDate

/**
 * Shows the event page according to its Id.
 *
 * @param eventId Int, event's id.
 * @param appData AppData, the application data.
 * @param navController NavController, if there is related events so we can navigate.
 */
@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun OneEvent(eventId: Int, appData: AppData, navController: NavController) {
    val event = remember {
        mutableStateOf(appData.dbManager.getDb().eventDao().getById(eventId))
    }
    if (event.value == null) {
        Text("404")
    } else {
        val context = LocalContext.current
        val articles = remember { mutableStateOf(emptyMap<String, String>()) }
        val imagesPath = remember {
            val imagesList = appData.appCache
                .eventsImagesPath.getOrDefault(event.value!!.id, ArrayList())
            mutableStateOf(imagesList.toList())
        }
        val images = remember {
            mutableStateListOf<ImageBitmap>()
        }
        val relations = appData.dbManager.getDb().eventRelationDao().findAll(event.value!!.id)
        val relatedEvents = ArrayList<Event>()
        relations.forEach { relation ->
            val relatedEvent = appData.dbManager.getDb().eventDao().getById(relation.relatedEventId)
            if(relatedEvent != null){
                relatedEvents.add(relatedEvent)
            }
        }
        val isFavorite = remember { mutableStateOf(appData.appConfig
            .isAlreadyAdded(event.value!!.id)) }
        val isError = remember { mutableStateOf(false) }
        LaunchedEffect(event) {
            try {
                var fetchedDescription = withContext(Dispatchers.IO) {
                    WikiAPI.getEventDetails(event.value!!,
                        appData.appConfig.language,
                        appData.appCache)
                }
                fetchedDescription = returnLineFormat(fetchedDescription)
                fetchedDescription.let {
                    articles.value = textCutter(returnLineFormat(it))
                }
            } catch (e: Exception) {
                Log.e("DISPLAY EVENT", "Error while fetching description: $e")
                isError.value = true
            }
            try {
                val fetchedImagesPath = withContext(Dispatchers.IO) {
                    WikiAPI.updateEventImagesPath(event.value!!,
                        appData.appConfig.language,
                        appData.appCache)
                    appData.appCache.save(context)
                    appData.appCache.eventsImagesPath.getOrDefault(event.value!!.id,
                        ArrayList())
                }
                fetchedImagesPath.let {
                    imagesPath.value = it
                }
            } catch (e : Exception) {
                Log.e("DISPLAY EVENT", "Error while updating images: $e")
                isError.value = true
            }
            try {
                val fetchedImages = withContext(Dispatchers.IO) {
                    val imagesList = arrayListOf<ImageBitmap>()
                    imagesPath.value.forEach { imagePath ->
                        //val image = WikiAPI.downloadImage(LocalContext.current, imagePath.imageURL)
                        val image = WikiAPI.getOrDownloadImage(context, imagePath)
                        if (image != null) {
                            imagesList.add(image)
                        }
                    }
                    imagesList
                }
                fetchedImages.let {
                    images.clear()
                    it.forEach { image ->
                        images.add(image)
                    }
                }
            } catch (e : Exception) {
                Log.e("DISPLAY EVENT", "Error while displaying images: $e")
                isError.value = true
            }
        }
        Scaffold(
            floatingActionButton = {
                FloatingActionButton(onClick = {
                    isFavorite.value = if (appData.appConfig.isAlreadyAdded(event.value!!.id)) {
                        appData.appConfig.remove(event.value!!.id)
                        false
                    } else {
                        appData.appConfig.addToFavorites(event.value!!.id)
                        true
                    }
                }) {
                    if (isFavorite.value) {
                        Icon(Icons.Filled.Delete, "delete")
                    } else {
                        Icon(Icons.Filled.Favorite, "favorite")
                    }
                }
            },
            content = {
                val lazyListState = rememberLazyListState()
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    LazyColumn(
                        state = lazyListState,
                        contentPadding = PaddingValues(end = 8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        item {
                            OneEventTitle(title = event.value!!.title)
                        }
                        item {
                            OneEventDate(beginDate = event.value!!.beginDate,
                                endDate = event.value!!.endDate)
                        }
                        item {
                            OneEventArticle(articles = articles.value)
                        }
                        item {
                            OneEventImages(eventImages = images)
                        }
                        item {
                            OneEventRelatedEvents(relatedEvents = relatedEvents,
                                navController = navController)
                        }
                    }
                }
            }
        )
    }
}

@Composable
private fun OneEventTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.h4,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .padding(bottom = 8.dp)
            .padding(horizontal = 16.dp), // Add horizontal padding
        color = MaterialTheme.colors.primary
    )
}

@Composable
private fun OneEventDate(beginDate: LocalDate, endDate: LocalDate) {
    val formattedDate = if (beginDate.isEqual(endDate)) {
        toCorrectFormat(beginDate.toString())
    } else {
        LocalContext.current.getString(
            R.string.date_from_to,
            toCorrectFormat(beginDate.toString()),
            toCorrectFormat(endDate.toString())
        )
    }

    Text(
        text = formattedDate,
        style = MaterialTheme.typography.subtitle1,
        color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
        modifier = Modifier
            .padding(bottom = 8.dp)
            .padding(horizontal = 16.dp), // Add horizontal padding
    )
}

@Composable
private fun OneEventArticle(articles: Map<String, String>) {
    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp) // Add horizontal padding
    ) {
        articles.forEach { (title, content) ->
            val importanceOfSection = title.count { it == '=' }
            if (importanceOfSection <= 4) {
                Text(
                    text = title.replace("=", ""),
                    style = MaterialTheme.typography.subtitle1,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp),
                    color = MaterialTheme.colors.secondary
                )
            } else {
                Text(
                    text = title.replace("=", ""),
                    style = MaterialTheme.typography.subtitle2,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp),
                    color = MaterialTheme.colors.secondary
                )
            }
            Text(
                text = content,
                style = MaterialTheme.typography.body1,
                modifier = Modifier.padding(bottom = 8.dp),
                color = MaterialTheme.colors.onSurface
            )
        }
    }
}

@Composable
private fun OneEventImages(eventImages: List<ImageBitmap>) {
    ImageSlider(eventImages)
}

@Composable
private fun OneEventRelatedEvents(relatedEvents: List<Event>, navController: NavController) {
    if (relatedEvents.isNotEmpty()) {
        Text(
            text = LocalContext.current.getString(R.string.related_events),
            style = MaterialTheme.typography.h4,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(bottom = 8.dp)
                .padding(horizontal = 16.dp), // Add horizontal padding
            color = MaterialTheme.colors.primary
        )
    }
    if (relatedEvents.isNotEmpty()) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp) // Add horizontal padding
        ) {
            relatedEvents.forEach { relatedEvent ->
                EventListItem(event = relatedEvent, onClick = { eventId ->
                    navController.navigate("details/$eventId") {
                        restoreState = true
                    }
                })
            }
        }
    }
}

/**
 * Sets the size of each image.
 *
 * @param images List of ImageBitmap.
 */
@Composable
fun ImageSlider(images: List<ImageBitmap>) {
    Column(
        Modifier
            .padding(horizontal = 16.dp) // Add horizontal padding
            .padding(vertical = 16.dp) // Add vertical padding
    ) {
        LazyRow(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)) {
            items(images) { image ->
                Image(
                    painter = BitmapPainter(image),
                    contentDescription = null,
                    modifier = Modifier
                        .size(200.dp)
                        .clip(shape = MaterialTheme.shapes.medium)
                        .padding(8.dp)
                )
            }
        }
    }
}