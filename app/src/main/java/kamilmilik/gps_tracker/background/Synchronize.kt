package kamilmilik.gps_tracker.background

import android.util.Log
import kamilmilik.gps_tracker.utils.Constants
import kamilmilik.gps_tracker.utils.LogUtils
import kamilmilik.gps_tracker.utils.ObjectsUtils
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

    fun setSynchronizeCounter(databaseNode: String, size: Long) {
        if (databaseNode == Constants.DATABASE_FOLLOWING) {
            countOfFollowingUser = size
        }
        if (databaseNode == Constants.DATABASE_FOLLOWERS) {
            countOfFollowersUser = size
        }
        ObjectsUtils.safeLet(countOfFollowersUser, countOfFollowingUser) { countOfFollowersUser, countOfFollowingUser ->
            LogUtils(foregroundService).appendLog(TAG, "setSynchronizeCounter() countOfFollowingUser ${countOfFollowingUser} countOfFollowersUser $countOfFollowersUser")
        }
    }


    fun doOnlyOneAction() {
        // sytuacja gdy w dwa razy findConnectionUsers nic nie znajduje plus serwis skonczyl prace czyli zincrementowal
        doOnlyOneTaskDoneCounter.incrementAndGet()
        LogUtils(foregroundService).appendLog(TAG, "doOnlyOneAction() doOnlyOneTaskDoneCounter = " + doOnlyOneTaskDoneCounter)
        if (doOnlyOneTaskDoneCounter.compareAndSet(3, 0)) {
            finishServiceIfServiceIsNotNullAndOtherTaskFinished()
        }

    }


    fun endOfPolygonIterateAction(listSize: Int) {
        polygonActionCounter.incrementAndGet()
        LogUtils(foregroundService).appendLog(TAG, "endOfPolygonIterateAction() polygonActionCounter = " + polygonActionCounter + " list size " + listSize)
        if (polygonActionCounter.compareAndSet(listSize, 0)) {
            //koncz zadanie
            //ok zarowno findFollowing and findFollowers moge inkrementowac polygonActionCounter ale to nic bo i tak na koncu patrzymy czy howManyTimesAction.. sie wykonalo countFollowers i countFollowing razy
            //ok wykonalo sie tyle razy ile jest polygonow w liscie, wiec policz ze dla danego usera skonczylismy liczyc
            oneUserEndPolygonAction()
        }
    }

    private fun oneUserEndPolygonAction() {
        howManyTimesActionRunConnectedUser.incrementAndGet()
        ObjectsUtils.safeLet(countOfFollowersUser, countOfFollowingUser) { countOfFollowersUser, countOfFollowingUser ->
            LogUtils(foregroundService).appendLog(TAG, "oneUserEndPolygonAction() howManyTimesActionRunConnectedUser = " + howManyTimesActionRunConnectedUser)
            if (howManyTimesActionRunConnectedUser.compareAndSet((countOfFollowersUser + countOfFollowingUser).toInt(), 0)) {
                LogUtils(foregroundService).appendLog(TAG, "oneUserEndPolygonAction() if z dwoma countami wszedl")
                allTaskDoneAction()
            }
        }

    }

    fun allTaskDoneAction() {
        allTaskDoneCounter.incrementAndGet()
        LogUtils(foregroundService).appendLog(TAG, "allTaskDoneAction() allTaskDoneCounter = " + allTaskDoneCounter)
        if (allTaskDoneCounter.compareAndSet(2, 0)) {// 2 bo findConnectionUsers wywoluje i tam sprawdzam czy oba findConnections sie wywolaly i raz inkrementowac ma serwis
            finishServiceIfServiceIsNotNullAndOtherTaskFinished()
        }
    }

    private fun finishServiceIfServiceIsNotNullAndOtherTaskFinished() {
        Log.i(TAG, "finishServiceIfServiceIsNotNullAndOtherTaskFinished()")
        foregroundService.finishServiceAction()
    }
}