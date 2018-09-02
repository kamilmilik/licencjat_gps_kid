package kamilmilik.gps_tracker.models

/**
 * Created by kamil on 11.08.2018.
 */
// Class representing the firebase node.
class ConnectionUser {
    var email: String = ""
    var user_id: String? = null
    var user_name: String? = null

    constructor() {}
    constructor(userId: String, email: String, userName: String) {
        this.user_id = userId
        this.email = email
        this.user_name = userName
    }
}