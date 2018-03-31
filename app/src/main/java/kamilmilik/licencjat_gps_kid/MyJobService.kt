package kamilmilik.licencjat_gps_kid

import android.content.Intent
import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import android.os.AsyncTask
import android.os.AsyncTask.execute
import android.util.Log
import kamilmilik.licencjat_gps_kid.Utils.PolygonAndLocationService


/**
 * Created by kamil on 31.03.2018.
 */
class MyJobService : JobService() {
    private val TAG = MyJobService::class.java.simpleName
    private var mBackgroundTask: AsyncTask<Object, Void, Object>? = null
    override fun onStartJob(job: JobParameters): Boolean {
        Log.i(TAG,"onStartJob()")
        mBackgroundTask = object : AsyncTask<Object, Void, Object>() {
            override fun doInBackground(vararg params: Object?): Object? {
                var intent = Intent(applicationContext, PolygonAndLocationService::class.java)
                applicationContext.startService(intent)
                return null
            }

            override fun onPostExecute(result: Object?) {
                jobFinished(job, false) //it must be called here, since I return true
                super.onPostExecute(result)
            }
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