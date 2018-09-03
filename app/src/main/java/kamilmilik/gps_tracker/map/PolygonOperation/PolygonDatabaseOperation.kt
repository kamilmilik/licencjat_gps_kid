package kamilmilik.gps_tracker.map.PolygonOperation

import android.util.Log
import android.view.View
import android.widget.RelativeLayout
import com.google.android.gms.maps.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kamilmilik.gps_tracker.R
import kamilmilik.gps_tracker.map.MapActivity
import kamilmilik.gps_tracker.utils.Constants
import kamilmilik.gps_tracker.utils.Tools
import kamilmilik.gps_tracker.map.adapter.GeoLatLng
import kamilmilik.gps_tracker.models.PolygonModel
import kamilmilik.gps_tracker.utils.LocationUtils
import kamilmilik.gps_tracker.utils.ObjectsUtils
import kamilmilik.gps_tracker.utils.listeners.OnGetDataListener

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
        polygonMap.tag?.let { polygonTag ->
            val tag = polygonTag.substring(polygonTag.lastIndexOf('@') + 1)
            val databaseReference = FirebaseDatabase.getInstance().getReference(Constants.DATABASE_USER_POLYGONS)
            val currentUser = FirebaseAuth.getInstance().currentUser
            databaseReference.child(currentUser?.uid).child(tag)
                    .setValue(polygonMap)
        }
    }

    fun removePolygonFromDatabase(polygonTagToRemove: String) {
        val polygonTagToRemove = polygonTagToRemove.substring(polygonTagToRemove.lastIndexOf('@') + 1)
        val databaseReference = FirebaseDatabase.getInstance().getReference(Constants.DATABASE_USER_POLYGONS)
        val currentUser = FirebaseAuth.getInstance().currentUser
        databaseReference.child(currentUser?.uid)
                .child(polygonTagToRemove).removeValue()
    }

    private fun getPolygonFromDatabase(onGetDataListener: OnGetDataListener) {
        val progressBarClickable = mapActivity.getActivity().findViewById<RelativeLayout>(R.id.progressBarClickableRelative)
        val progressBar = mapActivity.getActivity().findViewById<RelativeLayout>(R.id.progressBarRelative)
        if (progressBar.visibility == View.GONE) {
            progressBarClickable.visibility = View.VISIBLE
        }
        val databaseReference = FirebaseDatabase.getInstance().getReference(Constants.DATABASE_USER_POLYGONS)
        FirebaseAuth.getInstance().currentUser?.let { currentUser ->
            databaseReference
                    .orderByKey()
                    .equalTo(currentUser.uid)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot?) {
                            dataSnapshot?.let {
                                for (singleSnapshot in dataSnapshot.children) {
                                    for (child in singleSnapshot.children) {
                                        val polygonsFromDbMap = child.getValue(PolygonModel::class.java)
                                        ObjectsUtils.safeLet(polygonsFromDbMap, polygonsFromDbMap?.tag) { polygonsFromDb, polygonTag ->
                                            polygonsMap[polygonTag] = polygonsFromDb.polygonLatLngList

                                            val newList: ArrayList<LatLng> = LocationUtils.changePolygonModelWithMyOwnLatLngListToLatLngList(polygonsFromDb)

                                            drawPolygonFromDatabase(polygonTag, newList)

                                        }
                                    }
                                }
                                markersMap?.let { markers ->
                                    onGetDataListener.setOnMarkerDragListenerAfterAddPolygon(markers)
                                    progressBarClickable.visibility = View.GONE
                                }
                            }
                        }

                        override fun onCancelled(p0: DatabaseError?) {}
                    })
        }
    }

    /**
     * It draw polygon on map based on data from database.
     * @param polygonTag tag of each polygon from database
     * @param polygonList ArrayList with LatLng of polygon
     */
    private fun drawPolygonFromDatabase(polygonTag: String, polygonList: ArrayList<LatLng>) {
        polygon = mapActivity.getMap().addPolygon(PolygonOptions().addAll(polygonList))
        polygon?.isClickable = true
        polygon?.tag = polygonTag

        polygonList.forEach { position ->
            val marker = createMarker(position, polygonTag)
            marker.isVisible = false
            markerList.add(marker)
        }
        polygon?.let { polygon ->
            markersMap?.put(markerList, polygon)
        }
        markerList = ArrayList()

        mapActivity.getMap().setOnPolygonClickListener { polygon ->
            // This is run before user draw some polygon.
            val alert = Tools.makeAlertDialogBuilder(mapActivity.getActivity(), mapActivity.getString(R.string.deleteAreaPolygon), mapActivity.getString(R.string.deleteAreaPolygonMessage))
            alert.setPositiveButton(R.string.ok) { dialog, whichButton ->
                polygonsMap.remove(polygon.tag.toString())
                removePolygonFromDatabase(polygon.tag.toString())
                removePolygon(polygon.tag.toString())
            }.setNegativeButton(R.string.cancel) { dialog, whichButton -> }.create().show()
        }
    }


}