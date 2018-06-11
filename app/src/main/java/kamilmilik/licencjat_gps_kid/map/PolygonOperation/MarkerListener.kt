package kamilmilik.licencjat_gps_kid.map.PolygonOperation

import android.util.Log
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.Polygon
import kamilmilik.licencjat_gps_kid.map.adapter.GeoLatLng
import kamilmilik.licencjat_gps_kid.models.PolygonModel

/**
 * Created by kamil on 26.03.2018.
 */
class MarkerListener(private var polygonsGeoLatLngMap: HashMap<String, ArrayList<GeoLatLng>>, private var markersMap: HashMap<ArrayList<Marker>, Polygon>?, private var polygonDatabaseOperation: PolygonDatabaseOperation) : GoogleMap.OnMarkerDragListener{
    private val TAG = MarkerListener::class.java.simpleName

    override fun onMarkerDragEnd(marker: Marker?) {
        Log.i(TAG,"onMarkerDragEnd()")
        //TODO tutaj dodac zeby sie wlaczal z notification getPolygonFromDatabase zeby wyslal notyfikacje jak po edycji user bedzie w obszarze lub poza
    }

    override fun onMarkerDragStart(marker: Marker?) {
        Log.i(TAG,"onMarkerDragStart()")
        editPolygonAction(marker)
    }

    override fun onMarkerDrag(marker: Marker?) {
        editPolygonAction(marker)
    }

    private fun editPolygonAction(marker: Marker?){
        for((markerList, polygon) in markersMap!!){
            Log.i(TAG, "tag " + polygon.tag.toString() + " only polygon " + polygon.toString()+" " + marker!!.tag.toString())
            if (polygon.tag != null) {
                if (polygon.tag!! == marker.tag) {
                    polygon.points = markersToLatLng(markerList)
                    var myOwnLatLngList = changeLatLngListToMyLatLngList(polygon.points)
                    // I must add to this map, since then it update latlng in DrawPolygon class, since we iterate through this map and add to database in this class and we want update points there
                    polygonsGeoLatLngMap.put((polygon.tag as String?)!!, myOwnLatLngList)
                    polygonDatabaseOperation.savePolygonToDatabase(PolygonModel(polygon.tag!!.toString(), myOwnLatLngList))
                }
            }
        }
    }
    private fun markersToLatLng(markersList: List<Marker>?): List<LatLng> {
        val latLngList : ArrayList<LatLng> = ArrayList()
        if (markersList == null) {
            return latLngList
        }
        markersList.mapTo(latLngList) { it.position }
        return latLngList
    }
    private fun changeLatLngListToMyLatLngList(latLngList: List<LatLng>) : ArrayList<GeoLatLng>{
        var newList : ArrayList<GeoLatLng> = ArrayList(latLngList.size)
        latLngList.mapTo(newList) { GeoLatLng(it.latitude, it.longitude) }
        return newList
    }
}