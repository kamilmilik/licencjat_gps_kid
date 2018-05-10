package kamilmilik.licencjat_gps_kid

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import kamilmilik.licencjat_gps_kid.Utils.ForegroundOnTaskRemovedActivity

/**
 * Created by kamil on 28.04.2018.
 */
class AlarmReceiver : BroadcastReceiver() {
    private val TAG = AlarmReceiver::class.java.simpleName
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.i(TAG,"onReceive()")

    }

    val WAKE : String = "Wake up"
}