package kamilmilik.gps_tracker.invite

import android.location.Location
import android.os.Bundle
import android.text.TextUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kamilmilik.gps_tracker.R
import kamilmilik.gps_tracker.models.UserUniqueKey
import kotlinx.android.synthetic.main.activity_enter_invite.*
import android.text.InputFilter
import android.util.Log
import android.view.View
import android.widget.Toast
import kamilmilik.gps_tracker.ApplicationActivity
import kamilmilik.gps_tracker.map.MapActivity
import kamilmilik.gps_tracker.models.ConnectionUser
import kamilmilik.gps_tracker.models.LocationPermissionModel
import kamilmilik.gps_tracker.utils.Tools
import kamilmilik.gps_tracker.utils.Constants
import kamilmilik.gps_tracker.utils.Constants.DATABASE_LOCATIONS
import kamilmilik.gps_tracker.utils.Constants.DATABASE_PERMISSIONS_FIELD
import kamilmilik.gps_tracker.utils.ObjectsUtils
import kotlinx.android.synthetic.main.progress_bar.*
import android.support.design.widget.Snackbar


class EnterInviteActivity : ApplicationActivity() {
    private val TAG = EnterInviteActivity::class.java.simpleName

    private var userUniqueKeyModel: UserUniqueKey? = null

    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)
        Tools.setupToolbar(this, true).title = getString(R.string.EnterInviteCodeActivityName)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_enter_invite)

        submitEnteredInviteCodeButtonAction()
    }

    private fun submitEnteredInviteCodeButtonAction() {
        editTextEnterInviteCode.filters = arrayOf<InputFilter>(InputFilter.AllCaps())
        buttonSubmitInvite.setOnClickListener {
            var enteredInviteCode = editTextEnterInviteCode.text.toString().toUpperCase()
            enteredInviteCode = Tools.removeWhiteSpaceFromString(enteredInviteCode)
            if (!TextUtils.isEmpty(enteredInviteCode)) {
                findUserWhichGeneratedInviteCode(enteredInviteCode)
            }
        }
    }

    /**
     * Find in firebase database given invite code and return user uid who generated this invite code.
     * @param enteredInviteCode
     */
    private fun findUserWhichGeneratedInviteCode(enteredInviteCode: String) {
        progressBarRelative.visibility = View.VISIBLE
        val reference = FirebaseDatabase.getInstance().reference
        val query = reference.child(Constants.DATABASE_USER_KEYS)
                .orderByChild(Constants.DATABASE_UNIQUE_KEY_FIELD)
                .equalTo(enteredInviteCode)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (singleSnapshot in dataSnapshot.children) {
                    userUniqueKeyModel = singleSnapshot.getValue(UserUniqueKey::class.java)
                    userUniqueKeyModel?.let { userUniqueKeyModel ->
                        val currentUser = FirebaseAuth.getInstance().currentUser
                        if (!checkIfGivenUsersAreDifferent(userUniqueKeyModel.user_id, currentUser?.uid)) {// Prevent add user self.
                            addConnectedUserToDatabase(userUniqueKeyModel)
                        }
                    }
                }
                if (dataSnapshot.value == null) {
                    Toast.makeText(this@EnterInviteActivity, getString(R.string.invalidCode), Toast.LENGTH_LONG).show()
                    progressBarRelative.visibility = View.GONE
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun checkIfGivenUsersAreDifferent(user1Id: String?, user2Id: String?): Boolean {
        return if (user1Id.equals(user2Id)) {
            Toast.makeText(this, getString(R.string.yourselfInviteCode), Toast.LENGTH_LONG).show()
            true
        } else {
            false
        }
    }

    /**
     * Add new connection between two account, @param is founded user who had generated invite code.
     * @param userUniqueKeyModel
     */
    private fun addConnectedUserToDatabase(userUniqueKeyModel: UserUniqueKey) {
        FirebaseAuth.getInstance().currentUser?.let { currentFirebaseUser ->
            ObjectsUtils.safeLetFirebaseUser(currentFirebaseUser) { currentUserUid, currentUserEmail, currentUserName ->
                userUniqueKeyModel.user_id?.let { followedUserId ->
                    val currentUser = ConnectionUser(currentUserUid)
                    val followedUser = ConnectionUser(followedUserId)

                    FirebaseDatabase.getInstance().reference
                            .child(Constants.DATABASE_FOLLOWING)
                            .child(currentUser.user_id)
                            .child(followedUser.user_id)
                            .child(Constants.DATABASE_USER_FIELD)
                            .setValue(followedUser)

                    FirebaseDatabase.getInstance().reference
                            .child(Constants.DATABASE_FOLLOWERS)
                            .child(userUniqueKeyModel.user_id)
                            .child(currentUser.user_id)
                            .child(Constants.DATABASE_USER_FIELD)
                            .setValue(currentUser)

                    FirebaseDatabase.getInstance().reference
                            .child(DATABASE_LOCATIONS)
                            .child(currentUserUid)
                            .child(DATABASE_PERMISSIONS_FIELD)
                            .child(userUniqueKeyModel.user_id)
                            .setValue(LocationPermissionModel(true))
                }
                Tools.makeAlertDialogBuilder(this, getString(R.string.followingTitle), getString(R.string.inviteUserInformation)).setPositiveButton(R.string.ok) { diaog, whichButton ->
                    finish()
                }
                        .setCancelable(false).create().show()
                progressBarRelative.visibility = View.GONE
            }
        }
    }
}
