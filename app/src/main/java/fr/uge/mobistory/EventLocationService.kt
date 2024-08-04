package fr.uge.mobistory

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import fr.uge.mobistory.database.DatabaseManager
import fr.uge.mobistory.database.entity.Event
import fr.uge.mobistory.localstorage.AppConfig
import kotlin.math.cos

class EventLocationService : Service() {
    private var running = false
    var locationManager: LocationManager? = null
    var locationListener: LocationListener? = null
    var appDatabase : DatabaseManager? = null
    private var notificationId: Int = 1
    private val CHANNEL_ID = "MobyStoryLocationService"

    // Not used
    override fun onBind(arg0: Intent?): IBinder? {
        return null
    }

    /**
     * Create the event location notification channel
     */
    private fun createNotificationChannel() {
        val name: CharSequence = "Event location channel"
        val descriptionText = "Channel for the event location"
        val importance: Int = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }

        // Register the channel
        val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    /**
     * Setup the service on the creation
     */
    override fun onCreate() {
        createNotificationChannel()
        val context = this
        appDatabase = DatabaseManager(context)

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager?
        locationListener = LocationListener { location ->
            val appConfig = AppConfig(context)

            // Check if functionality is activated
            if(!appConfig.enableEventLocation){
                return@LocationListener
            }

            // For a bounding box of 1 km² with location at center
            val minLat = location.latitude - ((1/110.574)/2) // ~ 1/2 km
            val maxLat = location.latitude + ((1/110.574)/2) // ~ 1/2 km
            val minLong = location.longitude - ((1/(111.320 * cos(minLat))) / 2) // ~ 1/2 km
            val maxLong = location.longitude + ((1/(111.320 * cos(maxLat))) / 2) // ~ 1/2 km
            /*
             Approx
             Latitude: 1 deg = 110.574 km.
             Longitude: 1 deg = 111.320*cos(latitude) km
            */
            val eventsAround = appDatabase?.getDb()?.eventLocationDao()?.findAllInRange(minLong, minLat, maxLong, maxLat)
            if(eventsAround != null){
                with(NotificationManagerCompat.from(context)) {
                    // Send notification if device own the required permission
                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                        eventsAround.forEach(){ev ->
                            val event = appDatabase?.getDb()?.eventDao()?.getById(ev.eventId)
                            if(event != null) {
                                Log.i("LOCATION EVENT", event.toString())
                                notify(notificationId, createEventLocationNotification(event))
                                notificationId++
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Create a notification for an event location
     *
     * @param event Event displayed in the notification
     */
    private fun createEventLocationNotification(event: Event): Notification{
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            action = EVENT_LOCATION_ACTION
            putExtra("idE", event.id.toString())
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)


        val builder: NotificationCompat.Builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.mobystorylogo)
            .setContentTitle(getString(R.string.notif_location_title))
            .setContentText(getString(R.string.notif_location_short) )
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText(getString(R.string.notif_location_long,event.title)))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            // Set the intent that will fire when the user taps the notification
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
        //TODO add intent to start the activity and directly come in the event
        return builder.build()
    }

    /**
     * Call on service start
     */
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        try {
            var provider = intent.getStringExtra("provider")
            if (provider == null) provider = LocationManager.GPS_PROVIDER
            locationListener?.let {
                locationManager?.requestLocationUpdates(
                    provider,
                    30_000,
                    100f,
                    it
                )
            }
            running = true
        } catch (e: SecurityException) {
            stopSelf()
        }
        return START_REDELIVER_INTENT // Restart the service with the intent if it is destroyed
    }

    /**
     * Call when the service is destroyed
     */
    override fun onDestroy() {
        try {
            locationListener?.let { locationManager?.removeUpdates(it) }
        } catch (e: SecurityException) {
            // this case should not be encountered
        }
        running = false
    }

    companion object {
        val EVENT_LOCATION_ACTION: String = EventLocationService::class.java.name + ".checkEventLocation"
    }

}