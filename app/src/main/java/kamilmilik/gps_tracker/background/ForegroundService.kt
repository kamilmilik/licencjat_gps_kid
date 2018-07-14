package kamilmilik.gps_tracker.background

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.IBinder
import android.os.Looper
import android.support.v4.app.NotificationCompat
import android.util.Log
import com.google.android.gms.location.*
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import kamilmilik.gps_tracker.R
import kamilmilik.gps_tracker.models.TrackingModel
import kamilmilik.gps_tracker.utils.*

/**
 * Created by kamil on 23.04.2018.
 */
open class ForegroundService : Service() {

    private val TAG = ForegroundService::class.java.simpleName

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var locationRequest: LocationRequest? = null

    private var locationCallback: LocationCallback? = null

    private var notificationMethods: kamilmilik.gps_tracker.map.PolygonOperation.notification.Notification? = null

    private var synchronizeAction : Synchronize? = null

    override fun onBind(p0: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (FirebaseAuth.getInstance().currentUser != null) {
            notificationMethods = kamilmilik.gps_tracker.map.PolygonOperation.notification.Notification(this@ForegroundService)
            synchronizeAction = Synchronize(this)
            notificationMethods?.synchronizeAction = synchronizeAction
            startForegroundService()

            getCurrentLocationAction()
            polygonAction()
        }

        return START_STICKY
    }

    private fun createLocationRequest() {
        locationRequest = LocationRequest.create()
        locationRequest!!.interval = Constants.LOCATION_INTERVAL
        locationRequest!!.fastestInterval = Constants.LOCATION_FASTEST_INTERVAL
        locationRequest!!.smallestDisplacement = Constants.LOCATION_SMALLEST_DISPLACEMENT
        locationRequest!!.priority = Constants.LOCATION_PRIORITY
    }

    private fun getCurrentLocationAction() {
        object : Thread() {
            override fun run() {
                Log.i(TAG, "dzialam")
                if (Tools.isGooglePlayServicesAvailable(this@ForegroundService)) {
                    fusedLocationClient = LocationServices.getFusedLocationProviderClient(this@ForegroundService)
                    createLocationRequest()
                }
                if (Tools.isGooglePlayServicesAvailable(this@ForegroundService)) {
                    if (Tools.checkApkVersion()) {
                        if (Tools.checkPermissionGranted(this@ForegroundService, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                            getCurrentLocation()
                        }
                    } else {
                        getCurrentLocation()
                    }
                } else {
                    Log.i(TAG, "run() no google play services available")
                }
            }
        }.start()
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                Log.i(TAG, "onLocationResult() $locationResult")
                for (location in locationResult!!.locations) {
                    addCurrentUserLocationToFirebase(location)
                }
            }

            override fun onLocationAvailability(locationAvailability: LocationAvailability?) {
                super.onLocationAvailability(locationAvailability)
                Log.i(TAG, "onLocationAvailability() " + locationAvailability!!.isLocationAvailable + " stop service")
                if (!locationAvailability!!.isLocationAvailable) {
                    fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                        if (location != null) {
                            addCurrentUserLocationToFirebase(location!!)
                        }
                    }
                }
            }
        }
        fusedLocationClient.requestLocationUpdates(locationRequest,
                locationCallback, Looper.getMainLooper())
    }

    private fun startForegroundService() {
        val notificationIntent = Intent(this@ForegroundService, ForegroundService::class.java)
        val pendingIntent = PendingIntent.getActivity(this@ForegroundService, 0, notificationIntent, 0)

        val notification = NotificationCompat.Builder(this@ForegroundService, Constants.NOTIFICATION_CHANNEL_FOREGROUND)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.getlocationInformation))
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentIntent(pendingIntent)
                .build()

        startForeground(Constants.NOTIFICATION_ID_GET_LOCATION, notification)
    }

    private fun addCurrentUserLocationToFirebase(lastLocation: Location) {
        FirebaseApp.initializeApp(applicationContext)
        val currentUser = FirebaseAuth.getInstance().currentUser
        Log.i(TAG, "addCurrentUserMarkerAndRemoveOld() current user: " + currentUser?.uid + " location " + lastLocation.toString())
        currentUser?.getIdToken(true)?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val tokenId = task.result.token
                RestFirebaseAsync(TrackingModel(currentUser.uid,
                        currentUser.email!!,
                        lastLocation.latitude.toString(),
                        lastLocation.longitude.toString(),
                        currentUser.displayName!!), tokenId!!, object : OnDataAddedListener {
                    override fun onDataAdded() {
                        Log.i(TAG, "onComplete() position: $lastLocation saved to firebase database")
                        synchronizeAction!!.allTaskDoneCounter.incrementAndGet()
                        synchronizeAction!!.doOnlyOneTaskDoneCounter.incrementAndGet()
                        Log.i(TAG,"onDataAdded() tutaj sie ma wykonac foregroundservice allTaskDoneCounter = " + synchronizeAction!!.allTaskDoneCounter + " doOnlyOneTaskDoneCounter " + synchronizeAction!!.doOnlyOneTaskDoneCounter)
                        if(synchronizeAction!!.allTaskDoneCounter.compareAndSet(2,0) || synchronizeAction!!.doOnlyOneTaskDoneCounter.compareAndSet(3, 0)){
                            Log.i(TAG,"onDataAdded() stop in foregroundService with allTaskDoneCounter = ${synchronizeAction!!.allTaskDoneCounter} and  doOnlyOneTaskDoneCounter = ${synchronizeAction!!.doOnlyOneTaskDoneCounter}")
                            finishServiceAction()
                        }
                            fusedLocationClient.removeLocationUpdates(locationCallback)

                    }
                }).execute()
            }
        }
    }

    private fun polygonAction() {
        object : Thread() {
            override fun run() {
                FirebaseApp.initializeApp(applicationContext)//I must called this first otherwise foreground/background service is not running since without it get nullPointerException
                notificationMethods!!.notificationAction(true)
            }
        }.start()
    }

    fun finishServiceAction(){
       stopForeground(true)
        stopSelf()
        notificationMethods?.removeValueEventListeners()
    }

}