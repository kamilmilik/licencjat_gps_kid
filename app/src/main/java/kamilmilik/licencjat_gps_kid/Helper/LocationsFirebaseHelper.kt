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
class LocationsFirebaseHelper(var mGoogleMap: GoogleMap) {
    private val TAG : String = LocationsFirebaseHelper::class.java.simpleName


     fun addCurrentUserLocationToFirebase(lastLocation : Location){
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
    var followingUserLoc = Location("")
    var currentUserLocation = Location("")
    fun loadLocationsFromDatabaseForCurrentUser(userId: String){
        Log.i(TAG,"loadLocationsFromDatabaseForCurrentUser")
        var locations = FirebaseDatabase.getInstance().getReference("Locations")
        var query : Query = locations.orderByChild("userId").equalTo(userId)
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                for(singleSnapshot in dataSnapshot!!.children){
                    var user = singleSnapshot.getValue(TrackingModel::class.java)
                    var userLocation = LatLng(user.lat!!.toDouble(), user.lng!!.toDouble())

                    if(userId == FirebaseAuth.getInstance().currentUser!!.uid){
                        currentUserLocation.latitude = user.lat!!.toDouble()
                        currentUserLocation.longitude = user.lng!!.toDouble()
                    }else{
                        followingUserLoc.latitude = user.lat!!.toDouble()
                        followingUserLoc.longitude = user.lng!!.toDouble()
                    }

                    if(markersMap.containsKey(userId)){
                        markersMap.getValue(userId).remove()
                    }
                    //to jest tak, user zmienia lokalizacje uruchamia sie onLocationChanged a tam dodajemy nowa lokalizacje do bazy i odpytujemy baze z istniejacych followerowanych userow i kazdy znaleziony user jest przesylany do metody ktora sprawdza lokalizacje tego usera i wyswietla na mapie
                    //i to jest wykowane dla aktualnego usera, a inny user ktory sledzi tego aktualnego usera ma poprostu metode ktora dodaje do mapy znacznik, gdzie jest listener ktory patrzy na dane w bazie w lokalizacji i jak sie zmieni dla ktoregos usera to jest wykonywana, czyli dodaje usera do mapy
                    //addValueEventListeners The listener is triggered once for the initial state of the data and again anytime the data changes
                    if(userId == FirebaseAuth.getInstance().currentUser!!.uid){
                        var currentUser = FirebaseAuth.getInstance().currentUser
                        var currentMarker = mGoogleMap!!.addMarker(MarkerOptions()
                                    .position(LatLng(currentUserLocation.latitude!!, currentUserLocation.longitude!!))
                                    .title(currentUser!!.email))
                        markersMap.put(userId, currentMarker)
                        var location = Location("")
                        location.latitude = currentMarker.position.latitude
                        location.longitude = currentMarker.position.longitude
                        updateMarkerSnippetDistance(userId,location)

                        mGoogleMap!!.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(currentUserLocation.latitude, currentUserLocation.longitude),12.0f))
                    }else{
                        var location = Location("")
                        location.latitude = userLocation.latitude
                        location.longitude = userLocation.longitude
                        var firstDistanceSecondMeasure = calculateDistanceBetweenTwoPoints(currentUserLocation,location)
                        Log.i(TAG,"ustawiam marker obserwowanego na pozycje : " + userLocation + " dla " + user.email)
                        var markerFollowingUser = mGoogleMap!!.addMarker(MarkerOptions()
                                .position(userLocation)
                                .title(user.email)
                                .snippet("Distance " + DecimalFormat("#.#").format(firstDistanceSecondMeasure.first) + firstDistanceSecondMeasure.second)
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)))
                        mGoogleMap!!.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(userLocation.latitude, userLocation.longitude),12.0f))
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
    private fun calculateDistanceBetweenTwoPoints(currentUserLocation: Location , followingUserLoc: Location ) : Pair<Float,String>{
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