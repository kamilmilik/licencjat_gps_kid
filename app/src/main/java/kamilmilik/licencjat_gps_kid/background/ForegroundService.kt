package kamilmilik.licencjat_gps_kid.background

import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.support.annotation.RequiresApi
import android.util.Log
import com.google.android.gms.location.*
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GetTokenResult
import kamilmilik.licencjat_gps_kid.R
import kamilmilik.licencjat_gps_kid.utils.Constants
import kamilmilik.licencjat_gps_kid.utils.OnDataAddedListener
import kamilmilik.licencjat_gps_kid.utils.RestFirebaseAsync
import kamilmilik.licencjat_gps_kid.utils.Tools
import kamilmilik.licencjat_gps_kid.models.TrackingModel
import java.util.concurrent.atomic.AtomicInteger

/**
 * Created by kamil on 23.04.2018.
 */
open class ForegroundService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var locationRequest: LocationRequest? = null

    private val TAG = ForegroundService::class.java.simpleName
    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    private var locationCallback : LocationCallback? = null

    private var notificationMethods : kamilmilik.licencjat_gps_kid.map.PolygonOperation.notification.Notification? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if(FirebaseAuth.getInstance().currentUser != null){// Prevent if user try log in in another device to run this functionality
            //Location action
            object : Thread() {
                @SuppressLint("MissingPermission")
                @RequiresApi(Build.VERSION_CODES.O)
                override fun run() {
                    Log.i(TAG, "dzialam")
                    val notificationIntent = Intent(this@ForegroundService, ForegroundService::class.java)
                    val pendingIntent = PendingIntent.getActivity(this@ForegroundService, 0, notificationIntent, 0)

                    val notification = Notification.Builder(this@ForegroundService)
                            .setContentTitle(getString(R.string.app_name))
                            .setContentText(getString(R.string.getlocationInformation))
                            .setSmallIcon(R.drawable.ic_launcher_background)
                            .setContentIntent(pendingIntent)
                            .build()

                    startForeground(Constants.NOTIFICATION_ID_GET_LOCATION, notification)
                    if(Tools.isGooglePlayServicesAvailable(this@ForegroundService)){
                        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this@ForegroundService)

                        createLocationRequest()
                        Log.i(TAG,"is internet? " + Tools.isInternetConnection(this@ForegroundService))
                    }
                    if(Tools.isGooglePlayServicesAvailable(this@ForegroundService)){
                        locationCallback = object : LocationCallback(){
                                    override fun onLocationResult(locationResult: LocationResult?) {
                                        Log.i(TAG,"onLocationResult() $locationResult")
                                        for (location in locationResult!!.locations) {
                                            addCurrentUserLocationToFirebase(location)
                                        }
                                    }

                                    override fun onLocationAvailability(locationAvailability : LocationAvailability?) {
                                        super.onLocationAvailability(locationAvailability)
                                        Log.i(TAG,"onLocationAvailability() " + locationAvailability!!.isLocationAvailable + " stop service" )
                                        if(!locationAvailability!!.isLocationAvailable){
                                            fusedLocationClient.lastLocation.addOnSuccessListener { location : Location? ->
                                                if(location != null){
                                                    addCurrentUserLocationToFirebase(location!!)
                                                }
                                            }
                                        }
                                    }
                                }
                        fusedLocationClient.requestLocationUpdates(locationRequest,
                                locationCallback, Looper.getMainLooper())
                    }else{
                        Log.i(TAG,"run() no google play services available")
                    }
                }
            }.start()

            //PolygonAction
            object : Thread() {
                override fun run() {
                    FirebaseApp.initializeApp(applicationContext)//I must called this first otherwise foreground/background service is not running since without it get nullPointerException
                    notificationMethods = kamilmilik.licencjat_gps_kid.map.PolygonOperation.notification.Notification(this@ForegroundService)
                    notificationMethods!!.notificationAction(true)
                }
            }.start()
        }

        return START_STICKY
    }
    private fun createLocationRequest() {
        Log.i(TAG, "createLocationRequest")
        locationRequest = LocationRequest.create()
        locationRequest!!.interval = 1000//1000 = 1 second
        locationRequest!!.fastestInterval = 5000
        locationRequest!!.smallestDisplacement = 1F
        locationRequest!!.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
    }

    private fun addCurrentUserLocationToFirebase(lastLocation: Location) {
        FirebaseApp.initializeApp(applicationContext)//I must called this first otherwise foreground/background service is not running since without it get nullPointerException
        var currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {//prevent if user click logout to not update locationOfUserWhoChangeIt
            Log.i(TAG, "addCurrentUserMarkerAndRemoveOld() current user: " + currentUser!!.uid + " location " + lastLocation.toString())
            object : Thread(){
                override fun run() {
                    Log.i(TAG,"addCurrentUserLocationToFirebase() is internet? " + Tools.isInternetConnection(this@ForegroundService))
                }
            }.start()
            var mUser = FirebaseAuth.getInstance().getCurrentUser();
            mUser?.getIdToken(true)?.addOnCompleteListener(object : OnCompleteListener<GetTokenResult> {
                override fun onComplete(task: Task<GetTokenResult>) {
                    if(task.isSuccessful){
                        var tokenId = task.result.token
                        RestFirebaseAsync(TrackingModel(tokenId!!,
                                currentUser!!.email!!,
                                lastLocation.latitude.toString(),
                                lastLocation.longitude.toString(),
                                currentUser!!.displayName!!,
                                System.currentTimeMillis()), object : OnDataAddedListener {
                            override fun onDataAdded() {
                                Log.i(TAG, "onComplete() position: $lastLocation saved to firebase database")
                                Log.i(TAG, "okey stop service")
                                stopForeground(true)
                                stopSelf()
                                fusedLocationClient.removeLocationUpdates(locationCallback)
                                notificationMethods?.removeValueEventListeners()
                            }
                        }).execute()
                    }
                }

            });
        }
    }

}