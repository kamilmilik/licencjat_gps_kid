package kamilmilik.gps_tracker.models

/**
 * Created by kamil on 11.08.2018.
 */
// Class representing the firebase node.
class ConnectionUser {
    var user_id: String? = null

    constructor() {}
    constructor(userId: String) {
        this.user_id = userId
    }
}