package kamilmilik.licencjat_gps_kid

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import android.os.AsyncTask
import android.os.AsyncTask.execute
import android.os.Build
import android.os.IBinder
import android.util.Log
import kamilmilik.licencjat_gps_kid.Helper.Notification
import kamilmilik.licencjat_gps_kid.Utils.ForegroundOnTaskRemovedActivity
import kamilmilik.licencjat_gps_kid.Utils.PolygonAndLocationService


/**
 * Created by kamil on 31.03.2018.
 */
class MyJobService : JobService() {
    private val TAG = MyJobService::class.java.simpleName
    private var mBackgroundTask: AsyncTask<Object, Void, Object>? = null
    private var notificationMethods: Notification? = null
    var job : JobParameters? = null

    override fun onCreate() {
        Log.i(TAG,"onCreate()")
        super.onCreate()
    }
    override fun onStartJob(job: JobParameters): Boolean {
        Log.i(TAG,"onStartJob()")
        this.job = job
        notificationMethods = Notification(this@MyJobService,this, job)
        mBackgroundTask = object : AsyncTask<Object, Void, Object>() {
            override fun doInBackground(vararg params: Object?): Object? {
//                var intent = Intent(applicationContext, PolygonAndLocationService::class.java)
//                applicationContext.startService(intent)
                notificationMethods!!.notificationAction()
                return null
            }

//            override fun onPostExecute(result: Object?) {
//                Log.i(TAG,"onPostExecute()")
//                jobFinished(job, false) //it must be called here, since I return true
//                super.onPostExecute(result)
//            }
        }
        mBackgroundTask!!.execute()
        return true // true if use separate threat liek asynctask
    }

    override fun onStopJob(job: JobParameters): Boolean {
        if (mBackgroundTask != null) {
            mBackgroundTask!!.cancel(true);
        }
        Log.i("TAG", "onStopJob");
        /* true means, we're not done, please reschedule */
        return true;
    }
}