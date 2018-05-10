package kamilmilik.licencjat_gps_kid.Helper.LocationOperation

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.Marker
import com.google.firebase.auth.FirebaseAuth
import kamilmilik.licencjat_gps_kid.Helper.OnMarkerAddedCallback
import kamilmilik.licencjat_gps_kid.Helper.PermissionHelper
import kamilmilik.licencjat_gps_kid.LocationUpdateCallback
import kamilmilik.licencjat_gps_kid.models.TrackingModel
import java.util.HashMap
import com.google.android.gms.common.GoogleApiAvailability
import android.app.Activity
import com.google.firebase.database.*
import kamilmilik.licencjat_gps_kid.Utils.Tools
import kamilmilik.licencjat_gps_kid.models.User


/**
 * Created by kamil on 24.02.2018.
 */
class LocationHelper(
        var context: Context,
        var permissionHelper: PermissionHelper,
        var locationFirebaseHelper: LocationFirebaseHelper,
        var onMarkerAddedCallback: OnMarkerAddedCallback) {
    var TAG: String = LocationHelper::class.java.simpleName

    lateinit var fusedLocationClient: FusedLocationProviderClient
    lateinit var locationCallback : LocationCallback
    var mLocationRequest: LocationRequest? = null
    var mLastLocation: Location? = null
    var mCurrLocationMarker: Marker? = null




    private fun addCurrentUserLocationToFirebase(lastLocation: Location) {
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
                                Log.i(TAG, "onComplete() position saved to firebase database")
                                Log.i(TAG, "okey stop service")
                            } else {
                                Log.i(TAG, "there is problem to add data to database")
                            }
                        }
                    })

        }
    }
    @SuppressLint("MissingPermission")
    fun getLocation() {
        if (permissionHelper!!.checkPermissionGranted()) {
            Log.i(TAG,"permission granted in locationhelper")
            if (Tools.isGooglePlayServicesAvailable(context)) {
                fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                createLocationRequest()
                locationCallback = object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult?) {
                        Log.i(TAG, "onLocationResult() $locationResult")
                        for (location in locationResult!!.locations) {
                            if (FirebaseAuth.getInstance().currentUser != null) {
                                Log.i(TAG, "onLocationChanged")
                                mLastLocation = location!!
                                //TODO czy to cos robi?
                                if (mCurrLocationMarker != null) {//prevent if user click logout to not update locationOfUserWhoChangeIt
                                    mCurrLocationMarker!!.remove();
                                }

                                locationFirebaseHelper!!.addCurrentUserMarkerAndRemoveOld(location!!, onMarkerAddedCallback)
                                //TODO addCurrentUserLocationToFirebase is only for test in emulator, in normal device you should comment this, remove this later
                                //addCurrentUserLocationToFirebase(location)
                            }
                        }
                    }
                }
                fusedLocationClient.requestLocationUpdates(mLocationRequest,
                        locationCallback
                        , Looper.myLooper())
            }else{
                Log.i(TAG,"getLocation() wrong version of google service")
            }
        }
    }

    fun createLocationRequest() {
        Log.i(TAG, "createLocationRequest")
        mLocationRequest = LocationRequest.create()
        mLocationRequest!!.interval = 1000
        mLocationRequest!!.fastestInterval = 5000
        mLocationRequest!!.smallestDisplacement = 1F
        mLocationRequest!!.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
    }

}