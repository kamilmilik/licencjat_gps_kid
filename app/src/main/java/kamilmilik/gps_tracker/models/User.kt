package kamilmilik.gps_tracker.models

/**
 * Created by kamil on 19.02.2018.
 */
class User{
    var email: String = ""
    var user_id: String? = null
    var device_token: String? = null
    var user_name: String? = null

    constructor() {}
    constructor(userId : String,email : String, deviceToken : String, userName : String){
        this.user_id = userId
        this.email = email
        this.device_token = deviceToken
        this.user_name = userName
    }
//    constructor(userId : String,email : String, deviceToken : String){
//        this.user_id = userId
//        this.email = email
//        this.device_token = deviceToken
//    }
}