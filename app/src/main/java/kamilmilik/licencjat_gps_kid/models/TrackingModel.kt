package kamilmilik.licencjat_gps_kid.models

/**
 * Created by kamil on 23.02.2018.
 */
class TrackingModel{
    var email: String = ""
    var userId : String? = null
    var lat : String? = null
    var lng : String? = null

    constructor() {}
    constructor(userId : String,email : String, lat : String, lng : String){
        this.userId = userId
        this.email = email
        this.lat = lat
        this.lng = lng
    }
}