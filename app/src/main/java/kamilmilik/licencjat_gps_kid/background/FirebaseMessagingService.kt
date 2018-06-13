package kamilmilik.licencjat_gps_kid.background

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.support.v4.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.content.Intent
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.IntentFilter
import android.os.PowerManager
import android.support.annotation.RequiresApi
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import kamilmilik.licencjat_gps_kid.utils.Constants
import kamilmilik.licencjat_gps_kid.utils.Tools
import kamilmilik.licencjat_gps_kid.utils.WakeLocker


/**
 * Created by kamil on 03.03.2018.
 */
class FirebaseMessagingService : FirebaseMessagingService() {

    private val TAG = FirebaseMessagingService::class.java.simpleName

    private var receiver: BroadcastReceiver? = null

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "onCreate() internet " + Tools.isInternetConnection(this))
//        WakeLocker.acquire(this)
    }


    private var authListener: FirebaseAuth.AuthStateListener? = null
    @SuppressLint("MissingPermission")
    override fun onMessageReceived(remoteMessage: RemoteMessage?) {
        super.onMessageReceived(remoteMessage)
//        WakeLocker.release()
        Log.i(TAG, "onMessageReceived()")
        authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                setupAlarmManager()

                setupDetectDeviceInIdleMode()
            } else {
                Log.i(TAG, "onMessageReceived() user not loggin")
            }
        }
        FirebaseAuth.getInstance().addAuthStateListener(authListener!!)
    }

    private fun setupAlarmManager() {
        var alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        var alarmIntent = PendingIntent.getBroadcast(this, 0, Intent(this, AlarmReceiver::class.java), 0)

        if (Build.VERSION.SDK_INT >= 23) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), alarmIntent)
        } else if (Build.VERSION.SDK_INT >= 19) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), alarmIntent)
        }
    }

    private fun setupDetectDeviceInIdleMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            receiver = object : BroadcastReceiver() {
                @RequiresApi(api = Build.VERSION_CODES.M)
                override fun onReceive(context: Context, intent: Intent) {
                    val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
                    if (pm.isDeviceIdleMode) {
                        Log.i(TAG, "onReceive() doze mode")
                    } else {
                        Log.i(TAG, "onReceive() not doze")
                    }
                }
            }
            registerReceiver(receiver, IntentFilter(PowerManager.ACTION_DEVICE_IDLE_MODE_CHANGED))
        }
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        unregisterBroadcast()
        super.onTaskRemoved(rootIntent)
    }
    override fun onDestroy() {
        unregisterBroadcast()
        super.onDestroy()
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        Log.i(TAG, "onTrimMemory()")
        unregisterBroadcast()
        FirebaseAuth.getInstance().removeAuthStateListener(authListener!!)
    }

    private fun unregisterBroadcast(){
        if (receiver != null) {
            unregisterReceiver(receiver)
        }
    }
}