package kamilmilik.gps_tracker.map.PolygonOperation

import android.util.Log
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.Polygon
import kamilmilik.gps_tracker.map.adapter.GeoLatLng
import kamilmilik.gps_tracker.models.PolygonModel

/**
 * Created by kamil on 26.03.2018.
 */
class MarkerListener(private var polygonsGeoLatLngMap: HashMap<String, ArrayList<GeoLatLng>>, private var markersMap: HashMap<ArrayList<Marker>, Polygon>?, private var polygonDatabaseOperation: PolygonDatabaseOperation) : GoogleMap.OnMarkerDragListener {
    private val TAG = MarkerListener::class.java.simpleName

    private var editedPolygonModelList: ArrayList<PolygonModel>? = null

    override fun onMarkerDragEnd(marker: Marker?) {
        Log.i(TAG, "onMarkerDragEnd()")
    }

    override fun onMarkerDragStart(marker: Marker?) {
        Log.i(TAG, "onMarkerDragStart()")
        editPolygonAction(marker)
    }

    override fun onMarkerDrag(marker: Marker?) {
        editPolygonAction(marker)
    }

    private fun editPolygonAction(marker: Marker?) {
        editedPolygonModelList = ArrayList()
        markersMap?.let { markersMap ->
            for ((markerList, polygon) in markersMap) {
                Log.i(TAG, "tag " + polygon.tag.toString() + " only polygon " + polygon.toString() + " " + marker?.tag.toString())
                polygon.tag?.let { polygonTag ->
                    if (polygonTag == marker?.tag) {
                        polygon.points = markersToLatLng(markerList)
                        val myOwnLatLngList = changeLatLngListToMyLatLngList(polygon.points)
                        // I must add to this map, since then it update latlng in DrawPolygon class, since we iterate through this map and add to database in this class and we want update points there.
                        polygonsGeoLatLngMap.put((polygon.tag as String), myOwnLatLngList)
                        // Add polygon model to list to save to database when user unclick edit area button.
                        editedPolygonModelList?.add(PolygonModel(polygonTag.toString(), myOwnLatLngList))
                    }
                }
            }

        }
    }

    fun saveEditedPolygonToDatabase() {
        editedPolygonModelList?.let { editedPolygonModel ->
            for (polygonModel in editedPolygonModel) {
                polygonDatabaseOperation.savePolygonToDatabase(polygonModel)
            }
        }
    }

    private fun markersToLatLng(markersList: List<Marker>?): List<LatLng> {
        val latLngList: ArrayList<LatLng> = ArrayList()
        if (markersList == null) {
            return latLngList
        }
        markersList.mapTo(latLngList) { it.position }
        return latLngList
    }

    private fun changeLatLngListToMyLatLngList(latLngList: List<LatLng>): ArrayList<GeoLatLng> {
        val newList: ArrayList<GeoLatLng> = ArrayList(latLngList.size)
        latLngList.mapTo(newList) { GeoLatLng(it.latitude, it.longitude) }
        return newList
    }
}