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
import android.text.TextUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import kamilmilik.licencjat_gps_kid.ListOnline
import kamilmilik.licencjat_gps_kid.R
import kamilmilik.licencjat_gps_kid.models.User


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
//          unneeded ?
//           var geofences : List<Geofence> = event.triggeringGeofences
//            var geofence = geofences[0]
//            var requestId = geofence.requestId

            if(transition == Geofence.GEOFENCE_TRANSITION_ENTER || transition == Geofence.GEOFENCE_TRANSITION_EXIT){

                findFollowersConnection(transition)


                // Get the geofences that were triggered. A single event can trigger
                // multiple geofences.
                val triggeringGeofences  : List<Geofence> = event.triggeringGeofences
                // Create a detail message with Geofences received
                val geofenceTransitionDetails = getGeofenceTransitionDetails(transition, triggeringGeofences)
                // Send notification details as a String
                //sendNotification(geofenceTransitionDetails)
            }

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



    private fun findFollowersConnection(transition : Int) {
        Log.i(TAG, "findFollowersConnection, current user id : " + FirebaseAuth.getInstance().currentUser!!.uid)
        var currentUser = FirebaseAuth.getInstance().currentUser
        val reference = FirebaseDatabase.getInstance().reference
        findFollowersUser(reference, currentUser!!, transition)
        findFollowingUser(reference, currentUser!!, transition)
    }

    /**
     * listener for followers in database,  this method run listener for location change in database and add to recycler view followers user
     * @param reference
     * @param currentUser
     */
    private fun findFollowersUser(reference: DatabaseReference, currentUser: FirebaseUser,transition : Int) {
        val query = reference.child("followers")
                .orderByKey()
                .equalTo(currentUser!!.uid)
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (singleSnapshot in dataSnapshot.children) {
                    for (childSingleSnapshot in singleSnapshot.children) {
                        var userFollowers = childSingleSnapshot.child("user").getValue(User::class.java)
                        Log.i(TAG, "value followers: " + userFollowers!!.user_id + " " + userFollowers.email)
                        if(transition == Geofence.GEOFENCE_TRANSITION_ENTER ){
                            saveToDatabaseNotificationsToAnotherDevice(userFollowers.user_id!!,currentUser.uid)
                        }else if(transition == Geofence.GEOFENCE_TRANSITION_EXIT){
                            removeValueFromDatabaseNotifications(userFollowers.user_id!!,currentUser.uid)
                        }
                    }
                }
                if (dataSnapshot.value == null) {//nothing found
                    Log.i(TAG, "nothing found in onDataChange in followers")
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Log.i(TAG, "onCancelled: " + databaseError.message)
            }
        })

    }

    /**
     * listener for following in database,  this method run listener for location change in database and add to recycler view following user
     * @param reference
     * @param currentUser
     */
    private fun findFollowingUser(reference: DatabaseReference, currentUser: FirebaseUser, transition: Int) {
        val query = reference.child("following")
                .orderByKey()
                .equalTo(currentUser!!.uid)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (singleSnapshot in dataSnapshot.children) {
                    for (childSingleSnapshot in singleSnapshot.children) {
                        var userFollowing = childSingleSnapshot.child("user").getValue(User::class.java)
                        Log.i(TAG, "value following: " + userFollowing!!.user_id + " " + userFollowing!!.email)
                        if(transition == Geofence.GEOFENCE_TRANSITION_ENTER ){
                            saveToDatabaseNotificationsToAnotherDevice(userFollowing.user_id!!,currentUser.uid)
                        }else if(transition == Geofence.GEOFENCE_TRANSITION_EXIT){
                            removeValueFromDatabaseNotifications(userFollowing.user_id!!, currentUser.uid)
                        }
                    }
                }
                if (dataSnapshot.value == null) {//nothing found
                    Log.i(TAG, "nothing found in onDataChange in following")
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Log.i(TAG, "onCancelled: " + databaseError.message)
            }
        })
    }

    fun saveToDatabaseNotificationsToAnotherDevice(userIdToSendNotification: String, currentUserId: String){

        var notificationData : HashMap<String, String> = HashMap()
        Log.i(TAG, "robie mape z wartoci " + currentUserId)
        notificationData.put("from", currentUserId)
        notificationData.put("type","request")

        addNotificationIfUserNotExistInDatabase(currentUserId,userIdToSendNotification,notificationData)
    }
    private fun addNotificationIfUserNotExistInDatabase(currentUserId: String,userIdToSendNotification: String, notificationData : HashMap<String,String>){
        var notificationsDatabase = FirebaseDatabase.getInstance().reference.child("notifications")
        notificationsDatabase.child(userIdToSendNotification).orderByChild("from").equalTo(currentUserId).addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                if(!dataSnapshot!!.exists()){
                    notificationsDatabase.child(userIdToSendNotification).push().setValue(notificationData)
                }
            }
            override fun onCancelled(databaseError: DatabaseError?) {}
        })
    }
    fun removeValueFromDatabaseNotifications(userIdToDelete : String, currentUserId: String){
        Log.i(TAG,"removeValueFromDatabaseNotifications userIdToDelete" +  userIdToDelete)
        var notificationsDatabase = FirebaseDatabase.getInstance().reference.child("notifications")
//        notificationsDatabase.child(userIdToDelete).removeValue() it is done in javascript functions file 
        var map   = java.util.HashMap<String, Any>() as MutableMap<String,Any>
        map.put("from", currentUserId)
        map.put("type", "delete")
        notificationsDatabase.child(userIdToDelete).addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                Log.i(TAG, dataSnapshot.toString() + "  " + dataSnapshot!!.children.toString())
                for(snapshot in dataSnapshot!!.children){
                    notificationsDatabase.child(userIdToDelete).child(snapshot.key).setValue(map)
                }
            }
            override fun onCancelled(databaseError: DatabaseError?) {}
        })
    }

}