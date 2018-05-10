//package kamilmilik.licencjat_gps_kid.Utils
//
//import android.Manifest
//import android.app.*
//import android.content.Intent
//import android.os.IBinder
//import android.content.Context
//import android.content.pm.PackageManager
//import android.location.Location
//import android.os.Build
//import android.os.Bundle
//import android.support.v4.app.ActivityCompat
//import android.util.Log
//import com.google.android.gms.common.ConnectionResult
//import com.google.android.gms.common.api.GoogleApiClient
//import com.google.android.gms.location.LocationRequest
//import com.google.android.gms.location.LocationServices
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.database.*
//import kamilmilik.licencjat_gps_kid.Constants
//import kamilmilik.licencjat_gps_kid.Helper.Notification
//import kamilmilik.licencjat_gps_kid.models.TrackingModel
//import java.util.HashMap
//
//
///**
// * Created by kamil on 18.03.2018.
// */
//class PolygonAndLocationService : Service,
//        GoogleApiClient.ConnectionCallbacks,
//        GoogleApiClient.OnConnectionFailedListener,
//        com.google.android.gms.location.LocationListener {
//
//    private val TAG = PolygonAndLocationService::class.java.simpleName
//
//    private var notificationMethods: Notification? = null
//    constructor() : super(){}
//
//    override fun onCreate() {
////        notificationMethods = Notification(this@PolygonAndLocationService)
////        var thread = object : Thread(){
////            override fun run() {
////                buildGoogleApiClient()
////            }
////        }
////        thread.start()
//        super.onCreate()
//        Log.i(TAG,"onCreate() - > PolygonAndLocationService")
//    }
//
//    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
//        Log.i(TAG, "PolygonAndLocationService started")
//        val thread = object : Thread() {
//            override fun run() {
////                createNotificationChannelForApi26()
////                intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
////                intent.setClassName("com.miui.powerkeeper",
////                        "com.miui.powerkeeper.ui.HiddenAppsContainerManagementActivity")
////                intent.setClassName("com.coloros.oppoguardelf",
////                        "com.coloros.powermanager.fuelgaue.PowerConsumptionActivity")
////                intent.setClassName("com.coloros.safecenter",
////                        "com.coloros.safecenter.permission.startup.StartupAppListActivity");
////                val pendingIntent = PendingIntent.getActivity(this@PolygonAndLocationService, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
////                Log.i(TAG, "intent in service before notif: " + intent + " " + intent.action)
////                val notification = android.app.Notification.Builder(this@PolygonAndLocationService)
////                        .setContentTitle("Kid Tracker")
////                        .setContentText("Kid tracker check your location")
////                        .setSmallIcon(R.drawable.abc_cab_background_internal_bg)
////                        .setContentIntent(pendingIntent)
////                        .setTicker("ticker")
////                        .build()
////                startForeground(2, notification)
//                buildGoogleApiClient()
////                notificationMethods!!.notificationAction()
//
//            }
//        }
//        thread.start()
////        val handler = Handler()
////        handler.postDelayed(Runnable {
////            Log.i(TAG,"okey stop service")
////            //mGoogleApiClient!!.disconnect()
////            stopForeground(true)
////            stopSelf()
////        }, 100000)
//
//        return START_STICKY
//    }
//    var locationRequest: LocationRequest? = null
//    var mGoogleApiClient: GoogleApiClient? = null
//    var mLastLocation: Location? = null
//
//    var mRequestLocationUpdatesPendingIntent : PendingIntent? = null
//    fun buildGoogleApiClient() {
//        synchronized(this) {
//            Log.i(TAG, "buildGoogleApiClient")
//            mGoogleApiClient = GoogleApiClient.Builder(this)
//                    .addConnectionCallbacks(this)
//                    .addOnConnectionFailedListener(this)
//                    .addApi(LocationServices.API)
//                    .build()
//            mGoogleApiClient!!.connect()
//        }
//    }
//
//    private fun createLocationRequest() {
//        Log.i(TAG, "createLocationRequest")
//        locationRequest = LocationRequest()
//        locationRequest!!.interval = 100
//        locationRequest!!.fastestInterval = 100
//        locationRequest!!.smallestDisplacement = 1F
//        locationRequest!!.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
//    }
//    override fun onConnected(p0: Bundle?) {
//        Log.i(TAG, "onConnected")
//        createLocationRequest()
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            return
//        }
//        Log.i(TAG, "start request locationOfUserWhoChangeIt updates ")
//        val intent = Intent(this, ForegroundOnTaskRemovedActivity::class.java)
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//        startActivity(intent)
//        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, this)
//        // request locationOfUserWhoChangeIt updates
//        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
//                locationRequest,
//                mRequestLocationUpdatesPendingIntent)
//
//    }
//
//    override fun onConnectionSuspended(p0: Int) {
//        Log.i(TAG, "onConnectionSuspended")
//        mGoogleApiClient!!.connect()
//    }
//    override fun onConnectionFailed(p0: ConnectionResult) {
//        Log.i(TAG, "onConnectionFailed: google maps" + p0.errorMessage)
//    }
//
//    override fun onLocationChanged(location: Location?) {
//        if (FirebaseAuth.getInstance().currentUser != null) {
//            Log.i(TAG, "onLocationChanged")
//            mLastLocation = location!!
//            addCurrentUserLocationToFirebase(mLastLocation!!)
//        }
//    }
//
//    private fun addCurrentUserLocationToFirebase(lastLocation: Location) {
//        var locations = FirebaseDatabase.getInstance().getReference("Locations")
//        var currentUser = FirebaseAuth.getInstance().currentUser
//        if (currentUser != null) {//prevent if user click logout to not update locationOfUserWhoChangeIt
//        Log.i(TAG, "addCurrentUserMarkerAndRemoveOld() current user: " + currentUser!!.uid + " location " + lastLocation.toString() )
//
////            val scoresRef = locations.child(currentUser!!.uid)
////            scoresRef.keepSynced(true)
////            val connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected")
////            connectedRef.addValueEventListener(object : ValueEventListener {
////                override fun onDataChange(snapshot: DataSnapshot) {
////                    val connected = snapshot.getValue(Boolean::class.java)!!
////                    //FirebaseApp.initializeApp(applicationContext)
////                    if (connected) {
////                        Log.i(TAG,"connected")
//
//                        locations.child(currentUser!!.uid).onDisconnect()
//                                .setValue(TrackingModel(currentUser.uid,
//                                        currentUser!!.email!!,
//                                        lastLocation.latitude.toString(),
//                                        lastLocation.longitude.toString(),
//                                        System.currentTimeMillis()), object : DatabaseReference.CompletionListener {
//                                    override fun onComplete(error: DatabaseError?, reference: DatabaseReference?) {
//                                        Log.i(TAG,"onComplete()")
//                                        if(error == null){
//                                            Log.i(TAG,"onComplete() position saved to firebase database")
//                                            Log.i(TAG,"okey stop service")
//                                            mGoogleApiClient!!.disconnect()
//                                        }else{
//                                            Log.i(TAG,"there is problem to add data to database")
//                                        }
//                                    }
//                                })
////                    } else {
////                        Log.i(TAG,"not connected")
////                    }
////                }
////                override fun onCancelled(error: DatabaseError) {
////                    System.err.println("Listener was cancelled")
////                }
////            })
//        }
//    }
//
//
//    //prevent kill background service in kitkat android
//    override fun onTaskRemoved(rootIntent: Intent) {
//        Log.i(TAG,"onTaskRemoved()")
//        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1 && Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2) {
//            val intent = Intent(this, ForegroundOnTaskRemovedActivity::class.java)
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//            startActivity(intent)
//        }
//
//    }
//
//    private fun createNotificationChannelForApi26(){
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            // Create the NotificationChannel, but only on API 26+ because
//            // the NotificationChannel class is new and not in the support library
//            val name = "My channel name"
//            val description = "Description channel"
//            val importance = NotificationManager.IMPORTANCE_DEFAULT
//            val channel = NotificationChannel(Constants.CHANNEL_ID, name, importance)
//            channel.description = description
//            // Register the channel with the system
//            val notificationManager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//            notificationManager.createNotificationChannel(channel)
//        }
//    }
//    override fun onDestroy() {
//        Log.i(TAG,"Problem: service destroy it couldn't happen")
//        super.onDestroy()
//    }
//
//    override fun onBind(arg0: Intent): IBinder? {
//        return null
//    }
//
//
//}
