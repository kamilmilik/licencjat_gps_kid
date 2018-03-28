package kamilmilik.licencjat_gps_kid.Utils

import android.Manifest
import android.app.*
import android.content.Intent
import android.os.IBinder
import android.os.SystemClock
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.util.Log
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.Marker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kamilmilik.licencjat_gps_kid.Constants
import kamilmilik.licencjat_gps_kid.Helper.Notification
import kamilmilik.licencjat_gps_kid.R
import kamilmilik.licencjat_gps_kid.models.TrackingModel
import kamilmilik.licencjat_gps_kid.DummyActivity




/**
 * Created by kamil on 18.03.2018.
 */
class PolygonAndLocationService : Service,
        GoogleApiClient.ConnectionCallbacks,
GoogleApiClient.OnConnectionFailedListener,
com.google.android.gms.location.LocationListener {

    private val TAG = PolygonAndLocationService::class.java.simpleName

    private var notificationMethods: Notification? = null
    constructor() : super(){}

    override fun onCreate() {
        notificationMethods = Notification(this@PolygonAndLocationService)
        super.onCreate()
        Log.i(TAG,"onCreate() - > PolygonAndLocationService")
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.i(TAG, "PolygonAndLocationService started")
        val thread = object : Thread() {
            override fun run() {
                createNotificationChannelForApi26()
                intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
                val pendingIntent = PendingIntent.getActivity(this@PolygonAndLocationService, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
                Log.i(TAG, "intent in service before notif: " + intent + " " + intent.action)
                val notification = android.app.Notification.Builder(this@PolygonAndLocationService)
                        .setContentTitle("title")
                        .setContentText("message")
                        .setSmallIcon(R.drawable.abc_cab_background_internal_bg)
                        .setContentIntent(pendingIntent)
                        .setTicker("ticker")
                        .build()
                startForeground(2, notification)
                buildGoogleApiClient()
                notificationMethods!!.notificationAction()

            }
        }
        thread.start()

        return START_STICKY
    }
    var mLocationRequest: LocationRequest? = null
    var mGoogleApiClient: GoogleApiClient? = null
    var mLastLocation: Location? = null
    var mCurrLocationMarker: Marker? = null

    var mRequestLocationUpdatesPendingIntent : PendingIntent? = null
    fun buildGoogleApiClient() {
        synchronized(this) {
            Log.i(TAG, "buildGoogleApiClient")
            mGoogleApiClient = GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build()
            mGoogleApiClient!!.connect()
        }
    }

    private fun createLocationRequest() {
        Log.i(TAG, "createLocationRequest")
        mLocationRequest = LocationRequest()
        mLocationRequest!!.interval = 100
        mLocationRequest!!.fastestInterval = 100
        mLocationRequest!!.smallestDisplacement = 1F
        mLocationRequest!!.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }
    override fun onConnected(p0: Bundle?) {
        Log.i(TAG, "onConnected")
        createLocationRequest()
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
            Log.i(TAG, "start request locationOfUserWhoChangeIt updates ")
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this)
            // request locationOfUserWhoChangeIt updates
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                    mLocationRequest,
                    mRequestLocationUpdatesPendingIntent)

    }

    override fun onConnectionSuspended(p0: Int) {
        Log.i(TAG, "onConnectionSuspended")
        mGoogleApiClient!!.connect()
    }
    override fun onConnectionFailed(p0: ConnectionResult) {
        Log.i(TAG, "onConnectionFailed: google maps" + p0.errorMessage)
    }

    override fun onLocationChanged(location: Location?) {
        if (FirebaseAuth.getInstance().currentUser != null) {
            Log.i(TAG, "onLocationChanged")
            mLastLocation = location!!
            addCurrentUserLocationToFirebase(mLastLocation!!)
            //TODO czy to cos robi?
            if (mCurrLocationMarker != null) {//prevent if user click logout to not update locationOfUserWhoChangeIt
                mCurrLocationMarker!!.remove();
            }
        }
    }
    private fun addCurrentUserLocationToFirebase(lastLocation: Location) {
        Log.i(TAG, "addCurrentUserMarkerAndRemoveOld()")
        var locations = FirebaseDatabase.getInstance().getReference("Locations")
        var currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {//prevent if user click logout to not update locationOfUserWhoChangeIt
            locations.child(currentUser!!.uid)
                    .setValue(TrackingModel(currentUser.uid,
                            currentUser!!.email!!,
                            lastLocation.latitude.toString(),
                            lastLocation.longitude.toString()))
        }
    }


    //prevent kill background service in kitkat android
    override fun onTaskRemoved(rootIntent: Intent) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1 && Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2) {
            val intent = Intent(this, DummyActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    private fun createNotificationChannelForApi26(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel, but only on API 26+ because
            // the NotificationChannel class is new and not in the support library
            val name = "My channel name"
            val description = "Description channel"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(Constants.CHANNEL_ID, name, importance)
            channel.description = description
            // Register the channel with the system
            val notificationManager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    override fun onDestroy() {
        Log.i(TAG,"Problem: service destroy it couldn't happen")
        super.onDestroy()
    }

    override fun onBind(arg0: Intent): IBinder? {
        return null
    }


}