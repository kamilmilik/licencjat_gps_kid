package kamilmilik.licencjat_gps_kid.map

import android.app.Activity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kamilmilik.licencjat_gps_kid.R
import kamilmilik.licencjat_gps_kid.models.User
import kamilmilik.licencjat_gps_kid.utils.IRecyclerViewListener
import kamilmilik.licencjat_gps_kid.models.UserMarkerInformationModel
import kamilmilik.licencjat_gps_kid.utils.Constants
import kamilmilik.licencjat_gps_kid.utils.Tools

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
            valueSet.add(UserMarkerInformationModel(currentUser.email!!, currentUser.displayName!!, currentUser.uid))
            var valueList = ArrayList(valueSet)
            adapter = RecyclerViewAdapter(activity, valueList)
            recyclerView.adapter = adapter
            adapter.setClickListener(this)
        }
    }

    fun updateRecyclerView() {
        var currentUser = FirebaseAuth.getInstance().currentUser
        valueSet.add(UserMarkerInformationModel(currentUser!!.email!!, currentUser.displayName!!, currentUser.uid))
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

    override fun setOnItemClick(view: View, position: Int) {
        var valueList = ArrayList(valueSet)
        var clickedUser = valueList[position]
        locationFirebaseMarkerAction!!.goToThisMarker(clickedUser)
    }

    override fun setOnLongItemClick(view: View, position: Int): Boolean {
        var alert2 = Tools.makeAlertDialogBuilder(activity, "Unfollowe user", "Are you sure to unfollow this user?")
        alert2.setPositiveButton(activity.getString(R.string.ok)) { dialog, whichButton ->
            unfollowUser(position)
        }
        alert2.setNegativeButton(activity.getString(R.string.cancel)) { dialog, whichButton -> }
        alert2.show()
        return true
    }

    private fun unfollowUser(position: Int){
        var valueList = ArrayList(valueSet)
        var clickedUser = valueList[position]

        if(clickedUser.userId.equals(FirebaseAuth.getInstance().currentUser!!.uid)){
            Toast.makeText(activity, activity.getString(R.string.unfollowYourselfInformation), Toast.LENGTH_LONG).show()
        }else{
            val reference = FirebaseDatabase.getInstance().reference

            removeUserFromFollowers(clickedUser.userId, reference, Constants.DATABASE_FOLLOWERS, Constants.DATABASE_FOLLOWING)
            removeUserFromFollowers(clickedUser.userId, reference, Constants.DATABASE_FOLLOWING, Constants.DATABASE_FOLLOWERS)
        }
    }

    private fun removeUserFromFollowers(userToUnfollowId: String, reference: DatabaseReference, nodeTable : String, nodeTable2 : String) {
        val query = reference.child(nodeTable)
                .orderByKey()
                .equalTo(userToUnfollowId)
        removeUser(query, reference, userToUnfollowId, nodeTable2)
    }

    private fun removeUser(query : Query, reference: DatabaseReference, userToUnfollowId: String, nodeTable2: String ){
        val currentUserId = FirebaseAuth.getInstance().currentUser!!.uid
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (singleSnapshot in dataSnapshot.children) {
                    for (childSingleSnapshot in singleSnapshot.children) {
                        var userFollowing = childSingleSnapshot.child(Constants.DATABASE_USER_FIELD).getValue(User::class.java)
                        val query = reference.child(nodeTable2)
                                .orderByKey()
                                .equalTo(currentUserId)
                        query.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                for (singleSnapshot in dataSnapshot.children) {
                                    for (childSingleSnapshot in singleSnapshot.children) {
                                        var userFollowers = childSingleSnapshot.child(Constants.DATABASE_USER_FIELD).getValue(User::class.java)
                                        // It prevent for remove user which we not delete, we must delete only currentUser, userFollowing could have other user which he follow
                                        if (userFollowers?.user_id.equals(userToUnfollowId)) {
                                            childSingleSnapshot.ref.removeValue()
                                        }
                                    }
                                }
                                if(userFollowing?.user_id.equals(currentUserId)){ // Delete me.
                                    childSingleSnapshot.ref.removeValue()
                                }
                            }
                            override fun onCancelled(databaseError: DatabaseError?) {}
                        })
                    }
                }
            }
            override fun onCancelled(databaseError: DatabaseError?) {}
        })
    }

}
