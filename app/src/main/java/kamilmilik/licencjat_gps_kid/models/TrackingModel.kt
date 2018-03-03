package kamilmilik.licencjat_gps_kid.models

/**
 * Created by kamil on 23.02.2018.
 */
class TrackingModel{
    var email: String = ""
    var user_id: String? = null
    var lat : String? = null
    var lng : String? = null

    constructor() {}
    constructor(user_id : String,email : String, lat : String, lng : String){
        this.user_id = user_id
        this.email = email
        this.lat = lat
        this.lng = lng
    }
}