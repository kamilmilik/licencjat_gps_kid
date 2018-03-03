package kamilmilik.licencjat_gps_kid.Invite

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
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
import kamilmilik.licencjat_gps_kid.ListOnline
import kamilmilik.licencjat_gps_kid.models.User


class EnterInviteActivity : AppCompatActivity() {
    private val TAG : String = "EnterInviteActivity"
    private var userUniqueKeyModel : UserUniqueKey? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_enter_invite)

        setupToolbar()

        submitEnteredInviteCodeButtonAction()
    }
    private fun setupToolbar(){
        toolbarEnterInvite.setTitle("Generate Invite Code")
        setSupportActionBar(toolbarEnterInvite)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true);
        supportActionBar!!.setDisplayShowHomeEnabled(true);
    }
    private fun submitEnteredInviteCodeButtonAction(){
        editTextEnterInviteCode.filters = arrayOf<InputFilter>(InputFilter.AllCaps())
        buttonSubmitInvite.setOnClickListener({
            var enteredInviteCode = editTextEnterInviteCode.text.toString().toUpperCase()
            enteredInviteCode = removeWhiteSpaceFromString(enteredInviteCode)
            if(!TextUtils.isEmpty(enteredInviteCode)){
                findUserWhichGeneratedInviteCode(enteredInviteCode)
            }

        })
    }
    private fun removeWhiteSpaceFromString(givenString: String) : String{
        return givenString.replace("\\s".toRegex(), "")
    }
    /**
     * Find in firebase database given invite code and return user uid who generated this invite code
     * @param enteredInviteCode
     */
    private fun findUserWhichGeneratedInviteCode(enteredInviteCode : String)  {
        val reference = FirebaseDatabase.getInstance().reference
        val query = reference.child("user_keys")
                .orderByChild("unique_key")
                .equalTo(enteredInviteCode)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (singleSnapshot in dataSnapshot.children) {
                    userUniqueKeyModel = singleSnapshot.getValue(UserUniqueKey::class.java)
                    Log.i(TAG, "found user : " + userUniqueKeyModel!!.user_id)
                    var currentUser = FirebaseAuth.getInstance().currentUser
                    if(!checkIfGivenUsersAreDifferent(userUniqueKeyModel!!.user_id,currentUser!!.uid)){//prevent add user self
                        addConnectedUserToDatabase(userUniqueKeyModel!!)
                    }
                    goToPreviousActivity()
                }
                if(dataSnapshot.value == null){
                    Toast.makeText(this@EnterInviteActivity,"This code is invalid",Toast.LENGTH_LONG).show()
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Log.i(TAG,"onCancelled: " + databaseError.message)
            }
        })
    }
    private fun checkIfGivenUsersAreDifferent(user1Id : String?, user2Id: String) : Boolean{
        if(user1Id.equals(user2Id)){
            Toast.makeText(this,"You cannot add your invite code", Toast.LENGTH_LONG).show()
            return true
        }else{
            return false
        }
        //return if (user1Id.equals(user2Id)) true else false
    }
    /**
     * add new connection between two account, @param is founded user who had generated invite code
     * @param userUniqueKeyModel
     */
    private fun addConnectedUserToDatabase(userUniqueKeyModel : UserUniqueKey){
        Log.i(TAG, "addConnectedUserToDatabase: add user data following and followers to database")
        var currentUserId = FirebaseAuth.getInstance().currentUser!!.uid
        var currentUserEmail = FirebaseAuth.getInstance().currentUser!!.email
        var deviceTokenId = FirebaseInstanceId.getInstance().token
        var currentUser = User(currentUserId, currentUserEmail!!,deviceTokenId!!)
        var followedUser = User(userUniqueKeyModel.user_id!!, userUniqueKeyModel!!.user_email!!,userUniqueKeyModel.device_token!!)
        FirebaseDatabase.getInstance().getReference()
                .child("following")
                .child(currentUser.user_id)
                .child(followedUser!!.user_id)
                .child("user")
                .setValue(followedUser);

        FirebaseDatabase.getInstance().getReference()
                .child("followers")
                .child(userUniqueKeyModel!!.user_id)
                .child(currentUser.user_id)
                .child("user")
                .setValue(currentUser)
    }
    override fun onBackPressed() {
        goToPreviousActivity()
        super.onBackPressed()
    }
    private fun goToPreviousActivity(){
        var  intent =  Intent(this@EnterInviteActivity, ListOnline::class.java);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish()
    }
}
