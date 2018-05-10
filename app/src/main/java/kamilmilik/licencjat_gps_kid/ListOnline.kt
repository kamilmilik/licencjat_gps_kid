package kamilmilik.licencjat_gps_kid

import android.annotation.SuppressLint
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.Menu
import kotlinx.android.synthetic.main.activity_list_online.*
import android.content.Intent
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import kamilmilik.licencjat_gps_kid.Invite.EnterInviteActivity
import kamilmilik.licencjat_gps_kid.Invite.SendInviteActivity
import kamilmilik.licencjat_gps_kid.Utils.RecyclerViewAdapter
import kamilmilik.licencjat_gps_kid.Utils.OnItemClickListener
import android.os.Build
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.auth.FirebaseAuth
import kamilmilik.licencjat_gps_kid.Helper.*
import android.view.MotionEvent
import kamilmilik.licencjat_gps_kid.Helper.LocationOperation.LocationFirebaseHelper
import kamilmilik.licencjat_gps_kid.Helper.LocationOperation.LocationHelper
import kamilmilik.licencjat_gps_kid.Helper.PolygonOperation.DrawPolygon
import kamilmilik.licencjat_gps_kid.Helper.UserOperations.OnlineUserHelper
import com.firebase.jobdispatcher.*
import com.google.firebase.FirebaseApp
import kamilmilik.licencjat_gps_kid.Utils.LocationJobService
import android.app.ActivityManager
import android.app.ProgressDialog
import android.content.ComponentCallbacks2
import android.content.Context
import com.firebase.ui.auth.ui.ProgressDialogHolder
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.UserInfo
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kamilmilik.licencjat_gps_kid.Login.MainActivity
import kamilmilik.licencjat_gps_kid.Profile.ProfileActivity
import kamilmilik.licencjat_gps_kid.Utils.ForegroundOnTaskRemovedActivity
import kamilmilik.licencjat_gps_kid.models.UserMarkerInformationModel
import java.util.HashMap


