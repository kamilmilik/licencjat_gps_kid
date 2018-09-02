package kamilmilik.gps_tracker.background

import android.annotation.SuppressLint
import android.app.AlarmManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import android.content.Context
import android.os.Build
import android.content.Intent
import android.app.PendingIntent
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import kamilmilik.gps_tracker.utils.LogUtils


/**
 * Created by kamil on 03.03.2018.
 */
class FirebaseMessagingService : FirebaseMessagingService() {

    private val TAG = FirebaseMessagingService::class.java.simpleName

    private var authListener: FirebaseAuth.AuthStateListener? = null

    override fun onMessageReceived(remoteMessage: RemoteMessage?) {
        super.onMessageReceived(remoteMessage)
        LogUtils(this).appendLog(TAG, "onMessageReceived()")
        authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                setupAlarmManager()
            } else {
                Log.i(TAG, "onMessageReceived() user not log in")
            }
        }

        authListener?.let {
            FirebaseAuth.getInstance().addAuthStateListener(it)
        }
    }

    private fun setupAlarmManager() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmIntent = PendingIntent.getBroadcast(this, 0, Intent(this, AlarmReceiver::class.java), 0)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), alarmIntent)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), alarmIntent)
        }
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        authListener?.let {
            FirebaseAuth.getInstance().removeAuthStateListener(it)
        }
    }

}