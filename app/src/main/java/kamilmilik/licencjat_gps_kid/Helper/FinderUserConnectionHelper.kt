package kamilmilik.licencjat_gps_kid.Helper

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.RelativeLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import kamilmilik.licencjat_gps_kid.Helper.LocationOperation.LocationFirebaseHelper
import kamilmilik.licencjat_gps_kid.Utils.OnItemClickListener
import kamilmilik.licencjat_gps_kid.Utils.RecyclerViewAdapter
import kamilmilik.licencjat_gps_kid.models.User
import kamilmilik.licencjat_gps_kid.models.UserMarkerInformationModel
import java.util.HashMap
import java.util.concurrent.atomic.AtomicInteger

/**
 * Created by kamil on 24.02.2018.
 */
class FinderUserConnectionHelper(var context: Context,
                                 var listener: OnItemClickListener,
                                 var valueSet: HashSet<UserMarkerInformationModel>,
                                 var adapter: RecyclerViewAdapter,
                                 var recyclerView: RecyclerView,
                                 var locationFirebaseHelper: LocationFirebaseHelper,
                                 var progressBar: ProgressDialog) : OnMarkerAddedCallback {

    private var isMyLocationSet : Boolean = false

    private var isUserConnectionSet : Boolean = false

    override fun myLocationMarkerAddedListener(isMyLocationSet: Boolean) {
        this.isMyLocationSet = isMyLocationSet
    }

    override fun userConnectionMarkerAddedListener(isUserConnectionSet: Boolean) {
        this.isUserConnectionSet = isUserConnectionSet
    }

    override fun onMarkerAddedListener() {
        if(isMyLocationSet && isUserConnectionSet){
            //run this after myLocation marker is set nad userConnection marker is set
            updateRecyclerView()
           // Log.i(TAG,"onMarkerAddedListener visibility progress bar GONE")
            workCounter!!.incrementAndGet()
            Log.i(TAG,"workCounter in onMarkerAddedListener "  + workCounter!!.get())
            if(workCounter!!.compareAndSet(3, 0)){
                Log.i(TAG,"onMarkerAddedListener onDataChange() visibility progress bar GONE")
                //progressBar.visibility = View.GONE
                progressBar.dismiss()
            }
        }
    }

    override fun updateChangeUserNameInRecycler(userInformation : UserMarkerInformationModel){
        val iterator = valueSet.iterator()
        while (iterator.hasNext()) {
            val value = iterator.next()
            if( value.email == userInformation.email ) run {
                iterator.remove()
            }
        }
        valueSet.add(userInformation)
    }
    //TODO usunac tą metode i tak zrefaktoryzować kod cały aby nie musieć tak robić
    fun getMarkerAddedListener() : OnMarkerAddedCallback {
        return this
    }

    private val TAG = FinderUserConnectionHelper::class.java.simpleName

    fun listenerForConnectionsUserChangeInFirebaseAndUpdateRecyclerView() {
        findFollowersConnectionAndUpdateRecyclerView()
    }

    private fun findFollowersConnectionAndUpdateRecyclerView() {
        Log.i(TAG, "findFollowersConnectionAndUpdateRecyclerView, current user id : " + FirebaseAuth.getInstance().currentUser!!.uid)
        var currentUser = FirebaseAuth.getInstance().currentUser
        val reference = FirebaseDatabase.getInstance().reference

        findFollowersUser(reference, currentUser!!)
        findFollowingUser(reference, currentUser!!)
    }

    /**
     * listener for followers in database,  this method run listener for locationOfUserWhoChangeIt change in database and add to recycler view followers user
     * @param reference
     * @param currentUser
     */
    private fun findFollowersUser(reference: DatabaseReference, currentUser: FirebaseUser) {
        //TODO przerobic moze cala metode zamiast valueeventlistener to na ChildEventListener przyklad jak uzywac w applicationactivity
        reference.child("followers").addChildEventListener(object : ChildEventListener {
                    override fun onCancelled(p0: DatabaseError?) {
                        Log.i(TAG,"onCancelled() followers")
                    }
                    override fun onChildMoved(dataSnapshot: DataSnapshot?, p1: String?) {
                        Log.i(TAG,"onChildMoved() followers")
                    }
                    override fun onChildChanged(dataSnapshot: DataSnapshot?, p1: String?) {
                        Log.i(TAG,"onChildChanged() followers")
                    }
                    override fun onChildAdded(dataSnapshot: DataSnapshot?, previousKey: String?) {
                        Log.i(TAG,"onChildAdded() followers")
                    }

                    override fun onChildRemoved(dataSnapshot: DataSnapshot?) {
                        Log.i(TAG,"onChildRemoved() followers")
                        (context as Activity).recreate()
                    }
                })
        reference.child("following").addChildEventListener(object : ChildEventListener {
            override fun onCancelled(p0: DatabaseError?) {
                Log.i(TAG,"onCancelled() following")
            }
            override fun onChildMoved(dataSnapshot: DataSnapshot?, p1: String?) {
                Log.i(TAG,"onChildMoved() following")
            }
            override fun onChildChanged(dataSnapshot: DataSnapshot?, p1: String?) {
                Log.i(TAG,"onChildChanged() following")
            }
            override fun onChildAdded(dataSnapshot: DataSnapshot?, previousKey: String?) {
                Log.i(TAG,"onChildAdded() following")
            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot?) {
                Log.i(TAG,"onChildRemoved() following")
                (context as Activity).recreate()
            }
        })
        val query = reference.child("followers")
                .orderByKey()
                .equalTo(currentUser!!.uid)
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Log.i(TAG,"onDataChange() super teraz recreate view w following")
                for (singleSnapshot in dataSnapshot.children) {
                    for (childSingleSnapshot in singleSnapshot.children) {
                        var userFollowers = childSingleSnapshot.child("user").getValue(User::class.java)
                        Log.i(TAG, "value followers: " + userFollowers!!.user_id + " " + userFollowers.email)
                        var userInformation = UserMarkerInformationModel(userFollowers!!.email, userFollowers.user_name!!)
//                        for(value in valueSet){
//                            if( value.email == userInformation.email ) run {
//                                valueSet.remove(value)
//                            }
//                        }
                        val iterator = valueSet.iterator()
                        while (iterator.hasNext()) {
                            val value = iterator.next()
                            if( value.email == userInformation.email ) run {
                                iterator.remove()
                            }
                        }
                        valueSet.add(userInformation)
                        locationFirebaseHelper!!.listenerForLocationsChangeInFirebase(userFollowers.user_id!!, this@FinderUserConnectionHelper, progressBar)
                    }
                }
                workCounter!!.incrementAndGet()
                if (dataSnapshot.value == null) {//nothing found
                    workCounterForUserWhoNotHaveConnection!!.incrementAndGet()
                    Log.i(TAG, "nothing found in onDataChange in followers")
                    if(workCounterForUserWhoNotHaveConnection!!.compareAndSet(2, 0)){
                        Log.i(TAG,"nothing found in onDataChange in followers onDataChange() visibility progress bar GONE")
                        //progressBar.visibility = View.GONE
                        progressBar.dismiss()
                    }

                } else {
                    for (user in valueSet) {
                        Log.i(TAG, "findFollowersUser user complete : " + user)
                    }
                    //updateRecyclerView()
                }
                Log.i(TAG,"workCounter in findFollowersUser "  + workCounter!!.get())
                if(workCounter!!.compareAndSet(3, 0)){
                    Log.i(TAG,"findFollowersUser onDataChange() visibility progress bar GONE")
                    //progressBar.visibility = View.GONE
                    progressBar.dismiss()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.i(TAG, "onCancelled: " + databaseError.message)
            }
        })

    }

    /**
     * listener for following in database,  this method run listener for locationOfUserWhoChangeIt change in database and add to recycler view following user
     * @param reference
     * @param currentUser
     */
    //TODO pomyslec czy zostawiamy te atomicIntegery czy uzywamy z callbackow(OnMarkerAddedCallback) bo teraz uzywam tego i tego
    private var workCounter: AtomicInteger? = AtomicInteger(0)

    private var workCounterForUserWhoNotHaveConnection: AtomicInteger? = AtomicInteger(0)

    private fun findFollowingUser(reference: DatabaseReference, currentUser: FirebaseUser) {
        val query = reference.child("following")
                .orderByKey()
                .equalTo(currentUser!!.uid)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (singleSnapshot in dataSnapshot.children) {
                    for (childSingleSnapshot in singleSnapshot.children) {
                        var userFollowing = childSingleSnapshot.child("user").getValue(User::class.java)
                        Log.i(TAG, "value following: " + userFollowing!!.user_id + " " + userFollowing!!.email)
                        //TODO jak sie zmienia user name to w dtugim telefonie ktory nasluchuje tych zmian mamy dwa razy w recycler view stare imie i nowe
                        var userInformation = UserMarkerInformationModel(userFollowing!!.email, userFollowing.user_name!!)
//                        for (value in valueSet) {
//                            if (value.email == userInformation.email) run {
//                                valueSet.remove(value)
//                            }
//                        }
                        val iterator = valueSet.iterator()
                        while (iterator.hasNext()) {
                            val value = iterator.next()
                            if( value.email == userInformation.email ) run {
                                iterator.remove()
                            }
                        }
                        valueSet.add(userInformation)
                        locationFirebaseHelper!!.listenerForLocationsChangeInFirebase(userFollowing.user_id!!, this@FinderUserConnectionHelper, progressBar)
                    }
                }
                workCounter!!.incrementAndGet()
                if (dataSnapshot.value == null) {//nothing found
                    workCounterForUserWhoNotHaveConnection!!.incrementAndGet()
                    Log.i(TAG, "nothing found in onDataChange in following")
                    if(workCounterForUserWhoNotHaveConnection!!.compareAndSet(2, 0)){
                        Log.i(TAG,"nothing found in onDataChange in following onDataChange() visibility progress bar GONE")
                        //progressBar.visibility = View.GONE
                        progressBar.dismiss()
                    }
                } else {
                    for (user in valueSet) {
                        Log.i(TAG,  " findFollowingUser user complete : " + user)
                    }
                    //updateRecyclerView()
                }
                Log.i(TAG,"workCounter in findfollowinguser "  + workCounter!!.get())
                if (workCounter!!.compareAndSet(3, 0)) {
                        Log.i(TAG,"findFollowingUser onDataChange() visibility progress bar GONE")
                        //progressBar.visibility = View.GONE
                        progressBar.dismiss()
                    }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Log.i(TAG, "onCancelled: " + databaseError.message)
            }
        })
    }

    private fun updateRecyclerView() {
        Log.i(TAG,"updateRecyclerView()")
        var currentUser = FirebaseAuth.getInstance().currentUser
//        valueSet.add(currentUser!!.email + " (ja)")
        valueSet.add(UserMarkerInformationModel(currentUser!!.email!!, currentUser.displayName!!))
        var valueList = ArrayList(valueSet)
        for (user in valueList) {
            Log.i(TAG, "user complete Show set : " + user)
        }
        adapter = RecyclerViewAdapter(context, valueList)
        recyclerView.adapter = adapter
        adapter.setClickListener(listener)

        adapter.notifyDataSetChanged()
    }

}