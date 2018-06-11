package kamilmilik.licencjat_gps_kid.background

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.annotation.RequiresApi

import android.util.Log


/**
 * Created by kamil on 28.04.2018.
 */
class AlarmReceiver : BroadcastReceiver() {
    private val TAG = AlarmReceiver::class.java.simpleName

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.i(TAG,"onReceive() alarm")

        var intent = Intent(context, ForegroundService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context!!.startForegroundService(intent);
        } else {
            context!!.startService(intent);
        }
    }
}