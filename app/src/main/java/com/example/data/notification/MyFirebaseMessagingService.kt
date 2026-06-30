package com.example.data.notification

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {
    private val TAG = "MyFirebaseMessaging"

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New Cloud Messaging Token generated: $token")
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "Cloud Notification Message received: ${remoteMessage.messageId}")

        // Check if message contains a notification payload.
        remoteMessage.notification?.let {
            val title = it.title ?: "🎁 PK Earn Pro Message"
            val body = it.body ?: "Open the app to claim your rewards!"
            NotificationHelper.showNotification(applicationContext, title, body)
        }

        // Check if message contains data payload
        if (remoteMessage.data.isNotEmpty()) {
            val title = remoteMessage.data["title"] ?: remoteMessage.data["status"] ?: "🎁 PK Earn Pro Update"
            val message = remoteMessage.data["message"] ?: remoteMessage.data["note"] ?: "Check out your wallet for updates!"
            NotificationHelper.showNotification(applicationContext, title, message)
        }
    }
}
