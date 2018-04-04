package kamilmilik.licencjat_gps_kid.Helper

import android.content.Context
import android.location.Location
import android.util.Log
import com.firebase.jobdispatcher.JobParameters
import com.firebase.jobdispatcher.JobService
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import kamilmilik.licencjat_gps_kid.Constants
import kamilmilik.licencjat_gps_kid.Helper.PolygonOperation.InsideOrOutsideArea
import kamilmilik.licencjat_gps_kid.models.PolygonModel
import kamilmilik.licencjat_gps_kid.models.TrackingModel
import kamilmilik.licencjat_gps_kid.models.User

/**
 * Created by kamil on 17.03.2018.
 */
class Notification(var context: Context, var jobService : JobService, var job : JobParameters){
    private var TAG = Notification::class.java.simpleName

    fun notificationAction() {
        Log.i(TAG,"notificationAction() job: " + job.toString() + " job servcie " + jobService )
        Log.i(TAG, "notificationAction, current user id : " + FirebaseAuth.getInstance().currentUser!!.uid)
        var currentUser = FirebaseAuth.getInstance().currentUser
        val reference = FirebaseDatabase.getInstance().reference
        findFollowersUser(reference, currentUser!!)
        findFollowingUser(reference, currentUser!!)
    }

    /**
     * listener for followers in database,  this method run listener for locationOfUserWhoChangeIt change in database and load locations from database for given user Id
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
     * listener for following in database,  this method run listener for locationOfUserWhoChangeIt change in database and load locations from database for given user Id
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

    /**
     * load locations for given user id from database
     * @param userId
     */
    private fun loadLocationsFromDatabaseForGivenUserId(userId: String) {
        Log.i(TAG, "loadLocationsFromDatabaseForGivenUserId")
        var locations = FirebaseDatabase.getInstance().getReference("Locations")
        var query: Query = locations.orderByChild("user_id").equalTo(userId)
        //addValueEventListeners The listener is triggered once for the initial state of the data and again anytime the data changes
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                Log.i(TAG, "onDataChange in Locations listener " + dataSnapshot.toString())
                for (singleSnapshot in dataSnapshot!!.children) {
                    var userWhoChangeLocation = singleSnapshot.getValue(TrackingModel::class.java)
                    Log.i(TAG,"usser who change locationOfUserWhoChangeIt : " + userWhoChangeLocation!!.email + " locationOfUserWhoChangeIt " + userWhoChangeLocation!!.lat + " " + userWhoChangeLocation!!.lng )
                    var locationOfUserWhoChangeIt = createLocationVariable(LatLng(userWhoChangeLocation!!.lat!!.toDouble(),userWhoChangeLocation!!.lng!!.toDouble()))
                    var userIdToSendNotification = userWhoChangeLocation.user_id
                    var currentUser = FirebaseAuth.getInstance().currentUser
                    getPolygonFromDatabase(locationOfUserWhoChangeIt, userIdToSendNotification!!, currentUser!!.uid)
                }
                if (dataSnapshot.value == null) {//nothing found
                    Log.i(TAG, "nothing found in onDataChange")
                }
            }
            override fun onCancelled(databaseError: DatabaseError?) {}
        })
    }

    /**
     * change LatLng locationOfUserWhoChangeIt to Location
     * @param userLatLng
     */
    private fun createLocationVariable(userLatLng: LatLng): Location {
        var userLoc = Location("")
        userLoc.latitude = userLatLng.latitude
        userLoc.longitude = userLatLng.longitude
        return userLoc
    }

    /**
     * get polygon lat lng from database and check if given user is inside area or outside
     * @param locationOfUserWhoChangeIt it is a locationOfUserWhoChangeIt of user who change locationOfUserWhoChangeIt
     * @param userIdToSendNotification user id where we send notification
     * @param currentUserId user id who send this notification
     */
    private fun getPolygonFromDatabase(locationOfUserWhoChangeIt: Location, userIdToSendNotification: String, currentUserId: String){
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
                    Log.i(TAG,"Check if given locationOfUserWhoChangeIt: $locationOfUserWhoChangeIt is in polygon")
                    if(!polygonsLatLngMap.isEmpty()){
                        Log.i(TAG,"isInArea in polygon?")
                        var insideOrOutsideArea = InsideOrOutsideArea(context, locationOfUserWhoChangeIt!!)
                        var listOfIsInArea = insideOrOutsideArea.isPointInsidePolygon(polygonsLatLngMap)
                        Log.i(TAG, listOfIsInArea.toString())
//                        listOfIsInArea
//                                .filter { it == Constants.ENTER || it == Constants.EXIT }
//                                .forEach { notification(it,userIdToSendNotification,currentUserId) }
                        for(it in listOfIsInArea){
                            if(it == Constants.ENTER || it == Constants.EXIT  ){
                                notification(it,userIdToSendNotification,currentUserId)
                            }else{
                                Log.i(TAG,"finish job in else block user not change polygon action")
                                jobService.jobFinished(job,false)
                            }
                        }
                    }else{
                        Log.i(TAG,"finish job in else block")
                        jobService.jobFinished(job,false)
                    }

                    if (dataSnapshot.value == null) {//nothing found
                        Log.i(TAG,"finish job if nothing found jobService " + jobService.toString() + " job " + job.toString())
                        jobService.jobFinished(job,false)
                        Log.i(TAG, "nothing found in onDataChange")
                    }
                }

                override fun onCancelled(p0: DatabaseError?) {}
            })
        }
    }

    /**
     * convert given polygon map object (PolygonModel) with MyOwnLAtLng to polygon map object with LatLng
     * @param polygonsFromDbMap given polygon map model (model with tag and arrayList<MyOwnLatLng)
     */
    private fun changePolygonModelWithMyOwnLatLngListToLatLngList(polygonsFromDbMap : PolygonModel) : ArrayList<LatLng>{
        var newList : ArrayList<LatLng> = ArrayList(polygonsFromDbMap!!.polygonLatLngList!!.size)
        polygonsFromDbMap!!.polygonLatLngList!!.mapTo(newList) { LatLng(it.latitude!!, it.longitude!!) }
        return newList
    }

    /**
     * it save to database notification data or remove notification data
     * @param transition Polygon Area Status
     * @param userIdToSend user id where notification will be send
     * @param userIdFromSend user who send this notification
     */
    private fun notification(transition : Int,userIdToSend : String, userIdFromSend : String ){
        if(transition == Constants.ENTER ){
            saveToDatabaseNotificationsToAnotherDevice(userIdFromSend,userIdToSend)
        }else if(transition == Constants.EXIT){
            removeValueFromDatabaseNotifications( userIdFromSend,userIdToSend)
        }
    }

    /**
     * save notification data as HashMap to database
     * @param userIdToSendNotification
     * @param currentUserId
     */
    private fun saveToDatabaseNotificationsToAnotherDevice(userIdToSendNotification: String, currentUserId: String){
        Log.i(TAG, "saveToDatabaseNotificationsToAnotherDevice")
        var notificationData : HashMap<String, String> = HashMap()
        notificationData.put("from", currentUserId)
        notificationData.put("type","request")

        addNotificationIfUserNotExistInDatabase(currentUserId,userIdToSendNotification,notificationData)
    }

    /**
     * add to database notification data if it wasn't added before
     */
    private fun addNotificationIfUserNotExistInDatabase(userIdToSendNotification: String, currentUserId: String, notificationData : HashMap<String,String>){
        var notificationsDatabase = FirebaseDatabase.getInstance().reference.child("notifications")
        notificationsDatabase.child(currentUserId).orderByChild("from").equalTo(userIdToSendNotification).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                if(!dataSnapshot!!.exists()){
                    notificationsDatabase.child(currentUserId).push().setValue(notificationData){
                        databaseError, databaseReference -> jobService.jobFinished(job,false)
                    }
                }
            }
            override fun onCancelled(databaseError: DatabaseError?) {}
        })
    }
    private fun removeValueFromDatabaseNotifications(userIdToDelete : String, currentUserId: String){
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
                    notificationsDatabase.child(userIdToDelete).child(snapshot.key).setValue(map) {
                        databaseError, databaseReference -> jobService.jobFinished(job,false)
                    }
                }
            }
            override fun onCancelled(databaseError: DatabaseError?) {}
        })
    }
}