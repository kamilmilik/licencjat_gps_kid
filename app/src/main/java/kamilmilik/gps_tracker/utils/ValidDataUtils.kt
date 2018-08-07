package kamilmilik.gps_tracker.utils

import android.content.Context
import android.text.TextUtils
import android.util.Log
import android.widget.Toast

/**
 * Created by kamil on 07.08.2018.
 */
object ValidDataUtils {
    private val TAG = ValidDataUtils::class.java.simpleName

    fun checkIfUserEnterValidData(context: Context?, email: String, password: String, name: String): Boolean {
        Log.i(TAG, "checkIfUserEnterValidData: check if is valid data in user login/register")
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(context, context!!.getString(kamilmilik.gps_tracker.R.string.enterEmail), Toast.LENGTH_SHORT).show()
            return false
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(context, context!!.getString(kamilmilik.gps_tracker.R.string.enterPassword), Toast.LENGTH_SHORT).show()
            return false
        }
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(context, context!!.getString(kamilmilik.gps_tracker.R.string.enterName), Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    fun checkIfUserEnterValidData(context: Context?, email: String, password: String): Boolean {
        Log.i(TAG, "checkIfUserEnterValidData: check if is valid data in user login/register")
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(context, context!!.getString(kamilmilik.gps_tracker.R.string.enterEmail), Toast.LENGTH_SHORT).show()
            return false
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(context, context!!.getString(kamilmilik.gps_tracker.R.string.enterPassword), Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }
}