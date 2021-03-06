package kamilmilik.gps_tracker.map

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kamilmilik.gps_tracker.R
import kamilmilik.gps_tracker.models.ConnectionUser
import kamilmilik.gps_tracker.models.User
import kamilmilik.gps_tracker.utils.listeners.IRecyclerViewListener
import kamilmilik.gps_tracker.models.UserBasicInfo
import kamilmilik.gps_tracker.utils.Constants
import kamilmilik.gps_tracker.utils.ObjectsUtils
import kamilmilik.gps_tracker.utils.Tools

/**
 * Created by kamil on 30.05.2018.
 */
class RecyclerViewAction(private var mapActivity: MapActivity) : IRecyclerViewListener {

    private val TAG = RecyclerViewAction::class.java.simpleName

    private lateinit var adapter: RecyclerViewAdapter

    private lateinit var recyclerView: RecyclerView

    private lateinit var valueSet: HashSet<UserBasicInfo>

    fun setupRecyclerView() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        recyclerView = mapActivity.getActivity().findViewById(R.id.listOnline)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(mapActivity.getActivity(), LinearLayoutManager.HORIZONTAL, false)

        valueSet = HashSet()
        FirebaseAuth.getInstance().currentUser?.let {
            ObjectsUtils.safeLetFirebaseUser(currentUser) { currentUserUid, currentUserEmail, currentUserName ->
                valueSet.add(UserBasicInfo(currentUserUid, currentUserEmail, currentUserName))
                val valueList = ArrayList(valueSet)
                adapter = RecyclerViewAdapter(mapActivity.getActivity(), valueList)
                recyclerView.adapter = adapter
                adapter.setClickListener(this)
            }
        }
    }

    fun updateRecyclerView() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        ObjectsUtils.safeLetFirebaseUser(currentUser) { currentUserUid, currentUserEmail, currentUserName ->
            valueSet.add(UserBasicInfo(currentUserUid, currentUserEmail, currentUserName))
            notifyDataSetChanged()
        }
    }

    fun changeUserNameAndNotifyDataSetChanged(userMarkerInformation: UserBasicInfo) {
        updateChangeUserNameInRecycler(userMarkerInformation)
        notifyDataSetChanged()
    }

    fun removeUserFromRecyclerAndNotifyDataSetChanged(userIdToRemove: String) {
        removeUserFromRecycler(userIdToRemove)
        notifyDataSetChanged()
    }

    private fun removeUserFromRecycler(userIdToRemove: String) {
        val iterator = valueSet.iterator()
        while (iterator.hasNext()) {
            val value = iterator.next()
            if (value.userId == userIdToRemove) run {
                iterator.remove()
            }
        }
    }

    private fun notifyDataSetChanged() {
        val valueList = ArrayList(valueSet)
        adapter = RecyclerViewAdapter(mapActivity.getActivity(), valueList)
        recyclerView.adapter = adapter
        adapter.setClickListener(this)
        adapter.notifyDataSetChanged()
    }


    fun updateChangeUserNameInRecycler(userInformation: UserBasicInfo) {
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
        val valueList = ArrayList(valueSet)
        val clickedUser = valueList[position]
        mapActivity.goToThisMarker(clickedUser)
    }

    override fun setOnLongItemClick(view: View, position: Int): Boolean {
        val alert = Tools.makeAlertDialogBuilder(mapActivity.getActivity(), mapActivity.getActivity().getString(R.string.unfollowUserTitle), mapActivity.getActivity().getString(R.string.unfollowUser))
        alert.setPositiveButton(mapActivity.getActivity().getString(R.string.ok)) { dialog, whichButton ->
            unfollowUser(position)
        }
        alert.setNegativeButton(mapActivity.getActivity().getString(R.string.cancel)) { dialog, whichButton -> }
        alert.show()
        return true
    }

    private fun unfollowUser(position: Int) {
        val valueList = ArrayList(valueSet)
        val clickedUser = valueList[position]

        if (clickedUser.userId == FirebaseAuth.getInstance().currentUser?.uid) {
            Toast.makeText(mapActivity.getActivity(), mapActivity.getActivity().getString(R.string.unfollowYourselfInformation), Toast.LENGTH_LONG).show()
        } else {
            val reference = FirebaseDatabase.getInstance().reference
            removeUserFromFollowers(clickedUser.userId, reference, Constants.DATABASE_FOLLOWERS, Constants.DATABASE_FOLLOWING)
            removeUserFromFollowers(clickedUser.userId, reference, Constants.DATABASE_FOLLOWING, Constants.DATABASE_FOLLOWERS)
            Tools.removeUserPermission(reference, clickedUser.userId)
        }
    }

    private fun removeUserFromFollowers(userToUnfollowId: String, reference: DatabaseReference, nodeTable: String, nodeTable2: String) {
        val query = reference.child(nodeTable)
                .orderByKey()
                .equalTo(userToUnfollowId)
        removeUser(query, reference, userToUnfollowId, nodeTable2)
    }

    private fun removeUser(query: Query, reference: DatabaseReference, userToUnfollowId: String, nodeTable2: String) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (singleSnapshot in dataSnapshot.children) {
                    for (childSingleSnapshot in singleSnapshot.children) {
                        val user = childSingleSnapshot.child(Constants.DATABASE_USER_FIELD).getValue(ConnectionUser::class.java)
                        val query2 = reference.child(nodeTable2)
                                .orderByKey()
                                .equalTo(currentUserId)
                        query2.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                for (singleSnapshot2 in dataSnapshot.children) {
                                    for (childSingleSnapshot2 in singleSnapshot2.children) {
                                        val user2 = childSingleSnapshot2.child(Constants.DATABASE_USER_FIELD).getValue(ConnectionUser::class.java)
                                        if (user2?.user_id.equals(userToUnfollowId)) {
                                            childSingleSnapshot2.ref.removeValue()
                                        }
                                    }
                                }
                                if (user?.user_id.equals(currentUserId)) {
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
