package kamilmilik.licencjat_gps_kid.Helper.PolygonOperation

import android.content.Context
import android.graphics.Point
import android.util.Log
import android.view.MotionEvent
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import kamilmilik.licencjat_gps_kid.models.MyOwnLatLng
import kamilmilik.licencjat_gps_kid.models.PolygonModel
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.LatLng
import kamilmilik.licencjat_gps_kid.R
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError


/**
 * Created by kamil on 17.03.2018.
 */
class DrawPolygon(var googleMap: GoogleMap,var context : Context) : OnGetDataListener {
    /**
     * listener for getting markersMap from database from PolygonDatabaseOperation class
     */
    override fun onSuccess(markersMap: HashMap<ArrayList<Marker>,Polygon>) {
        Log.i(TAG,"onSuccess()")
        this.markersMap = markersMap
        googleMap.setOnMarkerDragListener(MarkerListener(markersMap!!,polygonDatabaseOperation!!))

    }

    private var TAG = DrawPolygon::class.java.simpleName

    private var polygonDatabaseOperation : PolygonDatabaseOperation? = null
    init {
        //googleMap.setOnMarkerDragListener(this)
        polygonDatabaseOperation = PolygonDatabaseOperation(googleMap,context,this)
    }
    private var polygonPoints: ArrayList<MyOwnLatLng> = ArrayList()
    private var polygonPoints2: ArrayList<LatLng> = ArrayList()

    private var polygonsMap: HashMap<String, ArrayList<MyOwnLatLng>> = HashMap()
    private var polygonsMap2: HashMap<String, ArrayList<LatLng>> = HashMap()


    private var polygon: Polygon? = null
    private var markersMap: HashMap<ArrayList<Marker>,Polygon>? = HashMap()
    private var markerList : ArrayList<Marker> = ArrayList()
    fun onTouchAction(motionEvent: MotionEvent?) {
        Log.i(TAG,"onTouchAction()")
        var position = googleMap!!.projection.fromScreenLocation(
                Point(motionEvent!!.x.toInt(), motionEvent!!.y.toInt()));
        var action = motionEvent.action
        Log.i(TAG, "action " + action)
        when (action) {
            MotionEvent.ACTION_DOWN -> {
                Log.i(TAG, "Action Down")
                if (polygon != null) {
                    polygon = null
                    polygonPoints.clear();
                    polygonPoints2.clear()
                }
                polygonPoints.add(MyOwnLatLng(position.latitude,position.longitude))
                polygonPoints2.add(position)

                polygon = googleMap!!.addPolygon(PolygonOptions().addAll(polygonPoints2))
                polygon!!.tag = polygon.toString().replace(".","")
                polygon!!.isClickable = true

                if(!isAddedOnePointPolygon()) {
                    var marker = createMarker(position)
                    markerList!!.add(marker)
                }
            }
            MotionEvent.ACTION_MOVE -> {
                Log.i(TAG, "Action Move")
                polygonPoints.add(MyOwnLatLng(position.latitude,position.longitude));
                polygonPoints2.add(position);

                polygon!!.points = polygonPoints2
                if(!isAddedOnePointPolygon()) {
                    var marker = createMarker(position)
                    markerList!!.add(marker)
                }
            }
            MotionEvent.ACTION_UP -> {
                googleMap!!.setOnPolygonClickListener { polygon ->
                    //this is run when user draw before
                    Log.i(TAG, "clicked in action_up " + polygon!!.tag.toString())
                    polygonsMap.remove(polygon!!.tag.toString())
                    polygonsMap2.remove(polygon!!.tag.toString())
                    polygonDatabaseOperation!!.removePolygonFromDatabase(polygon!!.tag.toString())

                    markersMap!!.forEach { (markerList,polygonValue) ->
                        Log.i(TAG,polygonValue.tag.toString() + " " + polygon.tag.toString())
                        if(polygonValue.tag!! == polygon!!.tag){
                            markerList.forEach({marker ->
                                marker.remove()
                            })
                        }
                    }
                    polygon!!.remove()
                }


                Log.i(TAG, "Action Up")
                //need copy this since i cant put polygonPoints because it is a reference, and polygonPoints list change
                if(!isAddedOnePointPolygon()){
                    var copyPolygonPoints: ArrayList<MyOwnLatLng> = ArrayList()
                    var copyPolygonPoints2: ArrayList<LatLng> = ArrayList()
                    copyPolygonPoints.addAll(polygonPoints)
                    copyPolygonPoints2.addAll(polygonPoints2)

                    polygonsMap.put(polygon!!.tag.toString(), copyPolygonPoints)
                    polygonsMap2.put(polygon!!.tag.toString(), copyPolygonPoints2)

                    var copyMarkerList : ArrayList<Marker> = ArrayList()
                    copyMarkerList.addAll(markerList)
                    markersMap!!.put(copyMarkerList, polygon!!)
                    Log.i(TAG,"markersMap size: " + markersMap!!.size + " markerList size: " + copyMarkerList.size)
                    markerList.clear()

                    //googleMap.setOnMarkerDragListener(MarkerListener(markersMap!!,polygonDatabaseOperation!!))

                    for (polygon in polygonsMap) {
                        Log.i(TAG, "polygonMAPSIZE " + polygonsMap.size + "polygon.key" + "\n${polygon.value}")
                        polygonDatabaseOperation!!.savePolygonToDatabase(PolygonModel(polygon.key,polygon.value))
                    }
                }
            }
        }
    }

    private fun createMarker(position : LatLng) : Marker{
        val circleDrawable = context.resources.getDrawable(R.drawable.round_icon)
        val markerIcon = getMarkerIconFromDrawable(circleDrawable)
        var marker : Marker = googleMap!!.addMarker(MarkerOptions()
                .position(position).draggable(true).icon(markerIcon).anchor(0.5f,0.5f))
        marker.tag = polygon.toString().replace(".","")
        return marker
    }
    private fun getMarkerIconFromDrawable(drawable: Drawable): BitmapDescriptor {
        val canvas = Canvas()
        val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        canvas.setBitmap(bitmap)
        drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
        drawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    private fun isAddedOnePointPolygon() : Boolean {
        return if(polygonPoints.size > 1 && polygonPoints2.size > 1) false else true
    }
}