class ListOnline : ApplicationActivity(),
        OnItemClickListener,
        OnMapReadyCallback {
    val TAG : String = ListOnline::class.java.simpleName

    //Firebase
    private var onlineUserHelper: OnlineUserHelper? = null
    //view
    lateinit var adapter : RecyclerViewAdapter
    lateinit var recyclerView : RecyclerView
    lateinit var valueSet:HashSet<UserMarkerInformationModel>
    //permission
    private var permissionHelper : PermissionHelper = PermissionHelper(this)
    private var mPermissionDenied = false
    //Location
    private  var locationHelper : LocationHelper? = null
    private var finderUserConnectionHelper : FinderUserConnectionHelper? = null
    private var locationFirebaseHelper : LocationFirebaseHelper? = null
    //maps
    private var mGoogleMap: GoogleMap? = null

    private var progressDialog : ProgressDialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseApp.initializeApp(applicationContext)//I must called this first otherwise foreground/background service is not running since without it get nullPointerException

        setContentView(R.layout.activity_list_online)
//        FirebaseAuth.getInstance().signOut()

        dispatcher2 = FirebaseJobDispatcher(GooglePlayDriver(applicationContext))
        //dispatcher2.cancelAll()
        dispatcher2.cancel("my-location-job")

//        val am = applicationContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
//        Log.i(TAG,"package name " + packageName)
//        am.killBackgroundProcesses(packageName)
//        var activityManger  : ActivityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager;
//        var  list : List<ActivityManager.RunningAppProcessInfo> = activityManger.runningAppProcesses;
//        if (list != null)
//            for (i in list.indices) {
//                var apinfo : ActivityManager.RunningAppProcessInfo = list.get(i);
//                Log.i(TAG,"onCreate() sd f " + apinfo )
//                var pkgList : Array<String> = apinfo.pkgList;
//
//                if (apinfo.importance > ActivityManager.RunningAppProcessInfo.IMPORTANCE_PERCEPTIBLE ) {
//                    for (j in pkgList.indices) {
//                        Log.i(TAG,"onCreate() sd  ${pkgList[j]}")
//                        activityManger.killBackgroundProcesses(pkgList[j]);
//                    }
//                }
//            }

//        Log.i(TAG,"onCreate() kill my process " + android.os.Process.myPid())
//        android.os.Process.killProcess(android.os.Process.myPid())

        var am: ActivityManager = this.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager;
        am.killBackgroundProcesses("kamilmilik.licencjat_gps_kid.Utils");
        am.killBackgroundProcesses("kamilmilik.licencjat_gps_kid:separate");
        am.killBackgroundProcesses("kamilmilik.licencjat_gps_kid.Utils.LocationJobService");


        var taskInfo: List<ActivityManager.RunningTaskInfo> = am.getRunningTasks(100);
        for (taskInformation: ActivityManager.RunningTaskInfo in taskInfo) {
            Log.i(TAG,"onCreate() task " + taskInformation.baseActivity.getClassName())
            if ("kamilmilik.licencjat_gps_kid.Utils".equals(taskInformation.baseActivity.getClassName(), ignoreCase = true)) {
                var pack: String = taskInformation.baseActivity.getClassName().substring(0, taskInformation.baseActivity.getClassName().lastIndexOf("."));
                Log.i(TAG, "onCreate() pack " + pack)
                am.killBackgroundProcesses("kamilmilik.licencjat_gps_kid.Utils");
            }
}

        //setupPolygonBackgroundService()

        //firebaseDispatcherAction()

        setupRecyclerView()

        generateCodeButtonAction()
        enterCodeButtonAction()

//        setupToolbar()

        setupAddOnlineUserToDatabaseHelper()

        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

//    private fun setupPolygonBackgroundService(){
//        var intent = Intent(this, PolygonAndLocationService::class.java)
//        startService(intent)
//    }
    private fun setupFinderUserConnectionHelper(locationFirebaseHelper: LocationFirebaseHelper) {
        progressDialog = ProgressDialog.show(this, "Please wait...", "Proccessing...", true)

        finderUserConnectionHelper = FinderUserConnectionHelper(this, this, valueSet, adapter, recyclerView, locationFirebaseHelper, progressDialog!!)
    }
    private fun setupLocationHelper(locationFirebaseHelper: LocationFirebaseHelper){
        Log.i(TAG,permissionHelper.toString() + " " + mGoogleMap)
        locationHelper = LocationHelper(this, permissionHelper, locationFirebaseHelper, finderUserConnectionHelper!!.getMarkerAddedListener())
    }

    var buttonClickedToDrawPolyline: Boolean? = false // to detect map is movable
    var buttonClickedToEditPolyline: Boolean? = false // to detect map is movable
    var drawPolygon : DrawPolygon? = null
    override fun onMapReady(googleMap: GoogleMap) {
        mGoogleMap = googleMap
        locationFirebaseHelper = LocationFirebaseHelper(mGoogleMap!!, this)
        setupFinderUserConnectionHelper(locationFirebaseHelper!!)
        finderUserConnectionHelper!!.listenerForConnectionsUserChangeInFirebaseAndUpdateRecyclerView()
        setupLocationHelper(locationFirebaseHelper!!)
        drawPolygon = DrawPolygon(mGoogleMap!!,this)
        drawPolygonButtonAction()
        editPolygonButtonAction()
        //Initialize Google Play Services
        if (permissionHelper!!.checkApkVersion()) {
            Log.i(TAG, "vers(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) ion " + android.os.Build.VERSION.SDK_INT + " >= " + Build.VERSION_CODES.M)
            if (permissionHelper!!.checkPermissionGranted()) {
                Log.i(TAG, " Location Permission already granted")
                //Location Permission already granted
                locationHelper!!.getLocation()
                mGoogleMap!!.isMyLocationEnabled = true
            } else {
                Log.i(TAG, "Request Location Permission")
                //Request Location Permission
                permissionHelper!!.checkLocationPermission()
            }
        } else {
            Log.i(TAG, "version " + android.os.Build.VERSION.SDK_INT + " < " + Build.VERSION_CODES.M)
            locationHelper!!.getLocation()
            mGoogleMap!!.isMyLocationEnabled = true
        }
    }

//    private fun firebaseDispatcherAction(){
//        val dispatcher = FirebaseJobDispatcher(GooglePlayDriver(applicationContext))
//        val myJob = dispatcher.newJobBuilder()
//                .setService(PolygonJobService::class.java) // the JobService that will be called
//                .setTag("my-unique-tag")        // uniquely identifies the job
//                .setLifetime(Lifetime.UNTIL_NEXT_BOOT)
//                // start between windowStart in sec and windowEnd in seconds from now
//                .setTrigger(Trigger.executionWindow(0, 60))
//                .setRecurring(true)//to reschedule job
//                .setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL)
//                .setReplaceCurrent(true)
//                .build()
//
//        dispatcher.mustSchedule(myJob)
//
//        val dispatcher2 = FirebaseJobDispatcher(GooglePlayDriver(applicationContext))
//        val myLocationJob = dispatcher2.newJobBuilder()
//                .setService(LocationJobService::class.java) // the JobService that will be called
//                .setTag("my-location-job")        // uniquely identifies the job
//                .setLifetime(Lifetime.UNTIL_NEXT_BOOT)
//                // start between windowStart in sec and windowEnd in seconds from now
//                .setTrigger(Trigger.executionWindow(0, 20))
//                .setRecurring(true)//to reschedule job
//                .setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL)
//                .setReplaceCurrent(true)
//                .build()
//
//        dispatcher2.mustSchedule(myLocationJob)
//    }
    private fun editPolygonButtonAction(){
        editPolygonButton.setOnClickListener {
            buttonClickedToEditPolyline = !buttonClickedToEditPolyline!!
            drawButton.isEnabled = !buttonClickedToEditPolyline!!
            if(!buttonClickedToEditPolyline!!) {
                drawPolygon!!.hideAllMarkers()
            }else{
                drawPolygon!!.showMarkers()
            }
        }
    }
    private fun drawPolygonButtonAction(){
        drawButton.setOnClickListener {
            drawButton.isEnabled = false
//            buttonClickedToDrawPolyline = !buttonClickedToDrawPolyline!!
//            if(!buttonClickedToDrawPolyline!!) {
//                draggable.setOnTouchListener(null)
//            }else{
             draggable.setOnTouchListener(object : View.OnTouchListener{
                    override fun onTouch(v: View?, motionEvent: MotionEvent?): Boolean {
                        drawPolygon!!.onTouchAction(motionEvent,draggable)
                        drawButton.isEnabled = true
                        //return buttonClickedToDrawPolyline!!
                        return true
                    }
                })
//            }
        }
    }

    override fun onStop() {
        Log.i(TAG,"onStop()")
        super.onStop()
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            Constants.MY_PERMISSION_REQUEST_CODE ->{
                Log.i(TAG,"checkIsPermissionGrantedInRP " + permissionHelper!!.checkIsPermissionGrantedInRequestPermission(grantResults))
                if (permissionHelper!!.checkIsPermissionGrantedInRequestPermission(grantResults)) {
                    Log.i(TAG,"permission was granted, yay!")
                    // permission was granted, yay! Do the
                    // locationOfUserWhoChangeIt-related task you need to do.
                    if (permissionHelper!!.checkPermissionGranted()) {
                        locationHelper!!.getLocation()
                        mGoogleMap!!.setMyLocationEnabled(true)
                    }

                } else {
                    mPermissionDenied = true
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show()
                }
                return
            }
        }
    }

    override fun onResumeFragments() {
        super.onResumeFragments()
        if (mPermissionDenied) {
            // Permission was not granted, display error dialog.
            permissionHelper!!.showMissingPermissionError()
            mPermissionDenied = false
        }
    }

    //-----------firebase--------------------------------------------------------------------
    private fun setupRecyclerView(){
        recyclerView =  findViewById(R.id.listOnline)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false)

        //add current user and other users are add in FinderUserConnectionHelper
        valueSet = HashSet()
