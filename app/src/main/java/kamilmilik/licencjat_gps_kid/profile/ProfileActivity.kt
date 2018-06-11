package kamilmilik.licencjat_gps_kid.profile

import android.app.Activity
import android.os.Bundle
import android.view.View
import kamilmilik.licencjat_gps_kid.R
import kotlinx.android.synthetic.main.activity_profile.*
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kamilmilik.licencjat_gps_kid.online.DatabaseOnlineUserAction
import kamilmilik.licencjat_gps_kid.login.LoginActivity
import kamilmilik.licencjat_gps_kid.utils.Tools
import android.text.method.PasswordTransformationMethod
import kotlinx.android.synthetic.main.activity_profile.*

import android.widget.RelativeLayout
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.database.*
import kamilmilik.licencjat_gps_kid.ApplicationActivity
import kamilmilik.licencjat_gps_kid.utils.Constants
import kamilmilik.licencjat_gps_kid.models.User
import kotlinx.android.synthetic.main.progress_bar.*
import java.util.concurrent.atomic.AtomicInteger


class ProfileActivity : ApplicationActivity() {

    private val TAG = ProfileActivity::class.java.simpleName

    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)
        Tools.setupToolbar(this, true).title = getString(R.string.Profile)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val currentUser = FirebaseAuth.getInstance().currentUser

        changeUserNameAction(currentUser!!)

        changePasswordAction(currentUser)

