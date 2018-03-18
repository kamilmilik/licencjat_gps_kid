package kamilmilik.licencjat_gps_kid.Utils

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.SystemClock
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.util.Log
import kamilmilik.licencjat_gps_kid.Helper.Notification


/**
 * Created by kamil on 18.03.2018.
 */
class PolygonInsideOrOutsideService : Service {

    private val TAG = PolygonInsideOrOutsideService::class.java.simpleName

    private var notification : Notification? = null
    constructor() : super(){}

    override fun onCreate() {
        Log.i(TAG,"onCreate() - > PolygonInsideOrOutsideService")
        notification = Notification(this@PolygonInsideOrOutsideService)
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.i(TAG, "PolygonInsideOrOutsideService started")
        val thread = object : Thread() {
            override fun run() {
                notification!!.notificationAction()
            }
        }
        thread.start()
        return Service.START_STICKY
    }

    //prevent kill background service in kitkat android
    override fun onTaskRemoved(rootIntent: Intent) {
        val restartService = Intent(applicationContext,
                this.javaClass)
        restartService.`package` = packageName
        val restartServicePI = PendingIntent.getService(
                applicationContext, 1, restartService,
                PendingIntent.FLAG_ONE_SHOT)
        //Restart the service once it has been killed android
        val alarmService = applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmService.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 100, restartServicePI)
    }


    override fun onDestroy() {
        Log.i(TAG,"Problem: service destroy it couldn't happen")
        super.onDestroy()
    }

    override fun onBind(arg0: Intent): IBinder? {
        return null
    }


}
