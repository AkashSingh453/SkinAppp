package com.example.skinappp.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Person
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
import com.example.skinappp.ui.appointments.AppointmentViewModel
import com.example.skinappp.ui.theme.*
import com.example.skinappp.util.Resource
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun HomeScreen(
    onStartAnalysis: () -> Unit,
    onViewAllAppointments: () -> Unit,
    onAppointmentClick: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
    appointmentViewModel: AppointmentViewModel = hiltViewModel()
) {
    val userState by viewModel.userState.collectAsState()
    val myAppointments by appointmentViewModel.myAppointments.collectAsState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .verticalScroll(rememberScrollState())
    ) {
        // Gradient header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(TealPrimary, TealSecondary),
                        Offset(0f, 0f), Offset(Float.POSITIVE_INFINITY, 400f)
                    )
                )
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) { Icon(Icons.Default.Person, null, tint = Color.White, modifier = Modifier.size(28.dp)) }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Hello,", color = Color.White.copy(0.85f), fontSize = 14.sp)
                        Text(userState.userName, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.height(20.dp))

                // Analysis CTA card
                Card(
                    modifier = Modifier.fillMaxWidth().clickable(onClick = onStartAnalysis),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.18f)),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Start Skin Analysis", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text("AI-powered diagnosis in seconds", color = Color.White.copy(0.85f), fontSize = 13.sp)
                        }
                        Box(
                            modifier = Modifier.size(52.dp).clip(CircleShape).background(Color.White.copy(0.25f)),
                            contentAlignment = Alignment.Center
                        ) { Icon(Icons.Default.CameraAlt, null, tint = Color.White, modifier = Modifier.size(26.dp)) }
                    }
                }
            }
        }

        // Upcoming appointments
        Spacer(Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Upcoming Appointments", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
            TextButton(onClick = onViewAllAppointments) {
                Text("View All", color = TealPrimary, fontWeight = FontWeight.SemiBold)
            }
        }
        Spacer(Modifier.height(8.dp))
        when( myAppointments ){
            is Resource.Loading ->{
                Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = TealPrimary)
                }
            }
            is Resource.Success -> {
                if( (myAppointments as Resource.Success<List<AppointmentResponse>>).data.isEmpty()){
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("No upcoming appointments", color = TextSecondary, fontSize = 14.sp)
                        }
                    }
                }else{
                    (myAppointments as Resource.Success<List<AppointmentResponse>>).data.forEach { appointment ->
                        HomeAppointmentCard(
                            appointment = appointment, 
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
                            onClick = {
                                appointmentViewModel.selectAppointment(appointment)
                                onAppointmentClick(appointment.appointmentId)
                            }
                        )
                    }
                }

            }
            else -> {
                Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("failed To Fetch Your Appointment", color = TextSecondary, fontSize = 14.sp)
                }
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
fun HomeAppointmentCard(
    appointment: AppointmentResponse, 
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val formatter = DateTimeFormatter.ofPattern("MMM d, yyyy")
    val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a")
    val instant = runCatching { Instant.parse(appointment.appointmentTime) }.getOrNull()
    val dateStr = instant?.atZone(ZoneId.systemDefault())?.format(formatter) ?: appointment.appointmentTime
    val timeStr = instant?.atZone(ZoneId.systemDefault())?.format(timeFormatter) ?: ""

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        onClick = onClick
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(42.dp).clip(CircleShape).background(TealSoft),
                contentAlignment = Alignment.Center
            ) { Icon(Icons.Default.Person, null, tint = TealPrimary, modifier = Modifier.size(22.dp)) }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Dr. ${appointment.doctorId.take(8)}", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 15.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarToday, null, tint = TextSecondary, modifier = Modifier.size(13.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("$dateStr  $timeStr", color = TextSecondary, fontSize = 12.sp)
                }
            }
            Surface(color = SuccessSoft, shape = RoundedCornerShape(20.dp)) {
                Text("Confirmed", modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), color = SuccessGreen, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
