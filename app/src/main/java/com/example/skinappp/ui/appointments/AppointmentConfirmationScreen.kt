package com.example.skinappp.ui.appointments

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.skinappp.ui.theme.*
import com.example.skinappp.util.Resource

@Composable
fun AppointmentConfirmationScreen(
    appointmentId: String,
    onGoHome: () -> Unit,
    viewModel: AppointmentViewModel = hiltViewModel()
) {
    // Collect the state directly from the new ViewModel
    val bookedAppointmentState by viewModel.bookedAppointment.collectAsState()

    // Extract the actual appointment data if the state is Success
    // (Assuming your Resource class uses `val data: T` for the success payload)
    val appointment = (bookedAppointmentState as? Resource.Success)?.data

    // Pulse animation for the success icon
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.08f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
        label = "scale"
    )

    Box(
        modifier = Modifier.fillMaxSize()
            .background(Brush.linearGradient(listOf(TealPrimary, TealSecondary), Offset(0f, 0f), Offset(0f, Float.POSITIVE_INFINITY))),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
            // Animated success circle
            Box(
                modifier = Modifier.scale(scale).size(110.dp).clip(CircleShape).background(Color.White.copy(0.25f)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier.size(80.dp).clip(CircleShape).background(Color.White),
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Default.Check, null, tint = TealPrimary, modifier = Modifier.size(46.dp)) }
            }

            Spacer(Modifier.height(32.dp))
            Text("Appointment Confirmed!", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Text("Your appointment has been successfully booked.", color = Color.White.copy(0.85f), fontSize = 14.sp, textAlign = TextAlign.Center)

            Spacer(Modifier.height(32.dp))

            // Details card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("Booking Details", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 17.sp)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                    DetailRow("Appointment ID", appointmentId.take(12) + "...")

                    // Will render if the appointment was successfully fetched
                    appointment?.let {
                        DetailRow("Status", it.status)
                        DetailRow("Doctor ID", it.doctorId.take(12) + "...")
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
            Button(
                onClick = onGoHome,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White)
            ) { Text("Back to Home", color = TealPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp) }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = TextSecondary, fontSize = 14.sp)
        Text(value, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
    }
}