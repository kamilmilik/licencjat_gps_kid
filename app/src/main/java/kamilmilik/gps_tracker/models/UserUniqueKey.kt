package kamilmilik.gps_tracker.models

/**
 * Created by kamil on 20.02.2018.
 */
// Class representing the firebase node.
class UserUniqueKey {
    var unique_key: String? = ""
    var user_id: String? = null
    var user_email: String? = null
    var user_name: String? = null
    var time: Long? = null

    constructor() {}
    constructor(userId: String, user_email: String, unique_key: String, user_name: String) {
        this.user_id = userId
        this.unique_key = unique_key
        this.user_email = user_email
        this.user_name = user_name
    }
}