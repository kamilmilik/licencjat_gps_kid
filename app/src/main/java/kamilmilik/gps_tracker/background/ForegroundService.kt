package kamilmilik.gps_tracker.background

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.IBinder
import android.os.Looper
import android.util.Log
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
import android.os.AsyncTask
import android.os.Handler
import kamilmilik.gps_tracker.utils.Constants.LOCATION_ONE_SECOND_DELAY
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Created by kamil on 23.04.2018.
 */
open class ForegroundService : Service()/*, LocationListener */ {

    private val TAG = ForegroundService::class.java.simpleName

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var locationRequest: LocationRequest? = null

    private var locationCallback: LocationCallback? = null

    private var notificationMethods: kamilmilik.gps_tracker.map.PolygonOperation.notification.Notification? = null

    private var synchronizeAction: Synchronize? = null

    private val isWaitingForOnLocationResult: AtomicBoolean = AtomicBoolean(false)

    override fun onBind(p0: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        FirebaseAuth.getInstance().currentUser?.let {
            notificationMethods = kamilmilik.gps_tracker.map.PolygonOperation.notification.Notification(this@ForegroundService)
            synchronizeAction = Synchronize(this)
            LogUtils(this).appendLog(TAG, "new Synchronize object : " + synchronizeAction?.allTaskDoneCounter?.get() + " " + synchronizeAction?.doOnlyOneTaskDoneCounter?.get() + " " + synchronizeAction?.howManyTimesActionRunConnectedUser?.get() + " " + synchronizeAction?.polygonActionCounter?.get())
            notificationMethods?.synchronizeAction = synchronizeAction
            startForegroundService()

            getCurrentLocationAction()
            polygonAction()
        }

        return START_NOT_STICKY
    }

    private fun startForegroundService() {
        val pendingIntent = NotificationUtils.createPendingIntent(this@ForegroundService, ForegroundService::class.java, false)
        val notificationModel = NotificationModel(getString(R.string.app_name), getString(R.string.getLocationInformation), NOTIFICATION_CHANNEL_FOREGROUND, NOTIFICATION_ID_GET_LOCATION, false)
        val notification = NotificationUtils.createNotification(this@ForegroundService, notificationModel)
                ?.setContentIntent(pendingIntent)
                ?.build()

        NotificationUtils.createNotificationChannel(this, notificationModel)

        startForeground(NOTIFICATION_ID_GET_LOCATION, notification)
    }

    private fun polygonAction() {
        object : Thread() {
            override fun run() {
                // Foreground service must initialize Firebase.
                FirebaseApp.initializeApp(applicationContext)
                notificationMethods?.notificationAction(true)
            }
        }.start()
    }

