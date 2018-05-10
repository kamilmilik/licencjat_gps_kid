package kamilmilik.licencjat_gps_kid

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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kamilmilik.licencjat_gps_kid.models.TrackingModel
import kamilmilik.licencjat_gps_kid.models.User

/**
 * Created by kamil on 23.04.2018.
 */
class TestLocationForeground : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var locationRequest: LocationRequest? = null

    private val TAG = TestLocationForeground::class.java.simpleName
    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val thread = object : Thread() {
            @SuppressLint("MissingPermission")
            @RequiresApi(Build.VERSION_CODES.O)
            override fun run() {
                Log.i(TAG, "dzialam")
                val notificationIntent = Intent(this@TestLocationForeground, TestLocationForeground::class.java)
                val pendingIntent = PendingIntent.getActivity(this@TestLocationForeground, 0, notificationIntent, 0)

                val notification = Notification.Builder(this@TestLocationForeground)
                        .setContentTitle("location")
                        .setContentText("Pobieram lokalizacje")
                        .setSmallIcon(R.drawable.ic_launcher_background)
                        .setContentIntent(pendingIntent)
                        .build()

                startForeground(2, notification)
                fusedLocationClient = LocationServices.getFusedLocationProviderClient(this@TestLocationForeground)
                createLocationRequest()
                fusedLocationClient.requestLocationUpdates(locationRequest,
                        object : LocationCallback(){
                            override fun onLocationResult(locationResult: LocationResult?) {
                                Log.i(TAG,"onLocationResult() $locationResult")
                                for (location in locationResult!!.locations) {
                                    addCurrentUserLocationToFirebase(location)
                                }
                            }
                        }
                        , Looper.getMainLooper())
            }
        }
        thread.start()

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
//        val intent = Intent(this, ForegroundOnTaskRemovedActivity::class.java)
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//        startActivity(intent)
        var locations = FirebaseDatabase.getInstance().getReference("Locations")
        var currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {//prevent if user click logout to not update locationOfUserWhoChangeIt
            Log.i(TAG, "addCurrentUserMarkerAndRemoveOld() current user: " + currentUser!!.uid + " location " + lastLocation.toString() )
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
                                                stopForeground(true)
                                                stopSelf()
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
}