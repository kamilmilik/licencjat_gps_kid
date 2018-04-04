//package kamilmilik.licencjat_gps_kid
//
//import com.evernote.android.job.Job
//import com.evernote.android.job.JobCreator
//import android.R.attr.tag
//import android.util.Log
//
//
///**
// * Created by kamil on 30.03.2018.
// */
//class DemoJobCreator : JobCreator{
//    override fun create(tag: String): Job? {
//        Log.i("DemoJobCreator","create() "  + tag)
//        when (tag) {
//            DemoSyncJob.GET_TAG.TAG -> return DemoSyncJob()
//            else -> return null
//        }
//    }
//
//}