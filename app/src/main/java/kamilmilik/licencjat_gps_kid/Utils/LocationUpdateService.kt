package kamilmilik.licencjat_gps_kid.Utils

import android.app.IntentService
import android.content.Intent
import android.location.Location
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import kamilmilik.licencjat_gps_kid.Helper.Notification
import kamilmilik.licencjat_gps_kid.Helper.PolygonOperation.InsideOrOutsideArea
import kamilmilik.licencjat_gps_kid.models.PolygonModel
import kamilmilik.licencjat_gps_kid.models.TrackingModel
import kamilmilik.licencjat_gps_kid.models.User

/**
 * Created by kamil on 12.03.2018.
 */
class LocationUpdateService : IntentService{

    private val TAG  = LocationUpdateService::class.java.simpleName

    var location : Location? = null

    constructor() : super("LocationUpdateService")

    override fun onHandleIntent(intent: Intent?) {
        if (LocationResult.hasResult(intent)) {
            val locationResult = LocationResult.extractResult(intent)
            location = locationResult.lastLocation
            if (location != null) {
                addCurrentUserLocationToFirebase(location!!)
            }
        }
    }
    private fun addCurrentUserLocationToFirebase(lastLocation: Location) {
        Log.i(TAG, "addCurrentUserLocationToFirebase()")
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
    private val ENTER = 1
    private val EXIT = 2
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
                            var polygonsFromDbMap   = child.getValue(PolygonModel::class.java)
                            Log.i(TAG,polygonsFromDbMap!!.tag + " " + polygonsFromDbMap!!.polygonLatLngList)

                            var newList : ArrayList<LatLng> = changePolygonModelWithMyOwnLatLngListToLatLngList(polygonsFromDbMap)

                            polygonsLatLngMap.put(polygonsFromDbMap!!.tag!!, newList)
                        }
                    }
                    var insideOrOutsideArea = InsideOrOutsideArea(this@LocationUpdateService, location!!)
                    var listOfIsInArea = insideOrOutsideArea.isPointInsidePolygon(polygonsLatLngMap)
                    Log.i(TAG, listOfIsInArea.toString())
                    listOfIsInArea
                            .filter { it == ENTER || it == EXIT }
                            .forEach { Notification.findFollowersConnection(it) }

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



}