package kamilmilik.licencjat_gps_kid.Utils

import android.annotation.SuppressLint
import android.support.v4.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import android.support.v4.app.NotificationManagerCompat
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.content.Intent
import android.app.PendingIntent
import android.location.Location
import android.os.Looper
import android.util.Log
import com.firebase.jobdispatcher.*
import com.google.android.gms.location.*
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kamilmilik.licencjat_gps_kid.*
import kamilmilik.licencjat_gps_kid.R
import kamilmilik.licencjat_gps_kid.models.TrackingModel


/**
 * Created by kamil on 03.03.2018.
 */
class FirebaseMessagingService : FirebaseMessagingService() {

    private val TAG = FirebaseMessagingService::class.java.simpleName

    private var locationRequest: LocationRequest? = null

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    override fun onCreate() {
        super.onCreate()
        Log.i(TAG,"onCreate()")
//        if(Tools.isGooglePlayServicesAvailable(this)){
//            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
//
//            createLocationRequest()
//        }
        WakeLocker.acquire(this)
    }
    private lateinit var dispatcher1 : FirebaseJobDispatcher
    private lateinit var dispatcher2 : FirebaseJobDispatcher

    private var mAuthListener : FirebaseAuth.AuthStateListener? = null
    @SuppressLint("MissingPermission")
    override fun onMessageReceived(remoteMessage: RemoteMessage?) {
        super.onMessageReceived(remoteMessage)
        WakeLocker.release()
        Log.i(TAG,"onMessageReceived()")
        mAuthListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            Log.i(TAG, "user " + user)
            if (user != null) {
                Log.i(TAG, "user log in")
                dispatcher2 = FirebaseJobDispatcher(GooglePlayDriver(applicationContext))
                    val myLocationJob = dispatcher2.newJobBuilder()
                            .setService(LocationJobService::class.java) // the JobService that will be called
                            .setTag("my-location-job")        // uniquely identifies the job
                            .setLifetime(Lifetime.UNTIL_NEXT_BOOT)
                            .setTrigger(Trigger.NOW)
                            .setRecurring(false)//to reschedule job
                            .setReplaceCurrent(true)
                            .build()

                    dispatcher2.mustSchedule(myLocationJob)

                    dispatcher1 = FirebaseJobDispatcher(GooglePlayDriver(applicationContext))
                    val myLocationJob2 = dispatcher1.newJobBuilder()
                            .setService(PolygonJobService::class.java) // the JobService that will be called
                            .setTag("my-polygon-job")        // uniquely identifies the job
                            .setLifetime(Lifetime.UNTIL_NEXT_BOOT)
                            // start between windowStart in sec and windowEnd in seconds from now
                            .setTrigger(Trigger.NOW)
                            .setRecurring(false)//to reschedule job
                            .setReplaceCurrent(true)
                            .build()

                    dispatcher1.mustSchedule(myLocationJob2)
            }
        }
        FirebaseAuth.getInstance().addAuthStateListener(mAuthListener!!)

//        var clickAction = remoteMessage!!.notification!!.clickAction
//        var notificationTitle = remoteMessage!!.notification!!.title
//        var notificationMessage = remoteMessage!!.notification!!.body
//        Log.i(TAG,"onMessageReceived() title ${notificationTitle} message ${notificationMessage} action " + clickAction)

//        var fromUserId = remoteMessage.data.get("from_user_id")//get value from node from_user_id
//        val mBuilder = NotificationCompat.Builder(this,Constants.CHANNEL_ID)
//                .setSmallIcon(R.drawable.notification_icon_background)
//                .setContentTitle(notificationTitle)
//                .setContentText(notificationMessage)
//                //.setOngoing(true)//then you cannot clear notification
//                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
//        createNotificationChannelForApi26()
//
//        tapToNotificationAction(mBuilder,clickAction!!)
//
//        createNotificationId(mBuilder)

    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        Log.i(TAG,"onTrimMemory()")
        //remove listener
        FirebaseAuth.getInstance().removeAuthStateListener(mAuthListener!!)
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
    private fun createNotificationId(builder : NotificationCompat.Builder){
        var notificationId = (System.currentTimeMillis()).toInt()
        var notificationManager : NotificationManager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, builder.build())
    }
    private fun tapToNotificationAction(builder : NotificationCompat.Builder, clickAction : String){
        val intent = Intent(clickAction)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, 0)
        builder.setContentIntent(pendingIntent)
    }
}