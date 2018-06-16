package kamilmilik.licencjat_gps_kid.map

import android.app.ProgressDialog
import android.content.Context
import android.location.Location
import android.util.Log
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import kamilmilik.licencjat_gps_kid.R
import kamilmilik.licencjat_gps_kid.utils.Constants
import kamilmilik.licencjat_gps_kid.utils.Tools
import kamilmilik.licencjat_gps_kid.models.*
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import com.google.firebase.database.ValueEventListener




/**
 * Created by kamil on 25.02.2018.
 */
class LocationFirebaseMarkerAction(var mapAdapter: GoogleMap, var context: Context) : BasicListenerContent() {
    private val TAG: String = LocationFirebaseMarkerAction::class.java.simpleName

    private var currentMarkerPosition: LatLng? = null

    private var markersMap = HashMap<UserMarkerInformationModel, Marker>()

    private var currentUserLocation = Location("")

    private var userWhoChangeLocation: TrackingModel? = null

    private var workCounterForNoFriendsUser: AtomicInteger? = AtomicInteger(0)

    private var previousUserMarkerInformation: UserMarkerInformationModel? = null

    private var isMyLocationAdded = false

    private var isUsersLocationsAdded = false

    fun addCurrentUserMarkerAndRemoveOld(lastLocation: Location, recyclerViewAction: RecyclerViewAction, progressDialog: ProgressDialog) {
        Log.i(TAG, "addCurrentUserMarkerAndRemoveOld")
        var currentUser = FirebaseAuth.getInstance().currentUser
        var reference = FirebaseDatabase.getInstance().reference
        var locations = reference.child(Constants.DATABASE_LOCATIONS)

        if (currentUser != null) {//prevent if user click logout to not update locationOfUserWhoChangeIt
            var userMarkerInformation = UserMarkerInformationModel(currentUser.email!!, currentUser!!.displayName!!, currentUser.uid)
            locations.child(currentUser!!.uid)
                    .setValue(TrackingModel(currentUser.uid,
                            currentUser!!.email!!,
                            lastLocation.latitude.toString(),
                            lastLocation.longitude.toString(),
                            currentUser!!.displayName!!,
                            System.currentTimeMillis()))

            deletePreviousMarker(userMarkerInformation)

            var currentMarker = mapAdapter!!.addMarker(MarkerOptions()
                    .position(LatLng(lastLocation.latitude!!, lastLocation.longitude!!))
                    .title(currentUser!!.displayName))
            markersMap.put(userMarkerInformation, currentMarker)
            currentUserLocation = Tools.createLocationVariable(currentMarker.position)

            updateMarkerSnippetDistance(userMarkerInformation, currentUserLocation)

            currentMarkerPosition = currentMarker.position

            recyclerViewAction.updateRecyclerView()

            onGetMyLocationSuccess()
            onSuccessGetLocations(progressDialog)

            progressDialogDismissAction(reference, currentUser, progressDialog)
        }
    }

