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
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.util.rangeTo
import fr.uge.mobistory.database.DatabaseManager
import fr.uge.mobistory.database.entity.Event
import fr.uge.mobistory.localstorage.AppConfig
import fr.uge.mobistory.utils.toLocalDate
import java.sql.Timestamp
import java.time.LocalDate
import java.util.Calendar
import java.util.Date

class DailyEventService : Service() {
    var appDatabase : DatabaseManager? = null
    private var notificationId: Int = 100000
    private val CHANNEL_ID = "MobyStoryDailyEventService"
    private val LAST_EVENT_NOTIFY_KEY = "NOTIFICATION_DAILY_EVENT"
    private var notifierThread : Thread? = null
    private val checkDelay = 30_000L

    override fun onBind(arg0: Intent?): IBinder? {
        return null
    }

    /**
     * Create the notification channel
     */
    private fun createNotificationChannel() {
        val name: CharSequence = "Daily event notifier channel"
        val descriptionText = "Channel for the daily event notification of MobyStory"
        val importance: Int = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    override fun onCreate() {
        createNotificationChannel()
        val context = this
        appDatabase = DatabaseManager(context)
    }

    /**
     * Create the daily event notification
     */
    private fun createDailyEventNotification(event: Event): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            action = DAILY_EVENT_NOTIFIER_ACTION
            putExtra("idE", event.id.toString())
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val builder: NotificationCompat.Builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.mobystorylogo)
            .setContentTitle(getString(R.string.notif_daily_event_title))
            .setContentText(getString(R.string.notif_daily_event_short))
            .setStyle(NotificationCompat.BigTextStyle()
            .bigText(getString(R.string.notif_daily_event_long, event.beginDate, event.title)))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
        return builder.build()
    }

    /**
     * Call on service start with the intent
     */
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        notifierThread = Thread(){
            val context = this
            try{
                while (!Thread.interrupted()) {
                    val appConfig = AppConfig(context)

                    // Check if functionality is activated
                    if(!appConfig.enableDailyEvent){
                        Thread.sleep(checkDelay)
                        continue
                    }


                    // Retrieve from shared preferences the last time notification
                    val preferences = context.getSharedPreferences(LAST_EVENT_NOTIFY_KEY, MODE_PRIVATE)
                    val lastEventTime = preferences.getLong(LAST_EVENT_NOTIFY_KEY, -1L)
                    val currentDate = LocalDate.now()

                    // If the cache is empty use the same date as now
                    if(lastEventTime != -1L){
                        val lastEventDate = LocalDate.ofEpochDay(lastEventTime)

                        //If it is not the first daily event of the application
                        if(currentDate.isEqual(lastEventDate)) {
                            Thread.sleep(checkDelay)
                            continue
                        }
                    }

                    val editor = preferences.edit()
                    editor.putLong(LAST_EVENT_NOTIFY_KEY, currentDate.toEpochDay())
                    editor.apply()

                    val notifiedEvent = appDatabase?.let { Utils.getDailyEvent(it, currentDate) }

                    // If a corresponding event was found then send the
                    if(notifiedEvent != null){
                        with(NotificationManagerCompat.from(context)) {
                            // notificationId is a unique int for each notification that you must define
                            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                                notify(notificationId, createDailyEventNotification(notifiedEvent))
                                notificationId++
                                Log.i("DAILY EVENT", notifiedEvent.toString())
                            }
                        }
                    }
                    Thread.sleep(checkDelay)
                }
            }catch (e: InterruptedException){
                Log.w("DAILY EVENT", "Daily event checker was interrupted: $e")
            }
        }
        notifierThread?.start()
        return START_REDELIVER_INTENT // Restart the service with the intent if it is destroyed
    }

    override fun onDestroy() {
        notifierThread?.interrupt()
    }

    companion object {
        val DAILY_EVENT_NOTIFIER_ACTION: String = DailyEventService::class.java.name + ".checkDailyEvent"
    }

}