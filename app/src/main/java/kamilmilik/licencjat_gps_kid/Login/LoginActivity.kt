package kamilmilik.licencjat_gps_kid.Login

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast

import android.content.Intent
import android.app.ProgressDialog
import android.text.TextUtils
import android.util.Log
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import kamilmilik.licencjat_gps_kid.ListOnline
import kamilmilik.licencjat_gps_kid.R
import kamilmilik.licencjat_gps_kid.Utils.CheckValidDataInEditText
import kotlinx.android.synthetic.main.activity_login.*


class LoginActivity : AppCompatActivity() {
    private val TAG : String = "LoginActivity"
    private var firebaseAuth: FirebaseAuth? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        firebaseAuth = FirebaseAuth.getInstance()
    }

    fun btnUserLogin_Click(v: View) {
        Log.i(TAG,"btnUserLogin_Click: sign In with currentUserId and password")
        var email = emailLoginEditText!!.text.toString()
        var password = passwordLoginEditText!!.text.toString()
        if(CheckValidDataInEditText(this).checkIfUserEnterValidData(email,password)){
            val progressDialog = ProgressDialog.show(this, "Please wait...", "Proccessing...", true)
            firebaseAuth!!.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        progressDialog.dismiss()
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Login successful", Toast.LENGTH_LONG).show()
                            val intent = Intent(this, ListOnline::class.java)
                            intent.putExtra("Email", firebaseAuth!!.currentUser!!.email)
                            intent.setFlags((Intent.FLAG_ACTIVITY_NEW_TASK ) or Intent.FLAG_ACTIVITY_CLEAR_TASK )
                            startActivity(intent)
                            finish()
                        } else {
                            Log.e("ERROR", task.exception!!.toString())
                            Toast.makeText(this, task.exception!!.message, Toast.LENGTH_LONG).show()
                        }
                    }
        }
    }


}
