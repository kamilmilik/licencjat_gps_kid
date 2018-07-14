package kamilmilik.gps_tracker.login

import android.app.Activity
import android.app.ProgressDialog
import android.os.Bundle

import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_registration.*
import kamilmilik.gps_tracker.ApplicationActivity
import kamilmilik.gps_tracker.R
import kamilmilik.gps_tracker.utils.Tools


class RegistrationActivity : ApplicationActivity() {
    private val TAG = RegistrationActivity::class.java.simpleName

    private var email: String? = null

    private var password: String? = null

    private var name: String? = null

    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)
        Tools.setupToolbar(this, true).title = getString(R.string.registrationActivityName)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        registrationButtonAction()
    }


    private fun registrationButtonAction() {
        registrationButton.setOnClickListener({
            email = emailLoginEditText!!.text.toString()
            password = passwordLoginEditText!!.text.toString()
            name = nameEditText!!.text.toString()
            if (Tools.checkIfUserEnterValidData(this, email!!, password!!, name!!)) {
                registerNewUser(email!!, password!!, name!!, this)
            }
        })
    }

    private fun registerNewUser(email: String, password: String, name: String, activity: Activity) {
        val progressDialog = ProgressDialog.show(this, getString(R.string.waitInformation), getString(R.string.waitMessage), true)
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    progressDialog.dismiss()
                    if (task.isSuccessful) {//user successfull registrered and logged in
                        sendEmailVerification(activity, name)
                    } else {
                        Toast.makeText(this, task.exception!!.message, Toast.LENGTH_LONG).show()
                    }
                }
    }


    private fun sendEmailVerification(activity: Activity, name: String) {
        val user = FirebaseAuth.getInstance().currentUser
        FirebaseAuth.getInstance().useAppLanguage()
        user!!.sendEmailVerification()
                .addOnCompleteListener(activity, OnCompleteListener<Void> { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, getString(R.string.verificationEmailSentInformation) + user.email!!, Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, getString(R.string.failedVerificationEmailSend), Toast.LENGTH_SHORT).show()
                    }
                    Tools.updateProfileName(this, user, name, OnCompleteListener { activity.finish() })
                })
    }
}
