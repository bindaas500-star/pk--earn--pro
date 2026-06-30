package com.example.data.admob

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

object AdMobHelper {
    private const val TAG = "AdMobHelper"
    // Google AdMob Standard Test Rewarded Ad Unit ID
    private const val REWARDED_TEST_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"

    private var mRewardedAd: RewardedAd? = null
    private var isAdLoading = false

    fun initialize(context: Context) {
        try {
            MobileAds.initialize(context) { status ->
                Log.d(TAG, "AdMob Initialized: $status")
                loadRewardedAd(context)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize MobileAds: ", e)
        }
    }

    fun loadRewardedAd(context: Context, onLoaded: ((Boolean) -> Unit)? = null) {
        if (isAdLoading || mRewardedAd != null) {
            onLoaded?.invoke(mRewardedAd != null)
            return
        }
        isAdLoading = true
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(context, REWARDED_TEST_AD_UNIT_ID, adRequest, object : RewardedAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.e(TAG, "Ad failed to load: ${adError.message}")
                mRewardedAd = null
                isAdLoading = false
                onLoaded?.invoke(false)
            }

            override fun onAdLoaded(rewardedAd: RewardedAd) {
                Log.d(TAG, "Ad loaded successfully.")
                mRewardedAd = rewardedAd
                isAdLoading = false
                onLoaded?.invoke(true)
            }
        })
    }

    fun showRewardedAd(activity: Activity, onRewardEarned: (Int) -> Unit, onAdClosedOrFailed: (String) -> Unit) {
        val ad = mRewardedAd
        if (ad != null) {
            ad.fullScreenContentCallback = object : com.google.android.gms.ads.FullScreenContentCallback() {
                override fun onAdClicked() {
                    Log.d(TAG, "Ad was clicked.")
                }

                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "Ad dismissed full screen content.")
                    mRewardedAd = null
                    // Preload next ad
                    loadRewardedAd(activity)
                    onAdClosedOrFailed("Ad watched successfully! Return to claim reward.")
                }

                override fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError) {
                    Log.e(TAG, "Ad failed to show full screen content: ${adError.message}")
                    mRewardedAd = null
                    loadRewardedAd(activity)
                    onAdClosedOrFailed("Ad playback failed. Fallback simulation credited.")
                }

                override fun onAdImpression() {
                    Log.d(TAG, "Ad recorded an impression.")
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "Ad showed full screen content.")
                }
            }
            ad.show(activity) { rewardItem ->
                val rewardAmount = rewardItem.amount
                // We reward our standard 120 coins if test reward is different
                val coinsToReward = if (rewardAmount > 0) rewardAmount else 120
                onRewardEarned(coinsToReward)
            }
        } else {
            // Attempt to load for next time
            loadRewardedAd(activity)
            onAdClosedOrFailed("REAL_AD_NOT_READY")
        }
    }
}