    private fun checkIfUserExistInDatabase(query : Query){
        query.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError?) {}

            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                if(!dataSnapshot!!.exists()){
                    Log.i(TAG,"onDataChange() no friends")
                    Log.i(TAG,"onDataChange() increment workCounterForNoFirendsUser")
                    workCounterForNoFriendsUser!!.incrementAndGet()
                }
                putValueEventListenersToMap(query, this)
            }
        })
    }


    fun userLocationAction(userId: String, recyclerViewAction: RecyclerViewAction, progressDialog: ProgressDialog) {
        Log.i(TAG, "userLocationAction")

        var reference = FirebaseDatabase.getInstance().reference
        var query = reference.child(Constants.DATABASE_LOCATIONS)
                .orderByChild(Constants.DATABASE_USER_ID_FIELD)
                .equalTo(userId)
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                Log.i(TAG, "onDataChange in Locations listener " + dataSnapshot.toString())
                for (singleSnapshot in dataSnapshot!!.children) {
                    userWhoChangeLocation = singleSnapshot.getValue(TrackingModel::class.java)

                    var userEmail = userWhoChangeLocation!!.email
                    var userName = userWhoChangeLocation!!.user_name!!
                    var userId = userWhoChangeLocation!!.user_id!!

                    if (userWhoChangeLocation != null && FirebaseAuth.getInstance().currentUser != null) {
                        recyclerViewAction.updateChangeUserNameInRecycler(UserMarkerInformationModel(userEmail, userName, userId))

                        updateUserNameIfChange(reference)

                        var userMarkerInformation = UserMarkerInformationModel(userEmail, userName, userId)
                        var locationOfTheUserWhoChangeLocation = LatLng(userWhoChangeLocation!!.lat!!.toDouble(), userWhoChangeLocation!!.lng!!.toDouble())

                        deletePreviousMarker(userMarkerInformation)

                        var firstDistanceSecondMeasure = Tools.calculateDistanceBetweenTwoPoints(currentUserLocation, Tools.createLocationVariable(locationOfTheUserWhoChangeLocation))
                        var markerFollowingUser = mapAdapter!!.addMarker(MarkerOptions()
                                .position(locationOfTheUserWhoChangeLocation)
                                .title(userName)
                                .snippet(context.getString(R.string.distance) + " " + DecimalFormat("#.#").format(firstDistanceSecondMeasure.first) + firstDistanceSecondMeasure.second
                                        + context.getString(R.string.timeLocationReport) + " " + SimpleDateFormat("MMM dd,yyyy HH:mm").format(Date(userWhoChangeLocation!!.time!!)))
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)))
                        markersMap.put(userMarkerInformation, markerFollowingUser)
                        previousUserMarkerInformation = userMarkerInformation

                        recyclerViewAction.updateRecyclerView()

                        Log.i(TAG, "onDataChange() increment workCounter")

                        // I use functions instead AtomicInteger to dismiss dialog since, onDataChange could run multiple times and then could unnecessarily increment counter
                        onGetUsersLocationSuccess()
                        onSuccessGetLocations(progressDialog)

                        dismissProgressDialog(progressDialog)
                    }
                }
                putValueEventListenersToMap(query, this)
            }

            override fun onCancelled(databaseError: DatabaseError?) {}
        })
    }

    private fun deletePreviousMarker(userMarkerInformation: UserMarkerInformationModel){
        for ((key, value) in markersMap) {
            if (key.email == userMarkerInformation.email) {
                markersMap[key]!!.remove()
            }
        }
    }

    private fun progressDialogDismissAction(reference: DatabaseReference, currentUser : FirebaseUser, progressDialog: ProgressDialog){
        workCounterForNoFriendsUser!!.incrementAndGet()
        Log.i(TAG,"progressDialogDismissAction() increment workCounter and WorkCounterForNoFriendsUSer")
        var query = reference.child(Constants.DATABASE_FOLLOWERS)
                .orderByKey()
                .equalTo(currentUser!!.uid)
        checkIfUserExistInDatabase(query)
        query = reference.child(Constants.DATABASE_FOLLOWING)
                .orderByKey()
                .equalTo(currentUser!!.uid)
        checkIfUserExistInDatabase(query)

        dismissProgressDialog(progressDialog)
    }

    private fun dismissProgressDialog(progressDialog: ProgressDialog) {
        if (workCounterForNoFriendsUser!!.compareAndSet(3, 0)) {
            progressDialog.dismiss()
        }
    }

    private fun updateUserNameIfChange(reference: DatabaseReference) {
        var query = reference.child(Constants.DATABASE_FOLLOWERS).orderByKey()
                .equalTo(userWhoChangeLocation!!.user_id)
        updateName(query, FirebaseAuth.getInstance().currentUser!!.uid, FirebaseAuth.getInstance().currentUser!!.displayName!!)

        query = reference.child(Constants.DATABASE_FOLLOWING).orderByKey()
                .equalTo(FirebaseAuth.getInstance().currentUser!!.uid)
        updateName(query, userWhoChangeLocation!!.user_id!!, userWhoChangeLocation!!.user_name!!)

    }

    private fun updateName(query: Query, userId : String, userName : String){
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {}
            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                for (singleSnapshot in dataSnapshot!!.children) {
                    for (childSingleSnapshot in singleSnapshot.children) {
                        var user = childSingleSnapshot.child(Constants.DATABASE_USER_FIELD).getValue(User::class.java)
                        if (user!!.user_id == userId) {
                            Log.i(TAG, "onDataChange() update name" + childSingleSnapshot)
                            var map = HashMap<String, Any>() as MutableMap<String, Any>
                            map.put(Constants.DATABASE_USER_NAME_FIELD, userName)
                            childSingleSnapshot!!.ref.child(Constants.DATABASE_USER_FIELD).updateChildren(map)
                        }
                    }
                }
            }
        })
    }

    private fun updateMarkerSnippetDistance(userToAvoidUpdate: UserMarkerInformationModel, currentUserLocation: Location) {
        for ((key, value) in markersMap) {
            if (key != userToAvoidUpdate) {
                var location = Tools.createLocationVariable(value.position)
                var firstDistanceSecondMeasure = Tools.calculateDistanceBetweenTwoPoints(currentUserLocation, location)
                value.hideInfoWindow()
                value.snippet = context.getString(R.string.distance) + " " + DecimalFormat("#.#").format(firstDistanceSecondMeasure.first) + firstDistanceSecondMeasure.second + context.getString(R.string.timeLocationReport) + " " +  SimpleDateFormat("MMM dd,yyyy HH:mm").format(Date(userWhoChangeLocation!!.time!!))
            }
        }
    }

    fun goToThisMarker(clickedUserMarkerInformation: UserMarkerInformationModel) {
        Log.i(TAG, "goToThisMarker() clicked user email " + clickedUserMarkerInformation)
        var searchedMarker = findMarker(clickedUserMarkerInformation)
        mapAdapter!!.animateCamera(CameraUpdateFactory.newLatLngZoom(searchedMarker!!.position, 12.0f))
        searchedMarker.showInfoWindow()
    }

    //hash code and equals in UserMarkerInformationModel is important here
    private fun findMarker(searchedUserMarkerInformationKey: UserMarkerInformationModel): Marker? = markersMap.getValue(searchedUserMarkerInformationKey)

    fun onGetMyLocationSuccess() {
        isMyLocationAdded = true
    }

    fun onGetUsersLocationSuccess() {
        isUsersLocationsAdded = true
    }

    fun onSuccessGetLocations(progressDialog: ProgressDialog) {
        if(isMyLocationAdded && isUsersLocationsAdded){
            progressDialog.dismiss()
            isMyLocationAdded = false
            isUsersLocationsAdded = false
        }
    }
}