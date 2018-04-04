package kamilmilik.licencjat_gps_kid.Utils

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.util.Log
import com.firebase.jobdispatcher.JobParameters
import com.firebase.jobdispatcher.JobService
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kamilmilik.licencjat_gps_kid.models.TrackingModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ValueEventListener



/**
 * Created by kamil on 02.04.2018.
 */
class LocationJobService : JobService(),
GoogleApiClient.ConnectionCallbacks,
GoogleApiClient.OnConnectionFailedListener,
com.google.android.gms.location.LocationListener{
    private var mBackgroundTask: AsyncTask<Object, Void, Object>? = null
    var job : JobParameters? = null
    override fun onStartJob(job: JobParameters?): Boolean {
        Log.i(TAG,"onStartJob()")
        this.job = job
//        mBackgroundTask = object : AsyncTask<Object, Void, Object>(){
//            override fun doInBackground(vararg params: Object?): Object? {
//                //buildGoogleApiClient()
//                Log.i(TAG,"backgroundasync")
//                return null
//            }
//
//            override fun onPostExecute(result: Object?) {
//                jobFinished(job!!,false)
//                super.onPostExecute(result)
//            }
//
//        }
//        mBackgroundTask!!.execute()
        var thread = object : Thread(){
            override fun run() {
                //buildGoogleApiClient()
                var intent = Intent(applicationContext, PolygonAndLocationService::class.java)
                applicationContext.startService(intent)
                Log.i(TAG,"run()")
            }
        }
        thread.start()
        return true
    }
    override fun onStopJob(job: JobParameters?): Boolean {
//        if (mBackgroundTask != null) {
//            mBackgroundTask!!.cancel(true);
//        }
        Log.i("TAG", "onStopJob");
        /* true means, we're not done, please reschedule */
        return true;
    }

    private val TAG = LocationJobService::class.java.simpleName

    var mLocationRequest: LocationRequest? = null
    var mGoogleApiClient: GoogleApiClient? = null
    var mLastLocation: Location? = null


    var mRequestLocationUpdatesPendingIntent : PendingIntent? = null
    fun buildGoogleApiClient() {
        synchronized(this) {
            Log.i(TAG, "buildGoogleApiClient")
            mGoogleApiClient = GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build()
            mGoogleApiClient!!.connect()
        }
    }

    private fun createLocationRequest() {
        Log.i(TAG, "createLocationRequest")
        mLocationRequest = LocationRequest()
        mLocationRequest!!.interval = 100
        mLocationRequest!!.fastestInterval = 100
        mLocationRequest!!.smallestDisplacement = 1F
        mLocationRequest!!.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }
    override fun onConnected(p0: Bundle?) {
        Log.i(TAG, "onConnected")
        createLocationRequest()
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
        }
    }

    private fun addCurrentUserLocationToFirebase(lastLocation: Location) {
        var locations = FirebaseDatabase.getInstance().getReference("Locations")
        var currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {//prevent if user click logout to not update locationOfUserWhoChangeIt
            Log.i(TAG, "addCurrentUserMarkerAndRemoveOld() current user: " + currentUser!!.uid + " location " + lastLocation.toString() )

            val scoresRef = locations.child(currentUser!!.uid)
            scoresRef.keepSynced(true)
            val connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected")
            connectedRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val connected = snapshot.getValue(Boolean::class.java)!!
                    if (connected) {
                        Log.i(TAG,"connected")
                        locations.child(currentUser!!.uid)
                                .setValue(TrackingModel(currentUser.uid,
                                        currentUser!!.email!!,
                                        lastLocation.latitude.toString(),
                                        lastLocation.longitude.toString()), object : DatabaseReference.CompletionListener {
                                    override fun onComplete(error: DatabaseError?, reference: DatabaseReference?) {
                                        Log.i(TAG,"onComplete()")
                                        if(error == null){
                                            Log.i(TAG,"onComplete() position saved to firebase database")
                                            Log.i(TAG,"okey stop service")
                                            mGoogleApiClient!!.disconnect()
                                            jobFinished(job!!, false) //it must be called here, since I return true
                                        }else{
                                            Log.i(TAG,"there is problem to add data to database")
                                        }
                                    }
                                })
                    } else {
                        Log.i(TAG,"not connected")
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    System.err.println("Listener was cancelled")
                }
            })
        }
    }

    override fun onDestroy() {
        Log.i(TAG,"Problem: service destroy it couldn't happen")
        super.onDestroy()
    }

}