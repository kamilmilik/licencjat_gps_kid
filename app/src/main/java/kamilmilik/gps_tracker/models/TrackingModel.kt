package kamilmilik.gps_tracker.models


/**
 * Created by kamil on 23.02.2018.
 */
// Class representing the firebase node.
class TrackingModel{
    var email: String = ""
    var user_id: String? = null
    var lat : Double? = null
    var lng : Double? = null
    var user_name :String? = null

    constructor() {}
    constructor(user_id: String, email: String, lat: Double, lng: Double, user_name : String){
        this.user_id = user_id
        this.email = email
        this.lat = lat
        this.lng = lng
        this.user_name = user_name
    }


}