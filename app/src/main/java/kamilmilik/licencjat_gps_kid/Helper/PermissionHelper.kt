package kamilmilik.licencjat_gps_kid.Helper

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Build
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.util.Log
import android.widget.Toast

/**
 * Created by kamil on 26.02.2018.
 */
class PermissionHelper(var context : Context){
    val TAG : String = PermissionHelper::class.java.simpleName

    val MY_PERMISSIONS_REQUEST_LOCATION = 99

    fun checkApkVersion() : Boolean{
        return (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
    }
    fun checkPermissionGranted():Boolean{
        return (ContextCompat.checkSelfPermission(context,
                android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
    }
    fun checkLocationPermission() {
        Log.i(TAG,"checkLocationPermission checkPermissionGranted " + checkPermissionGranted())
        if (!checkPermissionGranted()) {
            Log.i(TAG, " not granted")
            // Should we show an explanation?
            if (checkIsWeShouldShowExplanationOfPermission()) {
                Log.i(TAG,"show explanation dialog")
                showExplanationDialogOfUsedPermission()
            } else {
                Log.i(TAG,"request permission")
                // No explanation needed, we can request the permission.
                requestPermission()
            }
        }
    }
    fun checkIsWeShouldShowExplanationOfPermission() : Boolean{
        return (ActivityCompat.shouldShowRequestPermissionRationale(context as Activity,
                android.Manifest.permission.ACCESS_FINE_LOCATION))
    }
    fun showExplanationDialogOfUsedPermission(){
        // Show an explanation to the user *asynchronously* -- don't block
        // this thread waiting for the user's response! After the user
        // sees the explanation, try again to request the permission.
        AlertDialog.Builder(context)
                .setTitle("Location Permission Needed")
                .setMessage("This app needs the Location permission, please accept to use location functionality")
                .setPositiveButton("Location Settings", DialogInterface.OnClickListener { paramDialogInterface, paramInt ->
                    //Prompt the user once explanation has been shown
                    ActivityCompat.requestPermissions(context as Activity,
                            arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                            MY_PERMISSIONS_REQUEST_LOCATION );
                })
                .setNegativeButton("Cancel", DialogInterface.OnClickListener { paramDialogInterface, paramInt ->
                    paramDialogInterface.cancel()
                }).create().show()
    }
    fun requestPermission(){
        ActivityCompat.requestPermissions(context as Activity,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                MY_PERMISSIONS_REQUEST_LOCATION )
    }
    fun checkIsPermissionGrantedInRequestPermission(grantResults: IntArray) : Boolean{
        return grantResults.isNotEmpty()
                && grantResults[0] == PackageManager.PERMISSION_GRANTED
    }
    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
     fun showMissingPermissionError() {
        Toast.makeText(context,"Your permission was not granted", Toast.LENGTH_LONG).show()
    }
}












//package kamilmilik.licencjat_gps_kid
//
//import android.support.v7.app.AppCompatActivity
//import android.os.Bundle
//import android.support.v7.widget.LinearLayoutManager
//import android.support.v7.widget.RecyclerView
//import android.util.Log
//import android.view.Menu
//import kotlinx.android.synthetic.main.activity_list_online.*
//import android.content.Intent
//import android.location.Location
//import android.view.MenuItem
//import android.view.View
//import android.widget.Toast
//import com.google.android.gms.common.api.GoogleApiClient
//import com.google.android.gms.maps.GoogleMap
//import com.google.android.gms.maps.OnMapReadyCallback
//import com.google.android.gms.maps.SupportMapFragment
//import kamilmilik.licencjat_gps_kid.Helper.OnlineUserHelper
//import kamilmilik.licencjat_gps_kid.Invite.EnterInviteActivity
//import kamilmilik.licencjat_gps_kid.Invite.SendInviteActivity
//import kamilmilik.licencjat_gps_kid.Login.LoginActivity
//import kamilmilik.licencjat_gps_kid.models.User
//import kamilmilik.licencjat_gps_kid.Utils.RecyclerViewAdapter
//import kamilmilik.licencjat_gps_kid.Helper.FinderUserConnectionHelper
//import kamilmilik.licencjat_gps_kid.Helper.LocationHelper
//import kamilmilik.licencjat_gps_kid.Utils.OnItemClickListener
//import com.google.android.gms.maps.model.Marker
//import com.google.android.gms.location.LocationRequest
//import android.annotation.SuppressLint
//import android.os.Build
//import com.google.android.gms.common.ConnectionResult
//import com.google.android.gms.location.LocationServices
//import com.google.android.gms.maps.CameraUpdateFactory
//import com.google.android.gms.maps.model.BitmapDescriptorFactory
//import com.google.android.gms.maps.model.LatLng
//import com.google.android.gms.maps.model.MarkerOptions
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.database.*
//import kamilmilik.licencjat_gps_kid.Helper.PermissionHelper
//import kamilmilik.licencjat_gps_kid.models.TrackingModel
//import java.text.DecimalFormat
//
//
//class ListOnline : AppCompatActivity(),
//        OnItemClickListener,
//        OnMapReadyCallback,
//        GoogleApiClient.ConnectionCallbacks,
//        GoogleApiClient.OnConnectionFailedListener,
//        com.google.android.gms.location.LocationListener {
//
//
//
//    val TAG : String = ListOnline::class.java.simpleName
//
//
//    //Firebase
//    private var onlineUserHelper: OnlineUserHelper? = null
//    //view
//    lateinit var adapter : RecyclerViewAdapter
//    lateinit var recyclerView : RecyclerView
//    lateinit var valueSet :ArrayList<User>
//    //permission
//    private val MY_PERMISSION_REQUEST_CODE : Int = 7171
//    //Location
//    private  var locationHelper : LocationHelper? = null
//    private var mPermissionDenied = false
//
//
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_list_online)
//
//        setupRecyclerView()
//
//        generateCodeButtonAction()
//        enterCodeButtonAction()
//
//
//        setupToolbar()
//
//        setupFinderUserConnectionHelper()
//
//        setupAddOnlineUserToDatabaseHelper()
//
//        //setupLocationHelper()
//
//        val mapFragment = supportFragmentManager
//                .findFragmentById(R.id.map) as SupportMapFragment
//        mapFragment.getMapAsync(this)
////        var locationFirebaseHelper = LocationFirebaseHelper(mapFragment)
////        locationFirebaseHelper.initMapAsynch()
////        locationFirebaseHelper.loadLocationsFromDatabaseForCurrentUser("lurFM7tblDTaxNqIbaCnF9Dnv8k1",65.9666967,-18.5333)
//
//    }
//
//    private fun setupFinderUserConnectionHelper(){
//        var finderUserConnectionHelper  = FinderUserConnectionHelper(this@ListOnline, this, valueSet, adapter, recyclerView)
//        finderUserConnectionHelper.findFollowersConnectionAndUpdateRecyclerView()
//    }
//    private fun setupLocationHelper(){
//        locationHelper = LocationHelper(this, this@ListOnline)
//        locationHelper!!.currentUserLocationAction()
//    }
//
//    var mGoogleMap: GoogleMap? = null
//    var mapFrag: SupportMapFragment? = null
//    var mLocationRequest: LocationRequest? = null
//    var mGoogleApiClient: GoogleApiClient? = null
//    var mLastLocation: Location? = null
//    var mCurrLocationMarker: Marker? = null
//
//    var permissionHelper : PermissionHelper? = PermissionHelper(this)
//    override fun onMapReady(googleMap: GoogleMap) {
//        mGoogleMap = googleMap
//
//        //Initialize Google Play Services
//        if (permissionHelper!!.checkApkVersion()) {
//            Log.i(TAG,"vers(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) ion " + android.os.Build.VERSION.SDK_INT + " >= " + Build.VERSION_CODES.M)
//            if (permissionHelper!!.checkPermissionGranted()) {
//                Log.i(TAG, " Location Permission already granted")
//                //Location Permission already granted
//                buildGoogleApiClient()
//                mGoogleMap!!.isMyLocationEnabled = true
//            } else {
//                //Request Location Permission
//                checkLocationPermission()
//            }
//        } else {
//            Log.i(TAG,"version " + android.os.Build.VERSION.SDK_INT + " < " + Build.VERSION_CODES.M)
//            buildGoogleApiClient()
//            mGoogleMap!!.isMyLocationEnabled = true
//        }
//    }
//    fun buildGoogleApiClient() {
//        synchronized(this){
//            Log.i(TAG, "buildGoogleApiClient")
//            mGoogleApiClient =  GoogleApiClient.Builder(this)
//                    .addConnectionCallbacks(this)
//                    .addOnConnectionFailedListener(this)
//                    .addApi(LocationServices.API)
//                    .build();
//            mGoogleApiClient!!.connect();
//        }
//    }
//    @SuppressLint("MissingPermission")
//    override fun onConnected(p0: Bundle?) {
//        Log.i(TAG,"onConnected")
//        mLocationRequest =  LocationRequest()
//        mLocationRequest!!.interval = 1000
//        mLocationRequest!!.fastestInterval = 1000
//        mLocationRequest!!.smallestDisplacement = 10F
//        mLocationRequest!!.priority = LocationRequest.PRIORITY_HIGH_ACCURACY //it must be high accuracy if not it not run onLocationChanged in first run and when allow permission
//        if (permissionHelper!!.checkPermissionGranted()) {
//            Log.i(TAG, "start request location updates ")
//            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this)
//        }
//    }
//
//    override fun onConnectionSuspended(p0: Int) {
//    }
//
//    override fun onConnectionFailed(p0: ConnectionResult) {
//    }
//
//
//    override fun onLocationChanged(location: Location?) {
//        Log.i(TAG,"onLocationChanged")
//        mLastLocation = location
//        if (mCurrLocationMarker != null) {
//            mCurrLocationMarker!!.remove();
//        }
//        addCurrentUserLocationToFirebase(location!!)
//        loadLocationsFromDatabaseForCurrentUser("lurFM7tblDTaxNqIbaCnF9Dnv8k1", location!!.latitude, location!!.longitude)
//    }
//    private fun addCurrentUserLocationToFirebase( lastLocation : Location){
//        var locations = FirebaseDatabase.getInstance().getReference("Locations")
//        var currentUser = FirebaseAuth.getInstance().currentUser
//
//        locations.child(currentUser!!.uid)
//                .setValue(TrackingModel(currentUser.uid,
//                        currentUser!!.email!!,
//                        lastLocation.latitude.toString(),
//                        lastLocation.longitude.toString()))
//    }
//    fun loadLocationsFromDatabaseForCurrentUser(followingUserId : String, currentUserLat : Double, currentUserLng : Double){
//
//        var locations = FirebaseDatabase.getInstance().getReference("Locations")
//        var query : Query = locations.orderByChild("user_id").equalTo(followingUserId)
//        query.addValueEventListener(object : ValueEventListener {
//            override fun onDataChange(dataSnapshot: DataSnapshot?) {
//                for(singleSnapshot in dataSnapshot!!.children){
//                    var followingUserTracking = singleSnapshot.getValue(TrackingModel::class.java)
//                    var followingUserLocation = LatLng(followingUserTracking.lat!!.toDouble(), followingUserTracking.lng!!.toDouble())
//
//                    var followingUserLoc  = Location("")
//                    followingUserLoc.latitude = followingUserTracking.lat!!.toDouble()
//                    followingUserLoc.longitude = followingUserTracking.lng!!.toDouble()
//
//                    var currentUserLocation = Location("")
//                    currentUserLocation.latitude = currentUserLat!!
//                    currentUserLocation.longitude = currentUserLng!!
//
//                    mGoogleMap!!.clear()
//                    //add followingUser marker
//                    var measure : String?
//                    var distance : Float = currentUserLocation.distanceTo(followingUserLoc)
//                    if (distance > 1000){
//                        distance = (distance / 1000)
//                        measure = " km"
//                    }else{
//                        measure = " m"
//                    }
//                    Log.i(TAG,"ustawiam marker na pozycje: " + followingUserLocation + " dla " + followingUserTracking.email)
//                    mGoogleMap!!.addMarker(MarkerOptions()
//                            .position(followingUserLocation)
//                            .title(followingUserTracking.email)
//                            .snippet("Distance " + DecimalFormat("#.#").format(distance) + measure)
//                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)))
//                    mGoogleMap!!.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(followingUserLocation.latitude, followingUserLocation.longitude),12.0f))
//                }
//                if(dataSnapshot.value == null){//nothing found
//                    Log.i(TAG,"nothing found in onDataChange")
//                }
//                //add currentUser marker
//                Log.i(TAG,"ustawiam marker na pozycje: " + currentUserLat +" " + currentUserLng + " dla " + FirebaseAuth.getInstance().currentUser!!.email)
//                mGoogleMap!!.addMarker(MarkerOptions()
//                        .position(LatLng(currentUserLat!!, currentUserLng!!))
//                        .title(FirebaseAuth.getInstance().currentUser!!.email))
//            }
//
//            override fun onCancelled(databaseError: DatabaseError?) {
//            }
//
//
//        })
//    }
//    val MY_PERMISSIONS_REQUEST_LOCATION = 99
//    fun checkLocationPermission() {
//        if (permissionHelper!!.checkPermissionGranted()) {
//
//            // Should we show an explanation?
//            if (permissionHelper!!.checkIsWeShouldShowExplanationOfPermission()) {
//                permissionHelper!!.showExplanationDialogOfUsedPermission()
//            } else {
//                // No explanation needed, we can request the permission.
//                permissionHelper!!.requestPermission()
//            }
//        }
//    }
//
//    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        when(requestCode){
//            MY_PERMISSION_REQUEST_CODE ->{
//                if (permissionHelper!!.checkIsPermissionGrantedInRequestPermission(grantResults)) {
//                    // permission was granted, yay! Do the
//                    // location-related task you need to do.
//                    if (permissionHelper!!.checkPermissionGranted()) {
//
//                        if (mGoogleApiClient == null) {
//                            buildGoogleApiClient()
//                        }
//                        mGoogleMap!!.setMyLocationEnabled(true)
//                    }
//
//                } else {
//                    mPermissionDenied = true
//                    // permission denied, boo! Disable the
//                    // functionality that depends on this permission.
//                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show()
//                }
//                return
//            }
//        }
//    }
//
//    override fun onResumeFragments() {
//        super.onResumeFragments()
//        if (mPermissionDenied) {
//            // Permission was not granted, display error dialog.
//            permissionHelper!!.showMissingPermissionError()
//            mPermissionDenied = false
//        }
//    }
//
//    /**
//     * Displays a dialog with error message explaining that the location permission is missing.
//     */
//    private fun showMissingPermissionError() {
//        Toast.makeText(this,"Your permission was not granted",Toast.LENGTH_LONG).show()
//    }
//    //    override fun onPause() {
////        super.onPause()
////
////        //stop location updates when Activity is no longer active
////        if (mGoogleApiClient != null) {
////            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this)
////        }
////    }
//    //-----------firebase--------------------------------------------------------------------
//    private fun setupRecyclerView(){
//        recyclerView =  findViewById(R.id.listOnline)
//        recyclerView.setHasFixedSize(true)
//        recyclerView.layoutManager = LinearLayoutManager(this)
//
//        valueSet = ArrayList()
//        adapter = RecyclerViewAdapter(this@ListOnline, valueSet)
//        recyclerView.adapter = adapter
//        adapter.setClickListener(this)
//    }
//    override fun setOnItemClick(view: View, position: Int) {
//        Log.i(TAG,"setOnItemClick: clicked to item view in RecyclerView : position: "+ position + " user " + valueSet.get(position).email)
////        var intent = Intent(this, MapTrackingActivity::class.java)
////        intent.putExtra("followingUserId", valueSet.get(position).user_id)
////        intent.putExtra("currentUserLat", locationHelper!!.lastLocation.latitude)
////        intent.putExtra("currentUserLng", locationHelper!!.lastLocation.longitude)
////        startActivity(intent)
//    }
//    private fun generateCodeButtonAction(){
//        buttonToActivityGenerateCode.setOnClickListener({
//            var intent  = Intent(this, SendInviteActivity::class.java)
//            startActivity(intent)
//        })
//    }
//    private fun enterCodeButtonAction(){
//        buttonToActivityEnterInvite.setOnClickListener({
//            var intent = Intent(this, EnterInviteActivity::class.java)
//            startActivity(intent)
//        })
//    }
//
//
//    private fun setupAddOnlineUserToDatabaseHelper(){
//        onlineUserHelper = OnlineUserHelper()
//        onlineUserHelper!!.addOnlineUserToDatabase()
//    }
//
//    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        menuInflater.inflate(R.menu.main_menu, menu)
//        return true
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
//        when(item!!.itemId){
//            R.id.action_join->{//now it not work since if we click logout we are moved to login activity
//                onlineUserHelper!!.joinUserAction()
//            }
//            R.id.action_logout->{
//                onlineUserHelper!!.logoutUser()
//                startActivity(Intent(this, LoginActivity::class.java))
//            }
//        }
//        return super.onOptionsItemSelected(item)
//    }
//
//    private fun setupToolbar(){
//        toolbar.title = "Presence System"
//        setSupportActionBar(toolbar)
//    }
//
////    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
////        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
////        when(requestCode){
////            MY_PERMISSION_REQUEST_CODE ->{
////                    if(grantResults.isNotEmpty() && grantResults.get(0) == PackageManager.PERMISSION_GRANTED){
////                        locationHelper!!.setupCurrentLocation()
////                    }else{
////                        Toast.makeText(this,"Permission denied", Toast.LENGTH_LONG).show()
////                    }
////            }
////        }
////    }
////    override fun onStart() {
////        super.onStart()
////        locationHelper!!.connectToGoogleApi()
////    }
////
////    override fun onStop() {
////        locationHelper!!.disconnectGoogleApi()
////        super.onStop()
////    }
////
////    override fun onPostResume() {
////        super.onPostResume()
////        locationHelper!!.checkPlayServices()
////    }
////
////    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
////        locationHelper!!.setupCurrentLocation()
////    }
//}
