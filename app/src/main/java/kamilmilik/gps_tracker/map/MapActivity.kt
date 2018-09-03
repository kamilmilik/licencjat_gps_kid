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
import android.util.Log
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.firebase.database.*
import kamilmilik.gps_tracker.ApplicationActivity
import kamilmilik.gps_tracker.R
import kamilmilik.gps_tracker.login.LoginActivity
import kamilmilik.gps_tracker.map.PolygonOperation.notification.Notification
import kamilmilik.gps_tracker.models.User
import kamilmilik.gps_tracker.models.UserBasicInfo
import kamilmilik.gps_tracker.profile.ProfileActivity
import kamilmilik.gps_tracker.utils.*
import kamilmilik.gps_tracker.utils.Constants.CHANGE_NAME_ACTIVITY_RESULT
import kamilmilik.gps_tracker.utils.Constants.CHANGE_NAME_ACTIVITY_RESULT_REQUEST
import kotlinx.android.synthetic.main.progress_bar.*

class MapActivity : ApplicationActivity(), OnMapReadyCallback {

    private val TAG: String = MapActivity::class.java.simpleName

    private var databaseOnlineUserAction: DatabaseOnlineUserAction? = null

    private var isPermissionDenied = false

    private var locationOperations: LocationOperations? = null

    private var notifyOtherUsersChanges: NotifyOtherUsersChanges? = null

    private var locationFirebaseMarkerAction: LocationFirebaseMarkerAction? = null

    private lateinit var googleMap: GoogleMap

    private var recyclerViewAction: RecyclerViewAction? = null

    private var drawPolygon: DrawPolygon? = null

