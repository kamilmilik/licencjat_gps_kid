package kamilmilik.licencjat_gps_kid.Utils

import android.app.Application
import android.util.Log
import kamilmilik.licencjat_gps_kid.DemoJobCreator
import com.evernote.android.job.JobManager
import kamilmilik.licencjat_gps_kid.DemoSyncJob


/**
 * Created by kamil on 30.03.2018.
 */
class App : Application(){
    override fun onCreate() {
        Log.i("App", "App onCreate")
        super.onCreate()
        JobManager.create(this).addJobCreator(DemoJobCreator())
        DemoSyncJob.ScheduleJob.scheduleAdvancedJob()

    }
}