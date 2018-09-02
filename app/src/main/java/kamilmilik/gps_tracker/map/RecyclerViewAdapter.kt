package kamilmilik.gps_tracker.map

import android.content.Context
import android.widget.TextView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kamilmilik.gps_tracker.R
import kamilmilik.gps_tracker.models.UserBasicInfo
import kamilmilik.gps_tracker.utils.listeners.IRecyclerViewListener


/**
 * Created by kamil on 22.02.2018.
 */
class RecyclerViewAdapter(internal var context: Context, private var dataList: ArrayList<UserBasicInfo>) : RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>() {

    private val TAG = RecyclerViewAdapter::class.java.simpleName

    private var recyclerViewListener: IRecyclerViewListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.users_recycler_layout, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val data = dataList[position]

        holder.textEmail.text = data.userName

    }

    fun setClickListener(IRecyclerViewListener: IRecyclerViewListener) {
        this.recyclerViewListener = IRecyclerViewListener
    }

    override fun getItemCount(): Int {

        return dataList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnLongClickListener, View.OnClickListener {

        override fun onLongClick(v: View?): Boolean {
            recyclerViewListener?.apply {
                v?.let {
                    return setOnLongItemClick(v, adapterPosition)
                }
            }
            return false
        }

        override fun onClick(v: View?) {
            v?.let {
                recyclerViewListener?.setOnItemClick(v, adapterPosition)
            }
        }


        var textEmail: TextView = itemView.findViewById(R.id.emailText)

        init {
            itemView.setOnClickListener(this)
            itemView.setOnLongClickListener(this)
        }

    }
}