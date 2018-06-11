package kamilmilik.licencjat_gps_kid.utils

import android.content.Context
import android.widget.TextView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kamilmilik.licencjat_gps_kid.R
import kamilmilik.licencjat_gps_kid.models.UserMarkerInformationModel


/**
 * Created by kamil on 22.02.2018.
 */
class RecyclerViewAdapter(internal var context: Context, internal var dataList: ArrayList<UserMarkerInformationModel>) : RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>() {
    private var IRecyclerViewListener: IRecyclerViewListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.users_recycler_layout, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val data = dataList[position]

        holder.textEmail.text = data.userName

//        holder.itemView.setOnClickListener(View.OnClickListener {
//            var intent = Intent(context,MapTrackingActivity::class.java)
//            intent.putExtra("user_id", data.user_id)
//            context.startActivity(intent)
//        })


    }
    fun setClickListener(IRecyclerViewListener: IRecyclerViewListener){
        this.IRecyclerViewListener = IRecyclerViewListener
    }
    override fun getItemCount(): Int {

        return dataList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),View.OnClickListener {

        override fun onClick(v: View?) {
            IRecyclerViewListener!!.setOnItemClick(v!!, getAdapterPosition())
        }

        var textEmail: TextView

        init {

            textEmail = itemView.findViewById(R.id.emailText)
            itemView.setOnClickListener(this)

        }

    }
}