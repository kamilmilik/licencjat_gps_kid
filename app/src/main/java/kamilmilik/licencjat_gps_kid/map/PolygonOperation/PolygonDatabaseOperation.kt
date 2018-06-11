package kamilmilik.licencjat_gps_kid.map.PolygonOperation

import android.content.Context
import android.util.Log
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kamilmilik.licencjat_gps_kid.utils.Constants
import kamilmilik.licencjat_gps_kid.utils.Tools
import kamilmilik.licencjat_gps_kid.map.adapter.GeoLatLng
import kamilmilik.licencjat_gps_kid.models.PolygonModel

/**
 * Created by kamil on 17.03.2018.
 */
class PolygonDatabaseOperation(override var googleMap: GoogleMap, override var context: Context, var onGetDataListener: OnGetDataListener) : PolygonContent(googleMap, context) {

    private var TAG = PolygonDatabaseOperation::class.java.simpleName

    init {
        getPolygonFromDatabase(onGetDataListener)
    }

    private var polygonsMap: HashMap<String, ArrayList<GeoLatLng>> = HashMap()

    fun savePolygonToDatabase(polygonMap: PolygonModel) {
        var tag = polygonMap.tag!!.substring(polygonMap.tag!!.lastIndexOf('@') + 1)
        var databaseReference = FirebaseDatabase.getInstance().getReference(Constants.DATABASE_USER_POLYGONS)
        var currentUser = FirebaseAuth.getInstance().currentUser
        Log.i(TAG, "Polygon tag : " + tag + " polygon " + polygonMap.toString())
        databaseReference.child(currentUser!!.uid).child(tag)
                .setValue(polygonMap)
    }

    fun removePolygonFromDatabase(polygonTagToRemove: String) {
        Log.i(TAG, "removePolygonFromDatabase " + polygonTagToRemove)
        var polygonTagToRemove = polygonTagToRemove.substring(polygonTagToRemove.lastIndexOf('@') + 1)
        var databaseReference = FirebaseDatabase.getInstance().getReference(Constants.DATABASE_USER_POLYGONS)
        var currentUser = FirebaseAuth.getInstance().currentUser
        databaseReference.child(currentUser!!.uid)
                .child(polygonTagToRemove).removeValue()
    }

    private fun getPolygonFromDatabase(onGetDataListener: OnGetDataListener) {
        var databaseReference = FirebaseDatabase.getInstance().getReference(Constants.DATABASE_USER_POLYGONS)
        var currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {//prevent if user click logout
            databaseReference
                    .orderByKey()
                    .equalTo(currentUser.uid)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot?) {
                            for (singleSnapshot in dataSnapshot!!.children) {
                                for (child in singleSnapshot.children) {
                                    var polygonsFromDbMap = child.getValue(PolygonModel::class.java)
                                    //Log.i(TAG,polygonsFromDbMap!!.tag + " " + polygonsFromDbMap!!.polygonLatLngList)
                                    polygonsMap.put(polygonsFromDbMap!!.tag!!, polygonsFromDbMap.polygonLatLngList)

                                    var newList: ArrayList<LatLng> = Tools.changePolygonModelWithMyOwnLatLngListToLatLngList(polygonsFromDbMap)

                                    drawPolygonFromDatabase(polygonsFromDbMap.tag!!, newList)
                                }
                            }
                            onGetDataListener.setOnMarkerDragListenerAfterAddPolygon(markersMap!!)
                        }

                        override fun onCancelled(p0: DatabaseError?) {}
                    })
        }
    }

    /**
     * It draw polygon on map based on data from database
     * @param polygonTag tag of each polygon from database
     * @param polygonList ArrayList with LatLng of polygon
     */
    private fun drawPolygonFromDatabase(polygonTag: String, polygonList: ArrayList<LatLng>) {
        Log.i(TAG, "drawPolygonFromDatabase()")
        polygon = googleMap.addPolygon(PolygonOptions().addAll(polygonList))
        polygon!!.isClickable = true
        polygon!!.tag = polygonTag

        polygonList.forEach { position ->
            var marker = createMarker(position, polygonTag)
            marker.isVisible = false
            markerList.add(marker)
        }
        markersMap!!.put(markerList, polygon!!)
        Log.i(TAG, "markersMap size: " + markersMap!!.size + " markerList size: " + markerList.size)
        markerList = ArrayList()

        googleMap.setOnPolygonClickListener { polygon ->
            //this is run before user draw some polygon
            Log.i(TAG, "clicked in drawPolygon" + polygon!!.tag.toString())
            polygonsMap.remove(polygon.tag.toString())
            removePolygonFromDatabase(polygon.tag.toString())

            markersMap!!.forEach { (markerList, polygonFromMap) ->
                if (polygonFromMap.tag != null) {
                    if (polygonFromMap.tag!! == polygon.tag) {
                        markerList.forEach({ marker ->
                            marker.remove()
                        })
                        //markersMap!!.remove(markerList)
                    }
                }
            }
            polygon.remove()
        }
    }


}