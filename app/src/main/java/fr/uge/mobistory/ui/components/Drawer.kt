package fr.uge.mobistory.ui.components

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import fr.uge.mobistory.R
import fr.uge.mobistory.database.entity.Event
import fr.uge.mobistory.utils.toCorrectFormat

/**
 * Creates a drawer composable for the pages.
 * This drawer will contain privately defined routes.
 *
 * @param modifier Modifier, visual modifier for the composable.
 * @param onDestinationClicked (String) -> Unit, the action we should do upon clicking a string.
 */
@Composable
fun Drawer(
    event: Event?,
    onEventClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    onDestinationClicked: (route: String) -> Unit
) {
    val link = allDrawerRoutes()
    Column(
        modifier
            .fillMaxSize()
            .padding(start = 24.dp, top = 48.dp)
    ) {
        Image(
            painter = painterResource(R.drawable.mobystorylogo),
            contentDescription = "App icon"
        )
        link.forEach { (name, route) ->
            Spacer(Modifier.height(24.dp))
            Text(
                text = name,
                style = MaterialTheme.typography.h4,
                modifier = Modifier.clickable {
                    onDestinationClicked(route)
                }
            )
        }
        if (event != null) {
            EventInDrawer(event = event, onClick = onEventClick)
        }
    }
}

/**
 * For the drawer content, we simply define every page/route in a HashMap.
 *
 * @return Map of String and String, the name of the page and route.
 */
@Composable
private fun allDrawerRoutes(): Map<String, String> {
    val context = LocalContext.current
    val hashMap = LinkedHashMap<String, String>()
    hashMap[context.getString(R.string.event_list)] = "event"
    hashMap[context.getString(R.string.favorites)] = "favorite"
    hashMap[context.getString(R.string.quiz)] = "quiz"
    hashMap[context.getString(R.string.settings)] = "settings"
    return hashMap
}

/**
 * Represents an Event in the drawer.
 *
 * @param event Event.
 * @param onClick (Int) -> Unit, we grab the event Id to navigate to the detailed event page.
 */
@Composable
private fun EventInDrawer(event: Event, onClick: (Int) -> Unit) {
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