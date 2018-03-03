package kamilmilik.licencjat_gps_kid.Utils

import android.content.Context
import android.widget.TextView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kamilmilik.licencjat_gps_kid.R
import kamilmilik.licencjat_gps_kid.models.User


/**
 * Created by kamil on 22.02.2018.
 */
class RecyclerViewAdapter(internal var context: Context, internal var dataList: ArrayList<String>) : RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>() {
    private var onItemClickListener: OnItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.user_layout, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val data = dataList[position]

        holder.textEmail.text = data

//        holder.itemView.setOnClickListener(View.OnClickListener {
//            var intent = Intent(context,MapTrackingActivity::class.java)
//            intent.putExtra("user_id", data.user_id)
//            context.startActivity(intent)
//        })


    }
    fun setClickListener(onItemClickListener: OnItemClickListener){
        this.onItemClickListener = onItemClickListener
    }
    override fun getItemCount(): Int {

        return dataList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),View.OnClickListener {

        override fun onClick(v: View?) {
            onItemClickListener!!.setOnItemClick(v!!, getAdapterPosition())
        }

        var textEmail: TextView

        init {

            textEmail = itemView.findViewById(R.id.emailText)
            itemView.setOnClickListener(this)

        }

    }
}