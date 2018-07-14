package kamilmilik.gps_tracker.map

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kamilmilik.gps_tracker.utils.Constants
import kamilmilik.gps_tracker.models.User
import kamilmilik.gps_tracker.models.UserMarkerInformationModel

/**
 * Created by kamil on 24.02.2018.
 */
class FinderUserConnection(private var mapActivity: MapActivity) : BasicListenerContent() {

    private val TAG = FinderUserConnection::class.java.simpleName

    fun findFollowersConnectionAndUpdateRecyclerView() {
        Log.i(TAG, "findFollowersConnectionAndUpdateRecyclerView, current user id : " + FirebaseAuth.getInstance().currentUser!!.uid)
        val reference = FirebaseDatabase.getInstance().reference

        findConnectedUser(Constants.DATABASE_FOLLOWING)
        findConnectedUser(Constants.DATABASE_FOLLOWERS)

        reloadScreenAfterDeleteUser(reference)
    }

    private fun findConnectedUser(nodeName : String) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val reference = FirebaseDatabase.getInstance().reference
        val query = reference.child(nodeName)
                .orderByKey()
                .equalTo(currentUser!!.uid)
        childEventAction(query)
    }

    private fun childEventAction(query: Query){
        query.addChildEventListener(object : ChildEventListener {
            override fun onCancelled(p0: DatabaseError?) {}

            override fun onChildMoved(dataSnapshot: DataSnapshot?, p1: String?) {}

            override fun onChildChanged(dataSnapshot: DataSnapshot?, p1: String?) {
                Log.i(TAG, "onChildChanged()")
                userInFollowingSystemAction(dataSnapshot)
                putChildEventListenersToMap(query, this)
            }

            override fun onChildAdded(dataSnapshot: DataSnapshot?, previousKey: String?) {
                Log.i(TAG, "onChildAdded()")
                userInFollowingSystemAction(dataSnapshot)
                putChildEventListenersToMap(query, this)
            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot?) {
                Log.i(TAG, "onChildRemoved()")
                mapActivity.getActivity().recreate()
                putChildEventListenersToMap(query, this)
            }
        })
    }

    private fun userInFollowingSystemAction(dataSnapshot: DataSnapshot?) {
        for (singleSnapshot in dataSnapshot!!.children) {
            for (childSingleSnapshot in singleSnapshot.children) {
                val user = childSingleSnapshot!!.getValue(User::class.java)
                Log.i(TAG, "value following system: " + user!!.user_id + " " + user.email)
                val userInformation = UserMarkerInformationModel(user.email, user.user_name!!, user.user_id!!)

                mapActivity.updateChangeUserNameInRecycler(userInformation)
                Log.i(TAG,"userInFollowingSystemAction() startuje userLocationAction")
                mapActivity.userLocationAction(user)
            }
        }
    }

    private fun reloadScreenAfterDeleteUser(reference: DatabaseReference){
        var query = reference.child(Constants.DATABASE_FOLLOWERS)
        childRemovedAction(query)
        query = reference.child(Constants.DATABASE_FOLLOWING)
        childRemovedAction(query)
    }

    private fun childRemovedAction(query: Query){
            query.addChildEventListener(object : ChildEventListener {
                override fun onCancelled(p0: DatabaseError?) {}

                override fun onChildMoved(dataSnapshot: DataSnapshot?, p1: String?) {}

                override fun onChildChanged(dataSnapshot: DataSnapshot?, p1: String?) {}

                override fun onChildAdded(dataSnapshot: DataSnapshot?, previousKey: String?) {}

                override fun onChildRemoved(dataSnapshot: DataSnapshot?) {
                    Log.i(TAG, "onChildRemoved()")
                    mapActivity.getActivity().recreate()
                    putChildEventListenersToMap(query, this)
                }
            })
    }

}