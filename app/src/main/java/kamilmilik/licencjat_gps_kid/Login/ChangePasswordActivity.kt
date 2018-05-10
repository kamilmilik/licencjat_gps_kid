package kamilmilik.licencjat_gps_kid.Login

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import kamilmilik.licencjat_gps_kid.R
import kotlinx.android.synthetic.main.activity_change_password.*
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import kamilmilik.licencjat_gps_kid.ApplicationActivity


class ChangePasswordActivity : ApplicationActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)

//        setupToolbar()

        sendChangePasswordButton.setOnClickListener(View.OnClickListener {
            var emailAddress = changeUserPasswordEditText.text.toString()
            if (!emailAddress.isEmpty()) {
                FirebaseAuth.getInstance().useAppLanguage()
                FirebaseAuth.getInstance().sendPasswordResetEmail(emailAddress)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(this@ChangePasswordActivity, "Change password link sent to email " + emailAddress, Toast.LENGTH_LONG).show()
                                finish()
                            } else {
                                Toast.makeText(this@ChangePasswordActivity, "Write valid email", Toast.LENGTH_LONG).show()
                            }
                        }
            }else{
                Toast.makeText(this@ChangePasswordActivity, "Field cannot be empty", Toast.LENGTH_LONG).show()
            }
            })
    }

//    private fun setupToolbar() {
//        toolbarResetPassword.title = "Reset password"
//        setSupportActionBar(toolbarResetPassword)
//        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
//        supportActionBar!!.setDisplayShowHomeEnabled(true)
//    }

}
