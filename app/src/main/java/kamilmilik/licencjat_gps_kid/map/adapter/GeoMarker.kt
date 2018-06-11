package kamilmilik.licencjat_gps_kid.map.adapter

/**
 * Created by kamil on 07.06.2018.
 */
interface GeoMarker {

    var id: String

    var draggable: Boolean

    var title: String

    var snippet: String

    var isVisible : Boolean

    var position: GeoLatLng

    fun setIcon(iconResId: Int)

    fun remove()

    fun showInfoWindow()

    fun hideInfoWindow()
}
