package kamilmilik.licencjat_gps_kid.map

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Menu
import kotlinx.android.synthetic.main.activity_map.*
import android.content.Intent
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.google.android.gms.maps.GoogleMap
import kamilmilik.licencjat_gps_kid.invite.EnterInviteActivity
import kamilmilik.licencjat_gps_kid.invite.SendInviteActivity
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.auth.FirebaseAuth
import android.view.MotionEvent
import kamilmilik.licencjat_gps_kid.utils.LocationOperations
import kamilmilik.licencjat_gps_kid.map.PolygonOperation.DrawPolygon
import kamilmilik.licencjat_gps_kid.online.DatabaseOnlineUserAction
import com.google.firebase.FirebaseApp
import android.app.ProgressDialog
import android.content.ComponentCallbacks2
import com.google.firebase.database.FirebaseDatabase
import kamilmilik.licencjat_gps_kid.ApplicationActivity
import kamilmilik.licencjat_gps_kid.R
import kamilmilik.licencjat_gps_kid.login.LoginActivity
import kamilmilik.licencjat_gps_kid.map.PolygonOperation.notification.Notification
import kamilmilik.licencjat_gps_kid.profile.ProfileActivity
import kamilmilik.licencjat_gps_kid.utils.Constants
import kamilmilik.licencjat_gps_kid.utils.Tools
import kamilmilik.licencjat_gps_kid.map.adapter.google.GoogleGeoMap
import kamilmilik.licencjat_gps_kid.map.adapter.GeoMap


class MapActivity : ApplicationActivity(), kamilmilik.licencjat_gps_kid.map.adapter.GeoOnMapReadyCallback {

    private val TAG: String = MapActivity::class.java.simpleName

    var geoMap: GeoMap? = null

    //Firebase
    private var databaseOnlineUserAction: DatabaseOnlineUserAction? = null
    //view

    //permission
    private var isPermissionDenied = false
    //Location
    private var locationOperations: LocationOperations? = null

    private var finderUserConnection: FinderUserConnection? = null

    private var locationFirebaseMarkerAction: LocationFirebaseMarkerAction? = null
    //maps
    private var googleMap: GoogleMap? = null

    private var progressDialog: ProgressDialog? = null

    var recyclerViewAction: RecyclerViewAction? = null

    var buttonClickedPolygonAction: Boolean? = false // to detect map is movable

    var drawPolygon: DrawPolygon? = null

    private var notificationMethods : Notification? = null

    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)
        Tools.setupToolbar(this, false).title = getString(R.string.mapActivityName)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseApp.initializeApp(applicationContext)//I must called this first otherwise foreground/background service is not running since without it get nullPointerException

        setContentView(R.layout.activity_map)

        geoMap = GoogleGeoMap()
        geoMap!!.getMapAsync(this, this)

        Tools.goToAddIgnoreBatteryOptimizationSettings(this)

        generateCodeButtonAction()
        enterCodeButtonAction()
        profileActivityAction()

        setupAddOnlineUserToDatabaseHelper()
    }


    @SuppressLint("MissingPermission")
    override fun onMapReady(geoMap: GeoMap) {
        this.googleMap = (this.geoMap as GoogleGeoMap).googleMap

        locationFirebaseMarkerAction = LocationFirebaseMarkerAction(this.googleMap!!, this)
        recyclerViewAction = RecyclerViewAction(this, locationFirebaseMarkerAction!!)
        recyclerViewAction!!.setupRecyclerView()
        progressDialog = ProgressDialog.show(this, getString(R.string.waitInformation), getString(R.string.waitMessage), true)
        finderUserConnection = FinderUserConnection(this, progressDialog!!, recyclerViewAction!!, locationFirebaseMarkerAction!!)
        finderUserConnection!!.findFollowersConnectionAndUpdateRecyclerView()
        locationOperations = LocationOperations(this, progressDialog!!, locationFirebaseMarkerAction!!, recyclerViewAction!!)

        drawPolygon = DrawPolygon(this.googleMap!!, this)
        drawPolygonButtonAction()
        editPolygonButtonAction()

        locationPermissionAction()

        runCheckAreaAction()
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
        if (Tools.checkApkVersion()) {
            if (Tools.checkPermissionGranted(this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                locationOperations!!.getLocation()
                this.googleMap!!.isMyLocationEnabled = true
            } else {
                Tools.checkLocationPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
            }
        } else {
            locationOperations!!.getLocation()
            this.googleMap!!.isMyLocationEnabled = true
        }
    }

    private fun editPolygonButtonAction() {
        editPolygonButton.setOnClickListener {
            buttonClickedPolygonAction = !buttonClickedPolygonAction!!
            drawButton.isEnabled = !buttonClickedPolygonAction!!
            if (!buttonClickedPolygonAction!!) {
                drawPolygon!!.hideAllMarkers()
            } else {
                drawPolygon!!.showAllMarkers()
            }
        }
    }

    private fun drawPolygonButtonAction() {
        drawButton.setOnClickListener {
            drawButton.isEnabled = false
            draggable.setOnTouchListener(object : View.OnTouchListener {
                override fun onTouch(v: View?, motionEvent: MotionEvent?): Boolean {
                    drawPolygon!!.onTouchAction(motionEvent, draggable)
                    drawButton.isEnabled = true
                    return true
                }
            })
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            Constants.MY_PERMISSION_REQUEST_CODE -> {
                if (Tools.checkIsPermissionGrantedInRequestPermission(grantResults)) {
                    if (Tools.checkPermissionGranted(this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
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
            var intent = Intent(this, SendInviteActivity::class.java)
            startActivity(intent)
        })
    }

    private fun enterCodeButtonAction() {
        buttonToActivityEnterInvite.setOnClickListener({
            var intent = Intent(this, EnterInviteActivity::class.java)
            startActivity(intent)
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

        var response = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this)
        if (response != ConnectionResult.SUCCESS) {
           GoogleApiAvailability.getInstance().getErrorDialog(this, response, 1).show()
        }
    }


    override fun onDestroy() {
        progressDialog!!.dismiss()
        super.onDestroy()
    }

    override fun onTrimMemory(level: Int) {
        if (level == ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN) {//detect when UI is hidden
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
                databaseOnlineUserAction!!.logoutUser()
                // Remove old listener when user sign out.
                finderUserConnection?.removeChildEventListeners()
                locationFirebaseMarkerAction?.removeValueEventListeners()
                notificationMethods?.removeValueEventListeners()
                locationOperations?.fusedLocationClient?.removeLocationUpdates(locationOperations?.locationCallback)
                Tools.startNewActivityWithoutPrevious(this, LoginActivity::class.java)
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
