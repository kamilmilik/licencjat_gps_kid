package kamilmilik.licencjat_gps_kid.Utils

import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.location.Location
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.google.maps.android.PolyUtil
import kamilmilik.licencjat_gps_kid.Helper.LocationFirebaseHelper
import kamilmilik.licencjat_gps_kid.models.TrackingModel
import kamilmilik.licencjat_gps_kid.models.User
import java.lang.reflect.Type

/**
 * Created by kamil on 12.03.2018.
 */
class LocationUpdateService : IntentService{
    private val TAG  = LocationUpdateService::class.java.simpleName
    init {
        Log.i(TAG,"hejkaaa")
    }
    constructor() : super("LocationUpdateService")
    var location : Location? = null
    override fun onHandleIntent(intent: Intent?) {
        if (LocationResult.hasResult(intent)) {
            val locationResult = LocationResult.extractResult(intent)
            location = locationResult.lastLocation
            if (location != null) {
                addCurrentUserLocationToFirebase(location!!)
                Log.d("locationtesting", "accuracy: " + location!!.getAccuracy() + " lat: " + location!!.getLatitude() + " lon: " + location!!.getLongitude())
            }
        }
    }
    fun addCurrentUserLocationToFirebase(lastLocation: Location) {
        Log.i(TAG, "addCurrentUserLocationToFirebase")
        var locations = FirebaseDatabase.getInstance().getReference("Locations")
        var currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {//prevent if user click logout to not update location
            locations.child(currentUser!!.uid)
                    .setValue(TrackingModel(currentUser.uid,
                            currentUser!!.email!!,
                            lastLocation.latitude.toString(),
                            lastLocation.longitude.toString()))
            getPolygonFromDatabase()
        }
    }


    //database
    private fun getPolygonFromDatabase(){
        var databaseReference = FirebaseDatabase.getInstance().getReference("user_polygons")
        var currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {//prevent if user click logout
            var polygonsLatLngMap : HashMap<String, ArrayList<LatLng>> = HashMap()
            var query: Query = databaseReference.orderByKey().equalTo(currentUser.uid)
            query.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot?) {
                    for (singleSnapshot in dataSnapshot!!.children) {
                        for(child in singleSnapshot.children){
                            var polygonsFromDbMap   = child.getValue(LocationFirebaseHelper.Test::class.java)
                            Log.i(TAG,polygonsFromDbMap!!.tag + " " + polygonsFromDbMap!!.list)
                            var newList : ArrayList<LatLng> = ArrayList(polygonsFromDbMap!!.list!!.size)
                            polygonsFromDbMap!!.list!!.mapTo(newList) { LatLng(it.latitude!!, it.longitude!!) }

                            polygonsLatLngMap.put(polygonsFromDbMap!!.tag!!, newList)
                        }
                    }
                    var listOfIsInArea = isPointInsidePolygon(polygonsLatLngMap)
                    Log.i(TAG, listOfIsInArea.toString())
                    for(transition in listOfIsInArea){
                        if(transition == ENTER || transition == EXIT){
                            findFollowersConnection(transition)
                        }
                    }
                    if (dataSnapshot.value == null) {//nothing found
                        Log.i(TAG, "nothing found in onDataChange")
                    }
                }

                override fun onCancelled(p0: DatabaseError?) {}
            })
        }
    }
    private fun isPointInsidePolygon(polygonsMap : HashMap<String, ArrayList<LatLng>>) : ArrayList<Int>{
        var listOfIsInArea: ArrayList<Int> = ArrayList()
        for(polygon in polygonsMap){
            listOfIsInArea.add(isInArea(polygon.key, polygon.value))
        }
        return listOfIsInArea
    }
    //---inside or outside area
    private val STILL_OUTSIDE_OR_INSIDE = 0
    private val ENTER = 1
    private val EXIT = 2
   // private var isInAreaPreviousMap: HashMap<String, Boolean> = HashMap()
    private var isInArea: Boolean? = null
    private fun isInArea(polygonKey: String, polygonPoints: ArrayList<LatLng>): Int {
        isInArea = PolyUtil.containsLocation(LatLng(location!!.latitude,location!!.longitude), polygonPoints, false)
        var isInAreaPreviousMap: HashMap<String, Boolean> = getValueFromSharedPreferences()
        var previousValueInMap = isInAreaPreviousMap[polygonKey]
        Log.i(TAG, "polygonKey: $polygonKey")
        Log.i(TAG, " previous $previousValueInMap isInArea $isInArea")
        isInAreaPreviousMap.put(polygonKey, isInArea!!)
        writeValueToSharedPreferences(isInAreaPreviousMap)
        if (previousValueInMap == null) {
            if (isInArea == true) {
                return ENTER
            } else if (isInArea == false) {//if user isn't in area not push notification
                return STILL_OUTSIDE_OR_INSIDE
            }
        }
        if (previousValueInMap == isInArea) {
            return STILL_OUTSIDE_OR_INSIDE
        } else if (previousValueInMap == false && isInArea == true) {
            return ENTER
        } else if (previousValueInMap == true && isInArea == false) {
            return EXIT
        }

        return STILL_OUTSIDE_OR_INSIDE
    }
    private fun getValueFromSharedPreferences() : HashMap<String,Boolean>{
        val sharedPref = getSharedPreferences("shared",Context.MODE_PRIVATE)
        val jsonString = sharedPref.getString("map","")
        if(!jsonString.equals("")){
            var type : Type =  object : TypeToken<HashMap<String,Boolean>>() {}.type
            var map : HashMap<String,Boolean> =  Gson().fromJson<HashMap<String,Boolean>>(jsonString, type)
            return map
        }
        return HashMap()
    }
    private fun writeValueToSharedPreferences(isInAreaPreviousMap: HashMap<String, Boolean>){
        val sharedPref = getSharedPreferences("shared",Context.MODE_PRIVATE) ?: return
        var builder =  GsonBuilder()
        var gson = builder.enableComplexMapKeySerialization().setPrettyPrinting().create()
        var type : Type =  object : TypeToken<HashMap<String,Boolean>>() {}.type
        var json = gson.toJson(isInAreaPreviousMap, type);
        with (sharedPref.edit()) {
            putString("map",json)
            commit()
        }
    }
    //notyfications

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

}