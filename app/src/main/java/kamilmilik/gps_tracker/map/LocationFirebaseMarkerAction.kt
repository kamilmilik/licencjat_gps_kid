package kamilmilik.gps_tracker.map

import android.location.Location
import android.util.Log
import android.view.View
import android.widget.RelativeLayout
import android.widget.Toast
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import kamilmilik.gps_tracker.R
import kamilmilik.gps_tracker.utils.Constants
import kamilmilik.gps_tracker.models.*
import java.text.DecimalFormat
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import com.google.firebase.database.ValueEventListener
import kamilmilik.gps_tracker.utils.LocationUtils
import kamilmilik.gps_tracker.utils.ObjectsUtils


/**
 * Created by kamil on 25.02.2018.
 */
class LocationFirebaseMarkerAction(private var mapActivity: MapActivity) : BasicListenerContent() {
    private val TAG: String = LocationFirebaseMarkerAction::class.java.simpleName

    private var currentMarkerPosition: LatLng? = null

    private var markersMap = HashMap<UserBasicInfo, Marker>()

    private var currentUserLocation = Location("")

    private var userWhoChangeLocation: TrackingModel? = null

    private var workCounterForNoFriendsUser: AtomicInteger? = AtomicInteger(0)

    private var previousUserMarkerInformation: UserBasicInfo? = null

    private var isMyLocationAdded = false

    private var isUsersLocationsAdded = false

    fun addCurrentUserMarkerAndRemoveOld(lastLocation: Location, recyclerViewAction: RecyclerViewAction, progressBar: RelativeLayout) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val reference = FirebaseDatabase.getInstance().reference
        val locationsDatabaseReference = reference.child(Constants.DATABASE_LOCATIONS)

