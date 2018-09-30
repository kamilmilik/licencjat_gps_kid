package kamilmilik.gps_tracker.map

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import kamilmilik.gps_tracker.models.*
import kamilmilik.gps_tracker.utils.Constants.DATABASE_FOLLOWERS
import kamilmilik.gps_tracker.utils.Constants.DATABASE_FOLLOWING
import kamilmilik.gps_tracker.utils.Constants.DATABASE_LOCATIONS
import kamilmilik.gps_tracker.utils.Constants.DATABASE_USER_FIELD
import kamilmilik.gps_tracker.utils.ObjectsUtils
import kamilmilik.gps_tracker.utils.Tools
import kamilmilik.gps_tracker.R
import kamilmilik.gps_tracker.utils.Constants.DATABASE_DATA_FIELD
import kamilmilik.gps_tracker.utils.Constants.DATABASE_PERMISSIONS_FIELD

/**
 * Created by kamil on 24.02.2018.
 */
class NotifyOtherUsersChanges(private var mapActivity: MapActivity) : BasicListenerContent() {

    private val TAG = NotifyOtherUsersChanges::class.java.simpleName

    private var reference: DatabaseReference = FirebaseDatabase.getInstance().reference

    private var currentUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser

    fun notifyOtherUserChangesAction() {
        findUsersConnectionUpdateRecyclerViewOrDeleteRemovedUserDataAndCheckLocationPermissions()
    }

    private fun findUsersConnectionUpdateRecyclerViewOrDeleteRemovedUserDataAndCheckLocationPermissions() {
        currentUser?.uid?.let { currentUserId ->
            checkCurrentUserConnections(DATABASE_FOLLOWERS, currentUserId)
            checkCurrentUserConnections(DATABASE_FOLLOWING, currentUserId)
        }
    }

    private fun checkCurrentUserConnections(databaseNode: String, currentUserId: String) {
        val query = reference.child(databaseNode).child(currentUserId)
        query.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                dataSnapshot?.let {
                    for (singleSnapshot in dataSnapshot.children) {
                        for (childSnapshot in singleSnapshot.children) {
                            childSnapshot.getValue(ConnectionUser::class.java)?.let { connectedUser ->
                                connectedUser.user_id?.let { otherUserId ->
                                    checkIfCurrentUserGrantedOtherUser(currentUserId, otherUserId) // Check if current user granted other.
                                    notifyWhenChildIsRemovedAndThenDeleteHisData(databaseNode, currentUserId, otherUserId)
                                }
                                checkIfPermissionDialogShow(connectedUser, currentUserId)
                            }
                        }
                    }
                }
                putValueEventListenersToMap(QueryUserModel(currentUserId, query), this)
            }

