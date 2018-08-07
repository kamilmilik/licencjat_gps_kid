package kamilmilik.gps_tracker.map.PolygonOperation.notification

import android.content.Context
import android.location.Location
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kamilmilik.gps_tracker.models.*
import kamilmilik.gps_tracker.map.MapActivity
import android.support.v4.app.NotificationManagerCompat
import kamilmilik.gps_tracker.background.Synchronize
import kamilmilik.gps_tracker.map.BasicListenerContent
import kamilmilik.gps_tracker.utils.*
import kamilmilik.gps_tracker.utils.Constants.NOTIFICATION_CHANNEL_AREA


/**
 * Created by kamil on 17.03.2018.
 */

class Notification(var context: Context) : BasicListenerContent(){
    private var TAG = Notification::class.java.simpleName

    var synchronizeAction : Synchronize? = null

    fun notificationAction(isRunOnlyOnce: Boolean) {
        Log.i(TAG, "notificationAction, current user id : " + FirebaseAuth.getInstance()?.currentUser?.uid)
        findConnectionUsers(Constants.DATABASE_FOLLOWERS, isRunOnlyOnce)
        findConnectionUsers(Constants.DATABASE_FOLLOWING, isRunOnlyOnce)
    }

    private fun findConnectionUsers(databaseNode: String, isRunOnlyOnce: Boolean) {
        val currentUser = FirebaseAuth.getInstance()?.currentUser
        val reference = FirebaseDatabase.getInstance().reference
        val query = reference.child(databaseNode).orderByKey().equalTo(currentUser?.uid)
        LogUtils(context).appendLog(TAG,"findConnectionUsers() for databaseNode " + databaseNode)
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if(synchronizeAction != null){
                    synchronizeAction?.setSynchronizeCounter(databaseNode, dataSnapshot)
                }
                for (singleSnapshot in dataSnapshot.children) {
                    for (childSingleSnapshot in singleSnapshot.children) {
                        val user = childSingleSnapshot.child(Constants.DATABASE_USER_FIELD).getValue(User::class.java)
                        LogUtils(context).appendLog(TAG, "value : " + user!!.user_id + " " + user.email)
                        loadLocationsFromDatabaseForGivenUserId(user.user_id!!, isRunOnlyOnce)
                    }
                }
                if(isRunOnlyOnce){
                    Log.i(TAG,"onDataChange() remove listener")
                    query.removeEventListener(this)
                }
                if (dataSnapshot.value == null) {//nothing found
                    if(synchronizeAction != null) {
                        synchronizeAction?.doOnlyOneAction()
                    }
                    LogUtils(context).appendLog(TAG, "nothing found in onDataChange in $databaseNode")
                }
                putValueEventListenersToMap(query, this)
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun loadLocationsFromDatabaseForGivenUserId(userId: String, isRunOnlyOnce: Boolean) {
        Log.i(TAG, "userLocationAction")
        val locations = FirebaseDatabase.getInstance().getReference(Constants.DATABASE_LOCATIONS)
        val query: Query = locations.orderByChild(Constants.DATABASE_USER_ID_FIELD).equalTo(userId)
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                Log.i(TAG, "onDataChange in Locations listener notification " + dataSnapshot.toString())
                for (singleSnapshot in dataSnapshot!!.children) {
                    val userWhoChangeLocation = singleSnapshot.getValue(TrackingModel::class.java)
                    val locationOfUserWhoChangeIt = LocationUtils.createLocationVariable(LatLng(userWhoChangeLocation!!.lat!!.toDouble(),userWhoChangeLocation.lng!!.toDouble()))
                    val currentUser = FirebaseAuth.getInstance().currentUser
                    LogUtils(context).appendLog(TAG,"onDataChange() jestem w loadLocationsFromDatabaseForGivenUserId a currentUser = " + currentUser + " userWhoChangeLocation " + userWhoChangeLocation!!.email + " locationOfUserWhoChangeIt " + userWhoChangeLocation.lat + " " + userWhoChangeLocation.lng )
                    if(currentUser != null){// Prevent if user click logout.
                        getPolygonFromDatabase(locationOfUserWhoChangeIt, userWhoChangeLocation.user_id!!)
                    }
                }
                if(isRunOnlyOnce){
                    Log.i(TAG,"onDataChange() remove listener")
                    query.removeEventListener(this)
                }
                if (dataSnapshot.value == null) {//nothing found
                    LogUtils(context).appendLog(TAG, "nothing found in onDataChange it couldn't happen since we have locations in db of other user")
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
                    LogUtils(context).appendLog(TAG, "onDataChange() w getPolygonFromDatabase")
                    for (singleSnapshot in dataSnapshot!!.children) {
                        for(child in singleSnapshot.children){
                            val polygonsFromDbMap = child.getValue(PolygonModel::class.java)

                            val newList : ArrayList<LatLng> = LocationUtils.changePolygonModelWithMyOwnLatLngListToLatLngList(polygonsFromDbMap!!)
                            val userAndPolygonKeyModel = UserAndPolygonKeyModel(userIdWhoChangeLocation,polygonsFromDbMap.tag!!)
                            polygonsLatLngMap.put(userAndPolygonKeyModel, newList)
                        }
                    }
                    Log.i(TAG,"Check if given locationOfUserWhoChangeIt: $locationOfUserWhoChangeIt is in polygon")
                    if(!polygonsLatLngMap.isEmpty()){
                        Log.i(TAG,"isInArea in polygon?")
                        val insideOrOutsideArea = InsideOrOutsideArea(context, locationOfUserWhoChangeIt)
                        val listOfIsInArea = insideOrOutsideArea.isPointInsidePolygon(polygonsLatLngMap)
                        LogUtils(context).appendLog(TAG, "list of isInArea " + listOfIsInArea.toString())
                        for(it in listOfIsInArea){
                            if(it == Constants.EPolygonAreaState.ENTER.idOfState || it == Constants.EPolygonAreaState.EXIT.idOfState  ){
                                notification(it, userIdWhoChangeLocation, listOfIsInArea.size)
                            }else{
                                //nie ma zmiany wiec poprostu policz iteracje
                                if(synchronizeAction != null) {
                                    synchronizeAction?.endOfPolygonIterateAction(listOfIsInArea.size)
                                    LogUtils(context).appendLog(TAG,"onDataChange() isInPolygon not change polygonActionCounter " + synchronizeAction?.polygonActionCounter + " howManyTimesActionRunConnectedUser " + synchronizeAction?.howManyTimesActionRunConnectedUser + " allTaskDone " + synchronizeAction?.allTaskDoneCounter)
                                }
                            }
                        }
                    }

                    if (dataSnapshot.value == null) {//nothing found
                        //nie ma polygon wieksz zwieksz ile sie razy wykonalo bo w ifa nie wchodzi z polygonsLatLngMap
                        //ok tu sie moze to konczyc bo nie ma polygonu wiec sprawdz wynik
                        if(synchronizeAction != null) {
                            synchronizeAction?.oneUserEndPolygonAction()
                            LogUtils(context).appendLog(TAG,"no polygon " + " howManyTimesActionRunConnectedUser " + synchronizeAction?.howManyTimesActionRunConnectedUser + " allTaskDone " + synchronizeAction?.allTaskDoneCounter)
                        }
                        Log.i(TAG, "nothing found in onDataChange")
                    }
                    putValueEventListenersToMap(query, this)
                }

                override fun onCancelled(p0: DatabaseError?) {}
            })
        }
    }

    private fun notification(transition : Int, userIdWhoChangeLocation: String, listSize : Int){
        val NOTIFICATION_ID_ENTER = java.lang.System.currentTimeMillis().toInt()
        val NOTIFICATION_ID_EXIT = java.lang.System.currentTimeMillis().toInt()
        if(transition == Constants.EPolygonAreaState.ENTER.idOfState ){
            userNotifyAction(userIdWhoChangeLocation,
                    context.getString(kamilmilik.gps_tracker.R.string.inPolygonInformation),
                    NOTIFICATION_CHANNEL_AREA,
                    NOTIFICATION_ID_ENTER,
                    listSize)
        } else if (transition == Constants.EPolygonAreaState.EXIT.idOfState) {
            userNotifyAction(userIdWhoChangeLocation,
                    context.getString(kamilmilik.gps_tracker.R.string.exitPolygonInformation),
                    NOTIFICATION_CHANNEL_AREA,
                    NOTIFICATION_ID_EXIT,
                    listSize)
        }
    }

    private fun userNotifyAction(userIdWhoChangeLocation: String, contentText: String, notificationChanelId : String, notificationId : Int, listSize: Int){
        Log.i(TAG, "userNotifyAction " + userIdWhoChangeLocation)
        FirebaseDatabase.getInstance().getReference(Constants.DATABASE_USER_ACCOUNT_SETTINGS).orderByKey().equalTo(userIdWhoChangeLocation)
                .addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onCancelled(p0: DatabaseError?) {}

                    override fun onDataChange(dataSnapshot: DataSnapshot?) {
                        for (singleSnapshot in dataSnapshot!!.children) {
                            val user = singleSnapshot!!.getValue(User::class.java)
                            Log.i(TAG,"onDataChange() Build notification for " + user!!.user_name + " " + notificationChanelId + " " + notificationId)
                            buildNotificationForInsideOrOutsideArea(NotificationModel(context.getString(kamilmilik.gps_tracker.R.string.app_name),
                                    "${user.user_name} $contentText",
                                    notificationChanelId,
                                    notificationId,
                                    true)
                            )
                        }
                        if(synchronizeAction != null){
                            synchronizeAction?.endOfPolygonIterateAction(listSize)
                            Log.i(TAG,"userNotifyAction pobralem dane usera ktory zmienil polozenie polygonActionCounter " + synchronizeAction?.polygonActionCounter + " howManyTimesActionRunConnectedUser " + synchronizeAction?.howManyTimesActionRunConnectedUser + " allTaskDone " + synchronizeAction?.allTaskDoneCounter)

                        }
                    }
                })
    }

    fun buildNotificationForInsideOrOutsideArea(notificationModel : NotificationModel){
        LogUtils(context).appendLog(TAG, "Build notification ${notificationModel.contentText}" )
        Log.i(TAG,"buildNotificationForInsideOrOutsideArea() build notif")
        val pendingIntent = NotificationUtils.createPendingIntent(context, MapActivity::class.java, true)

        val notificationBuilder = NotificationUtils.createNotification(context, notificationModel)
                ?.setContentIntent(pendingIntent)
        NotificationUtils.createNotificationChannel(context, notificationModel)

        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(notificationModel.notificationId, notificationBuilder!!.build())
    }
}