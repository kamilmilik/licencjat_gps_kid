package kamilmilik.licencjat_gps_kid

import android.util.Log
import com.firebase.jobdispatcher.JobParameters
import com.firebase.jobdispatcher.JobService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

/**
 * Created by kamil on 28.04.2018.
 */
class PresenceService : JobService() {
    private val TAG = PresenceService::class.java.simpleName
    val auth = FirebaseAuth.getInstance()
    val database = FirebaseDatabase.getInstance()

    override fun onStartJob(job: JobParameters): Boolean {
        Log.i(TAG, "Starting Service")

        database.getReference(".info/connected")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val connected = snapshot.getValue(Boolean::class.java)!!

                        Log.i(TAG, "Status is connected: $connected")

                        val statusRef = database.getReference("presence").child(auth.currentUser?.uid)

                        statusRef.onDisconnect().setValue(false, object: DatabaseReference.CompletionListener{
                            override fun onComplete(error: DatabaseError?, p1: DatabaseReference?) {
                                if(error == null){
                                    Log.i(TAG, "onComplete1")
                                }
                            }

                        })
                        statusRef.setValue(connected, object: DatabaseReference.CompletionListener{
                            override fun onComplete(error: DatabaseError?, p1: DatabaseReference?) {
                                if(error == null){
                                    Log.i(TAG, "onComplete2")
                                }
                            }

                        })
                    }

                    override fun onCancelled(p0: DatabaseError?) {
                        Log.e("Debug", p0?.toException()?.message)
                    }
                })
        return false
    }

    override fun onStopJob(job: JobParameters): Boolean {
        return false
    }
}