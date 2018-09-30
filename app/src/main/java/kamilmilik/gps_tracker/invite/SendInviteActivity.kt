package kamilmilik.gps_tracker.invite

import android.os.Bundle
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kamilmilik.gps_tracker.R
import kotlinx.android.synthetic.main.activity_send_invite.*
import com.google.firebase.database.ServerValue
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ValueEventListener
import kamilmilik.gps_tracker.ApplicationActivity
import kamilmilik.gps_tracker.utils.Constants
import kamilmilik.gps_tracker.utils.Tools
import kamilmilik.gps_tracker.models.UserUniqueKey
import kamilmilik.gps_tracker.utils.ObjectsUtils
import kotlinx.android.synthetic.main.progress_bar.*
import java.util.*


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
        addCurrentUserGeneratedKeyToDatabase(Tools.generateRandomKey(Constants.LENGTH_RANDOM_CHARACTERS))
    }

    /**
     * Add generated unique key from current user to firebase database.
     * @param generatedUniqueKey
     */
    private fun addCurrentUserGeneratedKeyToDatabase(generatedUniqueKey: String) {
        progressBarRelative.visibility = View.VISIBLE
        FirebaseAuth.getInstance().currentUser?.let { currentFirebaseUser ->
            val uniqueKeyId = FirebaseDatabase.getInstance().reference.push().key
            ObjectsUtils.safeLetFirebaseUser(currentFirebaseUser) { uid, email, name ->

                val userUniqueKey = UserUniqueKey(uid, generatedUniqueKey)
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
                            addCurrentUserGeneratedKeyToDatabase(Tools.generateRandomKey(Constants.LENGTH_RANDOM_CHARACTERS))
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {}
                })
            }
        }

    }

    private fun addTimeDateToDatabase(uniqueKeyId: String) {
        val ref = FirebaseDatabase.getInstance().reference
        val map = HashMap<String, Any>() as MutableMap<String, Any>
        map.put(Constants.DATABASE_TIME_FIELD, ServerValue.TIMESTAMP)
        ref.child(Constants.DATABASE_USER_KEYS)
                .child(uniqueKeyId)
                .updateChildren(map)
    }
}