//        valueSet.add(FirebaseAuth.getInstance().currentUser!!.email!! + " (ja)")
        if(FirebaseAuth.getInstance().currentUser != null){
            Log.i(TAG,"setupRecyclerView() current user mail " + FirebaseAuth.getInstance().currentUser!!.email!!)
            valueSet.add(UserMarkerInformationModel(FirebaseAuth.getInstance().currentUser!!.email!!, FirebaseAuth.getInstance().currentUser!!.displayName!!))
            var valueList = ArrayList(valueSet)
            adapter = RecyclerViewAdapter(this@ListOnline, valueList)
            recyclerView.adapter = adapter
            adapter.setClickListener(this)
        }
    }
    override fun setOnItemClick(view: View, position: Int) {
        var valueList = ArrayList(valueSet)
        var clickedUserEmail = valueList[position]
        Log.i(TAG,"setOnItemClick: clicked to item view in RecyclerView : position: "+ position + " user " + clickedUserEmail + "koniec")
        locationFirebaseHelper!!.goToThisMarker(clickedUserEmail)
    }

    private fun generateCodeButtonAction(){
        buttonToActivityGenerateCode.setOnClickListener({
            var intent  = Intent(this, SendInviteActivity::class.java)
            startActivity(intent)
        })
    }
    private fun enterCodeButtonAction(){
        buttonToActivityEnterInvite.setOnClickListener({
            var intent = Intent(this, EnterInviteActivity::class.java)
            startActivity(intent)
        })

        buttonToActivityChangeName.setOnClickListener({
            //set user name
            startActivity(Intent(this@ListOnline, ProfileActivity::class.java))
//            val currentUser = FirebaseAuth.getInstance().currentUser
//            var name : String = "test1"
//            val profileUpdates = UserProfileChangeRequest.Builder()
//                    .setDisplayName(name)
//                    .build()
//            currentUser!!.updateProfile(profileUpdates)
//                    .addOnCompleteListener {
//                        Log.i(TAG, "onComplete() name ")
//                        recreate()
//                        var map   = HashMap<String,Any>() as MutableMap<String,Any>
//                        map.put("user_name", name)
//                        FirebaseDatabase.getInstance().reference.child("user_account_settings").child(currentUser.uid).updateChildren(map)
//                        FirebaseDatabase.getInstance().reference.child("Locations").child(currentUser.uid).updateChildren(map)
//
//                    }
        })
    }

    private fun setupAddOnlineUserToDatabaseHelper(){
        if(FirebaseDatabase.getInstance().reference != null && FirebaseAuth.getInstance().currentUser != null ){
            onlineUserHelper = OnlineUserHelper()
            onlineUserHelper!!.addOnlineUserToDatabase()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item!!.itemId){
            R.id.action_join->{//now it not work since if we click logout we are moved to login activity
                onlineUserHelper!!.joinUserAction()
            }
            R.id.action_logout->{

                dispatcher2 = FirebaseJobDispatcher(GooglePlayDriver(applicationContext))
                dispatcher2.cancelAll()
                dispatcher2.cancel("my-location-job")

                dispatcher1 = FirebaseJobDispatcher(GooglePlayDriver(applicationContext))
                dispatcher1.cancelAll()
                dispatcher1.cancel("my-polygon-job")

                onlineUserHelper!!.logoutUser()
                var  intent =  Intent(this, MainActivity::class.java);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

//    private fun setupToolbar(){
//        toolbar.title = "Presence System"
//        setSupportActionBar(toolbar)
//    }

    @SuppressLint("MissingPermission")
    override fun onResume() {
        Log.i(TAG,"onResume")
        super.onResume()

        dispatcher2 = FirebaseJobDispatcher(GooglePlayDriver(applicationContext))
        //dispatcher2.cancelAll()
        dispatcher2.cancel("my-location-job")


        var am: ActivityManager = this.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager;
        am.killBackgroundProcesses("kamilmilik.licencjat_gps_kid.Utils");
        am.killBackgroundProcesses("kamilmilik.licencjat_gps_kid:separate");
        am.killBackgroundProcesses("kamilmilik.licencjat_gps_kid.Utils.LocationJobService")
        am.killBackgroundProcesses("kamilmilik.licencjat_gps_kid")
        stopService(Intent(this@ListOnline, LocationJobService::class.java))
        stopService(Intent(this@ListOnline, ForegroundOnTaskRemovedActivity::class.java))

        var  listprocInfos  : List<ActivityManager.RunningAppProcessInfo> = (this.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager).getRunningAppProcesses();

        for(  procInfos in listprocInfos ) {
            Log.i(TAG,"running process " + procInfos.processName)
            (this.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager).killBackgroundProcesses(procInfos.processName);
            // or activityManager.restartPackage(procInfos.processName);
        }
//        val activityManager = this.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
//        //TODO permission
//        activityManager.killBackgroundProcesses("kamilmilik.licencjat_gps_kid")

        var response = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this)
        if(response != ConnectionResult.SUCCESS){
            Log.i(TAG, "Google Play services not available")
            GoogleApiAvailability.getInstance().getErrorDialog(this,response,1).show()
        }
    }
    private lateinit var dispatcher2 : FirebaseJobDispatcher
    private lateinit var dispatcher1 : FirebaseJobDispatcher


    //TODO onPause musze dac odnosnie calej aplikacji bo teraz jak daje przycisk wstecz z jakiejs innej aktywnosci np to sie uruchamia
    override fun onPause() {
        Log.i(TAG,"onPause()")
        super.onPause()
        //firebase support test
//        var dispatcher = FirebaseJobDispatcher(GooglePlayDriver(this))
//
//        val myJob = dispatcher.newJobBuilder()
//                .setService(PresenceService::class.java)
//                .setTag("presence-tag")
//                .setRecurring(true)
//                .setLifetime(Lifetime.UNTIL_NEXT_BOOT)
//                .setTrigger(Trigger.executionWindow(0, 60))
//                .setReplaceCurrent(false)
//                .setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL)
//                .build()
//
//        //Service will execute within at most 60 seconds
//        dispatcher.mustSchedule(myJob)
    }

    override fun onDestroy() {
        Log.i(TAG,"onDestroy() run job")
        progressDialog!!.dismiss()
        super.onDestroy()
    }

    override fun onTrimMemory(level: Int) {
        if (level == ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN) {//detect when UI is hidden
            Log.i(TAG,"onTrimMemory()")
            try {
                locationHelper!!.fusedLocationClient.removeLocationUpdates(locationHelper!!.locationCallback)
                Log.i(TAG,"onPause() remove fusedLocationClient")
            } catch (exception: Exception) {
                exception.printStackTrace()
            }catch (exception : UninitializedPropertyAccessException ){
                exception.printStackTrace()
            }
            dispatcher2 = FirebaseJobDispatcher(GooglePlayDriver(applicationContext))
            val myLocationJob = dispatcher2.newJobBuilder()
                    .setService(LocationJobService::class.java) // the JobService that will be called
                    .setTag("my-location-job")        // uniquely identifies the job
                    .setLifetime(Lifetime.UNTIL_NEXT_BOOT)
                    // start between windowStart in sec and windowEnd in seconds from now
                    .setTrigger(Trigger.executionWindow(0,  60))
                    .setRecurring(true)//to reschedule job
                    .setRetryStrategy(dispatcher2.newRetryStrategy(RetryStrategy.RETRY_POLICY_EXPONENTIAL,30,3000))
                    .setReplaceCurrent(true)
                    .build()

            dispatcher2.mustSchedule(myLocationJob)

            dispatcher1 = FirebaseJobDispatcher(GooglePlayDriver(applicationContext))
            val myLocationJob2 = dispatcher1.newJobBuilder()
                    .setService(PolygonJobService::class.java) // the JobService that will be called
                    .setTag("my-polygon-job")        // uniquely identifies the job
                    .setLifetime(Lifetime.UNTIL_NEXT_BOOT)
                    // start between windowStart in sec and windowEnd in seconds from now
                    .setTrigger(Trigger.executionWindow(0,  60))
                    .setRecurring(true)//to reschedule job
                    .setRetryStrategy(dispatcher1.newRetryStrategy(RetryStrategy.RETRY_POLICY_EXPONENTIAL,30,3000))
                    .setReplaceCurrent(true)
                    .build()

            dispatcher1.mustSchedule(myLocationJob2)
        }
            super.onTrimMemory(level)
    }
}
