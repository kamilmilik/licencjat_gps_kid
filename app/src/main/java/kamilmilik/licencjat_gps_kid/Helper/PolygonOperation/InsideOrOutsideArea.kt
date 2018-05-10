package kamilmilik.licencjat_gps_kid.Helper.PolygonOperation

import android.content.Context
import android.location.Location
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.google.maps.android.PolyUtil
import kamilmilik.licencjat_gps_kid.Constants
import kamilmilik.licencjat_gps_kid.models.UserAndPolygonKeyModel
import java.lang.reflect.Type
import kamilmilik.licencjat_gps_kid.models.MyCustomJsonSerializer



/**
 * Created by kamil on 17.03.2018.
 */

class InsideOrOutsideArea(var context : Context ,var locationOfUserWhoChangeIt: Location){

    private val TAG = InsideOrOutsideArea::class.java.simpleName

    // private var isInAreaPreviousMap: HashMap<String, Boolean> = HashMap()
    private var isInArea: Boolean? = null

    /**
     * check if given in class location is inside or outside or not change status in given polygon
     * @param polygonKey
     * @param polygonPoints
     */
    private fun isInArea(polygonKey: UserAndPolygonKeyModel, polygonPoints: ArrayList<LatLng>): Int {
        //TODO przy paru userach to nie ma sensu, zmienic zeby zapisywac usera i jego stan, bo obecnie sprawdza tylko dany polygon a chyba user tez jest wazny?
        isInArea = PolyUtil.containsLocation(LatLng(locationOfUserWhoChangeIt!!.latitude, locationOfUserWhoChangeIt!!.longitude), polygonPoints, false)
        var isInAreaPreviousMap: HashMap<UserAndPolygonKeyModel, Boolean> = getValueFromSharedPreferences()
        var previousValueInMap = isInAreaPreviousMap[polygonKey]
        Log.i(TAG, "polygonKey: $polygonKey")
        Log.i(TAG, " previous $previousValueInMap isInArea $isInArea")
        isInAreaPreviousMap.put(polygonKey, isInArea!!)
        writeValueToSharedPreferences(isInAreaPreviousMap)
        if (previousValueInMap == null) {//wasn't any polygon previous
            if (isInArea == true) {
                return Constants.ENTER
            } else if (isInArea == false) {//if user isn't in area not push notification
                return Constants.STILL_OUTSIDE_OR_INSIDE
            }
        }
        if (previousValueInMap == isInArea) {
            return Constants.STILL_OUTSIDE_OR_INSIDE
        } else if (previousValueInMap == false && isInArea == true) {
            return Constants.ENTER
        } else if (previousValueInMap == true && isInArea == false) {
            return Constants.EXIT
        }

        return Constants.STILL_OUTSIDE_OR_INSIDE
    }
    private fun getValueFromSharedPreferences() : HashMap<UserAndPolygonKeyModel,Boolean>{
        val sharedPref = context.getSharedPreferences("shared", Context.MODE_PRIVATE)
        val jsonString : String= sharedPref.getString("map-polygon","")
        if(jsonString != ""){
            var type : Type =  object : TypeToken<HashMap<UserAndPolygonKeyModel, Boolean>>() {}.type
            Log.i(TAG,"getValueFromSharedPreferences() json " + jsonString)
            val gson = GsonBuilder().create()
            var map : HashMap<UserAndPolygonKeyModel,Boolean> =  gson.fromJson<HashMap<UserAndPolygonKeyModel,Boolean>>(jsonString, type)
            return map
        }
        return HashMap()
    }
    private fun writeValueToSharedPreferences(isInAreaPreviousMap: HashMap<UserAndPolygonKeyModel, Boolean>){
        val sharedPref = context.getSharedPreferences("shared", Context.MODE_PRIVATE) ?: return
        var builder =  GsonBuilder()
        var gson = builder.enableComplexMapKeySerialization().setPrettyPrinting().registerTypeAdapter(UserAndPolygonKeyModel::class.java, MyCustomJsonSerializer()).create()
        var type : Type =  object : TypeToken<HashMap<UserAndPolygonKeyModel, Boolean>>() {}.type
        var json = gson.toJson(isInAreaPreviousMap, type);
        Log.i(TAG,"writeValueToSharedPreferences() save json " + json)
        with (sharedPref.edit()) {
            putString("map-polygon",json)
            commit()
        }
    }

    fun isPointInsidePolygon(polygonsMap : HashMap<UserAndPolygonKeyModel, ArrayList<LatLng>>) : ArrayList<Int>{
        var listOfIsInArea: ArrayList<Int> = ArrayList()
        for(mapData in polygonsMap){
            listOfIsInArea.add(isInArea(mapData.key, mapData.value))
        }
        return listOfIsInArea
    }
}