    private fun getCurrentLocationAction() {
        object : Thread() {
            override fun run() {
                Log.i(TAG, "dzialam")
                if (Tools.isGooglePlayServicesAvailable(this@ForegroundService)) {
                    fusedLocationClient = LocationServices.getFusedLocationProviderClient(this@ForegroundService)
                    locationRequest = LocationUtils.createLocationFastRequest(false)
                    if (PermissionsUtils.checkApkVersion()) {
                        if (PermissionsUtils.checkPermissionGranted(this@ForegroundService, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                            getCurrentLocation()
//                            getLocations()
                        }
                    } else {
                        getCurrentLocation()
//                        getLocations()
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
            override fun onLocationAvailability(locationAvailability: LocationAvailability?) {
                super.onLocationAvailability(locationAvailability)
                locationAvailability?.let {
                    LogUtils(this@ForegroundService).appendLog(TAG, "onLocationAvailability isWaitingForOnLocationResult ${isWaitingForOnLocationResult.get()}")
                    if (!isWaitingForOnLocationResult.get()) {
                        LogUtils(this@ForegroundService).appendLog(TAG, "isLocationAvailability? " + locationAvailability?.isLocationAvailable)
                        if (locationAvailability.isLocationAvailable) {
                            val lastLocation = LocationUtils.getLocationFromSharedPref(this@ForegroundService)
                            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                                if (location != null) {
                                    LogUtils(this@ForegroundService).appendLog(TAG, "add location in lastLocation : ${location.longitude} ${location.latitude} accuracy ${location.accuracy} isBetterLocation? ${LocationUtils.isBetterLocation(this@ForegroundService, location, lastLocation)}")
                                    if (LocationUtils.isBetterLocation(this@ForegroundService, location, lastLocation)) {
                                        LogUtils(this@ForegroundService).appendLog(TAG, "add location  : ${location.longitude} ${location.latitude} accuracy: ${location.accuracy}")
                                        addCurrentUserLocationToFirebase(location)
                                    } else {
                                        isWaitingForOnLocationResult.set(true)
                                        // After that stop service and location update.
                                        waitingForLocation()
                                        // prepareFinishService(true)
                                    }
                                }
                            }
                        } else {
                            prepareFinishService()
                        }
                    }
                }
            }

            override fun onLocationResult(locationResult: LocationResult?) {
                super.onLocationResult(locationResult)
                locationResult?.let {
                    LogUtils(this@ForegroundService).appendLog(TAG, "onLocationResult isWaitingForOnLocationResult ${isWaitingForOnLocationResult.get()}")
                    if (isWaitingForOnLocationResult.get()) {
                        val lastLocation = LocationUtils.getLocationFromSharedPref(this@ForegroundService)
                        for (location in locationResult.locations) {
                            LogUtils(this@ForegroundService).appendLog(TAG, "add location in onLocationResult : ${location.longitude} ${location.latitude} accuracy ${location.accuracy} isBetterLocation? ${LocationUtils.isBetterLocation(this@ForegroundService, location, lastLocation)}")
                            if (LocationUtils.isBetterLocation(this@ForegroundService, location, lastLocation)) {
                                LogUtils(this@ForegroundService).appendLog(TAG, "locationResult loop add location  : ${location.longitude} ${location.latitude} accuracy: ${location.accuracy}")
                                addCurrentUserLocationToFirebase(location)
                            }
                        }
                    }
                }
            }
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    private fun addCurrentUserLocationToFirebase(lastLocation: Location) {
        FirebaseApp.initializeApp(applicationContext)
        LocationUtils.saveLocationToSharedPref(this@ForegroundService, lastLocation)
        val currentUser = FirebaseAuth.getInstance().currentUser
        LogUtils(this@ForegroundService).appendLog(TAG, "addCurrentUserMarkerAndRemoveOld() current user: " + currentUser?.uid + " location " + lastLocation.toString())
        currentUser?.getIdToken(true)?.addOnCompleteListener { task ->
            // This token is only for rest. This is NOT token for firebase messaging!
            if (task.isSuccessful) {
                LogUtils(this@ForegroundService).appendLog(TAG, "addCurrentUserLocationToFirebase() task.isSuccessful")
                val tokenId = task.result.token
                ObjectsUtils.safeLetRestFirebase(currentUser, lastLocation, tokenId) { uid, email, name, latitude, longitude, token ->
                    RestFirebaseUtils.RestFirebaseAsync(TrackingModel(uid, email, latitude, longitude, name), token, object : OnDataAddedListener {
                        override fun onDataAdded() {
                            LogUtils(this@ForegroundService).appendLog(TAG, "onComplete() position: ${lastLocation.latitude} ${lastLocation.longitude} saved to firebase database ")
                            Log.i(TAG, "onDataAdded() isWaitingForOnLocationResult ${isWaitingForOnLocationResult.get()}")
                            if (!isWaitingForOnLocationResult.get()) {
                                Log.i(TAG, "onDataAdded() preparefinishService")
                                prepareFinishService()
                            }
                        }
                    }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
                }
            }
        }
    }

    fun prepareFinishService() {
        Log.i(TAG, "prepareFinishService()")
        synchronizeAction?.allTaskDoneCounter?.incrementAndGet()
        synchronizeAction?.doOnlyOneTaskDoneCounter?.incrementAndGet()
        LogUtils(this@ForegroundService).appendLog(TAG, " tutaj sie ma wykonac foregroundservice allTaskDoneCounter = " + synchronizeAction?.allTaskDoneCounter + " doOnlyOneTaskDoneCounter " + synchronizeAction?.doOnlyOneTaskDoneCounter)
        fusedLocationClient.removeLocationUpdates(locationCallback)
//        fusedLocationClient.removeLocationUpdates(pendingIntent)
        synchronizeAction?.let { synchronizeAction ->
            if (synchronizeAction.allTaskDoneCounter.compareAndSet(2, 0) || synchronizeAction.doOnlyOneTaskDoneCounter.compareAndSet(3, 0)) {
                LogUtils(this@ForegroundService).appendLog(TAG, "onDataAdded() stop in foregroundService with allTaskDoneCounter = ${synchronizeAction?.allTaskDoneCounter} and  doOnlyOneTaskDoneCounter = ${synchronizeAction?.doOnlyOneTaskDoneCounter}")
                finishServiceAction()
            }
        }
    }

    fun finishServiceAction() {
        LogUtils(this).deleteTooBigfile()
        LogUtils(this).writeLogToServerAsync(object : OnDataAddedListener {
            override fun onDataAdded() {
                LogUtils(this@ForegroundService).appendLog(TAG, "finishServiceAction : " + synchronizeAction?.allTaskDoneCounter?.get() + " " + synchronizeAction?.doOnlyOneTaskDoneCounter?.get() + " " + synchronizeAction?.howManyTimesActionRunConnectedUser?.get() + " " + synchronizeAction?.polygonActionCounter?.get())
//                LogUtils(this@ForegroundService).deleteLogFileAtTimeBetweenGivenHours(5, 7)
                stopForeground(true)
                stopSelf()
                notificationMethods?.removeValueEventListeners()
            }

        })
    }

    fun waitingForLocation() {
        Log.i(TAG, "waitingForLocation() start")
        val handler = Handler()
        handler.postDelayed({
            LogUtils(this).appendLog(TAG, "waitingForLocation() end waiting")
            fusedLocationClient.removeLocationUpdates(locationCallback) // jakos to poprawic bo dwa razy bedzie removeLocationUpdates wykonywane
            isWaitingForOnLocationResult.set(false)
            prepareFinishService()
        }, LOCATION_ONE_SECOND_DELAY * 60 * 1)
    }

}