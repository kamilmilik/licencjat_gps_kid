package kamilmilik.gps_tracker.map

import android.app.Activity
import android.os.Bundle
import android.view.Menu
import kotlinx.android.synthetic.main.activity_map.*
import android.content.Intent
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.google.android.gms.maps.GoogleMap
import kamilmilik.gps_tracker.invite.EnterInviteActivity
import kamilmilik.gps_tracker.invite.SendInviteActivity
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.auth.FirebaseAuth
import kamilmilik.gps_tracker.map.PolygonOperation.DrawPolygon
import kamilmilik.gps_tracker.login.DatabaseOnlineUserAction
import com.google.firebase.FirebaseApp
import android.content.ComponentCallbacks2
import android.graphics.Color
import android.location.Location
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.firebase.database.FirebaseDatabase
import kamilmilik.gps_tracker.ApplicationActivity
import kamilmilik.gps_tracker.R
import kamilmilik.gps_tracker.login.LoginActivity
import kamilmilik.gps_tracker.map.PolygonOperation.notification.Notification
import kamilmilik.gps_tracker.models.User
import kamilmilik.gps_tracker.models.UserMarkerInformationModel
import kamilmilik.gps_tracker.profile.ProfileActivity
import kamilmilik.gps_tracker.utils.*
import kotlinx.android.synthetic.main.progress_bar.*


class MapActivity : ApplicationActivity(), OnMapReadyCallback {

    private val TAG: String = MapActivity::class.java.simpleName

    private var databaseOnlineUserAction: DatabaseOnlineUserAction? = null

    private var isPermissionDenied = false

    private var locationOperations: LocationOperations? = null

    private var finderUserConnection: FinderUserConnection? = null

    private var locationFirebaseMarkerAction: LocationFirebaseMarkerAction? = null

    private var googleMap: GoogleMap? = null

    private var recyclerViewAction: RecyclerViewAction? = null

    private var drawPolygon: DrawPolygon? = null

