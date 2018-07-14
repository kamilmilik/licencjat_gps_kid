package kamilmilik.gps_tracker.background

import com.google.firebase.iid.FirebaseInstanceIdService
import kamilmilik.gps_tracker.utils.Tools

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
        Tools.addDeviceTokenToDatabase()
    }
}