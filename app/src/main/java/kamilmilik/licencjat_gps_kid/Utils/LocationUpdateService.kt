package kamilmilik.licencjat_gps_kid.Utils

import android.app.IntentService
import android.content.Intent
import android.location.Location
import android.util.Log
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import kamilmilik.licencjat_gps_kid.Helper.LocationFirebaseHelper
import kamilmilik.licencjat_gps_kid.models.TrackingModel

/**
 * Created by kamil on 12.03.2018.
 */
class LocationUpdateService : IntentService{
    private val TAG  = LocationUpdateService::class.java.simpleName
    private var location : Location? = null
    constructor() : super("LocationUpdateService")

    override fun onHandleIntent(intent: Intent?) {
        if (LocationResult.hasResult(intent)) {
            val locationResult = LocationResult.extractResult(intent)
            val location = locationResult.lastLocation
            if (location != null) {
                addCurrentUserLocationToFirebase(location)
                Log.d("locationtesting", "accuracy: " + location!!.getAccuracy() + " lat: " + location!!.getLatitude() + " lon: " + location!!.getLongitude())
            }
        }
    }
    fun addCurrentUserLocationToFirebase(lastLocation: Location) {
        Log.i(TAG, "addCurrentUserLocationToFirebase")
        var locations = FirebaseDatabase.getInstance().getReference("Locations")
        var currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {//prevent if user click logout to not update location
            locations.child(currentUser!!.uid)
                    .setValue(TrackingModel(currentUser.uid,
                            currentUser!!.email!!,
                            lastLocation.latitude.toString(),
                            lastLocation.longitude.toString()))

        }
    }
}