    private var notificationMethods : Notification? = null

    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)
        Tools.setupToolbar(this, false).title = getString(R.string.mapActivityName)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseApp.initializeApp(applicationContext)

        setContentView(R.layout.activity_map)
        progressBarRelative?.visibility = View.VISIBLE

        (supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment).getMapAsync(this)

        BatteryOptimizationUtils.goToAddIgnoreBatteryOptimizationSettings(this)

        generateCodeButtonAction()
        enterCodeButtonAction()
        profileActivityAction()

        setupAddOnlineUserToDatabaseHelper()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap

        initialize()

        recyclerViewAction?.setupRecyclerView()
        finderUserConnection!!.findFollowersConnectionAndUpdateRecyclerView()

        drawPolygonButtonAction()
        editPolygonButtonAction()

        locationPermissionAction()

        runCheckAreaAction()
    }

    private fun initialize(){
        locationFirebaseMarkerAction = LocationFirebaseMarkerAction(this)
        recyclerViewAction = RecyclerViewAction(this)
        locationOperations = LocationOperations(this)
        finderUserConnection = FinderUserConnection(this)
        drawPolygon = DrawPolygon(this)
    }
    private fun runCheckAreaAction(){
        // PolygonAction it is also run in foregroundService.
        object : Thread() {
            override fun run() {
                FirebaseApp.initializeApp(applicationContext)//I must called this first otherwise foreground/background service is not running since without it get nullPointerException
                notificationMethods = Notification(this@MapActivity)
                notificationMethods!!.notificationAction(false)
            }
        }.start()
    }

    private fun locationPermissionAction() {
        if(locationOperations != null && this.googleMap != null ){
            if (PermissionsUtils.checkApkVersion()) {
                if (PermissionsUtils.checkPermissionGranted(this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                    locationOperations!!.getLocation()
                    this.googleMap!!.isMyLocationEnabled = true
                } else {
                    PermissionsUtils.checkLocationPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                }
            } else {
                locationOperations!!.getLocation()
                this.googleMap!!.isMyLocationEnabled = true
            }
        }
    }

    private fun editPolygonButtonAction() {
        editPolygonButton.setOnClickListener {
            if(!drawPolygon!!.markersMap!!.isEmpty()){
                if(editPolygonButton.textColors.defaultColor == resources.getColor(R.color.blackColor)){
                    editPolygonButton.setTextColor(Color.GRAY)
                    drawPolygon!!.showAllMarkers()
                    drawButton.isEnabled = false
                } else {
                    editPolygonButton.setTextColor(resources.getColor(R.color.blackColor))
                    drawButton.isEnabled = true
                    drawPolygon!!.hideAllMarkers()
                }
            }
        }
    }

    private fun drawPolygonButtonAction() {
        drawButton.setOnClickListener {
            if(drawButton.textColors.defaultColor == resources.getColor(R.color.blackColor)){
                drawButton.setTextColor(Color.GRAY)
                editPolygonButton.isEnabled = false
                draggable.setOnTouchListener { v, motionEvent ->
                    drawPolygon!!.onTouchAction(motionEvent, draggable)
                    drawButton.setTextColor(resources.getColor(R.color.blackColor))
                    editPolygonButton.isEnabled = true
                    true
                }
            }else{
                draggable.setOnTouchListener(null)
                editPolygonButton.isEnabled = true
                drawButton.setTextColor(resources.getColor(R.color.blackColor))
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            Constants.LOCATION_PERMISSION_REQUEST_CODE -> {
                if (PermissionsUtils.checkIsPermissionGrantedInRequestPermission(grantResults)) {
                    if (PermissionsUtils.checkPermissionGranted(this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                        locationOperations!!.getLocation()
                        googleMap!!.isMyLocationEnabled = true
                    }
                } else {
                    isPermissionDenied = true
                    databaseOnlineUserAction!!.logoutUser()
                    Tools.startNewActivityWithoutPrevious(this, LoginActivity::class.java)
                    Toast.makeText(this, getString(R.string.permissionDenied), Toast.LENGTH_LONG).show()
                }
                return
            }
        }
    }

    private fun generateCodeButtonAction() {
        buttonToActivityGenerateCode.setOnClickListener({
            startActivity(Intent(this, SendInviteActivity::class.java))
        })
    }

    private fun enterCodeButtonAction() {
        buttonToActivityEnterInvite.setOnClickListener({
            startActivity(Intent(this, EnterInviteActivity::class.java))
        })

    }

    private fun profileActivityAction() {
        buttonToChangeProfile.setOnClickListener({
            startActivity(Intent(this@MapActivity, ProfileActivity::class.java))
        })
    }

    private fun setupAddOnlineUserToDatabaseHelper() {
        if (FirebaseDatabase.getInstance().reference != null && FirebaseAuth.getInstance().currentUser != null) {
            databaseOnlineUserAction = DatabaseOnlineUserAction()
            databaseOnlineUserAction!!.addOnlineUserToDatabase()
        }
    }

    override fun onResume() {
        super.onResume()

        locationPermissionAction() // Update location point when user return to app.
        val response = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this)
        if (response != ConnectionResult.SUCCESS) {
            GoogleApiAvailability.getInstance().getErrorDialog(this, response, 1).show()
        }
    }

    override fun onTrimMemory(level: Int) {
        if (level == ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN) {// Detect when UI is hidden.
            try {
                locationOperations!!.fusedLocationClient.removeLocationUpdates(locationOperations!!.locationCallback)
            } catch (exception: Exception) {
                exception.printStackTrace()
            } catch (exception: UninitializedPropertyAccessException) {
                exception.printStackTrace()
            }
        }
        super.onTrimMemory(level)
    }

    override fun onResumeFragments() {
        super.onResumeFragments()
        if (isPermissionDenied) {
            databaseOnlineUserAction!!.logoutUser()
            Tools.startNewActivityWithoutPrevious(this, LoginActivity::class.java)
            Toast.makeText(this, getString(R.string.permissionNotGrantedInformation), Toast.LENGTH_LONG).show()
            isPermissionDenied = false
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.action_logout -> {
                val alert = Tools.makeAlertDialogBuilder(this, getString(R.string.logout), getString(R.string.logoutMessage))
                alert.setPositiveButton(R.string.ok) { dialog, whichButton ->
                    databaseOnlineUserAction!!.logoutUser()
                    removeOldListeners()
                    Tools.startNewActivityWithoutPrevious(this, LoginActivity::class.java)
                }.setNegativeButton(R.string.cancel) { dialog, wchichButton -> }.create().show()

            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun removeOldListeners(){
        finderUserConnection?.removeChildEventListeners()
        locationFirebaseMarkerAction?.removeValueEventListeners()
        notificationMethods?.removeValueEventListeners()
        locationOperations?.fusedLocationClient?.removeLocationUpdates(locationOperations?.locationCallback)
    }

    fun getActivity() : Activity = this

    fun getMap() : GoogleMap = googleMap!!

    fun updateChangeUserNameInRecycler(userInformation : UserMarkerInformationModel){
        recyclerViewAction?.updateChangeUserNameInRecycler(userInformation)
    }

    fun userLocationAction(user : User){
        locationFirebaseMarkerAction?.userLocationAction(user.user_id!!, recyclerViewAction!!, progressBarRelative!!)
    }

    fun goToThisMarker(clickedUser: UserMarkerInformationModel){
        locationFirebaseMarkerAction?.goToThisMarker(clickedUser)
    }

    fun addCurrentUserMarkerAndRemoveOld(lastLocation: Location){
        locationFirebaseMarkerAction?.addCurrentUserMarkerAndRemoveOld(lastLocation, recyclerViewAction!!, progressBarRelative!!)
    }
}
