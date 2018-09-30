package kamilmilik.gps_tracker.models

/**
 * Created by kamil on 20.02.2018.
 */
// Class representing the firebase node.
class UserUniqueKey {
    var unique_key: String? = ""
    var user_id: String? = null
    var time: Long? = null

    constructor() {}
    constructor(userId: String, unique_key: String) {
        this.user_id = userId
        this.unique_key = unique_key
    }
}