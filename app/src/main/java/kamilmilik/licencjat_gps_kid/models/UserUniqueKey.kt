package kamilmilik.licencjat_gps_kid.models

/**
 * Created by kamil on 20.02.2018.
 */
class UserUniqueKey{
    var uniqueKey: String = ""
    var userId : String? = null
    var userEmail : String? = null
    var time : Long? = null
    constructor() {}
    constructor(userId : String, userEmail : String,uniqueKey: String){
        this.userId = userId
        this.uniqueKey = uniqueKey
        this.userEmail = userEmail
    }
}