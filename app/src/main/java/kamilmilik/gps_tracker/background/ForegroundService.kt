package kamilmilik.gps_tracker.background

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.google.android.gms.location.*
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import kamilmilik.gps_tracker.R
import kamilmilik.gps_tracker.models.NotificationModel
import kamilmilik.gps_tracker.models.TrackingModel
import kamilmilik.gps_tracker.utils.*
import kamilmilik.gps_tracker.utils.Constants.NOTIFICATION_CHANNEL_FOREGROUND
import kamilmilik.gps_tracker.utils.Constants.NOTIFICATION_ID_GET_LOCATION
import kamilmilik.gps_tracker.utils.listeners.OnDataAddedListener


/**
 * Created by kamil on 23.04.2018.
 */
open class ForegroundService : Service() {

    private val TAG = ForegroundService::class.java.simpleName

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var locationRequest: LocationRequest? = null

    private var locationCallback: LocationCallback? = null

    private var pendingIntent: PendingIntent? = null

    private var notificationMethods: kamilmilik.gps_tracker.map.PolygonOperation.notification.Notification? = null

    private var synchronizeAction: Synchronize? = null

    override fun onBind(p0: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (FirebaseAuth.getInstance().currentUser != null) {
            notificationMethods = kamilmilik.gps_tracker.map.PolygonOperation.notification.Notification(this@ForegroundService)
            synchronizeAction = Synchronize(this)
            LogUtils(this).appendLog(TAG, "new Synchronize object : " + synchronizeAction?.allTaskDoneCounter?.get() + " " + synchronizeAction?.doOnlyOneTaskDoneCounter?.get() + " " + synchronizeAction?.howManyTimesActionRunConnectedUser?.get() + " " + synchronizeAction?.polygonActionCounter?.get())
            notificationMethods?.synchronizeAction = synchronizeAction
            startForegroundService()

            getCurrentLocationAction()
            polygonAction()
        }

        return START_STICKY
    }

