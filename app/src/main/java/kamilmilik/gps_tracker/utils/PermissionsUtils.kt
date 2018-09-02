package kamilmilik.gps_tracker.utils

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Build
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import kamilmilik.gps_tracker.R
import kamilmilik.gps_tracker.login.LoginActivity
import kamilmilik.gps_tracker.models.PermissionMessageExplanation

/**
 * Created by kamil on 07.08.2018.
 */
object PermissionsUtils {
    fun checkApkVersion(): Boolean = (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)


    fun checkPermissionGranted(context: Context, permission: String): Boolean = (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED)

    fun checkLocationPermission(activity: Activity, permission: String) {
        if (!checkPermissionGranted(activity, permission)) {
            if (checkIsWeShouldShowExplanationOfPermission(activity, permission)) {
                showExplanationDialogOfUsedPermission(activity, PermissionMessageExplanation(
                        permission,
                        Constants.LOCATION_PERMISSION_REQUEST_CODE,
                        activity.getString(R.string.needLocationPermission),
                        activity.getString(R.string.acceptLocationPermission),
                        activity.getString(R.string.locationSettings))
                )
            } else {
                requestPermission(activity, permission, Constants.LOCATION_PERMISSION_REQUEST_CODE)
            }
        }
    }

    private fun checkIsWeShouldShowExplanationOfPermission(activity: Activity, permission: String): Boolean = (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission))

    private fun showExplanationDialogOfUsedPermission(activity: Activity, permission: PermissionMessageExplanation) {
        AlertDialog.Builder(activity)
                .setTitle(permission.title)
                .setMessage(permission.message)
                .setPositiveButton(permission.positiveButton) { paramDialogInterface, paramInt ->
                    ActivityCompat.requestPermissions(activity,
                            arrayOf(permission.permission),
                            permission.requestCode)
                }
                .setNegativeButton(activity.getString(R.string.cancel)) { paramDialogInterface, paramInt ->
                    paramDialogInterface.cancel()
                    FirebaseAuth.getInstance().signOut()
                    Tools.startNewActivityWithoutPrevious(activity, LoginActivity::class.java)
                }.create().show()
    }

    private fun requestPermission(activity: Activity, permission: String, permissionRequestCode: Int) {
        ActivityCompat.requestPermissions(activity,
                arrayOf(permission),
                permissionRequestCode)
    }

    fun checkIsPermissionGrantedInRequestPermission(grantResults: IntArray): Boolean {
        return grantResults.isNotEmpty()
                && grantResults[0] == PackageManager.PERMISSION_GRANTED
    }

}