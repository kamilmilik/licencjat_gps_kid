package kamilmilik.licencjat_gps_kid.Utils

import android.app.Application
import android.util.Log
import kamilmilik.licencjat_gps_kid.DemoJobCreator
import com.evernote.android.job.JobManager
import kamilmilik.licencjat_gps_kid.DemoSyncJob
import com.google.firebase.auth.FirebaseAuth
import kamilmilik.licencjat_gps_kid.MyJobService
import android.support.design.widget.CoordinatorLayout.Behavior.setTag
import com.firebase.jobdispatcher.*
import com.google.firebase.FirebaseApp


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

                val dispatcher = FirebaseJobDispatcher(GooglePlayDriver(applicationContext))
                val myJob = dispatcher.newJobBuilder()
                        .setService(MyJobService::class.java) // the JobService that will be called
                        .setTag("my-unique-tag")        // uniquely identifies the job
                        .setLifetime(Lifetime.FOREVER)
                        // start between 0 and 60 seconds from now
                        .setTrigger(Trigger.executionWindow(0, 60))
                        // don't overwrite an existing job with the same tag
                        .setRecurring(true)
                        .setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL)
                        .setReplaceCurrent(true)
                        .build()

                dispatcher.mustSchedule(myJob)

            }else{
                Log.i(TAG,"user not log in")
            }
        }
        FirebaseApp.initializeApp(applicationContext)
        FirebaseAuth.getInstance().addAuthStateListener(mAuthListener)

    }
}