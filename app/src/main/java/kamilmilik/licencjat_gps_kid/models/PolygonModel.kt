package kamilmilik.licencjat_gps_kid.models


/**
 * Created by kamil on 17.03.2018.
 */
class PolygonModel{
    var tag : String? = null
    var polygonLatLngList: ArrayList<MyOwnLatLng> = ArrayList()
    constructor(){}
    constructor(tag : String, list : ArrayList<MyOwnLatLng>){
        this.tag = tag;
        this.polygonLatLngList = list
    }
}