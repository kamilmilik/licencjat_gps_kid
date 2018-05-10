package kamilmilik.licencjat_gps_kid.models

/**
 * Created by kamil on 20.02.2018.
 */
class UserUniqueKey{
    var unique_key: String = ""
    var user_id: String? = null
    var user_email: String? = null
    var device_token : String? = null
    var user_name : String? = null
    var time : Long? = null
    constructor() {}
    constructor(userId : String, user_email: String, unique_key: String, device_token : String, user_name : String){
        this.user_id = userId
        this.unique_key = unique_key
        this.user_email = user_email
        this.device_token = device_token
        this.user_name = user_name
    }
}