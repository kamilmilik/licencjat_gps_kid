package kamilmilik.gps_tracker.models

import com.google.firebase.database.Query

data class QueryUserModel(val userId: String, val query: Query) {

//    override fun equals(other: Any?): Boolean {
//        if (this === other) return true
//        if (other !is QueryUserModel) return false
//
//        return this.query == other.query && this.userId == other.userId
//    }
//
//    override fun hashCode(): Int {
//        var hash = 3
//        hash = 7 * hash + this.query.hashCode()
//        hash = 7 * hash + this.userId.hashCode()
//        return hash
//    }
}