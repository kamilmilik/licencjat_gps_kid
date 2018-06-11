package kamilmilik.licencjat_gps_kid.login

import android.os.Bundle
import kamilmilik.licencjat_gps_kid.R
import kotlinx.android.synthetic.main.activity_change_password.*
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kamilmilik.licencjat_gps_kid.ApplicationActivity
import kamilmilik.licencjat_gps_kid.utils.Tools


class ResetPasswordActivity : ApplicationActivity() {

    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)
        Tools.setupToolbar(this, true).title = getString(R.string.resetPasswordActivityName)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)
        sendNewPasswordToEmail()
    }

    private fun sendNewPasswordToEmail() {
        sendChangePasswordButton.setOnClickListener({
            var emailAddress = changeUserPasswordEditText.text.toString()
            if (!emailAddress.isEmpty()) {
                FirebaseAuth.getInstance().useAppLanguage()
                FirebaseAuth.getInstance().sendPasswordResetEmail(emailAddress)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(this@ResetPasswordActivity, getString(R.string.changePasswordSent) + emailAddress, Toast.LENGTH_LONG).show()
                                finish()
                            } else {
                                Toast.makeText(this@ResetPasswordActivity, getString(R.string.validEmail), Toast.LENGTH_LONG).show()
                            }
                        }
            } else {
                Toast.makeText(this@ResetPasswordActivity, getString(R.string.emptyFieldInformation), Toast.LENGTH_LONG).show()
            }
        })
    }

}
