package kamilmilik.licencjat_gps_kid.Helper.PolygonOperation

import android.util.Log
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polygon
import com.google.android.gms.maps.model.PolygonOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kamilmilik.licencjat_gps_kid.models.MyOwnLatLng
import kamilmilik.licencjat_gps_kid.models.PolygonModel

/**
 * Created by kamil on 17.03.2018.
 */
class PolygonDatabaseOperation(var googleMap: GoogleMap){
    private var TAG = PolygonDatabaseOperation::class.java.simpleName

    init{
        getPolygonFromDatabase()
    }
    private var polygonsMap: HashMap<String, ArrayList<MyOwnLatLng>> = HashMap()

    private var polygon: Polygon? = null

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
    private fun getPolygonFromDatabase(){
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

        googleMap!!.setOnPolygonClickListener { polygon ->//this is run before user draw some polygon
            Log.i(TAG, "clicked in drawPolygon" + polygon!!.tag.toString())
            polygonsMap.remove(polygon!!.tag.toString())
            removePolygonFromDatabase(polygon!!.tag.toString())
            polygon!!.remove()
        }
    }

}