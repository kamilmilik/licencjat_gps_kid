package kamilmilik.licencjat_gps_kid.Login

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.iid.FirebaseInstanceId
import kamilmilik.licencjat_gps_kid.ListOnline
import kamilmilik.licencjat_gps_kid.R
import kamilmilik.licencjat_gps_kid.models.User
import android.support.annotation.NonNull
import com.google.firebase.auth.FirebaseUser
import kamilmilik.licencjat_gps_kid.Utils.Tools


/**
 * Created by kamil on 19.02.2018.
 */
class FirebaseRejestrationHelper {
    @SuppressLint("LongLogTag")
    private val TAG : String = FirebaseRejestrationHelper::class.java.simpleName

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
    fun registerNewUser(email : String, password : String, name: String, activity: Activity){
        Log.i(TAG, "registerNewUser: register new user Authentication")
        val progressDialog = ProgressDialog.show(context, "Please wait...", "Processing...", true)
        firebaseAuth!!.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    progressDialog.dismiss()
                    if (task.isSuccessful) {//user successfull registrered and logged in
                        userId = firebaseAuth!!.currentUser!!.uid

                        //TODO do emulatora,sendemail zakomentowane , normalnie odkomentowac i wywalic updateprofile name co jest pod spodem tej funkcji
//                        sendEmailVerification(activity, name)

                        val user = FirebaseAuth.getInstance().currentUser
                        val profileUpdates = UserProfileChangeRequest.Builder()
                                .setDisplayName(name)
                                .build()
                        user!!.updateProfile(profileUpdates)
                                .addOnCompleteListener(object : OnCompleteListener<Void> {
                                    override fun onComplete(p0: Task<Void>) {
                                        Log.i(TAG, "onComplete() name " + name)
//                                            val intent = Intent(context, ListOnline::class.java)
//                                            intent.setFlags((Intent.FLAG_ACTIVITY_NEW_TASK) or Intent.FLAG_ACTIVITY_CLEAR_TASK) //then we can't go back when press back button
//                                            context!!.startActivity(intent)
                                        activity.finish()
                                    }
                                })

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
    fun addNewUserAccount(email : String, name: String){
        Log.i(TAG, "addNewUserAccount: add new user to database")
        var deviceTokenId = FirebaseInstanceId.getInstance().token
        var user = User(userId!!,email,deviceTokenId!!, name)

        databaseReference!!.child(context!!.getString(R.string.db_user_account_settings_node_name))
                .child(userId)
                .setValue(user)
    }

    private fun sendEmailVerification(activity: Activity, name : String) {
        val user = FirebaseAuth.getInstance().currentUser
        FirebaseAuth.getInstance().useAppLanguage()
        user!!.sendEmailVerification()
                .addOnCompleteListener(activity, OnCompleteListener<Void> { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(context,
                                "Verification email sent to " + user.email!!,
                                Toast.LENGTH_SHORT).show()
                    } else {
                        Log.e(TAG, "sendEmailVerification", task.exception)
                        Toast.makeText(context,
                                "Failed to send verification email.",
                                Toast.LENGTH_SHORT).show()
                    }
                    //TODO polaczyc metode update user z tools z tym zeby byla jedna
                    val profileUpdates = UserProfileChangeRequest.Builder()
                            .setDisplayName(name)
                            .build()
                    user!!.updateProfile(profileUpdates)
                            .addOnCompleteListener(object : OnCompleteListener<Void> {
                                override fun onComplete(p0: Task<Void>) {
                                    Log.i(TAG, "onComplete() name " + name)
//                                            val intent = Intent(context, ListOnline::class.java)
//                                            intent.setFlags((Intent.FLAG_ACTIVITY_NEW_TASK) or Intent.FLAG_ACTIVITY_CLEAR_TASK) //then we can't go back when press back button
//                                            context!!.startActivity(intent)
                                    activity.finish()
                                }
                            })
                })
    }
}