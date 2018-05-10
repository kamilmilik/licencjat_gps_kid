package kamilmilik.licencjat_gps_kid

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DatabaseError
import kamilmilik.licencjat_gps_kid.Helper.UserOperations.OnlineUserHelper
import kamilmilik.licencjat_gps_kid.Login.MainActivity
import kamilmilik.licencjat_gps_kid.Utils.Tools
import kamilmilik.licencjat_gps_kid.models.User


/**
 * Created by kamil on 06.05.2018.
 */
open class ApplicationActivity : AppCompatActivity() {

    private val TAG = ApplicationActivity::class.java.simpleName

    private var toolbar : Toolbar? = null

    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)

        toolbar = Tools.setupToolbar(this)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG,"onCreate()")

        logoutDeletedUser()

//        toolbar = Tools.setupToolbar(this)
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
                                var intent = Intent(this@ApplicationActivity, MainActivity::class.java);
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
}


//---------------------
//Przyklad uzycia ChildEventListener onchildadded dziala ok
//            val query = FirebaseDatabase.getInstance().reference.child("followers")
//                    .orderByKey()
//                    .equalTo(currentUser!!.uid)
//            query.addChildEventListener(object : ChildEventListener {
//                override fun onCancelled(p0: DatabaseError?) {
//
//                }
//
//                override fun onChildMoved(dataSnapshot: DataSnapshot?, p1: String?) {
//                    Log.i(TAG,"onChildMoved()")
//                }
//
//                override fun onChildChanged(dataSnapshot: DataSnapshot?, p1: String?) {
//                    Log.i(TAG,"onChildChanged() " + dataSnapshot)
//                }
//
//                override fun onChildAdded(dataSnapshot: DataSnapshot?, previousKey: String?) {
//                    Log.i(TAG, "onChildAdded() " + dataSnapshot)
//                    for (singleSnapshot in dataSnapshot!!.children) {
//                        Log.i(TAG,"onChildAdded() children " + singleSnapshot)
//                        for (childSingleSnapshot in singleSnapshot.children) {
//                            var userFollowers = childSingleSnapshot.getValue(User::class.java)
//                            Log.i(TAG, "value followers: " + userFollowers!!.user_id + " " + userFollowers.email)
//
//                        }
//                    }
//
//                    var userFollowers = dataSnapshot!!.child("user").getValue(User::class.java)
//                    Log.i(TAG, "value followers: " + userFollowers!!.user_id + " " + userFollowers.email)
//                }
//
//                override fun onChildRemoved(dataSnapshot: DataSnapshot?) {
//
//                }
//            })
//
//            val query2 = FirebaseDatabase.getInstance().reference.child("following")
//                    .orderByKey()
//                    .equalTo(currentUser!!.uid)
//            query2.addChildEventListener(object : ChildEventListener {
//                override fun onCancelled(p0: DatabaseError?) {
//
//                }
//
//                override fun onChildMoved(dataSnapshot: DataSnapshot?, p1: String?) {
//                    Log.i(TAG,"onChildMoved()")
//                }
//
//                override fun onChildChanged(dataSnapshot: DataSnapshot?, p1: String?) {
//                    Log.i(TAG,"onChildChanged() " + dataSnapshot)
//                }
//
//                override fun onChildAdded(dataSnapshot: DataSnapshot?, previousKey: String?) {
//                    Log.i(TAG,"onChildAdded() " + dataSnapshot)
//                }
//
//                override fun onChildRemoved(dataSnapshot: DataSnapshot?) {
//
//                }
//            })