package kamilmilik.gps_tracker.map

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kamilmilik.gps_tracker.models.QueryUserModel
import kamilmilik.gps_tracker.utils.Constants
import kamilmilik.gps_tracker.models.User
import kamilmilik.gps_tracker.models.UserBasicInfo
import kamilmilik.gps_tracker.utils.Constants.DATABASE_FOLLOWERS
import kamilmilik.gps_tracker.utils.Constants.DATABASE_FOLLOWING
import kamilmilik.gps_tracker.utils.Constants.DATABASE_USER_FIELD
import kamilmilik.gps_tracker.utils.ObjectsUtils

/**
 * Created by kamil on 24.02.2018.
 */
class NotifyOtherUsersChanges(private var mapActivity: MapActivity) : BasicListenerContent() {

    private val TAG = NotifyOtherUsersChanges::class.java.simpleName

    fun findUsersConnectionUpdateRecyclerViewOrDeleteRemovedUserData() {
        Log.i(TAG, "findUsersConnectionUpdateRecyclerViewOrDeleteRemovedUserData, current user id : " + FirebaseAuth.getInstance().currentUser?.uid)
        val reference = FirebaseDatabase.getInstance().reference
        FirebaseAuth.getInstance().currentUser?.uid?.let { currentUserId ->
            connectedUserAction(reference, DATABASE_FOLLOWERS, currentUserId)
            connectedUserAction(reference, DATABASE_FOLLOWING, currentUserId)
        }
    }

    private fun connectedUserAction(reference: DatabaseReference, databaseNode: String, userIdQuery: String) {
        val query = reference.child(databaseNode).orderByKey().equalTo(userIdQuery)
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (singleSnapshot in dataSnapshot.children) {
                    for (childSingleSnapshot in singleSnapshot.children) {
                        childSingleSnapshot.child(Constants.DATABASE_USER_FIELD).getValue(User::class.java)?.let { user ->
                            ObjectsUtils.safeLet(user.user_id, user.email, user.user_name) { userId, userEmail, userName ->
                                Log.i(TAG, "value connectedUserAction : " + userId + " " + userEmail + " " + userName)
                                val userInformation = UserBasicInfo(userId, userEmail, userName)

                                mapActivity.updateChangeOthersUserNameInRecycler(userInformation)
                                Log.i(TAG, "userInFollowingSystemAction() startuje userLocationAction")
                                mapActivity.userLocationAction(user)

                                notifyWhenChildIsRemovedAndThenDeleteHisData(reference, userId, databaseNode, userIdQuery)
                            }
                        }
                    }
                }
                putValueEventListenersToMap(QueryUserModel(userIdQuery, query), this)
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    fun notifyWhenChildIsRemovedAndThenDeleteHisData(reference: DatabaseReference, childId: String, databaseNode: String, currentUserId: String) {
        Log.i(TAG, "notifyWhenChildIsRemovedAndThenDeleteHisData() na user " + childId + " node " + databaseNode)
        val query = reference.child(databaseNode).child(currentUserId).orderByKey().equalTo(childId)
        this.onChildRemovedAction(childId, query)
    }

    private fun onChildRemovedAction(queryUserId: String, query: Query) {
        query.addChildEventListener(object : ChildEventListener {
            override fun onCancelled(p0: DatabaseError?) {}

            override fun onChildMoved(dataSnapshot: DataSnapshot?, p1: String?) {}

            override fun onChildChanged(dataSnapshot: DataSnapshot?, p1: String?) {}

            override fun onChildAdded(dataSnapshot: DataSnapshot?, previousKey: String?) {}

            override fun onChildRemoved(dataSnapshot: DataSnapshot?) {
                dataSnapshot?.let {
                    val removedUserId = dataSnapshot.key
                    Log.i(TAG, "onChildRemovedAction() dataSnap " + dataSnapshot)
                    Log.i(TAG, "onChildRemovedAction() removed user id " + removedUserId)
                    removeUserFromAppAction(dataSnapshot)
                }
                putChildEventListenersToMap(QueryUserModel(queryUserId, query), this)
            }
        })
    }

    private fun removeUserFromAppAction(dataSnapshot: DataSnapshot?) {
        dataSnapshot?.let {
            Log.i(TAG, "removeUserFromAppAction() dataSnap " + dataSnapshot)
            val removedUser = dataSnapshot.child(DATABASE_USER_FIELD).getValue(User::class.java)
            ObjectsUtils.safeLet(removedUser?.user_id, removedUser?.email, removedUser?.user_name) { id, email, name ->
                val removedUserInfo = UserBasicInfo(id, email, name)
                clearUserDataFromApp(removedUserInfo)
            }
        }
    }

    private fun clearUserDataFromApp(userBasicInfo: UserBasicInfo) {
        val userIdToRemove = userBasicInfo.userId
        mapActivity.removeAllEventListenersForGivenUserId(userIdToRemove)
        mapActivity.removeMarkerFromMapForGivenUser(userBasicInfo) // Remember to first delete user from map next from recycler
        mapActivity.removeUserFromRecycler(userIdToRemove)
    }
}