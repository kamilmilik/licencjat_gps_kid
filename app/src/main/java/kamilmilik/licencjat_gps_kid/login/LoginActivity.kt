package kamilmilik.licencjat_gps_kid.login

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.iid.FirebaseInstanceId
import kamilmilik.licencjat_gps_kid.ApplicationActivity
import kamilmilik.licencjat_gps_kid.utils.Constants
import kotlinx.android.synthetic.main.activity_login.*
import kamilmilik.licencjat_gps_kid.map.MapActivity
import kamilmilik.licencjat_gps_kid.R
import kamilmilik.licencjat_gps_kid.utils.Tools
import kamilmilik.licencjat_gps_kid.models.User


class LoginActivity : ApplicationActivity() {
    private val TAG = LoginActivity::class.java.simpleName

    private var firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)
        Tools.setupToolbar(this, false).title = getString(R.string.loginActivityName)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        startMapActivityIfUserLogged()

        loginButtonAction()
        registrationButtonAction()
        resetPasswordButtonAction()
    }

    private fun startMapActivityIfUserLogged() {
        if (Constants.TEST_MODE_FLAG) {//this is use only for test app, normally use code in else block
            if (firebaseAuth.currentUser != null) {
                Tools.startNewActivityWithoutPrevious(this, MapActivity::class.java)
            }
        } else {
            if (firebaseAuth.currentUser != null && firebaseAuth.currentUser!!.isEmailVerified) {
                Tools.startNewActivityWithoutPrevious(this, MapActivity::class.java)
            }
        }
    }

    private fun loginButtonAction() {
        loginButton.setOnClickListener({
            var email = emailLoginEditText!!.text.toString()
            var password = passwordLoginEditText!!.text.toString()
            if (Tools.checkIfUserEnterValidData(this, email, password)) {
                val progressDialog = ProgressDialog.show(this, getString(R.string.waitInformation), getString(R.string.waitMessage), true)
                firebaseAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            progressDialog.dismiss()
                            if (task.isSuccessful) {
                                if (!Constants.TEST_MODE_FLAG) {
                                    if (firebaseAuth.currentUser!!.isEmailVerified) {
                                        Toast.makeText(this, getString(R.string.loginSuccess), Toast.LENGTH_LONG).show()
                                        addDeviceTokenToDatabaseAndStartNewActivity()
                                        addNewUserAccountToDatabase(email, firebaseAuth.currentUser!!.displayName!!)
                                    } else {
                                        Toast.makeText(this, getString(R.string.emailNotVerified), Toast.LENGTH_LONG).show()
                                        firebaseAuth.signOut()
                                    }
                                } else {
                                    Toast.makeText(this, getString(R.string.loginSuccess), Toast.LENGTH_LONG).show()
                                    addDeviceTokenToDatabaseAndStartNewActivity()
                                    addNewUserAccountToDatabase(email, firebaseAuth.currentUser!!.displayName!!)
                                }
                            } else {
                                Toast.makeText(this, task.exception!!.message, Toast.LENGTH_LONG).show()
                            }
                        }
            }
        })
    }

    private fun registrationButtonAction() {
        registrationButton.setOnClickListener({
            val intent = Intent(this@LoginActivity, RegistrationActivity::class.java)
            startActivity(intent)
        })
    }

    private fun resetPasswordButtonAction() {
        resetPasswordButton.setOnClickListener({
            startActivity(Intent(this, ResetPasswordActivity::class.java))
        })
    }

    private fun addDeviceTokenToDatabaseAndStartNewActivity() {
        var userDatabase = FirebaseDatabase.getInstance().reference.child(Constants.DATABASE_USER_ACCOUNT_SETTINGS)
        var currentUserId = firebaseAuth!!.currentUser!!.uid
        var deviceTokenId = FirebaseInstanceId.getInstance().token
        userDatabase!!.child(currentUserId).child(Constants.DATABASE_DEVICE_TOKEN_FIELD).setValue(deviceTokenId).addOnSuccessListener {
            Tools.startNewActivityWithoutPrevious(this, MapActivity::class.java)
        }
    }

    fun addNewUserAccountToDatabase(email: String, name: String) {
        var deviceTokenId = FirebaseInstanceId.getInstance().token
        var user = User(firebaseAuth!!.currentUser!!.uid, email, deviceTokenId!!, name)

        FirebaseDatabase.getInstance().reference!!.child(Constants.DATABASE_USER_ACCOUNT_SETTINGS)
                .child(firebaseAuth!!.currentUser!!.uid)
                .setValue(user)
    }

    override fun onBackPressed() {
        finishAffinity()
    }
}
