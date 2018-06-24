package kamilmilik.licencjat_gps_kid.utils

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import android.util.Log
import android.widget.RelativeLayout
import com.google.android.gms.location.*
import com.google.firebase.auth.FirebaseAuth
import kamilmilik.licencjat_gps_kid.map.RecyclerViewAction
import kamilmilik.licencjat_gps_kid.map.LocationFirebaseMarkerAction
import kamilmilik.licencjat_gps_kid.map.MapActivity


/**
 * Created by kamil on 24.02.2018.
 */
class LocationOperations(private var mapActivity: MapActivity) {

    var TAG: String = LocationOperations::class.java.simpleName

    lateinit var fusedLocationClient: FusedLocationProviderClient
    lateinit var locationCallback: LocationCallback
    var locationRequest: LocationRequest? = null
    var lastLocation: Location? = null

    @SuppressLint("MissingPermission")
    fun getLocation() {
        if (Tools.checkPermissionGranted(mapActivity.getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION)) {
            if (Tools.isGooglePlayServicesAvailable(mapActivity.getActivity())) {
                fusedLocationClient = LocationServices.getFusedLocationProviderClient(mapActivity.getActivity())
                createLocationRequest()
                locationCallback = object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult?) {
                        for (location in locationResult!!.locations) {
                            Log.i(TAG,"onLocationResult() w callbacku")
                            if (FirebaseAuth.getInstance().currentUser != null) {
                                lastLocation = location!!

                                mapActivity.addCurrentUserMarkerAndRemoveOld(location)
                            }
                        }
                    }
                    override fun onLocationAvailability(locationAvailability : LocationAvailability?) {
                        super.onLocationAvailability(locationAvailability)
                        Log.i(TAG,"onLocationAvailability() " + locationAvailability!!.isLocationAvailable + " stop service" )
                                    if(!locationAvailability.isLocationAvailable){
                                        fusedLocationClient.lastLocation.addOnSuccessListener { location : Location? ->
                                            //TODO przy pierwszej instalacji apki location jest null caly czzas nawet jak zmieniam polozenie(mozliwe ze tylko w emulatorze) sprawdzic to
                                            if(location != null){
                                                mapActivity.addCurrentUserMarkerAndRemoveOld(location)
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
        locationRequest = LocationRequest.create()
        locationRequest!!.interval = Constants.LOCATION_INTERVAL
        locationRequest!!.fastestInterval = Constants.LOCATION_FASTEST_INTERVAL
        locationRequest!!.smallestDisplacement = Constants.LOCATION_SMALLEST_DISPLACEMENT
        locationRequest!!.priority = Constants.LOCATION_PRIORITY
    }

}