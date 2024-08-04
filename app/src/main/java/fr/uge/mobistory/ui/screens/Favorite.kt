package fr.uge.mobistory.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.navigation.NavController
import fr.uge.mobistory.database.entity.Event
import fr.uge.mobistory.localstorage.AppData
import fr.uge.mobistory.ui.components.DropdownFilter
import fr.uge.mobistory.ui.components.SearchView
import java.util.Locale

/**
 * Shows the user's favorite events.
 *
 * @param appData AppData.
 * @param navController NavController, to navigate into detailed event.
 */
@Composable
fun FavoriteList(appData: AppData, navController: NavController) {
    val favoriteEvents = ArrayList<Event>()
    appData.appConfig.favoriteEvents.forEach { eventId ->
        val ev = appData.dbManager.getDb().eventDao().getById(eventId)
        if(ev != null){
            favoriteEvents.add(ev)
        }
    }
    val listState = rememberLazyListState()
    val textState = remember { mutableStateOf(TextFieldValue("")) }
    val filterId = remember { mutableIntStateOf(0) }
    val events = remember { mutableStateOf(favoriteEvents.toList()) }
    val filteredEvents = rememberSaveable(events, filterId.intValue, textState.value.text) {
        val eventsFilteredByTitle = events.value.filter { event ->
            val isTitleMatch = event.title.lowercase(Locale.getDefault())
                .contains(textState.value.text.lowercase(Locale.getDefault()))
            isTitleMatch
        }
        applyFilter(eventsFilteredByTitle, filterId.intValue)
    }
    Column {
        SearchView(textState)
        DropdownFilter(filterId)
        LazyColumn(state = listState, modifier = Modifier.fillMaxWidth()) {
            items(filteredEvents){event ->
                EventListItem(event, onClick = { eventId ->
                    navController.navigate("details/$eventId") {
                        restoreState = true
                    }
                })
            }
        }
    }
}

/**
 * Applies a filter according to the given int.
 * The filters are statically defined.
 * It is kind of weird but it works !
 *
 * @param events List of Event.
 * @param filter Int, represents the type of filter.
 * @return A filtered list of Event.
 */
private fun applyFilter(events: List<Event>, filter: Int): List<Event> {
    return when(filter) {
        0 -> events.sortedBy { it.beginDate }//toLocalDate(it.beginDate) }
        1 -> events.sortedByDescending { it.beginDate } //toLocalDate() }
        2 -> events.sortedBy { it.popularity }
        3 -> events.sortedByDescending { it.popularity }
        else -> {
            events
        }
    }
}