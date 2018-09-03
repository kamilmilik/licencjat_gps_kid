package kamilmilik.gps_tracker.background

import android.util.Log
import kamilmilik.gps_tracker.utils.Constants
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
    }


    fun doOnlyOneAction() {
        doOnlyOneTaskDoneCounter.incrementAndGet()
        if (doOnlyOneTaskDoneCounter.compareAndSet(3, 0)) {
            finishServiceIfServiceIsNotNullAndOtherTaskFinished()
        }

    }


    fun endOfPolygonIterateAction(listSize: Int) {
        polygonActionCounter.incrementAndGet()
        if (polygonActionCounter.compareAndSet(listSize, 0)) {
            oneUserEndPolygonAction()
        }
    }

    private fun oneUserEndPolygonAction() {
        howManyTimesActionRunConnectedUser.incrementAndGet()
        ObjectsUtils.safeLet(countOfFollowersUser, countOfFollowingUser) { countOfFollowersUser, countOfFollowingUser ->
            if (howManyTimesActionRunConnectedUser.compareAndSet((countOfFollowersUser + countOfFollowingUser).toInt(), 0)) {
                allTaskDoneAction()
            }
        }

    }

    fun allTaskDoneAction() {
        allTaskDoneCounter.incrementAndGet()
        if (allTaskDoneCounter.compareAndSet(2, 0)) {
            finishServiceIfServiceIsNotNullAndOtherTaskFinished()
        }
    }

    private fun finishServiceIfServiceIsNotNullAndOtherTaskFinished() {
        foregroundService.finishServiceAction()
    }
}