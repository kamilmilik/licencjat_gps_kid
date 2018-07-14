package kamilmilik.gps_tracker.map

import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import java.util.HashMap

/**
 * Created by kamil on 14.06.2018.
 */
open class BasicListenerContent {
    private val firebaseDatabaseValueEventListenersMap: HashMap<Query, ValueEventListener>? = HashMap()

    private val firebaseDatabaseChildEventListenersMap: HashMap<Query, ChildEventListener>? = HashMap()

    fun putValueEventListenersToMap(query: Query, valueEventListener: ValueEventListener) {
        firebaseDatabaseValueEventListenersMap?.put(query, valueEventListener)
    }

    fun putChildEventListenersToMap(query: Query, childEventListener: ChildEventListener) {
        firebaseDatabaseChildEventListenersMap?.put(query, childEventListener)
    }

    fun removeValueEventListeners() {
        if (firebaseDatabaseValueEventListenersMap != null) {
            for ((key, value) in firebaseDatabaseValueEventListenersMap) {
                key.removeEventListener(value)
            }
        }
    }

    fun removeChildEventListeners() {
        if (firebaseDatabaseChildEventListenersMap != null) {
            for ((key, value) in firebaseDatabaseChildEventListenersMap) {
                key.removeEventListener(value)
            }
        }
    }

}