package fr.uge.mobistory.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import fr.uge.mobistory.database.entity.Event
import fr.uge.mobistory.localstorage.AppData
import fr.uge.mobistory.ui.components.DropdownFilter
import fr.uge.mobistory.ui.components.SearchView
import fr.uge.mobistory.utils.toCorrectFormat
import java.util.Locale

/**
 * List every events.
 *
 * @param appData AppData.
 * @param navController NavController, to navigate into the detailed event.
 */
@Composable
fun EventList(appData: AppData, navController: NavController) {
    val listState = rememberLazyListState()
    val textState = remember { mutableStateOf(TextFieldValue("")) }
    val filterId = remember { mutableIntStateOf(0) }
    val events = remember { mutableStateOf(appData.dbManager.getDb().eventDao().getAll()) }
    val filteredEvents = remember (events, filterId.intValue, textState.value.text) {
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
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxWidth()
        ) {
            items(filteredEvents) { event ->
                EventListItem(event = event, onClick = { eventId ->
                    navController.navigate("details/$eventId") {
                        launchSingleTop = true
                        restoreState = true
                    }
                })
            }
        }
    }
}

/**
 * Represents an Event in the list.
 *
 * @param event Event.
 * @param onClick (Int) -> Unit, we grab the event Id to navigate to the detailed event page.
 */
@Composable
fun EventListItem(event: Event, onClick: (Int) -> Unit) {
    Card(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
            .clickable { onClick(event.id) },
        elevation = 8.dp,
        shape = RoundedCornerShape(8.dp),
        backgroundColor = MaterialTheme.colors.surface,
        contentColor = MaterialTheme.colors.onSurface
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = event.title,
                style = MaterialTheme.typography.h6,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = toCorrectFormat(event.beginDate.toString()),
                style = MaterialTheme.typography.body2,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = event.description,
                style = MaterialTheme.typography.body1,
                modifier = Modifier.padding(bottom = 8.dp)
            )
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
        2 -> events.sortedByDescending { it.popularity }
        3 -> events.sortedBy { it.popularity }
        else -> {
            events
        }
    }
}