package com.example.skinappp.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.skinappp.domain.repository.AuthRepository
import com.example.skinappp.ui.theme.*
import javax.inject.Inject

@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().background(AppBackground).verticalScroll(rememberScrollState())
    ) {
        // Header
        Box(
            modifier = Modifier.fillMaxWidth()
                .background(Brush.linearGradient(listOf(TealPrimary, TealSecondary), Offset(0f, 0f), Offset(Float.POSITIVE_INFINITY, 300f)))
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(Modifier.height(12.dp))
                Box(
                    modifier = Modifier.size(80.dp).clip(CircleShape).background(Color.White.copy(0.25f)),
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Default.Person, null, tint = Color.White, modifier = Modifier.size(46.dp)) }
                Spacer(Modifier.height(12.dp))
                Text(uiState.fullName, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text("Member since May 2026", color = Color.White.copy(0.8f), fontSize = 13.sp)
                Spacer(Modifier.height(12.dp))
            }
        }

        Spacer(Modifier.height(20.dp))

        // Info section
        ProfileSection(title = null) {
            ProfileInfoRow(Icons.Default.Person, "Full Name", uiState.fullName)
            HorizontalDivider(modifier = Modifier.padding(start = 52.dp), color = BorderSoft)
            ProfileInfoRow(Icons.Default.Phone, "Phone Number", "+91 98765 43210")
        }

        Spacer(Modifier.height(16.dp))

        // Medical context
        ProfileSection(title = "Medical Context") {
            ProfileInfoRow(Icons.Default.WaterDrop, "Skin Type", "Combination / Sensitive")
            HorizontalDivider(modifier = Modifier.padding(start = 52.dp), color = BorderSoft)
            ProfileInfoRow(Icons.Default.Info, "Known Allergies", "Benzoyl Peroxide, Fragrance")
        }

        Spacer(Modifier.height(16.dp))

        // Settings
        ProfileSection(title = "Settings") {
            ProfileInfoRow(Icons.Default.Notifications, "Push Notifications", "Enabled")
            HorizontalDivider(modifier = Modifier.padding(start = 52.dp), color = BorderSoft)
            ProfileInfoRow(Icons.Default.Security, "Account Status", "Verified")
        }

        Spacer(Modifier.height(24.dp))

        OutlinedButton(
            onClick = { showLogoutDialog = true },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
            border = ButtonDefaults.outlinedButtonBorder
        ) {
            Icon(Icons.Default.Logout, null)
            Spacer(Modifier.width(8.dp))
            Text("Sign Out", fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(32.dp))
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Sign Out") },
            text = { Text("Are you sure you want to sign out?") },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    onLogout()
                    viewModel.logout()
                }) { Text("Sign Out", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun ProfileSection(title: String?, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        title?.let {
            Text(it, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary, modifier = Modifier.padding(bottom = 10.dp))
        }
        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp)) {
            Column(modifier = Modifier.padding(vertical = 4.dp)) { content() }
        }
    }
}

@Composable
fun ProfileInfoRow(icon: ImageVector, label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = TealPrimary, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(14.dp))
        Column {
            Text(label, color = TextSecondary, fontSize = 11.sp)
            Text(value, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Medium)
        }
    }
}
