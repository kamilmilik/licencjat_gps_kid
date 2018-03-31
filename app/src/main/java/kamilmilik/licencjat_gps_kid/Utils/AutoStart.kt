package kamilmilik.licencjat_gps_kid.Utils

import android.content.Intent
import android.content.BroadcastReceiver
import android.content.Context
import android.util.Log


/**
 * Created by kamil on 31.03.2018.
 */
class AutoStart : BroadcastReceiver() {
    internal var alarm = Alarm()
    private val TAG = AutoStart::class.java.simpleName
    override fun onReceive(context: Context, intent: Intent) {
        Log.i(TAG,"onReceive()")
        if (intent.action == "android.intent.action.BOOT_COMPLETED") {
            alarm.setAlarm(context)
        }
    }
}