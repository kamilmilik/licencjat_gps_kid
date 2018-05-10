package kamilmilik.licencjat_gps_kid

import android.content.Intent
import android.location.Location
import android.util.Log
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kamilmilik.licencjat_gps_kid.Utils.ForegroundOnTaskRemovedActivity
import kamilmilik.licencjat_gps_kid.models.TrackingModel
import kamilmilik.licencjat_gps_kid.models.User

/**
 * Created by kamil on 15.04.2018.
 */
class LocationUpdateCallback(var iLocationJobDispatcher: ILocationJobDispatcher) : LocationCallback() {
    private val TAG = LocationUpdateCallback::class.java.simpleName

    override fun onLocationResult(locationResult: LocationResult?) {
        Log.i(TAG,"onLocationResult()")
        locationResult ?: return
        for (location in locationResult!!.locations) {
            addCurrentUserLocationToFirebase(location)
        }
    }
    private fun addCurrentUserLocationToFirebase(lastLocation: Location) {
        val intent = Intent(iLocationJobDispatcher.getContext(), ForegroundOnTaskRemovedActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        iLocationJobDispatcher.getContext().startActivity(intent)
        var locations = FirebaseDatabase.getInstance().getReference("Locations")
        var currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {//prevent if user click logout to not update locationOfUserWhoChangeIt
            Log.i(TAG, "addCurrentUserMarkerAndRemoveOld() current user: " + currentUser!!.uid + " location " + lastLocation.toString())
            locations.child(currentUser!!.uid)
                    .setValue(TrackingModel(currentUser.uid,
                            currentUser!!.email!!,
                            lastLocation.latitude.toString(),
                            lastLocation.longitude.toString(),
                            currentUser!!.displayName!!,
                            System.currentTimeMillis()), object : DatabaseReference.CompletionListener {
                        override fun onComplete(error: DatabaseError?, reference: DatabaseReference?) {
                            Log.i(TAG, "onComplete()")
                            if (error == null) {
                                Log.i(TAG, "onComplete() position: $lastLocation saved to firebase database")
                                Log.i(TAG, "okey stop service")
                                iLocationJobDispatcher.finishJob()
                            } else {
                                Log.i(TAG, "there is problem to add data to database")
                            }
                        }
                    })

        }
    }

}