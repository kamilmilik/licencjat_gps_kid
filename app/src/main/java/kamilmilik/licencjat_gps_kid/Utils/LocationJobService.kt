package kamilmilik.licencjat_gps_kid.Utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.support.v4.app.ActivityCompat
import android.util.Log
import com.firebase.jobdispatcher.JobParameters
import com.firebase.jobdispatcher.JobService
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kamilmilik.licencjat_gps_kid.models.TrackingModel
import android.os.PowerManager.WakeLock
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.*
import com.google.firebase.FirebaseApp
import kamilmilik.licencjat_gps_kid.ILocationJobDispatcher
import kamilmilik.licencjat_gps_kid.LocationUpdateCallback
import kamilmilik.licencjat_gps_kid.TestLocationForeground
import kamilmilik.licencjat_gps_kid.models.User


/**
 * Created by kamil on 02.04.2018.
 */
class LocationJobService : JobService(),
        ILocationJobDispatcher{
    private val TAG = LocationJobService::class.java.simpleName

    private var locationRequest: LocationRequest? = null
    private lateinit var locationUpdateCallback : LocationUpdateCallback
    private var job : JobParameters? = null

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate() {
        Log.i(TAG,"onCreate()")
        super.onCreate()
        if(Tools.isGooglePlayServicesAvailable(this)){
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

            createLocationRequest()
            locationUpdateCallback = LocationUpdateCallback(this)
        }

    }
    var wakeLock : WakeLock? = null
    @SuppressLint("MissingPermission")
    override fun onStartJob(job: JobParameters?): Boolean {
        Log.i(TAG,"onStartJob()")
        this.job = job

        if(Tools.isGooglePlayServicesAvailable(this)){
            fusedLocationClient.requestLocationUpdates(locationRequest,
                    object : LocationCallback(){
                        override fun onLocationResult(locationResult: LocationResult?) {
                            Log.i(TAG,"onLocationResult() $locationResult")
                            for (location in locationResult!!.locations) {
                                addCurrentUserLocationToFirebase(location, this)
                            }
                        }
                    }
                    , Looper.myLooper())
        }

//        var thread = object : Thread(){
//            override fun run() {
//                Log.i(TAG,"run()")
//                //getLastLocation()
//
//                //getLocation()
////                var intent = Intent(applicationContext, PolygonAndLocationService::class.java)
////                applicationContext.startService(intent)
//            }
//        }
//        thread.start()
        return false // true if use separate threat like asynctask
    }
    override fun onStopJob(job: JobParameters?): Boolean {
        Log.i("TAG", "onStopJob");
        /* true means, we're not done, please reschedule */
        return false;
    }

//    @SuppressLint("MissingPermission")
//    private fun getLastLocation(){// relies on other apps having already made location requests
//        fusedLocationClient.lastLocation
//                .addOnSuccessListener { location : Location? ->
//                    if(location != null){
//                        addCurrentUserLocationToFirebase(location)
//                    }
//                }
//    }

    private fun createLocationRequest() {
        Log.i(TAG, "createLocationRequest")
        locationRequest = LocationRequest.create()
        locationRequest!!.interval = 1000//1000 = 1 second
        locationRequest!!.fastestInterval = 5000
        locationRequest!!.smallestDisplacement = 1F
        locationRequest!!.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
    }


    private fun addCurrentUserLocationToFirebase(lastLocation: Location, locationCallback: LocationCallback) {
        val intent = Intent(this, ForegroundOnTaskRemovedActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        FirebaseApp.initializeApp(applicationContext)//I must called this first otherwise foreground/background service is not running since without it get nullPointerException
        var locations = FirebaseDatabase.getInstance().getReference("Locations")
        var currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {//prevent if user click logout to not update locationOfUserWhoChangeIt
            Log.i(TAG, "addCurrentUserMarkerAndRemoveOld() current user: " + currentUser!!.uid + " location " + lastLocation.toString())
            locations.child(currentUser!!.uid)
                    .setValue(TrackingModel(currentUser.uid,
                            currentUser!!.email!!,
                            lastLocation.latitude.toString(),
                            lastLocation.longitude.toString(),
                            currentUser!!.displayName!!,
                            System.currentTimeMillis()), object : DatabaseReference.CompletionListener {
                        override fun onComplete(error: DatabaseError?, reference: DatabaseReference?) {
                            Log.i(TAG, "onComplete()")
                            if (error == null) {
                                Log.i(TAG, "onComplete() position: $lastLocation saved to firebase database")
                                Log.i(TAG, "okey stop service")
                                jobFinished(job!!, false)
                                fusedLocationClient.removeLocationUpdates(locationCallback)
                                //                                if(mGoogleApiClient != null){
                                //                                    mGoogleApiClient!!.disconnect()
                                //                                }
                            } else {
                                Log.i(TAG, "there is problem to add data to database")
                            }
                        }
                    })

        }
    }

    override fun onDestroy() {
        Log.i(TAG,"Problem: service destroy it couldn't happen")
        fusedLocationClient.removeLocationUpdates(locationUpdateCallback)
        super.onDestroy()
    }

    override fun getContext(): Context {
        return this
    }

    override fun finishJob() {
        jobFinished(job!!, false)
    }

}