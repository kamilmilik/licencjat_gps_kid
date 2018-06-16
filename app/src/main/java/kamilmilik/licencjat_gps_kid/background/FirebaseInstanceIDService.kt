package kamilmilik.licencjat_gps_kid.background

import android.util.Log
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.FirebaseInstanceIdService
import kamilmilik.licencjat_gps_kid.map.MapActivity
import kamilmilik.licencjat_gps_kid.utils.Tools

/**
 * Created by kamil on 14.06.2018.
 */
/**
* Class that refreshes the Token ID when it changes.
 */
class FirebaseInstanceIDService : FirebaseInstanceIdService() {

    private val TAG = FirebaseInstanceIDService::class.java.simpleName

    // The onTokenRefreshcallback fires whenever a new token is generated.
    override fun onTokenRefresh() {
        Log.i(TAG,"onTokenRefresh()")
        Tools.addDeviceTokenToDatabase()
    }
}