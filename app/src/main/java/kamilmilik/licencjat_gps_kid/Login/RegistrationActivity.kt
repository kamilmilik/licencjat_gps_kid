package kamilmilik.licencjat_gps_kid.Login

import android.support.v7.app.AppCompatActivity
import android.os.Bundle

import android.util.Log
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_registration.*
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DataSnapshot
import kamilmilik.licencjat_gps_kid.R
import kamilmilik.licencjat_gps_kid.Utils.CheckValidDataInEditText
import kamilmilik.licencjat_gps_kid.Helper.FirebaseRejestrationHelper


class RegistrationActivity : AppCompatActivity() {
    private val TAG : String = "RegistrationActivity"

    private var email : String? = null
    private var password : String? = null

    private var firebaseAuth: FirebaseAuth? = null
    private var firebaseMehods : FirebaseRejestrationHelper? = null
    private var firebaseListener : FirebaseAuth.AuthStateListener? = null

    private var firebaseDatabase : FirebaseDatabase? = null
    private var databaseReference : DatabaseReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        setupFirebase()
    }

    /**
     * Setup the firebase object
     */
    private fun setupFirebase(){
        Log.i(TAG,"setupFirebase: setup firebase")
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseMehods = FirebaseRejestrationHelper(this@RegistrationActivity)
        firebaseDatabase = FirebaseDatabase.getInstance()
        databaseReference = firebaseDatabase!!.getReference()
        firebaseListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                Log.i(TAG,"AuthStateListener: signed in: " + user.uid )

                databaseReference!!.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        // This method is called once with the initial value and again
                        // whenever data at this location is updated.

                        //add new user_account_settings to database
                        firebaseMehods!!.addNewUserAccount(email!!)

                        //generate unique key
                        //databaseReference.push().key
                    }
                    override fun onCancelled(error: DatabaseError) {
                        Log.i(TAG, "Failed to read value.", error.toException())
                    }
                })
            }else{
                Log.i(TAG,"AuthStateListener: signed out" )
            }
        }
    }

    fun btnRegistrationUser_Click(v: View) {
        email = emailRegistrationEditText!!.text.toString()
        password = passwordRegistrationEditText!!.text.toString()
        Log.i(TAG,"btnRegistrationUser_Click: create user with currentUserId and password")
        if(CheckValidDataInEditText(this).checkIfUserEnterValidData(email!!,password!!)){
            firebaseMehods!!.registerNewUser(email!!, password!!)
        }
// val progressDialog = ProgressDialog.show(this, "Please wait...", "Processing...", true)
//        firebaseAuth!!.createUserWithEmailAndPassword(emailRegistrationEditText!!.text.toString(), passwordRegistrationEditText!!.text.toString())
//                .addOnCompleteListener { task ->
//                    progressDialog.dismiss()
//                    if (task.isSuccessful) {//user successfull registrered and logged in
//                        Toast.makeText(this, "Registration successful", Toast.LENGTH_LONG).show()
//                        finish()
//                        val intent = Intent(this, ListOnline::class.java)
//                        startActivity(intent)
//                    } else {
//                        Log.e("ERROR", task.exception!!.toString())
//                        Toast.makeText(this, task.exception!!.message, Toast.LENGTH_LONG).show()
//                    }
//                }
    }

    public override fun onStart() {
        super.onStart()
        firebaseAuth!!.addAuthStateListener(firebaseListener!!)
    }

    public override fun onStop() {
        super.onStop()
        if (firebaseListener != null) {
            firebaseAuth!!.removeAuthStateListener(firebaseListener!!)
        }
    }
}
