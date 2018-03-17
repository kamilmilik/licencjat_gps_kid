package kamilmilik.licencjat_gps_kid.models

/**
 * Created by kamil on 17.03.2018.
 */
class MyOwnLatLng{
    var latitude : Double? = null
    var longitude: Double? = null
    constructor(){}
    constructor(latitude : Double, longitude : Double){
        this.latitude = latitude
        this.longitude = longitude
    }
}