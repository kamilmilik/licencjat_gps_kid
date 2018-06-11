package kamilmilik.licencjat_gps_kid.utils

/**
 * Created by kamil on 19.03.2018.
 */
object Constants{
    val SHARED_KEY = "shared"

    val SHARED_POLYGON_KEY = "map-polygon"
    //permission
    val MY_PERMISSION_REQUEST_CODE : Int = 99

    var TEST_MODE_FLAG : Boolean = true

    //polygon
    val STILL_OUTSIDE_OR_INSIDE = 0

    val ENTER = 1

    val EXIT = 2

    //notification
    var NOTIFICATION_CHANNEL_ID_ENTER = "2";

    var NOTIFICATION_CHANNEL_ID_EXIT = "3";

    var NOTIFICATION_ID_ENTER = 2;

    var NOTIFICATION_ID_EXIT = 3;

    var NOTIFICATION_ID_GET_LOCATION = 1102


    //firebase messanging
    val CHANNEL_ID : String = "IMPORTANCE_DEFAULT"

    val GOOGLE_PLAY_SERVICES_VERSION = 1200000 //means version 12.0

    val DATABASE_USER_ACCOUNT_SETTINGS = "user_account_settings"

    val DATABASE_USER_KEYS = "user_keys"

    val DATABASE_USER_NAME_FIELD = "user_name"

    val DATABASE_LOCATIONS = "Locations"

    val DATABASE_USER_POLYGONS = "user_polygons"

    val DATABASE_LAST_ONLINE = "last_online"

    val DATABASE_FOLLOWERS = "followers"

    val DATABASE_FOLLOWING = "following"

    val DATABASE_USER = "user"

    val DATABASE_USER_ID_FIELD = "user_id"

    val DATABASE_DEVICE_TOKEN_FIELD = "device_token"

    val DATABASE_UNIQUE_KEY_FIELD = "unique_key"

    val DATABASE_TIME_FIELD = "time"

}