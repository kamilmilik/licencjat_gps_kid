package kamilmilik.licencjat_gps_kid.Invite

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kamilmilik.licencjat_gps_kid.R
import kamilmilik.licencjat_gps_kid.Utils.RandomIdGenerator
import kotlinx.android.synthetic.main.activity_send_invite.*
import com.google.firebase.database.ServerValue
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ValueEventListener
import kamilmilik.licencjat_gps_kid.models.UserUniqueKey
import java.util.*
import java.util.concurrent.TimeUnit


class SendInviteActivity : AppCompatActivity() {
    val TAG : String = "SendInviteActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_send_invite)

        setupToolbar()

        generatedUniqueCodeAction()
    }
    private fun setupToolbar(){
        toolbarSendInvite.setTitle("Generate Invite Code")
        setSupportActionBar(toolbarSendInvite)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true);
        supportActionBar!!.setDisplayShowHomeEnabled(true);
    }

    private fun setGenerateCode() : String{
        var generatedUniqueKey = RandomIdGenerator.getBase36(8)
        textViewGeneratedCode.setText(generatedUniqueKey)
        return generatedUniqueKey
    }
    /**
     * add generated unique key from current user to firebase database
     * @param generatedUniqueKey
     */
    private fun addCurrentUserGeneratedKeyToDatabase(generatedUniqueKey : String){
        Log.i(TAG, "addGeneratedKeyToCurrentUserToDatabase: add user data following and followers to database")
        var userId = FirebaseAuth.getInstance().getCurrentUser()!!.uid
        var userEmail = FirebaseAuth.getInstance().getCurrentUser()!!.email
        var uniqueKeyId = FirebaseDatabase.getInstance().reference.push().key
        var userUniqueKey  = UserUniqueKey(userId, userEmail!!,generatedUniqueKey)
        FirebaseDatabase.getInstance().reference
                .child("user_keys")
                .child(uniqueKeyId)
                .setValue(userUniqueKey)
        addTimeDateToDatabase(uniqueKeyId)

    }
    private fun addTimeDateToDatabase(uniqueKeyId : String){
        var userId = FirebaseAuth.getInstance().getCurrentUser()!!.uid
        val ref = FirebaseDatabase.getInstance().reference
        var map   = HashMap<String,Any>() as MutableMap<String,Any>
        map.put("time", ServerValue.TIMESTAMP)
        ref.child("user_keys")
                .child(uniqueKeyId)
                .updateChildren(map)


    }
    private fun generatedUniqueCodeAction(){
        var uniqueKey = setGenerateCode()
        addCurrentUserGeneratedKeyToDatabase(uniqueKey)
        //removeUniqueKeyAfterGivenTimeAfterClickGenerateCode()
    }
    private fun removeUniqueKeyAfterGivenTimeAfterClickGenerateCode(){
        val refDb = FirebaseDatabase.getInstance().reference
        val cutoff = (Date().getTime() - TimeUnit.MILLISECONDS.convert(10, TimeUnit.SECONDS)).toDouble()
        Log.i(TAG,"cutOff: " + cutoff)
        val oldItems = refDb.child("user_keys").orderByChild("time").endAt(cutoff)
        oldItems.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (itemSnapshot in snapshot.children) {
                    itemSnapshot.ref.removeValue()
                    Log.i(TAG,"value to delete adter time: " + itemSnapshot.value)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                throw databaseError.toException()
            }
        })
    }
}
