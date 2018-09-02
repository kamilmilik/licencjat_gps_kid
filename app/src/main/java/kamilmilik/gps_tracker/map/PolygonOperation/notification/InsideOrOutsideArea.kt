package kamilmilik.gps_tracker.map.PolygonOperation.notification

import android.content.Context
import android.location.Location
import com.google.android.gms.maps.model.LatLng
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.google.maps.android.PolyUtil
import kamilmilik.gps_tracker.utils.Constants
import kamilmilik.gps_tracker.models.UserAndPolygonKeyModel
import java.lang.reflect.Type
import kamilmilik.gps_tracker.models.MyCustomJsonSerializer


/**
 * Created by kamil on 17.03.2018.
 */

class InsideOrOutsideArea(var context: Context, var locationOfUserWhoChangeIt: Location) {

    private val TAG = InsideOrOutsideArea::class.java.simpleName

    private var isInArea: Boolean? = null

    private fun isInArea(polygonKey: UserAndPolygonKeyModel, polygonPoints: ArrayList<LatLng>): Int {
//        Log.i(TAG,"isInArea() polygon points " + polygonPoints)
        isInArea = PolyUtil.containsLocation(LatLng(locationOfUserWhoChangeIt.latitude, locationOfUserWhoChangeIt.longitude), polygonPoints, false)
//        Log.i(TAG,"isInArea()? = " + isInArea)
        val isInAreaPreviousMap: HashMap<UserAndPolygonKeyModel, Boolean> = getValueFromSharedPreferences()
        val previousValueInMap = isInAreaPreviousMap[polygonKey]// When user first run it return null.
//        Log.i(TAG, "polygonKey: $polygonKey")
//        Log.i(TAG, " previous $previousValueInMap isInArea $isInArea")
        isInArea?.let { isInArea ->
            isInAreaPreviousMap.put(polygonKey, isInArea)
        }
        writeValueToSharedPreferences(isInAreaPreviousMap)
        if (previousValueInMap == null) {// There wasn't any polygon previous.
            if (isInArea == true) {
                return Constants.PolygonAreaState.ENTER.idOfState
            } else if (isInArea == false) { // If user isn't in area, not push notification.
                return Constants.PolygonAreaState.STILL_OUTSIDE_OR_INSIDE.idOfState
            }
        }
        if (previousValueInMap == isInArea) {
            return Constants.PolygonAreaState.STILL_OUTSIDE_OR_INSIDE.idOfState
        } else if (previousValueInMap == false && isInArea == true) {
            return Constants.PolygonAreaState.ENTER.idOfState
        } else if (previousValueInMap == true && isInArea == false) {
            return Constants.PolygonAreaState.EXIT.idOfState
        }

        return Constants.PolygonAreaState.STILL_OUTSIDE_OR_INSIDE.idOfState
    }

    private fun getValueFromSharedPreferences(): HashMap<UserAndPolygonKeyModel, Boolean> {
        val sharedPref = context.getSharedPreferences(Constants.SHARED_KEY, Context.MODE_PRIVATE)
        val jsonString: String = sharedPref.getString(Constants.SHARED_POLYGON_KEY, "")
        if (jsonString != "") {
            val type: Type = object : TypeToken<HashMap<UserAndPolygonKeyModel, Boolean>>() {}.type
            return GsonBuilder().create().fromJson<HashMap<UserAndPolygonKeyModel, Boolean>>(jsonString, type)
        }
        return HashMap()
    }

    private fun writeValueToSharedPreferences(isInAreaPreviousMap: HashMap<UserAndPolygonKeyModel, Boolean>) {
        val sharedPref = context.getSharedPreferences(Constants.SHARED_KEY, Context.MODE_PRIVATE)
                ?: return
        val gson = GsonBuilder().enableComplexMapKeySerialization().setPrettyPrinting().registerTypeAdapter(UserAndPolygonKeyModel::class.java, MyCustomJsonSerializer()).create()
        val type: Type = object : TypeToken<HashMap<UserAndPolygonKeyModel, Boolean>>() {}.type
        val json = gson.toJson(isInAreaPreviousMap, type);
        with(sharedPref.edit()) {
            putString(Constants.SHARED_POLYGON_KEY, json)
            commit()
        }
    }

    fun isPointInsidePolygon(polygonsMap: HashMap<UserAndPolygonKeyModel, ArrayList<LatLng>>): ArrayList<Int> {
        val listOfIsInArea: ArrayList<Int> = ArrayList()
        for (mapData in polygonsMap) {
            listOfIsInArea.add(isInArea(mapData.key, mapData.value))
        }
        return listOfIsInArea
    }
}