package kamilmilik.gps_tracker.models

/**
 * Created by kamil on 27.07.2018.
 */
data class NotificationModel(var contentTitle: String,
                             var contentText: String,
                             var notificationChanelId: String,
                             var notificationId: Int,
                             var isVibrate: Boolean)