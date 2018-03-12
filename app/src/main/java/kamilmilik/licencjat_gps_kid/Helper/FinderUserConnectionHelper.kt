package kamilmilik.licencjat_gps_kid.Helper

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import kamilmilik.licencjat_gps_kid.Utils.OnGetDataListener
import kamilmilik.licencjat_gps_kid.Utils.OnItemClickListener
import kamilmilik.licencjat_gps_kid.Utils.RecyclerViewAdapter
import kamilmilik.licencjat_gps_kid.models.User

/**
 * Created by kamil on 24.02.2018.
 */
class FinderUserConnectionHelper(var context: Context,
                                 var listener: OnItemClickListener,
                                 var valueSet: HashSet<String>,
                                 var adapter: RecyclerViewAdapter,
                                 var recyclerView: RecyclerView,
                                 var locationFirebaseHelper: LocationFirebaseHelper,
                                 var permissionHelper: PermissionHelper) : OnGetDataListener {
    override fun onStart() {
        Log.i(TAG,"onStart")
    }

    override fun onSuccess(data: DataSnapshot) {
        Log.i(TAG,"onSuccess " + data)

    }

    override fun onFailed(databaseError: DatabaseError) {
        Log.i(TAG,"onFailed")
    }

    private val TAG = FinderUserConnectionHelper::class.java.simpleName

    fun listenerForConnectionsUserChangeInFirebaseAndUpdateRecyclerView() {
        findFollowersConnectionAndUpdateRecyclerView()
    }

    private fun findFollowersConnectionAndUpdateRecyclerView() {
        Log.i(TAG, "findFollowersConnectionAndUpdateRecyclerView, current user id : " + FirebaseAuth.getInstance().currentUser!!.uid)
        var currentUser = FirebaseAuth.getInstance().currentUser
        val reference = FirebaseDatabase.getInstance().reference
        findFollowersUser(reference, currentUser!!,this)
        findFollowingUser(reference, currentUser!!,this)
        startGeofence()
    }
    private fun startGeofence(){
        var geofence = kamilmilik.licencjat_gps_kid.Geofence(context, permissionHelper, locationFirebaseHelper!!)
        geofence.startGeofence()
    }
    /**
     * listener for followers in database,  this method run listener for location change in database and add to recycler view followers user
     * @param reference
     * @param currentUser
     */
    private fun findFollowersUser(reference: DatabaseReference, currentUser: FirebaseUser, onGetDataListener: OnGetDataListener) {
        val query = reference.child("followers")
                .orderByKey()
                .equalTo(currentUser!!.uid)
        onGetDataListener.onStart()
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (singleSnapshot in dataSnapshot.children) {
                    for (childSingleSnapshot in singleSnapshot.children) {
                        var userFollowers = childSingleSnapshot.child("user").getValue(User::class.java)
                        Log.i(TAG, "value followers: " + userFollowers!!.user_id + " " + userFollowers.email)
                        valueSet.add(userFollowers.email)
                        locationFirebaseHelper!!.listenerForLocationsChangeInFirebase(userFollowers.user_id!!)
                    }
                }
                if (dataSnapshot.value == null) {//nothing found
                    Log.i(TAG, "nothing found in onDataChange in followers")

                } else {
                    for (user in valueSet) {
                        Log.i(TAG, "user complete : " + user)
                    }
                    onGetDataListener.onSuccess(dataSnapshot)
                    updateRecyclerView()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.i(TAG, "onCancelled: " + databaseError.message)
                onGetDataListener.onFailed(databaseError)
            }
        })

    }

    /**
     * listener for following in database,  this method run listener for location change in database and add to recycler view following user
     * @param reference
     * @param currentUser
     */
    private fun findFollowingUser(reference: DatabaseReference, currentUser: FirebaseUser, onGetDataListener: OnGetDataListener) {
        val query = reference.child("following")
                .orderByKey()
                .equalTo(currentUser!!.uid)
        onGetDataListener.onStart()
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (singleSnapshot in dataSnapshot.children) {
                    for (childSingleSnapshot in singleSnapshot.children) {
                        var userFollowing = childSingleSnapshot.child("user").getValue(User::class.java)
                        Log.i(TAG, "value following: " + userFollowing!!.user_id + " " + userFollowing!!.email)
                        valueSet.add(userFollowing!!.email)
                        locationFirebaseHelper!!.loadLocationsFromDatabaseForCurrentUser(userFollowing.user_id!!)
                    }
                }
                if (dataSnapshot.value == null) {//nothing found
                    Log.i(TAG, "nothing found in onDataChange in following")
                } else {
                    for (user in valueSet) {
                        Log.i(TAG, "user complete : " + user)
                    }
                    onGetDataListener.onSuccess(dataSnapshot)
                    updateRecyclerView()
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Log.i(TAG, "onCancelled: " + databaseError.message)
                onGetDataListener.onFailed(databaseError)
            }
        })
    }

    private fun updateRecyclerView() {
        var currentUser = FirebaseAuth.getInstance().currentUser
        valueSet.add(currentUser!!.email + " (ja)")
        var valueList = ArrayList(valueSet)
        for (user in valueList) {
            Log.i(TAG, "user complete Show set : " + user)
        }
        adapter = RecyclerViewAdapter(context, valueList)
        recyclerView.adapter = adapter
        adapter.setClickListener(listener)

        adapter.notifyDataSetChanged()
    }

}