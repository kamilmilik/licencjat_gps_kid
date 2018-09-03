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

    fun logoutUser() {
        FirebaseAuth.getInstance().let{
            currentUserRef.onDisconnect().removeValue()
            counterRef.onDisconnect().removeValue()
            currentUserRef.removeValue()
            FirebaseAuth.getInstance().signOut()
        }
    }

}