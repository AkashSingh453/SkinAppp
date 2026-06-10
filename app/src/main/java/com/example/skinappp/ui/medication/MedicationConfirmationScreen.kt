package com.example.skinappp.ui.medication

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.skinappp.data.dto.MedicationPlanDto
import com.example.skinappp.data.dto.PrescriptionAnalysisResponse
import com.example.skinappp.ui.theme.*
import com.example.skinappp.util.Resource
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MedicationConfirmationScreen(
    appointmentId: String,
    onNavigateBack: () -> Unit,
    onSchedulingComplete: () -> Unit,
    viewModel: MedicationViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val analysisState by viewModel.analysisResult.collectAsState()
    val schedulingState by viewModel.schedulingResult.collectAsState()

    // Permissions and Dialog States
    val notificationPermissionState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)
    } else null

    var showExactAlarmDialog by remember { mutableStateOf(false) }
    var showBatteryOptimizationDialog by remember { mutableStateOf(false) }
    
    val alarmManager = remember { context.getSystemService(Context.ALARM_SERVICE) as AlarmManager }
    val powerManager = remember { context.getSystemService(Context.POWER_SERVICE) as PowerManager }

    LaunchedEffect(appointmentId) {
        if (analysisState == null) {
            viewModel.analyzePrescription(appointmentId)
        }
    }

    LaunchedEffect(schedulingState) {
        when (schedulingState) {
            is Resource.Success -> {
                Toast.makeText(context, "Reminders scheduled!", Toast.LENGTH_SHORT).show()
                viewModel.clearState()
                onSchedulingComplete()
            }
            is Resource.Error -> {
                Toast.makeText(context, "Failed to schedule", Toast.LENGTH_SHORT).show()
            }
            else -> {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        // Simple Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(TealPrimary)
                .padding(horizontal = 16.dp, vertical = 20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Review Medication Plan",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            when (val state = analysisState) {
                is Resource.Loading -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = TealPrimary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("AI is analyzing prescription...", color = TextSecondary)
                    }
                }
                is Resource.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Failed to analyze.", color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.analyzePrescription(appointmentId) }) {
                            Text("Retry")
                        }
                    }
                }
                is Resource.Success -> {
                    val response = state.data
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(20.dp)
                    ) {
                        Text("Summary", fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text(response.summary, color = TextSecondary, fontSize = 14.sp)
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        response.medications.forEach { med ->
                            MedicationPlanCard(med)
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        Button(
                            onClick = { 
                                if (notificationPermissionState != null && !notificationPermissionState.status.isGranted) {
                                    notificationPermissionState.launchPermissionRequest()
                                    return@Button
                                }
                                
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                                    showExactAlarmDialog = true
                                    return@Button
                                }
                                
                                if (!powerManager.isIgnoringBatteryOptimizations(context.packageName)) {
                                    showBatteryOptimizationDialog = true
                                    return@Button
                                }
                                
                                viewModel.confirmAndSchedule(response) 
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                        ) {
                            if (schedulingState is Resource.Loading) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            } else {
                                Icon(Icons.Default.CheckCircle, null, tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Confirm & Set Alarms", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                null -> {}
            }
        }
        
        if (showExactAlarmDialog) {
            AlertDialog(
                onDismissRequest = { showExactAlarmDialog = false },
                title = { Text("Exact Alarms Required") },
                text = { Text("To ensure you never miss a medication, we need permission to set exact alarms.") },
                confirmButton = {
                    TextButton(onClick = {
                        showExactAlarmDialog = false
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                            context.startActivity(intent)
                        }
                    }) { Text("Open Settings") }
                },
                dismissButton = {
                    TextButton(onClick = { showExactAlarmDialog = false }) { Text("Cancel") }
                }
            )
        }

        if (showBatteryOptimizationDialog) {
            AlertDialog(
                onDismissRequest = { showBatteryOptimizationDialog = false },
                title = { Text("Battery Optimization") },
                text = { Text("Your phone's battery saver might kill medication alarms. Please exempt this app from battery optimization.") },
                confirmButton = {
                    TextButton(onClick = {
                        showBatteryOptimizationDialog = false
                        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                        intent.data = Uri.parse("package:${context.packageName}")
                        context.startActivity(intent)
                    }) { Text("Allow") }
                },
                dismissButton = {
                    TextButton(onClick = { showBatteryOptimizationDialog = false }) { Text("Skip") }
                }
            )
        }
    }
}

@Composable
fun MedicationPlanCard(med: MedicationPlanDto) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(med.medicationName, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Dosage", fontSize = 12.sp, color = TextSecondary)
                    Text(med.dosage, fontSize = 15.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                }
                Column {
                    Text("Duration", fontSize = 12.sp, color = TextSecondary)
                    Text("${med.durationInDays} days", fontSize = 15.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = BorderSoft)
            Spacer(modifier = Modifier.height(12.dp))
            
            Text("Times (${med.frequencyPerDay}x/day)", fontSize = 12.sp, color = TextSecondary)
            Text(med.specificTimes.joinToString(", "), fontSize = 15.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
            
            if (med.instructions.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Instructions: ${med.instructions}", fontSize = 13.sp, color = TealPrimary)
            }
        }
    }
}
