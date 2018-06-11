package kamilmilik.licencjat_gps_kid.models

import kamilmilik.licencjat_gps_kid.map.adapter.GeoLatLng


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