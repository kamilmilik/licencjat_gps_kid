package kamilmilik.gps_tracker.utils

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.preference.PreferenceManager
import android.provider.Settings
import android.view.Gravity
import android.widget.Toast
import kamilmilik.gps_tracker.R
import android.content.ComponentName


/**
 * Created by kamil on 07.08.2018.
 */
object BatteryOptimizationUtils {
//    fun addAppToWhiteList(activity: Activity) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            val intent = Intent()
//            val packageName = activity.packageName
//            val pm = activity.getSystemService(Context.POWER_SERVICE) as PowerManager
//            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
//                intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
//                intent.data = Uri.parse("package:" + packageName)
//                activity.startActivity(intent)
//            }
//        }
//    }
//
//    fun ignoreBatteryOptimizationSettings(activity: Activity) {
//        optimizationAction(activity)
//        val intent = Intent()
//        val packageName = activity.packageName
//        val pm = activity.getSystemService(Context.POWER_SERVICE) as PowerManager
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
//                intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
//                intent.data = Uri.parse("package:" + packageName)
//                activity.startActivity(intent)
//            }
//        }
//    }

    fun optimizationAction(activity: Activity) {
        try {
            if (!getIsFirstRunFromShared(activity)) {
                saveIsFirstRunToShared(activity)
                when (Build.BRAND) {
                    "xiaomi" -> {
                        activity.startActivity(Intent().apply {
                            setClassName("com.miui.powerkeeper", "com.miui.powerkeeper.ui.HiddenAppsContainerManagementActivity")
                        })
                        showOptimizationToast(activity, activity.getString(R.string.xiaomiBatteryOptimizationInformation, activity.applicationInfo.loadLabel(activity.packageManager)))
                    }
                }
            }
        } catch (ex: ExceptionInInitializerError) {
            ex.printStackTrace()
        } catch (ex: ActivityNotFoundException) {
            ex.printStackTrace()
        }
    }

    private fun saveIsFirstRunToShared(activity: Activity) {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(activity) ?: return
        with(sharedPref.edit()) {
            putBoolean(Constants.IS_FIRST_RUN, true)
            apply()
        }
    }

    private fun getIsFirstRunFromShared(activity: Activity): Boolean = PreferenceManager.getDefaultSharedPreferences(activity).getBoolean(Constants.IS_FIRST_RUN, false)

    private fun showOptimizationToast(activity: Activity, message: String) {
        for (i in 1..5) {
            Toast.makeText(activity, message, Toast.LENGTH_LONG).apply {
                setGravity(Gravity.CENTER, 0, 0)
                show()
            }
        }
    }
}