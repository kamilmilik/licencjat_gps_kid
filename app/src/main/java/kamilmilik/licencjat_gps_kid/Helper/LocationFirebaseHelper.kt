package kamilmilik.licencjat_gps_kid.Helper

import android.location.Location
import android.util.Log
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kamilmilik.licencjat_gps_kid.models.TrackingModel
import java.text.DecimalFormat

/**
 * Created by kamil on 25.02.2018.
 */
class LocationFirebaseHelper(var mGoogleMap: GoogleMap) {
    private val TAG : String = LocationFirebaseHelper::class.java.simpleName


    fun addCurrentUserLocationToFirebase(lastLocation: Location) {
        Log.i(TAG, "addCurrentUserLocationToFirebase")
        var locations = FirebaseDatabase.getInstance().getReference("Locations")
        var currentUser = FirebaseAuth.getInstance().currentUser

        locations.child(currentUser!!.uid)
                .setValue(TrackingModel(currentUser.uid,
                        currentUser!!.email!!,
                        lastLocation.latitude.toString(),
                        lastLocation.longitude.toString()))
    }
    fun listenerForLocationsChangeInFirebase(followingUserId : String){
        loadLocationsFromDatabaseForCurrentUser(followingUserId)
    }

    var markersMap = HashMap<String, Marker>()
    var currentUserLocation = Location("")
    fun loadLocationsFromDatabaseForCurrentUser(userId: String){
        Log.i(TAG,"loadLocationsFromDatabaseForCurrentUser")
        var locations = FirebaseDatabase.getInstance().getReference("Locations")
        var query : Query = locations.orderByChild("userId").equalTo(userId)
        
        //addValueEventListeners The listener is triggered once for the initial state of the data and again anytime the data changes
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                for(singleSnapshot in dataSnapshot!!.children){
                    var userWhoChangeLocation = singleSnapshot.getValue(TrackingModel::class.java)
                    var locationOfTheUserWhoChangeLocation = LatLng(userWhoChangeLocation.lat!!.toDouble(), userWhoChangeLocation.lng!!.toDouble())

                    removeOldMarker(userId)
                    if(userId == FirebaseAuth.getInstance().currentUser!!.uid){
                        currentUserLocation = createLocationVariable(userWhoChangeLocation)
                        var currentUser = FirebaseAuth.getInstance().currentUser
                        var currentMarker = mGoogleMap!!.addMarker(MarkerOptions()
                                    .position(LatLng(currentUserLocation.latitude!!, currentUserLocation.longitude!!))
                                    .title(currentUser!!.email))
                        markersMap.put(userId, currentMarker)
                        updateMarkerSnippetDistance(userId,currentUserLocation)

                        mGoogleMap!!.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(currentUserLocation.latitude, currentUserLocation.longitude),12.0f))
                    }else{
                        var location = createLocationVariable(locationOfTheUserWhoChangeLocation)
                        var firstDistanceSecondMeasure = calculateDistanceBetweenTwoPoints(currentUserLocation,location)
                        Log.i(TAG,"ustawiam marker obserwowanego na pozycje : " + locationOfTheUserWhoChangeLocation + " dla " + userWhoChangeLocation.email)
                        var markerFollowingUser = mGoogleMap!!.addMarker(MarkerOptions()
                                .position(locationOfTheUserWhoChangeLocation)
                                .title(userWhoChangeLocation.email)
                                .snippet("Distance " + DecimalFormat("#.#").format(firstDistanceSecondMeasure.first) + firstDistanceSecondMeasure.second)
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)))
                        mGoogleMap!!.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(locationOfTheUserWhoChangeLocation.latitude, locationOfTheUserWhoChangeLocation.longitude),12.0f))
                        markersMap.put(userId, markerFollowingUser)
                    }

                }

                if(dataSnapshot.value == null){//nothing found
                    Log.i(TAG,"nothing found in onDataChange")
                }

            }

            override fun onCancelled(databaseError: DatabaseError?) {}
        })
    }
    private fun removeOldMarker(userId : String){
        if(markersMap.containsKey(userId)){
            markersMap.getValue(userId).remove()
        }
    }
    private fun createLocationVariable(userLatLng : TrackingModel) : Location{
        var userLoc = Location("")
        userLoc.latitude = userLatLng.lat!!.toDouble()
        userLoc.longitude = userLatLng.lng!!.toDouble()
        return userLoc
    }
    private fun createLocationVariable(userLatLng : LatLng) : Location{
        var userLoc = Location("")
        userLoc.latitude = userLatLng.latitude
        userLoc.longitude = userLatLng.longitude
        return userLoc
    }    private fun calculateDistanceBetweenTwoPoints(currentUserLocation: Location , followingUserLoc: Location ) : Pair<Float,String>{
        var measure : String?
        var distance : Float = currentUserLocation.distanceTo(followingUserLoc)
        if (distance > 1000){
            distance = (distance / 1000)
            measure = "km"
        }else {
            measure = "m"
        }

        return Pair(distance, measure)
    }
    private fun updateMarkerSnippetDistance(userIdToAvoidUpdate: String, currentUserLocation: Location){
        for((key,value) in markersMap){
            if(key != userIdToAvoidUpdate){
                var location  = Location("")
                location.latitude = value.position.latitude
                location.longitude = value.position.longitude
                var firstDistanceSecondMeasure = calculateDistanceBetweenTwoPoints(currentUserLocation, location)
                value.hideInfoWindow()
                value.snippet = "Distance " + DecimalFormat("#.#").format(firstDistanceSecondMeasure.first) + firstDistanceSecondMeasure.second
            }
        }
    }
}