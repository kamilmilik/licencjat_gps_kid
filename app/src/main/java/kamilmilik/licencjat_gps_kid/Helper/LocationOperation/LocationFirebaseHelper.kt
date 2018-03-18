package kamilmilik.licencjat_gps_kid.Helper.LocationOperation

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Point
import android.location.Location
import android.util.Log
import android.view.MotionEvent
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.maps.android.PolyUtil
import kamilmilik.licencjat_gps_kid.models.MyOwnLatLng
import kamilmilik.licencjat_gps_kid.models.PolygonModel
import kamilmilik.licencjat_gps_kid.models.TrackingModel
import java.text.DecimalFormat


/**
 * Created by kamil on 25.02.2018.
 */
class LocationFirebaseHelper(var mGoogleMap: GoogleMap, var context: Context) {
    private val TAG: String = LocationFirebaseHelper::class.java.simpleName
//    init {
//        getPolygonFromDatabase()
//    }
    var currentMarkerPosition: LatLng? = null
    fun addCurrentUserLocationToFirebase(lastLocation: Location) {
        Log.i(TAG, "addCurrentUserLocationToFirebase")
//        var locations = FirebaseDatabase.getInstance().getReference("Locations")
        var currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {//prevent if user click logout to not update locationOfUserWhoChangeIt
//            it should be method which add location to db but It is done in LocationUpdateService
//                 locations.child(currentUser!!.uid)
//                    .setValue(TrackingModel(currentUser.uid,
//                            currentUser!!.email!!,
//                            lastLocation.latitude.toString(),
//                            lastLocation.longitude.toString()))


            removeOldMarker(currentUser.email!!)

            var currentMarker = mGoogleMap!!.addMarker(MarkerOptions()
                    .position(LatLng(lastLocation.latitude!!, lastLocation.longitude!!))
                    .title(currentUser!!.email))
            markersMap.put(currentUser.email!!, currentMarker)

            //mGoogleMap!!.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(lastLocation.latitude, lastLocation.longitude), 12.0f))
            currentUserLocation = createLocationVariable(currentMarker.position)

            updateMarkerSnippetDistance(currentUser!!.email!!, currentUserLocation)

            currentMarkerPosition = currentMarker.position
            //createGeofencePendingIntent()

        }
    }

    fun listenerForLocationsChangeInFirebase(followingUserId: String) {
        loadLocationsFromDatabaseForGivenUserId(followingUserId)
    }

    var markersMap = HashMap<String, Marker>()
    var currentUserLocation = Location("")
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
                    var locationOfTheUserWhoChangeLocation = LatLng(userWhoChangeLocation!!.lat!!.toDouble(), userWhoChangeLocation!!.lng!!.toDouble())

                    removeOldMarker(userWhoChangeLocation!!.email)
                    var location = createLocationVariable(locationOfTheUserWhoChangeLocation)
                    var firstDistanceSecondMeasure = calculateDistanceBetweenTwoPoints(currentUserLocation, location)
                    Log.i(TAG, "ustawiam marker obserwowanego na pozycje : " + locationOfTheUserWhoChangeLocation + " dla " + userWhoChangeLocation!!.email)
                    var markerFollowingUser = mGoogleMap!!.addMarker(MarkerOptions()
                            .position(locationOfTheUserWhoChangeLocation)
                            .title(userWhoChangeLocation!!.email)
                            .snippet("Distance " + DecimalFormat("#.#").format(firstDistanceSecondMeasure.first) + firstDistanceSecondMeasure.second)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)))
                    //mGoogleMap!!.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(locationOfTheUserWhoChangeLocation.latitude, locationOfTheUserWhoChangeLocation.longitude), 12.0f))
                    markersMap.put(userWhoChangeLocation!!.email, markerFollowingUser)

                }

                if (dataSnapshot.value == null) {//nothing found
                    Log.i(TAG, "nothing found in onDataChange")
                }

            }

            override fun onCancelled(databaseError: DatabaseError?) {}
        })
    }

    private fun removeOldMarker(userEmail: String) {
        if (markersMap.containsKey(userEmail)) {
            markersMap.getValue(userEmail).remove()
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

    private fun updateMarkerSnippetDistance(userEmailToAvoidUpdate: String, currentUserLocation: Location) {
        for ((key, value) in markersMap) {
            if (key != userEmailToAvoidUpdate) {
                var location = Location("")
                location.latitude = value.position.latitude
                location.longitude = value.position.longitude
                var firstDistanceSecondMeasure = calculateDistanceBetweenTwoPoints(currentUserLocation, location)
                value.hideInfoWindow()
                value.snippet = "Distance " + DecimalFormat("#.#").format(firstDistanceSecondMeasure.first) + firstDistanceSecondMeasure.second
            }
        }
    }

    fun goToThisMarker(clickedUserEmail: String) {
        var searchedMarker = findMarker(clickedUserEmail)
        mGoogleMap!!.animateCamera(CameraUpdateFactory.newLatLngZoom(searchedMarker.position, 12.0f))
        searchedMarker.showInfoWindow()
    }

    private fun findMarker(searchedMarkerKey: String): Marker {
        return markersMap.getValue(searchedMarkerKey)
    }
}