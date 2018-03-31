package kamilmilik.licencjat_gps_kid.Utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import android.os.PowerManager
import android.app.AlarmManager
import android.app.PendingIntent
import android.util.Log
import com.google.android.gms.cast.CastRemoteDisplayLocalService.startService


/**
 * Created by kamil on 31.03.2018.
 */
class Alarm : BroadcastReceiver() {
    private val TAG = Alarm::class.java.simpleName
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.i(TAG,"onReceive()")
        val pm = context!!.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "")
        wl.acquire()

        // Put here YOUR code.
        var intent = Intent(context, PolygonAndLocationService::class.java)
        context.startService(intent)
        wl.release()
    }
    fun setAlarm(context : Context){
        Log.i(TAG,"setAlarm()")
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val i = Intent(context, Alarm::class.java)
        val pi = PendingIntent.getBroadcast(context, 0, i, 0)
        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),100 * 60,pi);
    }

    fun CancelAlarm(context: Context) {
        val intent = Intent(context, Alarm::class.java)
        val sender = PendingIntent.getBroadcast(context, 0, intent, 0)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(sender)
    }
}