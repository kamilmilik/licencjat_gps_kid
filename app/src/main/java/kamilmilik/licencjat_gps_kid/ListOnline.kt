package kamilmilik.licencjat_gps_kid

import android.app.PendingIntent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.Menu
import kotlinx.android.synthetic.main.activity_list_online.*
import android.content.Intent
import android.graphics.Point
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import kamilmilik.licencjat_gps_kid.Invite.EnterInviteActivity
import kamilmilik.licencjat_gps_kid.Invite.SendInviteActivity
import kamilmilik.licencjat_gps_kid.Login.LoginActivity
import kamilmilik.licencjat_gps_kid.Utils.RecyclerViewAdapter
import kamilmilik.licencjat_gps_kid.Utils.OnItemClickListener
import android.os.Build
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.auth.FirebaseAuth
import kamilmilik.licencjat_gps_kid.Helper.*
import android.view.MotionEvent
import com.google.android.gms.maps.model.*
import com.google.maps.android.PolyUtil
import kamilmilik.licencjat_gps_kid.Utils.PolygonService


class ListOnline : AppCompatActivity(),
        OnItemClickListener,
        OnMapReadyCallback{
    val TAG : String = ListOnline::class.java.simpleName

    //Firebase
    private var onlineUserHelper: OnlineUserHelper? = null
    //view
    lateinit var adapter : RecyclerViewAdapter
    lateinit var recyclerView : RecyclerView
    lateinit var valueSet:HashSet<String>
    //permission
    private val MY_PERMISSION_REQUEST_CODE : Int = 99
    private var permissionHelper : PermissionHelper = PermissionHelper(this)
    private var mPermissionDenied = false
    //Location
    private  var locationHelper : LocationHelper? = null
    private var finderUserConnectionHelper : FinderUserConnectionHelper? = null
    private var locationFirebaseHelper : LocationFirebaseHelper? = null
    //maps
    private var mGoogleMap: GoogleMap? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_online)
        setupRecyclerView()

        generateCodeButtonAction()
        enterCodeButtonAction()

        setupToolbar()

        setupAddOnlineUserToDatabaseHelper()

        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun setupFinderUserConnectionHelper(locationFirebaseHelper: LocationFirebaseHelper){
        finderUserConnectionHelper  = FinderUserConnectionHelper(this, this, valueSet, adapter, recyclerView,locationFirebaseHelper,permissionHelper)
    }
    private fun setupLocationHelper(locationFirebaseHelper: LocationFirebaseHelper){
        Log.i(TAG,permissionHelper.toString() + " " + mGoogleMap)
        locationHelper = LocationHelper(this,permissionHelper, locationFirebaseHelper)
    }

    private var polygonPoints: ArrayList<LatLng> = ArrayList()
    private var polygon : Polygon? = null
    var buttonClickedToDrawPolyline: Boolean? = false // to detect map is movable

    override fun onMapReady(googleMap: GoogleMap) {
        mGoogleMap = googleMap
        locationFirebaseHelper = LocationFirebaseHelper(mGoogleMap!!,this)
        setupFinderUserConnectionHelper(locationFirebaseHelper!!)
        finderUserConnectionHelper!!.listenerForConnectionsUserChangeInFirebaseAndUpdateRecyclerView()
        setupLocationHelper(locationFirebaseHelper!!)
        drawButton.setOnClickListener {
            buttonClickedToDrawPolyline = !buttonClickedToDrawPolyline!!
            if(!buttonClickedToDrawPolyline!!) {
                draggable.setOnTouchListener(null)
            }else{
                draggable.setOnTouchListener(object : View.OnTouchListener{
                    override fun onTouch(v: View?, motionEvent: MotionEvent?): Boolean {
                        locationFirebaseHelper!!.onTouchAction(motionEvent)
                        return buttonClickedToDrawPolyline!!
                    }

                })
            }
        }

        //Initialize Google Play Services
        if (permissionHelper!!.checkApkVersion()) {
            Log.i(TAG, "vers(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) ion " + android.os.Build.VERSION.SDK_INT + " >= " + Build.VERSION_CODES.M)
            if (permissionHelper!!.checkPermissionGranted()) {
                Log.i(TAG, " Location Permission already granted")
                //Location Permission already granted
                locationHelper!!.buildGoogleApiClient()
                mGoogleMap!!.isMyLocationEnabled = true
            } else {
                Log.i(TAG, "Request Location Permission")
                //Request Location Permission
                permissionHelper!!.checkLocationPermission()
            }
        } else {
            Log.i(TAG, "version " + android.os.Build.VERSION.SDK_INT + " < " + Build.VERSION_CODES.M)
            locationHelper!!.buildGoogleApiClient()
            mGoogleMap!!.isMyLocationEnabled = true
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            MY_PERMISSION_REQUEST_CODE ->{
                Log.i(TAG,"checkIsPermissionGrantedInRP " + permissionHelper!!.checkIsPermissionGrantedInRequestPermission(grantResults))
                if (permissionHelper!!.checkIsPermissionGrantedInRequestPermission(grantResults)) {
                    Log.i(TAG,"permission was granted, yay!")
                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (permissionHelper!!.checkPermissionGranted()) {

                        if (locationHelper!!.mGoogleApiClient == null) {
                            locationHelper!!.buildGoogleApiClient()
                        }
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

        valueSet = HashSet()
        valueSet.add(FirebaseAuth.getInstance().currentUser!!.email!! + " (ja)")
        var valueList = ArrayList(valueSet)
        adapter = RecyclerViewAdapter(this@ListOnline, valueList)
        recyclerView.adapter = adapter
        adapter.setClickListener(this)
    }
    override fun setOnItemClick(view: View, position: Int) {
        var valueList = ArrayList(valueSet)
        var clickedUserEmail = valueList[position].replace(" (ja)","")
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
    }


    private fun setupAddOnlineUserToDatabaseHelper(){
        onlineUserHelper = OnlineUserHelper()
        onlineUserHelper!!.addOnlineUserToDatabase()
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
                onlineUserHelper!!.logoutUser()
                startActivity(Intent(this, LoginActivity::class.java))
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupToolbar(){
        toolbar.title = "Presence System"
        setSupportActionBar(toolbar)
    }

    override fun onResume() {
        Log.i(TAG,"onResume")
        super.onResume()

        var response = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this)
        if(response != ConnectionResult.SUCCESS){
            Log.i(TAG, "Google Play services not available")
            GoogleApiAvailability.getInstance().getErrorDialog(this,response,1).show()
        }
    }

}