//        changeEmailAction(currentUser)

        deleteAccountAction(currentUser)

    }

    private fun changeUserNameAction(currentUser: FirebaseUser) {
        var userName = currentUser!!.displayName
        userNameTextView.text = userName
        userNameRelative.setOnClickListener(View.OnClickListener {
            var alert = Tools.makeAlertDialogBuilder(this, getString(R.string.editName), getString(R.string.writeNewName))
            var input = EditText(this)
            alert.setView(input)
            alert.setPositiveButton("Ok") { dialog, whichButton ->
                val newName = input.text.toString()
                if (newName.isEmpty()) {
                    Toast.makeText(this@ProfileActivity, getString(R.string.emptyFieldInformation), Toast.LENGTH_LONG).show()
                } else {
                    Tools.updateProfileName(this, currentUser, newName, OnCompleteListener {
                        sendNewNameToDatabase(this, currentUser, newName)
                    })
                }
            }
            alert.setNegativeButton(getString(R.string.cancel)) { dialog, whichButton -> }
            alert.show()
        })
    }

    private fun sendNewNameToDatabase(activity : Activity, currentUser: FirebaseUser, newName : String) {
        var map = HashMap<String, Any>() as MutableMap<String, Any>
        map.put(Constants.DATABASE_USER_NAME_FIELD, newName)
        FirebaseDatabase.getInstance().reference.child(Constants.DATABASE_USER_ACCOUNT_SETTINGS).child(currentUser.uid).updateChildren(map)
        FirebaseDatabase.getInstance().reference.child(Constants.DATABASE_LOCATIONS).child(currentUser.uid).updateChildren(map)
        activity.userNameTextView.text = currentUser.displayName
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

    private fun changePasswordAction(currentUser: FirebaseUser) {
        userPasswordRelative.setOnClickListener(View.OnClickListener {
            var alert2 = Tools.makeAlertDialogBuilder(this, getString(R.string.reauthenticate), getString(R.string.reauthenticateInformation))
            var inflate = layoutInflater.inflate(R.layout.login_dialog, null)
            alert2.setView(inflate)
            alert2.setPositiveButton(getString(R.string.ok)) { dialog, whichButton ->
                var email = inflate.findViewById<View>(R.id.emailDialog) as EditText
                var password = inflate.findViewById<View>(R.id.passwordDialog) as EditText
                if (email.text.isEmpty() || password.text.isEmpty()) {
                    Toast.makeText(this@ProfileActivity, getString(R.string.emptyFieldInformation), Toast.LENGTH_LONG).show()
                } else {
                    val credential = EmailAuthProvider
                            .getCredential(email.text.toString(), password.text.toString())
                    currentUser!!.reauthenticate(credential)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    var alert = Tools.makeAlertDialogBuilder(this, getString(R.string.changePassword), getString(R.string.enterNewPassword))
                                    var input = EditText(this)
                                    input.transformationMethod = PasswordTransformationMethod.getInstance()
                                    alert.setView(input)
                                    alert.setPositiveButton(getString(R.string.ok)) { dialog, whichButton ->
                                        val newPassword = input.text.toString()
                                        if (newPassword.isEmpty()) {
                                            Toast.makeText(this@ProfileActivity, getString(R.string.emptyFieldInformation), Toast.LENGTH_LONG).show()
                                        } else {
                                            currentUser.updatePassword(newPassword)
                                                    .addOnCompleteListener({ task ->
                                                        if (task.isSuccessful) {
                                                            Toast.makeText(this@ProfileActivity, getString(R.string.passwordChanged), Toast.LENGTH_LONG).show()
                                                            DatabaseOnlineUserAction().logoutUser()
                                                            Tools.startNewActivityWithoutPrevious(this, LoginActivity::class.java)
                                                        }
                                                    })
                                        }
                                    }
                                    alert.setNegativeButton(getString(R.string.cancel)) { dialog, whichButton -> }
                                    alert.show()
                                } else {
                                    Toast.makeText(this@ProfileActivity, getString(R.string.emailOrPasswordIncorrect), Toast.LENGTH_LONG).show()
                                }
                            }
                }
            }
            alert2.setNegativeButton(getString(R.string.cancel)) { dialog, whichButton -> }
            alert2.show()
        })
    }

    private fun deleteAccountAction(currentUser: FirebaseUser) {
        deleteAccountRelative.setOnClickListener({
            var alert2 = Tools.makeAlertDialogBuilder(this, getString(R.string.reauthenticate), getString(R.string.reauthenticateInformation))
            var inflate = layoutInflater.inflate(R.layout.login_dialog, null)
            alert2.setView(inflate)
            alert2.setPositiveButton(getString(R.string.ok)) { dialog, whichButton ->
                var email = inflate.findViewById<View>(R.id.emailDialog) as EditText
                var password = inflate.findViewById<View>(R.id.passwordDialog) as EditText
                if (email.text.isEmpty() || password.text.isEmpty()) {
                    Toast.makeText(this@ProfileActivity, getString(R.string.emptyFieldInformation), Toast.LENGTH_LONG).show()
                } else {
                    val credential = EmailAuthProvider
                            .getCredential(email.text.toString(), password.text.toString())
                    currentUser!!.reauthenticate(credential)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    var alert = Tools.makeAlertDialogBuilder(this, getString(R.string.deleteUser), getString(R.string.deleteUserConfirmation))
                                    alert.setPositiveButton("Ok") { dialog, whichButton ->
                                        //TODO przy 3 userach za duzo usuwa
                                        removeUserFromDatabase(currentUser)
                                        currentUser.delete()
                                                .addOnCompleteListener({ task ->
                                                    if (task.isSuccessful) {
                                                        Toast.makeText(this@ProfileActivity, getString(R.string.deletedAccountInformation), Toast.LENGTH_LONG).show()
                                                        Tools.startNewActivityWithoutPrevious(this, LoginActivity::class.java)
                                                    }
                                                })
                                    }
                                    alert.setNegativeButton("Cancel") { dialog, whichButton -> }
                                    alert.show()
                                } else {
                                    Toast.makeText(this@ProfileActivity, getString(R.string.emailOrPasswordIncorrect), Toast.LENGTH_LONG).show()
                                }
                            }
                }
            }
            alert2.setNegativeButton(getString(R.string.cancel)) { dialog, whichButton -> }
            alert2.show()
        })
    }

    private fun removeUserFromDatabase(currentUser: FirebaseUser) {
        FirebaseDatabase.getInstance().getReference(Constants.DATABASE_USER_ACCOUNT_SETTINGS).child(currentUser.uid).removeValue()
        FirebaseDatabase.getInstance().getReference(Constants.DATABASE_USER_POLYGONS).child(currentUser.uid).removeValue()
        FirebaseDatabase.getInstance().getReference(Constants.DATABASE_LOCATIONS).child(currentUser.uid).removeValue()
        FirebaseDatabase.getInstance().getReference(Constants.DATABASE_LAST_ONLINE).child(currentUser.uid).removeValue()

        val reference = FirebaseDatabase.getInstance().reference
        removeUserFromFollowing(currentUser, reference)
        removeUserFromFollowers(currentUser, reference)


    }

    private fun removeUserFromFollowing(currentUser: FirebaseUser, reference: DatabaseReference) {
        val query = reference.child(Constants.DATABASE_FOLLOWERS)
                .orderByKey()
                .equalTo(currentUser!!.uid)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (singleSnapshot in dataSnapshot.children) {
                    for (childSingleSnapshot in singleSnapshot.children) {
                        var userFollowers = childSingleSnapshot.child(Constants.DATABASE_USER).getValue(User::class.java)
                        val query = reference.child(Constants.DATABASE_FOLLOWING)
                                .orderByKey()
                                .equalTo(userFollowers!!.user_id)
                        query.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                for (singleSnapshot in dataSnapshot.children) {
                                    for (childSingleSnapshot in singleSnapshot.children) {
                                        var userFollowing = childSingleSnapshot.child(Constants.DATABASE_USER).getValue(User::class.java)
                                        // It prevent for remove user which we not delete, we must delete only currentUser, userFollowing could have other user which he follow
                                        if (userFollowing!!.user_id.equals(currentUser.uid)) {
                                            childSingleSnapshot.ref.removeValue()
                                            FirebaseDatabase.getInstance().getReference(Constants.DATABASE_FOLLOWERS).child(currentUser.uid).removeValue()
                                        }
                                    }
                                }
                            }
                            override fun onCancelled(databaseError: DatabaseError?) {}
                        })
                    }
                }
            }
            override fun onCancelled(databaseError: DatabaseError?) {}
        })
    }

    private fun removeUserFromFollowers(currentUser: FirebaseUser, reference: DatabaseReference) {
        val query = reference.child(Constants.DATABASE_FOLLOWING)
                .orderByKey()
                .equalTo(currentUser!!.uid)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (singleSnapshot in dataSnapshot.children) {
                    for (childSingleSnapshot in singleSnapshot.children) {
                        var userFollowing = childSingleSnapshot.child(Constants.DATABASE_USER).getValue(User::class.java)
                        val query = reference.child(Constants.DATABASE_FOLLOWERS)
                                .orderByKey()
                                .equalTo(userFollowing!!.user_id)
                        query.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                for (singleSnapshot in dataSnapshot.children) {
                                    for (childSingleSnapshot in singleSnapshot.children) {
                                        var userFollowers = childSingleSnapshot.child(Constants.DATABASE_USER).getValue(User::class.java)
                                        // It prevent for remove user which we not delete, we must delete only currentUser, userFollowing could have other user which he follow
                                        if (userFollowers!!.user_id.equals(currentUser.uid)) {
                                            childSingleSnapshot.ref.removeValue()
                                            FirebaseDatabase.getInstance().getReference(Constants.DATABASE_FOLLOWING).child(currentUser.uid).removeValue()
                                        }
                                    }
                                }
                            }
                            override fun onCancelled(databaseError: DatabaseError?) {}
                        })
                    }
                }
            }
            override fun onCancelled(databaseError: DatabaseError?) {}
        })
    }

}
