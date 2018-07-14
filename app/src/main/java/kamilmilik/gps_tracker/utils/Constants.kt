package kamilmilik.gps_tracker.utils

import com.google.android.gms.location.LocationRequest

/**
 * Created by kamil on 19.03.2018.
 */
object Constants{
    val SHARED_KEY = "shared"

    val MAP_CAMERA_ZOOM = 12.0f

    val SHARED_POLYGON_KEY = "map-polygon"
    //permission
    val MY_PERMISSION_REQUEST_CODE : Int = 99

    //notification
    val NOTIFICATION_CHANNEL_ID_ENTER = "2";

    val NOTIFICATION_CHANNEL_ID_EXIT = "3";

    var NOTIFICATION_ID_ENTER = 2;

    val NOTIFICATION_ID_EXIT = 3;

    val NOTIFICATION_ID_GET_LOCATION = 1102

    val NOTIFICATION_CHANNEL_FOREGROUND = "1103"

    //Random key
    val LENGTH_RANDOM_CHARACTERS = 8

    val RANGE_RANDOM = 36

    //Location
    val LOCATION_INTERVAL = 1000L // 1000 = 1 second.

    val LOCATION_FASTEST_INTERVAL = 5000L

    val LOCATION_SMALLEST_DISPLACEMENT = 1f

    val LOCATION_PRIORITY = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY

    val GOOGLE_PLAY_SERVICES_VERSION = 1200000 //means version 12.0

    // Database
    val DATABASE_WHO_IS_CONNECTED = ".info/connected"

    val DATABASE_USER_ACCOUNT_SETTINGS = "user_account_settings"

    val DATABASE_USER_KEYS = "user_keys"

    val DATABASE_USER_NAME_FIELD = "user_name"

    val DATABASE_LOCATIONS = "Locations"

    val DATABASE_USER_POLYGONS = "user_polygons"

    val DATABASE_LAST_ONLINE = "last_online"

    val DATABASE_FOLLOWERS = "followers"

    val DATABASE_FOLLOWING = "following"

    val DATABASE_USER_FIELD = "user"

    val DATABASE_USER_LOGGED = "user_logged"

    val DATABASE_USER_ID_FIELD = "user_id"

    val DATABASE_DEVICE_TOKEN_FIELD = "device_token"

    val DATABASE_UNIQUE_KEY_FIELD = "unique_key"

    val DATABASE_TIME_FIELD = "time"


    enum class EPolygonAreaState(val idOfState : Int){
        STILL_OUTSIDE_OR_INSIDE(0), ENTER(1), EXIT(2)
    }

}