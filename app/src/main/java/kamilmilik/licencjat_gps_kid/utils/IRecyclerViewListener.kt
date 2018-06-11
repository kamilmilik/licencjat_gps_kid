package kamilmilik.licencjat_gps_kid.utils

import android.view.View

/**
 * Created by kamil on 23.02.2018.
 */
/**
 * interface callback to get clicked position in recyclerView
 */
interface IRecyclerViewListener {
    fun setOnItemClick(view: View, position: Int)
}