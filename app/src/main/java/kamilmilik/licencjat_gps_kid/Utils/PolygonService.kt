package kamilmilik.licencjat_gps_kid.Utils

import android.app.IntentService
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.maps.android.PolyUtil
import kamilmilik.licencjat_gps_kid.models.User

/**
 * Created by kamil on 05.03.2018.
 */
class PolygonService : IntentService {//IntentService uses a worker thread to handle all of the start requests, one at a time
    val TAG = PolygonService::class.java.simpleName
    private val ENTER = 1
    private val EXIT = 2

    constructor() : super("PolygonService") {}
    //IntentService Creates a work queue that passes one intent at a time to your onHandleIntent() implementation, so you never have to worry about multi-threading.
    override fun onHandleIntent(intent: Intent?) {
       // Log.i(TAG,"intent " + intent.toString())
        var listOfIsInArea = intent!!.getSerializableExtra("isInArea") as ArrayList<Int>
        Log.i(TAG, listOfIsInArea.toString())
        for(transition in listOfIsInArea){
            if(transition == ENTER || transition == EXIT){
                findFollowersConnection(transition)
            }
        }

    }

//send notification
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
    private fun findFollowersUser(reference: DatabaseReference, currentUser: FirebaseUser, transition : Int) {
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
        notificationsDatabase.child(userIdToSendNotification).orderByChild("from").equalTo(currentUserId).addListenerForSingleValueEvent(object : ValueEventListener {
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
        notificationsDatabase.child(userIdToDelete).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                Log.i(TAG, dataSnapshot.toString() + "  " + dataSnapshot!!.children.toString())
                for(snapshot in dataSnapshot!!.children){
                    notificationsDatabase.child(userIdToDelete).child(snapshot.key).setValue(map)
                }
            }
            override fun onCancelled(databaseError: DatabaseError?) {}
        })
    }




    //0 is still outside or inside
    //1 enter
    //2 exit

}