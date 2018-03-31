package kamilmilik.licencjat_gps_kid.Utils

import android.app.Application
import android.util.Log
import kamilmilik.licencjat_gps_kid.DemoJobCreator
import com.evernote.android.job.JobManager
import kamilmilik.licencjat_gps_kid.DemoSyncJob
import com.google.firebase.auth.FirebaseAuth


/**
 * Created by kamil on 30.03.2018.
 */
class App : Application(){
    private val TAG = App::class.java.simpleName
    override fun onCreate() {
        Log.i("App", "App onCreate")
        super.onCreate()
        //this is needed to some device to work correctly notifications
        var mAuthListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                Log.i(TAG,"user log in")
                JobManager.create(this).addJobCreator(DemoJobCreator())
                DemoSyncJob.ScheduleJob.scheduleAdvancedJob()
            }else{
                Log.i(TAG,"user not log in")
            }
        }
        FirebaseAuth.getInstance().addAuthStateListener(mAuthListener)

    }
}