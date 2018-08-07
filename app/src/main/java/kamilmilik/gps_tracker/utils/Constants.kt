package kamilmilik.gps_tracker.utils

import com.google.android.gms.location.LocationRequest
import com.google.firebase.auth.FirebaseAuth
import java.io.File

/**
 * Created by kamil on 19.03.2018.
 */
object Constants{
    val SHARED_KEY = "shared"

    val MAP_CAMERA_ZOOM = 12.0f

    val SHARED_POLYGON_KEY = "map-polygon"
    //permission
    val LOCATION_PERMISSION_REQUEST_CODE: Int = 99

    val NOTIFICATION_ID_GET_LOCATION = 1102

    val NOTIFICATION_CHANNEL_FOREGROUND = "1103"

    val NOTIFICATION_CHANNEL_AREA = "1104"

    val NOTIFICATION_VIBRATION_PATTERN = longArrayOf(100, 400, 100, 400, 100)

    //Random key
    val LENGTH_RANDOM_CHARACTERS = 8

    val RANGE_RANDOM = 36

    //Location
    val LOCATION_INTERVAL = 1000L * 10 // 1000 * 10 = 10 seconds.

    val LOCATION_FASTEST_INTERVAL = 2000L // 2 seconds.

    val LOCATION_SMALLEST_DISPLACEMENT = 1f

    val LOCATION_PRIORITY = LocationRequest.PRIORITY_HIGH_ACCURACY

    val LAST_LOCATION_LATITUDE = "locationLat"

    val LAST_LOCATION_LONGITUDE = "locationLng"

    val LAST_LOCATION_ACCURACY = "locationAccuracy"

    val LAST_LOCATION_TIME = "locationTime"

    val LAST_LOCATION_PROVIDER = "locationProvider"

    val GOOGLE_PLAY_SERVICES_VERSION = 1200000 // Means version 12.0

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