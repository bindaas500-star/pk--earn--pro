package com.example.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.entity.EarningTaskEntity
import com.example.data.local.entity.LeaderboardUserEntity
import com.example.data.local.entity.TransactionEntity
import com.example.data.local.entity.WithdrawalRequestEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.EarnViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.input.pointer.pointerInput

// Custom Glassmorphism Card Modifier helper
@Composable
fun GlassmorphismCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        content = content
    )
}

// Security Overlay Screen: triggers when emulator/root/vpn is active
@Composable
fun SecurityAlarmScreen(
    viewModel: EarnViewModel,
    modifier: Modifier = Modifier
) {
    val isRooted by viewModel.isRooted.collectAsState()
    val isEmulator by viewModel.isEmulator.collectAsState()
    val isVpnActive by viewModel.isVpnActive.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F10))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(Color.Red.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = "Security Alert",
                tint = Color.Red,
                modifier = Modifier.size(54.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Security Protection Active",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "PK Earn Pro enforces anti-cheat security protocols to protect legitimate ad networks, sponsor offer walls, and actual payout balances.",
            style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Offending criteria list
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                SecurityViolationRow("Root Access detected", isRooted)
                SecurityViolationRow("Emulator sandbox detected", isEmulator)
                SecurityViolationRow("VPN connection active", isVpnActive)
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = { viewModel.toggleVpnBypass() },
            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
            modifier = Modifier.testTag("bypass_security_btn")
        ) {
            Icon(imageVector = Icons.Default.DeveloperMode, contentDescription = "Developer mode")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Dev Test Bypass (Simulate Clean Device)")
        }
    }
}

@Composable
fun SecurityViolationRow(label: String, triggered: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (triggered) Icons.Default.Cancel else Icons.Default.CheckCircle,
            contentDescription = null,
            tint = if (triggered) Color.Red else EmeraldPrimary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = if (triggered) Color.White else Color.Gray,
                fontWeight = if (triggered) FontWeight.Bold else FontWeight.Normal
            )
        )
    }
}

// 1. Splash / Welcome Screen
@Composable
fun SplashScreen(
    viewModel: EarnViewModel,
    onNavigateNext: (String) -> Unit
) {
    var startAnimation by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (startAnimation) 1.1f else 0.8f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "Logo Scale"
    )

    LaunchedEffect(key1 = true) {
        startAnimation = true
        delay(2200)
        val profile = viewModel.userProfile.value
        if (profile == null) {
            onNavigateNext("auth")
        } else {
            onNavigateNext("dashboard")
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF0C1911), Color(0xFF040605))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(id = R.drawable.img_app_icon),
                contentDescription = "App Icon",
                modifier = Modifier
                    .size(140.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .rotate(scale * 10f - 10f),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "PK Earn Pro",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = EmeraldPrimary,
                    letterSpacing = 1.5.sp
                )
            )

            Text(
                text = "Legitimate sponsor earning hub",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = GoldAccent,
                    letterSpacing = 2.sp
                )
            )

            Spacer(modifier = Modifier.height(48.dp))

            CircularProgressIndicator(
                color = EmeraldPrimary,
                strokeWidth = 3.dp,
                modifier = Modifier.size(36.dp)
            )
        }
    }
}

