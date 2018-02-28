package kamilmilik.licencjat_gps_kid.Utils

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kamilmilik.licencjat_gps_kid.ListOnline
import kamilmilik.licencjat_gps_kid.R
import kamilmilik.licencjat_gps_kid.models.User

/**
 * Created by kamil on 19.02.2018.
 */
class FirebaseMethods{
    private val TAG : String = "FirebaseMethods"
    private var firebaseAuth: FirebaseAuth? = null
    private var firebaseDatabase : FirebaseDatabase? = null
    private var databaseReference : DatabaseReference? = null
    private var userId : String? = null
    private var context : Context? = null
    constructor(context : Context) {
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance()
        databaseReference = firebaseDatabase!!.getReference()
        if(firebaseAuth!!.currentUser != null){
            userId = firebaseAuth!!.currentUser!!.uid
        }

        this.context = context
    }

    /**
     * Register a new user to Firebase
     * @param email
     * @param password
     */
    fun registerNewUser(email : String, password : String){
        Log.i(TAG, "registerNewUser: register new user Authentication")
        val progressDialog = ProgressDialog.show(context, "Please wait...", "Processing...", true)
        firebaseAuth!!.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    progressDialog.dismiss()
                    if (task.isSuccessful) {//user successfull registrered and logged in
                        userId = firebaseAuth!!.currentUser!!.uid
                        Toast.makeText(context, "Registration successful", Toast.LENGTH_LONG).show()
                        (context as Activity)!!.finish()
                        val intent = Intent(context, ListOnline::class.java)
                        intent.setFlags((Intent.FLAG_ACTIVITY_NEW_TASK ) or Intent.FLAG_ACTIVITY_CLEAR_TASK ) //then we can't go back when press back button
                        context!!.startActivity(intent)
                    } else {
                        Log.e("ERROR", task.exception!!.toString())
                        Toast.makeText(context, task.exception!!.message, Toast.LENGTH_LONG).show()
                    }
                }
    }

    /**
     * add a new user from registration form to database to user_account_settings node
     * @param userId
     * @param email
     */
    fun addNewUserAccount(email : String){
        Log.i(TAG, "addNewUserAccount: add new user to database")
        var user : User = User(userId!!,email)
        databaseReference!!.child(context!!.getString(R.string.db_user_account_settings_node_name))
                .child(userId)
                .setValue(user)
    }
}