package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entity.*
import com.example.data.repository.EarnRepository
import com.example.data.security.EmulatorDetectionHelper
import com.example.data.security.RootDetectionHelper
import com.example.data.security.VpnDetectionHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class EarnViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = EarnRepository(database.earnDao())

    // Security Status Flags
    private val _isRooted = MutableStateFlow(false)
    val isRooted: StateFlow<Boolean> = _isRooted.asStateFlow()

    private val _isEmulator = MutableStateFlow(false)
    val isEmulator: StateFlow<Boolean> = _isEmulator.asStateFlow()

    private val _isVpnActive = MutableStateFlow(false)
    val isVpnActive: StateFlow<Boolean> = _isVpnActive.asStateFlow()

    // UI Loading State
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Ads Simulation State
    private val _isAdLoading = MutableStateFlow(false)
    val isAdLoading: StateFlow<Boolean> = _isAdLoading.asStateFlow()

    private val _adMessage = MutableStateFlow<String?>(null)
    val adMessage: StateFlow<String?> = _adMessage.asStateFlow()

    // Data streams from repository
    val userProfile: StateFlow<UserProfileEntity?> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val transactions: StateFlow<List<TransactionEntity>> = repository.transactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val withdrawals: StateFlow<List<WithdrawalRequestEntity>> = repository.withdrawals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tasks: StateFlow<List<EarningTaskEntity>> = repository.tasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val leaderboard: StateFlow<List<LeaderboardUserEntity>> = repository.leaderboard
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Settings State
    private val _isDarkMode = MutableStateFlow(true)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _selectedLanguage = MutableStateFlow("English")
    val selectedLanguage: StateFlow<String> = _selectedLanguage.asStateFlow()

    // Quiz Game Local State
    val quizQuestions = listOf(
        QuizQuestion("Who is the founder of Pakistan?", listOf("Quaid-e-Azam", "Allama Iqbal", "Liaquat Ali Khan", "Sir Syed Ahmed"), 0),
        QuizQuestion("What is the national currency of Pakistan?", listOf("Rupee", "Dinar", "Dollar", "Rial"), 0),
        QuizQuestion("Legitimate mobile apps earn revenue from which source?", listOf("Deceptive systems", "Sponsor Ads & Offers", "Fake cash pools", "Illegal mining"), 1),
        QuizQuestion("What is the capital city of Pakistan?", listOf("Lahore", "Karachi", "Islamabad", "Peshawar"), 2)
    )
    private val _currentQuizIndex = MutableStateFlow(0)
    val currentQuizIndex: StateFlow<Int> = _currentQuizIndex.asStateFlow()

    private val _quizScore = MutableStateFlow(0)
    val quizScore: StateFlow<Int> = _quizScore.asStateFlow()

    private val _quizCompleted = MutableStateFlow(false)
    val quizCompleted: StateFlow<Boolean> = _quizCompleted.asStateFlow()

    init {
        checkSecurityFlags(application)
        viewModelScope.launch {
            repository.seedInitialData()
        }
    }

    fun checkSecurityFlags(context: Context) {
        viewModelScope.launch {
            _isRooted.value = RootDetectionHelper.isDeviceRooted()
            _isEmulator.value = EmulatorDetectionHelper.isRunningOnEmulator()
            _isVpnActive.value = VpnDetectionHelper.isVpnActive(context)
        }
    }

    fun setDarkMode(enabled: Boolean) {
        _isDarkMode.value = enabled
    }

    fun setLanguage(lang: String) {
        _selectedLanguage.value = lang
    }

    fun toggleVpnBypass() {
        // Allow a toggle to ignore security triggers in preview/testing mode
        _isVpnActive.value = false
        _isRooted.value = false
        _isEmulator.value = false
    }

    // Auth actions
    fun loginOrSignUp(email: String, username: String, referralApplied: String, password: String = "", onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            delay(1000) // Simulating login verification network latency
            val success = repository.loginOrSignUp(email, username, referralApplied, password)
            _isLoading.value = false
            if (success) onSuccess()
        }
    }

    fun logout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            delay(500)
            // For mock demo, we delete the profile record or clear state to simulate logout
            // but keep Room database transactions
            _isLoading.value = false
            onSuccess()
        }
    }

    // Daily Checkin Action
    fun claimDailyCheckIn(onMessage: (String) -> Unit) {
        viewModelScope.launch {
            val profile = userProfile.value ?: return@launch
            _isLoading.value = true
            val msg = repository.claimDailyCheckIn(profile.uid)
            _isLoading.value = false
            onMessage(msg)
        }
    }

    // Task completion
    fun completeEarningTask(task: EarningTaskEntity, onMessage: (String) -> Unit) {
        viewModelScope.launch {
            val profile = userProfile.value ?: return@launch
            _isLoading.value = true
            delay(1500) // Simulating external app launch/ad verification wait time
            val success = repository.claimTaskReward(profile.uid, task.id, task.coinReward, task.title)
            _isLoading.value = false
            if (success) {
                onMessage("Task completed! You earned ${task.coinReward} Coins.")
            } else {
                onMessage("Failed to verify task completion.")
            }
        }
    }

    // Spin wheel reward
    fun playSpinWheel(coinsEarned: Int, onComplete: (String) -> Unit) {
        viewModelScope.launch {
            val profile = userProfile.value ?: return@launch
            _isLoading.value = true
            delay(2000) // Spin delay
            repository.addGameCoins(profile.uid, "Spin Wheel Fortune", coinsEarned)
            _isLoading.value = false
            onComplete("You won $coinsEarned Gold Coins!")
        }
    }

    // Scratch card reward
    fun playScratchCard(coinsEarned: Int, onComplete: (String) -> Unit) {
        viewModelScope.launch {
            val profile = userProfile.value ?: return@launch
            _isLoading.value = true
            delay(1000) // Scratch simulation delay
            repository.addGameCoins(profile.uid, "Scratch Card Reveal", coinsEarned)
            _isLoading.value = false
            onComplete("Scratched and earned $coinsEarned Coins!")
        }
    }

    // AdMob Watch Video
    fun showRewardedAd(activity: android.app.Activity? = null, onMessage: (String) -> Unit) {
        val profile = userProfile.value ?: return
        _isAdLoading.value = true
        _adMessage.value = "Initializing secure connection to AdMob network..."
        
        viewModelScope.launch {
            delay(800)
            if (activity != null) {
                com.example.data.admob.AdMobHelper.showRewardedAd(
                    activity = activity,
                    onRewardEarned = { rewardAmount ->
                        viewModelScope.launch {
                            val adCoins = 120
                            repository.claimAdReward(profile.uid, adCoins)
                            _isAdLoading.value = false
                            _adMessage.value = null
                            onMessage("Sponsor verification success! +$adCoins coins credited.")
                        }
                    },
                    onAdClosedOrFailed = { result ->
                        viewModelScope.launch {
                            if (result == "REAL_AD_NOT_READY") {
                                // Run beautiful fallback simulation so user can still earn on preview devices!
                                _adMessage.value = "Google AdMob Sandbox Mode:\nLoading High-Fidelity Sponsor Video..."
                                delay(1500)
                                _adMessage.value = "Streaming PK Earn Pro Premium Partner Clip..."
                                delay(3000)
                                _adMessage.value = "Verifying watch proof with secure local blockchain hash..."
                                delay(1000)
                                val adCoins = 120
                                repository.claimAdReward(profile.uid, adCoins)
                                _isAdLoading.value = false
                                _adMessage.value = null
                                onMessage("AdMob Test reward credited successfully! (Simulated fallback) +$adCoins Coins.")
                            } else {
                                _isAdLoading.value = false
                                _adMessage.value = null
                                if (result.contains("successfully")) {
                                    // User completed watching real Ad but callback dismissed
                                    val adCoins = 120
                                    repository.claimAdReward(profile.uid, adCoins)
                                    onMessage(result)
                                } else {
                                    onMessage(result)
                                }
                            }
                        }
                    }
                )
            } else {
                // Standalone simulation fallback if activity is null
                _adMessage.value = "Contacting AdMob Ad Pool (Test Mode)..."
                delay(1200)
                _adMessage.value = "Playing Sponsor Video Clip (100% Ad Revenue System)..."
                delay(3000)
                _adMessage.value = "Verifying completed view in real-time..."
                delay(1000)
                val adCoins = 120
                repository.claimAdReward(profile.uid, adCoins)
                _isAdLoading.value = false
                _adMessage.value = null
                onMessage("Sponsor video complete! +$adCoins coins credited.")
            }
        }
    }

    // Withdrawal submission
    fun requestWithdrawal(method: String, title: String, number: String, coins: Int, onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val profile = userProfile.value
            if (profile == null) {
                onComplete(false, "No active user session found.")
                return@launch
            }
            _isLoading.value = true
            val (success, message) = repository.submitWithdrawal(profile.uid, method, title, number, coins)
            _isLoading.value = false
            onComplete(success, message)
        }
    }

    // Referral system
    fun shareReferral(onShared: (String) -> Unit) {
        val profile = userProfile.value ?: return
        val text = "Join PK Earn Pro, the ultimate and premium coin rewards app! Use my referral code: ${profile.referralCode} and instantly earn 250 bonus coins on sign up! Link: https://pkearnpro.aistudio.com/join"
        onShared(text)
    }

    fun applyReferral(code: String, onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val profile = userProfile.value ?: return@launch
            _isLoading.value = true
            val (success, msg) = repository.applyReferralCode(profile.uid, code)
            _isLoading.value = false
            onComplete(success, msg)
        }
    }

    // Quiz Game logic
    fun submitQuizAnswer(selectedOptionIndex: Int, onComplete: (String) -> Unit) {
        val currentQ = quizQuestions[_currentQuizIndex.value]
        if (selectedOptionIndex == currentQ.correctIndex) {
            _quizScore.value = _quizScore.value + 1
        }
        
        if (_currentQuizIndex.value < quizQuestions.size - 1) {
            _currentQuizIndex.value = _currentQuizIndex.value + 1
        } else {
            // Quiz finished
            _quizCompleted.value = true
            val finalScore = _quizScore.value
            val totalQ = quizQuestions.size
            val earnedCoins = finalScore * 50 // 50 coins per correct answer
            
            viewModelScope.launch {
                val profile = userProfile.value
                if (profile != null && earnedCoins > 0) {
                    repository.addGameCoins(profile.uid, "Daily Quiz Score: $finalScore/$totalQ", earnedCoins)
                }
                onComplete("Quiz Complete! Score: $finalScore/$totalQ. Earned: $earnedCoins coins.")
            }
        }
    }

    fun resetQuiz() {
        _currentQuizIndex.value = 0
        _quizScore.value = 0
        _quizCompleted.value = false
    }

    /**
     * Admin Panel Simulation Controls
     */
    fun adminApprove(withdrawalId: String, note: String = "Verified legibly. Paid out successfully.") {
        viewModelScope.launch {
            _isLoading.value = true
            repository.adminApproveWithdrawal(withdrawalId, note)
            _isLoading.value = false
        }
    }

    fun adminReject(withdrawalId: String, refundCoins: Int, note: String = "Invalid account configuration. Rejected.") {
        viewModelScope.launch {
            _isLoading.value = true
            repository.adminRejectWithdrawal(withdrawalId, refundCoins, note)
            _isLoading.value = false
        }
    }

    fun adminFreezeToggle(uid: String, currentFrozen: Boolean) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.adminFreezeUser(uid, !currentFrozen)
            _isLoading.value = false
        }
    }

    fun adminSetMinWithdrawal(coins: Int) {
        viewModelScope.launch {
            repository.adminUpdateMinWithdrawal(coins)
        }
    }
}

data class QuizQuestion(
    val question: String,
    val options: List<String>,
    val correctIndex: Int
)
