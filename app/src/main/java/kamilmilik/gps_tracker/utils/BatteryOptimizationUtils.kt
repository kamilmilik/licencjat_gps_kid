package kamilmilik.gps_tracker.utils

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings

/**
 * Created by kamil on 07.08.2018.
 */
object BatteryOptimizationUtils{
    fun addAppToWhiteList(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent()
            val packageName = activity.packageName
            val pm = activity.getSystemService(Context.POWER_SERVICE) as PowerManager
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                intent.data = Uri.parse("package:" + packageName)
                activity.startActivity(intent)
            }
        }
    }

    fun goToAddIgnoreBatteryOptimizationSettings(activity: Activity) {
//        xiaomiOptimizationAction(activity)
        val intent = Intent()
        val packageName = activity.packageName
        val pm = activity.getSystemService(Context.POWER_SERVICE) as PowerManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                intent.data = Uri.parse("package:" + packageName)
                activity.startActivity(intent)
            }
        }
    }

    private fun xiaomiOptimizationAction(activity: Activity) {
        if (Build.BRAND.equals("xiaomi", true)) {
            val intent = Intent()
            intent.component = ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity")
            //com.miui.powerkeeper/com.miui.powerkeeper.PowerKeeperBackgroundService -> to sie wlacza jak wejde w tel xiaomi w zachowanie aplikacji w tle
            activity.startActivity(intent)
        }
    }
}