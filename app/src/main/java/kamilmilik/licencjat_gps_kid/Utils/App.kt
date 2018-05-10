//package kamilmilik.licencjat_gps_kid.Utils
//
//import android.app.ActivityManager
//import android.app.Application
//import android.content.Context
//import android.content.Intent
//import android.util.Log
//import com.evernote.android.job.JobManager
//import com.google.firebase.auth.FirebaseAuth
//import kamilmilik.licencjat_gps_kid.PolygonJobService
//import android.support.design.widget.CoordinatorLayout.Behavior.setTag
//import com.firebase.jobdispatcher.*
//import com.google.firebase.FirebaseApp
//import com.google.firebase.database.FirebaseDatabase
//import kamilmilik.licencjat_gps_kid.TestLocationForeground
//
//
///**
// * Created by kamil on 30.03.2018.
// */
///**
// * this class is require for Evernote
// */
//class App : Application(){
//    private val TAG = App::class.java.simpleName
//
////    private lateinit var dispatcher :  FirebaseJobDispatcher
////    private lateinit var dispatcher2 : FirebaseJobDispatcher
//    override fun onCreate() {
//        Log.i("App", "App onCreate")
//        super.onCreate()
//
//        FirebaseApp.initializeApp(applicationContext)//I must called this first otherwise foreground/background service is not running since without it get nullPointerException
//        //FirebaseDatabase.getInstance().setPersistenceEnabled(true)
//        //this is needed to some device to work correctly notifications
////        dispatcher = FirebaseJobDispatcher(GooglePlayDriver(applicationContext))
////        dispatcher2 = FirebaseJobDispatcher(GooglePlayDriver(applicationContext))
//        var mAuthListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
//            val user = firebaseAuth.currentUser
//            Log.i(TAG, "user " + user)
//            if (user != null) {
//                Log.i(TAG,"user log in")
//
//                var am: ActivityManager = this.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager;
//                am.killBackgroundProcesses("kamilmilik.licencjat_gps_kid:separate");
//
//
////                val myJob = dispatcher.newJobBuilder()
////                        .setService(PolygonJobService::class.java) // the JobService that will be called
////                        .setTag("my-unique-tag")        // uniquely identifies the job
////                        .setLifetime(Lifetime.UNTIL_NEXT_BOOT)
////                        // start between windowStart in sec and windowEnd in seconds from now
////                        .setTrigger(Trigger.executionWindow(0, 60))
////                        .setRecurring(true)//to reschedule job
////                        .setRetryStrategy(RetryStrategy.DEFAULT_LINEAR)
////                        .setReplaceCurrent(true)
////                        .build()
////
////                dispatcher.mustSchedule(myJob)
//
//                try {
////                    val myLocationJob = dispatcher2.newJobBuilder()
////                            .setService(LocationJobService::class.java) // the JobService that will be called
////                            .setTag("my-location-job")        // uniquely identifies the job
////                            .setLifetime(Lifetime.FOREVER)
////                            // start between windowStart in sec and windowEnd in seconds from now
////                            .setTrigger(Trigger.executionWindow(60, 3*60+1))
////                            .setRecurring(true)//to reschedule job
////                            .setRetryStrategy(dispatcher2.newRetryStrategy(RetryStrategy.RETRY_POLICY_EXPONENTIAL,30,3000))
////                            .setReplaceCurrent(true)
////                            .build()
////
////                    dispatcher2.mustSchedule(myLocationJob)
////                    Log.i(TAG,"onCreate() started dispatcher " + dispatcher2.toString())
//                } catch (scheduleFailedException: FirebaseJobDispatcher.ScheduleFailedException) {
//                    Log.i(TAG, "scheduleFailedException " + scheduleFailedException)
//                }
//
//            }else{
////                dispatcher2.cancelAll()
////                dispatcher.cancelAll()
//                Log.i(TAG,"user not log in")
//            }
//        }
//        FirebaseAuth.getInstance().addAuthStateListener(mAuthListener)
//
//    }
//}