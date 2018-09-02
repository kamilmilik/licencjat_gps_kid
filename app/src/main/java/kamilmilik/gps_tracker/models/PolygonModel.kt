package kamilmilik.gps_tracker.models

import kamilmilik.gps_tracker.map.adapter.GeoLatLng


/**
 * Created by kamil on 17.03.2018.
 */
// Class representing the firebase node.
class PolygonModel {
    var tag: String? = null
    var polygonLatLngList: ArrayList<GeoLatLng> = ArrayList()

    constructor() {}
    constructor(tag: String, polygonLatLngList: ArrayList<GeoLatLng>) {
        this.tag = tag;
        this.polygonLatLngList = polygonLatLngList
    }
}