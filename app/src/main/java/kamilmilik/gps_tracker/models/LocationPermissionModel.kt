package kamilmilik.gps_tracker.models

class LocationPermissionModel {
    var isPermissionGranted: Boolean? = null

    constructor() {}
    constructor(isPermissionGranted: Boolean) {
        this.isPermissionGranted = isPermissionGranted
    }
}