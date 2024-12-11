package com.example.groupprojectapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import android.Manifest
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityCompat


object Notification {

    private const val CHANNEL_ID = "price_alert_channel"
    private const val CHANNEL_NAME = "Price Alerts"
    private const val CHANNEL_DESCRIPTION = "Notifications for stock and crypto price changes"

    fun createNotificationChannel(context: Context) {
        Log.d("Notification", "Attempting to create notification channel")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESCRIPTION
            }

            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            Log.d("Notification", "Notification channel created successfully")
        } else {
            Log.d("Notification", "Device running on Android version lower than 8.0 (Oreo), no need to create channel")
        }
    }

    fun requestNotificationPermission(context: Context, requestPermissionLauncher: ActivityResultLauncher<String>) {
        Log.d("Notification", "Checking notification permission")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                Log.d("Notification", "Permission not granted, requesting permission")
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                Log.d("Notification", "Permission already granted")
            }
        } else {
            Log.d("Notification", "Device running on Android version lower than 13, no need to request permission")
        }
    }

    fun sendPriceNotification(context: Context, title: String, message: String) {
        Log.d("Notification", "Preparing to send notification")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                Log.e("Notification", "Notification permission not granted. Aborting notification.")
                Toast.makeText(context, "Notification permission not granted", Toast.LENGTH_SHORT).show()
                return
            } else {
                Log.d("Notification", "Notification permission granted. Proceeding to send notification.")
            }
        }

        val notificationId = System.currentTimeMillis().toInt()
        Log.d("Notification", "Generated notification ID: $notificationId")

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        Log.d("Notification", "Notification builder created")

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(notificationId, builder.build())
            Log.d("Notification", "Notification sent successfully")
        } catch (e: Exception) {
            Log.e("Notification", "Error sending notification: ${e.message}", e)
        }
    }
}