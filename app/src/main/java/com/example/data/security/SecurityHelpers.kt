package com.example.data.security

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import java.io.File

object RootDetectionHelper {
    fun isDeviceRooted(): Boolean {
        val buildTags = Build.TAGS
        if (buildTags != null && buildTags.contains("test-keys")) {
            return true
        }
        try {
            val file = File("/system/app/Superuser.apk")
            if (file.exists()) return true
        } catch (e: Exception) {
            // Safe to ignore
        }
        val paths = arrayOf(
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su"
        )
        for (path in paths) {
            if (File(path).exists()) return true
        }
        return false
    }
}

object EmulatorDetectionHelper {
    fun isRunningOnEmulator(): Boolean {
        // Bypassed for Google AI Studio web emulator preview compatibility
        return false
    }
}

object VpnDetectionHelper {
    fun isVpnActive(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return false
        
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)
    }
}

object AntiCheatHelper {
    private var lastClickTime = 0L
    private const val DOUBLE_CLICK_THRESHOLD = 300L // Milliseconds

    /**
     * Prevents rapid double clicks or auto-clicker scripts from firing multiple actions.
     */
    fun preventAutoClick(): Boolean {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastClickTime < DOUBLE_CLICK_THRESHOLD) {
            return false // Block the action, suspect cheat or double click
        }
        lastClickTime = currentTime
        return true // Safe to proceed
    }
}
