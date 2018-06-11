package kamilmilik.licencjat_gps_kid.utils

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Context
import android.location.Location
import android.os.Looper
import android.util.Log
import com.google.android.gms.location.*
import com.google.firebase.auth.FirebaseAuth
import kamilmilik.licencjat_gps_kid.map.RecyclerViewAction
import kamilmilik.licencjat_gps_kid.map.LocationFirebaseMarkerAction


/**
 * Created by kamil on 24.02.2018.
 */
class LocationOperations(
        var context: Context,
        var progressDialog: ProgressDialog,
        var locationFirebaseMarkerAction: LocationFirebaseMarkerAction,
        var recyclerViewAction: RecyclerViewAction) {

    var TAG: String = LocationOperations::class.java.simpleName

    lateinit var fusedLocationClient: FusedLocationProviderClient
    lateinit var locationCallback: LocationCallback
    var locationRequest: LocationRequest? = null
    var lastLocation: Location? = null

    @SuppressLint("MissingPermission")
    fun getLocation() {
        if (Tools.checkPermissionGranted(context, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
            if (Tools.isGooglePlayServicesAvailable(context)) {
                fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                createLocationRequest()
                locationCallback = object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult?) {
                        for (location in locationResult!!.locations) {
                            Log.i(TAG,"onLocationResult() w callbacku")
                            if (FirebaseAuth.getInstance().currentUser != null) {
                                lastLocation = location!!

                                locationFirebaseMarkerAction!!.addCurrentUserMarkerAndRemoveOld(location!!, recyclerViewAction, progressDialog)
                            }
                        }
                    }
                    override fun onLocationAvailability(locationAvailability : LocationAvailability?) {
                        super.onLocationAvailability(locationAvailability)
                        Log.i(TAG,"onLocationAvailability() " + locationAvailability!!.isLocationAvailable + " stop service" )
                                    if(!locationAvailability!!.isLocationAvailable){
                                        fusedLocationClient.lastLocation.addOnSuccessListener { location : Location? ->
                                            //TODO przy pierwszej instalacji apki location jest null caly czzas nawet jak zmieniam polozenie(mozliwe ze tylko w emulatorze) sprawdzic to
                                            if(location != null){
                                                locationFirebaseMarkerAction!!.addCurrentUserMarkerAndRemoveOld(location!!, recyclerViewAction, progressDialog)
                                            }
                                        }
                                    }
                    }
                }
                fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
            }
        }
    }

    private fun createLocationRequest() {
        Log.i(TAG, "createLocationRequest")
        locationRequest = LocationRequest.create()
        locationRequest!!.interval = 1000
        locationRequest!!.fastestInterval = 5000
        locationRequest!!.smallestDisplacement = 1F
        locationRequest!!.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
    }

}