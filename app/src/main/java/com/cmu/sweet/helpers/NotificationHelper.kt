package com.cmu.sweet.helpers


import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.cmu.sweet.R

object NotificationHelper {

    private const val NOTIFICATION_ID = 1

    fun notifyUser(context: Context, title: String, message: String) {

        // Build the notification
        val notification = NotificationCompat.Builder(context, "location")
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.cake_48px)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        // Send the notification
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)


    }

    fun cancelNotification(context: Context) {
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.cancel(NOTIFICATION_ID)  // Cancel the notification with the ID
    }

    fun cancelAllNotifications(context: Context) {
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.cancelAll()  // Cancel all notifications
    }

}