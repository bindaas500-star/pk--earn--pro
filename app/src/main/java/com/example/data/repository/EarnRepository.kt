package com.example.data.repository

import com.example.data.local.dao.EarnDao
import com.example.data.local.entity.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class EarnRepository(
    private val earnDao: EarnDao
) {
    private var firebaseAuth: FirebaseAuth? = null
    private var firestore: FirebaseFirestore? = null

    init {
        try {
            firebaseAuth = FirebaseAuth.getInstance()
            firestore = FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            // Firebase might not be configured, we will fall back gracefully to Room-based offline state
        }
    }

    // Flow exposes
    val userProfile: Flow<UserProfileEntity?> = earnDao.getUserProfile()
    val transactions: Flow<List<TransactionEntity>> = earnDao.getAllTransactions()
    val withdrawals: Flow<List<WithdrawalRequestEntity>> = earnDao.getAllWithdrawals()
    val tasks: Flow<List<EarningTaskEntity>> = earnDao.getAllTasks()
    val leaderboard: Flow<List<LeaderboardUserEntity>> = earnDao.getLeaderboard()

    // Config parameters
    var minWithdrawalCoins = 1000
    var referralRewardCoins = 250
    var adsRevenueSharedPercent = 80 // Legitimate earning based on real ads revenue

    /**
     * Seeds initial database items if they don't exist yet
     */
    suspend fun seedInitialData() {
        withContext(Dispatchers.IO) {
            // Check if leaderboard is empty
            val currentLeaderboard = earnDao.getLeaderboard().firstOrNull()
            if (currentLeaderboard.isNullOrEmpty()) {
                earnDao.insertLeaderboard(
                    listOf(
                        LeaderboardUserEntity(1, "Ali_Pro_99", 54200, 25, "Emerald Expert"),
                        LeaderboardUserEntity(2, "Sana_Khan", 41500, 18, "Gold Master"),
                        LeaderboardUserEntity(3, "Zain_Earns", 38900, 15, "Bronze Star"),
                        LeaderboardUserEntity(4, "Ayesha_786", 29100, 12, "Active Earner"),
                        LeaderboardUserEntity(5, "Kamran_Earn", 22500, 9, "Novice")
                    )
                )
            }

            // Check if tasks are empty
            val currentTasks = earnDao.getAllTasks().firstOrNull()
            if (currentTasks.isNullOrEmpty()) {
                earnDao.insertTasks(
                    listOf(
                        EarningTaskEntity("v1", "Watch High-Paying Video Ad", "Watch 30s video sponsor and earn instantly.", 150, "WATCH_VIDEO"),
                        EarningTaskEntity("i1", "Install 'Fast Crypto Wallet' App", "Download and launch for 1 minute to unlock your reward.", 600, "INSTALL_APP", targetUrl = "https://play.google.com/store"),
                        EarningTaskEntity("q1", "Solve Daily General Knowledge Quiz", "Test your knowledge and get a 10/10 streak reward.", 200, "DAILY_QUIZ"),
                        EarningTaskEntity("s1", "Spin Premium Wheel of Fortune", "Spin the wheel for high potential multiplier coins.", 80, "SPIN"),
                        EarningTaskEntity("c1", "Scratch Card Jackpot", "Scratch the surface to reveal matching gold symbols.", 100, "SCRATCH")
                    )
                )
            }
        }
    }

    /**
     * Authentication handling (Firebase with offline-fallback)
     */
    suspend fun loginOrSignUp(email: String, username: String, referralCodeApplied: String = "", password: String = ""): Boolean {
        return withContext(Dispatchers.IO) {
            var uid = UUID.randomUUID().toString().take(8)
            var isFirebaseSuccess = false

            val auth = firebaseAuth
            if (auth != null && email.isNotEmpty()) {
                val securePassword = if (password.length >= 6) password else "${password}pkpro123"
                try {
                    // Try to sign in first
                    val signInTask = auth.signInWithEmailAndPassword(email, securePassword)
                    val authResult = com.google.android.gms.tasks.Tasks.await(signInTask)
                    authResult.user?.let {
                        uid = it.uid
                        isFirebaseSuccess = true
                    }
                } catch (signInEx: Exception) {
                    // If sign in fails, try to sign up
                    try {
                        val signUpTask = auth.createUserWithEmailAndPassword(email, securePassword)
                        val authResult = com.google.android.gms.tasks.Tasks.await(signUpTask)
                        authResult.user?.let {
                            uid = it.uid
                            isFirebaseSuccess = true
                        }
                    } catch (signUpEx: Exception) {
                        // Fall back to offline/random UID if Firebase fails or is not configured
                    }
                }
            }

            // Sync with Firestore if logged in successfully and record exists
            if (isFirebaseSuccess) {
                try {
                    val userDocTask = firestore?.collection("Users")?.document(uid)?.get()
                    if (userDocTask != null) {
                        val document = com.google.android.gms.tasks.Tasks.await(userDocTask)
                        if (document.exists()) {
                            val cloudUsername = document.getString("username") ?: username
                            val cloudCoinBalance = document.getLong("coinBalance")?.toInt() ?: 100
                            val cloudTotalEarnings = document.getLong("totalEarningsCoins")?.toInt() ?: 100
                            val cloudReferralEarnings = document.getLong("referralEarningsCoins")?.toInt() ?: 0
                            val cloudReferralCode = document.getString("referralCode") ?: "PKPRO-${cloudUsername.uppercase().take(4)}-${uid.take(2)}"
                            val cloudUserLevel = document.getLong("userLevel")?.toInt() ?: 1
                            val cloudProgress = document.getDouble("progress")?.toFloat() ?: 0.05f
                            val cloudRecentActivity = document.getString("recentActivityText") ?: "Welcome back!"
                            val cloudIsFrozen = document.getBoolean("isFrozen") ?: false

                            val cloudProfile = UserProfileEntity(
                                uid = uid,
                                username = cloudUsername,
                                email = email,
                                coinBalance = cloudCoinBalance,
                                totalEarningsCoins = cloudTotalEarnings,
                                referralEarningsCoins = cloudReferralEarnings,
                                referralCode = cloudReferralCode,
                                userLevel = cloudUserLevel,
                                progress = cloudProgress,
                                recentActivityText = cloudRecentActivity,
                                isFrozen = cloudIsFrozen
                            )
                            earnDao.insertOrUpdateProfile(cloudProfile)
                            return@withContext true
                        }
                    }
                } catch (e: Exception) {
                    // Ignore and proceed to create new profile
                }
            }

            val generatedCode = "PKPRO-${username.uppercase().take(4).padEnd(4, 'X')}-${uid.take(4).uppercase()}"
            val isSelfReferral = referralCodeApplied.isNotBlank() && (
                referralCodeApplied.trim().uppercase() == generatedCode.trim().uppercase() ||
                referralCodeApplied.trim().lowercase() == email.trim().lowercase()
            )
            val finalRefCode = if (isSelfReferral) "" else referralCodeApplied.trim()

            // If a valid referral code was applied, try to find and reward the referrer in Firestore!
            if (finalRefCode.isNotEmpty() && firestore != null) {
                try {
                    val queryTask = firestore!!.collection("Users")
                        .whereEqualTo("referralCode", finalRefCode)
                        .get()
                    val querySnapshot = com.google.android.gms.tasks.Tasks.await(queryTask)
                    if (!querySnapshot.isEmpty) {
                        val referrerDoc = querySnapshot.documents.first()
                        val referrerUid = referrerDoc.id
                        
                        // Enforce no self-referral using their firestore UID too
                        if (referrerUid != uid) {
                            val currentBal = referrerDoc.getLong("coinBalance")?.toInt() ?: 0
                            val currentTotal = referrerDoc.getLong("totalEarningsCoins")?.toInt() ?: 0
                            val currentRefEarn = referrerDoc.getLong("referralEarningsCoins")?.toInt() ?: 0
                            val currentInvites = referrerDoc.getLong("inviteCount")?.toInt() ?: 0
                            
                            val referrerUpdate = mapOf(
                                "coinBalance" to currentBal + 250,
                                "totalEarningsCoins" to currentTotal + 250,
                                "referralEarningsCoins" to currentRefEarn + 250,
                                "inviteCount" to currentInvites + 1,
                                "recentActivityText" to "Referred user: $username!"
                            )
                            com.google.android.gms.tasks.Tasks.await(
                                firestore!!.collection("Users").document(referrerUid).update(referrerUpdate)
                            )
                        }
                    }
                } catch (e: Exception) {
                    // Ignore background referrer update failures if offline
                }
            }

            val profile = UserProfileEntity(
                uid = uid,
                username = username,
                email = email,
                coinBalance = if (finalRefCode.isNotEmpty()) 350 else 100, // 250 bonus for referral
                totalEarningsCoins = if (finalRefCode.isNotEmpty()) 350 else 100,
                referralEarningsCoins = 0,
                referralCode = generatedCode,
                userLevel = 1,
                progress = 0.05f,
                recentActivityText = if (finalRefCode.isNotEmpty()) "Joined via referral code '$finalRefCode'! Welcome bonus received." else "Joined PK Earn Pro! Welcome bonus received."
            )
            earnDao.insertOrUpdateProfile(profile)
            earnDao.insertTransaction(
                TransactionEntity(
                    type = "Welcome Reward",
                    coins = 100,
                    description = "Welcome bonus for signing up on PK Earn Pro",
                    status = "SUCCESS"
                )
            )

            if (finalRefCode.isNotEmpty()) {
                earnDao.insertTransaction(
                    TransactionEntity(
                        type = "Referral Bonus",
                        coins = 250,
                        description = "Referral code '$finalRefCode' applied",
                        status = "SUCCESS"
                    )
                )
            }

            // Sync with Firebase Firestore if online
            try {
                firestore?.collection("Users")?.document(uid)?.set(profile)
            } catch (e: Exception) {
                // Ignore Firestore sync errors for seamless offline use
            }

            true
        }
    }

    suspend fun claimDailyCheckIn(uid: String): String {
        return withContext(Dispatchers.IO) {
            val checkInReward = 150
            earnDao.addCoins(uid, checkInReward)
            earnDao.insertTransaction(
                TransactionEntity(
                    type = "Daily Check-in",
                    coins = checkInReward,
                    description = "Claimed daily consecutive attendance rewards.",
                    status = "SUCCESS"
                )
            )
            syncProfileToFirestore(uid)
            "Successfully claimed $checkInReward Coins!"
        }
    }

    suspend fun claimTaskReward(uid: String, taskId: String, coins: Int, title: String): Boolean {
        return withContext(Dispatchers.IO) {
            earnDao.completeTask(taskId)
            earnDao.addCoins(uid, coins)
            earnDao.insertTransaction(
                TransactionEntity(
                    type = "Task: $title",
                    coins = coins,
                    description = "Earned legitimately through Sponsor Offer Wall verification.",
                    status = "SUCCESS"
                )
            )
            syncProfileToFirestore(uid)
            true
        }
    }

    suspend fun claimAdReward(uid: String, rewardCoins: Int): Boolean {
        return withContext(Dispatchers.IO) {
            earnDao.addCoins(uid, rewardCoins)
            earnDao.insertTransaction(
                TransactionEntity(
                    type = "Google AdMob Reward",
                    coins = rewardCoins,
                    description = "Sponsor advertisement successfully watched and verified.",
                    status = "SUCCESS"
                )
            )
            syncProfileToFirestore(uid)
            true
        }
    }

    suspend fun addGameCoins(uid: String, gameType: String, coins: Int): Boolean {
        return withContext(Dispatchers.IO) {
            earnDao.addCoins(uid, coins)
            earnDao.insertTransaction(
                TransactionEntity(
                    type = gameType,
                    coins = coins,
                    description = "Legitimate reward for playing $gameType",
                    status = "SUCCESS"
                )
            )
            syncProfileToFirestore(uid)
            true
        }
    }

    suspend fun submitWithdrawal(
        uid: String,
        method: String,
        accountTitle: String,
        accountNo: String,
        coins: Int
    ): Pair<Boolean, String> {
        return withContext(Dispatchers.IO) {
            val profile = earnDao.getUserProfile().firstOrNull()
            if (profile == null) return@withContext Pair(false, "User profile not found!")
            if (profile.coinBalance < coins) return@withContext Pair(false, "Insufficient balance! Minimum is $minWithdrawalCoins coins.")
            if (coins < minWithdrawalCoins) return@withContext Pair(false, "Minimum withdrawal is $minWithdrawalCoins coins.")

            // Deduct from local Room
            earnDao.deductCoins(uid, coins)

            val cashEquiv = coins.toDouble() / 100.0 // 100 coins = 1 PKR / Cent
            val withdrawalId = "WD-${UUID.randomUUID().toString().take(6).uppercase()}"

            val request = WithdrawalRequestEntity(
                id = withdrawalId,
                method = method,
                accountTitle = accountTitle,
                accountNumber = accountNo,
                coinAmount = coins,
                cashAmount = cashEquiv,
                status = "PENDING"
            )

            earnDao.insertWithdrawal(request)
            earnDao.insertTransaction(
                TransactionEntity(
                    type = "Withdrawal Submitted",
                    coins = coins,
                    description = "Requested $cashEquiv PKR/USD cashout via $method ($withdrawalId)",
                    status = "PENDING"
                )
            )

            // Sync with Firestore if possible
            try {
                firestore?.collection("Withdrawals")?.document(withdrawalId)?.set(request)
            } catch (e: Exception) {
                // Ignore
            }

            syncProfileToFirestore(uid)

            Pair(true, "Cashout requested successfully!")
        }
    }

    // Referral code share & reward
    suspend fun applyReferralCode(uid: String, promoCode: String): Pair<Boolean, String> {
        return withContext(Dispatchers.IO) {
            val currentProfile = earnDao.getUserProfile().firstOrNull() ?: return@withContext Pair(false, "Profile not found")
            if (promoCode == currentProfile.referralCode) {
                return@withContext Pair(false, "Cannot apply your own referral code!")
            }
            // Reward current user
            earnDao.addCoins(uid, referralRewardCoins)
            earnDao.insertTransaction(
                TransactionEntity(
                    type = "Referral Applied",
                    coins = referralRewardCoins,
                    description = "Bonus for applying referral code $promoCode",
                    status = "SUCCESS"
                )
            )
            syncProfileToFirestore(uid)
            Pair(true, "Referral applied! $referralRewardCoins Coins added!")
        }
    }

    /**
     * Admin Dashboard Panel Operations
     */
    suspend fun adminApproveWithdrawal(withdrawalId: String, note: String) {
        withContext(Dispatchers.IO) {
            earnDao.updateWithdrawalStatus(withdrawalId, "APPROVED", note)
            earnDao.insertTransaction(
                TransactionEntity(
                    type = "Withdrawal Approved",
                    coins = 0,
                    description = "Withdrawal request $withdrawalId approved. Note: $note",
                    status = "SUCCESS"
                )
            )
            try {
                firestore?.collection("Withdrawals")?.document(withdrawalId)?.update("status", "APPROVED")
            } catch (e: Exception) {}
        }
    }

    suspend fun adminRejectWithdrawal(withdrawalId: String, coinsRefund: Int, note: String) {
        withContext(Dispatchers.IO) {
            earnDao.updateWithdrawalStatus(withdrawalId, "REJECTED", note)
            val profile = earnDao.getUserProfile().firstOrNull()
            if (profile != null) {
                earnDao.addCoins(profile.uid, coinsRefund)
                syncProfileToFirestore(profile.uid)
            }
            earnDao.insertTransaction(
                TransactionEntity(
                    type = "Withdrawal Rejected",
                    coins = coinsRefund,
                    description = "Refunded $coinsRefund coins for rejected withdrawal $withdrawalId. Reason: $note",
                    status = "FAILED"
                )
            )
            try {
                firestore?.collection("Withdrawals")?.document(withdrawalId)?.update("status", "REJECTED")
            } catch (e: Exception) {}
        }
    }

    suspend fun adminFreezeUser(uid: String, isFrozen: Boolean) {
        withContext(Dispatchers.IO) {
            earnDao.freezeAccount(uid, isFrozen)
        }
    }

    suspend fun adminUpdateMinWithdrawal(coins: Int) {
        minWithdrawalCoins = coins
    }

    suspend fun updateLastSpinTimestamp(uid: String, timestamp: Long) {
        withContext(Dispatchers.IO) {
            earnDao.updateLastSpinTimestamp(uid, timestamp)
            syncProfileToFirestore(uid)
        }
    }

    suspend fun updateLastScratchTimestamp(uid: String, timestamp: Long) {
        withContext(Dispatchers.IO) {
            earnDao.updateLastScratchTimestamp(uid, timestamp)
            syncProfileToFirestore(uid)
        }
    }

    suspend fun logout() {
        withContext(Dispatchers.IO) {
            earnDao.clearUserProfile()
            try {
                firebaseAuth?.signOut()
            } catch (e: Exception) {}
        }
    }

    suspend fun syncProfileFromFirestore(uid: String) {
        withContext(Dispatchers.IO) {
            val db = firestore
            if (db != null) {
                try {
                    val userDocTask = db.collection("Users").document(uid).get()
                    val document = com.google.android.gms.tasks.Tasks.await(userDocTask)
                    if (document.exists()) {
                        val currentProfile = earnDao.getUserProfile().firstOrNull()
                        val username = document.getString("username") ?: currentProfile?.username ?: ""
                        val email = document.getString("email") ?: currentProfile?.email ?: ""
                        val coinBalance = document.getLong("coinBalance")?.toInt() ?: currentProfile?.coinBalance ?: 100
                        val totalEarnings = document.getLong("totalEarningsCoins")?.toInt() ?: currentProfile?.totalEarningsCoins ?: 100
                        val referralEarnings = document.getLong("referralEarningsCoins")?.toInt() ?: currentProfile?.referralEarningsCoins ?: 0
                        val referralCode = document.getString("referralCode") ?: currentProfile?.referralCode ?: ""
                        val inviteCount = document.getLong("inviteCount")?.toInt() ?: currentProfile?.inviteCount ?: 0
                        val isFrozen = document.getBoolean("isFrozen") ?: currentProfile?.isFrozen ?: false

                        val updatedProfile = UserProfileEntity(
                            uid = uid,
                            username = username,
                            email = email,
                            coinBalance = coinBalance,
                            totalEarningsCoins = totalEarnings,
                            referralEarningsCoins = referralEarnings,
                            referralCode = referralCode,
                            inviteCount = inviteCount,
                            isFrozen = isFrozen,
                            userLevel = currentProfile?.userLevel ?: 1,
                            progress = currentProfile?.progress ?: 0.05f,
                            recentActivityText = document.getString("recentActivityText") ?: currentProfile?.recentActivityText ?: "Synced profile with cloud."
                        )
                        earnDao.insertOrUpdateProfile(updatedProfile)
                    }
                } catch (e: Exception) {
                    // Ignore errors if offline
                }
            }
        }
    }

    private suspend fun syncProfileToFirestore(uid: String) {
        val auth = firebaseAuth
        val db = firestore
        if (auth != null && db != null) {
            try {
                val profile = earnDao.getUserProfile().firstOrNull()
                if (profile != null && profile.uid == uid) {
                    com.google.android.gms.tasks.Tasks.await(db.collection("Users").document(uid).set(profile))
                }
            } catch (e: Exception) {
                // Ignore background sync errors when network/keys are missing
            }
        }
    }
}
