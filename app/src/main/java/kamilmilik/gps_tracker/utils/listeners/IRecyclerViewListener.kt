package kamilmilik.gps_tracker.utils.listeners

import android.view.View

/**
 * Created by kamil on 23.02.2018.
 */
/**
 * interface callback to get clicked position in recyclerView
 */
interface IRecyclerViewListener {

    fun setOnItemClick(view: View, position: Int)

    fun setOnLongItemClick(view: View, position: Int): Boolean
}