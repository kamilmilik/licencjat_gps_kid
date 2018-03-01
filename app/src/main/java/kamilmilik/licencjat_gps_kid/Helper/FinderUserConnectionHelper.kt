package kamilmilik.licencjat_gps_kid.Helper

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
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
                                 var locationFirebaseHelper: LocationFirebaseHelper) {
    private val TAG = FinderUserConnectionHelper::class.java.simpleName
    fun listenerForConnectionsUserChangeinFirebaseAndUpdateRecyclerView() {
        findFollowersConnectionAndUpdateRecyclerView()
    }

    private fun findFollowersConnectionAndUpdateRecyclerView() {
        Log.i(TAG, "findFollowersConnectionAndUpdateRecyclerView, current user id : " + FirebaseAuth.getInstance().currentUser!!.uid)
        var currentUser = FirebaseAuth.getInstance().currentUser
        val reference = FirebaseDatabase.getInstance().reference
        findFollowersUser(reference, currentUser!!)
        findFollowingUser(reference, currentUser!!)
    }

    /**
     * listener for followers in database,  this method run listener for location change in database and add to recycler view followers user
     * @param reference
     * @param currentUser
     */
    private fun findFollowersUser(reference: DatabaseReference, currentUser: FirebaseUser) {
        val query = reference.child("followers")
                .orderByKey()
                .equalTo(currentUser!!.uid)
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (singleSnapshot in dataSnapshot.children) {
                    for (childSingleSnapshot in singleSnapshot.children) {
                        var userFollowers = childSingleSnapshot.child("user").getValue(User::class.java)
                        Log.i(TAG, "value followers: " + userFollowers!!.userId + " " + userFollowers.email)
                        valueSet.add(userFollowers.email)
                        locationFirebaseHelper!!.listenerForLocationsChangeInFirebase(userFollowers.userId!!)
                    }
                }
                if (dataSnapshot.value == null) {//nothing found
                    Log.i(TAG, "nothing found in onDataChange in followers")

                } else {
                    for (user in valueSet) {
                        Log.i(TAG, "user complete : " + user)
                    }
                    updateRecyclerView()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.i(TAG, "onCancelled: " + databaseError.message)
            }
        })

    }

    /**
     * listener for following in database,  this method run listener for location change in database and add to recycler view following user
     * @param reference
     * @param currentUser
     */
    private fun findFollowingUser(reference: DatabaseReference, currentUser: FirebaseUser) {
        val query = reference.child("following")
                .orderByKey()
                .equalTo(currentUser!!.uid)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (singleSnapshot in dataSnapshot.children) {
                    for (childSingleSnapshot in singleSnapshot.children) {
                        var userFollowing = childSingleSnapshot.child("user").getValue(User::class.java)
                        Log.i(TAG, "value following: " + userFollowing!!.userId + " " + userFollowing!!.email)
                        valueSet.add(userFollowing!!.email)
                        locationFirebaseHelper!!.loadLocationsFromDatabaseForCurrentUser(userFollowing.userId!!)
                    }
                }
                if (dataSnapshot.value == null) {//nothing found
                    Log.i(TAG, "nothing found in onDataChange in following")
                } else {
                    for (user in valueSet) {
                        Log.i(TAG, "user complete : " + user)
                    }
                    updateRecyclerView()
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Log.i(TAG, "onCancelled: " + databaseError.message)
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