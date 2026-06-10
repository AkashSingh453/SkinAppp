package com.example.skinappp.ui.appointments

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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.skinappp.data.dto.AppointmentResponse
import com.example.skinappp.ui.theme.*
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun AppointmentDetailScreen(
    appointmentId: String,
    onNavigateBack: () -> Unit,
    onReschedule: (String) -> Unit,
    onAnalyzePrescription: (String) -> Unit,
    viewModel: AppointmentViewModel = hiltViewModel()
) {
    val selectedAppointment by viewModel.selectedAppointment.collectAsState()
    val cancelledState by viewModel.cancelledAppointment.collectAsState()
    val appointmentsState by viewModel.myAppointments.collectAsState()

    LaunchedEffect(cancelledState) {
        // If cancelled successfully, the ViewModel will reload the list.
        // We could show a toast here. For now, the UI will reflect the cancelled status when re-selected.
    }

    if (appointmentsState is com.example.skinappp.util.Resource.Loading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = TealPrimary)
        }
        return
    }

    val appointments = (appointmentsState as? com.example.skinappp.util.Resource.Success)?.data ?: emptyList()
    val appointment = selectedAppointment ?: appointments.find { it.appointmentId == appointmentId }

    if (appointment == null || appointment.appointmentId != appointmentId) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Appointment details not available.", color = TextSecondary)
        }
        return
    }
    
    val dateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy")
    val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a")
    val instant = runCatching { Instant.parse(appointment.appointmentTime) }.getOrNull()
    val zone = ZoneId.systemDefault()
    val dateStr = instant?.atZone(zone)?.format(dateFormatter) ?: "—"
    val timeStr = instant?.atZone(zone)?.format(timeFormatter) ?: "—"

    val (statusColor, statusIcon, statusText) = when (appointment.status) {
        "CONFIRMED" -> Triple(SuccessGreen, Icons.Default.CheckCircle, "Confirmed")
        "PENDING" -> Triple(Color(0xFF856404), Icons.Default.Pending, "Awaiting Confirmation")
        "CANCELLED" -> Triple(MaterialTheme.colorScheme.error, Icons.Default.Cancel, "Cancelled")
        "COMPLETED" -> Triple(TealPrimary, Icons.Default.DoneAll, "Completed")
        else -> Triple(TextSecondary, Icons.Default.Info, appointment.status)
    }

    val (badgeBgColor, badgeTextColor) = when (appointment.status) {
        "CONFIRMED" -> SuccessSoft to SuccessGreen
        "PENDING" -> WarningSoft to Color(0xFF856404)
        "CANCELLED" -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.error
        "COMPLETED" -> TealSoft to TealPrimary
        else -> BorderSoft to TextSecondary
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(listOf(TealPrimary, TealSecondary), Offset(0f, 0f), Offset(Float.POSITIVE_INFINITY, 200f)))
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            Column {
                IconButton(onClick = onNavigateBack, modifier = Modifier.padding(bottom = 8.dp)) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Text("Appointment Details", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                Text("ID: ${appointment.appointmentId.take(8)}...", color = Color.White.copy(0.85f), fontSize = 13.sp)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Status Banner
        Surface(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            color = badgeBgColor,
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(statusIcon, contentDescription = null, tint = badgeTextColor, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text(statusText, color = badgeTextColor, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Doctor & Schedule Info
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Doctor Row
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(52.dp).clip(CircleShape).background(TealSoft),
                        contentAlignment = Alignment.Center
                    ) { 
                        Icon(Icons.Default.Person, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(28.dp)) 
                    }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text("Doctor", color = TextSecondary, fontSize = 12.sp)
                        Text("Dr. ${appointment.doctorId.take(8)}...", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 18.sp)
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = BorderSoft)

                // Date & Time Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Date", color = TextSecondary, fontSize = 12.sp)
                        Spacer(Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CalendarToday, null, tint = TealPrimary, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(dateStr, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        }
                    }
                    Column {
                        Text("Time", color = TextSecondary, fontSize = 12.sp)
                        Spacer(Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Schedule, null, tint = TealPrimary, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(timeStr, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        }
                    }
                }
                
                // AI Scan Attached
                appointment.associatedAnalysisId?.let {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = BorderSoft)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ContentPasteSearch, null, tint = TealPrimary, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("AI Scan Attached", fontWeight = FontWeight.SemiBold, color = TextPrimary, fontSize = 14.sp)
                            Text("ID: $it", color = TextSecondary, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Prescription Section (Only if COMPLETED and has prescription)
        if (appointment.status == "COMPLETED" && (appointment.prescriptionImageUrl != null || appointment.prescriptionText != null)) {
            Spacer(modifier = Modifier.height(20.dp))
            Text("Prescription", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary, modifier = Modifier.padding(horizontal = 20.dp))
            Spacer(modifier = Modifier.height(12.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    appointment.prescriptionImageUrl?.let { imageUrl ->
                        // Determine the full URL. Assuming backend is on localhost/ip config. 
                        // You'll likely need to use the actual base URL here.
                        val baseUrl = "http://192.168.31.81:8080/"
                        val fullUrl = if (imageUrl.startsWith("http")) imageUrl else "$baseUrl$imageUrl"
                        
                        AsyncImage(
                            model = fullUrl,
                            contentDescription = "Prescription Image",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.LightGray),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(Modifier.height(12.dp))
                    }
                    
                    appointment.prescriptionText?.let { text ->
                        Text("Doctor's Notes:", color = TextSecondary, fontSize = 13.sp)
                        Spacer(Modifier.height(4.dp))
                        Text(text, color = TextPrimary, fontSize = 15.sp)
                    }
                    
                    Spacer(Modifier.height(16.dp))
                    
                    Button(
                        onClick = { onAnalyzePrescription(appointment.appointmentId) },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White)
                        Spacer(Modifier.width(8.dp))
                        Text("Analyze & Set Reminders", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Action Buttons (For PENDING / CONFIRMED)
        if (appointment.status == "PENDING" || appointment.status == "CONFIRMED") {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = { viewModel.cancelAppointment(appointment.appointmentId) },
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = Brush.linearGradient(listOf(MaterialTheme.colorScheme.error, MaterialTheme.colorScheme.error)))
                ) {
                    Text("Cancel", fontWeight = FontWeight.Bold)
                }
                
                Button(
                    onClick = { onReschedule(appointment.doctorId) },
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                ) {
                    Text("Reschedule", fontWeight = FontWeight.Bold)
                }
            }
            
            if (appointment.status == "CONFIRMED") {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { /* TODO: Add standard local notification reminder for the appointment */ },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TealSecondary)
                ) {
                    Icon(Icons.Default.Alarm, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Remind Me", fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
