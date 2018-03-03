package kamilmilik.licencjat_gps_kid.models

/**
 * Created by kamil on 19.02.2018.
 */
class User{
    var email: String = ""
    var user_id: String? = null
    var device_token: String? = null

    constructor() {}
    constructor(userId : String,email : String, deviceToken : String){
        this.user_id = userId
        this.email = email
        this.device_token = deviceToken
    }
}