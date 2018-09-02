package kamilmilik.gps_tracker.utils

import android.content.Context
import android.location.Location
import android.preference.PreferenceManager
import android.util.Log
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.maps.model.LatLng
import kamilmilik.gps_tracker.models.DistanceMeasureModel
import kamilmilik.gps_tracker.models.PolygonModel
import java.util.ArrayList

/**
 * Created by kamil on 07.08.2018.
 */
object LocationUtils {
    private val TAG = LocationUtils::class.java.simpleName

    fun createLocationVariable(userLatLng: LatLng): Location {
        val userLoc = Location("")
        userLoc.latitude = userLatLng.latitude
        userLoc.longitude = userLatLng.longitude
        return userLoc
    }

    fun calculateDistanceBetweenTwoPoints(currentUserLocation: Location, followingUserLoc: Location): DistanceMeasureModel {
        val measure: String?
        var distance: Float = currentUserLocation.distanceTo(followingUserLoc)
        if (distance > 1000) {
            distance = (distance / 1000)
            measure = "km"
        } else {
            measure = "m"
        }
        return DistanceMeasureModel(distance, measure)
    }

    fun createLocationFastRequest(isSmallestDisplacement: Boolean): LocationRequest {
        val locationRequest = LocationRequest.create()
        locationRequest.interval = Constants.LOCATION_INTERVAL_FAST
        locationRequest.fastestInterval = Constants.LOCATION_FASTEST_INTERVAL_FAST
        locationRequest.priority = Constants.LOCATION_PRIORITY
        if (isSmallestDisplacement) {
            locationRequest.smallestDisplacement = Constants.LOCATION_SMALLEST_DISPLACEMENT
        }
        return locationRequest
    }

    /** Determines whether one Location reading is better than the current Location fix
     * @param location  The new Location that you want to evaluate
     * @param currentBestLocation  The current Location fix, to which you want to compare the new one
     */
    fun isBetterLocation(context: Context?, location: Location, currentBestLocation: Location?): Boolean {
        val THREE_MINUTES = 1000 * 60 * 3
        if (currentBestLocation == null) {
            return true
        }
        // Check whether the new location fix is newer or older
        val timeDelta = location.time - currentBestLocation.time
        val isSignificantlyNewer = timeDelta > THREE_MINUTES
        val isSignificantlyOlder = timeDelta < -THREE_MINUTES
        val isNewer = timeDelta > 0
//
//        // If it's been more than three minutes since the current location, use the new location
//        // because the user has likely moved
//        LogUtils(context).appendLog(TAG, "timeDelta: $timeDelta isSignificantlyNewer $isSignificantlyNewer isSignificantlyOlder $isSignificantlyOlder")
//        if (isSignificantlyNewer) {
//            return true
//            // If the new location is more than three minutes older, it must be worse
//        } else if (isSignificantlyOlder) {
//            return false
//        }

        // Check whether the new location fix is more or less accurate
        val accuracyDelta = (location.accuracy - currentBestLocation.accuracy).toInt()
        val isLessAccurate = accuracyDelta > 0
        val isMoreAccurate = accuracyDelta < 0
        val isLowAccuracy = location.accuracy > 1000
        val isWrong = (accuracyDelta == 0) // Sometimes last location gives wrong location and then accuracy is the same as previous
        val distanceBetweenLocations = currentBestLocation.distanceTo(location)

        // Jeśli accuracyDelta jest większa od 50 czyli jest mało accurate sprawdź czy odleglość większa od 50 i czy nowa lokalizacja ma wieksza dokladność jak 1000
        // Jeśli accuracy jest małe ale wtedy może być taka sytuacja currentAccuracy = 2200 nowa lokalizacja accuracy 2100 ale distance jest 8000 np
        val isSignificantlyLessAccurate = if (accuracyDelta > 50) {
            (distanceBetweenLocations > 50 && location.accuracy > 800)
        } else {
            if (isLowAccuracy) {
                (distanceBetweenLocations > 2000)
            } else {
                false
            }
        }


        // Check if the old and new location are from the same provider
//        val isFromSameProvider = isSameProvider(location.provider, currentBestLocation.provider)

        // Determine location quality using a combination of timeliness and accuracy
        if (context != null) { // If statement only for junit tests.
            LogUtils(context).appendLog(TAG, "location provider " + location.provider + " " + " currentLocation provider " + currentBestLocation.provider)
            LogUtils(context).appendLog(TAG, "locationAccuracy " + location.accuracy + " currentLocation accuracy " + currentBestLocation.accuracy)
            LogUtils(context).appendLog(TAG, "accuracyDelta $accuracyDelta isMoreAccurate " + isMoreAccurate + " isNewer " + isNewer + " curr location time " + currentBestLocation.time + " location time " + location.time + " !isLessAccurate " + (!isLessAccurate) + " isSignificantlyLessAccurate " + isSignificantlyLessAccurate + " distance between locations " + distanceBetweenLocations + " speed location " + location.speed/*+ " isFromSameProvider " + isFromSameProvider*/)
        }
        if (isWrong) {
            return false
        } else if (isNewer && isMoreAccurate && !isLowAccuracy) {
            return true
        } else if (isNewer && !isLessAccurate && !isLowAccuracy) {
            return true
        } else if (isNewer && !isSignificantlyLessAccurate/* && isFromSameProvider*/) {
            return true
        }
//        else if(!isSignificantlyLessAccurate /*&& isFromSameProvider*/){
//            return true
//        }
        return false
    }

