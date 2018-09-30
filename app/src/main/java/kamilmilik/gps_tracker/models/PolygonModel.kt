package kamilmilik.gps_tracker.models

import kamilmilik.gps_tracker.map.adapter.GeoLatLng


/**
 * Created by kamil on 17.03.2018.
 */
// Class representing the firebase node.
class PolygonModel {
    var tag: String? = null
    var polygon_lat_lng_list: ArrayList<GeoLatLng> = ArrayList()

    constructor() {}
    constructor(tag: String, polygonLatLngList: ArrayList<GeoLatLng>) {
        this.tag = tag
        this.polygon_lat_lng_list = polygonLatLngList
    }
}