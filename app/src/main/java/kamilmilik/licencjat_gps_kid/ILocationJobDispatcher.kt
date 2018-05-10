package kamilmilik.licencjat_gps_kid

import android.content.Context
import com.firebase.jobdispatcher.JobParameters

/**
 * Created by kamil on 15.04.2018.
 */
interface ILocationJobDispatcher{
    fun getContext() : Context
    fun finishJob()
}