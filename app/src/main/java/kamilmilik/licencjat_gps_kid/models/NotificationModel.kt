package kamilmilik.licencjat_gps_kid.models

/**
 * Created by kamil on 01.05.2018.
 */
class NotificationModel{
    var from : String? = null
    var type : String? = null
    constructor(){}
    constructor(from : String, type : String){
        this.from = from
        this.type = type
    }
}