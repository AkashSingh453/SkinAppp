package com.example.skinappp.ui.appointments

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.skinappp.data.dto.AppointmentResponse
import com.example.skinappp.ui.theme.*
import com.example.skinappp.util.Resource
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun MyAppointmentsScreen(
    viewModel: AppointmentViewModel = hiltViewModel(),
    onAppointmentClick: (String) -> Unit
) {
    val myAppointmentsState by viewModel.myAppointments.collectAsState()

    // Unpack the Resource cleanly
    val isLoading = myAppointmentsState is Resource.Loading
    val apiError = (myAppointmentsState as? Resource.Error)?.exception?.message
    val appointments = (myAppointmentsState as? Resource.Success)?.data ?: emptyList()

    var selectedTab by remember { mutableIntStateOf(0) }

    val upcoming = appointments.filter { it.status == "CONFIRMED" || it.status == "PENDING" }
    val past = appointments.filter { it.status == "COMPLETED" || it.status == "CANCELLED" }

    Column(modifier = Modifier.fillMaxSize().background(AppBackground)) {
        // Header
        Box(
            modifier = Modifier.fillMaxWidth()
                .background(Brush.linearGradient(listOf(TealPrimary, TealSecondary), Offset(0f, 0f), Offset(Float.POSITIVE_INFINITY, 200f)))
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            Column {
                Text("My Appointments", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                Text("Track your consultations", color = Color.White.copy(0.85f), fontSize = 13.sp)
            }
        }

        Spacer(Modifier.height(16.dp))

        // Tab selector
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Row(modifier = Modifier.padding(4.dp)) {
                listOf("Upcoming", "Past").forEachIndexed { index, label ->
                    Button(
                        onClick = { selectedTab = index },
                        modifier = Modifier.weight(1f).height(42.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedTab == index) TealPrimary else Color.Transparent,
                            contentColor = if (selectedTab == index) Color.White else TextSecondary
                        ),
                        elevation = ButtonDefaults.buttonElevation(0.dp)
                    ) { Text(label, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // Error display
        if (apiError != null) {
            Surface(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                color = MaterialTheme.colorScheme.errorContainer,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(apiError, modifier = Modifier.padding(14.dp), color = MaterialTheme.colorScheme.onErrorContainer, fontSize = 13.sp)
            }
            Spacer(Modifier.height(12.dp))
        }

        val displayList = if (selectedTab == 0) upcoming else past

        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = TealPrimary)
            }
        } else if (displayList.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.CalendarToday, null, tint = BorderSoft, modifier = Modifier.size(56.dp))
                    Spacer(Modifier.height(12.dp))
                    Text("No ${if (selectedTab == 0) "upcoming" else "past"} appointments", color = TextSecondary, fontSize = 16.sp)
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(displayList) { appointment ->
                    AppointmentCard(
                        appointment = appointment,
                        onClick = { 
                            viewModel.selectAppointment(appointment)
                            onAppointmentClick(appointment.appointmentId)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AppointmentCard(
    appointment: AppointmentResponse,
    onClick: () -> Unit
) {
    val dateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy")
    val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a")
    val instant = runCatching { Instant.parse(appointment.appointmentTime) }.getOrNull()
    val zone = ZoneId.systemDefault()
    val dateStr = instant?.atZone(zone)?.format(dateFormatter) ?: "—"
    val timeStr = instant?.atZone(zone)?.format(timeFormatter) ?: "—"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(3.dp),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Doctor row
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(46.dp).clip(CircleShape).background(TealSoft),
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Default.Person, null, tint = TealPrimary, modifier = Modifier.size(24.dp)) }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Dr. ${appointment.doctorId.take(10)}", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 16.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Schedule, null, tint = TextSecondary, modifier = Modifier.size(13.dp))
                        Text("  $timeStr", color = TextSecondary, fontSize = 13.sp)
                    }
                }
                val (badgeColor, badgeText) = when (appointment.status) {
                    "CONFIRMED" -> SuccessSoft to "CONFIRMED"
                    "PENDING" -> WarningSoft to "PENDING"
                    "CANCELLED" -> MaterialTheme.colorScheme.errorContainer to "CANCELLED"
                    else -> BorderSoft to appointment.status
                }
                val textColor = when (appointment.status) {
                    "CONFIRMED" -> SuccessGreen
                    "PENDING" -> Color(0xFF856404)
                    "CANCELLED" -> MaterialTheme.colorScheme.error
                    else -> TextSecondary
                }
                Surface(color = badgeColor, shape = RoundedCornerShape(20.dp)) {
                    Text(badgeText, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), color = textColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = BorderSoft)

            // Date row
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CalendarToday, null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(dateStr, color = TextSecondary, fontSize = 14.sp)
            }

            // AI scan attached
            appointment.associatedAnalysisId?.let {
                Spacer(Modifier.height(10.dp))
                HorizontalDivider(color = BorderSoft)
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ContentPasteSearch, null, tint = TealPrimary, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text("AI Scan Attached", fontWeight = FontWeight.SemiBold, color = TextPrimary, fontSize = 13.sp)
                        Text("Skin Analysis", color = TextSecondary, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}