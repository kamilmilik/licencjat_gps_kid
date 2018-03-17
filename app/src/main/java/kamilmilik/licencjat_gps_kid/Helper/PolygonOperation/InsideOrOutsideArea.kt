package kamilmilik.licencjat_gps_kid.Helper.PolygonOperation

import android.content.Context
import android.location.Location
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.google.maps.android.PolyUtil
import java.lang.reflect.Type

/**
 * Created by kamil on 17.03.2018.
 */
class InsideOrOutsideArea(var context : Context ,var location: Location){

    private val TAG = InsideOrOutsideArea::class.java.simpleName

    private val STILL_OUTSIDE_OR_INSIDE = 0
    private val ENTER = 1
    private val EXIT = 2

    // private var isInAreaPreviousMap: HashMap<String, Boolean> = HashMap()
    private var isInArea: Boolean? = null
    private fun isInArea(polygonKey: String, polygonPoints: ArrayList<LatLng>): Int {
        isInArea = PolyUtil.containsLocation(LatLng(location!!.latitude,location!!.longitude), polygonPoints, false)
        var isInAreaPreviousMap: HashMap<String, Boolean> = getValueFromSharedPreferences()
        var previousValueInMap = isInAreaPreviousMap[polygonKey]
        Log.i(TAG, "polygonKey: $polygonKey")
        Log.i(TAG, " previous $previousValueInMap isInArea $isInArea")
        isInAreaPreviousMap.put(polygonKey, isInArea!!)
        writeValueToSharedPreferences(isInAreaPreviousMap)
        if (previousValueInMap == null) {
            if (isInArea == true) {
                return ENTER
            } else if (isInArea == false) {//if user isn't in area not push notification
                return STILL_OUTSIDE_OR_INSIDE
            }
        }
        if (previousValueInMap == isInArea) {
            return STILL_OUTSIDE_OR_INSIDE
        } else if (previousValueInMap == false && isInArea == true) {
            return ENTER
        } else if (previousValueInMap == true && isInArea == false) {
            return EXIT
        }

        return STILL_OUTSIDE_OR_INSIDE
    }
    private fun getValueFromSharedPreferences() : HashMap<String,Boolean>{
        val sharedPref = context.getSharedPreferences("shared", Context.MODE_PRIVATE)
        val jsonString = sharedPref.getString("map","")
        if(!jsonString.equals("")){
            var type : Type =  object : TypeToken<HashMap<String, Boolean>>() {}.type
            var map : HashMap<String,Boolean> =  Gson().fromJson<HashMap<String,Boolean>>(jsonString, type)
            return map
        }
        return HashMap()
    }
    private fun writeValueToSharedPreferences(isInAreaPreviousMap: HashMap<String, Boolean>){
        val sharedPref = context.getSharedPreferences("shared", Context.MODE_PRIVATE) ?: return
        var builder =  GsonBuilder()
        var gson = builder.enableComplexMapKeySerialization().setPrettyPrinting().create()
        var type : Type =  object : TypeToken<HashMap<String, Boolean>>() {}.type
        var json = gson.toJson(isInAreaPreviousMap, type);
        with (sharedPref.edit()) {
            putString("map",json)
            commit()
        }
    }

    fun isPointInsidePolygon(polygonsMap : HashMap<String, ArrayList<LatLng>>) : ArrayList<Int>{
        var listOfIsInArea: ArrayList<Int> = ArrayList()
        for(polygon in polygonsMap){
            listOfIsInArea.add(isInArea(polygon.key, polygon.value))
        }
        return listOfIsInArea
    }
}