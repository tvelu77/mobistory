package fr.uge.mobistory.ui

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import fr.uge.mobistory.localstorage.AppData
import fr.uge.mobistory.ui.screens.EventList
import fr.uge.mobistory.ui.screens.FavoriteList
import fr.uge.mobistory.ui.screens.OneEvent
import fr.uge.mobistory.ui.screens.Quiz
import fr.uge.mobistory.ui.screens.Settings

/**
 * Serves the purpose to create routes to composable and display it.
 *
 * @param navController NavController, the navigation controller for page history and
 *                      navigation in general.
 * @param appData AppData, a class containing all of the data of this application
 *                (database, configuration).
 */
@Composable
fun NavigationHost(navController: NavController, appData: AppData) {
    NavHost(
        navController = navController as NavHostController,
        startDestination = "event"
    ) {
        composable("event") {
            EventList(navController = navController, appData = appData)
        }
        composable("details/{eventId}",
            listOf(navArgument("eventId") { NavType.StringType })) {
            it.arguments?.getString("eventId")?.let { eventId ->
                OneEvent(eventId.toInt(), appData, navController)
            }
        }
        composable("favorite") {
            FavoriteList(appData, navController)
        }
        composable("quiz") {
            Quiz(appData)
        }
        composable("settings") {
            Settings(appData = appData)
        }
    }
}