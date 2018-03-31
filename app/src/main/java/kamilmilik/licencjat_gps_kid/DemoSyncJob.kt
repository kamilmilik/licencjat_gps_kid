package kamilmilik.licencjat_gps_kid

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.util.Log
import com.evernote.android.job.Job
import com.evernote.android.job.JobManager
import com.evernote.android.job.JobRequest
import javax.xml.datatype.DatatypeConstants.MINUTES
import com.evernote.android.job.util.support.PersistableBundleCompat
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.Marker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kamilmilik.licencjat_gps_kid.Helper.Notification
import kamilmilik.licencjat_gps_kid.Utils.ForegroundOnTaskRemovedActivity
import kamilmilik.licencjat_gps_kid.Utils.PolygonAndLocationService
import kamilmilik.licencjat_gps_kid.models.TrackingModel
import java.util.concurrent.TimeUnit


/**
 * Created by kamil on 30.03.2018.
 */
class DemoSyncJob : Job(), GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener{
    private val TAG = DemoSyncJob::class.java.simpleName
    object GET_TAG{
        var TAG = "job_demo_tag"
     }

    override fun onRunJob(params: Params): Result {
        var intent = Intent(context, PolygonAndLocationService::class.java)
        context.startService(intent)
        return Result.SUCCESS
    }
    object ScheduleJob{

         fun scheduleAdvancedJob() {


            val jobId = JobRequest.Builder(GET_TAG.TAG)
//                    .setRequiresCharging(true)
//                    .setRequiresDeviceIdle(false)
//                    .setRequiredNetworkType(JobRequest.NetworkType.CONNECTED)
//                    .setRequirementsEnforced(true)
//                    .setUpdateCurrent(true)
                    .setPeriodic(TimeUnit.MINUTES.toMillis(15))
//                    .startNow()
                    .build()
                    .schedule()
        }

        fun runJobImmediately() {
            val jobId = JobRequest.Builder(DemoSyncJob.GET_TAG.TAG)
                    .startNow()
                    .build()
                    .schedule()
        }
    }


    private var notificationMethods: Notification? = null

    var mLocationRequest: LocationRequest? = null
    var mGoogleApiClient: GoogleApiClient? = null
    var mLastLocation: Location? = null
    var mCurrLocationMarker: Marker? = null

    var mRequestLocationUpdatesPendingIntent : PendingIntent? = null
    fun buildGoogleApiClient() {
        synchronized(this) {
            mGoogleApiClient = GoogleApiClient.Builder(context)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build()
            mGoogleApiClient!!.connect()
        }
    }

    private fun createLocationRequest() {
        mLocationRequest = LocationRequest()
        mLocationRequest!!.interval = 100
        mLocationRequest!!.fastestInterval = 100
        mLocationRequest!!.smallestDisplacement = 1F
        mLocationRequest!!.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }
    override fun onConnected(p0: Bundle?) {
        Log.i(TAG, "onConnected")
        createLocationRequest()
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        Log.i(TAG, "start request locationOfUserWhoChangeIt updates ")
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this)
        // request locationOfUserWhoChangeIt updates
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                mLocationRequest,
                mRequestLocationUpdatesPendingIntent)

    }

    override fun onConnectionSuspended(p0: Int) {
        Log.i(TAG, "onConnectionSuspended")
        mGoogleApiClient!!.connect()
    }
    override fun onConnectionFailed(p0: ConnectionResult) {
        Log.i(TAG, "onConnectionFailed: google maps" + p0.errorMessage)
    }

    override fun onLocationChanged(location: Location?) {
        if (FirebaseAuth.getInstance().currentUser != null) {
            Log.i(TAG, "onLocationChanged")
            mLastLocation = location!!
            addCurrentUserLocationToFirebase(mLastLocation!!)
            //TODO czy to cos robi?
            if (mCurrLocationMarker != null) {//prevent if user click logout to not update locationOfUserWhoChangeIt
                mCurrLocationMarker!!.remove();
            }
        }
    }
    private fun addCurrentUserLocationToFirebase(lastLocation: Location) {
        Log.i(TAG, "addCurrentUserMarkerAndRemoveOld()")
        var locations = FirebaseDatabase.getInstance().getReference("Locations")
        var currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {//prevent if user click logout to not update locationOfUserWhoChangeIt
            locations.child(currentUser!!.uid)
                    .setValue(TrackingModel(currentUser.uid,
                            currentUser!!.email!!,
                            lastLocation.latitude.toString(),
                            lastLocation.longitude.toString()))
        }
    }



    private fun createNotificationChannelForApi26(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel, but only on API 26+ because
            // the NotificationChannel class is new and not in the support library
            val name = "My channel name"
            val description = "Description channel"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(Constants.CHANNEL_ID, name, importance)
            channel.description = description
            // Register the channel with the system
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }



}