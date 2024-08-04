package fr.uge.mobistory

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState
import fr.uge.mobistory.database.DatabaseManager
import fr.uge.mobistory.localstorage.AppCache
import fr.uge.mobistory.localstorage.AppConfig
import fr.uge.mobistory.localstorage.AppData
import fr.uge.mobistory.ui.AppScaffold
import fr.uge.mobistory.ui.theme.MobistoryTheme

/**
 * This is where the magic operates !
 */
class MainActivity : ComponentActivity() {

    private lateinit var navController: NavController

    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            navController = rememberNavController()
            MobistoryTheme {

                // Start event location service
                val mIntent = Intent(LocalContext.current, EventLocationService::class.java)
                mIntent.action = EventLocationService.EVENT_LOCATION_ACTION
                startService(mIntent)
                //registerReceiver(receiver, IntentFilter(EventLocationService.EVENT_LOCATION_ACTION))

                // Start daily event service
                val mIntentDailyEvent = Intent(LocalContext.current, DailyEventService::class.java)
                mIntentDailyEvent.action = DailyEventService.DAILY_EVENT_NOTIFIER_ACTION
                startService(mIntentDailyEvent)

                // Start auto update events service
                val mIntentAutoUpdateEvent = Intent(LocalContext.current, AutoUpdateEventService::class.java)
                startService(mIntentAutoUpdateEvent)

                val appConfig = AppConfig(LocalContext.current)
                val appDatabaseManager = DatabaseManager(LocalContext.current)
                val appData = AppData(appDatabaseManager,appConfig, AppCache.loadCache(LocalContext.current))
                appDatabaseManager.LoadDatabase(appData)

                // Capture display event
                val displayEvent: MutableState<Int?> = remember { mutableStateOf(null) }
                if (intent.action == EventLocationService.EVENT_LOCATION_ACTION || intent.action == DailyEventService.DAILY_EVENT_NOTIFIER_ACTION) {
                    val eventId = intent.getStringExtra("idE")
                    if (eventId != null) {
                        Log.i("OPEN EVENT ID : ", eventId.toString())
                        displayEvent.value = eventId.toInt()
                    } else {
                        //displayEvent = null
                        Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show()
                    }
                }

                AppScaffold(appData, navController, displayEvent)
            }
        }
    }

}