    private var notificationMethods: Notification? = null

    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)
        Tools.setupToolbar(this, false).title = getString(R.string.mapActivityName)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        FirebaseApp.initializeApp(applicationContext)
        progressBarRelative?.visibility = View.VISIBLE

        (supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment).getMapAsync(this)

        BatteryOptimizationUtils.ignoreBatteryOptimizationSettings(this)

        generateCodeButtonAction()
        enterCodeButtonAction()
        profileActivityAction()

        setupAddOnlineUserToDatabaseHelper()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
        googleMap.uiSettings.isCompassEnabled = false

        mapTypeAction()
        initialize()
        switchMapTypeAction()

        recyclerViewAction?.setupRecyclerView()
        notifyOtherUsersChanges?.findUsersConnectionUpdateRecyclerViewOrDeleteRemovedUserData()


        drawPolygonButtonAction()
        editPolygonButtonAction()

        locationPermissionAction()

        runCheckAreaAction()
    }

    private fun mapTypeAction() {
        val mapType = LocationUtils.getMapTypeFromSharedPref(this)
        if (mapType != -1) {
            mapTypeChoose(mapType)
        }
    }

    private fun initialize() {
        locationFirebaseMarkerAction = LocationFirebaseMarkerAction(this)
        recyclerViewAction = RecyclerViewAction(this)
        locationOperations = LocationOperations(this)
        notifyOtherUsersChanges = NotifyOtherUsersChanges(this)
        drawPolygon = DrawPolygon(this)
    }

    private fun runCheckAreaAction() {
        object : Thread() {
            override fun run() {
                FirebaseApp.initializeApp(applicationContext)
                notificationMethods = Notification(this@MapActivity)
                notificationMethods?.notificationAction(false)
            }
        }.start()

        PolygonAndUserCounter(this).polygonAndUserCounterAction()

    }

    fun setTooMuchWorkOnUi(isTooMuchWorkOnUi: Boolean) {
        notificationMethods?.isTooMuchWorkOnUi?.set(isTooMuchWorkOnUi)
    }

    private fun locationPermissionAction() {
        locationOperations?.let { locationOperations ->
            if (PermissionsUtils.checkApkVersion()) {
                if (PermissionsUtils.checkPermissionGranted(this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                    locationOperations.getLocation()
                    this.googleMap.isMyLocationEnabled = true
                } else {
                    PermissionsUtils.checkLocationPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                }
            } else {
                locationOperations.getLocation()
                this.googleMap.isMyLocationEnabled = true
            }
        }
    }

    private fun editPolygonButtonAction() {
        editPolygonButton.setOnClickListener {
            drawPolygon?.markersMap?.let { markersMap ->
                if (!markersMap.isEmpty()) {
                    if (editPolygonButton.textColors.defaultColor == resources.getColor(R.color.blackColor)) {
                        editPolygonButton.setTextColor(Color.GRAY)
                        drawPolygon?.showAllMarkers()
                        drawButton.isEnabled = false
                    } else {
                        editPolygonButton.setTextColor(resources.getColor(R.color.blackColor))
                        drawButton.isEnabled = true
                        drawPolygon?.hideAllMarkers()
                        drawPolygon?.onMarkerDragListener?.saveEditedPolygonToDatabase()
                    }
                }
            }
        }
    }

    private fun drawPolygonButtonAction() {
        drawButton.setOnClickListener {
            if (drawButton.textColors.defaultColor == resources.getColor(R.color.blackColor)) {
                drawButton.setTextColor(Color.GRAY)
                editPolygonButton.isEnabled = false
                draggable.setOnTouchListener { v, motionEvent ->
                    drawPolygon?.onTouchAction(motionEvent, draggable)
                    drawButton.setTextColor(resources.getColor(R.color.blackColor))
                    editPolygonButton.isEnabled = true
                    true
                }
            } else {
                draggable.setOnTouchListener(null)
                editPolygonButton.isEnabled = true
                drawButton.setTextColor(resources.getColor(R.color.blackColor))
            }
        }
    }

    private fun switchMapTypeAction() {
        mapSatelliteButton.setOnClickListener {
            googleMap.let { googleMap ->
                if (googleMap.mapType == GoogleMap.MAP_TYPE_NORMAL) {
                    googleMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
                    mapSatelliteButton.setImageResource(R.drawable.ic_map)
                } else if (googleMap.mapType == GoogleMap.MAP_TYPE_SATELLITE) {
                    googleMap.mapType = GoogleMap.MAP_TYPE_NORMAL
                    mapSatelliteButton.setImageResource(R.drawable.ic_map_satellite)
                }
                LocationUtils.saveMapTypeToSharedPref(this, googleMap.mapType)

            }
        }
    }

    private fun mapTypeChoose(mapType: Int) {
        if (mapType == GoogleMap.MAP_TYPE_SATELLITE) {
            mapSatelliteButton.setImageResource(R.drawable.ic_map)
        } else if (mapType == GoogleMap.MAP_TYPE_NORMAL) {
            mapSatelliteButton.setImageResource(R.drawable.ic_map_satellite)
        }
        googleMap.mapType = mapType
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            Constants.LOCATION_PERMISSION_REQUEST_CODE -> {
                if (PermissionsUtils.checkIsPermissionGrantedInRequestPermission(grantResults)) {
                    if (PermissionsUtils.checkPermissionGranted(this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                        locationOperations?.getLocation()
                        googleMap.isMyLocationEnabled = true
                    }
                } else {
                    isPermissionDenied = true
                    databaseOnlineUserAction?.logoutUser()
                    Tools.startNewActivityWithoutPrevious(this, LoginActivity::class.java)
                    Toast.makeText(this, getString(R.string.permissionDenied), Toast.LENGTH_LONG).show()
                }
                return
            }
        }
    }

    private fun generateCodeButtonAction() {
        buttonToActivityGenerateCode.setOnClickListener {
            startActivity(Intent(this, SendInviteActivity::class.java))
        }
    }

    private fun enterCodeButtonAction() {
        buttonToActivityEnterInvite.setOnClickListener {
            startActivity(Intent(this, EnterInviteActivity::class.java))
        }

    }

    private fun profileActivityAction() {
        buttonToChangeProfile.setOnClickListener {
            startActivityForResult(Intent(this@MapActivity, ProfileActivity::class.java), 1)
        }
    }

    private fun setupAddOnlineUserToDatabaseHelper() {
        if (FirebaseDatabase.getInstance().reference != null && FirebaseAuth.getInstance().currentUser != null) {
            databaseOnlineUserAction = DatabaseOnlineUserAction()
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
                locationOperations?.fusedLocationClient?.removeLocationUpdates(locationOperations?.locationCallback)
            } catch (exception: Exception) {
                exception.printStackTrace()
            } catch (exception: UninitializedPropertyAccessException) {
                exception.printStackTrace()
            }
        }
        super.onTrimMemory(level)
    }

    override fun onPause() {
        if (locationOperations != null && locationOperations?.fusedLocationClient != null && locationOperations?.locationCallback != null) {
            locationOperations?.fusedLocationClient?.removeLocationUpdates(locationOperations?.locationCallback)

        }
        super.onPause()
    }

    override fun onDestroy() {
        removeOldListeners()
        super.onDestroy()
    }

    override fun onResumeFragments() {
        super.onResumeFragments()
        if (isPermissionDenied) {
            databaseOnlineUserAction?.logoutUser()
            Tools.startNewActivityWithoutPrevious(this, LoginActivity::class.java)
            Toast.makeText(this, getString(R.string.permissionNotGrantedInformation), Toast.LENGTH_LONG).show()
            isPermissionDenied = false
        }
    }

    override fun onBackPressed() {
        finishAffinity()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_logout -> {
                val alert = Tools.makeAlertDialogBuilder(this, getString(R.string.logout), getString(R.string.logoutMessage))
                alert.setPositiveButton(R.string.ok) { dialog, whichButton ->
                    databaseOnlineUserAction?.logoutUser()
                    removeOldListeners()
                    Tools.startNewActivityWithoutPrevious(this, LoginActivity::class.java)
                }.setNegativeButton(R.string.cancel) { dialog, whichButton -> }.create().show()

            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun removeOldListeners() {
        notifyOtherUsersChanges?.removeChildEventListeners()
        locationFirebaseMarkerAction?.removeValueEventListeners()
        notificationMethods?.removeValueEventListeners()
        locationOperations?.fusedLocationClient?.removeLocationUpdates(locationOperations?.locationCallback)
    }

    fun removeAllEventListenersForGivenUserId(userIdToRemove: String) {
        notifyOtherUsersChanges?.removeChildEventListenersForGivenUserId(userIdToRemove)
        locationFirebaseMarkerAction?.removeValueEventListenersForGivenUserId(userIdToRemove)
        notificationMethods?.removeValueEventListenersForGivenUserId(userIdToRemove)
    }

    fun getActivity(): Activity = this

    fun getMap(): GoogleMap = googleMap

    fun updateChangeOthersUserNameInRecycler(userInformation: UserBasicInfo) {
        recyclerViewAction?.updateChangeUserNameInRecycler(userInformation)
    }

    fun removeUserFromRecycler(userIdToRemove: String) {
        recyclerViewAction?.removeUserFromRecyclerAndNotifyDataSetChanged(userIdToRemove)
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        data?.let {
            if (requestCode == CHANGE_NAME_ACTIVITY_RESULT_REQUEST) { // User change name in other activity, so change also in this.
                if (resultCode == Activity.RESULT_OK) {
                    val isChangeUserName = data.getBooleanExtra(CHANGE_NAME_ACTIVITY_RESULT, false)
                    if (isChangeUserName) {
                        FirebaseAuth.getInstance().currentUser?.let { currentUser ->
                            ObjectsUtils.safeLet(currentUser.uid, currentUser.email, currentUser.displayName) { userId, userEmail, userName ->
                                val userMarkerInformation = UserBasicInfo(userId, userEmail, userName)
                                recyclerViewAction?.changeUserNameAndNotifyDataSetChanged(userMarkerInformation)
                            }
                        }
                    }
                }
            }
        }
    }

    fun userLocationAction(user: User) {
        ObjectsUtils.safeLet(user.user_id, recyclerViewAction, progressBarRelative) { userId, recyclerView, progressBar ->
            locationFirebaseMarkerAction?.userLocationAction(userId, recyclerView, progressBar)
        }
    }

    fun goToThisMarker(clickedUser: UserBasicInfo) {
        locationFirebaseMarkerAction?.goToThisMarker(clickedUser)
    }

    fun removeMarkerFromMapForGivenUser(userBasicInfo: UserBasicInfo) {
        try {
            locationFirebaseMarkerAction?.findMarker(userBasicInfo)?.remove()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    fun addCurrentUserMarkerAndRemoveOld(lastLocation: Location) {
        ObjectsUtils.safeLet(lastLocation, recyclerViewAction, progressBarRelative) { location, recyclerView, progressBar ->
            locationFirebaseMarkerAction?.addCurrentUserMarkerAndRemoveOld(location, recyclerView, progressBar)
        }
    }

}
