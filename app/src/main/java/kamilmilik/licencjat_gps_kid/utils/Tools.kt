package kamilmilik.licencjat_gps_kid.utils

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Context.POWER_SERVICE
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.View
import android.widget.EditText
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.progress_bar.*
import kotlinx.android.synthetic.main.toolbar.*
import java.util.*
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.text.TextUtils
import android.widget.Toast
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import kamilmilik.licencjat_gps_kid.login.LoginActivity
import kamilmilik.licencjat_gps_kid.models.PolygonModel
import org.json.JSONObject
import java.io.DataOutputStream
import java.net.HttpURLConnection
import java.net.URL


/**
 * Created by kamil on 22.04.2018.
 */
object Tools {

    private val TAG = Tools::class.java.simpleName

    fun checkIfUserEnterValidData(context: Context?, email: String, password: String, name: String): Boolean {
        Log.i(TAG, "checkIfUserEnterValidData: check if is valid data in user login/register")
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(context, "Please enter currentUserId", Toast.LENGTH_SHORT).show()
            return false
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(context, "Please enter password", Toast.LENGTH_SHORT).show()
            return false
        }
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(context, "Please enter name", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    fun checkIfUserEnterValidData(context: Context?, email: String, password: String): Boolean {
        Log.i(TAG, "checkIfUserEnterValidData: check if is valid data in user login/register")
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(context, "Please enter currentUserId", Toast.LENGTH_SHORT).show()
            return false
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(context, "Please enter password", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    fun isGooglePlayServicesAvailable(activity: Context): Boolean {
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val status = googleApiAvailability.isGooglePlayServicesAvailable(activity)
        val version = GoogleApiAvailability.GOOGLE_PLAY_SERVICES_VERSION_CODE
        Log.i(TAG, "isGooglePlayServicesAvailable() przed ifem status " + status + " version " + version)
        if (status != ConnectionResult.SUCCESS || version <= Constants.GOOGLE_PLAY_SERVICES_VERSION) {
            Log.i(TAG, "isGooglePlayServicesAvailable()")
            if (googleApiAvailability.isUserResolvableError(status)) {
                googleApiAvailability.getErrorDialog(activity as Activity?, status, 2404).show()
            }
            return false
        }
        return true
    }

    fun makeAlertDialogBuilder(context: Context, title: String, message: String): AlertDialog.Builder {
        val alert = AlertDialog.Builder(context)
        alert.setTitle(title)
        alert.setMessage(message)
        return alert
    }

    fun updateProfileName(activity: Activity, currentUser: FirebaseUser, newName: String, onCompleteListener: OnCompleteListener<Void>) {
        activity.progressBarRelative.visibility = View.VISIBLE

        val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(newName)
                .build()
        currentUser.updateProfile(profileUpdates)
                .addOnCompleteListener { task ->
                        onCompleteListener.onComplete(task)
                        activity.progressBarRelative.visibility = View.GONE
                }
    }

    fun setupToolbar(activity: AppCompatActivity, isBackButtonEnabled: Boolean): Toolbar {
        activity.setSupportActionBar(activity.toolbar)
        activity.supportActionBar!!.setDisplayHomeAsUpEnabled(isBackButtonEnabled)
        activity.supportActionBar!!.setDisplayShowHomeEnabled(isBackButtonEnabled)
        return activity.toolbar
    }

    fun <T> startNewActivityWithoutPrevious(activity: Activity, classType: Class<T>) {
        activity.finish()
        val intent = Intent(activity, classType)
        intent.flags = (Intent.FLAG_ACTIVITY_NEW_TASK) or Intent.FLAG_ACTIVITY_CLEAR_TASK
        activity.startActivity(intent)
    }

    fun generateRandomKey(length: Int): String {
        val baseChars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"
        var randomRange = 36
        val random = Random()
        val sb = StringBuilder(length)
        for (i in 0 until length)
            sb.append(baseChars[random.nextInt(randomRange)])
        return sb.toString()
    }

    fun removeWhiteSpaceFromString(givenString: String): String {
        return givenString.replace("\\s".toRegex(), "")
    }

    fun isInternetConnection(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return cm.activeNetworkInfo != null && cm.activeNetworkInfo.isConnected
    }

    fun checkApkVersion(): Boolean = (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)


    fun checkPermissionGranted(context: Context, permission: String): Boolean = (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED)

    fun checkLocationPermission(activity: Activity, permission: String) {
        if (!checkPermissionGranted(activity, permission)) {
            if (checkIsWeShouldShowExplanationOfPermission(activity, permission)) {
                showExplanationDialogOfUsedPermission(activity, permission)
            } else {
                requestPermission(activity, permission)
            }
        }
    }

    fun checkIsWeShouldShowExplanationOfPermission(activity: Activity, permission: String): Boolean = (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission))

    fun showExplanationDialogOfUsedPermission(activity: Activity, permission: String) {
        AlertDialog.Builder(activity)
                .setTitle("Location Permission Needed")
                .setMessage("This app needs the Location permission, please accept to use locationOfUserWhoChangeIt functionality")
                .setPositiveButton("Location Settings", DialogInterface.OnClickListener { paramDialogInterface, paramInt ->
                    ActivityCompat.requestPermissions(activity,
                            arrayOf(permission),
                            Constants.MY_PERMISSION_REQUEST_CODE);
                })
                .setNegativeButton("Cancel", DialogInterface.OnClickListener { paramDialogInterface, paramInt ->
                    paramDialogInterface.cancel()
                    FirebaseAuth.getInstance().signOut()
                    Tools.startNewActivityWithoutPrevious(activity, LoginActivity::class.java)
                }).create().show()
    }

    fun requestPermission(activity: Activity, permission: String) {
        ActivityCompat.requestPermissions(activity,
                arrayOf(permission),
                Constants.MY_PERMISSION_REQUEST_CODE)
    }

    fun checkIsPermissionGrantedInRequestPermission(grantResults: IntArray): Boolean {
        return grantResults.isNotEmpty()
                && grantResults[0] == PackageManager.PERMISSION_GRANTED
    }

    fun createLocationVariable(userLatLng: LatLng): Location {
        var userLoc = Location("")
        userLoc.latitude = userLatLng.latitude
        userLoc.longitude = userLatLng.longitude
        return userLoc
    }

    fun calculateDistanceBetweenTwoPoints(currentUserLocation: Location, followingUserLoc: Location): Pair<Float, String> {
        var measure: String?
        var distance: Float = currentUserLocation.distanceTo(followingUserLoc)
        if (distance > 1000) {
            distance = (distance / 1000)
            measure = "km"
        } else {
            measure = "m"
        }
        return Pair(distance, measure)
    }

    fun addAppToWhiteList(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            var intent = Intent()
            var packageName = activity.packageName
            var pm = activity.getSystemService(POWER_SERVICE) as PowerManager
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS;
                intent.data = Uri.parse("package:" + packageName);
                activity.startActivity(intent);
            }
        }
    }

