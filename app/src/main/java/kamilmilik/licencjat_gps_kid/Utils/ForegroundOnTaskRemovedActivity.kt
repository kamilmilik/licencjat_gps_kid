package kamilmilik.licencjat_gps_kid.Utils

import android.app.Activity
import android.os.Bundle
import android.util.Log


/**
 * Created by kamil on 28.03.2018.
 */
/**
 * this class is necessary to foreground service work correctly,
 * without this class if notification comes app crash since intent was null
 */
class ForegroundOnTaskRemovedActivity : Activity() {
    private val TAG = ForegroundOnTaskRemovedActivity::class.java.simpleName
    public override fun onCreate(bundle: Bundle?) {
        Log.i(TAG,"onCreate()")
        super.onCreate(bundle)
            Log.i(TAG, "finish affinity")
            finishAffinity()
    }
}