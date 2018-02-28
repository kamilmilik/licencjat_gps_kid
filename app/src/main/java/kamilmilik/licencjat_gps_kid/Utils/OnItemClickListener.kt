package kamilmilik.licencjat_gps_kid.Utils

import android.view.View

/**
 * Created by kamil on 23.02.2018.
 */
/**
 * interface callback to get clicked position in recyclerView
 */
interface OnItemClickListener {
    fun setOnItemClick(view: View, position: Int)

}