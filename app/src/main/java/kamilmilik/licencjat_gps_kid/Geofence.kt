package kamilmilik.licencjat_gps_kid

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.util.Log
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.ResultCallback
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import kamilmilik.licencjat_gps_kid.Helper.LocationFirebaseHelper
import kamilmilik.licencjat_gps_kid.Helper.PermissionHelper
import kamilmilik.licencjat_gps_kid.Utils.GeofenceService

/**
 * Created by kamil on 01.03.2018.
 */
class Geofence(var context : Context, var mGoogleApiClient: GoogleApiClient, var permissionHelper: PermissionHelper, var locationFirebaseHelper: LocationFirebaseHelper){
    private var  TAG = Geofence::class.java.simpleName
    private val GEOFENCE_REQ_ID = "My Geofence"
    private val GEOFENCE_RADIUS = 500.0f // in meters

    fun startGeofence() {
        Log.i(TAG, "startGeofence()")
        val geofence = createGeofence(LatLng(65.97056135, -18.53131691), GEOFENCE_RADIUS)
        val geofenceRequest = createGeofenceRequest(geofence)
        addGeofence(geofenceRequest)
    }
    // Create a Geofence
    private fun createGeofence(latLng: LatLng, radius: Float): Geofence {
        Log.d(TAG, "createGeofence")
        return Geofence.Builder()
                .setRequestId(GEOFENCE_REQ_ID)
                .setCircularRegion(latLng.latitude, latLng.longitude, radius)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setNotificationResponsiveness(1000)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
                .build()
    }

    // Create a Geofence Request
    private fun createGeofenceRequest(geofence: Geofence): GeofencingRequest {
        Log.d(TAG, "createGeofenceRequest")
        return GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence(geofence)
                .build()
    }

    private val geoFencePendingIntent: PendingIntent? = null
    private val GEOFENCE_REQ_CODE = 0
    private fun createGeofencePendingIntent(): PendingIntent {
        Log.d(TAG, "createGeofencePendingIntent")
        if (geoFencePendingIntent != null)
            return geoFencePendingIntent

        val intent = Intent(context, GeofenceService::class.java)
        return PendingIntent.getService(
                context, GEOFENCE_REQ_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    // Add the created GeofenceRequest to the device's monitoring list
    @SuppressLint("MissingPermission")
    private fun addGeofence(request: GeofencingRequest) {
        Log.d(TAG, "addGeofence")
        if(!mGoogleApiClient!!.isConnected){
            Log.i(TAG,"GoogleApiClient Is not connceted")
        }else{
            if (permissionHelper.checkPermissionGranted())
                LocationServices.GeofencingApi.addGeofences(
                        mGoogleApiClient,
                        request,
                        createGeofencePendingIntent()
                ).setResultCallback { status ->
                    if(status.isSuccess){
                        Log.i(TAG, "Successfully added geofence")
                        drawGeofence(LatLng(65.97056135, -18.53131691))
                    }else{
                        Log.i(TAG,"Failed to add geofence " + status.status)
                    }
                }
        }
    }

    private var geoFenceLimits: Circle? = null
    private fun drawGeofence(position : LatLng) {
        Log.d(TAG, "drawGeofence()")
        if (geoFenceLimits != null)
            geoFenceLimits!!.remove()

        val circleOptions = CircleOptions()
                .center(position)
                .strokeColor(Color.argb(50, 70, 70, 70))
                .fillColor(Color.argb(100, 150, 150, 150))
                .radius(GEOFENCE_RADIUS.toDouble())
        geoFenceLimits = locationFirebaseHelper.mGoogleMap.addCircle(circleOptions)
    }
    private fun stopGeofenceMonitoring(){
        Log.i(TAG,"StopGeofenceMonitoring")
        var geofenceIds : ArrayList<String> = ArrayList()
        geofenceIds.add(GEOFENCE_REQ_ID)
        LocationServices.GeofencingApi.removeGeofences(mGoogleApiClient,geofenceIds)
    }
}