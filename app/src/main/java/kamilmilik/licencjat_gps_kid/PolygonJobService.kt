package kamilmilik.licencjat_gps_kid

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import android.os.AsyncTask
import android.util.Log
import com.google.firebase.FirebaseApp
import kamilmilik.licencjat_gps_kid.Helper.Notification


/**
 * Created by kamil on 31.03.2018.
 */
class PolygonJobService : JobService() {
    private val TAG = PolygonJobService::class.java.simpleName
    private var mBackgroundTask: AsyncTask<Object, Void, Object>? = null
    private var notificationMethods: Notification? = null
    var job : JobParameters? = null

    override fun onStartJob(job: JobParameters): Boolean {
        Log.i(TAG,"onStartJob()")
        this.job = job
        FirebaseApp.initializeApp(applicationContext)//I must called this first otherwise foreground/background service is not running since without it get nullPointerException
        notificationMethods = Notification(this@PolygonJobService,this, job)
//        mBackgroundTask = object : AsyncTask<Object, Void, Object>() {
//            override fun doInBackground(vararg params: Object?): Object? {
////                var intent = Intent(applicationContext, PolygonAndLocationService::class.java)
////                applicationContext.startService(intent)
//                notificationMethods!!.notificationAction()
//                return null
//            }
//
////            override fun onPostExecute(result: Object?) {
////                Log.i(TAG,"onPostExecute()")
////                jobFinished(job, false) //it must be called here, since I return true
////                super.onPostExecute(result)
////            }
//        }
//        mBackgroundTask!!.execute()

        notificationMethods!!.notificationAction()

        return false // true if use separate threat liek asynctask
    }

    override fun onStopJob(job: JobParameters): Boolean {
//        if (mBackgroundTask != null) {
//            mBackgroundTask!!.cancel(true);
//        }
        Log.i("TAG", "onStopJob");
        /* true means, we're not done, please reschedule */
        return false;
    }
}