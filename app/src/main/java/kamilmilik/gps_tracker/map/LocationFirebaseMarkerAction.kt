package kamilmilik.gps_tracker.map

import android.location.Location
import android.util.Log
import android.view.View
import android.widget.RelativeLayout
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import kamilmilik.gps_tracker.R
import kamilmilik.gps_tracker.utils.Constants
import kamilmilik.gps_tracker.utils.Tools
import kamilmilik.gps_tracker.models.*
import java.text.DecimalFormat
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import com.google.firebase.database.ValueEventListener




/**
 * Created by kamil on 25.02.2018.
 */
class LocationFirebaseMarkerAction(private var mapActivity: MapActivity) : BasicListenerContent() {
    private val TAG: String = LocationFirebaseMarkerAction::class.java.simpleName

    private var currentMarkerPosition: LatLng? = null

    private var markersMap = HashMap<UserMarkerInformationModel, Marker>()

    private var currentUserLocation = Location("")

    private var userWhoChangeLocation: TrackingModel? = null

    private var workCounterForNoFriendsUser: AtomicInteger? = AtomicInteger(0)

    private var previousUserMarkerInformation: UserMarkerInformationModel? = null

    private var isMyLocationAdded = false

    private var isUsersLocationsAdded = false

    fun addCurrentUserMarkerAndRemoveOld(lastLocation: Location, recyclerViewAction: RecyclerViewAction, progressBar: RelativeLayout) {
        Log.i(TAG, "addCurrentUserMarkerAndRemoveOld")
        val currentUser = FirebaseAuth.getInstance().currentUser
        val reference = FirebaseDatabase.getInstance().reference
        val locationsDatabaseReference = reference.child(Constants.DATABASE_LOCATIONS)

        if (currentUser != null) {// Prevent if user click logout.
            val userMarkerInformation = UserMarkerInformationModel(currentUser.email!!, currentUser.displayName!!, currentUser.uid)

            saveLocationToDatabase(currentUser, locationsDatabaseReference, lastLocation)

            deletePreviousMarker(userMarkerInformation)

            val currentMarker = createMarker(currentUser, lastLocation)
            markersMap.put(userMarkerInformation, currentMarker)
            currentUserLocation = Tools.createLocationVariable(currentMarker.position)

            updateMarkerSnippetDistance(userMarkerInformation, currentUserLocation)

            currentMarkerPosition = currentMarker.position

            recyclerViewAction.updateRecyclerView()

            onGetMyLocationSuccess()
            onSuccessGetLocations(progressBar)

            progressBarDismissAction(reference, currentUser, progressBar)
        }
    }

    private fun saveLocationToDatabase(currentUser: FirebaseUser, locationsDatabaseReference: DatabaseReference, lastLocation: Location){
        locationsDatabaseReference.child(currentUser.uid)
                .setValue(TrackingModel(currentUser.uid,
                        currentUser.email!!,
                        lastLocation.latitude.toString(),
                        lastLocation.longitude.toString(),
                        currentUser.displayName!!))
    }

    private fun createMarker(currentUser: FirebaseUser, lastLocation: Location): Marker {
        return mapActivity.getMap().addMarker(MarkerOptions()
                .position(LatLng(lastLocation.latitude, lastLocation.longitude))
                .title(currentUser.displayName))
    }


