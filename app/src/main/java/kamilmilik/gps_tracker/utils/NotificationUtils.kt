package kamilmilik.gps_tracker.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.support.v4.app.NotificationCompat
import kamilmilik.gps_tracker.R
import kamilmilik.gps_tracker.models.NotificationModel

/**
 * Created by kamil on 07.08.2018.
 */
object NotificationUtils {
    fun <T> createPendingIntent(context: Context, classType: Class<T>, isClearTaskIntent: Boolean): PendingIntent {
        val notificationIntent = Intent(context, classType)
        if (isClearTaskIntent) {
            notificationIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        return PendingIntent.getActivity(context, 0, notificationIntent, 0)
    }

    fun createNotification(context: Context, notificationModel: NotificationModel): NotificationCompat.Builder? {
        val notification = NotificationCompat.Builder(context, notificationModel.notificationChanelId)
                .setContentTitle(notificationModel.contentTitle)
                .setContentText(notificationModel.contentText)
                .setSmallIcon(R.mipmap.gps_tracker_launcher)
        if (notificationModel.isVibrate) {
            notification.setVibrate(Constants.NOTIFICATION_VIBRATION_PATTERN)
        }
        return notification
    }

    fun createNotificationChannel(context: Context, notificationModel: NotificationModel) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(notificationModel.notificationChanelId, context.getString(R.string.app_name), NotificationManager.IMPORTANCE_HIGH)
            channel.description = context.getString(R.string.notificationAreaDescription)
            if (notificationModel.isVibrate) {
                channel.enableLights(true)
                channel.lightColor = Color.RED
                channel.enableVibration(true)
                channel.vibrationPattern = Constants.NOTIFICATION_VIBRATION_PATTERN
                channel.importance = NotificationManager.IMPORTANCE_HIGH
            } else {
                channel.enableVibration(false)
                channel.setSound(null, null)
                channel.importance = NotificationManager.IMPORTANCE_LOW
            }
            notificationManager.createNotificationChannel(channel)
        }
    }
}