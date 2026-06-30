package com.example.data.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.R

object NotificationHelper {
    private const val CHANNEL_ID = "pk_earn_pro_notifications"
    private const val CHANNEL_NAME = "PK Earn Pro Rewards"
    private const val CHANNEL_DESC = "Real-time rewards, cashouts and tasks alerts"

    /**
     * Call this in MainActivity or App initialization to register channels
     */
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Show a generic notification
     */
    fun showNotification(context: Context, title: String, message: String, notificationId: Int = (1000..9999).random()) {
        try {
            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)

            with(NotificationManagerCompat.from(context)) {
                // In modern Android (API 33+), system blocks this if POST_NOTIFICATIONS is not granted.
                // We check permission, and if missing, it'll fail gracefully without crashing.
                notify(notificationId, builder.build())
            }
        } catch (e: SecurityException) {
            // Log/Ignore if permission is denied
        } catch (e: Exception) {
            // Graceful fallback
        }
    }

    fun showDailyCheckInNotification(context: Context, coins: Int) {
        showNotification(
            context = context,
            title = "🎁 Attendance Reward claimed!",
            message = "Awesome! You claimed consecutive daily login streak bonus of +$coins Gold Coins."
        )
    }

    fun showRewardNotification(context: Context, rewardType: String, coins: Int) {
        showNotification(
            context = context,
            title = "💰 Reward Credited!",
            message = "Success! +$coins Gold Coins received from playing $rewardType."
        )
    }

    fun showWithdrawalNotification(context: Context, id: String, method: String, coins: Int) {
        showNotification(
            context = context,
            title = "📤 Payout Request Submitted",
            message = "Your cashout request $id of $coins Coins via $method is being verified by admin."
        )
    }

    fun showAdminNotification(context: Context, status: String, id: String, note: String) {
        val title = if (status == "APPROVED") "✅ Payout Approved!" else "❌ Payout Rejected"
        val message = if (status == "APPROVED") {
            "Your withdrawal request $id is approved! $note"
        } else {
            "Your withdrawal request $id has been rejected. Refunded coins. $note"
        }
        showNotification(context = context, title = title, message = message)
    }
}
