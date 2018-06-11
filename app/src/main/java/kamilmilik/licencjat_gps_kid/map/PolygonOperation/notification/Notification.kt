package kamilmilik.licencjat_gps_kid.map.PolygonOperation.notification

import android.content.Context
import android.location.Location
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import kamilmilik.licencjat_gps_kid.utils.Constants
import kamilmilik.licencjat_gps_kid.models.*
import android.app.PendingIntent
import android.content.Intent
import android.support.v4.app.NotificationCompat
import kamilmilik.licencjat_gps_kid.map.MapActivity
import android.support.v4.app.NotificationManagerCompat
import android.R
import kamilmilik.licencjat_gps_kid.utils.Tools


/**
 * Created by kamil on 17.03.2018.
 */

class Notification(var context: Context/*, var jobService : JobService, var job : JobParameters*/){
    private var TAG = Notification::class.java.simpleName

    fun notificationAction(isRunOnlyOnce: Boolean) {
        Log.i(TAG, "notificationAction, current user id : " + FirebaseAuth.getInstance().currentUser!!.uid)
        var currentUser = FirebaseAuth.getInstance().currentUser
        val reference = FirebaseDatabase.getInstance().reference
        findFollowersUser(reference, currentUser!!, isRunOnlyOnce)
        findFollowingUser(reference, currentUser!!, isRunOnlyOnce)
    }

