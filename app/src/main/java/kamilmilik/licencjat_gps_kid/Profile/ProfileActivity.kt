package kamilmilik.licencjat_gps_kid.Profile

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import kamilmilik.licencjat_gps_kid.R


class ProfileActivity : AppCompatActivity() {

    private var tvEmail: TextView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        tvEmail = findViewById<View>(R.id.tvEmailProfile) as TextView?
        tvEmail!!.text = intent.extras!!.getString("Email")
    }

}
