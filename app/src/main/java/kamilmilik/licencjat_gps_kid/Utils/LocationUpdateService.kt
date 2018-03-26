//package kamilmilik.licencjat_gps_kid.Utils
//
//import android.app.IntentService
//import android.content.Intent
//import android.location.Location
//import android.util.Log
//import com.google.android.gms.location.Geofence
//import com.google.android.gms.location.LocationResult
//import com.google.android.gms.maps.model.LatLng
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.auth.FirebaseUser
//import com.google.firebase.database.*
//import kamilmilik.licencjat_gps_kid.Helper.Notification
//import kamilmilik.licencjat_gps_kid.Helper.PolygonOperation.InsideOrOutsideArea
//import kamilmilik.licencjat_gps_kid.models.PolygonModel
//import kamilmilik.licencjat_gps_kid.models.TrackingModel
//import kamilmilik.licencjat_gps_kid.models.User
//
///**
// * Created by kamil on 12.03.2018.
// */
//class LocationUpdateService : IntentService{
//
//    private val TAG  = LocationUpdateService::class.java.simpleName
//
//    var location : Location? = null
//
//    constructor() : super("LocationUpdateService")
//
//    override fun onHandleIntent(intent: Intent?) {
//        if (LocationResult.hasResult(intent)) {
//            val locationResult = LocationResult.extractResult(intent)
//            location = locationResult.lastLocation
//            if (location != null) {
//                addCurrentUserLocationToFirebase(location!!)
//            }
//        }
//    }
//    private fun addCurrentUserLocationToFirebase(lastLocation: Location) {
//        Log.i(TAG, "addCurrentUserMarkerAndRemoveOld()")
//        var locations = FirebaseDatabase.getInstance().getReference("Locations")
//        var currentUser = FirebaseAuth.getInstance().currentUser
//        if (currentUser != null) {//prevent if user click logout to not update locationOfUserWhoChangeIt
//            locations.child(currentUser!!.uid)
//                    .setValue(TrackingModel(currentUser.uid,
//                            currentUser!!.email!!,
//                            lastLocation.latitude.toString(),
//                            lastLocation.longitude.toString()))
//        }
//    }
//}