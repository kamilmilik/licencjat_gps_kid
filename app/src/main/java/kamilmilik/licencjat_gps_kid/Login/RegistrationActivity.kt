package kamilmilik.licencjat_gps_kid.Login

import android.support.v7.app.AppCompatActivity
import android.os.Bundle

import android.util.Log
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_registration.*
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DataSnapshot
import kamilmilik.licencjat_gps_kid.ApplicationActivity
import kamilmilik.licencjat_gps_kid.R
import kamilmilik.licencjat_gps_kid.Utils.CheckValidDataInEditText
import java.util.*


class RegistrationActivity : ApplicationActivity() {
    private val TAG : String = "RegistrationActivity"

    private var email : String? = null
    private var password : String? = null
    private var name : String? = null

    private var firebaseAuth: FirebaseAuth? = null
    private var firebaseMehods : FirebaseRejestrationHelper? = null
    private var firebaseListener : FirebaseAuth.AuthStateListener? = null
    private var databaseListener : ValueEventListener? = null

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
        databaseReference = firebaseDatabase!!.reference
        firebaseListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                Log.i(TAG,"AuthStateListener: signed in: " + user.uid )

                databaseListener = databaseReference!!.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        // This method is called once with the initial value and again
                        // whenever data at this location is updated.

                        //add new user_account_settings to database
                        if(email != null && name != null){
                            firebaseMehods!!.addNewUserAccount(email!!, name!!)
                            FirebaseAuth.getInstance().removeAuthStateListener(firebaseListener!!)
                        }
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
        email = emailLoginEditText!!.text.toString()
        password = passwordLoginEditText!!.text.toString()
        name = nameEditText!!.text.toString()
        Log.i(TAG,"btnRegistrationUser_Click: create user with currentUserId and password")
        if(CheckValidDataInEditText(this).checkIfUserEnterValidData(email!!,password!!, name!!)){
            firebaseMehods!!.registerNewUser(email!!, password!!, name!!, this)
        }
    }

    public override fun onStart() {
        super.onStart()
        firebaseAuth!!.addAuthStateListener(firebaseListener!!)
    }

    public override fun onStop() {
        super.onStop()
        if (firebaseListener != null && databaseReference != null && databaseListener != null) {
            firebaseAuth!!.removeAuthStateListener(firebaseListener!!)
            databaseReference!!.removeEventListener(databaseListener)
        }
    }
}
