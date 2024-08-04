package fr.uge.mobistory.ui

import android.annotation.SuppressLint
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import fr.uge.mobistory.Utils
import fr.uge.mobistory.localstorage.AppData
import fr.uge.mobistory.ui.components.Drawer
import fr.uge.mobistory.ui.components.TopBar
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * Creates the navigation, components.
 *
 * @param appData AppData.
 */
@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun AppScaffold(appData: AppData, navController: NavController, openEventAction: MutableState<Int?>) {
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()
    val currentName by rememberSaveable { mutableStateOf("Mobistory") }
    var currentIcon by remember { mutableStateOf(Icons.Filled.Menu) }
    //val event = remember {
    //    mutableStateOf(openEventAction.value?.let { appData.dbManager.getDb().eventDao().getById(it) })
    //}
    val dailyEvent by remember {
        mutableStateOf(Utils.getDailyEvent(appData.dbManager, LocalDate.now()))
    }
    val topBar : @Composable () -> Unit = {
        TopBar(
            title = currentName,
            buttonIcon = currentIcon,
            onButtonClicked = {
                scope.launch {
                    when (currentIcon) {
                        Icons.Filled.Menu -> {
                            scaffoldState.drawerState.open()
                        }
                        Icons.Filled.ArrowBack -> navController.popBackStack()
                    }
                }
            }
        )
    }
    Scaffold(
        topBar = {
            topBar()
        },
        scaffoldState = scaffoldState,
        drawerContent = {
            Drawer(dailyEvent,
                onEventClick = {
                    scope.launch {
                        scaffoldState.drawerState.close()
                    }
                    navController.navigate("details/$it") {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onDestinationClicked = { route ->
                    scope.launch {
                    scaffoldState.drawerState.close()
                }
                navController.navigate(route) {
                    popUpTo(navController.graph.startDestinationId) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            })
        },
        drawerGesturesEnabled = scaffoldState.drawerState.isOpen,
    ) {
        NavigationHost(navController, appData)
        LaunchedEffect(navController) {
            navController.addOnDestinationChangedListener { _, destination, _ ->
                currentIcon = when (destination.route) {
                    "event" -> Icons.Filled.Menu
                    else -> Icons.Filled.ArrowBack
                }
            }
            if (openEventAction.value != null) {
                navController.navigate("details/${openEventAction.value}")
            }
        }
    }
}