    private fun checkIfUserExistInDatabase(query : Query, progressBar: RelativeLayout){
        query.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError?) {}

            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                if(!dataSnapshot!!.exists()){
                    Log.i(TAG,"onDataChange() no friends")
                    Log.i(TAG,"onDataChange() increment workCounterForNoFirendsUser")
                    workCounterForNoFriendsUser!!.incrementAndGet()
                    dismissProgressBar(progressBar)
                }
                putValueEventListenersToMap(query, this)
            }
        })
    }


    fun userLocationAction(userId: String, recyclerViewAction: RecyclerViewAction, progressBar: RelativeLayout) {
        Log.i(TAG, "userLocationAction")

        val reference = FirebaseDatabase.getInstance().reference
        val query = reference.child(Constants.DATABASE_LOCATIONS)
                .orderByChild(Constants.DATABASE_USER_ID_FIELD)
                .equalTo(userId)
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                Log.i(TAG, "onDataChange in Locations listener " + dataSnapshot.toString())
                for (singleSnapshot in dataSnapshot!!.children) {
                    userWhoChangeLocation = singleSnapshot.getValue(TrackingModel::class.java)

                    val userEmail = userWhoChangeLocation!!.email
                    val userName = userWhoChangeLocation!!.user_name!!
                    val userId = userWhoChangeLocation!!.user_id!!

                    if (userWhoChangeLocation != null && FirebaseAuth.getInstance().currentUser != null) {
                        recyclerViewAction.updateChangeUserNameInRecycler(UserMarkerInformationModel(userEmail, userName, userId))

                        updateUserNameIfChange(reference)

                        val userMarkerInformation = UserMarkerInformationModel(userEmail, userName, userId)
                        val locationOfTheUserWhoChangeLocation = LatLng(userWhoChangeLocation!!.lat!!.toDouble(), userWhoChangeLocation!!.lng!!.toDouble())

                        deletePreviousMarker(userMarkerInformation)

                        val firstDistanceSecondMeasure = Tools.calculateDistanceBetweenTwoPoints(currentUserLocation, Tools.createLocationVariable(locationOfTheUserWhoChangeLocation))
                        val markerFollowingUser = mapActivity.getMap().addMarker(MarkerOptions()
                                .position(locationOfTheUserWhoChangeLocation)
                                .title(userName)
                                .snippet(mapActivity.getActivity().getString(R.string.distance) + " " + DecimalFormat("#.#").format(firstDistanceSecondMeasure.first) + firstDistanceSecondMeasure.second)
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)))
                        markersMap.put(userMarkerInformation, markerFollowingUser)
                        previousUserMarkerInformation = userMarkerInformation

                        recyclerViewAction.updateRecyclerView()

                        Log.i(TAG, "onDataChange() increment workCounter")

                        // I use functions instead AtomicInteger to dismiss dialog since, onDataChange could run multiple times and then could unnecessarily increment counter.
                        onGetUsersLocationSuccess()
                        onSuccessGetLocations(progressBar)

                        dismissProgressBar(progressBar)
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

    private fun progressBarDismissAction(reference: DatabaseReference, currentUser : FirebaseUser, progressBar: RelativeLayout){
        var query = reference.child(Constants.DATABASE_FOLLOWERS)
                .orderByKey()
                .equalTo(currentUser!!.uid)
        checkIfUserExistInDatabase(query, progressBar)
        query = reference.child(Constants.DATABASE_FOLLOWING)
                .orderByKey()
                .equalTo(currentUser!!.uid)
        checkIfUserExistInDatabase(query, progressBar)

        dismissProgressBar(progressBar)
    }

    private fun dismissProgressBar(progressBar: RelativeLayout) {
        Log.i(TAG,"dismissProgressBar() workCounterForNoFriendsUser = " + workCounterForNoFriendsUser)
        if (workCounterForNoFriendsUser!!.compareAndSet(2, 0)) {
            progressBar.visibility = View.GONE
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
                        val user = childSingleSnapshot.child(Constants.DATABASE_USER_FIELD).getValue(User::class.java)
                        if (user!!.user_id == userId) {
                            Log.i(TAG, "onDataChange() update name" + childSingleSnapshot)
                            val map = HashMap<String, Any>() as MutableMap<String, Any>
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
                val location = Tools.createLocationVariable(value.position)
                val firstDistanceSecondMeasure = Tools.calculateDistanceBetweenTwoPoints(currentUserLocation, location)
                value.hideInfoWindow()
                value.snippet = mapActivity.getActivity().getString(R.string.distance) + " " + DecimalFormat("#.#").format(firstDistanceSecondMeasure.first) + firstDistanceSecondMeasure.second
            }
        }
    }

    fun goToThisMarker(clickedUserMarkerInformation: UserMarkerInformationModel) {
        Log.i(TAG, "goToThisMarker() clicked user email " + clickedUserMarkerInformation)
        val searchedMarker = findMarker(clickedUserMarkerInformation)
        mapActivity.getMap().animateCamera(CameraUpdateFactory.newLatLngZoom(searchedMarker!!.position, Constants.MAP_CAMERA_ZOOM))
        searchedMarker.showInfoWindow()
    }

    // Hash code and equals in UserMarkerInformationModel is important here.
    private fun findMarker(searchedUserMarkerInformationKey: UserMarkerInformationModel): Marker? = markersMap.getValue(searchedUserMarkerInformationKey)

    private fun onGetMyLocationSuccess() {
        isMyLocationAdded = true
    }

    fun onGetUsersLocationSuccess() {
        isUsersLocationsAdded = true
    }

    fun onSuccessGetLocations(progressBar: RelativeLayout) {
        if(isMyLocationAdded && isUsersLocationsAdded){
            progressBar.visibility = View.GONE
            isMyLocationAdded = false
            isUsersLocationsAdded = false
        }
    }
}