    private fun getCurrentLocationAction() {
        object : Thread() {
            override fun run() {
                Log.i(TAG, "dzialam")
                if (Tools.isGooglePlayServicesAvailable(this@ForegroundService)) {
                    fusedLocationClient = LocationServices.getFusedLocationProviderClient(this@ForegroundService)
                    locationRequest = LocationUtils.createLocationRequest()
                    if (PermissionsUtils.checkApkVersion()) {
                        if (PermissionsUtils.checkPermissionGranted(this@ForegroundService, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
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
                LogUtils(this@ForegroundService).appendLog(TAG, "onLocationResult() $locationResult")
                for (location in locationResult!!.locations) {
                    val lastLocation = LocationUtils.getLocationFromSharedPref(this@ForegroundService)
                    LogUtils(this@ForegroundService).appendLog(TAG, "accuracy locationResult: ${location.accuracy} isBetterLocation? " + LocationUtils.isBetterLocation(this@ForegroundService, location, lastLocation))
                    if (LocationUtils.isBetterLocation(this@ForegroundService, location, lastLocation)) {
                        LogUtils(this@ForegroundService).appendLog(TAG, "add location in onLocationResult : ${location.longitude} ${location.latitude} accuracy: ${location.accuracy}")
                        addCurrentUserLocationToFirebase(location)
                    }
                }
            }

            override fun onLocationAvailability(locationAvailability: LocationAvailability?) {
                super.onLocationAvailability(locationAvailability)
                LogUtils(this@ForegroundService).appendLog(TAG, "onLocationAvailability() " + locationAvailability!!.isLocationAvailable + " stop service")
                if (!locationAvailability.isLocationAvailable) {
                    fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                        if (location != null) {
                            LogUtils(this@ForegroundService).appendLog(TAG, "add location in onLocationAvailability : ${location.longitude} ${location.latitude}")
                            addCurrentUserLocationToFirebase(location)
                        }
                    }
                }
            }
        }
        fusedLocationClient.requestLocationUpdates(locationRequest,
                locationCallback, Looper.getMainLooper())
    }

    private fun startForegroundService() {
        pendingIntent = NotificationUtils.createPendingIntent(this@ForegroundService, ForegroundService::class.java, false)
        val notificationModel = NotificationModel(getString(R.string.app_name), getString(R.string.getlocationInformation), NOTIFICATION_CHANNEL_FOREGROUND, NOTIFICATION_ID_GET_LOCATION, false)
        val notification = NotificationUtils.createNotification(this@ForegroundService, notificationModel)
                ?.setContentIntent(pendingIntent)
                ?.build()

        NotificationUtils.createNotificationChannel(this, notificationModel)

        startForeground(NOTIFICATION_ID_GET_LOCATION, notification)
    }

    //TODO z doOnlyOneTaskDoneCounter jest problem czasami rowny jest 18 lub wiecej nawet, sprawdzic dlaczego tak
    private fun addCurrentUserLocationToFirebase(lastLocation: Location) {
        FirebaseApp.initializeApp(applicationContext)
        LocationUtils.saveLocationToSharedPref(this@ForegroundService, lastLocation)
        val currentUser = FirebaseAuth.getInstance().currentUser
        LogUtils(this@ForegroundService).appendLog(TAG, "addCurrentUserMarkerAndRemoveOld() current user: " + currentUser?.uid + " location " + lastLocation.toString())
        currentUser?.getIdToken(true)?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                LogUtils(this@ForegroundService).appendLog(TAG, "addCurrentUserLocationToFirebase() task.isSuccessful")
                val tokenId = task.result.token
                RestFirebaseUtils.RestFirebaseAsync(TrackingModel(currentUser.uid,
                        currentUser.email!!,
                        lastLocation.latitude.toString(),
                        lastLocation.longitude.toString(),
                        currentUser.displayName!!), tokenId!!, object : OnDataAddedListener {
                    override fun onDataAdded() {
                        LogUtils(this@ForegroundService).appendLog(TAG, "onComplete() position: ${lastLocation.latitude} ${lastLocation.longitude} saved to firebase database ")
                        synchronizeAction!!.allTaskDoneCounter.incrementAndGet()
                        synchronizeAction!!.doOnlyOneTaskDoneCounter.incrementAndGet()
                        LogUtils(this@ForegroundService).appendLog(TAG, "onDataAdded() tutaj sie ma wykonac foregroundservice allTaskDoneCounter = " + synchronizeAction!!.allTaskDoneCounter + " doOnlyOneTaskDoneCounter " + synchronizeAction!!.doOnlyOneTaskDoneCounter)
                        fusedLocationClient.removeLocationUpdates(locationCallback)
                        fusedLocationClient.removeLocationUpdates(pendingIntent)
                        if (synchronizeAction!!.allTaskDoneCounter.compareAndSet(2, 0) || synchronizeAction!!.doOnlyOneTaskDoneCounter.compareAndSet(3, 0)) {
                            LogUtils(this@ForegroundService).appendLog(TAG, "onDataAdded() stop in foregroundService with allTaskDoneCounter = ${synchronizeAction!!.allTaskDoneCounter} and  doOnlyOneTaskDoneCounter = ${synchronizeAction!!.doOnlyOneTaskDoneCounter}")
                            finishServiceAction()
                        }
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

    fun finishServiceAction() {
        LogUtils(this).writeLogToServerAsync(object : OnDataAddedListener {
            override fun onDataAdded() {
                LogUtils(this@ForegroundService).appendLog(TAG, "finishServiceAction : " + synchronizeAction?.allTaskDoneCounter?.get() + " " + synchronizeAction?.doOnlyOneTaskDoneCounter?.get() + " " + synchronizeAction?.howManyTimesActionRunConnectedUser?.get() + " " + synchronizeAction?.polygonActionCounter?.get())
                LogUtils(this@ForegroundService).deleteLogFileAtTimeBetweenGivenHours(2, 4)
                stopForeground(true)
                stopSelf()
                notificationMethods?.removeValueEventListeners()
            }

        })
    }

}