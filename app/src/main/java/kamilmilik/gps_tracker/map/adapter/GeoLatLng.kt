package kamilmilik.gps_tracker.map.adapter

/**
 * Created by kamil on 17.03.2018.
 */
class GeoLatLng {
    var latitude: Double? = null
    var longitude: Double? = null

    constructor() {}
    constructor(latitude: Double, longitude: Double) {
        this.latitude = latitude
        this.longitude = longitude
    }
}