package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.core.content.ContextCompat
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.admob.AdMobHelper
import com.example.data.notification.NotificationHelper
import com.example.ui.screens.*
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.EarnViewModel

class MainActivity : ComponentActivity() {
    
    private val viewModel: EarnViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Initialize Notification Channels
        NotificationHelper.createNotificationChannel(this)
        
        // Initialize Google AdMob SDK
        AdMobHelper.initialize(this)
        
        // Request runtime notification permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                val requestPermissionLauncher = registerForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { _ -> }
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        
        setContent {
            MyApplicationTheme {
                // Read security warning states (excluding emulator check)
                val isRooted by viewModel.isRooted.collectAsState()
                val isVpnActive by viewModel.isVpnActive.collectAsState()

                // Only show security screen if a real threat (rooted or VPN) is active
                if (isRooted || isVpnActive) {
                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        SecurityAlarmScreen(
                            viewModel = viewModel,
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                } else {
                    AppNavigationHost(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun AppNavigationHost(viewModel: EarnViewModel) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "auth",
        modifier = Modifier.fillMaxSize()
    ) {
        composable("splash") {
            SplashScreen(viewModel = viewModel) { nextScreen ->
                navController.navigate(nextScreen) {
                    popUpTo("splash") { inclusive = true }
                }
            }
        }

        composable("auth") {
            AuthScreen(viewModel = viewModel) {
                navController.navigate("main_container") {
                    popUpTo("auth") { inclusive = true }
                }
            }
        }

        // Main App Shell with Bottom Navigation
        composable("main_container") {
            MainContainer(viewModel = viewModel, onLogout = {
                navController.navigate("auth") {
                    popUpTo("main_container") { inclusive = true }
                }
            }, onNavigateToSubpage = { route ->
                navController.navigate(route)
            })
        }

        // Subpages that hide bottom navigation for immersive game experience
        composable("spin") {
            SpinWheelScreen(viewModel = viewModel, onNavigateBack = {
                navController.popBackStack()
            })
        }

        composable("scratch") {
            ScratchCardScreen(viewModel = viewModel, onNavigateBack = {
                navController.popBackStack()
            })
        }

        composable("quiz") {
            DailyQuizScreen(viewModel = viewModel, onNavigateBack = {
                navController.popBackStack()
            })
        }

        composable("tasks") {
            OfferwallTasksScreen(viewModel = viewModel, onNavigateBack = {
                navController.popBackStack()
            })
        }

        composable("withdraw") {
            WithdrawScreen(viewModel = viewModel, onNavigateBack = {
                navController.popBackStack()
            })
        }

        composable("admin") {
            AdminDashboardScreen(viewModel = viewModel, onNavigateBack = {
                navController.popBackStack()
            })
        }
    }
}

@Composable
fun MainContainer(
    viewModel: EarnViewModel,
    onLogout: () -> Unit,
    onNavigateToSubpage: (String) -> Unit
) {
    val subNavController = rememberNavController()
    val navBackStackEntry by subNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars),
                containerColor = Color(0xFF0C130E),
                contentColor = EmeraldPrimary
            ) {
                NavigationBarItem(
                    selected = currentRoute == "dashboard",
                    onClick = {
                        subNavController.navigate("dashboard") {
                            popUpTo(subNavController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(imageVector = Icons.Default.Home, contentDescription = "Dashboard") },
                    label = { Text("Home") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = EmeraldPrimary,
                        unselectedIconColor = Color.Gray,
                        indicatorColor = Color(0xFF162F1F)
                    ),
                    modifier = Modifier.testTag("nav_dashboard")
                )

                NavigationBarItem(
                    selected = currentRoute == "leaderboard",
                    onClick = {
                        subNavController.navigate("leaderboard") {
                            popUpTo(subNavController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(imageVector = Icons.Default.EmojiEvents, contentDescription = "Leaderboard") },
                    label = { Text("Leaderboard") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = GoldAccent,
                        unselectedIconColor = Color.Gray,
                        indicatorColor = Color(0xFF2F2416)
                    ),
                    modifier = Modifier.testTag("nav_leaderboard")
                )

                NavigationBarItem(
                    selected = currentRoute == "wallet",
                    onClick = {
                        subNavController.navigate("wallet") {
                            popUpTo(subNavController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(imageVector = Icons.Default.Wallet, contentDescription = "Wallet") },
                    label = { Text("Wallet") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = EmeraldPrimary,
                        unselectedIconColor = Color.Gray,
                        indicatorColor = Color(0xFF162F1F)
                    ),
                    modifier = Modifier.testTag("nav_wallet")
                )

                NavigationBarItem(
                    selected = currentRoute == "profile",
                    onClick = {
                        subNavController.navigate("profile") {
                            popUpTo(subNavController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(imageVector = Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = EmeraldPrimary,
                        unselectedIconColor = Color.Gray,
                        indicatorColor = Color(0xFF162F1F)
                    ),
                    modifier = Modifier.testTag("nav_profile")
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = subNavController,
            startDestination = "leaderboard",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("dashboard") {
                HomeDashboardScreen(viewModel = viewModel, onNavigate = onNavigateToSubpage)
            }
            
            composable("leaderboard") {
                LeaderboardScreen(viewModel = viewModel, onNavigateBack = {
                    subNavController.popBackStack()
                })
            }

            composable("wallet") {
                WalletScreen(viewModel = viewModel, onNavigateBack = {
                    subNavController.popBackStack()
                }, onNavigateWithdraw = {
                    onNavigateToSubpage("withdraw")
                })
            }

            composable("withdraw") {
                WithdrawalScreen(viewModel = viewModel, onNavigateBack = {
                    subNavController.popBackStack()
                })
            }

            composable("profile") {
                ProfileScreen(viewModel = viewModel, onLogout = onLogout, onNavigateBack = {
                    subNavController.popBackStack()
                })
            }
        }
    }
}
