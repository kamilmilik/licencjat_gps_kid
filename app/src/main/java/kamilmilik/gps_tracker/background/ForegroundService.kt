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
                FirebaseApp.initializeApp(applicationContext)
                notificationMethods?.notificationAction(true)
            }
        }.start()
    }

    private fun getCurrentLocationAction() {
        object : Thread() {
            override fun run() {
                if (Tools.isGooglePlayServicesAvailable(this@ForegroundService)) {
                    fusedLocationClient = LocationServices.getFusedLocationProviderClient(this@ForegroundService)
                    locationRequest = LocationUtils.createLocationFastRequest(false)
                    if (PermissionsUtils.checkApkVersion()) {
                        if (PermissionsUtils.checkPermissionGranted(this@ForegroundService, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                            getCurrentLocation()
                        }
                    } else {
                        getCurrentLocation()
                    }
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
                    if (!isWaitingForOnLocationResult.get()) {
                        if (locationAvailability.isLocationAvailable) {
                            val lastLocation = LocationUtils.getLocationFromSharedPref(this@ForegroundService)
                            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                                if (location != null) {
                                    if (LocationUtils.isBetterLocation(location, lastLocation)) {
                                        addCurrentUserLocationToFirebase(location)
                                    } else {
                                        isWaitingForOnLocationResult.set(true)
                                        // After that stop service and location update.
                                        waitingForLocation()
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
                    if (isWaitingForOnLocationResult.get()) {
                        val lastLocation = LocationUtils.getLocationFromSharedPref(this@ForegroundService)
                        for (location in locationResult.locations) {
                            if (LocationUtils.isBetterLocation(location, lastLocation)) {
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
        currentUser?.getIdToken(true)?.addOnCompleteListener { task ->
            // This token is only for rest. This is NOT token for firebase messaging!
            if (task.isSuccessful) {
                val tokenId = task.result.token
                ObjectsUtils.safeLetRestFirebase(currentUser, lastLocation, tokenId) { uid, email, name, latitude, longitude, token ->
                    RestFirebaseUtils.RestFirebaseAsync(TrackingModel(uid, email, latitude, longitude, name), token, object : OnDataAddedListener {
                        override fun onDataAdded() {
                            if (!isWaitingForOnLocationResult.get()) {
                                prepareFinishService()
                            }
                        }
                    }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
                }
            }
        }
    }

    fun prepareFinishService() {
        synchronizeAction?.allTaskDoneCounter?.incrementAndGet()
        synchronizeAction?.doOnlyOneTaskDoneCounter?.incrementAndGet()
        fusedLocationClient.removeLocationUpdates(locationCallback)
        synchronizeAction?.let { synchronizeAction ->
            if (synchronizeAction.allTaskDoneCounter.compareAndSet(2, 0) || synchronizeAction.doOnlyOneTaskDoneCounter.compareAndSet(3, 0)) {
                finishServiceAction()
            }
        }
    }

    fun finishServiceAction() {
        stopForeground(true)
        stopSelf()
        notificationMethods?.removeValueEventListeners()
    }

    fun waitingForLocation() {
        val handler = Handler()
        handler.postDelayed({
            fusedLocationClient.removeLocationUpdates(locationCallback)
            isWaitingForOnLocationResult.set(false)
            prepareFinishService()
        }, LOCATION_ONE_SECOND_DELAY * 60 * 1)
    }

}