    fun addLocationToFirebaseDatabaseByRest(email: String, latitude: String, longitude: String, currentTime: Long, userId: String, userName: String) {
        try {
            Log.i(TAG, "make request")
            var url = URL("https://licencjat-kid-track.firebaseio.com/Locations/$userId.json");
            var conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "PUT";
            conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.doOutput = true;
            conn.doInput = true;

            var jsonParam = JSONObject();
            jsonParam.put("email", email)
            jsonParam.put("lat", latitude)
            jsonParam.put("lng", longitude)
            jsonParam.put("time", currentTime)
            jsonParam.put("user_id", userId)
            jsonParam.put("user_name", userName)

            Log.i("JSON", jsonParam.toString());
            var os = DataOutputStream(conn.outputStream);
            os.writeBytes(jsonParam.toString());
            os.flush();
            os.close();
            Log.i("STATUS", (conn.responseCode).toString());
            Log.i("MSG", conn.responseMessage);
            conn.disconnect()
        } catch (e: Exception) {
            e.printStackTrace();
        }
    }

    /**
     * convert given polygon map object (PolygonModel) with MyOwnLAtLng to polygon map object with LatLng
     * @param polygonsFromDbMap given polygon map model (model with tag and arrayList<GeoLatLng)
     */
    fun changePolygonModelWithMyOwnLatLngListToLatLngList(polygonsFromDbMap: PolygonModel): ArrayList<LatLng> {
        var newList: ArrayList<LatLng> = ArrayList(polygonsFromDbMap!!.polygonLatLngList!!.size)
        polygonsFromDbMap!!.polygonLatLngList!!.mapTo(newList) { LatLng(it.latitude!!, it.longitude!!) }
        return newList
    }

    fun goToAddIgnoreBatteryOptimizationSettings(activity: Activity){
        xiaomiOptimizationAction(activity)
            var intent = Intent()
            var packageName = activity.packageName
            var pm = activity.getSystemService(Context.POWER_SERVICE) as PowerManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                    intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                    intent.data = Uri.parse("package:" + packageName)
                    activity.startActivity(intent);
                }
            }
    }

    private fun xiaomiOptimizationAction(activity: Activity){
        if (Build.BRAND.equals("xiaomi", true)) {
            var intent = Intent()
            intent.setComponent(ComponentName ("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity"))
            activity.startActivity(intent)
        }
    }
}