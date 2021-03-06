package kamilmilik.gps_tracker.map.PolygonOperation

import android.graphics.Point
import android.util.Log
import android.view.MotionEvent
import com.google.android.gms.maps.model.*
import kamilmilik.gps_tracker.map.adapter.GeoLatLng
import kamilmilik.gps_tracker.models.PolygonModel
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.LatLng
import kamilmilik.gps_tracker.R
import android.widget.FrameLayout
import com.google.firebase.auth.FirebaseAuth
import kamilmilik.gps_tracker.map.MapActivity
import kamilmilik.gps_tracker.utils.ObjectsUtils
import kamilmilik.gps_tracker.utils.Tools
import kamilmilik.gps_tracker.utils.listeners.OnGetDataListener


/**
 * Created by kamil on 17.03.2018.
 */
class DrawPolygon(override var mapActivity: MapActivity) : PolygonContent(mapActivity), OnGetDataListener {

    private var TAG = DrawPolygon::class.java.simpleName

    private var polygonDatabaseOperation: PolygonDatabaseOperation = PolygonDatabaseOperation(mapActivity, this)

    var onMarkerDragListener: MarkerListener? = null

    override fun setOnMarkerDragListenerAfterAddPolygon(markersMap: HashMap<ArrayList<Marker>, Polygon>) {
        this.markersMap = markersMap
        this.onMarkerDragListener = MarkerListener(polygonsGeoLatLngMap, markersMap, polygonDatabaseOperation)
        mapActivity.getMap().setOnMarkerDragListener(onMarkerDragListener)
    }

    fun onTouchAction(motionEvent: MotionEvent?, draggable: FrameLayout) {
        ObjectsUtils.safeLet(motionEvent?.x?.toInt(), motionEvent?.y?.toInt()) { x, y ->

            mapActivity.getMap().projection?.fromScreenLocation(Point(x, y))?.let { position ->

                when (motionEvent?.action) {
                    MotionEvent.ACTION_DOWN -> {
                        if (polygon != null) {
                            polygonGeoLatLngPoints = ArrayList()
                            polygonLatLngPoints = ArrayList()
                        }
                        polygonGeoLatLngPoints.add(GeoLatLng(position.latitude, position.longitude))
                        polygonLatLngPoints.add(position)

                        polygon = mapActivity.getMap().addPolygon(PolygonOptions().addAll(polygonLatLngPoints))
                        polygon?.tag = polygon.toString().replace(".", "")
                        polygon?.isClickable = true

                        addMarker(position)
                    }
                    MotionEvent.ACTION_MOVE -> {
                        polygonGeoLatLngPoints.add(GeoLatLng(position.latitude, position.longitude))
                        polygonLatLngPoints.add(position)

                        polygon?.points = polygonLatLngPoints
                        addMarker(position)
                    }
                    MotionEvent.ACTION_UP -> {
                        // It is called when user ended drawing polygon.
                        removePolygonAction()
                        if (!isAddedOnePointPolygon()) {
                            polygonsGeoLatLngMap.put(polygon?.tag.toString(), polygonGeoLatLngPoints)
                            polygonsLatLngMap.put(polygon?.tag.toString(), polygonLatLngPoints)
                            polygon?.let { polygon ->
                                markersMap?.put(markerList, polygon)

                            }
                            markerList = ArrayList()
                            for (polygon in polygonsGeoLatLngMap) {
                                polygonDatabaseOperation.savePolygonToDatabase(PolygonModel(polygon.key, polygon.value))
                            }
                        }
                        draggable.setOnTouchListener(null)
                    }
                }
            }
        }
    }

    private fun addMarker(position: LatLng) {
        if (!isAddedOnePointPolygon()) {
            val marker = makeMarkerWithTag(position)
            marker.isVisible = false
            markerList.add(marker)
        }
    }

    private fun removePolygonAction() {
        mapActivity.getMap().setOnPolygonClickListener { polygon ->
            val alert = Tools.makeAlertDialogBuilder(mapActivity.getActivity(), mapActivity.getString(R.string.deleteAreaPolygon), mapActivity.getString(R.string.deleteAreaPolygonMessage))
            alert.setPositiveButton(R.string.ok) { dialog, whichButton ->
                polygonsGeoLatLngMap.remove(polygon?.tag.toString())
                polygonsLatLngMap.remove(polygon.tag.toString())
                polygonDatabaseOperation.removePolygonFromDatabase(polygon.tag.toString())
                removePolygon(polygon.tag.toString())
            }.setNegativeButton(R.string.cancel) { dialog, whichButton -> }.create().show()
        }
    }

    private fun isAddedOnePointPolygon(): Boolean = !(polygonGeoLatLngPoints.size > 1 && polygonLatLngPoints.size > 1)

    fun showAllMarkers() {
        markersMap?.let { markersMap ->
            for ((markerList, polygon) in markersMap) {
                for (marker in markerList) {
                    marker.isVisible = true
                }
            }

        }
    }

    fun hideAllMarkers() {
        markersMap?.let { markersMap ->
            for ((markerList, polygon) in markersMap) {
                for (marker in markerList) {
                    marker.isVisible = false
                }
            }
        }
    }
}