    private fun findFollowersUser(reference: DatabaseReference, currentUser: FirebaseUser, isRunOnlyOnce: Boolean) {
        val query = reference.child(Constants.DATABASE_FOLLOWERS)
                .orderByKey()
                .equalTo(currentUser!!.uid)
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (singleSnapshot in dataSnapshot.children) {
                    for (childSingleSnapshot in singleSnapshot.children) {
                        var userFollowers = childSingleSnapshot.child(Constants.DATABASE_USER).getValue(User::class.java)
                        Log.i(TAG, "value followers: " + userFollowers!!.user_id + " " + userFollowers.email)
                        loadLocationsFromDatabaseForGivenUserId(userFollowers.user_id!!, isRunOnlyOnce)
                        if(isRunOnlyOnce){
                            query.removeEventListener(this)
                        }
                    }
                }
                if (dataSnapshot.value == null) {//nothing found
                    Log.i(TAG, "nothing found in onDataChange in followers")
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        })

    }

    private fun findFollowingUser(reference: DatabaseReference, currentUser: FirebaseUser, isRunOnlyOnce: Boolean) {
        val query = reference.child(Constants.DATABASE_FOLLOWING)
                .orderByKey()
                .equalTo(currentUser!!.uid)
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (singleSnapshot in dataSnapshot.children) {
                    for (childSingleSnapshot in singleSnapshot.children) {
                        var userFollowing = childSingleSnapshot.child(Constants.DATABASE_USER).getValue(User::class.java)
                        Log.i(TAG, "value following: " + userFollowing!!.user_id + " " + userFollowing!!.email)
                        loadLocationsFromDatabaseForGivenUserId(userFollowing.user_id!!, isRunOnlyOnce)
                        if(isRunOnlyOnce){
                            Log.i(TAG,"onDataChange() remove listener")
                            query.removeEventListener(this)
                        }
                    }
                }
                if (dataSnapshot.value == null) {//nothing found
                    Log.i(TAG, "nothing found in onDataChange in following")
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun loadLocationsFromDatabaseForGivenUserId(userId: String, isRunOnlyOnce: Boolean) {
        Log.i(TAG, "userLocationAction")
        var locations = FirebaseDatabase.getInstance().getReference(Constants.DATABASE_LOCATIONS)
        var query: Query = locations.orderByChild(Constants.DATABASE_USER_ID_FIELD).equalTo(userId)
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                Log.i(TAG, "onDataChange in Locations listener notification " + dataSnapshot.toString())
                for (singleSnapshot in dataSnapshot!!.children) {
                    var userWhoChangeLocation = singleSnapshot.getValue(TrackingModel::class.java)
                    Log.i(TAG,"user who change locationOfUserWhoChangeIt : " + userWhoChangeLocation!!.email + " locationOfUserWhoChangeIt " + userWhoChangeLocation!!.lat + " " + userWhoChangeLocation!!.lng )
                    var locationOfUserWhoChangeIt = Tools.createLocationVariable(LatLng(userWhoChangeLocation!!.lat!!.toDouble(),userWhoChangeLocation!!.lng!!.toDouble()))
                    var userIdToSendNotification = userWhoChangeLocation.user_id
                    var currentUser = FirebaseAuth.getInstance().currentUser
                    getPolygonFromDatabase(locationOfUserWhoChangeIt, userIdToSendNotification!!, currentUser!!.uid)
                    if(isRunOnlyOnce){
                        Log.i(TAG,"onDataChange() remove listener")
                        query.removeEventListener(this)
                    }
                }
                if (dataSnapshot.value == null) {//nothing found
                    Log.i(TAG, "nothing found in onDataChange")
                }
            }
            override fun onCancelled(databaseError: DatabaseError?) {}
        })
    }

    /**
     * get polygon lat lng from database and check if given user is inside area or outside
     * @param locationOfUserWhoChangeIt it is a locationOfUserWhoChangeIt of user who change locationOfUserWhoChangeIt
     * @param userIdToSendNotification user id where we send notification
     * @param currentUserId user id who send this notification
     */
    private fun getPolygonFromDatabase(locationOfUserWhoChangeIt: Location, userIdToSendNotification: String, currentUserId: String){
        var databaseReference = FirebaseDatabase.getInstance().getReference(Constants.DATABASE_USER_POLYGONS)
        var currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {//prevent if user click logout
            var polygonsLatLngMap : HashMap<UserAndPolygonKeyModel, ArrayList<LatLng>> = HashMap()
            var query: Query = databaseReference.orderByKey().equalTo(currentUser.uid)
            query.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot?) {
                    for (singleSnapshot in dataSnapshot!!.children) {
                        for(child in singleSnapshot.children){
                            var polygonsFromDbMap = child.getValue(PolygonModel::class.java)
                            Log.i(TAG,polygonsFromDbMap!!.tag + " " + polygonsFromDbMap!!.polygonLatLngList)

                            var newList : ArrayList<LatLng> = Tools.changePolygonModelWithMyOwnLatLngListToLatLngList(polygonsFromDbMap)
                            var userAndPolygonKeyModel = UserAndPolygonKeyModel(userIdToSendNotification,polygonsFromDbMap!!.tag!!)
                            polygonsLatLngMap.put(userAndPolygonKeyModel, newList)
                        }
                    }
                    Log.i(TAG,"Check if given locationOfUserWhoChangeIt: $locationOfUserWhoChangeIt is in polygon")
                    if(!polygonsLatLngMap.isEmpty()){
                        Log.i(TAG,"isInArea in polygon?")
                        var insideOrOutsideArea = InsideOrOutsideArea(context, locationOfUserWhoChangeIt!!)
                        var listOfIsInArea = insideOrOutsideArea.isPointInsidePolygon(polygonsLatLngMap)
                        Log.i(TAG, "list of isInArea " + listOfIsInArea.toString())
//                        listOfIsInArea
//                                .filter { it == Constants.ENTER || it == Constants.EXIT }
//                                .forEach { notification(it,userIdToSendNotification,currentUserId) }
                        for(it in listOfIsInArea){
                            if(it == Constants.ENTER || it == Constants.EXIT  ){
                                notification(it, userIdToSendNotification)
                            }else{
                                Log.i(TAG,"finish job in else block user not change polygon action")
                            }
                        }
                    }else{
                        Log.i(TAG,"finish job in else block")
                    }

                    if (dataSnapshot.value == null) {//nothing found
                        Log.i(TAG, "nothing found in onDataChange")
                    }
                }

                override fun onCancelled(p0: DatabaseError?) {}
            })
        }
    }

    private fun notification(transition : Int, userIdToSendNotification: String ){
        if(transition == Constants.ENTER ){
            userNotifyAction(userIdToSendNotification,
                    context.getString(kamilmilik.licencjat_gps_kid.R.string.inPolygonInformation),
                    Constants.NOTIFICATION_CHANNEL_ID_ENTER,
                    Constants.NOTIFICATION_ID_ENTER)
        } else if (transition == Constants.EXIT) {
            userNotifyAction(userIdToSendNotification,
                    context.getString(kamilmilik.licencjat_gps_kid.R.string.exitPolygonInformation),
                    Constants.NOTIFICATION_CHANNEL_ID_EXIT,
                    Constants.NOTIFICATION_ID_EXIT)
        }
    }

    private fun userNotifyAction(userIdToSendNotification: String, contentText: String, notificationChanelId : String, notificationId : Int){
        Log.i(TAG, "userNotifyAction " + userIdToSendNotification)
        FirebaseDatabase.getInstance().getReference(Constants.DATABASE_USER_ACCOUNT_SETTINGS).orderByKey().equalTo(userIdToSendNotification)
                .addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onCancelled(p0: DatabaseError?) {}

                    override fun onDataChange(dataSnapshot: DataSnapshot?) {
                        for (singleSnapshot in dataSnapshot!!.children) {
                            var user = singleSnapshot!!.getValue(User::class.java)
                            buildNotificationForInsideOrOutsideArea(context.getString(kamilmilik.licencjat_gps_kid.R.string.notificationAreaTitle),
                                    "${user!!.user_name} ${contentText}",
                                    notificationChanelId,
                                    notificationId
                            )
                        }
                    }
                })
    }

    fun buildNotificationForInsideOrOutsideArea(contentTitle : String, contentText : String, notificationChanelId : String, notificationId : Int){
        val intent = Intent(context, MapActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)

        val mBuilder = NotificationCompat.Builder(context, notificationChanelId)
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                .setSmallIcon(R.drawable.ic_dialog_dialer)
                .setContentIntent(pendingIntent)
                .setVibrate(longArrayOf(1000, 1000, 1000, 1000, 1000))

        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(notificationId, mBuilder.build())
    }

}