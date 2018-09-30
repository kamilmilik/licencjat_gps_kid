package kamilmilik.gps_tracker.map

import android.app.AlertDialog
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kamilmilik.gps_tracker.models.QueryUserModel
import kamilmilik.gps_tracker.utils.Constants.DATABASE_FOLLOWERS
import kamilmilik.gps_tracker.utils.Constants.DATABASE_FOLLOWING
import kamilmilik.gps_tracker.utils.Constants.DATABASE_USER_POLYGONS
import kamilmilik.gps_tracker.utils.ObjectsUtils
import kamilmilik.gps_tracker.R
import kamilmilik.gps_tracker.utils.Constants.WORK_ON_UI_ITEMS_LIMIT


class PolygonAndUserCounter(private val mapActivity: MapActivity) : BasicListenerContent() {
    private val TAG = PolygonAndUserCounter::class.java.simpleName

    private var polygonsCount: Long? = null

    private var userFollowingCount: Long? = null

    private var userFollowersCount: Long? = null

    private var isTooMuchWorkOnUiDialogShown: Boolean = false

    private var isNotTooMuchWorkOnUiDialogShown: Boolean = false

    fun polygonAndUserCounterAction() {
        countPolygons()
        countUsers()
    }

    private fun countPolygons() {
        FirebaseAuth.getInstance().currentUser?.uid?.let { currentUserId ->
            val query = FirebaseDatabase.getInstance().reference.child(DATABASE_USER_POLYGONS).child(currentUserId)
            query.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot?) {
                    dataSnapshot?.let {
                        polygonsCount = dataSnapshot.childrenCount
                        counterUsersAndPolygon()

                        if (!dataSnapshot.exists()) {
                            polygonsCount = 0
                        }
                    }

                    putValueEventListenersToMap(QueryUserModel(currentUserId, query), this)
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })

        }
    }

    private fun countUsers() {
        val reference = FirebaseDatabase.getInstance().reference
        FirebaseAuth.getInstance().currentUser?.uid?.let { currentUserId ->
            getCountForGivenNode(reference, DATABASE_FOLLOWING, currentUserId)
            getCountForGivenNode(reference, DATABASE_FOLLOWERS, currentUserId)
        }

    }

    private fun getCountForGivenNode(reference: DatabaseReference, databaseNode: String, userIdQuery: String) {
        val query = reference.child(databaseNode).orderByKey().equalTo(userIdQuery)
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                dataSnapshot?.let {
                    for (singleSnapshot in dataSnapshot.children) {
                        chooseWhichCounter(databaseNode, singleSnapshot.childrenCount)
                        counterUsersAndPolygon()
                    }
                    if (!dataSnapshot.exists()) {
                        chooseWhichCounter(databaseNode, 0)
                    }

                }

                putValueEventListenersToMap(QueryUserModel(userIdQuery, query), this)

            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    fun chooseWhichCounter(databaseNode: String, size: Long) {
        if (databaseNode == DATABASE_FOLLOWERS) {
            userFollowersCount = size
        } else if (databaseNode == DATABASE_FOLLOWING) {
            userFollowingCount = size
        }
    }

    private fun counterUsersAndPolygon() {
        ObjectsUtils.safeLet(polygonsCount, userFollowersCount, userFollowingCount) { polygonsCount, userFollowersCount, userFollowingCount ->
            val usersCount = userFollowersCount + userFollowingCount
            val itemsToCheckCount = polygonsCount * usersCount
            val workUiDifference = itemsToCheckCount - WORK_ON_UI_ITEMS_LIMIT
            val isTooMuchWorkOnUi = (workUiDifference > 0)
            if (isTooMuchWorkOnUi) {
                tooMuchWorkOnUiAction()
            } else {
                notTooMuchWorkOnUiAction()
            }
        }
    }

    private fun tooMuchWorkOnUiAction() {
        mapActivity.setTooMuchWorkOnUi(true)
        if (!isTooMuchWorkOnUiDialogShown) {
            showTooMuchWorkOnUiDialog()
        }
    }

    private fun notTooMuchWorkOnUiAction() {
        mapActivity.setTooMuchWorkOnUi(false)
        if (isTooMuchWorkOnUiDialogShown && !isNotTooMuchWorkOnUiDialogShown) {
            showNotTooMuchWorkOnUiDialog()
        }
    }

    private fun showTooMuchWorkOnUiDialog() {
        AlertDialog.Builder(mapActivity)
                .setTitle(mapActivity.getString(R.string.applicationPerformance))
                .setMessage(mapActivity.getString(R.string.applicationBadPerformanceMessage))
                .setPositiveButton(mapActivity.getString(R.string.ok)) { dialog, which -> }
                .show()
        isTooMuchWorkOnUiDialogShown = true
        isNotTooMuchWorkOnUiDialogShown = false
    }

    private fun showNotTooMuchWorkOnUiDialog() {
        AlertDialog.Builder(mapActivity)
                .setTitle(mapActivity.getString(R.string.applicationPerformance))
                .setMessage(mapActivity.getString(R.string.applicationGoodPerformanceMessage))
                .setPositiveButton(mapActivity.getString(R.string.ok)) { dialog, which -> }
                .show()
        isNotTooMuchWorkOnUiDialogShown = true
        isTooMuchWorkOnUiDialogShown = false
    }


}