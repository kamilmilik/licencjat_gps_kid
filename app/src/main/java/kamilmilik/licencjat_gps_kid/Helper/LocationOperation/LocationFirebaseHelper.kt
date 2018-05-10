package kamilmilik.licencjat_gps_kid.Helper.LocationOperation

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.location.Location
import android.util.Log
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kamilmilik.licencjat_gps_kid.Helper.OnMarkerAddedCallback
import kamilmilik.licencjat_gps_kid.models.*
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*


/**
 * Created by kamil on 25.02.2018.
 */
class LocationFirebaseHelper(var mGoogleMap: GoogleMap, var context: Context) {
    private val TAG: String = LocationFirebaseHelper::class.java.simpleName

    var currentMarkerPosition: LatLng? = null
    var markersMap = HashMap<UserMarkerInformationModel, Marker>()
    var currentUserLocation = Location("")

    var userWhoChangeLocation : TrackingModel? = null
    fun addCurrentUserMarkerAndRemoveOld(lastLocation: Location, onMarkerAddedCallback: OnMarkerAddedCallback) {
        Log.i(TAG, "addCurrentUserMarkerAndRemoveOld")
//        var locations = FirebaseDatabase.getInstance().getReference("Locations")
        var currentUser = FirebaseAuth.getInstance().currentUser
        var locations = FirebaseDatabase.getInstance().getReference("Locations")

        if (currentUser != null) {//prevent if user click logout to not update locationOfUserWhoChangeIt
//            it should be method which add location to db but It is done in LocationUpdateService
                            var userMarkerInformation = UserMarkerInformationModel(currentUser.email!!, currentUser!!.displayName!!)
                            locations.child(currentUser!!.uid)
                                    .setValue(TrackingModel(currentUser.uid,
                                            currentUser!!.email!!,
                                            lastLocation.latitude.toString(),
                                            lastLocation.longitude.toString(),
                                            currentUser!!.displayName!!,
                                            System.currentTimeMillis()))


                            removeOldMarker(userMarkerInformation)
                            var currentMarker = mGoogleMap!!.addMarker(MarkerOptions()
                                    .position(LatLng(lastLocation.latitude!!, lastLocation.longitude!!))
                                    .title(currentUser!!.displayName))
                            markersMap.put(userMarkerInformation, currentMarker)
                            //mGoogleMap!!.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(lastLocation.latitude, lastLocation.longitude), 12.0f))
                            currentUserLocation = createLocationVariable(currentMarker.position)

                            updateMarkerSnippetDistance(userMarkerInformation, currentUserLocation)

                            currentMarkerPosition = currentMarker.position


                            onMarkerAddedCallback.myLocationMarkerAddedListener(true)
                            onMarkerAddedCallback.onMarkerAddedListener()

        }
    }

    fun listenerForLocationsChangeInFirebase(followingUserId: String, onMarkerAddedCallback: OnMarkerAddedCallback, progressBar: ProgressDialog) {
        loadLocationsFromDatabaseForGivenUserId(followingUserId, onMarkerAddedCallback, progressBar)
    }

