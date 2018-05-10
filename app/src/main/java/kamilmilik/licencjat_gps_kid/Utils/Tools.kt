package kamilmilik.licencjat_gps_kid.Utils

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
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
import kamilmilik.licencjat_gps_kid.Constants
import kamilmilik.licencjat_gps_kid.R
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.progress_bar.*
import kotlinx.android.synthetic.main.toolbar.*

/**
 * Created by kamil on 22.04.2018.
 */
object Tools{

    private val TAG = Tools::class.java.simpleName

    fun isGooglePlayServicesAvailable(activity: Context): Boolean {
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val status = googleApiAvailability.isGooglePlayServicesAvailable(activity)
        val version = GoogleApiAvailability.GOOGLE_PLAY_SERVICES_VERSION_CODE
        Log.i(TAG,"isGooglePlayServicesAvailable() przed ifem status " + status + " version " + version)
        if (status != ConnectionResult.SUCCESS || version <= Constants.GOOGLE_PLAY_SERVICES_VERSION) {
            Log.i(TAG,"isGooglePlayServicesAvailable()")
            if (googleApiAvailability.isUserResolvableError(status)) {
                googleApiAvailability.getErrorDialog(activity as Activity?, status, 2404).show()
            }
            return false
        }
        return true
    }

    fun makeAlertDialogBuilder(context : Context, title : String, message : String) : AlertDialog.Builder{
        val alert = AlertDialog.Builder(context)
        alert.setTitle(title)
        alert.setMessage(message)
        return alert
    }

    fun updateProfileName(activity: Activity, currentUser: FirebaseUser, newName: String, inputEditText: EditText) {
        activity.progressBarRelative.visibility = View.VISIBLE

        val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(newName)
                .build()
        currentUser!!.updateProfile(profileUpdates)
                .addOnCompleteListener {
                    Log.i(TAG, "onComplete() name ")
                    var map = HashMap<String, Any>() as MutableMap<String, Any>
                    map.put("user_name", newName)
                    FirebaseDatabase.getInstance().reference.child("user_account_settings").child(currentUser.uid).updateChildren(map)
                    FirebaseDatabase.getInstance().reference.child("Locations").child(currentUser.uid).updateChildren(map)
                    activity.userNameTextView.text = currentUser.displayName
                    activity.progressBarRelative.visibility = View.GONE
                }
    }

    fun setupToolbar(activity : AppCompatActivity) : Toolbar {
        activity.setSupportActionBar(activity.toolbar)
        println("support action bar " + activity.supportActionBar)

        activity.supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        activity.supportActionBar!!.setDisplayShowHomeEnabled(true)
        return activity.toolbar
    }
}