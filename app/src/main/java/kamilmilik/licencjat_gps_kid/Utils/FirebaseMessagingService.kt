package kamilmilik.licencjat_gps_kid.Utils

import android.support.v4.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kamilmilik.licencjat_gps_kid.R
import android.support.v4.app.NotificationManagerCompat
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.content.Intent
import kamilmilik.licencjat_gps_kid.ListOnline
import android.app.PendingIntent
import kamilmilik.licencjat_gps_kid.Constants


/**
 * Created by kamil on 03.03.2018.
 */
class FirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage?) {
        super.onMessageReceived(remoteMessage)

        var clickAction = remoteMessage!!.notification!!.clickAction
        var notificationTitle = remoteMessage!!.notification!!.title
        var notificationMessage = remoteMessage!!.notification!!.body

        var fromUserId = remoteMessage.data.get("from_user_id")//get value from node from_user_id
        val mBuilder = NotificationCompat.Builder(this,Constants.CHANNEL_ID)
                .setSmallIcon(R.drawable.cast_ic_notification_small_icon)
                .setContentTitle(notificationTitle)
                .setContentText(notificationMessage)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        createNotificationChannelForApi26()

        tapToNotificationAction(mBuilder,clickAction!!)

        createNotificationId(mBuilder)

    }
    private fun createNotificationChannelForApi26(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel, but only on API 26+ because
            // the NotificationChannel class is new and not in the support library
            val name = "My channel name"
            val description = "Description channel"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(Constants.CHANNEL_ID, name, importance)
            channel.description = description
            // Register the channel with the system
            val notificationManager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    private fun createNotificationId(builder : NotificationCompat.Builder){
        var notificationId = (System.currentTimeMillis()).toInt()
        var notificationManager : NotificationManager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, builder.build())
    }
    private fun tapToNotificationAction(builder : NotificationCompat.Builder, clickAction : String){
        val intent = Intent(clickAction)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, 0)
        builder.setContentIntent(pendingIntent)
    }
}