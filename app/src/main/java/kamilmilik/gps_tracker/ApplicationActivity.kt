package kamilmilik.gps_tracker

import android.os.Bundle
import android.support.v4.app.NavUtils
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DatabaseError
import kamilmilik.gps_tracker.login.LoginActivity
import kamilmilik.gps_tracker.utils.Constants
import kamilmilik.gps_tracker.utils.Tools
import io.fabric.sdk.android.Fabric
import com.crashlytics.android.Crashlytics


/**
 * Created by kamil on 06.05.2018.
 */
open class ApplicationActivity : AppCompatActivity() {

    private val TAG = ApplicationActivity::class.java.simpleName

    private var toolbar: Toolbar? = null

    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)

        toolbar = Tools.setupToolbar(this, true)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initCrashReporting()
        deleteUserFromFirebaseAuth()
    }

    private fun initCrashReporting() {
        val fabric = Fabric.Builder(this)
                .kits(Crashlytics())
                .debuggable(true)
                .build()
        Fabric.with(fabric)
    }

    private fun deleteUserFromFirebaseAuth() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            FirebaseDatabase.getInstance().getReference(Constants.DATABASE_USER_ACCOUNT_SETTINGS).child(currentUser.uid).addChildEventListener(object : ChildEventListener {
                override fun onCancelled(databaseError: DatabaseError?) {}
                override fun onChildAdded(dataSnapshot: DataSnapshot?, previousKey: String?) {}
                override fun onChildChanged(dataSnapshot: DataSnapshot?, s: String?) {}
                override fun onChildRemoved(dataSnapshot: DataSnapshot?) {
                    if (FirebaseDatabase.getInstance() != null && FirebaseAuth.getInstance().currentUser != null) {
                        currentUser.delete()
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        Toast.makeText(this@ApplicationActivity, getString(R.string.deletedAccountInformation), Toast.LENGTH_LONG).show()
                                        Tools.startNewActivityWithoutPrevious(this@ApplicationActivity, LoginActivity::class.java)
                                    }
                                }
                    }
                }

                override fun onChildMoved(dataSnapshot: DataSnapshot?, s: String?) {}
            })
        }
    }

    override fun onBackPressed() {
        NavUtils.navigateUpFromSameTask(this)
        super.onBackPressed()
    }
}