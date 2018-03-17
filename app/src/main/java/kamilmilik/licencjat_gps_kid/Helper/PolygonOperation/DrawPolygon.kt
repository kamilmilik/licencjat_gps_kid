package kamilmilik.licencjat_gps_kid.Helper.PolygonOperation

import android.graphics.Point
import android.util.Log
import android.view.MotionEvent
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polygon
import com.google.android.gms.maps.model.PolygonOptions
import kamilmilik.licencjat_gps_kid.models.MyOwnLatLng
import kamilmilik.licencjat_gps_kid.models.PolygonModel

/**
 * Created by kamil on 17.03.2018.
 */
class DrawPolygon(var googleMap: GoogleMap){
    private var TAG = DrawPolygon::class.java.simpleName
    var polygonDatabaseOperation : PolygonDatabaseOperation? = null
    init {
        polygonDatabaseOperation = PolygonDatabaseOperation(googleMap)
    }
    private var polygonPoints: ArrayList<MyOwnLatLng> = ArrayList()
    private var polygonPoints2: ArrayList<LatLng> = ArrayList()

    private var polygonsMap: HashMap<String, ArrayList<MyOwnLatLng>> = HashMap()
    private var polygonsMap2: HashMap<String, ArrayList<LatLng>> = HashMap()


    private var polygon: Polygon? = null
    fun onTouchAction(motionEvent: MotionEvent?) {
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
            }
            MotionEvent.ACTION_MOVE -> {
                Log.i(TAG, "Action Move")
                polygonPoints.add(MyOwnLatLng(position.latitude,position.longitude));
                polygonPoints2.add(position);

                polygon!!.points = polygonPoints2
            }
            MotionEvent.ACTION_UP -> {
                googleMap!!.setOnPolygonClickListener(object : GoogleMap.OnPolygonClickListener {
                    override fun onPolygonClick(polygon: Polygon?) {//this is run when user draw before
                        Log.i(TAG, "clicked in action_up " + polygon!!.tag.toString())
                        polygonsMap.remove(polygon!!.tag.toString())
                        polygonsMap2.remove(polygon!!.tag.toString())
                        Log.i(TAG, "jaki polygonDatabaseOp.. " + polygonDatabaseOperation)
                        polygonDatabaseOperation!!.removePolygonFromDatabase(polygon!!.tag.toString())
                        polygon!!.remove()
                    }
                })
                Log.i(TAG, "Action Up")
                //need copy this since i cant put polygonPoints because it is a reference, and polygonPoints list change
                if(!isAddedOnePointPolygon()){
                    var copyPolygonPoints: ArrayList<MyOwnLatLng> = ArrayList()
                    var copyPolygonPoints2: ArrayList<LatLng> = ArrayList()
                    copyPolygonPoints.addAll(polygonPoints)
                    copyPolygonPoints2.addAll(polygonPoints2)

                    polygonsMap.put(polygon!!.tag.toString(), copyPolygonPoints)
                    polygonsMap2.put(polygon!!.tag.toString(), copyPolygonPoints2)

                    for (polygon in polygonsMap) {
                        Log.i(TAG, "polygonMAPSIZE " + polygonsMap.size + "polygon.key" + "\n${polygon.value}")
                        polygonDatabaseOperation!!.savePolygonToDatabase(PolygonModel(polygon.key,polygon.value))
                    }
                }
            }
        }
    }
    private fun isAddedOnePointPolygon() : Boolean {
        return if(polygonPoints.size > 1 && polygonPoints2.size > 1) false else true
    }
}