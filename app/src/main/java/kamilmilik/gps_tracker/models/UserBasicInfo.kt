package kamilmilik.gps_tracker.models


/**
 * Created by kamil on 29.04.2018.
 */
data class UserBasicInfo(var userId: String, var email: String, var userName: String) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is UserBasicInfo) return false

        return this.email == other.email && this.userName == other.userName && this.userId == other.userId
    }

    override fun hashCode(): Int {
        var hash = 3
        hash = 7 * hash + this.email.hashCode()
        hash = 7 * hash + this.userName.hashCode()
        hash = 7 * hash + this.userId.hashCode()
        return hash
    }
}