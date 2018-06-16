package kamilmilik.licencjat_gps_kid.invite

import android.os.Bundle
import android.text.TextUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kamilmilik.licencjat_gps_kid.R
import kamilmilik.licencjat_gps_kid.models.UserUniqueKey
import kotlinx.android.synthetic.main.activity_enter_invite.*
import android.text.InputFilter
import android.widget.Toast
import com.google.firebase.iid.FirebaseInstanceId
import kamilmilik.licencjat_gps_kid.ApplicationActivity
import kamilmilik.licencjat_gps_kid.map.MapActivity
import kamilmilik.licencjat_gps_kid.utils.Tools
import kamilmilik.licencjat_gps_kid.models.User
import kamilmilik.licencjat_gps_kid.utils.Constants


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
        buttonSubmitInvite.setOnClickListener({
            var enteredInviteCode = editTextEnterInviteCode.text.toString().toUpperCase()
            enteredInviteCode = Tools.removeWhiteSpaceFromString(enteredInviteCode)
            if (!TextUtils.isEmpty(enteredInviteCode)) {
                findUserWhichGeneratedInviteCode(enteredInviteCode)
            }
        })
    }

    /**
     * Find in firebase database given invite code and return user uid who generated this invite code
     * @param enteredInviteCode
     */
    private fun findUserWhichGeneratedInviteCode(enteredInviteCode: String) {
        val reference = FirebaseDatabase.getInstance().reference
        val query = reference.child(Constants.DATABASE_USER_KEYS)
                .orderByChild(Constants.DATABASE_UNIQUE_KEY_FIELD)
                .equalTo(enteredInviteCode)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (singleSnapshot in dataSnapshot.children) {
                    userUniqueKeyModel = singleSnapshot.getValue(UserUniqueKey::class.java)
                    var currentUser = FirebaseAuth.getInstance().currentUser
                    if (!checkIfGivenUsersAreDifferent(userUniqueKeyModel!!.user_id, currentUser!!.uid)) {//prevent add user self
                        addConnectedUserToDatabase(userUniqueKeyModel!!)
                    }
                    Tools.startNewActivityWithoutPrevious(this@EnterInviteActivity, MapActivity::class.java)
                }
                if (dataSnapshot.value == null) {
                    Toast.makeText(this@EnterInviteActivity, getString(R.string.invalidCode), Toast.LENGTH_LONG).show()
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun checkIfGivenUsersAreDifferent(user1Id: String?, user2Id: String): Boolean {
        if (user1Id.equals(user2Id)) {
            Toast.makeText(this, getString(R.string.yourselfInviteCode), Toast.LENGTH_LONG).show()
            return true
        } else {
            return false
        }
    }

    /**
     * add new connection between two account, @param is founded user who had generated invite code
     * @param userUniqueKeyModel
     */
    private fun addConnectedUserToDatabase(userUniqueKeyModel: UserUniqueKey) {
        var currentFirebaseUser = FirebaseAuth.getInstance().currentUser!!
        var currentUserId = currentFirebaseUser.uid
        var currentUserEmail = currentFirebaseUser.email
        var deviceTokenId = FirebaseInstanceId.getInstance().token
        var name = currentFirebaseUser.displayName

        var currentUser = User(currentUserId, currentUserEmail!!, deviceTokenId!!, name!!)
        var followedUser = User(userUniqueKeyModel.user_id!!, userUniqueKeyModel!!.user_email!!, userUniqueKeyModel.device_token!!, userUniqueKeyModel.user_name!!)
        FirebaseDatabase.getInstance().reference
                .child(Constants.DATABASE_FOLLOWING)
                .child(currentUser.user_id)
                .child(followedUser!!.user_id)
                .child(Constants.DATABASE_USER_FIELD)
                .setValue(followedUser);

        FirebaseDatabase.getInstance().reference
                .child(Constants.DATABASE_FOLLOWERS)
                .child(userUniqueKeyModel!!.user_id)
                .child(currentUser.user_id)
                .child(Constants.DATABASE_USER_FIELD)
                .setValue(currentUser)
    }
}
