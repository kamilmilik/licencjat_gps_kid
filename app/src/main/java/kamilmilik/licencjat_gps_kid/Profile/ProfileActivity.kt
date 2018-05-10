package kamilmilik.licencjat_gps_kid.Profile

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import kamilmilik.licencjat_gps_kid.R
import kotlinx.android.synthetic.main.activity_profile.*
import android.support.v4.app.NavUtils
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kamilmilik.licencjat_gps_kid.Helper.UserOperations.OnlineUserHelper
import kamilmilik.licencjat_gps_kid.Login.MainActivity
import kamilmilik.licencjat_gps_kid.Utils.Tools
import android.text.method.PasswordTransformationMethod
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.EmailAuthProvider
import android.support.annotation.NonNull
import android.widget.TextView
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.database.*
import kamilmilik.licencjat_gps_kid.ApplicationActivity
import kamilmilik.licencjat_gps_kid.ListOnline
import kamilmilik.licencjat_gps_kid.models.User
import java.nio.file.Files.delete
import java.util.concurrent.atomic.AtomicInteger


class ProfileActivity : ApplicationActivity() {

    private val TAG = ProfileActivity::class.java.simpleName

    private var workCounter: AtomicInteger? = AtomicInteger(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
//        setupToolbar()

        val currentUser = FirebaseAuth.getInstance().currentUser

        changeUserNameAction(currentUser!!)

        changePasswordAction(currentUser)

//        changeEmailAction(currentUser)

        deleteAccountAction(currentUser)

    }

//    private fun setupToolbar() {
//        toolbarProfile.title = "User Profile"
//        setSupportActionBar(toolbarProfile)
//        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
//        supportActionBar!!.setDisplayShowHomeEnabled(true)
//    }

    private fun changeUserNameAction(currentUser : FirebaseUser){
        var userName = currentUser!!.displayName
        userNameTextView.text = userName
        userNameRelative.setOnClickListener(View.OnClickListener {
            var alert = Tools.makeAlertDialogBuilder(this, "Edit name", "Write new name")
            var input = EditText(this)
            alert.setView(input)
            alert.setPositiveButton("Ok") { dialog, whichButton ->
                val value = input.text.toString()
                if(value.isEmpty()){
                    Toast.makeText(this@ProfileActivity, "Text field cannot be empty", Toast.LENGTH_LONG).show()
                }else{
                    Tools.updateProfileName(this, currentUser, value, input)
                }
            }
            alert.setNegativeButton("Cancel") { dialog, whichButton -> }
            alert.show()
        })
    }

//    private fun changeEmailAction(currentUser: FirebaseUser) {
//        currentUser!!.reload().addOnSuccessListener {
//            Log.i(TAG, "changeEmailAction() reload user " + currentUser.email)
//            userEmailTextView.text = currentUser.email
//            userEmailRelative.setOnClickListener({
//                var alert2 = Tools.makeAlertDialogBuilder(this, "Re-authenticate", "You must re-authenticate\n Sign in again to change email")
//                var inflate = layoutInflater.inflate(R.layout.login_dialog, null)
//                alert2.setView(inflate)
//                alert2.setPositiveButton("Ok") { dialog, whichButton ->
//                    var email = inflate.findViewById<View>(R.id.emailDialog) as EditText
//                    var password = inflate.findViewById<View>(R.id.passwordDialog) as EditText
//                    if (email.text.isEmpty() || password.text.isEmpty()) {
//                        Toast.makeText(this@ProfileActivity, "Text field cannot be empty", Toast.LENGTH_LONG).show()
//                    } else {
//                        val credential = EmailAuthProvider
//                                .getCredential(email.text.toString(), password.text.toString())
//                        currentUser!!.reauthenticate(credential)
//                                .addOnCompleteListener { task ->
//                                    if (task.isSuccessful) {
//                                        var alert = Tools.makeAlertDialogBuilder(this, "Change email", "Enter your new email")
//                                        var input = EditText(this)
//                                        alert.setView(input)
//                                        alert.setPositiveButton("Ok") { dialog, whichButton ->
//                                            val newEmail = input.text.toString()
//                                            if (newEmail.isEmpty()) {
//                                                Toast.makeText(this@ProfileActivity, "Text field cannot be empty", Toast.LENGTH_LONG).show()
//                                            } else {
//                                                FirebaseAuth.getInstance().useAppLanguage()
//                                                currentUser.updateEmail(newEmail)
//                                                        .addOnCompleteListener { task ->
//                                                            if (task.isSuccessful) {
//                                                                Toast.makeText(this@ProfileActivity, "Email changed", Toast.LENGTH_LONG).show()
//                                                                userEmailTextView.text = currentUser.email
//                                                            } else {
//                                                                Toast.makeText(this@ProfileActivity, "Error try again change your email.", Toast.LENGTH_LONG).show()
//                                                            }
//                                                        }
//                                            }
//                                        }
//                                        alert.setNegativeButton("Cancel") { dialog, whichButton -> }
//                                        alert.show()
//                                        Toast.makeText(this@ProfileActivity, "re-authenticate", Toast.LENGTH_LONG).show()
//                                    } else {
//                                        Toast.makeText(this@ProfileActivity, "Your email or password is incorrect. Try again.", Toast.LENGTH_LONG).show()
//                                    }
//                                }
//                    }
//                }
//                alert2.setNegativeButton("Cancel") { dialog, whichButton -> }
//                alert2.show()
//            })
//        }
//    }

