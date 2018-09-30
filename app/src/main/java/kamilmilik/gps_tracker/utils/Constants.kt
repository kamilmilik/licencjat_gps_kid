package kamilmilik.gps_tracker.utils

import com.google.android.gms.location.LocationRequest

/**
 * Created by kamil on 19.03.2018.
 */
object Constants {
    val SHARED_KEY = "shared"

    val MAP_CAMERA_ZOOM = 12.0f

    val SHARED_POLYGON_KEY = "map-polygon"
    // Permission
    val LOCATION_PERMISSION_REQUEST_CODE: Int = 99

    val NOTIFICATION_ID_GET_LOCATION = 1102

    val NOTIFICATION_CHANNEL_FOREGROUND = "1103"

    val NOTIFICATION_CHANNEL_AREA = "1104"

    val NOTIFICATION_VIBRATION_PATTERN = longArrayOf(100, 400, 100, 400, 100)

    // Random key
    val LENGTH_RANDOM_CHARACTERS = 8

    val RANGE_RANDOM = 36

    // Location
    val LOCATION_ONE_SECOND_DELAY = 1000L

    val LOCATION_NO_DELAY = 0L

    val LOCATION_INTERVAL_SLOW = LOCATION_ONE_SECOND_DELAY * 60 // 60 seconds = 1 minute

    val LOCATION_FASTEST_INTERVAL_SLOW = LOCATION_ONE_SECOND_DELAY * 30 // 30 seconds

    val LOCATION_SMALLEST_DISPLACEMENT = 10f // 10 meters

    val LOCATION_INTERVAL_FAST = LOCATION_ONE_SECOND_DELAY * 30 * 1  // 30 seconds

    val LOCATION_FASTEST_INTERVAL_FAST = LOCATION_ONE_SECOND_DELAY * 20 * 1 // 20 seconds

    val LOCATION_PRIORITY = LocationRequest.PRIORITY_HIGH_ACCURACY

    val LOCATION_MIN_DISTANCE_CHANGE_FOR_UPDATES: Float = 10f // 10 meters // The minimum distance to change updates.

    val LOCATION_MIN_TIME_BW_UPDATES = (1000 * 60 * 1).toLong() // 1 minute // The minimum time between updates.

    val LAST_LOCATION_LATITUDE = "locationLat"

    val LAST_LOCATION_LONGITUDE = "locationLng"

    val LAST_LOCATION_ACCURACY = "locationAccuracy"

    val LAST_LOCATION_TIME = "locationTime"

    val LAST_LOCATION_PROVIDER = "locationProvider"

    val IS_FIRST_RUN = "isFirstRun"

    val CHANGE_NAME_ACTIVITY_RESULT = "changeNameActivityResultRequest"

    val CHANGE_NAME_ACTIVITY_RESULT_REQUEST = 1

    val GOOGLE_PLAY_SERVICES_VERSION = 1200000 // Means version 12.0

    val GOOGLE_MAP_TYPE = "googleMapType"
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

    val DATABASE_DATA_FIELD = "data"

    val DATABASE_PERMISSIONS_FIELD = "permissions"

    val DATABASE_USER_LOGGED = "user_logged"

    val DATABASE_USER_ID_FIELD = "user_id"

    val DATABASE_DEVICE_TOKEN_FIELD = "device_token"

    val DATABASE_UNIQUE_KEY_FIELD = "unique_key"

    val DATABASE_TIME_FIELD = "time"

    val WORK_ON_UI_ITEMS_LIMIT = 16


    enum class PolygonAreaState(val idOfState: Int) {
        STILL_OUTSIDE_OR_INSIDE(0), ENTER(1), EXIT(2)
    }

}