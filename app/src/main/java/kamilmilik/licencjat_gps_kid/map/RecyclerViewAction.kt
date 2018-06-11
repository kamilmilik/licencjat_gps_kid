package kamilmilik.licencjat_gps_kid.map

import android.app.Activity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import kamilmilik.licencjat_gps_kid.R
import kamilmilik.licencjat_gps_kid.utils.IRecyclerViewListener
import kamilmilik.licencjat_gps_kid.utils.RecyclerViewAdapter
import kamilmilik.licencjat_gps_kid.models.UserMarkerInformationModel

/**
 * Created by kamil on 30.05.2018.
 */
class RecyclerViewAction(var activity: Activity, var locationFirebaseMarkerAction: LocationFirebaseMarkerAction) : IRecyclerViewListener {
    private val TAG = RecyclerViewAction::class.java.simpleName

    lateinit var adapter: RecyclerViewAdapter
    lateinit var recyclerView: RecyclerView
    lateinit var valueSet: HashSet<UserMarkerInformationModel>

    fun setupRecyclerView() {
        var currentUser = FirebaseAuth.getInstance().currentUser!!
        recyclerView = this.activity.findViewById(R.id.listOnline)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)

        //add current user and other users are add in FinderUserConnection
        valueSet = HashSet()
        if (FirebaseAuth.getInstance().currentUser != null) {
            valueSet.add(UserMarkerInformationModel(currentUser.email!!, currentUser.displayName!!))
            var valueList = ArrayList(valueSet)
            adapter = RecyclerViewAdapter(activity, valueList)
            recyclerView.adapter = adapter
            adapter.setClickListener(this)
        }
    }

    override fun setOnItemClick(view: View, position: Int) {
        var valueList = ArrayList(valueSet)
        var clickedUserEmail = valueList[position]
        locationFirebaseMarkerAction!!.goToThisMarker(clickedUserEmail)
    }

    fun updateRecyclerView() {
        var currentUser = FirebaseAuth.getInstance().currentUser
        valueSet.add(UserMarkerInformationModel(currentUser!!.email!!, currentUser.displayName!!))
        var valueList = ArrayList(valueSet)
        adapter = RecyclerViewAdapter(activity, valueList)
        recyclerView.adapter = adapter
        adapter.setClickListener(this)

        adapter.notifyDataSetChanged()
    }

    fun updateChangeUserNameInRecycler(userInformation: UserMarkerInformationModel) {
        val iterator = valueSet.iterator()
        while (iterator.hasNext()) {
            val value = iterator.next()
            if (value.email == userInformation.email) run {
                iterator.remove()
            }
        }
        valueSet.add(userInformation)
    }
}