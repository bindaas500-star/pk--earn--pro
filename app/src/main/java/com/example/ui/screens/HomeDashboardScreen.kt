package com.example.ui.screens

import android.widget.Toast
import android.app.Activity
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.*
import com.example.ui.viewmodel.EarnViewModel

@Composable
fun HomeDashboardScreen(
    viewModel: EarnViewModel,
    onNavigate: (String) -> Unit
) {
    val profile by viewModel.userProfile.collectAsState()
    val context = LocalContext.current
    
    if (profile == null) return

    Scaffold(
        containerColor = DarkBg,
        topBar = {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Hello,", style = MaterialTheme.typography.bodyMedium.copy(color = DarkTextSecondary))
                    Text(text = "${profile?.username}", style = MaterialTheme.typography.headlineSmall.copy(color = DarkText, fontWeight = FontWeight.Bold))
                }
                
                // Coin Balance Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, BentoPrimary)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = "💰", style = MaterialTheme.typography.titleMedium)
                        Text(text = "+250", style = MaterialTheme.typography.titleMedium.copy(color = BentoAccent, fontWeight = FontWeight.Bold))
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Daily Check-in card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = BentoPrimary.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(24.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "Daily Check-in", style = MaterialTheme.typography.titleMedium.copy(color = DarkText, fontWeight = FontWeight.Bold))
                            Text(text = "Claim your daily rewards!", style = MaterialTheme.typography.bodyMedium.copy(color = DarkTextSecondary))
                        }
                        Button(
                            onClick = { viewModel.claimDailyCheckIn { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() } },
                            colors = ButtonDefaults.buttonColors(containerColor = BentoPrimary)
                        ) {
                            Text("Claim")
                        }
                    }
                }
            }
            
            // Sponsor Tasks
            item {
                Text(text = "Sponsor Tasks", style = MaterialTheme.typography.titleLarge.copy(color = DarkText, fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(16.dp))
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        TaskCard("Spin Wheel", "Try your luck!", Icons.Default.Refresh, { onNavigate("spin") }, Modifier.weight(1f))
                        TaskCard("Scratch Cards", "Reveal rewards!", Icons.Default.Layers, { onNavigate("scratch") }, Modifier.weight(1f))
                    }
                    TaskCard("Offerwall", "More tasks!", Icons.Default.List, { onNavigate("tasks") }, Modifier.fillMaxWidth())
                }
            }
            
            // Watch Ads & Earn button
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { 
                        viewModel.showRewardedAd(
                            activity = context as Activity, 
                            onMessage = { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
                        )
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BentoAccent),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Watch Ads & Earn", style = MaterialTheme.typography.titleMedium.copy(color = Color.Black, fontWeight = FontWeight.Bold))
                }
            }
        }
    }
}

@Composable
fun TaskCard(title: String, subtitle: String, icon: ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null, tint = BentoAccent, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, style = MaterialTheme.typography.titleMedium.copy(color = DarkText, fontWeight = FontWeight.Bold))
            Text(subtitle, style = MaterialTheme.typography.bodySmall.copy(color = DarkTextSecondary))
        }
    }
}
