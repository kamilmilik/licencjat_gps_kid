package kamilmilik.licencjat_gps_kid.Utils

import android.content.Context
import android.text.TextUtils
import android.util.Log
import android.widget.Toast

/**
 * Created by kamil on 19.02.2018.
 */
class CheckValidDataInEditText(context : Context?){
    private var context : Context? = context
    val TAG : String = "CheckValidDatat"
    /**
     * Check that user enter valid data to edit text
     * @param email
     * @param password
     */
    fun checkIfUserEnterValidData(email : String, password: String): Boolean {
        Log.i(TAG,"checkIfUserEnterValidData: check if is valid data in user login/register")
        if(TextUtils.isEmpty(email)){
            Toast.makeText(context,"Please enter currentUserId", Toast.LENGTH_SHORT).show()
            return false
        }
        if(TextUtils.isEmpty(password)){
            Toast.makeText(context,"Please enter password", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }
}