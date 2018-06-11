package kamilmilik.licencjat_gps_kid.map.adapter

/**
 * Created by kamil on 07.06.2018.
 */


class GeoMarkerOptions {
    var position: GeoLatLng? = null
        private set

    private var isDraggable: Boolean? = false

    var visible: Boolean? = true
        private set

    var title: String? = null
        private set

    var snippet: String? = null
        private set

    private var alpha: Float? = 1f

    private var iconResId: Int = 0

    fun title(title: String): GeoMarkerOptions {
        this.title = title
        return this
    }

    fun snippet(snippet: String): GeoMarkerOptions {
        this.snippet = snippet
        return this
    }

    fun position(position: GeoLatLng): GeoMarkerOptions {
        this.position = position
        return this
    }

    fun setDraggable(draggable: Boolean?): GeoMarkerOptions {
        isDraggable = draggable
        return this
    }

    fun getIconResId(): Int {
        return iconResId
    }

    fun getAlpha(): Float? {
        return alpha
    }

}
