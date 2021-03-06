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
import kamilmilik.gps_tracker.utils.FirebaseAuthExceptions
import kamilmilik.gps_tracker.utils.ObjectsUtils
import kamilmilik.gps_tracker.utils.Tools
import kamilmilik.gps_tracker.utils.ValidDataUtils


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
        registrationButton.setOnClickListener {
            email = emailLoginEditText?.text.toString().replace("\\s".toRegex(), "")
            password = passwordLoginEditText?.text.toString()
            name = nameEditText?.text.toString()
            ObjectsUtils.safeLet(email, password, name) { userEmail, userPassword, userName ->
                if (ValidDataUtils.checkIfUserEnterValidData(this, userEmail, userPassword, userName)) {
                    registerNewUser(userEmail, userPassword, userName, this)
                }
            }
        }
    }

    private fun registerNewUser(email: String, password: String, name: String, activity: Activity) {
        val progressDialog = ProgressDialog.show(this, getString(R.string.waitInformation), getString(R.string.waitMessage), true)
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    progressDialog.dismiss()
                    if (task.isSuccessful) {//user successfully registered and logged in
                        sendEmailVerification(activity, name)
                    } else {
                        FirebaseAuthExceptions.translate(this, task)
                    }
                }
    }


    private fun sendEmailVerification(activity: Activity, name: String) {
        FirebaseAuth.getInstance().useAppLanguage()
        FirebaseAuth.getInstance().currentUser?.let { user ->
            user.sendEmailVerification()
                    .addOnCompleteListener(activity) { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, getString(R.string.verificationEmailSentInformation) + user.email, Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, getString(R.string.failedVerificationEmailSend), Toast.LENGTH_SHORT).show()
                        }
                        Tools.updateProfileName(this, user, name, OnCompleteListener { activity.finish() })
                    }
        }
    }
}
