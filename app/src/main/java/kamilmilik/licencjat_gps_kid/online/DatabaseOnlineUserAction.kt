package kamilmilik.licencjat_gps_kid.online

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.iid.FirebaseInstanceId
import kamilmilik.licencjat_gps_kid.models.User

/**
 * Created by kamil on 24.02.2018.
 */
class DatabaseOnlineUserAction {
    private val TAG: String = DatabaseOnlineUserAction::class.java.simpleName

    private var onlineRef: DatabaseReference = FirebaseDatabase.getInstance().reference.child(".info/connected")
    private var currentUserRef: DatabaseReference = FirebaseDatabase.getInstance().getReference("last_online").child(FirebaseAuth.getInstance().currentUser!!.uid)
    private var counterRef: DatabaseReference = FirebaseDatabase.getInstance().getReference("last_online")

    fun addOnlineUserToDatabase() {
        onlineRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {}
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var connected = dataSnapshot.getValue(Boolean::class.java)
                if (connected!!) {
                    currentUserRef.onDisconnect().removeValue()//Remove the value at this location when the client disconnects
                    //add to last_online current user
                    var deviceTokenId = FirebaseInstanceId.getInstance().token

                    counterRef.child(FirebaseAuth.getInstance().currentUser!!.uid).setValue(User(FirebaseAuth.getInstance().currentUser!!.uid, FirebaseAuth.getInstance().currentUser!!.email!!, deviceTokenId!!, FirebaseAuth.getInstance().currentUser!!.displayName!!))
                }
            }
        })
    }

    fun logoutUser() {
        if (currentUserRef != null && counterRef != null && currentUserRef != null && FirebaseAuth.getInstance() != null) {
            currentUserRef.onDisconnect().removeValue()
            counterRef.onDisconnect().removeValue()
            currentUserRef.removeValue()
            FirebaseAuth.getInstance().signOut()
        }
    }
}