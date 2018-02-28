package kamilmilik.licencjat_gps_kid.models

/**
 * Created by kamil on 19.02.2018.
 */
class User{
    var email: String = ""
    var userId : String? = null

    constructor() {}
    constructor(userId : String,email : String){
        this.userId = userId
        this.email = email
    }
}