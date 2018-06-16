package kamilmilik.licencjat_gps_kid.invite

import android.os.Bundle
import android.util.Log
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kamilmilik.licencjat_gps_kid.R
import kotlinx.android.synthetic.main.activity_send_invite.*
import com.google.firebase.database.ServerValue
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ValueEventListener
import com.google.firebase.iid.FirebaseInstanceId
import kamilmilik.licencjat_gps_kid.ApplicationActivity
import kamilmilik.licencjat_gps_kid.utils.Constants
import kamilmilik.licencjat_gps_kid.utils.Tools
import kamilmilik.licencjat_gps_kid.models.UserUniqueKey
import kotlinx.android.synthetic.main.progress_bar.*
import java.util.*
import java.util.concurrent.TimeUnit


class SendInviteActivity : ApplicationActivity() {
    private val TAG = SendInviteActivity::class.java.simpleName

    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)
        Tools.setupToolbar(this, true).title = getString(R.string.sendInviteCodeActivity)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_send_invite)

        generatedUniqueCodeAction()
    }

    private fun generatedUniqueCodeAction() {
        addCurrentUserGeneratedKeyToDatabase(Tools.generateRandomKey(8))
        //removeUniqueKeyAfterGivenTimeAfterClickGenerateCode()
    }

    /**
     * add generated unique key from current user to firebase database
     * @param generatedUniqueKey
     */
    private fun addCurrentUserGeneratedKeyToDatabase(generatedUniqueKey: String) {
        progressBarRelative.visibility = View.VISIBLE
        var currentFirebaseUser = FirebaseAuth.getInstance().currentUser!!
        var userId = currentFirebaseUser.uid
        var userEmail = currentFirebaseUser.email
        var uniqueKeyId = FirebaseDatabase.getInstance().reference.push().key
        var deviceTokenId = FirebaseInstanceId.getInstance().token
        var name = currentFirebaseUser.displayName

        var userUniqueKey = UserUniqueKey(userId, userEmail!!, generatedUniqueKey, deviceTokenId!!, name!!)
        val reference = FirebaseDatabase.getInstance().reference
        val query = reference.child(Constants.DATABASE_USER_KEYS)
                .orderByChild(Constants.DATABASE_UNIQUE_KEY_FIELD)
                .equalTo(generatedUniqueKey)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.value == null) {
                    FirebaseDatabase.getInstance().reference
                            .child(Constants.DATABASE_USER_KEYS)
                            .child(uniqueKeyId)
                            .setValue(userUniqueKey)
                    addTimeDateToDatabase(uniqueKeyId)
                    textViewGeneratedCode.text = generatedUniqueKey
                    progressBarRelative.visibility = View.GONE
                } else {
                    addCurrentUserGeneratedKeyToDatabase(Tools.generateRandomKey(8))
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })

    }

    private fun addTimeDateToDatabase(uniqueKeyId: String) {
        val ref = FirebaseDatabase.getInstance().reference
        var map = HashMap<String, Any>() as MutableMap<String, Any>
        map.put(Constants.DATABASE_TIME_FIELD, ServerValue.TIMESTAMP)
        ref.child(Constants.DATABASE_USER_KEYS)
                .child(uniqueKeyId)
                .updateChildren(map)
    }

    private fun removeUniqueKeyAfterGivenTimeAfterClickGenerateCode() {
        val refDb = FirebaseDatabase.getInstance().reference
        val cutoff = (Date().time - TimeUnit.MILLISECONDS.convert(10, TimeUnit.SECONDS)).toDouble()
        val oldItems = refDb.child(Constants.DATABASE_USER_KEYS).orderByChild(Constants.DATABASE_TIME_FIELD).endAt(cutoff)
        oldItems.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (itemSnapshot in snapshot.children) {
                    itemSnapshot.ref.removeValue()
                    Log.i(TAG, "value to delete adter time: " + itemSnapshot.value)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }
}
