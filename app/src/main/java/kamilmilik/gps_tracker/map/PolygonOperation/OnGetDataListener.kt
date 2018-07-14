package kamilmilik.gps_tracker.map.PolygonOperation

import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.Polygon

/**
 * Created by kamil on 26.03.2018.
 */
interface OnGetDataListener{
    fun setOnMarkerDragListenerAfterAddPolygon(markersMap: HashMap<ArrayList<Marker>, Polygon>)
}