    /** Checks whether two providers are the same  */
    private fun isSameProvider(provider1: String?, provider2: String?): Boolean {
        return if (provider1 == null) {
            provider2 == null
        } else provider1 == provider2
    }

    fun saveLocationToSharedPref(context: Context, location: Location) {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context) ?: return
        with(sharedPref.edit()) {
            putString(Constants.LAST_LOCATION_LATITUDE, location.latitude.toString())
            putString(Constants.LAST_LOCATION_LONGITUDE, location.longitude.toString())
            putString(Constants.LAST_LOCATION_ACCURACY, location.accuracy.toString())
            putString(Constants.LAST_LOCATION_TIME, location.time.toString())
            putString(Constants.LAST_LOCATION_PROVIDER, location.provider.toString())
            apply()
        }
    }

    fun getLocationFromSharedPref(context: Context): Location? {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        val latitude = sharedPref.getString(Constants.LAST_LOCATION_LATITUDE, null)
        val longitude = sharedPref.getString(Constants.LAST_LOCATION_LONGITUDE, null)
        val accuracy = sharedPref.getString(Constants.LAST_LOCATION_ACCURACY, null)
        val time = sharedPref.getString(Constants.LAST_LOCATION_TIME, null)
        val provider = sharedPref.getString(Constants.LAST_LOCATION_PROVIDER, null)
        return if (latitude != null && longitude != null && accuracy != null && time != null && provider != null) {
            val location = Location(provider)
            location.latitude = latitude.toDouble()
            location.longitude = longitude.toDouble()
            location.accuracy = accuracy.toFloat()
            location.time = time.toLong()
            location
        } else {
            null
        }
    }

    fun saveMapTypeToSharedPref(context: Context, mapType: Int) {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context) ?: return
        with(sharedPref.edit()) {
            putInt(Constants.GOOGLE_MAP_TYPE, mapType)
            Log.i(TAG, "saveMapTypeToSharedPref() mapType " + mapType)
            apply()
        }
    }

    fun getMapTypeFromSharedPref(context: Context): Int {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        return sharedPref.getInt(Constants.GOOGLE_MAP_TYPE, -1)
    }


    /**
     * convert given polygon map object (PolygonModel) with GeoLatLng to polygon map object with LatLng
     * @param polygonsFromDbMap given polygon map model (model with tag and arrayList<GeoLatLng)
     */
    fun changePolygonModelWithMyOwnLatLngListToLatLngList(polygonsFromDbMap: PolygonModel): ArrayList<LatLng> {
        val newList: ArrayList<LatLng> = ArrayList(polygonsFromDbMap.polygonLatLngList.size)
//        polygonsFromDbMap.polygonLatLngList
//                .mapTo(newList) { geoLatLng ->
//            LatLng(geoLatLng.latitude!!, geoLatLng.longitude!!)
//        }
        for (geoLatLng in polygonsFromDbMap.polygonLatLngList) {
            ObjectsUtils.safeLet(geoLatLng.latitude, geoLatLng.longitude) { lat, lng ->
                newList.add(LatLng(lat, lng))
            }
        }
        return newList
    }
}