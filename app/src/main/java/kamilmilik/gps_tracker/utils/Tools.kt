package kamilmilik.gps_tracker.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
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
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.iid.FirebaseInstanceId


/**
 * Created by kamil on 22.04.2018.
 */
object Tools {

    private val TAG = Tools::class.java.simpleName

    fun isGooglePlayServicesAvailable(activity: Context): Boolean {
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val status = googleApiAvailability.isGooglePlayServicesAvailable(activity)
        val version = GoogleApiAvailability.GOOGLE_PLAY_SERVICES_VERSION_CODE
        if (status != ConnectionResult.SUCCESS || version <= Constants.GOOGLE_PLAY_SERVICES_VERSION) {
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
        activity.supportActionBar?.setDisplayHomeAsUpEnabled(isBackButtonEnabled)
        activity.supportActionBar?.setDisplayShowHomeEnabled(isBackButtonEnabled)
        return activity.toolbar
    }

    fun <T> startNewActivityWithoutPrevious(activity: Activity, classType: Class<T>) {
        activity.finish()
        val intent = Intent(activity, classType)
        intent.flags = (Intent.FLAG_ACTIVITY_NEW_TASK) or Intent.FLAG_ACTIVITY_CLEAR_TASK
        activity.startActivity(intent)
    }

    fun addDeviceTokenToDatabase() {
        val currentUser = FirebaseAuth.getInstance()?.currentUser
        if (currentUser != null) {
            val userDatabase = FirebaseDatabase.getInstance().reference.child(Constants.DATABASE_USER_ACCOUNT_SETTINGS)
            val currentUserId = currentUser.uid
            val deviceTokenId = FirebaseInstanceId.getInstance().token
            if (deviceTokenId != null) {
                userDatabase?.child(currentUserId)?.child(Constants.DATABASE_DEVICE_TOKEN_FIELD)?.setValue(deviceTokenId)
            }
        }
    }

    fun <T> addDeviceTokenToDatabaseAndStartNewActivity(activity: Activity, classType: Class<T>) {
        val userDatabase = FirebaseDatabase.getInstance().reference.child(Constants.DATABASE_USER_ACCOUNT_SETTINGS)
        val currentUserId = FirebaseAuth.getInstance()?.currentUser?.uid
        val deviceTokenId = FirebaseInstanceId.getInstance().token
        if (deviceTokenId != null) {
            userDatabase?.child(currentUserId)?.child(Constants.DATABASE_DEVICE_TOKEN_FIELD)?.setValue(deviceTokenId)?.addOnSuccessListener {
                startNewActivityWithoutPrevious(activity, classType)
            }
        } else {
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

    fun removeUserPermission(reference: DatabaseReference, userIdToRemove: String) {
        FirebaseAuth.getInstance().currentUser?.uid?.let { currentUserUid ->
            reference.child(Constants.DATABASE_LOCATIONS).child(currentUserUid).child(Constants.DATABASE_PERMISSIONS_FIELD).child(userIdToRemove).removeValue()
        }
    }

}