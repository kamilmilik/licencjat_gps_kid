package kamilmilik.gps_tracker.map

import android.util.Log
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.ValueEventListener
import kamilmilik.gps_tracker.models.QueryUserModel
import java.util.HashMap

/**
 * Created by kamil on 14.06.2018.
 */
open class BasicListenerContent {

    private val firebaseDatabaseValueEventListenersMap: HashMap<QueryUserModel, ValueEventListener> = HashMap()

    private val firebaseDatabaseChildEventListenersMap: HashMap<QueryUserModel, ChildEventListener> = HashMap()

    fun putValueEventListenersToMap(queryUserModel: QueryUserModel, valueEventListener: ValueEventListener) {
        firebaseDatabaseValueEventListenersMap.put(queryUserModel, valueEventListener)
    }

    fun putChildEventListenersToMap(queryUserModel: QueryUserModel, childEventListener: ChildEventListener) {
        firebaseDatabaseChildEventListenersMap.put(queryUserModel, childEventListener)
    }

    fun removeValueEventListeners() {
        for ((key, value) in firebaseDatabaseValueEventListenersMap) {
            key.query.removeEventListener(value)
        }
    }

    fun removeValueEventListenersForGivenUserId(userId: String) {
        for ((key, value) in firebaseDatabaseValueEventListenersMap) {
            if (key.userId == userId) {
                key.query.removeEventListener(value)
            }
        }
    }

    fun removeChildEventListeners() {
        for ((key, value) in firebaseDatabaseChildEventListenersMap) {
            key.query.removeEventListener(value)
        }
    }

    fun removeChildEventListenersForGivenUserId(userId: String) {
        for ((key, value) in firebaseDatabaseChildEventListenersMap) {
            if (key.userId == userId) {
                key.query.removeEventListener(value)
            }
        }
    }

}