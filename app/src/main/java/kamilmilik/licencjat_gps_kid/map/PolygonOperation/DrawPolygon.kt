package kamilmilik.licencjat_gps_kid.map.PolygonOperation

import android.content.Context
import android.graphics.Point
import android.util.Log
import android.view.MotionEvent
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import kamilmilik.licencjat_gps_kid.map.adapter.GeoLatLng
import kamilmilik.licencjat_gps_kid.models.PolygonModel
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.LatLng
import kamilmilik.licencjat_gps_kid.R
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.widget.FrameLayout
import com.google.android.gms.maps.model.BitmapDescriptor


/**
 * Created by kamil on 17.03.2018.
 */
class DrawPolygon(override var googleMap: GoogleMap, override var context: Context) : PolygonContent(googleMap, context), OnGetDataListener {

    private var TAG = DrawPolygon::class.java.simpleName

    private var polygonDatabaseOperation: PolygonDatabaseOperation = PolygonDatabaseOperation(googleMap, context, this)

    /**
     * listener for getting markersMap from database from PolygonDatabaseOperation class
     */
    override fun setOnMarkerDragListenerAfterAddPolygon(markersMap: HashMap<ArrayList<Marker>, Polygon>) {
        Log.i(TAG, "setOnMarkerDragListenerAfterAddPolygon()")
        this.markersMap = markersMap
        googleMap.setOnMarkerDragListener(MarkerListener(polygonsGeoLatLngMap,markersMap, polygonDatabaseOperation!!))
    }

    fun onTouchAction(motionEvent: MotionEvent?, draggable: FrameLayout) {
        var position = googleMap.projection.fromScreenLocation(
                Point(motionEvent!!.x.toInt(), motionEvent!!.y.toInt()));
        when (motionEvent.action) {
            MotionEvent.ACTION_DOWN -> {
                if (polygon != null) {
                    polygonGeoLatLngPoints = ArrayList()
                    polygonLatLngPoints = ArrayList()
                }
                polygonGeoLatLngPoints.add(GeoLatLng(position.latitude, position.longitude))
                polygonLatLngPoints.add(position)

                polygon = googleMap.addPolygon(PolygonOptions().addAll(polygonLatLngPoints))
                polygon!!.tag = polygon.toString().replace(".", "")
                polygon!!.isClickable = true

                addMarker(position)
            }
            MotionEvent.ACTION_MOVE -> {
                polygonGeoLatLngPoints.add(GeoLatLng(position.latitude, position.longitude));
                polygonLatLngPoints.add(position);

                polygon!!.points = polygonLatLngPoints
                addMarker(position)
            }
            MotionEvent.ACTION_UP -> {
                // It is called when user ended drawing polygon
                removePolygonAction()
                if (!isAddedOnePointPolygon()) {
                    polygonsGeoLatLngMap.put(polygon!!.tag.toString(), polygonGeoLatLngPoints)
                    polygonsLatLngMap.put(polygon!!.tag.toString(), polygonLatLngPoints)
                    markersMap!!.put(markerList, polygon!!)
                    markerList = ArrayList()

                    for (polygon in polygonsGeoLatLngMap) {
                        polygonDatabaseOperation.savePolygonToDatabase(PolygonModel(polygon.key, polygon.value))
                    }
                }
                draggable.setOnTouchListener(null)
            }
        }
    }

    private fun addMarker(position: LatLng){
        if (!isAddedOnePointPolygon()) {
            val marker = makeMarkerWithTag(position)
            marker.isVisible = false
            markerList.add(marker)
        }
    }

    private fun removePolygonAction(){
        googleMap.setOnPolygonClickListener { polygon ->
            polygonsGeoLatLngMap.remove(polygon!!.tag.toString())
            polygonsLatLngMap.remove(polygon.tag.toString())
            polygonDatabaseOperation.removePolygonFromDatabase(polygon.tag.toString())

            markersMap!!.forEach { (markerList, polygonValue) ->
                Log.i(TAG, polygonValue.tag.toString() + " " + polygon.tag.toString())
                if (polygonValue.tag != null) {//prevent nullpointer it could happen when i do polygon.remove and if i not remove from map this polygon, then in map i have null as previous polygon
                    if (polygonValue.tag!! == polygon.tag) {
                        markerList.forEach({ marker ->
                            marker.remove()
                        })
                    }
                }
            }
            polygon.remove()
        }
    }

    private fun isAddedOnePointPolygon(): Boolean = !(polygonGeoLatLngPoints.size > 1 && polygonLatLngPoints.size > 1)

    fun showAllMarkers() {
        for ((markerList, polygon) in markersMap!!) {
            for (marker in markerList) {
                marker.isVisible = true
                Log.i(TAG, "marker " + marker + " " + marker.isVisible)
            }
        }
    }

    fun hideAllMarkers() {
        for ((markerList, polygon) in markersMap!!) {
            for (marker in markerList) {
                marker.isVisible = false
                Log.i(TAG, "marker " + marker + " " + marker.isVisible)
            }
        }
    }
}