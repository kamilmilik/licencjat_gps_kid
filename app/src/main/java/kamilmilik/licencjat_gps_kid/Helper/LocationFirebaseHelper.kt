package kamilmilik.licencjat_gps_kid.Helper

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
import kamilmilik.licencjat_gps_kid.Utils.PolygonService
import kamilmilik.licencjat_gps_kid.models.TrackingModel
import java.io.Serializable
import java.text.DecimalFormat


/**
 * Created by kamil on 25.02.2018.
 */
class LocationFirebaseHelper(var mGoogleMap: GoogleMap, var context : Context) {
    @Transient private val serialVersionUID = 1L
    private val TAG: String = LocationFirebaseHelper::class.java.simpleName

    var currentMarkerPosition : LatLng? = null
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


            removeOldMarker(currentUser.email!!)

            var currentMarker = mGoogleMap!!.addMarker(MarkerOptions()
                    .position(LatLng(lastLocation.latitude!!, lastLocation.longitude!!))
                    .title(currentUser!!.email))
            markersMap.put(currentUser.email!!, currentMarker)

            //mGoogleMap!!.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(lastLocation.latitude, lastLocation.longitude), 12.0f))
            currentUserLocation = createLocationVariable(currentMarker.position)

            updateMarkerSnippetDistance(currentUser!!.email!!, currentUserLocation)
            for(polygon in polygonsMap){
                Log.i(TAG,"polygon " + polygon.key + " \n ${polygon.value}")
            }
            currentMarkerPosition = currentMarker.position
            createGeofencePendingIntent()

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
                Log.i(TAG,"onDataChange in Locations listener " + dataSnapshot.toString() )
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
//-------- polygon
    var counter : Int = 0
val intent = Intent(context, PolygonService::class.java)
private var polygonPoints: ArrayList<LatLng> = ArrayList()
    private var polygonsMap:HashMap<String,ArrayList<LatLng>> = HashMap()
    private var polygon : Polygon? = null
    fun onTouchAction(motionEvent: MotionEvent?) {
        var position = mGoogleMap!!.projection.fromScreenLocation(
                Point(motionEvent!!.x.toInt(), motionEvent!!.y.toInt()));
        var action = motionEvent.action
        Log.i(TAG, "action " + action)
        when (action) {
            MotionEvent.ACTION_DOWN -> {
                Log.i(TAG, "Action Down")
                if (polygon != null) {
                    //polygon!!.remove()
                    polygon = null
                    polygonPoints.clear();
                }
                polygonPoints.add(position);
                polygon = mGoogleMap!!.addPolygon(PolygonOptions().addAll(polygonPoints))
                polygon!!.tag = "polygon " + counter
                counter++
                polygon!!.isClickable = true
            }
            MotionEvent.ACTION_MOVE -> {
                Log.i(TAG, "Action Move")
                polygonPoints.add(position);
                polygon!!.points = polygonPoints
            }
            MotionEvent.ACTION_UP -> {
                mGoogleMap!!.setOnPolygonClickListener(object : GoogleMap.OnPolygonClickListener {
                    override fun onPolygonClick(polygon: Polygon?) {
                        Log.i(TAG, "clicked " + polygon!!.tag.toString())
                        polygonsMap.remove(polygon!!.tag.toString())
                        polygon!!.remove()
                        createGeofencePendingIntent()
                    }
                })
                Log.i(TAG, "Action Up")
                Log.i(TAG, " " + PolyUtil.containsLocation(LatLng(65.9667, -18.5333), polygonPoints, false))
                var copyPolygonPoints : ArrayList<LatLng> = ArrayList()
                copyPolygonPoints.addAll(polygonPoints)
                polygonsMap.put(polygon!!.tag.toString(), copyPolygonPoints)
                createGeofencePendingIntent()

                // Close the polyline?
                // Send the polyline to make a search?
            }
        }
    }
    private val geoFencePendingIntent: PendingIntent? = null
    private fun createGeofencePendingIntent(): PendingIntent {
        Log.d(TAG, "createGeofencePendingIntent")
        if (geoFencePendingIntent != null)
            return geoFencePendingIntent

        var listOfIsInArea : ArrayList<Int> = ArrayList()
        for(polygon in polygonsMap){
            //Log.i(TAG, polygon.key + "\n${polygon.value}")
            Log.i(TAG, "isInArea " + isInArea(polygon.key,polygon.value))
            listOfIsInArea.add(isInArea(polygon.key,polygon.value))
            Log.i(TAG, "lista po dodani")
        }
        intent.putExtra("isInArea", listOfIsInArea)
        context.startService(Intent(intent))

        return PendingIntent.getService(
                 context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }
    //0 is still outside or inside
    //1 enter
    //2 exit
    private val STILL_OUTSIDE_OR_INSIDE = 0
    private val ENTER = 1
    private val EXIT = 2
    private var isInAreaPreviousMap: HashMap<String,Boolean> = HashMap()
    private var isInArea: Boolean? = null
    private fun isInArea(polygonKey : String ,polygonPoints : ArrayList<LatLng>): Int{
        isInArea = PolyUtil.containsLocation(currentMarkerPosition, polygonPoints, false)
        var previousValueInMap = isInAreaPreviousMap.get(polygonKey)
        Log.i(TAG,"polygonKey: $polygonKey")
        Log.i(TAG," previous $previousValueInMap isInArea $isInArea")
        isInAreaPreviousMap.put(polygonKey, isInArea!!)
        if(previousValueInMap == null){
            if(isInArea == true){
                return ENTER
            }else if(isInArea == false){//if user isn't in area not push notification
                return STILL_OUTSIDE_OR_INSIDE
            }
        }
        if(previousValueInMap == isInArea){
                return STILL_OUTSIDE_OR_INSIDE
            }else if (previousValueInMap == false && isInArea == true){
                return ENTER
            }else if (previousValueInMap == true && isInArea == false){
                return EXIT
            }

        return STILL_OUTSIDE_OR_INSIDE
    }
}