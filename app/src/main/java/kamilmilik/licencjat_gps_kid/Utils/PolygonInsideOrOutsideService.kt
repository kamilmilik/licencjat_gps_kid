package kamilmilik.licencjat_gps_kid.Utils

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.SystemClock
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.location.Location
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import kamilmilik.licencjat_gps_kid.Helper.PolygonOperation.InsideOrOutsideArea
import kamilmilik.licencjat_gps_kid.models.PolygonModel
import kamilmilik.licencjat_gps_kid.models.TrackingModel
import kamilmilik.licencjat_gps_kid.models.User


/**
 * Created by kamil on 18.03.2018.
 */
class PolygonInsideOrOutsideService : Service {

    private val TAG = PolygonInsideOrOutsideService::class.java.simpleName

    constructor() : super(){}

    override fun onBind(arg0: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.i(TAG, "PolygonInsideOrOutsideService started")
        val thread = object : Thread() {
            override fun run() {
                findFollowersConnection()
            }
        }
        thread.start()
        return Service.START_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent) {
        val restartService = Intent(applicationContext,
                this.javaClass)
        restartService.`package` = packageName
        val restartServicePI = PendingIntent.getService(
                applicationContext, 1, restartService,
                PendingIntent.FLAG_ONE_SHOT)

        //Restart the service once it has been killed android


        val alarmService = applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmService.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 100, restartServicePI)

    }

    override fun onCreate() {
        Log.i(TAG,"onCreate() - > PolygonInsideOrOutsideService")
        super.onCreate()

        //start a separate thread and start listening to your network object
    }

    override fun onDestroy() {
        Log.i(TAG,"Problem: service destroy it couldn't happen")
        super.onDestroy()
    }



    fun findFollowersConnection() {
        Log.i(TAG, "findFollowersConnection, current user id : " + FirebaseAuth.getInstance().currentUser!!.uid)
        var currentUser = FirebaseAuth.getInstance().currentUser
        val reference = FirebaseDatabase.getInstance().reference
        findFollowersUser(reference, currentUser!!)
        findFollowingUser(reference, currentUser!!)
    }

    /**
     * listener for followers in database,  this method run listener for location change in database and add to recycler view followers user
     * @param reference
     * @param currentUser
     */
    private fun findFollowersUser(reference: DatabaseReference, currentUser: FirebaseUser) {
        val query = reference.child("followers")
                .orderByKey()
                .equalTo(currentUser!!.uid)
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (singleSnapshot in dataSnapshot.children) {
                    for (childSingleSnapshot in singleSnapshot.children) {
                        var userFollowers = childSingleSnapshot.child("user").getValue(User::class.java)
                        Log.i(TAG, "value followers: " + userFollowers!!.user_id + " " + userFollowers.email)
                        loadLocationsFromDatabaseForGivenUserId(userFollowers.user_id!!)
//                        if(transition == Geofence.GEOFENCE_TRANSITION_ENTER ){
//                            saveToDatabaseNotificationsToAnotherDevice(userFollowers.user_id!!,currentUser.uid)
//                        }else if(transition == Geofence.GEOFENCE_TRANSITION_EXIT){
//                            removeValueFromDatabaseNotifications(userFollowers.user_id!!,currentUser.uid)
//                        }
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
    private fun findFollowingUser(reference: DatabaseReference, currentUser: FirebaseUser) {
        val query = reference.child("following")
                .orderByKey()
                .equalTo(currentUser!!.uid)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (singleSnapshot in dataSnapshot.children) {
                    for (childSingleSnapshot in singleSnapshot.children) {
                        var userFollowing = childSingleSnapshot.child("user").getValue(User::class.java)
                        Log.i(TAG, "value following: " + userFollowing!!.user_id + " " + userFollowing!!.email)
                        loadLocationsFromDatabaseForGivenUserId(userFollowing.user_id!!)
//                        if(transition == Geofence.GEOFENCE_TRANSITION_ENTER ){
//                            saveToDatabaseNotificationsToAnotherDevice(userFollowing.user_id!!,currentUser.uid)
//                        }else if(transition == Geofence.GEOFENCE_TRANSITION_EXIT){
//                            removeValueFromDatabaseNotifications(userFollowing.user_id!!, currentUser.uid)
//                        }
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
    fun loadLocationsFromDatabaseForGivenUserId(userId: String) {
        Log.i(TAG, "loadLocationsFromDatabaseForGivenUserId")
        var locations = FirebaseDatabase.getInstance().getReference("Locations")
        var query: Query = locations.orderByChild("user_id").equalTo(userId)
        //addValueEventListeners The listener is triggered once for the initial state of the data and again anytime the data changes
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                Log.i(TAG, "onDataChange in Locations listener " + dataSnapshot.toString())
                for (singleSnapshot in dataSnapshot!!.children) {
                    var userWhoChangeLocation = singleSnapshot.getValue(TrackingModel::class.java)
                    Log.i(TAG,"usser who change location : " + userWhoChangeLocation!!.email + " location " + userWhoChangeLocation!!.lat + " " + userWhoChangeLocation!!.lng )
                    var userLocation = createLocationVariable(LatLng(userWhoChangeLocation!!.lat!!.toDouble(),userWhoChangeLocation!!.lng!!.toDouble()))
                    var userIdToSendNotification = userWhoChangeLocation.user_id
                    var currentUser = FirebaseAuth.getInstance().currentUser
                    getPolygonFromDatabase(userLocation, userIdToSendNotification!!, currentUser!!.uid)
                }
                if (dataSnapshot.value == null) {//nothing found
                    Log.i(TAG, "nothing found in onDataChange")
                }
            }
            override fun onCancelled(databaseError: DatabaseError?) {}
        })
    }
    private fun createLocationVariable(userLatLng: LatLng): Location {
        var userLoc = Location("")
        userLoc.latitude = userLatLng.latitude
        userLoc.longitude = userLatLng.longitude
        return userLoc
    }
    private val ENTER = 1
    private val EXIT = 2
    private fun getPolygonFromDatabase(userLocation: Location,userIdToSendNotification: String, currentUserId: String){
        var databaseReference = FirebaseDatabase.getInstance().getReference("user_polygons")
        var currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {//prevent if user click logout
            var polygonsLatLngMap : HashMap<String, ArrayList<LatLng>> = HashMap()
            var query: Query = databaseReference.orderByKey().equalTo(currentUser.uid)
            query.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot?) {
                    for (singleSnapshot in dataSnapshot!!.children) {
                        for(child in singleSnapshot.children){
                            var polygonsFromDbMap   = child.getValue(PolygonModel::class.java)
                            Log.i(TAG,polygonsFromDbMap!!.tag + " " + polygonsFromDbMap!!.polygonLatLngList)

                            var newList : ArrayList<LatLng> = changePolygonModelWithMyOwnLatLngListToLatLngList(polygonsFromDbMap)

                            polygonsLatLngMap.put(polygonsFromDbMap!!.tag!!, newList)
                        }
                    }
                    Log.i(TAG,"Check if given location: $userLocation is in polygon")
                    var insideOrOutsideArea = InsideOrOutsideArea(this@PolygonInsideOrOutsideService, userLocation!!)
                    var listOfIsInArea = insideOrOutsideArea.isPointInsidePolygon(polygonsLatLngMap)
                    Log.i(TAG, listOfIsInArea.toString())
                    listOfIsInArea
                            .filter { it == ENTER || it == EXIT }
                            .forEach { notification(it,userIdToSendNotification,currentUserId) }

                    if (dataSnapshot.value == null) {//nothing found
                        Log.i(TAG, "nothing found in onDataChange")
                    }
                }

                override fun onCancelled(p0: DatabaseError?) {}
            })
        }
    }
    private fun changePolygonModelWithMyOwnLatLngListToLatLngList(polygonsFromDbMap : PolygonModel) : ArrayList<LatLng>{
        var newList : ArrayList<LatLng> = ArrayList(polygonsFromDbMap!!.polygonLatLngList!!.size)
        polygonsFromDbMap!!.polygonLatLngList!!.mapTo(newList) { LatLng(it.latitude!!, it.longitude!!) }
        return newList
    }
    private fun notification(transition : Int,userIdToSend : String, userIdFromSend : String ){
        if(transition == Geofence.GEOFENCE_TRANSITION_ENTER ){
            saveToDatabaseNotificationsToAnotherDevice(userIdFromSend,userIdToSend)
        }else if(transition == Geofence.GEOFENCE_TRANSITION_EXIT){
            removeValueFromDatabaseNotifications( userIdFromSend,userIdToSend)
        }
    }
    fun saveToDatabaseNotificationsToAnotherDevice(userIdToSendNotification: String, currentUserId: String){

        var notificationData : HashMap<String, String> = HashMap()
        Log.i(TAG, "robie mape z wartoci " + currentUserId)
        notificationData.put("from", currentUserId)
        notificationData.put("type","request")

        addNotificationIfUserNotExistInDatabase(currentUserId,userIdToSendNotification,notificationData)
    }
    private fun addNotificationIfUserNotExistInDatabase(userIdToSendNotification: String, currentUserId: String, notificationData : HashMap<String,String>){
        var notificationsDatabase = FirebaseDatabase.getInstance().reference.child("notifications")
        notificationsDatabase.child(currentUserId).orderByChild("from").equalTo(userIdToSendNotification).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                if(!dataSnapshot!!.exists()){
                    notificationsDatabase.child(currentUserId).push().setValue(notificationData)
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
}