package kamilmilik.licencjat_gps_kid.map.PolygonOperation

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import kamilmilik.licencjat_gps_kid.R
import kamilmilik.licencjat_gps_kid.map.adapter.GeoLatLng

/**
 * Created by kamil on 08.06.2018.
 */
open class PolygonContent(open var googleMap: GoogleMap, open var context: Context) {

    var polygon: Polygon? = null

    var polygonGeoLatLngPoints: ArrayList<GeoLatLng> = ArrayList()

    var polygonLatLngPoints: ArrayList<LatLng> = ArrayList()

    var polygonsGeoLatLngMap: HashMap<String, ArrayList<GeoLatLng>> = HashMap()

    var polygonsLatLngMap: HashMap<String, ArrayList<LatLng>> = HashMap()

    var markersMap: HashMap<ArrayList<Marker>, Polygon>? = HashMap()

    var markerList: ArrayList<Marker> = ArrayList()

    fun makeMarkerWithTag(position: LatLng): Marker {
        var marker: Marker = createMarker(position)
        marker.tag = polygon.toString().replace(".", "")
        return marker
    }

    fun createMarker(position: LatLng, polygonTag: String): Marker {
        var marker: Marker = createMarker(position)
        marker.tag = polygonTag
        return marker
    }

    private fun createMarker(position: LatLng) : Marker {
        val markerIcon = getMarkerIconFromDrawable(context.resources.getDrawable(R.drawable.round_icon))
        return googleMap.addMarker(MarkerOptions()
                .position(position).draggable(true).icon(markerIcon).anchor(0.5f, 0.5f))
    }

    private fun getMarkerIconFromDrawable(drawable: Drawable): BitmapDescriptor {
        val canvas = Canvas()
        val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        canvas.setBitmap(bitmap)
        drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
        drawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }
}