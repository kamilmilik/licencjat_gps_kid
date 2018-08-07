package kamilmilik.gps_tracker.login

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.*
import com.google.firebase.database.*
import com.google.firebase.iid.FirebaseInstanceId
import kamilmilik.gps_tracker.ApplicationActivity
import kamilmilik.gps_tracker.utils.Constants
import kotlinx.android.synthetic.main.activity_login.*
import kamilmilik.gps_tracker.map.MapActivity
import kamilmilik.gps_tracker.R
import kamilmilik.gps_tracker.utils.Tools
import kamilmilik.gps_tracker.models.User
import kamilmilik.gps_tracker.utils.FirebaseAuthExceptions
import kamilmilik.gps_tracker.utils.ValidDataUtils


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
            if (firebaseAuth.currentUser != null && firebaseAuth.currentUser!!.isEmailVerified) {
                Tools.startNewActivityWithoutPrevious(this, MapActivity::class.java)
            }
    }

    private fun loginButtonAction() {
        loginButton.setOnClickListener({
            val email = emailLoginEditText!!.text.toString().replace("\\s".toRegex(), "")
            val password = passwordLoginEditText!!.text.toString()
            if (ValidDataUtils.checkIfUserEnterValidData(this, email, password)) {
                val progressDialog = ProgressDialog.show(this, getString(R.string.waitInformation), getString(R.string.waitMessage), true)
                firebaseAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            progressDialog.dismiss()
                            if (task.isSuccessful) {
//                                checkIfUserLoggedInOtherDeviceAndIfNotLogIn(email)
                                //TODO jak wywale checkIfUserLoggedInOtherDeviceAndIfNotLogIn to to nizej caly ten if zostawic, jak nie wywalam to calego ifa z elsem usunac i odkomentowac jedynie checkIfUserLoggedInOtherDeviceAndIfNotLogIn i odkomentowac z DatabaseOnlineUserAction removeLoggedUser()z funkcji logoutUser

                                if (firebaseAuth.currentUser!!.isEmailVerified) {
                                    Toast.makeText(this@LoginActivity, getString(R.string.loginSuccess), Toast.LENGTH_LONG).show()
                                    Tools.addDeviceTokenToDatabaseAndStartNewActivity(this@LoginActivity, MapActivity::class.java)
                                    addNewUserAccountToDatabase(email, firebaseAuth.currentUser!!.displayName!!)
                                } else {
                                    Toast.makeText(this@LoginActivity, getString(R.string.emailNotVerified), Toast.LENGTH_LONG).show()
                                    firebaseAuth.signOut()
                                }
                            } else {
                                FirebaseAuthExceptions.translate(this, task)
                            }
                        }
            }
        })
    }

    fun addInformationAboutLoggedUser(){
        FirebaseDatabase.getInstance().reference!!.child(Constants.DATABASE_USER_LOGGED)
                .child(Constants.DATABASE_USER_FIELD)
                .child(firebaseAuth.currentUser!!.uid)
                .setValue(firebaseAuth.currentUser!!.uid)
    }

    private fun checkIfUserLoggedInOtherDeviceAndIfNotLogIn(email: String){
        FirebaseDatabase.getInstance().reference!!
                .child(Constants.DATABASE_USER_LOGGED)
                .child(Constants.DATABASE_USER_FIELD)
                .orderByKey()
                .equalTo(firebaseAuth.currentUser!!.uid)
                .addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onCancelled(p0: DatabaseError?) {}

                    override fun onDataChange(dataSnapshot: DataSnapshot?) {
                        for (singleSnapshot in dataSnapshot!!.children) {
                            firebaseAuth.signOut()
                            Toast.makeText(this@LoginActivity, getString(R.string.loginInOtherDevice), Toast.LENGTH_LONG).show()
                        }
                        if(!dataSnapshot.exists()){
                            Log.i(TAG,"onDataChange() user not logged in yea")
                                if (firebaseAuth.currentUser!!.isEmailVerified) {
                                    addInformationAboutLoggedUser()
                                    Toast.makeText(this@LoginActivity, getString(R.string.loginSuccess), Toast.LENGTH_LONG).show()
                                    Tools.addDeviceTokenToDatabaseAndStartNewActivity(this@LoginActivity, MapActivity::class.java)
                                    addNewUserAccountToDatabase(email, firebaseAuth.currentUser!!.displayName!!)
                                } else {
                                    Toast.makeText(this@LoginActivity, getString(R.string.emailNotVerified), Toast.LENGTH_LONG).show()
                                    firebaseAuth.signOut()
                                }
                        }
                    }

                })
    }

    private fun registrationButtonAction() {
        registrationButton.setOnClickListener({
            startActivity(Intent(this, RegistrationActivity::class.java))
        })
    }

    private fun resetPasswordButtonAction() {
        resetPasswordText.setOnClickListener({
            startActivity(Intent(this, ResetPasswordActivity::class.java))
        })
    }

    fun addNewUserAccountToDatabase(email: String, name: String) {
        val deviceTokenId = FirebaseInstanceId.getInstance().token
        val user = User(firebaseAuth.currentUser!!.uid, email, deviceTokenId!!, name)

        FirebaseDatabase.getInstance().reference!!.child(Constants.DATABASE_USER_ACCOUNT_SETTINGS)
                .child(firebaseAuth.currentUser!!.uid)
                .setValue(user)
    }

    override fun onBackPressed() {
        finishAffinity()
    }
}
