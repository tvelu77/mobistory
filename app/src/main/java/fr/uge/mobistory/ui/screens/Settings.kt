package fr.uge.mobistory.ui.screens

import android.Manifest
import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Text
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat.getString
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState
import fr.uge.mobistory.R
import fr.uge.mobistory.localstorage.AppConfig
import fr.uge.mobistory.localstorage.AppData
import java.time.LocalDate


/**
 * Shows options for the user.
 *
 * @param modifier Modifier, visual modifier for the composable.
 * @param appData AppData.
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun Settings(modifier: Modifier = Modifier, appData: AppData) {
    val context = LocalContext.current
    val appConfig = AppConfig(context)
    var eventLocationEnabled by remember {mutableStateOf(appData.appConfig.enableEventLocation) }
    var eventDailyEnabled by remember {mutableStateOf(appData.appConfig.enableDailyEvent) }
    var eventAutoUpdateEnabled by remember {mutableStateOf(appData.appConfig.enableEventAutoUpdate) }
    val updatingEvents = remember {mutableStateOf(false) }
    val locationPermissionsState = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
        )
    )
    val notificationPermissionsState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS) else null

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Column(modifier= Modifier.fillMaxWidth().fillMaxHeight(1f / 10f)) {
            Box(modifier = Modifier.fillMaxWidth().fillMaxHeight(5f / 10f)){
                Button(modifier= Modifier.fillMaxSize(),
                    onClick = {
                        updatingEvents.value = true
                        appData.dbManager.updateDatabase(appData.appConfig.language, updatingEvents)
                        appData.appConfig.lastEventUpdate = LocalDate.now().toEpochDay()
                    }) {
                    Text(text = context.getString(R.string.button_update_events))
                }
            }
            Box(modifier = Modifier.fillMaxWidth().fillMaxHeight(5f / 10f), contentAlignment = Alignment.Center){
                if(updatingEvents.value){
                    Text(text = context.getString(R.string.updating_state_setting))

                }else{
                   if(appConfig.lastEventUpdate == -1L){
                       Text(text = context.getString(R.string.updated_date_state_setting, "--"))
                   } else{
                       Text(text = context.getString(R.string.updated_date_state_setting, LocalDate.ofEpochDay(appConfig.lastEventUpdate).toString()))
                   }

                }
            }
        }

        // Setting for location event notification
        Row(
            Modifier
                .fillMaxWidth()
                .fillMaxHeight(1f / 10f)) {
            Box(modifier = Modifier
                .fillMaxWidth(1f / 10f)
                .fillMaxHeight(), contentAlignment = Alignment.CenterStart){
                Checkbox(checked = eventLocationEnabled, onCheckedChange = {newValue ->
                    if(newValue){
                        // Request location access permission
                        if (!locationPermissionsState.allPermissionsGranted) {
                            val alertDialog: AlertDialog = AlertDialog.Builder(context).create()
                            alertDialog.setTitle(getString(context, R.string.permission_request_title))
                            alertDialog.setMessage(getString(context, R.string.permission_location_desc))
                            alertDialog.setIcon(android.R.drawable.ic_dialog_info)
                            alertDialog.setButton(DialogInterface.BUTTON_NEUTRAL,"OK") { _, _ ->
                                locationPermissionsState.launchMultiplePermissionRequest()
                            }
                            alertDialog.show()
                        }
                        if(notificationPermissionsState  != null && !notificationPermissionsState.status.isGranted){
                            val alertDialog: AlertDialog = AlertDialog.Builder(context).create()
                            alertDialog.setTitle(getString(context,R.string.permission_request_title))
                            alertDialog.setMessage(getString(context, R.string.permission_notification_desc))
                            alertDialog.setIcon(android.R.drawable.ic_dialog_info)
                            alertDialog.setButton(DialogInterface.BUTTON_NEUTRAL,"OK") { _, _ ->
                                notificationPermissionsState.launchPermissionRequest()
                            }
                            alertDialog.show()
                        }
                        eventLocationEnabled = true
                        appData.appConfig.enableEventLocation = true
                    }else{
                        eventLocationEnabled = false
                        appData.appConfig.enableEventLocation = false
                    }
                })
            }
            Box(modifier = Modifier
                .fillMaxWidth(9f / 10f)
                .fillMaxHeight(), contentAlignment = Alignment.CenterStart){
                Text(text = getString(context,R.string.checkboxLocationEvent))
            }
        }

        // Setting for daily event notification
        Row(
            Modifier
                .fillMaxWidth()
                .fillMaxHeight(1f / 10f)) {
            Box(modifier = Modifier
                .fillMaxWidth(1f / 10f)
                .fillMaxHeight(), contentAlignment = Alignment.CenterStart){
                Checkbox(checked = eventDailyEnabled, onCheckedChange = {newValue ->
                    eventDailyEnabled = newValue
                    if(notificationPermissionsState  != null && !notificationPermissionsState.status.isGranted){
                        val alertDialog: AlertDialog = AlertDialog.Builder(context).create()
                        alertDialog.setTitle(getString(context,R.string.permission_request_title))
                        alertDialog.setMessage(getString(context, R.string.permission_notification_desc))
                        alertDialog.setIcon(android.R.drawable.ic_dialog_info)
                        alertDialog.setButton(DialogInterface.BUTTON_NEUTRAL,"OK") { _, _ ->
                            notificationPermissionsState.launchPermissionRequest()
                        }
                        alertDialog.show()
                    }
                    appData.appConfig.enableDailyEvent = newValue
                })
            }
            Box(modifier = Modifier
                .fillMaxWidth(9f / 10f)
                .fillMaxHeight(), contentAlignment = Alignment.CenterStart){
                Text(text = getString(context,R.string.checkboxDailyEvent))
            }
        }

        // Setting for event database update automatically
        Row(
            Modifier
                .fillMaxWidth()
                .fillMaxHeight(1f / 10f)) {
            Box(modifier = Modifier
                .fillMaxWidth(1f / 10f)
                .fillMaxHeight(), contentAlignment = Alignment.CenterStart){
                Checkbox(checked = eventAutoUpdateEnabled, onCheckedChange = {newValue ->
                    eventAutoUpdateEnabled = newValue
                    appData.appConfig.enableEventAutoUpdate = newValue
                })
            }
            Box(modifier = Modifier
                .fillMaxWidth(9f / 10f)
                .fillMaxHeight(), contentAlignment = Alignment.CenterStart){
                Text(text = getString(context,R.string.checkboxUpdaterEvent))
            }
        }
    }
}