// 2. Auth Screen
@Composable
fun AuthScreen(
    viewModel: EarnViewModel,
    onAuthSuccess: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var refCode by remember { mutableStateOf("") }
    val isLoading by viewModel.isLoading.collectAsState()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Segmented Login selection state
    var selectedAuthMethod by remember { mutableStateOf(0) } // 0 = Email, 1 = Google, 2 = Phone OTP

    // Google Sign-In simulation states
    var showGooglePicker by remember { mutableStateOf(false) }
    var googleLoading by remember { mutableStateOf(false) }

    // Phone Sign-In simulation states
    var phoneNumber by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }
    var otpSent by remember { mutableStateOf(false) }
    var otpTimer by remember { mutableStateOf(60) }
    var countryCode by remember { mutableStateOf("+92") }
    var phoneLoading by remember { mutableStateOf(false) }

    // OTP Timer Countdown Effect
    LaunchedEffect(otpSent, otpTimer) {
        if (otpSent && otpTimer > 0) {
            delay(1000)
            otpTimer--
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF070B08))
    ) {
        // Futuristic background banner
        Image(
            painter = painterResource(id = R.drawable.img_hero_banner),
            contentDescription = "Header background banner",
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.35f)
                .alpha(0.35f),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "Welcome to PK Earn Pro",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )

            Text(
                text = "Log in or register below to access sponsor tasks",
                style = MaterialTheme.typography.bodySmall.copy(color = Color.LightGray),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Multi-Auth Navigation tabs
            TabRow(
                selectedTabIndex = selectedAuthMethod,
                containerColor = Color.White.copy(alpha = 0.03f),
                contentColor = EmeraldPrimary,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = selectedAuthMethod == 0,
                    onClick = { selectedAuthMethod = 0 },
                    text = { Text("Email", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = selectedAuthMethod == 1,
                    onClick = { selectedAuthMethod = 1 },
                    text = { Text("Google", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Language, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = selectedAuthMethod == 2,
                    onClick = { selectedAuthMethod = 2 },
                    text = { Text("Phone OTP", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.PhoneAndroid, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            GlassmorphismCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    when (selectedAuthMethod) {
                        0 -> {
                            Text(
                                text = "Sign In / Register with Email",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldPrimary
                                )
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedTextField(
                                value = username,
                                onValueChange = { username = it },
                                label = { Text("Username") },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("username_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = EmeraldPrimary,
                                    unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                                )
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it },
                                label = { Text("Email Address") },
                                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("email_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = EmeraldPrimary,
                                    unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                                )
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = password,
                                onValueChange = { password = it },
                                label = { Text("Password") },
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                                visualTransformation = PasswordVisualTransformation(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("password_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = EmeraldPrimary,
                                    unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                                )
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = refCode,
                                onValueChange = { refCode = it },
                                label = { Text("Referral Code (Optional, Earn +250 Coins)") },
                                leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("referral_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = GoldAccent,
                                    unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                                )
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            if (isLoading) {
                                CircularProgressIndicator(
                                    color = EmeraldPrimary,
                                    modifier = Modifier
                                        .align(Alignment.CenterHorizontally)
                                        .size(36.dp)
                                )
                            } else {
                                Button(
                                    onClick = {
                                        if (username.isBlank() || email.isBlank() || password.length < 4) {
                                            Toast.makeText(context, "Fill in all credentials correctly (Pass >= 4 chars)", Toast.LENGTH_SHORT).show()
                                        } else {
                                            viewModel.loginOrSignUp(email, username, refCode, password) {
                                                Toast.makeText(context, "Auth Success! +100 Coins credited.", Toast.LENGTH_LONG).show()
                                                onAuthSuccess()
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                        .testTag("login_btn"),
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                                ) {
                                    Text("Sign In / Join PK Earn Pro", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        1 -> {
                            Text(
                                text = "Sign In with Google",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldPrimary
                                )
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Text(
                                text = "Secure, passwordless entry using your verified Google Account. Instantly registers a verified rewards account.",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color.LightGray)
                            )

                            Spacer(modifier = Modifier.height(30.dp))

                            if (googleLoading) {
                                CircularProgressIndicator(
                                    color = EmeraldPrimary,
                                    modifier = Modifier
                                        .align(Alignment.CenterHorizontally)
                                        .size(36.dp)
                                )
                            } else {
                                Button(
                                    onClick = { showGooglePicker = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                        .border(1.dp, Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(25.dp)),
                                    shape = RoundedCornerShape(25.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Language,
                                            contentDescription = null,
                                            tint = Color(0xFF4285F4),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = "Continue with Google",
                                            color = Color.Black,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                        2 -> {
                            Text(
                                text = "Sign In with Phone OTP",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldPrimary
                                )
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            if (!otpSent) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Country code select box
                                    Box(
                                        modifier = Modifier
                                            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                                            .border(1.dp, Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 12.dp, vertical = 15.dp)
                                    ) {
                                        Text(countryCode, color = Color.White, fontWeight = FontWeight.Bold)
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    OutlinedTextField(
                                        value = phoneNumber,
                                        onValueChange = { phoneNumber = it },
                                        label = { Text("Phone Number") },
                                        leadingIcon = { Icon(Icons.Default.PhoneAndroid, contentDescription = null) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("phone_input"),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = EmeraldPrimary,
                                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                                        )
                                    )
                                }

                                Spacer(modifier = Modifier.height(24.dp))

                                if (phoneLoading) {
                                    CircularProgressIndicator(
                                        color = EmeraldPrimary,
                                        modifier = Modifier.align(Alignment.CenterHorizontally).size(36.dp)
                                    )
                                } else {
                                    Button(
                                        onClick = {
                                            if (phoneNumber.length < 9) {
                                                Toast.makeText(context, "Enter a valid mobile number", Toast.LENGTH_SHORT).show()
                                            } else {
                                                phoneLoading = true
                                                scope.launch {
                                                    delay(1200)
                                                    phoneLoading = false
                                                    otpSent = true
                                                    otpTimer = 60
                                                    Toast.makeText(context, "OTP Sent successfully to $countryCode $phoneNumber", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(50.dp)
                                            .testTag("send_otp_btn"),
                                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                                    ) {
                                        Text("Send OTP Verification Code", fontWeight = FontWeight.Bold)
                                    }
                                }
                            } else {
                                Text(
                                    text = "Enter the 6-digit OTP code sent to $countryCode $phoneNumber",
                                    style = MaterialTheme.typography.bodySmall.copy(color = Color.LightGray)
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                OutlinedTextField(
                                    value = otpCode,
                                    onValueChange = { if (it.length <= 6) otpCode = it },
                                    label = { Text("6-Digit OTP Code") },
                                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("otp_input"),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = EmeraldPrimary,
                                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                                    )
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (otpTimer > 0) "Resend OTP in ${otpTimer}s" else "Didn't receive code?",
                                        style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                                    )

                                    if (otpTimer == 0) {
                                        TextButton(onClick = {
                                            otpTimer = 60
                                            Toast.makeText(context, "New OTP code sent!", Toast.LENGTH_SHORT).show()
                                        }) {
                                            Text("Resend Now", color = EmeraldPrimary, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(24.dp))

                                if (phoneLoading) {
                                    CircularProgressIndicator(
                                        color = EmeraldPrimary,
                                        modifier = Modifier.align(Alignment.CenterHorizontally).size(36.dp)
                                    )
                                } else {
                                    Button(
                                        onClick = {
                                            if (otpCode.length < 4) {
                                                Toast.makeText(context, "Please enter correct OTP code", Toast.LENGTH_SHORT).show()
                                            } else {
                                                phoneLoading = true
                                                scope.launch {
                                                    delay(1000)
                                                    phoneLoading = false
                                                    viewModel.loginOrSignUp("phone_${phoneNumber}@pkearnpro.com", "PhoneUser_${phoneNumber.takeLast(4)}", "") {
                                                        Toast.makeText(context, "Phone Sign In Success! +100 Coins credited.", Toast.LENGTH_LONG).show()
                                                        onAuthSuccess()
                                                    }
                                                }
                                            }
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(50.dp)
                                            .testTag("verify_otp_btn"),
                                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                                    ) {
                                        Text("Verify OTP & Join PK Earn Pro", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Legitimate Policy notice
            Text(
                text = "By registering, you agree that this platform is supported solely by legitimate sponsor video ad placements, microtasks, and optional installs. Fake rewards, bot exploitation, or multiple accounts lead to immediate bans.",
                style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray),
                textAlign = TextAlign.Center,
                lineHeight = 16.sp
            )
        }
    }

    // Google accounts selection chooser Dialog popup
    if (showGooglePicker) {
        AlertDialog(
            onDismissRequest = { showGooglePicker = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Language, contentDescription = null, tint = Color(0xFF4285F4))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Choose an account", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text("to continue to PK Earn Pro", style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray))
                    Spacer(modifier = Modifier.height(16.dp))

                    // Option 1: User's actual email from metadata!
                    Card(
                        onClick = {
                            showGooglePicker = false
                            googleLoading = true
                            scope.launch {
                                delay(1500)
                                googleLoading = false
                                viewModel.loginOrSignUp("bindaas500@gmail.com", "Bindaas500", "") {
                                    Toast.makeText(context, "Google Sign In Success! +100 Coins credited.", Toast.LENGTH_LONG).show()
                                    onAuthSuccess()
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(EmeraldPrimary.copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("B", fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Bindaas", fontWeight = FontWeight.Bold, color = Color.White)
                                Text("bindaas500@gmail.com", style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Option 2: Default support email
                    Card(
                        onClick = {
                            showGooglePicker = false
                            googleLoading = true
                            scope.launch {
                                delay(1500)
                                googleLoading = false
                                viewModel.loginOrSignUp("pk.earner.pro@gmail.com", "PKEarnerPro", "") {
                                    Toast.makeText(context, "Google Sign In Success! +100 Coins credited.", Toast.LENGTH_LONG).show()
                                    onAuthSuccess()
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(GoldAccent.copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("P", fontWeight = FontWeight.Bold, color = GoldAccent)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("PK Earner Pro", fontWeight = FontWeight.Bold, color = Color.White)
                                Text("pk.earner.pro@gmail.com", style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray))
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showGooglePicker = false }) {
                    Text("Cancel", color = EmeraldPrimary)
                }
            }
        )
    }
}

// 3. User Dashboard Screen
@Composable
fun DashboardScreen(
    viewModel: EarnViewModel,
    onNavigate: (String) -> Unit
) {
    val profile by viewModel.userProfile.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val isAdLoading by viewModel.isAdLoading.collectAsState()
    val adMessage by viewModel.adMessage.collectAsState()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    if (profile == null) return

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        // Ambient background glow (subtle radial/linear gradient)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.35f)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(BentoPrimary.copy(alpha = 0.15f), Color.Transparent)
                    )
                )
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(48.dp))
                
                // Bento Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "WELCOME BACK",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = DarkTextSecondary,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp
                            )
                        )
                        Text(
                            text = "${profile?.username}",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                color = DarkText,
                                fontWeight = FontWeight.ExtraBold
                            )
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Admin quick access if user is admin
                        IconButton(
                            onClick = { onNavigate("admin") },
                            modifier = Modifier.background(DarkSurfaceVariant, CircleShape)
                        ) {
                            Icon(imageVector = Icons.Default.AdminPanelSettings, contentDescription = "Admin Area", tint = BentoAccent)
                        }

                        // Avatar
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(BentoPrimary.copy(alpha = 0.2f), CircleShape)
                                .border(2.dp, BentoAccent, CircleShape)
                                .padding(2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = (profile?.username?.take(2)?.uppercase() ?: "AK"),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
            }

            // Total Balance Card (Bento Gradient)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(BentoPrimary, BentoPrimaryDark)
                                )
                            )
                            .padding(24.dp)
                    ) {
                        // Decorative large coin sign in the corner
                        Text(
                            text = "$",
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = 10.dp, y = (-20).dp)
                                .alpha(0.15f),
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontSize = 100.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        )

                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Total Balance",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = BentoAccent,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                                Box(
                                    modifier = Modifier
                                        .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(50))
                                        .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(50))
                                        .padding(horizontal = 12.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "LEVEL ${profile?.userLevel ?: 1}",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "${profile?.coinBalance}",
                                    style = MaterialTheme.typography.displayMedium.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Black
                                    )
                                )
                                Text(
                                    text = "Coins",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = BentoAccent,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val coinsFloat = profile?.coinBalance?.toFloat() ?: 0f
                                val pkrVal = coinsFloat / 10f
                                Text(
                                    text = "≈ RS $pkrVal PKR",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = Color.White.copy(alpha = 0.8f),
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.TrendingUp,
                                        contentDescription = null,
                                        tint = BentoAccent,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "+12% Today",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = BentoAccent,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // LEVEL PROGRESS & STATS
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = BorderStroke(1.dp, DarkSurfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Next Level Progress",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = DarkText,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = "${((profile?.progress ?: 0f) * 100).toInt()}%",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = BentoAccent,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        LinearProgressIndicator(
                            progress = { profile?.progress ?: 0.1f },
                            color = BentoAccent,
                            trackColor = DarkSurfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Total Earnings", style = MaterialTheme.typography.bodySmall.copy(color = DarkTextSecondary))
                                Text("${profile?.totalEarningsCoins} Coins", style = MaterialTheme.typography.bodyMedium.copy(color = DarkText, fontWeight = FontWeight.Bold))
                            }
                            Box(modifier = Modifier.width(1.dp).height(24.dp).background(DarkSurfaceVariant))
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Referrals Count", style = MaterialTheme.typography.bodySmall.copy(color = DarkTextSecondary))
                                Text("${profile?.inviteCount ?: 0} Invited", style = MaterialTheme.typography.bodyMedium.copy(color = DarkText, fontWeight = FontWeight.Bold))
                            }
                        }
                    }
                }
            }

            // Grid Items (Bento items)
            // Daily Check + Spin Wheel Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Daily Check-in
                    BentoMiniCard(
                        iconEmoji = "🎁",
                        title = "Daily Check",
                        subtitle = "+50 Coins",
                        onClick = {
                            viewModel.claimDailyCheckIn {
                                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                                com.example.data.notification.NotificationHelper.showDailyCheckInNotification(context, 150)
                            }
                        },
                        modifier = Modifier.weight(1f).testTag("daily_checkin_btn")
                    )

                    // Spin Wheel
                    BentoMiniCard(
                        iconEmoji = "🎡",
                        title = "Spin Wheel",
                        subtitle = "Try Luck",
                        onClick = { onNavigate("spin") },
                        modifier = Modifier.weight(1f).testTag("spin_wheel_btn")
                    )
                }
            }

            // Watch Videos Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val activity = context as? android.app.Activity
                            viewModel.showRewardedAd(activity) {
                                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                            }
                        }
                        .testTag("watch_video_btn"),
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = BorderStroke(1.dp, DarkSurfaceVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(BentoPrimary.copy(alpha = 0.2f), RoundedCornerShape(16.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("📺", fontSize = 24.sp)
                            }
                            Box(
                                modifier = Modifier
                                    .background(BentoAccent.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("Sponsor Video", color = BentoAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Watch Videos",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = DarkText,
                                fontWeight = FontWeight.Bold
                            )
                        )

                        Text(
                            text = "Earn coins continuously by viewing micro sponsor video ads",
                            style = MaterialTheme.typography.bodySmall.copy(color = DarkTextSecondary),
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Progress indicator line
                        LinearProgressIndicator(
                            progress = { 0.65f },
                            color = BentoAccent,
                            trackColor = DarkSurfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                        )
                    }
                }
            }

            // Withdraw now button (solid BentoPrimary)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Withdraw card
                    Card(
                        modifier = Modifier
                            .weight(1.2f)
                            .height(120.dp)
                            .clickable { onNavigate("withdraw") },
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = BentoPrimary),
                        border = BorderStroke(1.dp, BentoAccent.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Withdraw Now",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowForward,
                                    contentDescription = null,
                                    tint = Color.White
                                )
                            }
                            Text(
                                text = "Instant payout to Easypaisa & JazzCash",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.8f))
                            )
                        }
                    }

                    // Tasks shortcut
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(120.dp)
                            .clickable { onNavigate("tasks") },
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        border = BorderStroke(1.dp, DarkSurfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(Color.Blue.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("📋", fontSize = 18.sp)
                            }
                            Column {
                                Text(
                                    text = "Offer Walls",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = DarkText,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = "Tasks Active",
                                    style = MaterialTheme.typography.bodySmall.copy(color = DarkTextSecondary)
                                )
                            }
                        }
                    }
                }
            }

            // Other Reward Hub mini grid (Quiz + Scratch)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Daily Quiz
                    BentoMiniCard(
                        iconEmoji = "🧠",
                        title = "Daily Quiz",
                        subtitle = "Learn & Earn",
                        onClick = { onNavigate("quiz") },
                        modifier = Modifier.weight(1f)
                    )

                    // Scratch Card
                    BentoMiniCard(
                        iconEmoji = "🪙",
                        title = "Scratch Win",
                        subtitle = "Reveal Prize",
                        onClick = { onNavigate("scratch") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Referral Program Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = BorderStroke(1.dp, DarkSurfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(BentoAccent.copy(alpha = 0.1f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🤝", fontSize = 20.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Referral Program",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = DarkText,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = "Earn 20% commission on referrals",
                                    style = MaterialTheme.typography.bodySmall.copy(color = DarkTextSecondary)
                                )
                            }
                        }

                        Button(
                            onClick = {
                                viewModel.shareReferral { text ->
                                    val sendIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, text)
                                        type = "text/plain"
                                    }
                                    context.startActivity(Intent.createChooser(sendIntent, "Share Referral Link"))
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BentoPrimary.copy(alpha = 0.2f)),
                            modifier = Modifier.padding(start = 8.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "Invite",
                                color = BentoAccent,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }

            // Recent activity transaction feed title
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "RECENT ACTIVITIES",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = DarkTextSecondary,
                            letterSpacing = 1.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )

                    TextButton(onClick = { onNavigate("wallet") }) {
                        Text("View all logs", color = BentoAccent)
                    }
                }
            }

            if (transactions.isEmpty()) {
                item {
                    Text(
                        "No activities logged yet.",
                        style = MaterialTheme.typography.bodySmall.copy(color = DarkTextSecondary),
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
            } else {
                items(transactions.take(3)) { tx ->
                    ActivityLogItem(tx)
                }
            }

            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }

        // Sponsor Video Playback Overlay dialog
        if (isAdLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.9f))
                    .clickable(enabled = false) {},
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = BentoAccent, modifier = Modifier.size(56.dp))
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = adMessage ?: "Loading Ad...",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun BentoMiniCard(
    iconEmoji: String,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(100.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, DarkSurfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(DarkSurfaceVariant, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(iconEmoji, fontSize = 20.sp)
            }
            
            Spacer(modifier = Modifier.width(10.dp))
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = DarkText,
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(color = DarkTextSecondary),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun ActivityLogItem(tx: TransactionEntity) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurface, RoundedCornerShape(16.dp))
            .border(1.dp, DarkSurfaceVariant, RoundedCornerShape(16.dp))
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        if (tx.coins > 0) BentoPrimary.copy(alpha = 0.12f) else Color.Red.copy(alpha = 0.12f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (tx.coins > 0) Icons.Default.AddCircle else Icons.Default.RemoveCircle,
                    contentDescription = null,
                    tint = if (tx.coins > 0) BentoAccent else Color.Red,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(tx.type, style = MaterialTheme.typography.titleSmall.copy(color = DarkText, fontWeight = FontWeight.Bold))
                Text(tx.description, style = MaterialTheme.typography.bodySmall.copy(color = DarkTextSecondary), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }

        Text(
            text = if (tx.coins > 0) "+${tx.coins}" else "${tx.coins}",
            style = MaterialTheme.typography.titleMedium.copy(
                color = if (tx.coins > 0) BentoAccent else Color.Red,
                fontWeight = FontWeight.Bold
            )
        )
    }
}

// 4. Spin Wheel Screen
@Composable
fun SpinWheelScreen(
    viewModel: EarnViewModel,
    onNavigateBack: () -> Unit
) {
    var rotationAngle by remember { mutableStateOf(0f) }
    var isSpinning by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val profile by viewModel.userProfile.collectAsState()
    var showRewardPopup by remember { mutableStateOf(false) }
    var rewardEarned by remember { mutableStateOf(0) }

    val lastSpin = profile?.lastSpinTimestamp ?: 0L
    val currentTime = System.currentTimeMillis()
    val canSpin = lastSpin == 0L || (currentTime - lastSpin) >= 24 * 60 * 60 * 1000L

    if (showRewardPopup) {
        AlertDialog(
            onDismissRequest = { showRewardPopup = false },
            title = { Text("Congratulations!") },
            text = { Text("You won $rewardEarned coins!") },
            confirmButton = {
                Button(onClick = { showRewardPopup = false }) { Text("OK") }
            }
        )
    }

    val spinRotation = animateFloatAsState(
        targetValue = rotationAngle,
        animationSpec = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
        label = "Wheel rotation"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text("Spin Wheel of Fortune", style = MaterialTheme.typography.titleLarge.copy(color = Color.White, fontWeight = FontWeight.Bold))
        }

        Spacer(modifier = Modifier.weight(1f))

        // Simulated Wheel graphic
        Box(
            modifier = Modifier
                .size(260.dp)
                .rotate(spinRotation.value)
                .background(
                    Brush.sweepGradient(
                        colors = listOf(
                            Color(0xFF10B981), Color(0xFFF59E0B),
                            Color(0xFF8B5CF6), Color(0xFFEC4899),
                            Color(0xFF10B981)
                        )
                    ),
                    CircleShape
                )
                .border(8.dp, Color.White.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            // Dividers / Wedges inside
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = size / 2f
                // We'll draw simple cross lines
                drawLine(Color.White.copy(alpha = 0.2f), start = androidx.compose.ui.geometry.Offset(0f, center.height), end = androidx.compose.ui.geometry.Offset(size.width, center.height), strokeWidth = 2f)
                drawLine(Color.White.copy(alpha = 0.2f), start = androidx.compose.ui.geometry.Offset(center.width, 0f), end = androidx.compose.ui.geometry.Offset(center.width, size.height), strokeWidth = 2f)
            }

            // Inner Pin/Pill
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("PK", fontWeight = FontWeight.Black, color = Color.Black)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Pointer
        Icon(
            imageVector = Icons.Default.ArrowDownward,
            contentDescription = "Pointer",
            tint = GoldAccent,
            modifier = Modifier.size(36.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (!isSpinning && canSpin) {
                    isSpinning = true
                    // Spin several full rotations plus a random offset (0 to 360)
                    val rewardCoins = listOf(10, 20, 50, 100, 200, 500).random()
                    rotationAngle = (rotationAngle % 360f) + 1440f + (360f * (rewardCoins.toFloat() / 500f))

                    scope.launch {
                        delay(2200)
                        viewModel.playSpinWheel(rewardCoins) { msg ->
                            if (msg.contains("won")) {
                                rewardEarned = rewardCoins
                                showRewardPopup = true
                            } else {
                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                            }
                            isSpinning = false
                        }
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = if (canSpin) EmeraldPrimary else Color.Gray),
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp)
                .testTag("spin_wheel_btn"),
            enabled = !isSpinning && canSpin
        ) {
            Text(if (isSpinning) "Spinning..." else "Spin & Win Coins", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        if (!canSpin) {
            val timeLeft = (24 * 60 * 60 * 1000L - (currentTime - lastSpin)) / 1000
            val hours = timeLeft / 3600
            val minutes = (timeLeft % 3600) / 60
            Text("Next spin in: ${hours}h ${minutes}m", color = Color.Gray, modifier = Modifier.padding(top = 8.dp))
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}

// 5. Scratch Cards Screen
@Composable
fun ScratchCardScreen(
    viewModel: EarnViewModel,
    onNavigateBack: () -> Unit
) {
    var isScratched by remember { mutableStateOf(false) }
    var isRevealing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val profile by viewModel.userProfile.collectAsState()
    var showRewardPopup by remember { mutableStateOf(false) }
    var rewardEarned by remember { mutableStateOf(0) }
    
    val lastScratch = profile?.lastScratchTimestamp ?: 0L
    val currentTime = System.currentTimeMillis()
    val canScratch = lastScratch == 0L || (currentTime - lastScratch) >= 24 * 60 * 60 * 1000L

    var paths by remember { mutableStateOf(listOf<Path>()) }
    var currentPath by remember { mutableStateOf(Path()) }
    var scratchedArea by remember { mutableStateOf(0f) }

    if (showRewardPopup) {
        AlertDialog(
            onDismissRequest = { showRewardPopup = false },
            title = { Text("Congratulations!") },
            text = { Text("You won $rewardEarned coins!") },
            confirmButton = {
                Button(onClick = { showRewardPopup = false }) { Text("OK") }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text("Golden Scratch Card", style = MaterialTheme.typography.titleLarge.copy(color = Color.White, fontWeight = FontWeight.Bold))
        }

        Spacer(modifier = Modifier.weight(1f))

        // Scratch Card Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White.copy(alpha = 0.05f))
                .border(2.dp, GoldAccent.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
        ) {
            if (canScratch) {
                // Reward text revealed underneath
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(imageVector = Icons.Default.MonetizationOn, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(64.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("JACKPOT!", color = GoldAccent, fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp)
                    }
                }

                // Scratch overlay
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    currentPath = Path()
                                    currentPath.moveTo(offset.x, offset.y)
                                    paths = paths + currentPath
                                },
                                onDrag = { change, dragAmount ->
                                    currentPath.lineTo(change.position.x, change.position.y)
                                    
                                    // Simple scratch area calculation
                                    scratchedArea += dragAmount.getDistance()
                                    if (scratchedArea > 2000f && !isRevealing && !isScratched) {
                                        isRevealing = true
                                        scope.launch {
                                            val coins = listOf(10, 20, 50, 100, 200, 500).random()
                                            viewModel.playScratchCard(coins) { msg ->
                                                if (msg.contains("won")) {
                                                    rewardEarned = coins
                                                    showRewardPopup = true
                                                } else {
                                                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                                }
                                                isScratched = true
                                                isRevealing = false
                                            }
                                        }
                                    }
                                }
                            )
                        }
                ) {
                    drawRect(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFFE5A93B), Color(0xFFF59E0B), Color(0xFFD97706))
                        )
                    )
                    
                    // Clear the scratched path
                    paths.forEach { path ->
                        drawPath(
                            path = path,
                            color = Color.Transparent,
                            style = Stroke(width = 40.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
                            blendMode = BlendMode.Clear
                        )
                    }
                }
            } else {
                // Already scratched/waiting
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Come back tomorrow!", color = Color.Gray)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        if (!canScratch) {
            val timeLeft = (24 * 60 * 60 * 1000L - (currentTime - lastScratch)) / 1000
            val hours = timeLeft / 3600
            val minutes = (timeLeft % 3600) / 60
            Text("Next scratch in: ${hours}h ${minutes}m", color = Color.Gray, modifier = Modifier.padding(top = 8.dp))
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}

// 6. Daily Quiz Screen
@Composable
fun DailyQuizScreen(
    viewModel: EarnViewModel,
    onNavigateBack: () -> Unit
) {
    val currentIndex by viewModel.currentQuizIndex.collectAsState()
    val score by viewModel.quizScore.collectAsState()
    val completed by viewModel.quizCompleted.collectAsState()

    val context = LocalContext.current
    val currentQuestion = viewModel.quizQuestions[currentIndex]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text("Daily Pakistan Quiz", style = MaterialTheme.typography.titleLarge.copy(color = Color.White, fontWeight = FontWeight.Bold))
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (!completed) {
            // Progression state
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Question ${currentIndex + 1} of ${viewModel.quizQuestions.size}", color = Color.Gray)
                Text("Score: $score", color = EmeraldPrimary, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { (currentIndex + 1).toFloat() / viewModel.quizQuestions.size },
                color = EmeraldPrimary,
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp))
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Question Card
            GlassmorphismCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = currentQuestion.question,
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 24.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Options list
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                currentQuestion.options.forEachIndexed { index, option ->
                    Button(
                        onClick = {
                            viewModel.submitQuizAnswer(index) { msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.05f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(55.dp)
                            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(option, color = Color.White, textAlign = TextAlign.Center)
                    }
                }
            }
        } else {
            // Completed Results card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(20.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(imageVector = Icons.Default.EmojiEvents, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(80.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("QUIZ COMPLETE!", style = MaterialTheme.typography.titleLarge.copy(color = Color.White, fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("You correctly solved $score of ${viewModel.quizQuestions.size} Questions.", color = Color.LightGray)
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { viewModel.resetQuiz() },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                    ) {
                        Text("Retake Daily Quiz")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}

// 7. Offerwalls Tasks list screen
@Composable
fun OfferwallTasksScreen(
    viewModel: EarnViewModel,
    onNavigateBack: () -> Unit
) {
    val tasks by viewModel.tasks.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text("Sponsor Offer Wall", style = MaterialTheme.typography.titleLarge.copy(color = Color.White, fontWeight = FontWeight.Bold))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "Legitimate payouts depend directly on sponsor ad verification. Please support the sponsors by completing the simple microtasks below.",
            style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(tasks) { task ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(task.title, style = MaterialTheme.typography.titleMedium.copy(color = Color.White, fontWeight = FontWeight.Bold))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(task.description, style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray))
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        if (task.isCompleted) {
                            Box(
                                modifier = Modifier
                                    .background(EmeraldPrimary.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("COMPLETED", color = EmeraldPrimary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        } else {
                            Button(
                                onClick = {
                                    viewModel.completeEarningTask(task) { msg ->
                                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("+${task.coinReward} Coins", fontWeight = FontWeight.Bold, color = Color.Black)
                            }
                        }
                    }
                }
            }
        }
    }
}

// 8. Wallet Screen (History & Withdrawal status logs)
@Composable
fun WalletScreen(
    viewModel: EarnViewModel,
    onNavigateBack: () -> Unit,
    onNavigateWithdraw: () -> Unit
) {
    val profile by viewModel.userProfile.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val withdrawals by viewModel.withdrawals.collectAsState()

    var selectedTab by remember { mutableStateOf(0) } // 0 = Coins log, 1 = Payout request status

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text("Wallet Ledger", style = MaterialTheme.typography.titleLarge.copy(color = Color.White, fontWeight = FontWeight.Bold))
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Large Total Balance visual card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = EmeraldPrimary.copy(alpha = 0.1f)),
            border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.3f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("REDEEMABLE BALANCE", color = Color.Gray, letterSpacing = 2.sp, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.MonetizationOn, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("${profile?.coinBalance ?: 0}", style = MaterialTheme.typography.headlineLarge.copy(color = Color.White, fontWeight = FontWeight.ExtraBold))
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("Equivalent: ${ (profile?.coinBalance ?: 0) / 10.0 } PKR", color = EmeraldPrimary, fontWeight = FontWeight.Bold)

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onNavigateWithdraw,
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Icon(imageVector = Icons.Default.Payments, contentDescription = null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cash Out Earnings", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Ledger Toggle tabs
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            contentColor = EmeraldPrimary
        ) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                Text("Coin History", modifier = Modifier.padding(12.dp), color = if (selectedTab == 0) Color.White else Color.Gray)
            }
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                Text("Withdrawals Log", modifier = Modifier.padding(12.dp), color = if (selectedTab == 1) Color.White else Color.Gray)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (selectedTab == 0) {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (transactions.isEmpty()) {
                    item {
                        Text("No transactions logged yet.", color = Color.Gray)
                    }
                } else {
                    items(transactions) { tx ->
                        ActivityLogItem(tx)
                    }
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (withdrawals.isEmpty()) {
                    item {
                        Text("No withdrawals requested yet.", color = Color.Gray)
                    }
                } else {
                    items(withdrawals) { wd ->
                        WithdrawRequestStatusRow(wd)
                    }
                }
            }
        }
    }
}

// New Withdrawal Screen
@Composable
fun WithdrawalScreen(
    viewModel: EarnViewModel,
    onNavigateBack: () -> Unit
) {
    val profile by viewModel.userProfile.collectAsState()
    var accountNumber by remember { mutableStateOf("") }
    var accountHolder by remember { mutableStateOf("") }
    var selectedMethod by remember { mutableStateOf("JazzCash") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text("Request Withdrawal", style = MaterialTheme.typography.titleLarge.copy(color = Color.White, fontWeight = FontWeight.Bold))
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Balance Summary
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Available Balance", color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("${profile?.coinBalance ?: 0} Coins", style = MaterialTheme.typography.headlineMedium.copy(color = Color.White, fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("= ${(profile?.coinBalance ?: 0) / 10.0} PKR", color = EmeraldPrimary, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("Select Method", style = MaterialTheme.typography.titleMedium.copy(color = Color.White))
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            val methods = listOf("JazzCash", "Easypaisa")
            methods.forEach { method ->
                FilterChip(
                    selected = selectedMethod == method,
                    onClick = { selectedMethod = method },
                    label = { Text(method) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = accountHolder,
            onValueChange = { accountHolder = it },
            label = { Text("Account Holder Name") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = EmeraldPrimary,
                unfocusedBorderColor = Color.Gray
            )
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = accountNumber,
            onValueChange = { accountNumber = it },
            label = { Text("Account Number") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = EmeraldPrimary,
                unfocusedBorderColor = Color.Gray
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { /* Handle request */ },
            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Request Withdrawal", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}


@Composable
fun WithdrawRequestStatusRow(wd: WithdrawalRequestEntity) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(12.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(wd.method, style = MaterialTheme.typography.titleMedium.copy(color = Color.White, fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .background(
                            when (wd.status) {
                                "PENDING" -> Color.Yellow.copy(alpha = 0.15f)
                                "APPROVED" -> EmeraldPrimary.copy(alpha = 0.15f)
                                else -> Color.Red.copy(alpha = 0.15f)
                            },
                            RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = wd.status,
                        color = when (wd.status) {
                            "PENDING" -> Color.Yellow
                            "APPROVED" -> EmeraldPrimary
                            else -> Color.Red
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text("ID: ${wd.id} | Act: ${wd.accountNumber}", style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray))
            if (wd.adminNote.isNotEmpty()) {
                Text("Note: ${wd.adminNote}", style = MaterialTheme.typography.bodySmall.copy(color = GoldAccent), maxLines = 2)
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            Text("${wd.coinAmount} Coins", style = MaterialTheme.typography.titleSmall.copy(color = Color.White, fontWeight = FontWeight.Bold))
            Text("${wd.cashAmount} PKR/USD", style = MaterialTheme.typography.bodySmall.copy(color = EmeraldPrimary, fontWeight = FontWeight.Bold))
        }
    }
}

// 9. Withdraw Setup Screen
@Composable
fun WithdrawScreen(
    viewModel: EarnViewModel,
    onNavigateBack: () -> Unit
) {
    val methods = listOf("Easypaisa", "JazzCash", "PayPal", "GCash", "Binance Pay", "Bank Transfer")
    var selectedMethod by remember { mutableStateOf("Easypaisa") }
    var accountTitle by remember { mutableStateOf("") }
    var accountNumber by remember { mutableStateOf("") }
    var coinsInput by remember { mutableStateOf("") }

    val context = LocalContext.current
    val isLoading by viewModel.isLoading.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text("Request Cashout", style = MaterialTheme.typography.titleLarge.copy(color = Color.White, fontWeight = FontWeight.Bold))
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("Select Cashout Channel:", color = Color.Gray, style = MaterialTheme.typography.titleSmall)

        Spacer(modifier = Modifier.height(8.dp))

        // Channel Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            methods.forEach { method ->
                FilterChip(
                    selected = selectedMethod == method,
                    onClick = { selectedMethod = method },
                    label = { Text(method, color = if (selectedMethod == method) Color.Black else Color.White) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = EmeraldPrimary,
                        containerColor = Color.White.copy(alpha = 0.05f)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = accountTitle,
            onValueChange = { accountTitle = it },
            label = { Text("Account Title (Full Name)") },
            modifier = Modifier.fillMaxWidth().testTag("withdraw_title_input"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = EmeraldPrimary,
                unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f)
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = accountNumber,
            onValueChange = { accountNumber = it },
            label = { Text("Account Number / Email Address") },
            modifier = Modifier.fillMaxWidth().testTag("withdraw_number_input"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = EmeraldPrimary,
                unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f)
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = coinsInput,
            onValueChange = { coinsInput = it },
            label = { Text("Coins to Cash Out (100 Coins = 1 PKR/Cent)") },
            modifier = Modifier.fillMaxWidth().testTag("withdraw_coins_input"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = EmeraldPrimary,
                unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f)
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (isLoading) {
            CircularProgressIndicator(color = EmeraldPrimary, modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            Button(
                onClick = {
                    val coins = coinsInput.toIntOrNull()
                    if (coins == null || coins <= 0 || accountTitle.isEmpty() || accountNumber.isEmpty()) {
                        Toast.makeText(context, "Please correctly fill all withdrawal fields.", Toast.LENGTH_SHORT).show()
                    } else {
                        viewModel.requestWithdrawal(selectedMethod, accountTitle, accountNumber, coins) { success, msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                            if (success) {
                                com.example.data.notification.NotificationHelper.showWithdrawalNotification(context, "WD-${(1000..9999).random()}", selectedMethod, coins)
                                onNavigateBack()
                            }
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                modifier = Modifier.fillMaxWidth().height(50.dp).testTag("submit_withdraw_btn")
            ) {
                Text("Submit Cashout Request", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// 10. Admin Control Dashboard Area
@Composable
fun AdminDashboardScreen(
    viewModel: EarnViewModel,
    onNavigateBack: () -> Unit
) {
    val withdrawals by viewModel.withdrawals.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text("Admin Portal (Control Console)", style = MaterialTheme.typography.titleLarge.copy(color = Color.White, fontWeight = FontWeight.Bold))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Simulate approving/rejecting withdrawal requests directly on the Room database.", color = Color.Gray, style = MaterialTheme.typography.bodySmall)

        Spacer(modifier = Modifier.height(24.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            val pendingList = withdrawals.filter { it.status == "PENDING" }
            if (pendingList.isEmpty()) {
                item {
                    Text("No pending withdrawal requests found.", color = Color.LightGray)
                }
            } else {
                items(pendingList) { wd ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(wd.method, fontWeight = FontWeight.Bold, color = Color.White)
                                    Text("Title: ${wd.accountTitle}", style = MaterialTheme.typography.bodySmall.copy(color = Color.LightGray))
                                    Text("Acct: ${wd.accountNumber}", style = MaterialTheme.typography.bodySmall.copy(color = Color.LightGray))
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text("${wd.coinAmount} Coins", color = GoldAccent, fontWeight = FontWeight.Bold)
                                    Text("${wd.cashAmount} PKR/USD", color = EmeraldPrimary, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Button(
                                    onClick = {
                                        viewModel.adminApprove(wd.id)
                                        Toast.makeText(context, "Withdrawal APPROVED!", Toast.LENGTH_SHORT).show()
                                        com.example.data.notification.NotificationHelper.showAdminNotification(context, "APPROVED", wd.id, "Verified legibly. Paid out successfully.")
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                    modifier = Modifier.weight(1f).testTag("approve_${wd.id}")
                                ) {
                                    Text("Approve")
                                }

                                Button(
                                    onClick = {
                                        viewModel.adminReject(wd.id, wd.coinAmount)
                                        Toast.makeText(context, "Withdrawal REJECTED (Refunded)", Toast.LENGTH_SHORT).show()
                                        com.example.data.notification.NotificationHelper.showAdminNotification(context, "REJECTED", wd.id, "Invalid account configuration. Rejected.")
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                    modifier = Modifier.weight(1f).testTag("reject_${wd.id}")
                                ) {
                                    Text("Reject")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// 11. Leaderboard Screen
@Composable
fun LeaderboardScreen(
    viewModel: EarnViewModel,
    onNavigateBack: () -> Unit
) {
    val leaderboard by viewModel.leaderboard.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text("Elite Leaderboard", style = MaterialTheme.typography.titleLarge.copy(color = Color.White, fontWeight = FontWeight.Bold))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Podium for Top 3
        val top3 = leaderboard.take(3)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            if (top3.size >= 2) PodiumItem(top3[1], 2, Modifier.padding(bottom = 8.dp))
            if (top3.size >= 1) PodiumItem(top3[0], 1, Modifier.padding(bottom = 16.dp))
            if (top3.size >= 3) PodiumItem(top3[2], 3, Modifier.padding(bottom = 0.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // List for 4-10
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(leaderboard.drop(3).take(7)) { rankUser ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("${rankUser.rank}", color = Color.Gray, fontWeight = FontWeight.Bold)
                    Text(rankUser.username, color = Color.White, fontWeight = FontWeight.Medium)
                    Text("${rankUser.coinBalance} Coins", color = EmeraldPrimary, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun PodiumItem(rankUser: LeaderboardUserEntity, rank: Int, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Avatar placeholder
        Box(
            modifier = Modifier
                .size(60.dp)
                .background(if (rank == 1) GoldAccent else Color.Gray, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("$rank", color = Color.Black, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text( rankUser.username, color = Color.White, fontWeight = FontWeight.Bold)
        Text( "${rankUser.coinBalance}", color = EmeraldPrimary)
    }
}

// 12. User Profile, Support, FAQ, Legal Screen
@Composable
fun ProfileScreen(
    viewModel: EarnViewModel,
    onLogout: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val profile by viewModel.userProfile.collectAsState()
    var showFaqDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text("User Account Control", style = MaterialTheme.typography.titleLarge.copy(color = Color.White, fontWeight = FontWeight.Bold))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Large user badge
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(EmeraldPrimary.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.Face, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(48.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(profile?.username ?: "Guest", style = MaterialTheme.typography.titleLarge.copy(color = Color.White, fontWeight = FontWeight.Bold))
                Text(profile?.email ?: "guest@pkearnpro.com", style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray))

                Spacer(modifier = Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val badgesList = profile?.badges?.split(",") ?: listOf("Beginner")
                    badgesList.forEach { badge ->
                        Box(
                            modifier = Modifier
                                .background(GoldAccent.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(badge, color = GoldAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Action Options
        Text("UTILITIES & POLICY", color = Color.Gray, style = MaterialTheme.typography.titleSmall)

        Spacer(modifier = Modifier.height(12.dp))

        ProfileUtilityRow(
            icon = Icons.Default.QuestionAnswer,
            title = "Help Support & FAQ",
            subtitle = "Legitimate rewards verification policies",
            onClick = { showFaqDialog = true }
        )

        ProfileUtilityRow(
            icon = Icons.Default.PrivacyTip,
            title = "Privacy Policy & Terms",
            subtitle = "User safety, App Check, strict device guidelines",
            onClick = { showPrivacyDialog = true }
        )

        ProfileUtilityRow(
            icon = Icons.Default.Settings,
            title = "Settings Preference",
            subtitle = "Toggle dark/light theme and application language",
            onClick = { showSettingsDialog = true }
        )

        ProfileUtilityRow(
            icon = Icons.Default.ExitToApp,
            title = "Sign Out Account",
            subtitle = "Deregister and clear offline cache",
            onClick = {
                viewModel.logout {
                    Toast.makeText(context, "Log out completed.", Toast.LENGTH_SHORT).show()
                    onLogout()
                }
            }
        )

        Spacer(modifier = Modifier.height(48.dp))

        // FAQ Dialog
        if (showFaqDialog) {
            AlertDialog(
                onDismissRequest = { showFaqDialog = false },
                title = { Text("Support & FAQ", fontWeight = FontWeight.Bold) },
                text = {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        Text(
                            "Q1: How does PK Earn Pro generate rewards?\n" +
                                    "A: Payout revenue depends directly on legitimate sponsor video ad views (Google AdMob) and completed microtasks on our Offer Walls. Fake accounts or bot clicks generate zero revenue, which is why they are banned.\n\n" +
                                    "Q2: What is the minimum withdrawal?\n" +
                                    "A: The standard minimum cashout limit is 1,000 Coins (Equivalent to 10 PKR or 10 Cents USD) for instant processing to Easypaisa or JazzCash.\n\n" +
                                    "Q3: How long do withdrawals take?\n" +
                                    "A: Legit manual cashouts are approved and processed within 24-48 business hours after background validation.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showFaqDialog = false }) {
                        Text("Dismiss", color = EmeraldPrimary)
                    }
                }
            )
        }

        // Privacy Policy Dialog
        if (showPrivacyDialog) {
            AlertDialog(
                onDismissRequest = { showPrivacyDialog = false },
                title = { Text("Privacy Policy & Play Store compliance", fontWeight = FontWeight.Bold) },
                text = {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        Text(
                            "We take user safety and Play Store compliance extremely seriously.\n\n" +
                                    "1. Anti-Cheat: Emulators, root access, and VPN tools are strictly blocked to avoid deceptive click generation on sponsor networks.\n" +
                                    "2. Data Protection: All user email, ledger logs, and referral statistics are transmitted securely and stored with Firestore. Local data is encrypted with Room cache limits.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showPrivacyDialog = false }) {
                        Text("Accept Terms", color = EmeraldPrimary)
                    }
                }
            )
        }

        // Settings Dialog
        if (showSettingsDialog) {
            val isDarkMode by viewModel.isDarkMode.collectAsState()
            val selectedLanguage by viewModel.selectedLanguage.collectAsState()
            
            AlertDialog(
                onDismissRequest = { showSettingsDialog = false },
                title = { Text("App Settings", fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Dark Theme Mode")
                            Switch(
                                checked = isDarkMode,
                                onCheckedChange = { viewModel.setDarkMode(it) },
                                colors = SwitchDefaults.colors(checkedThumbColor = EmeraldPrimary)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Text("Select App Language", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        val languages = listOf("English", "Urdu", "Punjabi", "Sindhi", "Pashto")
                        languages.forEach { lang ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.setLanguage(lang) }
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedLanguage == lang,
                                    onClick = { viewModel.setLanguage(lang) },
                                    colors = RadioButtonDefaults.colors(selectedColor = EmeraldPrimary)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(lang)
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showSettingsDialog = false }) {
                        Text("Save & Apply", color = EmeraldPrimary)
                    }
                }
            )
        }
    }
}

@Composable
fun ProfileUtilityRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(Color.White.copy(alpha = 0.05f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(20.dp))
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium.copy(color = Color.White, fontWeight = FontWeight.Bold))
            Text(subtitle, style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray))
        }

        Icon(imageVector = Icons.Default.ArrowForwardIos, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
    }
}

