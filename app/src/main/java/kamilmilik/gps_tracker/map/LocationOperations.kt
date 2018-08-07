package kamilmilik.gps_tracker.map

import android.Manifest
import android.annotation.SuppressLint
import android.location.Location
import android.os.Looper
import android.util.Log
import com.google.android.gms.location.*
import com.google.firebase.auth.FirebaseAuth
import kamilmilik.gps_tracker.utils.LocationUtils
import kamilmilik.gps_tracker.utils.PermissionsUtils
import kamilmilik.gps_tracker.utils.Tools


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
        if (PermissionsUtils.checkPermissionGranted(mapActivity.getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)) {
            if (Tools.isGooglePlayServicesAvailable(mapActivity.getActivity())) {
                fusedLocationClient = LocationServices.getFusedLocationProviderClient(mapActivity.getActivity())
                locationRequest = LocationUtils.createLocationRequest()
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
                    //TODO sprawdzic czy moze to byc zakomentowane
//                    override fun onLocationAvailability(locationAvailability : LocationAvailability?) {
//                        super.onLocationAvailability(locationAvailability)
//                        Log.i(TAG,"onLocationAvailability() " + locationAvailability!!.isLocationAvailable + " stop service" )
//                                    if(!locationAvailability.isLocationAvailable){
//                                        fusedLocationClient.lastLocation.addOnSuccessListener { location : Location? ->
//                                            if(location != null){
//                                                mapActivity.addCurrentUserMarkerAndRemoveOld(location)
//                                            }
//                                        }
//                                    }
//                    }
                }
                fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
            }
        }
    }

}