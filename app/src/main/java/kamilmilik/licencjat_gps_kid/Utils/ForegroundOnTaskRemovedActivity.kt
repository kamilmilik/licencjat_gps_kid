package kamilmilik.licencjat_gps_kid.Utils

import android.app.Activity
import android.os.Bundle



/**
 * Created by kamil on 28.03.2018.
 */
/**
 * this class is necessary to foreground service work correctly,
 * without this class if notification comes app crash since intent was null
 */
class ForegroundOnTaskRemovedActivity : Activity() {
    public override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)
        finish()
    }
}