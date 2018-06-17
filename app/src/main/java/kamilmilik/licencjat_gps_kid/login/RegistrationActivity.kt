package kamilmilik.licencjat_gps_kid.login

import android.app.Activity
import android.app.ProgressDialog
import android.os.Bundle

import android.util.Log
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_registration.*
import com.google.firebase.database.ValueEventListener
import kamilmilik.licencjat_gps_kid.ApplicationActivity
import kamilmilik.licencjat_gps_kid.R
import kamilmilik.licencjat_gps_kid.utils.Tools


class RegistrationActivity : ApplicationActivity() {
    private val TAG = RegistrationActivity::class.java.simpleName

    private var email: String? = null

    private var password: String? = null

    private var name: String? = null

    private var firebaseAuth: FirebaseAuth? = null

    private var firebaseListener: FirebaseAuth.AuthStateListener? = null

    private var databaseListener: ValueEventListener? = null

    private var firebaseDatabase: FirebaseDatabase? = null

    private var databaseReference: DatabaseReference? = null

    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)
        Tools.setupToolbar(this, true).title = getString(R.string.registrationActivityName)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance()
        databaseReference = firebaseDatabase!!.reference

        registrationButtonAction()
    }


    fun registrationButtonAction() {
        registrationButton.setOnClickListener({
            email = emailLoginEditText!!.text.toString()
            password = passwordLoginEditText!!.text.toString()
            name = nameEditText!!.text.toString()
            if (Tools.checkIfUserEnterValidData(this, email!!, password!!, name!!)) {
                registerNewUser(email!!, password!!, name!!, this)
            }
        })
    }

    public override fun onStop() {
        super.onStop()
        if (firebaseListener != null && databaseReference != null && databaseListener != null) {
            databaseReference!!.removeEventListener(databaseListener)
        }
    }

    fun registerNewUser(email: String, password: String, name: String, activity: Activity) {
        val progressDialog = ProgressDialog.show(this, getString(R.string.waitInformation), getString(R.string.waitMessage), true)
        firebaseAuth!!.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    progressDialog.dismiss()
                    if (task.isSuccessful) {//user successfull registrered and logged in
                        //TODO do emulatora,sendemail zakomentowane , normalnie odkomentowac i wywalic updateprofile name co jest pod spodem tej funkcji
//                        sendEmailVerification(activity, name)

                        //TODO to wyrzucic w finalnej wersji bo to jest w sendEmailVerification wysylaniem emailu weryf poniewaz w tej metodzie jest nadawanie nazwy na serwerze
                        val user = FirebaseAuth.getInstance().currentUser
                        Tools.updateProfileName(this, user!!, name, OnCompleteListener { activity.finish() })
                        //TODO czasami po rejestracji chowa nam sie okno aplikacji(zwykle jak probujemy sie rejestrowac 2 raz tzn np rejestrujemy sie , usuwamy konto i znowi sie rejestrujemy), a jak je wznowimy z task manager to jestesmy automaczynie zalogowani(wersja bez weryfikacji email)

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
                        Toast.makeText(this,
                                getString(R.string.verificationEmailSentInformation) + user.email!!, Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, getString(R.string.failedVerificationEmailSend), Toast.LENGTH_SHORT).show()
                    }
                    Tools.updateProfileName(this, user, name, OnCompleteListener { activity.finish() })
                })
    }
}
