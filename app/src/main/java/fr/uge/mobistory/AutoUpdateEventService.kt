package fr.uge.mobistory

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import fr.uge.mobistory.database.DatabaseManager
import fr.uge.mobistory.localstorage.AppConfig
import java.time.LocalDate
import java.util.Date

class AutoUpdateEventService : Service() {
    private var appDatabase: DatabaseManager? = null
    private var updaterThread: Thread? = null
    private val checkDelay = 60_000L

    override fun onBind(arg0: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        val context = this
        appDatabase = DatabaseManager(context)
    }

    /**
     * Call on service start with the intent
     */
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        updaterThread = Thread() {
            val context = this
            try {
                while (!Thread.interrupted()) {
                    val appConfig = AppConfig(context)

                    // Check if functionality is activated
                    if(!appConfig.enableEventAutoUpdate){
                        Thread.sleep(checkDelay)
                        continue
                    }

                    // Retrieve from shared preferences the last time notification
                    val lastUpdateTime = appConfig.lastEventUpdate
                    val currentDate = LocalDate.now()

                    // If the cache is empty use the same date as now
                    if (lastUpdateTime != -1L) {
                        val lastUpdateDate = LocalDate.ofEpochDay(lastUpdateTime)

                        //If the update is not the same days then wait
                        if (currentDate.equals(lastUpdateDate)) {
                            Thread.sleep(checkDelay)
                            continue
                        }
                    }

                    Log.i("EVENT AUTO UPDATE", "Auto event updater service currently updating events...")
                    appDatabase?.updateDatabase(appConfig.language, null)

                    // Set new event update date
                    appConfig.lastEventUpdate = currentDate.toEpochDay()

                    Thread.sleep(checkDelay)
                }
            } catch (e: InterruptedException) {
                Log.w("EVENT UPDATER", "Events auto update was interrupted: $e")
            }
        }
        updaterThread?.start()
        return START_REDELIVER_INTENT // Restart the service with the intent if it is destroyed
    }

    override fun onDestroy() {
        updaterThread?.interrupt()
    }
}
