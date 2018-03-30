package kamilmilik.licencjat_gps_kid.Helper.PolygonOperation

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.Log
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kamilmilik.licencjat_gps_kid.R
import kamilmilik.licencjat_gps_kid.models.MyOwnLatLng
import kamilmilik.licencjat_gps_kid.models.PolygonModel

/**
 * Created by kamil on 17.03.2018.
 */
class PolygonDatabaseOperation(var googleMap: GoogleMap, var context : Context, var onGetDataListener: OnGetDataListener) {

    private var TAG = PolygonDatabaseOperation::class.java.simpleName

    init{
       // googleMap.setOnMarkerDragListener(this)
        getPolygonFromDatabase(onGetDataListener)
    }
    private var polygonsMap: HashMap<String, ArrayList<MyOwnLatLng>> = HashMap()

    private var polygon: Polygon? = null

    private var markersMap: HashMap<ArrayList<Marker>,Polygon>? = HashMap()
    private var markerList : ArrayList<Marker> = ArrayList()

    fun savePolygonToDatabase(polygonMap : PolygonModel){
        var tag = polygonMap.tag!!.substring(polygonMap.tag!!.lastIndexOf('@')+1)
        var databaseReference = FirebaseDatabase.getInstance().getReference("user_polygons")
        var currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {//prevent if user click logout
            Log.i(TAG,"Polygon tag : " + tag + " polygon " + polygonMap.toString())
            databaseReference.child(currentUser!!.uid).child(tag)
                    .setValue(polygonMap)
        }
    }
    fun removePolygonFromDatabase(polygonTagToRemove : String){
        Log.i(TAG,"removePolygonFromDatabase " + polygonTagToRemove)
        var polygonTagToRemove = polygonTagToRemove.substring(polygonTagToRemove.lastIndexOf('@')+1)
        var databaseReference = FirebaseDatabase.getInstance().getReference("user_polygons")
        var currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {//prevent if user click logout to not update locationOfUserWhoChangeIt
            databaseReference.child(currentUser!!.uid)
                    .child(polygonTagToRemove).removeValue()
        }
    }
    private fun getPolygonFromDatabase(onGetDataListener: OnGetDataListener){
        var databaseReference = FirebaseDatabase.getInstance().getReference("user_polygons")
        var currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {//prevent if user click logout
            var query: Query = databaseReference.orderByKey().equalTo(currentUser.uid)
            query.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot?) {
                    for (singleSnapshot in dataSnapshot!!.children) {
                        for(child in singleSnapshot.children){
                            var polygonsFromDbMap   = child.getValue(PolygonModel::class.java)
                            //Log.i(TAG,polygonsFromDbMap!!.tag + " " + polygonsFromDbMap!!.polygonLatLngList)
                            polygonsMap.put(polygonsFromDbMap!!.tag!!,polygonsFromDbMap!!.polygonLatLngList!!)

                            var newList : ArrayList<LatLng> = changePolygonModelWithMyOwnLatLngListToLatLngList(polygonsFromDbMap)

                            drawPolygonFromDatabase(polygonsFromDbMap!!.tag!!,newList)
                        }
                    }
                    onGetDataListener.onSuccess(markersMap!!)
                    if (dataSnapshot.value == null) {//nothing found
                        Log.i(TAG, "nothing found in onDataChange")
                    }
                }
                override fun onCancelled(p0: DatabaseError?) {}
            })
        }
    }
    private fun changePolygonModelWithMyOwnLatLngListToLatLngList(polygonsFromDbMap : PolygonModel) : ArrayList<LatLng>{
        var newList : ArrayList<LatLng> = ArrayList(polygonsFromDbMap!!.polygonLatLngList!!.size)
        polygonsFromDbMap!!.polygonLatLngList!!.mapTo(newList) { LatLng(it.latitude!!, it.longitude!!) }
        return newList
    }

    /**
     * it draw polygon on map based on data from database
     * @param polygonTag tag of each polygon from database
     * @param polygonList ArrayList with LatLng of polygon
     */
    private fun drawPolygonFromDatabase(polygonTag : String ,polygonList: ArrayList<LatLng>){
        Log.i(TAG,"Draw POLYGON FFFFFFFFFFFF========")
        polygon = googleMap!!.addPolygon(PolygonOptions().addAll(polygonList))
        polygon!!.isClickable = true
        polygon!!.tag = polygonTag

        polygonList.forEach { position ->
            var marker = createMarker(position, polygonTag)
            marker.isVisible = false
            markerList!!.add(marker)
        }
        var copyMarkerList : ArrayList<Marker> = ArrayList()
        copyMarkerList.addAll(markerList)
        markersMap!!.put(copyMarkerList, polygon!!)
        Log.i(TAG,"markersMap size: " + markersMap!!.size + " markerList size: " + copyMarkerList.size)
        markerList.clear()
        //googleMap.setOnMarkerDragListener(MarkerListener(markersMap!!,this))

        googleMap!!.setOnPolygonClickListener { polygon ->//this is run before user draw some polygon
            Log.i(TAG, "clicked in drawPolygon" + polygon!!.tag.toString())
            polygonsMap.remove(polygon!!.tag.toString())
            removePolygonFromDatabase(polygon!!.tag.toString())

            markersMap!!.forEach { (markerList,polygonFromMap) ->
                if(polygonFromMap!!.tag != null){
                    if(polygonFromMap.tag!! == polygon.tag){
                        markerList.forEach({marker ->
                            marker.remove()
                        })
                        //markersMap!!.remove(markerList)
                    }
                }
            }
            polygon!!.remove()
        }
    }
    private fun createMarker(position : LatLng, polygonTag : String) : Marker{
        val circleDrawable = context.resources.getDrawable(R.drawable.round_icon)
        val markerIcon = getMarkerIconFromDrawable(circleDrawable)
        var marker : Marker = googleMap!!.addMarker(MarkerOptions()
                .position(position).draggable(true).icon(markerIcon).anchor(0.5f,0.5f))
        marker.tag = polygonTag
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
}