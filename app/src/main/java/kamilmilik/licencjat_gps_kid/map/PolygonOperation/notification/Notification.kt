package kamilmilik.licencjat_gps_kid.map.PolygonOperation.notification

import android.content.Context
import android.location.Location
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kamilmilik.licencjat_gps_kid.utils.Constants
import kamilmilik.licencjat_gps_kid.models.*
import android.app.PendingIntent
import android.content.Intent
import android.support.v4.app.NotificationCompat
import kamilmilik.licencjat_gps_kid.map.MapActivity
import android.support.v4.app.NotificationManagerCompat
import android.R
import kamilmilik.licencjat_gps_kid.map.BasicListenerContent
import kamilmilik.licencjat_gps_kid.utils.Tools


/**
 * Created by kamil on 17.03.2018.
 */

class Notification(var context: Context) : BasicListenerContent(){
    private var TAG = Notification::class.java.simpleName

    fun notificationAction(isRunOnlyOnce: Boolean) {
        Log.i(TAG, "notificationAction, current user id : " + FirebaseAuth.getInstance()?.currentUser?.uid)
        findConnectionUsers(Constants.DATABASE_FOLLOWERS, isRunOnlyOnce)
        findConnectionUsers(Constants.DATABASE_FOLLOWING, isRunOnlyOnce)
    }

    private fun findConnectionUsers(databaseNode: String, isRunOnlyOnce: Boolean) {
        var currentUser = FirebaseAuth.getInstance()?.currentUser
        val reference = FirebaseDatabase.getInstance().reference
        val query = reference.child(databaseNode).orderByKey().equalTo(currentUser?.uid)
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (singleSnapshot in dataSnapshot.children) {
                    for (childSingleSnapshot in singleSnapshot.children) {
                        var user = childSingleSnapshot.child(Constants.DATABASE_USER_FIELD).getValue(User::class.java)
                        Log.i(TAG, "value : " + user!!.user_id + " " + user!!.email)
                        loadLocationsFromDatabaseForGivenUserId(user.user_id!!, isRunOnlyOnce)
                    }
                }
                if(isRunOnlyOnce){
                    Log.i(TAG,"onDataChange() remove listener")
                    query.removeEventListener(this)
                }
                if (dataSnapshot.value == null) {//nothing found
                    Log.i(TAG, "nothing found in onDataChange in following")
                }
                putValueEventListenersToMap(query, this)
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
                    var currentUser = FirebaseAuth.getInstance().currentUser
                    if(currentUser != null){// Prevent if user click logout.
                        getPolygonFromDatabase(locationOfUserWhoChangeIt, userWhoChangeLocation.user_id!!)
                    }
                }
                if(isRunOnlyOnce){
                    Log.i(TAG,"onDataChange() remove listener")
                    query.removeEventListener(this)
                }
                if (dataSnapshot.value == null) {//nothing found
                    Log.i(TAG, "nothing found in onDataChange")
                }
                putValueEventListenersToMap(query, this)
            }
            override fun onCancelled(databaseError: DatabaseError?) {}
        })
    }

    private fun getPolygonFromDatabase(locationOfUserWhoChangeIt: Location, userIdWhoChangeLocation: String){
        val databaseReference = FirebaseDatabase.getInstance().getReference(Constants.DATABASE_USER_POLYGONS)
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {// Prevent if user click logout.
            val polygonsLatLngMap : HashMap<UserAndPolygonKeyModel, ArrayList<LatLng>> = HashMap()
            val query: Query = databaseReference.orderByKey().equalTo(currentUser.uid)
            query.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot?) {
                    for (singleSnapshot in dataSnapshot!!.children) {
                        for(child in singleSnapshot.children){
                            val polygonsFromDbMap = child.getValue(PolygonModel::class.java)
                            Log.i(TAG,polygonsFromDbMap!!.tag + " " + polygonsFromDbMap.polygonLatLngList)

                            val newList : ArrayList<LatLng> = Tools.changePolygonModelWithMyOwnLatLngListToLatLngList(polygonsFromDbMap)
                            val userAndPolygonKeyModel = UserAndPolygonKeyModel(userIdWhoChangeLocation,polygonsFromDbMap.tag!!)
                            polygonsLatLngMap.put(userAndPolygonKeyModel, newList)
                        }
                    }
                    Log.i(TAG,"Check if given locationOfUserWhoChangeIt: $locationOfUserWhoChangeIt is in polygon")
                    if(!polygonsLatLngMap.isEmpty()){
                        Log.i(TAG,"isInArea in polygon?")
                        val insideOrOutsideArea = InsideOrOutsideArea(context, locationOfUserWhoChangeIt!!)
                        val listOfIsInArea = insideOrOutsideArea.isPointInsidePolygon(polygonsLatLngMap)
                        Log.i(TAG, "list of isInArea " + listOfIsInArea.toString())
                        for(it in listOfIsInArea){
                            if(it == Constants.EPolygonAreaState.ENTER.idOfState || it == Constants.EPolygonAreaState.EXIT.idOfState  ){
                                notification(it, userIdWhoChangeLocation)
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
                    putValueEventListenersToMap(query, this)
                }

                override fun onCancelled(p0: DatabaseError?) {}
            })
        }
    }

    private fun notification(transition : Int, userIdWhoChangeLocation: String ){
        if(transition == Constants.EPolygonAreaState.ENTER.idOfState ){
            userNotifyAction(userIdWhoChangeLocation,
                    context.getString(kamilmilik.licencjat_gps_kid.R.string.inPolygonInformation),
                    Constants.NOTIFICATION_CHANNEL_ID_ENTER,
                    Constants.NOTIFICATION_ID_ENTER)
        } else if (transition == Constants.EPolygonAreaState.EXIT.idOfState) {
            userNotifyAction(userIdWhoChangeLocation,
                    context.getString(kamilmilik.licencjat_gps_kid.R.string.exitPolygonInformation),
                    Constants.NOTIFICATION_CHANNEL_ID_EXIT,
                    Constants.NOTIFICATION_ID_EXIT)
        }
    }

    private fun userNotifyAction(userIdWhoChangeLocation: String, contentText: String, notificationChanelId : String, notificationId : Int){
        Log.i(TAG, "userNotifyAction " + userIdWhoChangeLocation)
        FirebaseDatabase.getInstance().getReference(Constants.DATABASE_USER_ACCOUNT_SETTINGS).orderByKey().equalTo(userIdWhoChangeLocation)
                .addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onCancelled(p0: DatabaseError?) {}

                    override fun onDataChange(dataSnapshot: DataSnapshot?) {
                        for (singleSnapshot in dataSnapshot!!.children) {
                            var user = singleSnapshot!!.getValue(User::class.java)
                            buildNotificationForInsideOrOutsideArea(context.getString(kamilmilik.licencjat_gps_kid.R.string.notificationAreaTitle),
                                    "${user!!.user_name} $contentText",
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