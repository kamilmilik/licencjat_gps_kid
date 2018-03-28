package kamilmilik.licencjat_gps_kid.Helper.PolygonOperation

import android.util.Log
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.Polygon
import kamilmilik.licencjat_gps_kid.models.MyOwnLatLng
import kamilmilik.licencjat_gps_kid.models.PolygonModel

/**
 * Created by kamil on 26.03.2018.
 */
class MarkerListener(private var markersMap: HashMap<ArrayList<Marker>, Polygon>?, private var polygonDatabaseOperation: PolygonDatabaseOperation) : GoogleMap.OnMarkerDragListener{
    private val TAG = MarkerListener::class.java.simpleName

    override fun onMarkerDragEnd(marker: Marker?) {
        Log.i(TAG,"onMarkerDragEnd()")
        //TODO tutaj dodac zeby sie wlaczal z notification getPolygonFromDatabase zeby wyslal notyfikacje jak po edycji user bedzie w obszarze lub poza
    }

    override fun onMarkerDragStart(marker: Marker?) {
        Log.i(TAG,"onMarkerDragStart()")
    }

    override fun onMarkerDrag(marker: Marker?) {
        Log.i(TAG,"onMarkerDrag() -> clicked marker : " + marker.toString() )
        for((markerList, polygon) in markersMap!!){
            Log.i(TAG, "tag " + polygon.tag.toString() + " only polygon " + polygon.toString()+" " + marker!!.tag.toString())
            if (polygon.tag != null) {
                if (polygon.tag!! == marker!!.tag) {
                    polygon.points = markersToLatLng(markerList)
                    var myOwnLatLngList = changeLatLngListToMyLatLngList(polygon.points)
                    polygonDatabaseOperation!!.savePolygonToDatabase(PolygonModel(polygon.tag!!.toString(), myOwnLatLngList))
                }
            }
        }
    }
    private fun markersToLatLng(markersList: List<Marker>?): List<LatLng> {
        val latLngs : ArrayList<LatLng> = ArrayList()
        if (markersList == null) {
            return latLngs
        }
        for (marker in markersList) {
            latLngs.add(marker.position)
        }
        return latLngs
    }
    private fun changeLatLngListToMyLatLngList(latLngList: List<LatLng>) : ArrayList<MyOwnLatLng>{
        var newList : ArrayList<MyOwnLatLng> = ArrayList(latLngList!!.size)
        latLngList!!.mapTo(newList) { MyOwnLatLng(it.latitude!!, it.longitude!!) }
        return newList
    }
}