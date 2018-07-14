package kamilmilik.gps_tracker.map.PolygonOperation

import android.util.Log
import com.google.android.gms.maps.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kamilmilik.gps_tracker.R
import kamilmilik.gps_tracker.map.MapActivity
import kamilmilik.gps_tracker.utils.Constants
import kamilmilik.gps_tracker.utils.Tools
import kamilmilik.gps_tracker.map.adapter.GeoLatLng
import kamilmilik.gps_tracker.models.PolygonModel

/**
 * Created by kamil on 17.03.2018.
 */
class PolygonDatabaseOperation(override var mapActivity: MapActivity, var onGetDataListener: OnGetDataListener) : PolygonContent(mapActivity) {

    private var TAG = PolygonDatabaseOperation::class.java.simpleName

    init {
        getPolygonFromDatabase(onGetDataListener)
    }

    private var polygonsMap: HashMap<String, ArrayList<GeoLatLng>> = HashMap()

    fun savePolygonToDatabase(polygonMap: PolygonModel) {
        val tag = polygonMap.tag!!.substring(polygonMap.tag!!.lastIndexOf('@') + 1)
        val databaseReference = FirebaseDatabase.getInstance().getReference(Constants.DATABASE_USER_POLYGONS)
        val currentUser = FirebaseAuth.getInstance().currentUser
        Log.i(TAG, "Polygon tag : " + tag + " polygon " + polygonMap.toString())
        databaseReference.child(currentUser!!.uid).child(tag)
                .setValue(polygonMap)
    }

    fun removePolygonFromDatabase(polygonTagToRemove: String) {
        Log.i(TAG, "removePolygonFromDatabase " + polygonTagToRemove)
        val polygonTagToRemove = polygonTagToRemove.substring(polygonTagToRemove.lastIndexOf('@') + 1)
        val databaseReference = FirebaseDatabase.getInstance().getReference(Constants.DATABASE_USER_POLYGONS)
        val currentUser = FirebaseAuth.getInstance().currentUser
        databaseReference.child(currentUser!!.uid)
                .child(polygonTagToRemove).removeValue()
    }

    private fun getPolygonFromDatabase(onGetDataListener: OnGetDataListener) {
        val databaseReference = FirebaseDatabase.getInstance().getReference(Constants.DATABASE_USER_POLYGONS)
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {//prevent if user click logout
            databaseReference
                    .orderByKey()
                    .equalTo(currentUser.uid)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot?) {
                            for (singleSnapshot in dataSnapshot!!.children) {
                                for (child in singleSnapshot.children) {
                                    val polygonsFromDbMap = child.getValue(PolygonModel::class.java)
                                    //Log.i(TAG,polygonsFromDbMap!!.tag + " " + polygonsFromDbMap!!.polygonLatLngList)
                                    polygonsMap.put(polygonsFromDbMap!!.tag!!, polygonsFromDbMap.polygonLatLngList)

                                    val newList: ArrayList<LatLng> = Tools.changePolygonModelWithMyOwnLatLngListToLatLngList(polygonsFromDbMap)

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
        polygon = mapActivity.getMap().addPolygon(PolygonOptions().addAll(polygonList))
        polygon!!.isClickable = true
        polygon!!.tag = polygonTag

        polygonList.forEach { position ->
            val marker = createMarker(position, polygonTag)
            marker.isVisible = false
            markerList.add(marker)
        }
        markersMap!!.put(markerList, polygon!!)
        Log.i(TAG, "markersMap size: " + markersMap!!.size + " markerList size: " + markerList.size)
        markerList = ArrayList()

        mapActivity.getMap().setOnPolygonClickListener { polygon ->
            //this is run before user draw some polygon
            Log.i(TAG, "clicked in drawPolygon" + polygon!!.tag.toString())
            val alert = Tools.makeAlertDialogBuilder(mapActivity.getActivity(), mapActivity.getString(R.string.deleteAreaPolygon), mapActivity.getString(R.string.deleteAreaPolygonMessage))
            alert.setPositiveButton(R.string.ok) { dialog, whichButton ->
                polygonsMap.remove(polygon.tag.toString())
                removePolygonFromDatabase(polygon.tag.toString())

                removePolygon()
            }.setNegativeButton(R.string.cancel) { dialog, wchichButton -> }.create().show()
        }
    }


}