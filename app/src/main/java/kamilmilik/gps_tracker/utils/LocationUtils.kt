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
        val timeDelta = location.time - currentBestLocation.time
        val isNewer = timeDelta > 0

        // Check whether the new location fix is more or less accurate
        val accuracyDelta = (location.accuracy - currentBestLocation.accuracy).toInt()
        val isLessAccurate = accuracyDelta > 0
        val isMoreAccurate = accuracyDelta < 0
        val isLowAccuracy = location.accuracy > 1000
        val isWrong = (accuracyDelta == 0) // Sometimes last location gives wrong location and then accuracy is the same as previous.
        val distanceBetweenLocations = currentBestLocation.distanceTo(location)

        val isSignificantlyLessAccurate = if (accuracyDelta > 50) {
            (distanceBetweenLocations > 50 && location.accuracy > 800)
        } else {
            if (isLowAccuracy) {
                (distanceBetweenLocations > 2000)
            } else {
                false
            }
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
        return false
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
            apply()
        }
    }

    fun getMapTypeFromSharedPref(context: Context): Int {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        return sharedPref.getInt(Constants.GOOGLE_MAP_TYPE, -1)
    }

    fun changePolygonModelWithMyOwnLatLngListToLatLngList(polygonsFromDbMap: PolygonModel): ArrayList<LatLng> {
        val newList: ArrayList<LatLng> = ArrayList(polygonsFromDbMap.polygonLatLngList.size)
        for (geoLatLng in polygonsFromDbMap.polygonLatLngList) {
            ObjectsUtils.safeLet(geoLatLng.latitude, geoLatLng.longitude) { lat, lng ->
                newList.add(LatLng(lat, lng))
            }
        }
        return newList
    }
}