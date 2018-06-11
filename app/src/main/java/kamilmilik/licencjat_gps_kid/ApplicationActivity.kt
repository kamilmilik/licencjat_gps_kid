package kamilmilik.licencjat_gps_kid

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.NavUtils
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DatabaseError
import kamilmilik.licencjat_gps_kid.login.LoginActivity
import kamilmilik.licencjat_gps_kid.utils.Tools


/**
 * Created by kamil on 06.05.2018.
 */
open class ApplicationActivity : AppCompatActivity() {

    private val TAG = ApplicationActivity::class.java.simpleName

    private var toolbar : Toolbar? = null

    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)

        toolbar = Tools.setupToolbar(this, true)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG,"onCreate()")

        logoutDeletedUser()
    }
    fun logoutDeletedUser(){
        var currentUser = FirebaseAuth.getInstance().currentUser
        if(currentUser != null){
            FirebaseDatabase.getInstance().getReference("user_account_settings").orderByKey().equalTo(currentUser!!.uid).
                    addChildEventListener(object : ChildEventListener {
                        override fun onCancelled(databaseError: DatabaseError?) {}
                        override fun onChildAdded(dataSnapshot: DataSnapshot?, previousKey: String?) {}
                        override fun onChildChanged(dataSnapshot: DataSnapshot?, s: String?) {}
                        override fun onChildRemoved(dataSnapshot: DataSnapshot?) {
                            Log.i(TAG,"onChildRemoved()")
                            if(FirebaseDatabase.getInstance() != null && FirebaseAuth.getInstance().currentUser != null){
                                FirebaseAuth.getInstance().signOut()
                                var intent = Intent(this@ApplicationActivity, LoginActivity::class.java);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                startActivity(intent)
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