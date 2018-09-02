package kamilmilik.gps_tracker.login

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.iid.FirebaseInstanceId
import kamilmilik.gps_tracker.models.User
import kamilmilik.gps_tracker.utils.Constants
import kamilmilik.gps_tracker.utils.ObjectsUtils

/**
 * Created by kamil on 24.02.2018.
 */
class DatabaseOnlineUserAction {
    private val TAG: String = DatabaseOnlineUserAction::class.java.simpleName

    private var currentUserRef: DatabaseReference = FirebaseDatabase.getInstance().getReference(Constants.DATABASE_LAST_ONLINE).child(FirebaseAuth.getInstance().currentUser?.uid)

    private var counterRef: DatabaseReference = FirebaseDatabase.getInstance().getReference(Constants.DATABASE_LAST_ONLINE)

    fun addOnlineUserToDatabase() {
        FirebaseDatabase.getInstance().reference.child(Constants.DATABASE_WHO_IS_CONNECTED)
                .addValueEventListener(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError?) {}
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val connected = dataSnapshot.getValue(Boolean::class.java)
                        connected?.let {
                            if (connected) {
                                currentUserRef.onDisconnect().removeValue()// Remove the value at this location when the client disconnects.
                                // Add to last_online current user.
                                val deviceTokenId = FirebaseInstanceId.getInstance().token
                                if(deviceTokenId != null){
                                    FirebaseAuth.getInstance().currentUser?.let {currentUser ->
                                        ObjectsUtils.safeLetFirebaseUser(currentUser){ uid, email, name ->
                                             counterRef.child(currentUser.uid).setValue(User(uid, email, deviceTokenId, name))
                                        }
                                    }
                                }
                            }
                        }
                    }
                })
    }

    fun logoutUser() {
        FirebaseAuth.getInstance().let{
//            removeLoggedUser()
            currentUserRef.onDisconnect().removeValue()
            counterRef.onDisconnect().removeValue()
            currentUserRef.removeValue()
            FirebaseAuth.getInstance().signOut()
        }
    }

    private fun removeLoggedUser() {
        FirebaseDatabase.getInstance().reference?.child(Constants.DATABASE_USER_LOGGED)?.
                child(Constants.DATABASE_USER_FIELD)?.
                child(FirebaseAuth.getInstance()?.currentUser?.uid)?.removeValue()
    }
}