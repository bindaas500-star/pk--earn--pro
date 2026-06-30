package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val uid: String,
    val username: String,
    val email: String,
    val coinBalance: Int,
    val totalEarningsCoins: Int,
    val referralEarningsCoins: Int,
    val referralCode: String,
    val userLevel: Int,
    val progress: Float,
    val recentActivityText: String,
    val country: String = "Pakistan",
    val language: String = "English",
    val badges: String = "Beginner,AdWatcher", // Comma-separated list of earned badge names
    val inviteCount: Int = 0,
    val ipAddress: String = "192.168.1.1",
    val deviceVerified: Boolean = true,
    val isFrozen: Boolean = false
)

@Entity(tableName = "transaction_history")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String, // "Daily Check-in", "Watch Video", "Spin Wheel", "Scratch Card", "Quiz", "Referral", "Withdrawal"
    val coins: Int,
    val description: String,
    val status: String, // "SUCCESS", "PENDING", "FAILED"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "earning_task")
data class EarningTaskEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val coinReward: Int,
    val type: String, // "WATCH_VIDEO", "INSTALL_APP", "DAILY_QUIZ", "SCRATCH", "SPIN"
    val isCompleted: Boolean = false,
    val targetUrl: String = ""
)

@Entity(tableName = "withdrawal_request")
data class WithdrawalRequestEntity(
    @PrimaryKey val id: String,
    val method: String, // "Easypaisa", "JazzCash", "PayPal", "GCash", "Binance Pay", "Bank Transfer"
    val accountTitle: String,
    val accountNumber: String,
    val coinAmount: Int,
    val cashAmount: Double,
    val status: String, // "PENDING", "APPROVED", "REJECTED"
    val adminNote: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "leaderboard")
data class LeaderboardUserEntity(
    @PrimaryKey val rank: Int,
    val username: String,
    val coinBalance: Int,
    val level: Int,
    val badge: String = "Pro"
)
