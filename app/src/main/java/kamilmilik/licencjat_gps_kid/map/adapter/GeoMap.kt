package kamilmilik.licencjat_gps_kid.map.adapter

import android.support.v7.app.AppCompatActivity
import android.app.Activity
import com.google.android.gms.maps.Projection


/**
 * Created by kamil on 31.05.2018.
 */
interface GeoMap {

    fun getMapAsync(activity: AppCompatActivity, onMapReadyCallback: GeoOnMapReadyCallback)

    fun setMyLocationEnabled(enabled: Boolean)

    fun zoomIn()

    fun zoomOut()

    fun moveCameraToGivenLatLng(latitude: Double, longitude: Double)

    fun moveCameraToGivenLatLngZoom(latitude: Double, longitude: Double, zoomLevel: Float)

    fun getCameraPositionTarget(): GeoLatLng

    fun clear()

    fun setupMap()

    fun getCameraPositionZoomLevel(): Float

    fun invalidate()

    fun getMapType(): Int

    fun getProjection() : Projection

    fun setMapType(mapType: Int)

    fun addMarker(options: GeoMarkerOptions, onMarkerClickListener: GeoOnMarkerClickListener, onMarkerDragListener: GeoOnMarkerDragListener): GeoMarker

    fun setOnMarkerClickListener(onMarkerClickListener: GeoOnMarkerClickListener)

    fun setOnMarkerDragListener(onMarkerDragListener: GeoOnMarkerDragListener)
}