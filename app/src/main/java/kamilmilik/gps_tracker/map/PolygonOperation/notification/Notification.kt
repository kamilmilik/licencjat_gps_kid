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
                            for (singleSnapshot in dataSnapshot.children) {
                                for (child in singleSnapshot.children) {
                                    child.getValue(PolygonModel::class.java)?.let { polygonModel ->
                                        polygonsFromDbMap.add(polygonModel)
                                    }
                                }
                            }

                            if (dataSnapshot.value == null) {//nothing found
                                synchronizeAction?.allTaskDoneAction()
                            } else {
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
            query.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (!isTooMuchWorkOnUi.get()) {
                        for (singleSnapshot in dataSnapshot.children) {
                            synchronizeAction?.setSynchronizeCounter(databaseNode, singleSnapshot.childrenCount)
                            for (childSingleSnapshot in singleSnapshot.children) {
                                val user = childSingleSnapshot.child(Constants.DATABASE_USER_FIELD).getValue(User::class.java)
                                user?.user_id?.let { userId ->
                                    loadLocationsFromDatabaseForGivenUserId(userId, isRunOnlyOnce)
                                }
                            }
                        }
                        if (isRunOnlyOnce) {
                            query.removeEventListener(this)
                        } else {
                            putValueEventListenersToMap(QueryUserModel(userIdQuery, query), this)
                        }
                        if (dataSnapshot.value == null) {
                            if (synchronizeAction != null) {
                                synchronizeAction?.setSynchronizeCounter(databaseNode, 0)
                                synchronizeAction?.doOnlyOneAction()
                            }
                        }

                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
        }
    }

    private fun loadLocationsFromDatabaseForGivenUserId(userId: String, isRunOnlyOnce: Boolean) {
        val locations = FirebaseDatabase.getInstance().getReference(Constants.DATABASE_LOCATIONS)
        val query: Query = locations.orderByChild(Constants.DATABASE_USER_ID_FIELD).equalTo(userId)
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                if (!isTooMuchWorkOnUi.get()) {
                    dataSnapshot?.let {
                        for (singleSnapshot in dataSnapshot.children) {
                            val userWhoChangeLocation = singleSnapshot.getValue(TrackingModel::class.java)
                            ObjectsUtils.safeLetTrackingModel(userWhoChangeLocation) { userIdWhoChangeLocation, email, name, lat, lng ->
                                val locationOfUserWhoChangeIt = LocationUtils.createLocationVariable(LatLng(lat.toDouble(), lng.toDouble()))
                                val currentUser = FirebaseAuth.getInstance().currentUser
                                val polygonsLatLngMap: HashMap<UserAndPolygonKeyModel, ArrayList<LatLng>> = HashMap()
                                if (currentUser != null) {
                                    for (polygon in polygonsFromDbMap) {
                                        polygon.tag?.let { polygonTag ->
                                            val newList: ArrayList<LatLng> = LocationUtils.changePolygonModelWithMyOwnLatLngListToLatLngList(polygon)
                                            val userAndPolygonKeyModel = UserAndPolygonKeyModel(userIdWhoChangeLocation, polygonTag)
                                            polygonsLatLngMap.put(userAndPolygonKeyModel, newList)
                                        }
                                    }
                                    val insideOrOutsideArea = InsideOrOutsideArea(context, locationOfUserWhoChangeIt)
                                    val listOfIsInArea = insideOrOutsideArea.isPointInsidePolygon(polygonsLatLngMap)
                                    for (state in listOfIsInArea) {
                                        if (state == Constants.PolygonAreaState.ENTER.idOfState || state == Constants.PolygonAreaState.EXIT.idOfState) {
                                            notification(state, userIdWhoChangeLocation, listOfIsInArea.size)
                                        } else {
                                            if (synchronizeAction != null) {
                                                synchronizeAction?.endOfPolygonIterateAction(listOfIsInArea.size)
                                            }
                                        }
                                    }
                                }
                            }

                        }
                    }
                    if (isRunOnlyOnce) {
                        query.removeEventListener(this)
                    } else {
                        putValueEventListenersToMap(QueryUserModel(userId, query), this)
                    }
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
        FirebaseDatabase.getInstance().getReference(Constants.DATABASE_USER_ACCOUNT_SETTINGS).orderByKey().equalTo(userIdWhoChangeLocation)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError?) {}

                    override fun onDataChange(dataSnapshot: DataSnapshot?) {
                        dataSnapshot?.let {
                            for (singleSnapshot in dataSnapshot.children) {
                                val user = singleSnapshot?.getValue(User::class.java)
                                buildNotificationForInsideOrOutsideArea(NotificationModel(context.getString(kamilmilik.gps_tracker.R.string.app_name),
                                        "${user?.user_name} $contentText",
                                        notificationChanelId,
                                        notificationId,
                                        true)
                                )
                            }
                            if (synchronizeAction != null) {
                                synchronizeAction?.endOfPolygonIterateAction(listSize)
                            }
                        }
                    }
                })
    }

    fun buildNotificationForInsideOrOutsideArea(notificationModel: NotificationModel) {
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