            override fun onCancelled(p0: DatabaseError?) {}
        })

    }

    private fun checkIfCurrentUserGrantedOtherUser(currentUserId: String, otherUserId: String) {
        val query = reference.child(DATABASE_LOCATIONS).child(currentUserId).child(DATABASE_PERMISSIONS_FIELD).child(otherUserId)
        query.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {}

            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                checkIfOneUserGrantedOtherUserAndGetThisUserInfo(dataSnapshot, otherUserId)
                putValueEventListenersToMap(QueryUserModel(otherUserId, query), this)
            }
        })
    }

    private fun getUserInfoToLocationAndUserNameAction(otherUserId: String) {
        val query = reference.child(DATABASE_LOCATIONS).child(otherUserId).child(DATABASE_DATA_FIELD)/*orderByKey().equalTo(otherUserId)*/
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                dataSnapshot?.let {
                    val userInfoTracking = dataSnapshot.getValue(TrackingModel::class.java)
                    ObjectsUtils.safeLetTrackingModel(userInfoTracking) { userId, userEmail, userName, lat, lng ->
                        val userInformation = UserBasicInfo(userId, userEmail, userName)
                        mapActivity.updateChangeOthersUserNameInRecycler(userInformation)
                        mapActivity.userLocationAction(userId)
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError?) {
                databaseError?.let {
                    if (databaseError.code.equals(DatabaseError.PERMISSION_DENIED)) {
                        checkIfOtherUserGrantedCurrent(otherUserId)
                    }
                }
            }
        })

    }

    private fun checkIfOtherUserGrantedCurrent(otherUserId: String) {
        currentUser?.uid?.let { currentUserId ->
            val query = reference.child(DATABASE_LOCATIONS)
                    .child(otherUserId)
                    .child(DATABASE_PERMISSIONS_FIELD)
                    .child(currentUserId)
            query.addValueEventListener(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError?) {}

                override fun onDataChange(dataSnapshot: DataSnapshot?) {
                    checkIfOneUserGrantedOtherUserAndGetThisUserInfo(dataSnapshot, otherUserId)
                    putValueEventListenersToMap(QueryUserModel(currentUserId, query), this)
                }
            })
        }
    }

    private fun checkIfOneUserGrantedOtherUserAndGetThisUserInfo(dataSnapshot: DataSnapshot?, userToGetInfoId: String) {
        dataSnapshot?.let {
            dataSnapshot.getValue(LocationPermissionModel::class.java)?.isPermissionGranted?.let { isGranted ->
                if (isGranted) {
                    getUserInfoToLocationAndUserNameAction(userToGetInfoId)
                }
            }
        }
    }

    fun notifyWhenChildIsRemovedAndThenDeleteHisData(databaseNode: String, currentUserId: String, otherUserId: String) {
        val query = reference.child(databaseNode).child(currentUserId).orderByKey().equalTo(otherUserId)
        query.addChildEventListener(object : ChildEventListener {
            override fun onCancelled(p0: DatabaseError?) {}

            override fun onChildMoved(dataSnapshot: DataSnapshot?, p1: String?) {}

            override fun onChildChanged(dataSnapshot: DataSnapshot?, p1: String?) {}

            override fun onChildAdded(dataSnapshot: DataSnapshot?, previousKey: String?) {}

            override fun onChildRemoved(dataSnapshot: DataSnapshot?) {
                dataSnapshot?.let {
                    removeUserFromAppAction(dataSnapshot)
                }
                putChildEventListenersToMap(QueryUserModel(otherUserId, query), this)
            }
        })
    }

    private fun removeUserFromAppAction(dataSnapshot: DataSnapshot?) {
        dataSnapshot?.let {
            val removedUser = dataSnapshot.child(DATABASE_USER_FIELD).getValue(ConnectionUser::class.java)
            removedUser?.user_id?.let { removedUserId ->
                clearUserDataFromApp(removedUserId)
                Tools.removeUserPermission(reference, removedUserId)
            }
        }
    }

    private fun clearUserDataFromApp(userIdToRemove: String) {
        mapActivity.removeAllEventListenersForGivenUserId(userIdToRemove)
        mapActivity.removeMarkerFromMapForGivenUser(userIdToRemove)
        mapActivity.removeUserFromRecycler(userIdToRemove)
    }


    private fun checkIfPermissionDialogShow(otherUser: ConnectionUser, currentUserId: String) {
        val query = reference.child(DATABASE_LOCATIONS).child(currentUserId).child(DATABASE_PERMISSIONS_FIELD)
        query.addListenerForSingleValueEvent(object : ValueEventListener { // SingleValueEvent since checkCurrentUserConnections run every time when user add other.
            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                dataSnapshot?.let {
                    if (!dataSnapshot.exists()) { // No users in permissions.
                        getUserInformationAndShowAskDialog(otherUser, currentUserId)
                    } else {
                        if (!dataSnapshot.child(otherUser.user_id).exists()) { // Users in permissions but this user not exist there, so ask dialog for current user to granted or not permission.
                            getUserInformationAndShowAskDialog(otherUser, currentUserId)
                        }
                    }
                }
            }

            override fun onCancelled(p0: DatabaseError?) {}

        })
    }

    private fun getUserInformationAndShowAskDialog(user: ConnectionUser, currentUserId: String) {
        user.user_id?.let { otherUserId ->
            val query = reference.child(DATABASE_LOCATIONS).child(otherUserId).child(DATABASE_DATA_FIELD)
            query.addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(dataSnapshot: DataSnapshot?) {
                    dataSnapshot?.let {
                        askUserConnectionPermission(dataSnapshot, currentUserId)
                    }
                }

                override fun onCancelled(p0: DatabaseError?) {}

            })
        }
    }

    private fun askUserConnectionPermission(dataSnapshot: DataSnapshot, currentUserId: String) {
        val userInfoTracking = dataSnapshot.getValue(TrackingModel::class.java)
        ObjectsUtils.safeLetTrackingModel(userInfoTracking) { otherUserId, otherUserEmail, otherUserName, otherUserLat, otherUserLng ->
            Tools.makeAlertDialogBuilder(mapActivity.getActivity(), mapActivity.getString(R.string.followingTitle), mapActivity.getString(R.string.followingAttemptMessage, otherUserEmail, otherUserName))
                    .setPositiveButton(mapActivity.getString(R.string.ok)) { dialog, whichButton ->
                        reference.child(DATABASE_LOCATIONS).child(currentUserId).child(DATABASE_PERMISSIONS_FIELD).child(otherUserId).setValue(LocationPermissionModel(true))
                    }
                    .setNegativeButton(mapActivity.getString(R.string.cancel)) { dialog, wchichButton ->
                        // Clear data, so in next invitation ask user permission dialog will show.
                        clearUnneededDataAfterNotPermissionGranted(currentUserId, otherUserId)
                    }
                    .setCancelable(false) // Very important here!
                    .show()
        }
    }

    private fun clearUnneededDataAfterNotPermissionGranted(currentUserId: String, otherUserId: String) {
        reference.child(DATABASE_LOCATIONS).child(currentUserId).child(DATABASE_PERMISSIONS_FIELD).child(otherUserId).removeValue()
        reference.child(DATABASE_FOLLOWERS).child(currentUserId).child(otherUserId).removeValue()
        reference.child(DATABASE_FOLLOWING).child(currentUserId).child(otherUserId).removeValue()
        reference.child(DATABASE_FOLLOWERS).child(otherUserId).child(currentUserId).removeValue()
        reference.child(DATABASE_FOLLOWING).child(otherUserId).child(currentUserId).removeValue()
    }
}
