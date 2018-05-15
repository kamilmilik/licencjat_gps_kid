package kamilmilik.licencjat_gps_kid

import android.content.Context
import android.os.PowerManager
import android.content.Context.POWER_SERVICE
import android.util.Log


/**
 * Created by kamil on 15.05.2018.
 */
object WakeLocker {

    private val TAG = WakeLocker::class.java.simpleName

    private var wakeLock: PowerManager.WakeLock? = null

    fun acquire(context: Context) {
        if (wakeLock != null) wakeLock!!.release()

        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK or
                PowerManager.ACQUIRE_CAUSES_WAKEUP or
                PowerManager.ON_AFTER_RELEASE, "WakeLock")
        wakeLock!!.acquire()
    }

    fun release() {
        if (wakeLock != null) {
            wakeLock!!.release()
        }else{
            Log.i(TAG,"release() wake lock is null")
        }
        wakeLock = null
    }
}