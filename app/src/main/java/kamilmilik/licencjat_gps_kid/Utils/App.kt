package kamilmilik.licencjat_gps_kid.Utils

import android.app.Application
import android.util.Log
import com.evernote.android.job.JobManager
import com.google.firebase.auth.FirebaseAuth
import kamilmilik.licencjat_gps_kid.MyJobService
import android.support.design.widget.CoordinatorLayout.Behavior.setTag
import com.firebase.jobdispatcher.*
import com.google.firebase.FirebaseApp
import com.google.firebase.database.FirebaseDatabase


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
//                JobManager.create(this).addJobCreator(DemoJobCreator())
//                DemoSyncJob.ScheduleJob.scheduleAdvancedJob()

                val dispatcher = FirebaseJobDispatcher(GooglePlayDriver(applicationContext))
                val myJob = dispatcher.newJobBuilder()
                        .setService(MyJobService::class.java) // the JobService that will be called
                        .setTag("my-unique-tag")        // uniquely identifies the job
                        .setLifetime(Lifetime.UNTIL_NEXT_BOOT)
                        // start between windowStart in sec and windowEnd in seconds from now
                        .setTrigger(Trigger.executionWindow(0, 60))
                        .setRecurring(true)//to reschedule job
                        .setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL)
                        .setReplaceCurrent(true)
                        .build()

                dispatcher.mustSchedule(myJob)

                try {
                    val dispatcher2 = FirebaseJobDispatcher(GooglePlayDriver(applicationContext))
                    val myLocationJob = dispatcher2.newJobBuilder()
                            .setService(LocationJobService::class.java) // the JobService that will be called
                            .setTag("my-location-job")        // uniquely identifies the job
                            .setLifetime(Lifetime.UNTIL_NEXT_BOOT)
                            // start between windowStart in sec and windowEnd in seconds from now
                            .setTrigger(Trigger.executionWindow(0, 60))
                            .setRecurring(true)//to reschedule job
                            .setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL)
                            .setReplaceCurrent(true)
                            .build()

                    dispatcher2.mustSchedule(myLocationJob)
                    Log.i(TAG,"onCreate() started dispatcher " + dispatcher2.toString())

                } catch (scheduleFailedException: FirebaseJobDispatcher.ScheduleFailedException) {
                    Log.i(TAG, "scheduleFailedException " + scheduleFailedException)
                }

            }else{
                Log.i(TAG,"user not log in")
            }
        }
        FirebaseApp.initializeApp(applicationContext)//I must called this first otherwise foreground/background service is not running since without it get nullPointerException
        //FirebaseDatabase.getInstance().setPersistenceEnabled(true)
        FirebaseAuth.getInstance().addAuthStateListener(mAuthListener)

    }
}