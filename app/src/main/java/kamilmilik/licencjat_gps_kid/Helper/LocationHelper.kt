package kamilmilik.licencjat_gps_kid.Helper

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Bundle
import android.util.Log
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.Marker
import com.google.firebase.auth.FirebaseAuth


/**
 * Created by kamil on 24.02.2018.
 */
class LocationHelper(
        var context: Context,
        var permissionHelper: PermissionHelper,
        var locationFirebaseHelper: LocationFirebaseHelper):GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener{
    var TAG : String = LocationHelper::class.java.simpleName


    var mLocationRequest: LocationRequest? = null
    var mGoogleApiClient: GoogleApiClient? = null
    var mLastLocation: Location? = null
    var mCurrLocationMarker: Marker? = null


    @SuppressLint("MissingPermission")
    override fun onConnected(p0: Bundle?) {
        Log.i(TAG,"onConnected")
        createLocationRequest()
        if (permissionHelper!!.checkPermissionGranted()) {
            Log.i(TAG, "start request location updates ")
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this)
        }
    }

    override fun onConnectionSuspended(p0: Int) {
        Log.i(TAG,"onConnectionSuspended")
        mGoogleApiClient!!.connect()
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        Log.i(TAG,"onConnectionFailed: google maps" + p0.errorMessage)
    }
    override fun onLocationChanged(location: Location?) {
        if(FirebaseAuth.getInstance().currentUser != null){
            Log.i(TAG, "onLocationChanged")
            mLastLocation = location!!
            if (mCurrLocationMarker != null) {//prevent if user click logout to not update location
                mCurrLocationMarker!!.remove();
            }
            locationFirebaseHelper!!.addCurrentUserLocationToFirebase(location!!)
        }
    }

    fun buildGoogleApiClient() {
        synchronized(this){
            Log.i(TAG, "buildGoogleApiClient")
            mGoogleApiClient =  GoogleApiClient.Builder(context)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            mGoogleApiClient!!.connect();
        }
    }
    fun createLocationRequest() {
        Log.i(TAG,"createLocationRequest")
        mLocationRequest = LocationRequest()
        mLocationRequest!!.interval = 100
        mLocationRequest!!.fastestInterval = 100
        mLocationRequest!!.smallestDisplacement = 1F
        mLocationRequest!!.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }
}