package kamilmilik.licencjat_gps_kid.Helper

import android.content.Context
import android.location.Location
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
class FinderUserConnectionHelper(var context : Context, var listener : OnItemClickListener, var valueSet: HashSet<String>, var adapter : RecyclerViewAdapter, var recyclerView: RecyclerView, var locationsFirebaseHelper: LocationsFirebaseHelper){
    private val TAG = FinderUserConnectionHelper::class.java.simpleName
    fun listenerForConnectionsUserChangeinFirebaseAndUpdateRecyclerView(){
        findFollowersConnectionAndUpdateRecyclerView()
    }
    fun findFollowersConnectionAndUpdateRecyclerView(){
        Log.i(TAG,"findFollowersConnectionAndUpdateRecyclerView, current user id : " + FirebaseAuth.getInstance().currentUser!!.uid)
        var currentUser = FirebaseAuth.getInstance().currentUser
        val reference = FirebaseDatabase.getInstance().reference
        findFollowersUser(reference, currentUser!!)
        findFollowingUser(reference, currentUser!!)

    }
    private fun findFollowersUser(reference : DatabaseReference, currentUser : FirebaseUser){
        val query = reference.child("followers")
                .orderByKey()
                .equalTo(currentUser!!.uid)
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (singleSnapshot in dataSnapshot.children) {
                    for(childSingleSnapshot in singleSnapshot.children){
                        var userFollowers = childSingleSnapshot.child("user").getValue(User::class.java)
                        Log.i(TAG,"value followers: " + userFollowers.userId + " " + userFollowers.email)
                        valueSet.add(userFollowers.email)
                        Log.i(TAG,"!!!!!!!!!!! user send to add to map : " + userFollowers.email + " " + userFollowers.userId)
                        locationsFirebaseHelper!!.listenerForLocationsChangeInFirebase(userFollowers.userId!!)
                    }
                    locationsFirebaseHelper!!.listenerForLocationsChangeInFirebase(currentUser.uid)
                    Log.i(TAG,"UWAGAAAAA " + currentUser!!.uid)
                }
                if(dataSnapshot.value == null){//nothing found
                    Log.i(TAG,"nothing found in onDataChange in followers")
                }else{
                    for(user in valueSet){
                        Log.i(TAG,"user complete : " + user)
                    }
                    updateRecyclerView()
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Log.i(TAG,"onCancelled: " + databaseError.message)
            }
        })

    }
    private fun findFollowingUser(reference : DatabaseReference, currentUser : FirebaseUser){
        val query = reference.child("following")
                .orderByKey()
                .equalTo(currentUser!!.uid)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (singleSnapshot in dataSnapshot.children) {
                    locationsFirebaseHelper!!.listenerForLocationsChangeInFirebase(currentUser.uid)
                    for(childSingleSnapshot in singleSnapshot.children){
                        var userFollowing = childSingleSnapshot.child("user").getValue(User::class.java)
                        Log.i(TAG,"value following: " + userFollowing.userId + " " + userFollowing.email)
                        valueSet.add(userFollowing.email)
                        Log.i(TAG,"!!!!!!!!!!! user send to add to map : " + userFollowing.email + " " + userFollowing.userId)
                        locationsFirebaseHelper!!.loadLocationsFromDatabaseForCurrentUser(userFollowing.userId!!)
                    }
                }
                if(dataSnapshot.value == null){//nothing found
                    Log.i(TAG,"nothing found in onDataChange in following")
                }else{
                    for(user in valueSet){
                        Log.i(TAG,"user complete : " + user)
                    }
                    updateRecyclerView()
                }

            }
            override fun onCancelled(databaseError: DatabaseError) {
                Log.i(TAG,"onCancelled: " + databaseError.message)
            }
        })
    }
    private fun updateRecyclerView(){
        var currentUser = FirebaseAuth.getInstance().currentUser
        valueSet.add(currentUser!!.email+" (ja)")
        var valueList = ArrayList(valueSet)
        for(user in valueList){
            Log.i(TAG,"user complete Show set : " + user)
        }
        adapter = RecyclerViewAdapter(context, valueList)
        recyclerView.adapter = adapter
        adapter.setClickListener(listener)

        adapter.notifyDataSetChanged()
    }

}