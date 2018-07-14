package kamilmilik.gps_tracker.utils

import android.R
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
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.progress_bar.*
import kotlinx.android.synthetic.main.toolbar.*
import java.util.*
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.iid.FirebaseInstanceId
import kamilmilik.gps_tracker.login.LoginActivity
import kamilmilik.gps_tracker.models.PolygonModel
import kamilmilik.gps_tracker.models.TrackingModel
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
            Toast.makeText(context, context!!.getString(kamilmilik.gps_tracker.R.string.enterEmail), Toast.LENGTH_SHORT).show()
            return false
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(context, context!!.getString(kamilmilik.gps_tracker.R.string.enterPassword), Toast.LENGTH_SHORT).show()
            return false
        }
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(context, context!!.getString(kamilmilik.gps_tracker.R.string.enterName), Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    fun checkIfUserEnterValidData(context: Context?, email: String, password: String): Boolean {
        Log.i(TAG, "checkIfUserEnterValidData: check if is valid data in user login/register")
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(context, context!!.getString(kamilmilik.gps_tracker.R.string.enterEmail), Toast.LENGTH_SHORT).show()
            return false
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(context, context!!.getString(kamilmilik.gps_tracker.R.string.enterPassword), Toast.LENGTH_SHORT).show()
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
        activity.progressBarRelative?.visibility = View.VISIBLE
        val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(newName)
                .build()
        currentUser.updateProfile(profileUpdates)
                .addOnCompleteListener { task ->
                    onCompleteListener.onComplete(task)
                    activity.progressBarRelative?.visibility = View.GONE
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

    fun  addDeviceTokenToDatabase() {
        val currentUser = FirebaseAuth.getInstance()!!.currentUser
            if(currentUser != null){
            val userDatabase = FirebaseDatabase.getInstance().reference.child(Constants.DATABASE_USER_ACCOUNT_SETTINGS)
            val currentUserId = currentUser.uid
            val deviceTokenId = FirebaseInstanceId.getInstance().token
            userDatabase!!.child(currentUserId).child(Constants.DATABASE_DEVICE_TOKEN_FIELD).setValue(deviceTokenId)
        }
    }

    fun <T> addDeviceTokenToDatabaseAndStartNewActivity(activity: Activity, classType: Class<T>) {
        val userDatabase = FirebaseDatabase.getInstance().reference.child(Constants.DATABASE_USER_ACCOUNT_SETTINGS)
        val currentUserId = FirebaseAuth.getInstance()!!.currentUser!!.uid
        val deviceTokenId = FirebaseInstanceId.getInstance().token
        userDatabase!!.child(currentUserId).child(Constants.DATABASE_DEVICE_TOKEN_FIELD).setValue(deviceTokenId).addOnSuccessListener {
            startNewActivityWithoutPrevious(activity, classType)
        }
    }

    fun generateRandomKey(length: Int): String {
        val baseChars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"
        val randomRange = Constants.RANGE_RANDOM
        val random = Random()
        val sb = StringBuilder(length)
        for (i in 0 until length)
            sb.append(baseChars[random.nextInt(randomRange)])
        return sb.toString()
    }

    fun removeWhiteSpaceFromString(givenString: String): String {
        return givenString.replace("\\s".toRegex(), "")
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

    private fun checkIsWeShouldShowExplanationOfPermission(activity: Activity, permission: String): Boolean = (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission))

    private fun showExplanationDialogOfUsedPermission(activity: Activity, permission: String) {
        AlertDialog.Builder(activity)
                .setTitle(activity.getString(kamilmilik.gps_tracker.R.string.needLocationPermission))
                .setMessage(activity.getString(kamilmilik.gps_tracker.R.string.acceptLocationPermission))
                .setPositiveButton(activity.getString(kamilmilik.gps_tracker.R.string.locationSettings), DialogInterface.OnClickListener { paramDialogInterface, paramInt ->
                    ActivityCompat.requestPermissions(activity,
                            arrayOf(permission),
                            Constants.MY_PERMISSION_REQUEST_CODE);
                })
                .setNegativeButton(activity.getString(R.string.cancel), DialogInterface.OnClickListener { paramDialogInterface, paramInt ->
                    paramDialogInterface.cancel()
                    FirebaseAuth.getInstance().signOut()
                    Tools.startNewActivityWithoutPrevious(activity, LoginActivity::class.java)
                }).create().show()
    }

    private fun requestPermission(activity: Activity, permission: String) {
        ActivityCompat.requestPermissions(activity,
                arrayOf(permission),
                Constants.MY_PERMISSION_REQUEST_CODE)
    }

    fun checkIsPermissionGrantedInRequestPermission(grantResults: IntArray): Boolean {
        return grantResults.isNotEmpty()
                && grantResults[0] == PackageManager.PERMISSION_GRANTED
    }

    fun createLocationVariable(userLatLng: LatLng): Location {
        val userLoc = Location("")
        userLoc.latitude = userLatLng.latitude
        userLoc.longitude = userLatLng.longitude
        return userLoc
    }

    fun calculateDistanceBetweenTwoPoints(currentUserLocation: Location, followingUserLoc: Location): Pair<Float, String> {
        val measure: String?
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
            val intent = Intent()
            val packageName = activity.packageName
            val pm = activity.getSystemService(POWER_SERVICE) as PowerManager
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS;
                intent.data = Uri.parse("package:" + packageName);
                activity.startActivity(intent);
            }
        }
    }

    fun addLocationToFirebaseDatabaseByRest(trackingModel: TrackingModel, tokenId : String) {
        try {
            val url = URL("https://licencjat-kid-track.firebaseio.com/Locations/${trackingModel.user_id}.json?auth=" + tokenId)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "PUT"
            conn.setRequestProperty("Accept", "application/json")
            conn.doOutput = true
            conn.doInput = true

            val jsonParam = JSONObject();
            jsonParam.put("email", trackingModel.email)
            jsonParam.put("lat", trackingModel.lat)
            jsonParam.put("lng", trackingModel.lng)
            jsonParam.put("user_id", trackingModel.user_id)
            jsonParam.put("user_name", trackingModel.user_name)

            Log.i("JSON", jsonParam.toString())
            val os = DataOutputStream(conn.outputStream)
            os.writeBytes(jsonParam.toString())
            os.flush()
            os.close()
            Log.i("STATUS", (conn.responseCode).toString())
            Log.i("MSG", conn.responseMessage)
            conn.disconnect()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * convert given polygon map object (PolygonModel) with MyOwnLAtLng to polygon map object with LatLng
     * @param polygonsFromDbMap given polygon map model (model with tag and arrayList<GeoLatLng)
     */
    fun changePolygonModelWithMyOwnLatLngListToLatLngList(polygonsFromDbMap: PolygonModel): ArrayList<LatLng> {
        val newList: ArrayList<LatLng> = ArrayList(polygonsFromDbMap.polygonLatLngList.size)
        polygonsFromDbMap.polygonLatLngList.mapTo(newList) { LatLng(it.latitude!!, it.longitude!!) }
        return newList
    }

    fun goToAddIgnoreBatteryOptimizationSettings(activity: Activity){
//        xiaomiOptimizationAction(activity)
        val intent = Intent()
        val packageName = activity.packageName
        val pm = activity.getSystemService(Context.POWER_SERVICE) as PowerManager
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
            val intent = Intent()
            intent.component = ComponentName ("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity")
            activity.startActivity(intent)
        }
    }
}