        currentUser?.let {
            ObjectsUtils.safeLetFirebaseUser(currentUser) { uid, email, name ->

                val userMarkerInformation = UserBasicInfo(uid, email, name)

                saveLocationToDatabase(currentUser, locationsDatabaseReference, lastLocation)

                deletePreviousMarker(userMarkerInformation)

                createMarker(currentUser, lastLocation)?.let { currentMarker ->

                    markersMap.put(userMarkerInformation, currentMarker)
                    currentUserLocation = LocationUtils.createLocationVariable(currentMarker.position)

                    updateMarkerSnippetDistance(userMarkerInformation, currentUserLocation)

                    currentMarkerPosition = currentMarker.position
                }

                if (currentMarkerPosition == null) {
                    recyclerViewAction.updateRecyclerView()
                }

                onGetMyLocationSuccess()
                onSuccessGetLocations(progressBar)

                progressBarDismissAction(reference, currentUser, progressBar)
            }
        }
    }

    private fun saveLocationToDatabase(currentUser: FirebaseUser, locationsDatabaseReference: DatabaseReference, lastLocation: Location) {
        ObjectsUtils.safeLetTrackingModel(currentUser, lastLocation) { userUid, userEmail, userName, lat, lng ->
            locationsDatabaseReference.child(userUid)
                    .setValue(TrackingModel(userUid, userEmail, lat, lng, userName))
        }
    }

    private fun createMarker(currentUser: FirebaseUser, lastLocation: Location): Marker? {
        return mapActivity.getMap().addMarker(MarkerOptions()
                .position(LatLng(lastLocation.latitude, lastLocation.longitude))
                .title(currentUser.displayName))
    }


    private fun checkIfUserExistInDatabase(userId: String, query: Query, progressBar: RelativeLayout) {
        query.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {}

            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                dataSnapshot?.let {
                    if (!dataSnapshot.exists()) {
                        workCounterForNoFriendsUser?.incrementAndGet()
                        dismissProgressBar(progressBar)
                    }
                    putValueEventListenersToMap(QueryUserModel(userId, query), this)
                }
            }
        })
    }

    fun userLocationAction(userId: String, recyclerViewAction: RecyclerViewAction, progressBar: RelativeLayout) {
        val reference = FirebaseDatabase.getInstance().reference
        val query = reference.child(Constants.DATABASE_LOCATIONS)
                .orderByChild(Constants.DATABASE_USER_ID_FIELD)
                .equalTo(userId)
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                dataSnapshot?.let {
                    for (singleSnapshot in dataSnapshot.children) {
                        userWhoChangeLocation = singleSnapshot.getValue(TrackingModel::class.java)

                        FirebaseAuth.getInstance().currentUser?.let { currentUser ->
                            ObjectsUtils.safeLetTrackingModel(userWhoChangeLocation) { id, email, name, userWhoChangeLocationLatitude, userWhoChangeLocationLongitude ->
                                val userMarkerInformation = UserBasicInfo(id, email, name)
                                recyclerViewAction.updateChangeUserNameInRecycler(userMarkerInformation)

                                updateUserNameIfChange(reference)

                                val locationOfTheUserWhoChangeLocation = LatLng(userWhoChangeLocationLatitude.toDouble(), userWhoChangeLocationLongitude.toDouble())

                                deletePreviousMarker(userMarkerInformation)

                                val distanceMeasure = LocationUtils.calculateDistanceBetweenTwoPoints(currentUserLocation, LocationUtils.createLocationVariable(locationOfTheUserWhoChangeLocation))
                                mapActivity.getMap().addMarker(MarkerOptions()
                                        .position(locationOfTheUserWhoChangeLocation)
                                        .title(name)
                                        .snippet(mapActivity.getActivity().getString(R.string.distance) + " " + DecimalFormat("#.#").format(distanceMeasure.distance) + distanceMeasure.measure)
                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)))?.let { markerFollowingUser ->

                                    markersMap.put(userMarkerInformation, markerFollowingUser)
                                }
                                previousUserMarkerInformation = userMarkerInformation

                                recyclerViewAction.updateRecyclerView()

                                // The method was used instead AtomicInteger to dismiss dialog since, onDataChange could run multiple times and then could unnecessarily increment counter.
                                onGetUsersLocationSuccess()
                                onSuccessGetLocations(progressBar)

                                dismissProgressBar(progressBar)

                            }

                        }

                    }
                }
                putValueEventListenersToMap(QueryUserModel(userId, query), this)
            }

            override fun onCancelled(databaseError: DatabaseError?) {}
        })
    }

    private fun deletePreviousMarker(userMarkerInformation: UserBasicInfo) {
        for ((key, value) in markersMap) {
            if (key.email == userMarkerInformation.email) {
                markersMap[key]?.remove()
            }
        }
    }

    private fun progressBarDismissAction(reference: DatabaseReference, currentUser: FirebaseUser, progressBar: RelativeLayout) {
        val userIdQuery = currentUser.uid
        var query = reference.child(Constants.DATABASE_FOLLOWERS)
                .orderByKey()
                .equalTo(userIdQuery)
        checkIfUserExistInDatabase(userIdQuery, query, progressBar)
        query = reference.child(Constants.DATABASE_FOLLOWING)
                .orderByKey()
                .equalTo(userIdQuery)
        checkIfUserExistInDatabase(userIdQuery, query, progressBar)

        dismissProgressBar(progressBar)
    }

    private fun dismissProgressBar(progressBar: RelativeLayout) {
        workCounterForNoFriendsUser?.let { workCounterForNoFriendsUser ->
            if (workCounterForNoFriendsUser.compareAndSet(2, 0)) {
                progressBar.visibility = View.GONE
            }
        }
    }

    private fun updateUserNameIfChange(reference: DatabaseReference) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        ObjectsUtils.safeLetTrackingModel(userWhoChangeLocation) { userWhoChangeLocationId, userWhoChangeLocationEmail, userWhoChangeLocationName, userWhoChangeLocationLat, userWhoChangeLocationLng ->
            ObjectsUtils.safeLetFirebaseUser(currentUser) { userUid, userEmail, userName ->
                var query = reference.child(Constants.DATABASE_FOLLOWERS).orderByKey()
                        .equalTo(userWhoChangeLocationId)
                updateName(query, userUid, userName)

                query = reference.child(Constants.DATABASE_FOLLOWING).orderByKey()
                        .equalTo(FirebaseAuth.getInstance().currentUser?.uid)
                updateName(query, userWhoChangeLocationId, userWhoChangeLocationName)

            }
        }

    }

    private fun updateName(query: Query, userId: String, userName: String) {
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {}
            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                dataSnapshot?.let {
                    for (singleSnapshot in dataSnapshot.children) {
                        for (childSingleSnapshot in singleSnapshot.children) {
                            val user = childSingleSnapshot.child(Constants.DATABASE_USER_FIELD).getValue(User::class.java)
                            if (user?.user_id == userId) {
                                val map = HashMap<String, Any>() as MutableMap<String, Any>
                                map.put(Constants.DATABASE_USER_NAME_FIELD, userName)
                                childSingleSnapshot?.ref?.child(Constants.DATABASE_USER_FIELD)?.updateChildren(map)
                            }
                        }
                    }

                }
            }
        })
    }

    private fun updateMarkerSnippetDistance(userToAvoidUpdate: UserBasicInfo, currentUserLocation: Location) {
        for ((key, value) in markersMap) {
            if (key != userToAvoidUpdate) {
                val location = LocationUtils.createLocationVariable(value.position)
                val distanceMeasure = LocationUtils.calculateDistanceBetweenTwoPoints(currentUserLocation, location)
                value.hideInfoWindow()
                value.snippet = mapActivity.getActivity().getString(R.string.distance) + " " + DecimalFormat("#.#").format(distanceMeasure.distance) + distanceMeasure.measure
            }
        }
    }

    fun goToThisMarker(clickedUserMarkerInformation: UserBasicInfo) {
        try {
            val searchedMarker = findMarker(clickedUserMarkerInformation)
            mapActivity.getMap().animateCamera(CameraUpdateFactory.newLatLngZoom(searchedMarker?.position, Constants.MAP_CAMERA_ZOOM))
            searchedMarker?.showInfoWindow()
        } catch (ex: Exception) {
            Toast.makeText(mapActivity, mapActivity.getString(R.string.dataLoadingInformation), Toast.LENGTH_SHORT).show()
        }
    }

    // Hash code and equals in UserBasicInfo is important here.
    fun findMarker(searchedUserMarkerInformationKey: UserBasicInfo): Marker? = markersMap.getValue(searchedUserMarkerInformationKey)

    private fun onGetMyLocationSuccess() {
        isMyLocationAdded = true
    }

    fun onGetUsersLocationSuccess() {
        isUsersLocationsAdded = true
    }

    fun onSuccessGetLocations(progressBar: RelativeLayout) {
        if (isMyLocationAdded && isUsersLocationsAdded) {
            progressBar.visibility = View.GONE
            isMyLocationAdded = false
            isUsersLocationsAdded = false
        }
    }
}