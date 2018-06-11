package kamilmilik.licencjat_gps_kid.map.adapter.google

import android.support.v7.app.AppCompatActivity

import com.google.android.gms.maps.SupportMapFragment
import kamilmilik.licencjat_gps_kid.R
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import android.app.Activity
import com.google.android.gms.maps.CameraUpdateFactory
import android.annotation.SuppressLint
import com.google.android.gms.maps.Projection
import kamilmilik.licencjat_gps_kid.map.adapter.*
import org.jetbrains.annotations.NotNull


/**
 * Created by kamil on 31.05.2018.
 */
class GoogleGeoMap : GeoMap {
    var googleMap: GoogleMap? = null

    private fun setGoogleMap(googleMap: GoogleMap): GoogleGeoMap {
        this.googleMap = googleMap
        return this
    }

    override fun getMapAsync(activity: AppCompatActivity, onMapReadyCallback: GeoOnMapReadyCallback) {
        if (googleMap != null) {
            onMapReadyCallback.onMapReady(this)
            return
        }

        (activity.supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment).getMapAsync { googleMap -> onMapReadyCallback.onMapReady(setGoogleMap(googleMap)) }

    }

    @SuppressLint("MissingPermission")
    override fun setMyLocationEnabled(enabled: Boolean) {
        googleMap!!.isMyLocationEnabled = enabled
    }

    override fun zoomIn() {
        googleMap!!.moveCamera(CameraUpdateFactory.zoomIn())
    }

    override fun zoomOut() {
        googleMap!!.moveCamera(CameraUpdateFactory.zoomOut())
    }

    override fun clear() {
        googleMap!!.clear()
    }

    @NotNull
    override fun getCameraPositionTarget(): GeoLatLng {
        val target = googleMap!!.cameraPosition.target
        return GeoLatLng(target.latitude, target.longitude)
    }

    override fun getCameraPositionZoomLevel(): Float {
        return googleMap!!.cameraPosition.zoom
    }

    override fun getProjection() : Projection{
        return googleMap!!.projection
    }

    override fun setupMap() {
        val ui = googleMap!!.uiSettings
        ui.isCompassEnabled = true
        ui.isMyLocationButtonEnabled = true
        ui.isZoomControlsEnabled = true
        ui.setAllGesturesEnabled(true)
        ui.isMapToolbarEnabled = true
    }

    override fun setMapType(mapType: Int) {
        googleMap!!.mapType = mapType
    }

    override fun moveCameraToGivenLatLng(latitude: Double, longitude: Double) {
        googleMap!!.moveCamera(CameraUpdateFactory.newLatLng(LatLng(latitude, longitude)))
    }

    override fun moveCameraToGivenLatLngZoom(latitude: Double, longitude: Double, zoomLevel: Float) {
        googleMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(latitude, longitude), zoomLevel))
    }

    override fun getMapType(): Int {
        return googleMap!!.mapType
    }

    override fun invalidate() {
        // It is only used in Open Street Map.
    }

    override fun addMarker(options: GeoMarkerOptions, onMarkerClickListener: GeoOnMarkerClickListener, onMarkerDragListener: GeoOnMarkerDragListener): GeoMarker {
        val geoLatLng = options.position
        val googleOptions = MarkerOptions()
                .alpha(options.getAlpha()!!)
                .position(LatLng(geoLatLng!!.latitude!!, geoLatLng.longitude!!))
                .visible(options.visible!!)
                .snippet(options.snippet)
                .title(options.title)
                .icon(BitmapDescriptorFactory.fromResource(options.getIconResId()))

        val marker = googleMap!!.addMarker(googleOptions)

        return GoogleGeoMarker(marker)
    }

    override fun setOnMarkerClickListener(onMarkerClickListener: GeoOnMarkerClickListener) {
        googleMap!!.setOnMarkerClickListener { marker ->
            val myMarker = GoogleGeoMarker(marker)
            onMarkerClickListener.onMarkerClick(myMarker)
        }
    }

    override fun setOnMarkerDragListener(onMarkerDragListener: GeoOnMarkerDragListener) {
        googleMap!!.setOnMarkerDragListener(object : GoogleMap.OnMarkerDragListener {
            override fun onMarkerDragStart(marker: Marker) {

            }

            override fun onMarkerDrag(marker: Marker) {

            }

            override fun onMarkerDragEnd(marker: Marker) {
                val myMarker = GoogleGeoMarker(marker)
                onMarkerDragListener.onMarkerDragEnd(myMarker)
            }
        })
    }
}