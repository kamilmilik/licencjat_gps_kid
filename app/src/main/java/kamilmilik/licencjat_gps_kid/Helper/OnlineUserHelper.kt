package kamilmilik.licencjat_gps_kid.Helper

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kamilmilik.licencjat_gps_kid.models.User

/**
 * Created by kamil on 24.02.2018.
 */
class OnlineUserHelper {
    private val TAG : String = OnlineUserHelper::class.java.simpleName
    lateinit var onlineRef : DatabaseReference
    lateinit var currentUserRef : DatabaseReference
    lateinit var counterRef : DatabaseReference
    fun addOnlineUserToDatabase() {
        Log.i(TAG, "addOnlineUserToDatabase: set up online account to list")
        onlineRef = FirebaseDatabase.getInstance().reference.child(".info/connected")
        counterRef = FirebaseDatabase.getInstance().getReference("last_online")
        currentUserRef = FirebaseDatabase.getInstance().getReference("last_online").child(FirebaseAuth.getInstance().currentUser!!.uid)
        onlineRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {
                System.err.println("Listener was cancelled")
            }
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var connected = dataSnapshot.getValue(Boolean::class.java)
                if (connected) {
                    currentUserRef.onDisconnect().removeValue()//Remove the value at this location when the client disconnects
                    //add to last_online current user
                    counterRef.child(FirebaseAuth.getInstance().currentUser!!.uid).setValue(User(FirebaseAuth.getInstance().currentUser!!.uid, FirebaseAuth.getInstance().currentUser!!.email!!))
                    // adapter!!.notifyDataSetChanged()
                }
            }
        })
    }
    fun joinUserAction(){
        counterRef.child(FirebaseAuth.getInstance().currentUser!!.uid).setValue(User(FirebaseAuth.getInstance().getCurrentUser()!!.uid,FirebaseAuth.getInstance().getCurrentUser()!!.getEmail()!!))
    }
    fun logoutUser(){
        currentUserRef.onDisconnect().removeValue()
        counterRef.onDisconnect().removeValue()
        currentUserRef.removeValue()
        FirebaseAuth.getInstance().signOut()
    }
}