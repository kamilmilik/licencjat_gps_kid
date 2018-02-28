package kamilmilik.licencjat_gps_kid.Helper

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.util.Log
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import kamilmilik.licencjat_gps_kid.Utils.OnItemClickListener
import kamilmilik.licencjat_gps_kid.Utils.RecyclerViewAdapter
import kamilmilik.licencjat_gps_kid.models.User


/**
 * Created by kamil on 24.02.2018.
 */
class LocationHelper(
        var context: Context,
        var permissionHelper: PermissionHelper,
        var mGoogleMap : GoogleMap,
        var locationsFirebaseHelper: LocationsFirebaseHelper):GoogleApiClient.ConnectionCallbacks,
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
    var currentMarker: Marker? = null

    override fun onLocationChanged(location: Location?) {
        Log.i(TAG, "onLocationChanged")
        mLastLocation = location!!
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker!!.remove();
        }
        locationsFirebaseHelper!!.addCurrentUserLocationToFirebase(location!!)

//        var finderUserConnectionHelper  = FinderUserConnectionHelper(context, listener, valueSet, adapter, recyclerView, locationsFirebaseHelper!!,location)
//        finderUserConnectionHelper.listenerForConnectionsUserChangeinFirebaseAndUpdateRecyclerView()
//
//        if(currentMarker!=null){
//            currentMarker!!.remove()
//        }
//        var currentUser = FirebaseAuth.getInstance().currentUser
//        currentMarker = mGoogleMap!!.addMarker(MarkerOptions()
//                .position(LatLng(location.latitude!!, location.longitude!!))
//                .title(currentUser!!.email))
        //locationsFirebaseHelper!!.loadLocationsFromDatabaseForCurrentUser("lurFM7tblDTaxNqIbaCnF9Dnv8k1", location!!.latitude, location!!.longitude)
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