package kamilmilik.licencjat_gps_kid.Utils

import android.app.*
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.google.android.gms.location.GeofenceStatusCodes
import android.content.Context
import android.graphics.Color
import android.support.v4.app.NotificationCompat
import kamilmilik.licencjat_gps_kid.MainActivity
import android.text.TextUtils
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.database.FirebaseDatabase
import kamilmilik.licencjat_gps_kid.ListOnline
import kamilmilik.licencjat_gps_kid.R


/**
 * Created by kamil on 01.03.2018.
 */
class GeofenceService : IntentService {
    val TAG = GeofenceService::class.java.simpleName
    val GEOFENCE_NOTIFICATION_ID = 0

    constructor() : super("GeofenceService") {}
    override fun onHandleIntent(intent: Intent?) {
        var event = GeofencingEvent.fromIntent(intent)
        if(event.hasError()){
            val errorMsg = getErrorString(event.errorCode)
            Log.e(TAG, errorMsg)
            return
        }else{
            var transition = event.geofenceTransition
            var geofences : List<Geofence> = event.triggeringGeofences
            var geofence = geofences[0]
            var requestId = geofence.requestId

            if(transition == Geofence.GEOFENCE_TRANSITION_ENTER || transition == Geofence.GEOFENCE_TRANSITION_EXIT){
                val triggeringGeofences  : List<Geofence> = event.triggeringGeofences
                // Create a detail message with Geofences received
                val geofenceTransitionDetails = getGeofenceTransitionDetails(transition, triggeringGeofences)
                // Send notification details as a String
                sendNotification(geofenceTransitionDetails)            }
        }
    }

    // Create a detail message with Geofences received
    private fun getGeofenceTransitionDetails(geoFenceTransition: Int, triggeringGeofences: List<Geofence>): String {
        // get the ID of each geofence triggered
        val triggeringGeofencesList : ArrayList<String> = ArrayList()
        for (geofence in triggeringGeofences) {
            triggeringGeofencesList.add(geofence.requestId)
        }

        var status: String? = null
        if (geoFenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER)
            status = "Entering "
        else if (geoFenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT)
            status = "Exiting "
        return status!! + TextUtils.join(", ", triggeringGeofencesList)
    }

    // Send a notification
    private fun sendNotification(msg: String) {
        Log.i(TAG, "sendNotification: " + msg)

        // Intent to start the main Activity
        val notificationIntent  = Intent(applicationContext,ListOnline::class.java)

        val stackBuilder = TaskStackBuilder.create(this)
        stackBuilder.addParentStack(ListOnline::class.java)
        stackBuilder.addNextIntent(notificationIntent)
        val notificationPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)

        // Creating and sending Notification
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(
                GEOFENCE_NOTIFICATION_ID,
                createNotification(msg, notificationPendingIntent))
    }

    // Create a notification
    private fun createNotification(msg: String, notificationPendingIntent: PendingIntent): Notification {
        val notificationBuilder = NotificationCompat.Builder(this)
        notificationBuilder
                .setSmallIcon(R.drawable.common_google_signin_btn_icon_dark)
                .setColor(Color.RED)
                .setContentTitle(msg)
                .setContentText("Geofence Notification!")
                .setContentIntent(notificationPendingIntent)
                .setDefaults(Notification.DEFAULT_LIGHTS or Notification.DEFAULT_VIBRATE or Notification.DEFAULT_SOUND)
                .setAutoCancel(true)
        return notificationBuilder.build()
    }

    // Handle errors
    private fun getErrorString(errorCode: Int): String {
        when (errorCode) {
            GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE -> return "GeoFence not available"
            GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES -> return "Too many GeoFences"
            GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS -> return "Too many pending intents"
            else -> return "Unknown error."
        }
    }
    fun notificationsToAnotherDevice(currentUserId : String,userIdToSendNotification : String){
        var notificationsDatabase = FirebaseDatabase.getInstance().reference.child("notifications")

        var notificationData : HashMap<String, String> = HashMap()
        notificationData.put("from", currentUserId)
        notificationData.put("type","request")

        notificationsDatabase.child(userIdToSendNotification).push().setValue(notificationData).addOnSuccessListener { object : OnSuccessListener<Void>{
            override fun onSuccess(void: Void?) {
                Log.i(TAG,"success set value in notifications in " + currentUserId)
            }

        }}
    }


}