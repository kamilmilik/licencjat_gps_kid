package kamilmilik.gps_tracker.profile

import android.app.Activity
import android.os.Bundle
import android.view.View
import kamilmilik.gps_tracker.R
import kotlinx.android.synthetic.main.activity_profile.*
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kamilmilik.gps_tracker.login.DatabaseOnlineUserAction
import kamilmilik.gps_tracker.login.LoginActivity
import kamilmilik.gps_tracker.utils.Tools
import android.text.method.PasswordTransformationMethod
import android.util.Log

import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.database.*
import kamilmilik.gps_tracker.ApplicationActivity
import kamilmilik.gps_tracker.utils.Constants
import kamilmilik.gps_tracker.models.User
import kamilmilik.gps_tracker.utils.Constants.CHANGE_NAME_ACTIVITY_RESULT


class ProfileActivity : ApplicationActivity() {

    private val TAG = ProfileActivity::class.java.simpleName

    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)
        Tools.setupToolbar(this, true).title = getString(R.string.Profile)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        FirebaseAuth.getInstance().currentUser?.let { currentUser ->
            changeUserNameAction(currentUser)

            changePasswordAction(currentUser)

            deleteAccountAction(currentUser)
        }


    }

    private fun changeUserNameAction(currentUser: FirebaseUser) {
        val userName = currentUser.displayName
        userNameTextView.text = userName
        userNameRelative.setOnClickListener(View.OnClickListener {
            val alert = Tools.makeAlertDialogBuilder(this, getString(R.string.editName), getString(R.string.writeNewName))
            val input = EditText(this)
            alert.setView(input)
            alert.setPositiveButton(getString(R.string.ok)) { dialog, whichButton ->
                val newName = input.text.toString()
                if (newName.isEmpty()) {
                    Toast.makeText(this@ProfileActivity, getString(R.string.emptyFieldInformation), Toast.LENGTH_LONG).show()
                } else {
                    Tools.updateProfileName(this, currentUser, newName, OnCompleteListener {
                        sendNewNameToDatabase(this, currentUser, newName)
                        setToTriggerChangeNameInPreviousActivity()
                    })
                }
            }
            alert.setNegativeButton(getString(R.string.cancel)) { dialog, whichButton -> }
            alert.show()
        })
    }

    private fun setToTriggerChangeNameInPreviousActivity() {
        intent.putExtra(CHANGE_NAME_ACTIVITY_RESULT, true)
        setResult(RESULT_OK, intent);
    }

    private fun sendNewNameToDatabase(activity: Activity, currentUser: FirebaseUser, newName: String) {
        val map = HashMap<String, Any>() as MutableMap<String, Any>
        map.put(Constants.DATABASE_USER_NAME_FIELD, newName)
        FirebaseDatabase.getInstance().reference.child(Constants.DATABASE_USER_ACCOUNT_SETTINGS).child(currentUser.uid).updateChildren(map)
        FirebaseDatabase.getInstance().reference.child(Constants.DATABASE_LOCATIONS).child(currentUser.uid).updateChildren(map)
        activity.userNameTextView.text = currentUser.displayName
    }

    private fun changePasswordAction(currentUser: FirebaseUser) {
        userPasswordRelative.setOnClickListener(View.OnClickListener {
            val alert2 = Tools.makeAlertDialogBuilder(this, getString(R.string.reauthenticate), getString(R.string.reauthenticateInformation))
            val inflate = layoutInflater.inflate(R.layout.login_dialog, null)
            alert2.setView(inflate)
            alert2.setPositiveButton(getString(R.string.ok)) { dialog, whichButton ->
                val email = inflate.findViewById<View>(R.id.emailDialog) as EditText
                val password = inflate.findViewById<View>(R.id.passwordDialog) as EditText
                if (email.text.isEmpty() || password.text.isEmpty()) {
                    Toast.makeText(this@ProfileActivity, getString(R.string.emptyFieldInformation), Toast.LENGTH_LONG).show()
                } else {
                    val credential = EmailAuthProvider
                            .getCredential(email.text.toString(), password.text.toString())
                    currentUser.reauthenticate(credential)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val alert = Tools.makeAlertDialogBuilder(this, getString(R.string.changePassword), getString(R.string.enterNewPassword))
                                    val input = EditText(this)
                                    input.transformationMethod = PasswordTransformationMethod.getInstance()
                                    alert.setView(input)
                                    alert.setPositiveButton(getString(R.string.ok)) { dialog, whichButton ->
                                        val newPassword = input.text.toString()
                                        if (newPassword.isEmpty()) {
                                            Toast.makeText(this@ProfileActivity, getString(R.string.emptyFieldInformation), Toast.LENGTH_LONG).show()
                                        } else {
                                            currentUser.updatePassword(newPassword)
                                                    .addOnCompleteListener { task ->
                                                        if (task.isSuccessful) {
                                                            Toast.makeText(this@ProfileActivity, getString(R.string.passwordChanged), Toast.LENGTH_LONG).show()
                                                            DatabaseOnlineUserAction().logoutUser()
                                                            Tools.startNewActivityWithoutPrevious(this, LoginActivity::class.java)
                                                        }
                                                    }
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
        deleteAccountRelative.setOnClickListener {
            val alert2 = Tools.makeAlertDialogBuilder(this, getString(R.string.reauthenticate), getString(R.string.reauthenticateInformation))
            val inflate = layoutInflater.inflate(R.layout.login_dialog, null)
            alert2.setView(inflate)
            alert2.setPositiveButton(getString(R.string.ok)) { dialog, whichButton ->
                val email = inflate.findViewById<View>(R.id.emailDialog) as EditText
                val password = inflate.findViewById<View>(R.id.passwordDialog) as EditText
                if (email.text.isEmpty() || password.text.isEmpty()) {
                    Toast.makeText(this@ProfileActivity, getString(R.string.emptyFieldInformation), Toast.LENGTH_LONG).show()
                } else {
                    val credential = EmailAuthProvider
                            .getCredential(email.text.toString(), password.text.toString())
                    currentUser.reauthenticate(credential)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val alert = Tools.makeAlertDialogBuilder(this, getString(R.string.deleteUser), getString(R.string.deleteUserConfirmation))
                                    alert.setPositiveButton(getString(R.string.ok)) { dialog, whichButton ->
                                        removeUserFromDatabase(currentUser)
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
        }
    }

    private fun removeUserFromDatabase(currentUser: FirebaseUser) {
        FirebaseDatabase.getInstance().getReference(Constants.DATABASE_USER_ACCOUNT_SETTINGS).child(currentUser.uid).removeValue()
        FirebaseDatabase.getInstance().getReference(Constants.DATABASE_USER_POLYGONS).child(currentUser.uid).removeValue()
        FirebaseDatabase.getInstance().getReference(Constants.DATABASE_LOCATIONS).child(currentUser.uid).removeValue()
        FirebaseDatabase.getInstance().getReference(Constants.DATABASE_LAST_ONLINE).child(currentUser.uid).removeValue()
        FirebaseDatabase.getInstance().getReference(Constants.DATABASE_USER_LOGGED).child(Constants.DATABASE_USER_FIELD).child(currentUser.uid).removeValue()


        val reference = FirebaseDatabase.getInstance().reference
        removeUsersFromConnection(currentUser, reference, Constants.DATABASE_FOLLOWERS, Constants.DATABASE_FOLLOWING)
        removeUsersFromConnection(currentUser, reference, Constants.DATABASE_FOLLOWING, Constants.DATABASE_FOLLOWERS)
    }

    private fun removeUsersFromConnection(currentUser: FirebaseUser, reference: DatabaseReference, databaseNode: String, databaseNode2: String) {
        Log.i(TAG, "removeUsersFromConnection() current user " + currentUser.uid)
        val query = reference.child(databaseNode)
                .orderByKey()
                .equalTo(currentUser.uid)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (singleSnapshot in dataSnapshot.children) {
                    for (childSingleSnapshot in singleSnapshot.children) {
                        // User class since other class similar has hashcode and equals
                        val userFollowers = childSingleSnapshot.child(Constants.DATABASE_USER_FIELD).getValue(User::class.java)
                        Log.i(TAG, "onDataChange()")
                        val query2 = reference.child(databaseNode2)
                                .orderByKey()
                                .equalTo(userFollowers?.user_id)
                        query2.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                for (singleSnapshot2 in dataSnapshot.children) {
                                    for (childSingleSnapshot2 in singleSnapshot2.children) {
                                        val userFollowing = childSingleSnapshot2.child(Constants.DATABASE_USER_FIELD).getValue(User::class.java)
                                        // It prevent for remove user which we not delete, we must delete only currentUser, userFollowing could have other user which he follow.
                                        if (userFollowing?.user_id.equals(currentUser.uid)) {
                                            childSingleSnapshot2.ref.removeValue()
                                            FirebaseDatabase.getInstance().getReference(databaseNode).child(currentUser.uid).removeValue()
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
