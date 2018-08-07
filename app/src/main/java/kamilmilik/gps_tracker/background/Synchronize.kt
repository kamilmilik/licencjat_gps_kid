package kamilmilik.gps_tracker.background

import android.util.Log
import com.google.firebase.database.DataSnapshot
import kamilmilik.gps_tracker.utils.Constants
import java.util.concurrent.atomic.AtomicInteger

/**
 * Created by kamil on 26.06.2018.
 */
class Synchronize(private var foregroundService: ForegroundService) {
    private val TAG = Synchronize::class.java.simpleName

    var countOfFollowingUser: Long? = null

    var countOfFollowersUser: Long? = null

    var allTaskDoneCounter: AtomicInteger = AtomicInteger(0)

    var doOnlyOneTaskDoneCounter: AtomicInteger = AtomicInteger(0)

    var polygonActionCounter: AtomicInteger = AtomicInteger(0)

    var howManyTimesActionRunConnectedUser: AtomicInteger = AtomicInteger(0)

    fun setSynchronizeCounter(databaseNode: String, dataSnapshot: DataSnapshot) {
        if (databaseNode == Constants.DATABASE_FOLLOWING) {
            countOfFollowingUser = dataSnapshot.childrenCount
        }
        if (databaseNode == Constants.DATABASE_FOLLOWERS) {
            countOfFollowersUser = dataSnapshot.childrenCount
        }
    }


    fun doOnlyOneAction() {
        // sytuacja gdy w dwa razy findConnectionUsers nic nie znajduje plus serwis skonczyl prace czyli zincrementowal
        doOnlyOneTaskDoneCounter.incrementAndGet()
        Log.i(TAG, "doOnlyOneAction() doOnlyOneTaskDoneCounter = " + doOnlyOneTaskDoneCounter)
        if (doOnlyOneTaskDoneCounter.compareAndSet(3, 0)) {
            finishServiceIfServiceIsNotNullAndOtherTaskFinished()
        }

    }


    fun endOfPolygonIterateAction(listSize: Int) {
        polygonActionCounter.incrementAndGet()
        Log.i(TAG, "endOfPolygonIterateAction() polygonActionCounter = " + polygonActionCounter + " list size " + listSize)
        if (polygonActionCounter.compareAndSet(listSize, 0)) {
            //koncz zadanie
            //ok zarowno findFollowing and findFollowers moge inkrementowac polygonActionCounter ale to nic bo i tak na koncu patrzymy czy howManyTimesAction.. sie wykonalo countFollowers i countFollowing razy
            //ok wykonalo sie tyle razy ile jest polygonow w liscie, wiec policz ze dla danego usera skonczylismy liczyc
            oneUserEndPolygonAction()
        }
    }

    fun oneUserEndPolygonAction() {
        howManyTimesActionRunConnectedUser.incrementAndGet()
        if (countOfFollowersUser != null || countOfFollowingUser != null) {
            Log.i(TAG, "oneUserEndPolygonAction() howManyTimesActionRunConnectedUser = " + howManyTimesActionRunConnectedUser)
            if (howManyTimesActionRunConnectedUser.compareAndSet((countOfFollowersUser!! + countOfFollowingUser!!).toInt(), 0)) {
                Log.i(TAG, "oneUserEndPolygonAction() if z dwoma countami wszedl")
                allTaskDoneAction()
            }
        }
    }

    private fun allTaskDoneAction() {
        allTaskDoneCounter.incrementAndGet()
        Log.i(TAG,"allTaskDoneAction() allTaskDoneCounter = " + allTaskDoneCounter)
        if (allTaskDoneCounter.compareAndSet(2, 0)) {// 2 bo findConnectionUsers wywoluje i tam sprawdzam czy oba findConnections sie wywolaly i raz inkrementowac ma serwis
            finishServiceIfServiceIsNotNullAndOtherTaskFinished()
        }
    }

    private fun finishServiceIfServiceIsNotNullAndOtherTaskFinished() {
        Log.i(TAG,"finishServiceIfServiceIsNotNullAndOtherTaskFinished()")
        foregroundService.finishServiceAction()
    }
}