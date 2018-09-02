package kamilmilik.gps_tracker.map.PolygonOperation.notification

import android.content.Context
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
import java.util.concurrent.atomic.AtomicBoolean


/**
 * Created by kamil on 17.03.2018.
 */

class Notification(var context: Context) : BasicListenerContent() {
    private var TAG = Notification::class.java.simpleName

    var synchronizeAction: Synchronize? = null // It may be null, if it isn't running in foreground service


    var polygonsFromDbMap: ArrayList<PolygonModel> = ArrayList()

    var isTooMuchWorkOnUi: AtomicBoolean = AtomicBoolean(false) // Not used in foreground service.

    init {
        Log.i(TAG, "init notification()")
    }

    fun notificationAction(isRunOnlyOnce: Boolean) {
        getPolygonsFromDatabase(isRunOnlyOnce)
    }

    private fun getPolygonsFromDatabase(isRunOnlyOnce: Boolean) {
        val databaseReference = FirebaseDatabase.getInstance().getReference(Constants.DATABASE_USER_POLYGONS)
        FirebaseAuth.getInstance().currentUser?.let { currentUser ->
            val userIdQuery = currentUser.uid
            val query: Query = databaseReference.orderByKey().equalTo(userIdQuery)
            query.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot?) {
                    if (!isTooMuchWorkOnUi.get()) {
                        polygonsFromDbMap = ArrayList()
                        dataSnapshot?.let {
                            LogUtils(context).appendLog(TAG, "onDataChange() w getPolygonFromDatabase")
                            for (singleSnapshot in dataSnapshot.children) {
                                for (child in singleSnapshot.children) {
                                    child.getValue(PolygonModel::class.java)?.let { polygonModel ->
                                        polygonsFromDbMap.add(polygonModel)
                                    }
                                }
                            }

                            //
                            if (dataSnapshot.value == null) {//nothing found
                                // zwieksz licznik i sprawdz jak lokalizacja pobrana to konczy serwis
                                synchronizeAction?.allTaskDoneAction()
                                //                            Log.i(TAG, "nothing found in onDataChange")
                            } else {
                                //tutaj policz ile jest polygonow bo tyle razy wykona sie loadLocationsFromDatabaseForGivenUserId
                                finderUserAction(isRunOnlyOnce)
                            }
                            if (isRunOnlyOnce) {
                                query.removeEventListener(this)
                            }
                        }
                    }
                }

                override fun onCancelled(p0: DatabaseError?) {}
            })
        }
    }

    private fun finderUserAction(isRunOnlyOnce: Boolean) {
        findConnectionUsers(Constants.DATABASE_FOLLOWERS, isRunOnlyOnce)
        findConnectionUsers(Constants.DATABASE_FOLLOWING, isRunOnlyOnce)
    }

    private fun findConnectionUsers(databaseNode: String, isRunOnlyOnce: Boolean) {
        val currentUser = FirebaseAuth.getInstance()?.currentUser
        val reference = FirebaseDatabase.getInstance().reference
        currentUser?.uid?.let { userIdQuery ->

            val query = reference.child(databaseNode).orderByKey().equalTo(userIdQuery)
//            LogUtils(context).appendLog(TAG, "findConnectionUsers() for databaseNode " + databaseNode)
            query.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (!isTooMuchWorkOnUi.get()) {
                        LogUtils(context).appendLog(TAG, "onDatachange in " + databaseNode)
                        for (singleSnapshot in dataSnapshot.children) {
                            // Ustawiam licznik ile razy wykonac sie ma ta metoda dla following a ile dla followers
                            synchronizeAction?.setSynchronizeCounter(databaseNode, singleSnapshot.childrenCount)
                            for (childSingleSnapshot in singleSnapshot.children) {
                                Log.i(TAG, "onDataChange() size " + singleSnapshot.childrenCount + " " + childSingleSnapshot.childrenCount)
                                val user = childSingleSnapshot.child(Constants.DATABASE_USER_FIELD).getValue(User::class.java)
                                user?.user_id?.let { userId ->
                                    LogUtils(context).appendLog(TAG, "value : " + user.user_id + " " + user.email)
                                    loadLocationsFromDatabaseForGivenUserId(userId, isRunOnlyOnce)
                                }
                            }
                        }
                        if (isRunOnlyOnce) {
                            Log.i(TAG, "onDataChange() remove listener in query " + query + " hash " + query.hashCode())
                            query.removeEventListener(this)
                        } else {
                            putValueEventListenersToMap(QueryUserModel(userIdQuery, query), this)
                        }
                        if (dataSnapshot.value == null) {//nothing found
                            if (synchronizeAction != null) {
                                synchronizeAction?.setSynchronizeCounter(databaseNode, 0) // No followers/following so set counter to 0
                                LogUtils(context).appendLog(TAG, "inkrementuje doOnlyOneTaskDoneCounter w findConnectionUsers() for databaseNode " + databaseNode)
                                synchronizeAction?.doOnlyOneAction()
                            }
                            LogUtils(context).appendLog(TAG, "nothing found in onDataChange in $databaseNode")
                        }

                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    LogUtils(context).appendLog(TAG, "onCancelled() $databaseNode error $databaseError")
                }
            })
        }
    }

    private fun loadLocationsFromDatabaseForGivenUserId(userId: String, isRunOnlyOnce: Boolean) {
        Log.i(TAG, "userLocationAction")
        val locations = FirebaseDatabase.getInstance().getReference(Constants.DATABASE_LOCATIONS)
        val query: Query = locations.orderByChild(Constants.DATABASE_USER_ID_FIELD).equalTo(userId)
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                if (!isTooMuchWorkOnUi.get()) {
                    Log.i(TAG, "onDataChange in Locations listener notification " + dataSnapshot.toString() + " for user " + userId)
                    dataSnapshot?.let {
                        for (singleSnapshot in dataSnapshot.children) {
                            val userWhoChangeLocation = singleSnapshot.getValue(TrackingModel::class.java)
                            ObjectsUtils.safeLetTrackingModel(userWhoChangeLocation) { userIdWhoChangeLocation, email, name, lat, lng ->
                                val locationOfUserWhoChangeIt = LocationUtils.createLocationVariable(LatLng(lat.toDouble(), lng.toDouble()))
                                val currentUser = FirebaseAuth.getInstance().currentUser
                                LogUtils(context).appendLog(TAG, "onDataChange() jestem w loadLocationsFromDatabaseForGivenUserId a currentUser = " + currentUser + " userWhoChangeLocation " + email + " locationOfUserWhoChangeIt " + lat + " " + lng)
                                val polygonsLatLngMap: HashMap<UserAndPolygonKeyModel, ArrayList<LatLng>> = HashMap()
                                if (currentUser != null) {// Prevent if user click logout.
                                    for (polygon in polygonsFromDbMap) {
                                        polygon.tag?.let { polygonTag ->
                                            val newList: ArrayList<LatLng> = LocationUtils.changePolygonModelWithMyOwnLatLngListToLatLngList(polygon)
                                            val userAndPolygonKeyModel = UserAndPolygonKeyModel(userIdWhoChangeLocation, polygonTag)
                                            //                                        Log.i(TAG, "onDataChange() wkladam do mapy " + userAndPolygonKeyModel.userId)
                                            // wkladam np SeF3706W4dOlPDFiygOMsG0p9p32 klucz i jako value listy polygonow moje
                                            polygonsLatLngMap.put(userAndPolygonKeyModel, newList)
                                        }
                                    }
                                    //                                Log.i(TAG,"Check if given locationOfUserWhoChangeIt: $locationOfUserWhoChangeIt is in polygon")
                                    //                                if(!polygonsLatLngMap.isEmpty()){ //Brak takiej sytuacji tutaj bo pierw bierzemy polygon a jak nie znajdzie zadnego
                                    // to to sie nie wykona poczwszy od finderUserAction
                                    Log.i(TAG, "isInArea in polygon?")
                                    val insideOrOutsideArea = InsideOrOutsideArea(context, locationOfUserWhoChangeIt)
                                    val listOfIsInArea = insideOrOutsideArea.isPointInsidePolygon(polygonsLatLngMap)
                                    //                                LogUtils(context).appendLog(TAG, "list of isInArea " + listOfIsInArea.toString())
                                    for (state in listOfIsInArea) { // iterujemy po polygonach [0,0,0,0] itd to jest zalezne ile jest polygonw w polygonsLatLngMap, a jest tyle ile znalazlo polygonow w getPolygonsFromDatabase
                                        if (state == Constants.PolygonAreaState.ENTER.idOfState || state == Constants.PolygonAreaState.EXIT.idOfState) {
                                            notification(state, userIdWhoChangeLocation, listOfIsInArea.size)
                                        } else {
                                            //nie ma zmiany wiec poprostu policz iteracje
                                            if (synchronizeAction != null) {
                                                synchronizeAction?.endOfPolygonIterateAction(listOfIsInArea.size)
                                                LogUtils(context).appendLog(TAG, "onDataChange() isInPolygon not change polygonActionCounter " + synchronizeAction?.polygonActionCounter + " howManyTimesActionRunConnectedUser " + synchronizeAction?.howManyTimesActionRunConnectedUser + " allTaskDone " + synchronizeAction?.allTaskDoneCounter)
                                            }
                                        }
                                    }


                                }
                            }

                        }
                    }
                    if (isRunOnlyOnce) {
                        //                    Log.i(TAG,"onDataChange() remove listener in query " + query + " hash " + query.hashCode() + " for user " + userId)
                        query.removeEventListener(this)
                    } else {
                        putValueEventListenersToMap(QueryUserModel(userId, query), this)
                    }
                    if (dataSnapshot?.value == null) {//nothing found
                        //                    LogUtils(context).appendLog(TAG, "nothing found in onDataChange it couldn't happen since we have locations in db of other user")
                    }
                    //                Log.i(TAG,"onDataChange() notification put to map query " + query + " hashcode " + query.hashCode() + " user " + userId)

                }
            }

            override fun onCancelled(databaseError: DatabaseError?) {}
        })
    }

    private fun notification(transition: Int, userIdWhoChangeLocation: String, listSize: Int) {
        val NOTIFICATION_ID_ENTER = java.lang.System.currentTimeMillis().toInt()
        val NOTIFICATION_ID_EXIT = java.lang.System.currentTimeMillis().toInt()
        if (transition == Constants.PolygonAreaState.ENTER.idOfState) {
            userNotifyAction(userIdWhoChangeLocation,
                    context.getString(kamilmilik.gps_tracker.R.string.inPolygonInformation),
                    NOTIFICATION_CHANNEL_AREA,
                    NOTIFICATION_ID_ENTER,
                    listSize)
        } else if (transition == Constants.PolygonAreaState.EXIT.idOfState) {
            userNotifyAction(userIdWhoChangeLocation,
                    context.getString(kamilmilik.gps_tracker.R.string.exitPolygonInformation),
                    NOTIFICATION_CHANNEL_AREA,
                    NOTIFICATION_ID_EXIT,
                    listSize)
        }
    }

    private fun userNotifyAction(userIdWhoChangeLocation: String, contentText: String, notificationChanelId: String, notificationId: Int, listSize: Int) {
        Log.i(TAG, "userNotifyAction " + userIdWhoChangeLocation)
        FirebaseDatabase.getInstance().getReference(Constants.DATABASE_USER_ACCOUNT_SETTINGS).orderByKey().equalTo(userIdWhoChangeLocation)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError?) {}

                    override fun onDataChange(dataSnapshot: DataSnapshot?) {
                        dataSnapshot?.let {
                            for (singleSnapshot in dataSnapshot.children) {
                                val user = singleSnapshot?.getValue(User::class.java)
                                Log.i(TAG, "onDataChange() Build notification for " + user?.user_name + " " + notificationChanelId + " " + notificationId)
                                buildNotificationForInsideOrOutsideArea(NotificationModel(context.getString(kamilmilik.gps_tracker.R.string.app_name),
                                        "${user?.user_name} $contentText",
                                        notificationChanelId,
                                        notificationId,
                                        true)
                                )
                            }
                            if (synchronizeAction != null) {
                                synchronizeAction?.endOfPolygonIterateAction(listSize)
                                Log.i(TAG, "userNotifyAction pobralem dane usera ktory zmienil polozenie polygonActionCounter " + synchronizeAction?.polygonActionCounter + " howManyTimesActionRunConnectedUser " + synchronizeAction?.howManyTimesActionRunConnectedUser + " allTaskDone " + synchronizeAction?.allTaskDoneCounter)

                            }
                        }
                    }
                })
    }

    fun buildNotificationForInsideOrOutsideArea(notificationModel: NotificationModel) {
        LogUtils(context).appendLog(TAG, "Build notification ${notificationModel.contentText}")
        Log.i(TAG, "buildNotificationForInsideOrOutsideArea() build notif")
        val pendingIntent = NotificationUtils.createPendingIntent(context, MapActivity::class.java, true)

        val notificationBuilder = NotificationUtils.createNotification(context, notificationModel)
                ?.setContentIntent(pendingIntent)
        NotificationUtils.createNotificationChannel(context, notificationModel)

        val notificationManager = NotificationManagerCompat.from(context)
        notificationBuilder?.let {
            notificationManager.notify(notificationModel.notificationId, notificationBuilder.build())
        }
    }
}