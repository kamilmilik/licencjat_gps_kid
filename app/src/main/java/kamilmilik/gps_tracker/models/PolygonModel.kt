package kamilmilik.gps_tracker.models

import kamilmilik.gps_tracker.map.adapter.GeoLatLng


/**
 * Created by kamil on 17.03.2018.
 */
class PolygonModel{
    var tag : String? = null
    var polygonLatLngList: ArrayList<GeoLatLng> = ArrayList()
    constructor(){}
    constructor(tag : String, list : ArrayList<GeoLatLng>){
        this.tag = tag;
        this.polygonLatLngList = list
    }
}