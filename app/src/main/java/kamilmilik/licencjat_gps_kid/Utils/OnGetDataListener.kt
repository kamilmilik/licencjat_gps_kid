package kamilmilik.licencjat_gps_kid.Utils

import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DataSnapshot



/**
 * Created by kamil on 02.03.2018.
 */
interface OnGetDataListener{
    fun onStart()
    fun onSuccess(data: DataSnapshot)
    fun onFailed(databaseError: DatabaseError)
}