    private fun changePasswordAction(currentUser : FirebaseUser){
        Log.i(TAG,"changePasswordAction()")
        userPasswordRelative.setOnClickListener(View.OnClickListener {
            var alert2 = Tools.makeAlertDialogBuilder(this, "Re-authenticate", "You must re-authenticate\nSign in again")
            var inflate = layoutInflater.inflate(R.layout.login_dialog, null)
            alert2.setView(inflate)
            alert2.setPositiveButton("Ok") { dialog, whichButton ->
                var email = inflate.findViewById<View>(R.id.emailDialog) as EditText
                var password = inflate.findViewById<View>(R.id.passwordDialog) as EditText
                if (email.text.isEmpty() || password.text.isEmpty()) {
                    Toast.makeText(this@ProfileActivity, "Text field cannot be empty", Toast.LENGTH_LONG).show()
                } else {
                    val credential = EmailAuthProvider
                            .getCredential(email.text.toString(), password.text.toString())
                    currentUser!!.reauthenticate(credential)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    var alert = Tools.makeAlertDialogBuilder(this, "Change password", "Enter your new password")
                                    var input = EditText(this)
                                    input.transformationMethod = PasswordTransformationMethod.getInstance()
                                    alert.setView(input)
                                    alert.setPositiveButton("Ok") { dialog, whichButton ->
                                        val newPassword = input.text.toString()
                                        if(newPassword.isEmpty()){
                                            Toast.makeText(this@ProfileActivity, "Text field cannot be empty", Toast.LENGTH_LONG).show()
                                        }else{
                                            currentUser.updatePassword(newPassword)
                                                    .addOnCompleteListener({ task ->
                                                        if (task.isSuccessful) {
                                                            Toast.makeText(this@ProfileActivity, "Password changed", Toast.LENGTH_LONG).show()
                                                            OnlineUserHelper().logoutUser()
                                                            var  intent =  Intent(this@ProfileActivity, MainActivity::class.java)
                                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                                                            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                                            startActivity(intent)
                                                        }
                                                    })
                                        }
                                    }
                                    alert.setNegativeButton("Cancel") { dialog, whichButton -> }
                                    alert.show()
                                } else {
                                    Toast.makeText(this@ProfileActivity, "Your email or password is incorrect. Try again.", Toast.LENGTH_LONG).show()
                                }
                            }
                }
            }
            alert2.setNegativeButton("Cancel") { dialog, whichButton -> }
            alert2.show()
        })
    }

    private fun deleteAccountAction(currentUser : FirebaseUser){
        deleteAccountRelative.setOnClickListener({
            var alert2 = Tools.makeAlertDialogBuilder(this, "Re-authenticate", "You must re-authenticate\nSign in again")
            var inflate = layoutInflater.inflate(R.layout.login_dialog, null)
            alert2.setView(inflate)
            alert2.setPositiveButton("Ok") { dialog, whichButton ->
                var email = inflate.findViewById<View>(R.id.emailDialog) as EditText
                var password = inflate.findViewById<View>(R.id.passwordDialog) as EditText
                if (email.text.isEmpty() || password.text.isEmpty()) {
                    Toast.makeText(this@ProfileActivity, "Text field cannot be empty", Toast.LENGTH_LONG).show()
                } else {
                    val credential = EmailAuthProvider
                            .getCredential(email.text.toString(), password.text.toString())
                    currentUser!!.reauthenticate(credential)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    var alert = Tools.makeAlertDialogBuilder(this, "delete user", "Are you sure to delete your account?")
                                    alert.setPositiveButton("Ok") { dialog, whichButton ->
                                        //TODO przy 3 userach za duzo usuwa
                                        removeUserFromDatabase(currentUser)
                                        currentUser.delete()
                                                .addOnCompleteListener({ task ->
                                                    if (task.isSuccessful) {
                                                        Toast.makeText(this@ProfileActivity, "Your account has been deleted", Toast.LENGTH_LONG).show()
                                                        var intent = Intent(this@ProfileActivity, MainActivity::class.java)
                                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                                                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                                        startActivity(intent)
                                                    }
                                                })
                                    }
                                    alert.setNegativeButton("Cancel") { dialog, whichButton -> }
                                    alert.show()
                                } else {
                                    Toast.makeText(this@ProfileActivity, "Your email or password is incorrect. Try again.", Toast.LENGTH_LONG).show()
                                }
                            }
                }
            }
            alert2.setNegativeButton("Cancel") { dialog, whichButton -> }
            alert2.show()
        })
    }

    private fun removeUserFromDatabase(currentUser : FirebaseUser){
        FirebaseDatabase.getInstance().getReference("user_account_settings").child(currentUser.uid).removeValue()
        FirebaseDatabase.getInstance().getReference("user_polygons").child(currentUser.uid).removeValue()
        FirebaseDatabase.getInstance().getReference("Locations").child(currentUser.uid).removeValue()
        FirebaseDatabase.getInstance().getReference("last_online").child(currentUser.uid).removeValue()

        val reference = FirebaseDatabase.getInstance().reference
        removeUserFromFollowing(currentUser, reference)
        removeUserFromFollowers(currentUser, reference)


    }
    private fun removeUserFromFollowing(currentUser: FirebaseUser, reference: DatabaseReference){
        Log.i(TAG,"removeUserFromFollowing()")
        val query = reference.child("followers")
                .orderByKey()
                .equalTo(currentUser!!.uid)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (singleSnapshot in dataSnapshot.children) {
                    for (childSingleSnapshot in singleSnapshot.children) {
                        var userFollowers = childSingleSnapshot.child("user").getValue(User::class.java)
                        Log.i(TAG,"onDataChange() found user followers " + userFollowers!!.email )
                        val query = reference.child("following")
                                .orderByKey()
                                .equalTo(userFollowers!!.user_id)
                        query.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                for (singleSnapshot in dataSnapshot.children) {
                                    for (childSingleSnapshot in singleSnapshot.children) {
                                        var userFollowing = childSingleSnapshot.child("user").getValue(User::class.java)
                                        // It prevent for remove user which we not delete, we must delete only currentUser, userFollowing could have other user which he follow
                                        if(userFollowing!!.user_id.equals(currentUser.uid)){
                                            childSingleSnapshot.ref.removeValue()
                                            FirebaseDatabase.getInstance().getReference("followers").child(currentUser.uid).removeValue()
                                        }
                                    }
                                }
                            }
                            override fun onCancelled(databaseError: DatabaseError?) {
                                Log.i(TAG, "onCancelled: " + databaseError!!.message)
                            }
                        })
                    }
                }
            }
            override fun onCancelled(databaseError: DatabaseError?) {
                Log.i(TAG, "onCancelled: " + databaseError!!.message)
            }
        })
    }

    private fun removeUserFromFollowers(currentUser: FirebaseUser, reference: DatabaseReference) {
        Log.i(TAG,"removeUserFromFollowers()")
        val query = reference.child("following")
                .orderByKey()
                .equalTo(currentUser!!.uid)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (singleSnapshot in dataSnapshot.children) {
                    for (childSingleSnapshot in singleSnapshot.children) {
                        var userFollowing = childSingleSnapshot.child("user").getValue(User::class.java)
                        Log.i(TAG,"onDataChange() found user " + userFollowing!!.email)
                        val query = reference.child("followers")
                                .orderByKey()
                                .equalTo(userFollowing!!.user_id)
                        query.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                for (singleSnapshot in dataSnapshot.children) {
                                    for (childSingleSnapshot in singleSnapshot.children) {
                                        var userFollowers = childSingleSnapshot.child("user").getValue(User::class.java)
                                        // It prevent for remove user which we not delete, we must delete only currentUser, userFollowing could have other user which he follow
                                        if(userFollowers!!.user_id.equals(currentUser.uid)){
                                            childSingleSnapshot.ref.removeValue()
                                            FirebaseDatabase.getInstance().getReference("following").child(currentUser.uid).removeValue()
                                        }
                                    }
                                }
                            }

                            override fun onCancelled(databaseError: DatabaseError?) {
                                Log.i(TAG, "onCancelled: " + databaseError!!.message)
                            }
                        })
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError?) {
                Log.i(TAG, "onCancelled: " + databaseError!!.message)
            }
        })
    }
        override fun onBackPressed() {
        NavUtils.navigateUpFromSameTask(this)
        super.onBackPressed()
    }
}
