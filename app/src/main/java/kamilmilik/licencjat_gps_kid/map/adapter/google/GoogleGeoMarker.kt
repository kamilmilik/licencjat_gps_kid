package kamilmilik.licencjat_gps_kid.map.adapter.google

import kamilmilik.licencjat_gps_kid.map.adapter.GeoLatLng
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Marker
import kamilmilik.licencjat_gps_kid.map.adapter.GeoMarker



/**
 * Created by kamil on 07.06.2018.
 */
class GoogleGeoMarker internal constructor(private val marker: Marker) : GeoMarker {
    override fun showInfoWindow() {
        marker.showInfoWindow()
    }

    override fun hideInfoWindow() {
        marker.hideInfoWindow()
    }

    override var id: String = ""
        get() = marker.id

    override var draggable: Boolean
        get() = marker.isDraggable
        set(isDraggable) {
            marker.isDraggable = isDraggable
        }

    override var isVisible: Boolean
        get() = marker.isDraggable
        set(isVisible) {
            marker.isDraggable = isVisible
        }

    override var title: String = ""
        get() = marker.title

    override var snippet: String = ""
        get() = marker.snippet

    override var position: GeoLatLng
        get() {
            val googlePosition = marker.position
            return GeoLatLng(googlePosition.latitude, googlePosition.longitude)
        }
        set(position) {
            val googleLatLng = LatLng(position.latitude!!, position.longitude!!)
            marker.position = googleLatLng
        }

    override fun setIcon(iconResId: Int) {
        marker.setIcon(BitmapDescriptorFactory.fromResource(iconResId))
    }

    override fun remove() {
        marker.remove()
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o !is GoogleGeoMarker) return false

        val other = o as GoogleGeoMarker?

        return marker == other!!.marker
    }

    override fun hashCode(): Int {
        return marker.hashCode()
    }
}
