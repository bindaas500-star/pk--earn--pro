package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface EarnDao {
    // User Profile Queries
    @Query("SELECT * FROM user_profile LIMIT 1")
    fun getUserProfile(): Flow<UserProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: UserProfileEntity)

    @Query("UPDATE user_profile SET coinBalance = coinBalance + :coins, totalEarningsCoins = totalEarningsCoins + :coins WHERE uid = :uid")
    suspend fun addCoins(uid: String, coins: Int)

    @Query("UPDATE user_profile SET coinBalance = coinBalance - :coins WHERE uid = :uid")
    suspend fun deductCoins(uid: String, coins: Int)

    @Query("UPDATE user_profile SET isFrozen = :isFrozen WHERE uid = :uid")
    suspend fun freezeAccount(uid: String, isFrozen: Boolean)

    @Query("UPDATE user_profile SET lastSpinTimestamp = :timestamp WHERE uid = :uid")
    suspend fun updateLastSpinTimestamp(uid: String, timestamp: Long)

    @Query("UPDATE user_profile SET lastScratchTimestamp = :timestamp WHERE uid = :uid")
    suspend fun updateLastScratchTimestamp(uid: String, timestamp: Long)

    // Transaction History Queries
    @Query("SELECT * FROM transaction_history ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    // Earning Tasks Queries
    @Query("SELECT * FROM earning_task")
    fun getAllTasks(): Flow<List<EarningTaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<EarningTaskEntity>)

    @Query("UPDATE earning_task SET isCompleted = 1 WHERE id = :taskId")
    suspend fun completeTask(taskId: String)

    // Withdrawal Queries
    @Query("SELECT * FROM withdrawal_request ORDER BY timestamp DESC")
    fun getAllWithdrawals(): Flow<List<WithdrawalRequestEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWithdrawal(withdrawal: WithdrawalRequestEntity)

    @Query("UPDATE withdrawal_request SET status = :status, adminNote = :note WHERE id = :id")
    suspend fun updateWithdrawalStatus(id: String, status: String, note: String)

    // Leaderboard Queries
    @Query("SELECT * FROM leaderboard ORDER BY rank ASC")
    fun getLeaderboard(): Flow<List<LeaderboardUserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLeaderboard(users: List<LeaderboardUserEntity>)

    @Query("DELETE FROM leaderboard")
    suspend fun clearLeaderboard()
}
