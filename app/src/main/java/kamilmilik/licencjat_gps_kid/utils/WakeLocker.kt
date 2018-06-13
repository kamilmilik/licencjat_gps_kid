package kamilmilik.licencjat_gps_kid.utils

import android.content.Context
import android.os.PowerManager
import android.util.Log
import android.net.wifi.WifiManager

/**
 * Created by kamil on 15.05.2018.
 */
object WakeLocker {

    private val TAG = WakeLocker::class.java.simpleName

    private var wakeLock: PowerManager.WakeLock? = null

    private var wifiLock: WifiManager.WifiLock? = null


    fun acquire(context: Context) {
        if (wakeLock != null) {
            wakeLock!!.release()
        }

        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK /*or
                PowerManager.ACQUIRE_CAUSES_WAKEUP or
                PowerManager.ON_AFTER_RELEASE*/, "WakeLock")
        wakeLock!!.acquire()

        if (wifiLock != null) {
            wifiLock!!.release()
        }
        val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        wifiLock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL, TAG)
        wifiLock!!.setReferenceCounted(false)

        if (!wifiLock!!.isHeld){
            wifiLock!!.acquire()
        }
    }

    fun release() {
        if (wakeLock != null) {
            wakeLock!!.release()
        }else{
            Log.i(TAG,"release() wake lock is null")
        }
        wakeLock = null

        if (wifiLock != null) {
            wifiLock!!.release()
        }else{
            Log.i(TAG,"release() Wifi lock is null")
        }
        wifiLock = null
    }
}