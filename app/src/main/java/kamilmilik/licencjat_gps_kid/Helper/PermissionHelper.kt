package kamilmilik.licencjat_gps_kid.Helper

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Build
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.util.Log
import android.widget.Toast
import kamilmilik.licencjat_gps_kid.Constants

/**
 * Created by kamil on 26.02.2018.
 */
class PermissionHelper(var context : Context){
    val TAG : String = PermissionHelper::class.java.simpleName

    fun checkApkVersion() : Boolean{
        return (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
    }
    fun checkPermissionGranted():Boolean{
        return (ContextCompat.checkSelfPermission(context,
                android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
    }
    fun checkLocationPermission() {
        Log.i(TAG,"checkLocationPermission checkPermissionGranted " + checkPermissionGranted())
        if (!checkPermissionGranted()) {
            Log.i(TAG, " not granted")
            // Should we show an explanation?
            if (checkIsWeShouldShowExplanationOfPermission()) {
                Log.i(TAG,"show explanation dialog")
                showExplanationDialogOfUsedPermission()
            } else {
                Log.i(TAG,"request permission")
                // No explanation needed, we can request the permission.
                requestPermission()
            }
        }
    }
    fun checkIsWeShouldShowExplanationOfPermission() : Boolean{
        return (ActivityCompat.shouldShowRequestPermissionRationale(context as Activity,
                android.Manifest.permission.ACCESS_FINE_LOCATION))
    }
    fun showExplanationDialogOfUsedPermission(){
        // Show an explanation to the user *asynchronously* -- don't block
        // this thread waiting for the user's response! After the user
        // sees the explanation, try again to request the permission.
        AlertDialog.Builder(context)
                .setTitle("Location Permission Needed")
                .setMessage("This app needs the Location permission, please accept to use locationOfUserWhoChangeIt functionality")
                .setPositiveButton("Location Settings", DialogInterface.OnClickListener { paramDialogInterface, paramInt ->
                    //Prompt the user once explanation has been shown
                    ActivityCompat.requestPermissions(context as Activity,
                            arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                            Constants.MY_PERMISSION_REQUEST_CODE );
                })
                .setNegativeButton("Cancel", DialogInterface.OnClickListener { paramDialogInterface, paramInt ->
                    paramDialogInterface.cancel()
                }).create().show()
    }
    fun requestPermission(){
        ActivityCompat.requestPermissions(context as Activity,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                Constants.MY_PERMISSION_REQUEST_CODE )
    }
    fun checkIsPermissionGrantedInRequestPermission(grantResults: IntArray) : Boolean{
        return grantResults.isNotEmpty()
                && grantResults[0] == PackageManager.PERMISSION_GRANTED
    }
    /**
     * Displays a dialog with error message explaining that the locationOfUserWhoChangeIt permission is missing.
     */
     fun showMissingPermissionError() {
        Toast.makeText(context,"Your permission was not granted", Toast.LENGTH_LONG).show()
    }
}