    var previousUserMarkerInformation : UserMarkerInformationModel? = null
    // /TODO tu chyba dac listener kiedy dodajemy usera bo teraz jest java.util.NoSuchElementException: Key ewacwieka@interia.pl is missing in the map. kiedy dodaje nowego usera bo on dodaje lokalizacje a jej nie zmienia chyba
    fun loadLocationsFromDatabaseForGivenUserId(followingUserId: String, onMarkerAddedCallback: OnMarkerAddedCallback, progressBar: ProgressDialog) {
        Log.i(TAG, "loadLocationsFromDatabaseForGivenUserId")
        //progressBar.visibility = View.VISIBLE

        var locations = FirebaseDatabase.getInstance().getReference("Locations")
        var query: Query = locations.orderByChild("user_id").equalTo(followingUserId)

        //addValueEventListeners The listener is triggered once for the initial state of the data and again anytime the data changes
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                Log.i(TAG, "onDataChange in Locations listener " + dataSnapshot.toString())
                for (singleSnapshot in dataSnapshot!!.children) {
                    userWhoChangeLocation = singleSnapshot.getValue(TrackingModel::class.java)
                    if(userWhoChangeLocation != null && FirebaseAuth.getInstance().currentUser != null){
                        onMarkerAddedCallback.updateChangeUserNameInRecycler(UserMarkerInformationModel(userWhoChangeLocation!!.email, userWhoChangeLocation!!.user_name!!))
                        //TODO dac jakas metode czy cos ze to aktualizuje user name jak go zmieni
                        //update following or followers user name if change
                        FirebaseDatabase.getInstance().getReference("followers").orderByKey()
                                .equalTo(userWhoChangeLocation!!.user_id )
                                .addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onCancelled(p0: DatabaseError?) {}
                                    override fun onDataChange(dataSnapshot: DataSnapshot?) {
                                        Log.i(TAG,"onDataChange() sprawdze w followers usera " + userWhoChangeLocation!!.user_id)
                                        for (singleSnapshot in dataSnapshot!!.children) {
                                            for (childSingleSnapshot in singleSnapshot.children) {
                                                var user = childSingleSnapshot.child("user").getValue(User::class.java)
                                                if(user!!.user_id == FirebaseAuth.getInstance().currentUser!!.uid) {
                                                    Log.i(TAG, "onDataChange() update name" + childSingleSnapshot)
                                                    var map = HashMap<String, Any>() as MutableMap<String, Any>
                                                    map.put("user_name", FirebaseAuth.getInstance().currentUser!!.displayName!!)
                                                    childSingleSnapshot!!.ref.child("user").updateChildren(map)
                                                }
                                            }
                                        }
                                    }
                                })
                        FirebaseDatabase.getInstance().getReference("following").orderByKey()
                                .equalTo(FirebaseAuth.getInstance().currentUser!!.uid )
                                .addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onCancelled(p0: DatabaseError?) {}
                                    override fun onDataChange(dataSnapshot: DataSnapshot?) {
                                        Log.i(TAG,"onDataChange() sprawdze w following usera " + FirebaseAuth.getInstance().currentUser!!.uid)
                                        for (singleSnapshot in dataSnapshot!!.children) {
                                            for (childSingleSnapshot in singleSnapshot.children) {
                                                var user = childSingleSnapshot.child("user").getValue(User::class.java)
                                                if(user!!.user_id == userWhoChangeLocation!!.user_id) {
                                                    Log.i(TAG, "onDataChange() update name" + childSingleSnapshot)
                                                    var map = HashMap<String, Any>() as MutableMap<String, Any>
                                                    map.put("user_name", userWhoChangeLocation!!.user_name!!)
                                                    childSingleSnapshot!!.ref.child("user").updateChildren(map)
                                                }
                                            }
                                        }
                                    }
                                })

                        var userMarkerInformation = UserMarkerInformationModel(userWhoChangeLocation!!.email, userWhoChangeLocation!!.user_name!!)
                        var locationOfTheUserWhoChangeLocation = LatLng(userWhoChangeLocation!!.lat!!.toDouble(), userWhoChangeLocation!!.lng!!.toDouble())

                        //TODO tutaj jakos usuwac po mailu? jeśli jest już w mapie to go usun
                        for((key, value) in markersMap){
                            if(key.email == userMarkerInformation.email){
                                markersMap[key]!!.remove()
                            }
                        }
                        //TODO czy to potrzebne ?
    //                        removeOldMarker(userMarkerInformation!!)
                        var location = createLocationVariable(locationOfTheUserWhoChangeLocation)
                        var firstDistanceSecondMeasure = calculateDistanceBetweenTwoPoints(currentUserLocation, location)
                        Log.i(TAG, "ustawiam marker obserwowanego na pozycje : " + locationOfTheUserWhoChangeLocation + " dla " + userWhoChangeLocation!!.email)
                        var markerFollowingUser = mGoogleMap!!.addMarker(MarkerOptions()
                                .position(locationOfTheUserWhoChangeLocation)
                                .title(userWhoChangeLocation!!.user_name)
                                .snippet("Distance " + DecimalFormat("#.#").format(firstDistanceSecondMeasure.first) + firstDistanceSecondMeasure.second
                                + " Location report " +  SimpleDateFormat("MMM dd,yyyy HH:mm").format(Date(userWhoChangeLocation!!.time!!)))
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)))
                        //mGoogleMap!!.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(locationOfTheUserWhoChangeLocation.latitude, locationOfTheUserWhoChangeLocation.longitude), 12.0f))
                        Log.i(TAG, "put to markersMap " + userWhoChangeLocation!!.email)
                        markersMap.put(userMarkerInformation, markerFollowingUser)
                        previousUserMarkerInformation = userMarkerInformation
                        onMarkerAddedCallback.userConnectionMarkerAddedListener(true)
                        onMarkerAddedCallback.onMarkerAddedListener()

                    }
                }

                if (dataSnapshot.value == null) {//nothing found
                    Log.i(TAG, "nothing found in onDataChange")
                }

            }

            override fun onCancelled(databaseError: DatabaseError?) {}
        })
    }

    private fun removeOldMarker(userMarkerInformation: UserMarkerInformationModel) {
        if (markersMap.containsKey(userMarkerInformation)) {
            markersMap.getValue(userMarkerInformation).remove()
        }
    }

    private fun createLocationVariable(userLatLng: LatLng): Location {
        var userLoc = Location("")
        userLoc.latitude = userLatLng.latitude
        userLoc.longitude = userLatLng.longitude
        return userLoc
    }

    private fun calculateDistanceBetweenTwoPoints(currentUserLocation: Location, followingUserLoc: Location): Pair<Float, String> {
        var measure: String?
        var distance: Float = currentUserLocation.distanceTo(followingUserLoc)
        if (distance > 1000) {
            distance = (distance / 1000)
            measure = "km"
        } else {
            measure = "m"
        }
        return Pair(distance, measure)
    }

    private fun updateMarkerSnippetDistance(userToAvoidUpdate: UserMarkerInformationModel, currentUserLocation: Location) {
        for ((key, value) in markersMap) {
            if (key != userToAvoidUpdate) {
                var location = Location("")
                location.latitude = value.position.latitude
                location.longitude = value.position.longitude
                var firstDistanceSecondMeasure = calculateDistanceBetweenTwoPoints(currentUserLocation, location)
                value.hideInfoWindow()
                value.snippet = "Distance " + DecimalFormat("#.#").format(firstDistanceSecondMeasure.first) + firstDistanceSecondMeasure.second + " Location report " + SimpleDateFormat("MMM dd,yyyy HH:mm").format(Date(userWhoChangeLocation!!.time!!))
            }
        }
    }

    fun goToThisMarker(clickedUserMarkerInformation: UserMarkerInformationModel) {
        Log.i(TAG,"goToThisMarker() clicked user email " + clickedUserMarkerInformation)
        var searchedMarker = findMarker(clickedUserMarkerInformation)
        mGoogleMap!!.animateCamera(CameraUpdateFactory.newLatLngZoom(searchedMarker!!.position, 12.0f))
        searchedMarker.showInfoWindow()
    }


    private fun findMarker(searchedUserMarkerInformationKey: UserMarkerInformationModel): Marker? {
        for ((key, value) in markersMap) {
            Log.i(TAG,"findMarker() " + key.userName + " " + key.email + " input " + searchedUserMarkerInformationKey.email + " " + searchedUserMarkerInformationKey.userName)
            Log.i(TAG,"findMarker() hash " + key.hashCode() + " " + searchedUserMarkerInformationKey.hashCode())
            Log.i(TAG,"findMarker() equals " + (key == searchedUserMarkerInformationKey))
        }
        return markersMap.getValue(searchedUserMarkerInformationKey)
    }
}