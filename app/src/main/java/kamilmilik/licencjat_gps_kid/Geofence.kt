package kamilmilik.licencjat_gps_kid

import android.annotation.SuppressLint
import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import kamilmilik.licencjat_gps_kid.Helper.LocationOperation.LocationFirebaseHelper
import kamilmilik.licencjat_gps_kid.Helper.PermissionHelper
import kamilmilik.licencjat_gps_kid.Utils.GeofenceService
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import java.lang.Exception


/**
 * Created by kamil on 01.03.2018.
 */
class Geofence(var context: Context, var permissionHelper: PermissionHelper, var locationFirebaseHelper: LocationFirebaseHelper) {
    private var TAG = Geofence::class.java.simpleName
    private val GEOFENCE_REQ_ID = "My Geofence"
    private val GEOFENCE_RADIUS = 500.0f // in meters

    private var mGeofencingClient: GeofencingClient? = null
    private var mGeofenceList: ArrayList<Geofence>? = null

    fun startGeofence() {
        Log.i(TAG, "startGeofence()")
        createGeofence(LatLng(65.97056135, -18.53131691), GEOFENCE_RADIUS)
        getGeofencingRequest()
        addGeofence()
    }

    private fun createGeofence(latLng: LatLng, radius: Float) {
        mGeofencingClient = LocationServices.getGeofencingClient(context)
        mGeofenceList = ArrayList()
        mGeofenceList!!.add(Geofence.Builder()
                .setRequestId(GEOFENCE_REQ_ID)
                .setCircularRegion(latLng.latitude, latLng.longitude, radius)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
                .build())
    }

    private fun getGeofencingRequest(): GeofencingRequest {
        val builder = GeofencingRequest.Builder()
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
        builder.addGeofences(mGeofenceList)
        return builder.build()
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

    @SuppressLint("MissingPermission")
    private fun addGeofence() {
        if (permissionHelper.checkPermissionGranted()) {
            mGeofencingClient!!.addGeofences(getGeofencingRequest(), createGeofencePendingIntent())
                    .addOnSuccessListener(context as Activity, object : OnSuccessListener<Void> {
                        override fun onSuccess(void: Void?) {
                            Log.i(TAG, "Successfully added geofence")
                            drawGeofence(LatLng(65.97056135, -18.53131691))
                        }

                    })
                    .addOnFailureListener(context as Activity, object : OnFailureListener {
                        override fun onFailure(e: Exception) {
                            Log.i(TAG, "Failed to add geofence " + e.toString())
                        }

                    })

        }
    }

    private var geoFenceLimits: Circle? = null
    private fun drawGeofence(position: LatLng) {
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
}